package vin.pam.closetly.feature.inspiration.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.feature.inspiration.InspirationViewModel
import vin.pam.closetly.feature.inspiration.ui.components.ConfirmDialog
import vin.pam.closetly.feature.inspiration.ui.components.InspirationSheet

/*
 * Destination composables used by the central AppNavHost. They take the shared
 * InspirationViewModel plus plain callbacks, never a NavController: the graph owns navigation.
 */

@Composable
fun CommunityDestination(
    viewModel: InspirationViewModel,
    onOpenPost: (postId: String) -> Unit,
    onOpenMyInspiration: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InspirationOverlayHost(state, viewModel, modifier) {
        CommunityScreen(
            posts = state.feedPosts, savedIds = state.savedIds, query = state.query, isLoading = state.isLoading,
            onQueryChange = viewModel::onQueryChange,
            onPostClick = onOpenPost, onBookmarkClick = viewModel::onBookmarkClick,
            onMyInspirationClick = onOpenMyInspiration,
        )
    }
}

@Composable
fun StyleDetailDestination(
    viewModel: InspirationViewModel,
    postId: String,
    onBack: () -> Unit,
    onOpenPost: (postId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val post = state.postById(postId)
    val context = LocalContext.current
    InspirationOverlayHost(state, viewModel, modifier) {
        StyleDetailScreen(
            post = post, related = post?.let(state::relatedTo).orEmpty(),
            savedIds = state.savedIds, likedIds = state.likedIds,
            onBack = onBack, onBookmarkClick = viewModel::onBookmarkClick,
            onLikeClick = viewModel::onLikeClick,
            onShareClick = { shared -> shareText(context, "${shared.title}\n${shared.caption}") },
            onPostClick = onOpenPost,
        )
    }
}

@Composable
fun MyInspirationDestination(
    viewModel: InspirationViewModel,
    onBack: () -> Unit,
    onOpenManage: () -> Unit,
    onOpenCollection: (collectionId: String) -> Unit,
    onOpenPost: (postId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InspirationOverlayHost(state, viewModel, modifier) {
        MyInspirationScreen(
            collections = state.collections, savedPosts = state.savedPosts, savedIds = state.savedIds,
            sortOrder = state.sortOrder, onBack = onBack, onManageClick = onOpenManage,
            onSortSelected = viewModel::onSortSelected, onCollectionClick = onOpenCollection,
            onAddCollectionClick = viewModel::onAddCollectionClick,
            onPostClick = onOpenPost, onBookmarkClick = viewModel::onBookmarkClick,
        )
    }
}

@Composable
fun InspirationCollectionDestination(
    viewModel: InspirationViewModel,
    collectionId: String,
    onBack: () -> Unit,
    onOpenManage: () -> Unit,
    onOpenPost: (postId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.collectionById(collectionId)
    if (summary == null) {
        // The collection was deleted in Manage Collections; this screen is now on top, so leave it.
        LaunchedEffect(Unit) { onBack() }
        return
    }
    InspirationOverlayHost(state, viewModel, modifier) {
        CollectionDetailScreen(
            collection = summary, posts = state.collectionPosts[collectionId].orEmpty(),
            savedIds = state.savedIds, sortOrder = state.sortOrder,
            onBack = onBack, onManageClick = onOpenManage, onSortSelected = viewModel::onSortSelected,
            onPostClick = onOpenPost, onBookmarkClick = viewModel::onBookmarkClick,
        )
    }
}

@Composable
fun ManageCollectionsDestination(
    viewModel: InspirationViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InspirationOverlayHost(state, viewModel, modifier) {
        ManageCollectionsScreen(
            collections = state.collections, onBack = onBack,
            onAddClick = viewModel::onAddCollectionClick, onEditClick = viewModel::onEditCollectionClick,
            onDeleteClick = viewModel::onDeleteCollectionClick,
        )
    }
}

/** Bottom sheet, confirmation dialog and snackbar, rendered above any Inspiration screen. */
@Composable
private fun InspirationOverlayHost(
    state: InspirationUiState,
    viewModel: InspirationViewModel,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }
    Box(modifier.fillMaxSize()) {
        content()
        state.sheet?.let { sheet ->
            InspirationSheet(
                sheet = sheet,
                post = (sheet as? SheetState.SaveToCollection)?.let { state.postById(it.postId) },
                collections = state.collections,
                onDismiss = viewModel::onDismissSheet,
                onSelectCollection = viewModel::onSelectCollection,
                onStartCreateInSheet = viewModel::onStartCreateInSheet,
                onCancelCreateInSheet = viewModel::onCancelCreateInSheet,
                onSubmitCreateInSheet = viewModel::onCreateCollectionInSheet,
                onConfirmSave = viewModel::onConfirmSave,
                onSubmitCollectionForm = viewModel::onSubmitCollectionForm,
            )
        }
        state.dialog?.let { dialog ->
            val isRemove = dialog is DialogState.RemoveSaved
            ConfirmDialog(
                title = if (isRemove) "Hapus Simpanan?" else "Hapus Koleksi?",
                message = if (isRemove) {
                    "Apakah kamu yakin ingin menghapus inspirasi ini dari inspirasi tersimpanmu?"
                } else {
                    "Menghapus koleksi ini juga akan menghapus semua inspirasi di dalamnya secara permanen."
                },
                confirmLabel = "Hapus",
                onConfirm = viewModel::onConfirmDialog,
                onDismiss = viewModel::onDismissDialog,
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

private fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null))
}