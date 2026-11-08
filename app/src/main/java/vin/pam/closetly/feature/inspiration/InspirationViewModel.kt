package vin.pam.closetly.feature.inspiration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.feature.inspiration.data.repository.InMemoryInspirationRepository
import vin.pam.closetly.feature.inspiration.data.repository.InspirationRepository
import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.model.InspirationCollection
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.SavedPost
import vin.pam.closetly.feature.inspiration.model.SortOrder
import vin.pam.closetly.feature.inspiration.ui.DialogState
import vin.pam.closetly.feature.inspiration.ui.InspirationUiState
import vin.pam.closetly.feature.inspiration.ui.SheetState

class InspirationViewModel(
    private val repository: InspirationRepository,
) : ViewModel() {

    private data class Local(
        val query: String = "",
        val sort: SortOrder = SortOrder.RecentlyAdded,
        val sheet: SheetState? = null,
        val dialog: DialogState? = null,
        val message: String? = null,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<InspirationUiState> = combine(
        repository.posts, repository.collections, repository.savedPosts, repository.likedPostIds, local, ::buildState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InspirationUiState())

    private fun buildState(
        posts: List<InspirationPost>,
        collections: List<InspirationCollection>,
        saved: List<SavedPost>,
        liked: Set<String>,
        l: Local,
    ): InspirationUiState {
        val byId = posts.associateBy { it.id }
        val ordered = if (l.sort == SortOrder.RecentlyAdded) {
            saved.sortedByDescending { it.savedAt }
        } else {
            saved.sortedBy { it.savedAt }
        }
        val collectionPosts = ordered.groupBy { it.collectionId }
            .mapValues { (_, items) -> items.mapNotNull { byId[it.postId] } }
        val summaries = collections.map { c ->
            val latest = saved.filter { it.collectionId == c.id }.maxByOrNull { it.savedAt }
            CollectionSummary(c, collectionPosts[c.id].orEmpty().size, latest?.let { byId[it.postId] })
        }
        val query = l.query.trim().lowercase()
        val feed = posts.sortedByDescending { it.createdAt }.filter { p ->
            query.isEmpty() || "${p.authorName} ${p.title} ${p.caption} ${p.tags.joinToString(" ")}".lowercase().contains(query)
        }
        return InspirationUiState(
            isLoading = false, allPosts = posts, feedPosts = feed, query = l.query, sortOrder = l.sort,
            savedPosts = ordered.mapNotNull { byId[it.postId] },
            savedIds = saved.map { it.postId }.toSet(), likedIds = liked,
            collections = summaries, collectionPosts = collectionPosts,
            sheet = l.sheet, dialog = l.dialog, message = l.message,
        )
    }

    // ---- Feed & sorting ----
    fun onQueryChange(value: String) = local.update { it.copy(query = value) }
    fun onSortSelected(order: SortOrder) = local.update { it.copy(sort = order) }

    // ---- Bookmark: save sheet, or remove confirmation if already saved ----
    fun onBookmarkClick(postId: String) {
        val alreadySaved = repository.savedPosts.value.any { it.postId == postId }
        local.update {
            if (alreadySaved) it.copy(dialog = DialogState.RemoveSaved(postId))
            else it.copy(sheet = SheetState.SaveToCollection(postId))
        }
    }

    fun onLikeClick(postId: String) {
        viewModelScope.launch { repository.toggleLike(postId) }
    }

    // ---- Save to Collection sheet ----
    fun onSelectCollection(collectionId: String) = updateSaveSheet { it.copy(selectedCollectionId = collectionId) }
    fun onStartCreateInSheet() = updateSaveSheet { it.copy(isCreating = true) }
    fun onCancelCreateInSheet() = updateSaveSheet { it.copy(isCreating = false) }

    /** Creates the collection and selects it. Does NOT bookmark the post. */
    fun onCreateCollectionInSheet(name: String) {
        val clean = name.trim()
        if (clean.isEmpty() || local.value.sheet !is SheetState.SaveToCollection) return
        viewModelScope.launch {
            val created = repository.createCollection(clean)
            updateSaveSheet { it.copy(selectedCollectionId = created.id, isCreating = false) }
        }
    }

    fun onConfirmSave() {
        val sheet = local.value.sheet as? SheetState.SaveToCollection ?: return
        val collectionId = sheet.selectedCollectionId ?: return
        viewModelScope.launch {
            repository.savePost(sheet.postId, collectionId)
            val name = repository.collections.value.firstOrNull { it.id == collectionId }?.name.orEmpty()
            local.update { it.copy(sheet = null, message = "Tersimpan di $name") }
        }
    }

    private fun updateSaveSheet(transform: (SheetState.SaveToCollection) -> SheetState.SaveToCollection) =
        local.update { l ->
            val s = l.sheet as? SheetState.SaveToCollection ?: return@update l
            l.copy(sheet = transform(s))
        }

    // ---- Create / rename (My Inspiration "+" card and Manage Collections) ----
    fun onAddCollectionClick() = local.update { it.copy(sheet = SheetState.CreateCollection) }

    fun onEditCollectionClick(collectionId: String) {
        val name = repository.collections.value.firstOrNull { it.id == collectionId }?.name ?: return
        local.update { it.copy(sheet = SheetState.RenameCollection(collectionId, name)) }
    }

    fun onSubmitCollectionForm(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        when (val sheet = local.value.sheet) {
            SheetState.CreateCollection -> viewModelScope.launch {
                repository.createCollection(clean)
                local.update { it.copy(sheet = null, message = "Koleksi dibuat") }
            }
            is SheetState.RenameCollection -> viewModelScope.launch {
                repository.renameCollection(sheet.collectionId, clean)
                local.update { it.copy(sheet = null, message = "Koleksi diperbarui") }
            }
            else -> Unit
        }
    }

    fun onDismissSheet() = local.update { it.copy(sheet = null) }

    // ---- Destructive actions always go through a confirmation dialog ----
    fun onDeleteCollectionClick(collectionId: String) =
        local.update { it.copy(dialog = DialogState.DeleteCollection(collectionId)) }

    fun onDismissDialog() = local.update { it.copy(dialog = null) }

    fun onConfirmDialog() {
        val dialog = local.value.dialog ?: return
        viewModelScope.launch {
            val message = when (dialog) {
                is DialogState.RemoveSaved -> {
                    repository.removeSavedPost(dialog.postId)
                    "Dihapus dari inspirasi tersimpan"
                }
                is DialogState.DeleteCollection -> {
                    repository.deleteCollection(dialog.collectionId)
                    "Koleksi dihapus"
                }
            }
            local.update { it.copy(dialog = null, message = message) }
        }
    }

    fun onMessageShown() = local.update { it.copy(message = null) }

    companion object {
        /**
         * Uses the in-memory repository. When the Supabase repository exists, build it from the
         * application container (di/) instead; no screen needs to change.
         */
        val Factory = viewModelFactory {
            initializer { InspirationViewModel(sharedRepository) }
        }
        private val sharedRepository: InspirationRepository by lazy { InMemoryInspirationRepository() }
    }
}