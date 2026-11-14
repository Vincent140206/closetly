package vin.pam.closetly.feature.wardrobe

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.get
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRepository
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.PhotoAdjustments
import vin.pam.closetly.feature.wardrobe.model.toDraft
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoProcessor
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoStore
import vin.pam.closetly.feature.wardrobe.photo.WhiteBalance
import vin.pam.closetly.feature.wardrobe.ui.PhotoEditorState
import vin.pam.closetly.feature.wardrobe.ui.WardrobeFormUiState

class WardrobeFormViewModel(
    private val repository: WardrobeRepository,
    private val photoStore: WardrobePhotoStore,
    private val processor: WardrobePhotoProcessor,
    private val itemId: String?,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val restored = savedState.get<String>("draft")?.let { Json.decodeFromString<ClothingDraft>(it) }
    private val state = MutableStateFlow(WardrobeFormUiState(
        draft = restored ?: ClothingDraft(), isEditing = itemId != null,
        isLoading = itemId != null, hasChanges = restored != null,
    ))
    val uiState = state.asStateFlow()
    private val requestId = savedState.get<String>("requestId")
        ?: UUID.randomUUID().toString().also { savedState["requestId"] = it }
    private var processJob: Job? = null
    private var mask: FloatArray? = null

    init {
        if (itemId != null) viewModelScope.launch {
            try {
                val item = repository.getItem(itemId)
                state.update { it.copy(draft = restored ?: item.toDraft()) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(errorMessage = error.message ?: "Pakaian gagal dimuat.") }
            } finally {
                state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun edit(draft: ClothingDraft) {
        if (state.value.isSaving) return
        savedState["draft"] = Json.encodeToString(draft)
        state.update { it.copy(draft = draft, hasChanges = true, errorMessage = null) }
    }

    fun createCameraUri(): Uri = photoStore.createCameraUri()
    fun reportPhotoError(message: String) = state.update { it.copy(errorMessage = message) }

    fun importPhoto(uri: Uri) {
        if (state.value.isSaving || state.value.isImporting) return
        viewModelScope.launch {
            state.update { it.copy(isImporting = true, errorMessage = null) }
            try {
                val path = photoStore.importPhoto(uri)
                val bitmap = photoStore.load(path)
                mask = null
                state.update { it.copy(photoEditor = PhotoEditorState(path, bitmap, bitmap)) }
                processPhoto(WhiteBalance(), removeBackground = true)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                reportPhotoError(error.message ?: "Foto tidak dapat dibuka.")
            } finally {
                state.update { it.copy(isImporting = false) }
            }
        }
    }

    fun openPhotoEditor() {
        val draft = state.value.draft
        val original = draft.originalImagePath ?: draft.imagePath ?: return
        viewModelScope.launch {
            state.update { it.copy(isImporting = true, errorMessage = null) }
            try {
                val path = if (original.startsWith("android.resource://")) photoStore.importPhoto(original.toUri()) else original
                val bitmap = photoStore.load(path)
                val preview = draft.imagePath?.let { photoStore.load(it) } ?: bitmap
                val adjustments = draft.photoAdjustments
                mask = null
                state.update { it.copy(photoEditor = PhotoEditorState(
                    originalPath = path, original = bitmap, preview = preview,
                    balance = WhiteBalance(adjustments.redGain, adjustments.greenGain, adjustments.blueGain),
                    backgroundRemoved = adjustments.backgroundRemoved,
                )) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                reportPhotoError(error.message ?: "Foto tidak dapat dibuka.")
            } finally {
                state.update { it.copy(isImporting = false) }
            }
        }
    }

    fun pickNeutral() = state.update { current ->
        current.copy(photoEditor = current.photoEditor?.let { it.copy(isPickingNeutral = !it.isPickingNeutral) })
    }

    fun calibrate(x: Int, y: Int) {
        val editor = state.value.photoEditor ?: return
        try {
            val sample = neutralSample(editor.original, x, y)
            val balance = WhiteBalance.fromNeutralSample(sample[0], sample[1], sample[2])
            processPhoto(balance, editor.backgroundRemoved)
        } catch (error: IllegalArgumentException) {
            state.update { it.copy(photoEditor = editor.copy(errorMessage = error.message, isPickingNeutral = true)) }
        }
    }

    fun resetCalibration() {
        val editor = state.value.photoEditor ?: return
        processPhoto(WhiteBalance(), editor.backgroundRemoved)
    }

    fun toggleBackground() {
        val editor = state.value.photoEditor ?: return
        processPhoto(editor.balance, !editor.backgroundRemoved)
    }

    private fun processPhoto(balance: WhiteBalance, removeBackground: Boolean) {
        val editor = state.value.photoEditor ?: return
        processJob?.cancel()
        state.update { it.copy(photoEditor = editor.copy(isProcessing = true, isPickingNeutral = false, errorMessage = null)) }
        processJob = viewModelScope.launch {
            try {
                if (removeBackground && mask == null) mask = processor.foregroundMask(editor.original)
                val result = processor.render(editor.original, balance, if (removeBackground) mask else null)
                state.update { it.copy(photoEditor = editor.copy(
                    preview = result, balance = balance, backgroundRemoved = removeBackground,
                )) }
            } catch (_: TimeoutCancellationException) {
                state.update { it.copy(photoEditor = editor.copy(
                    errorMessage = "Foto belum selesai diproses. Periksa koneksi internet lalu coba hapus background lagi.",
                )) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(photoEditor = editor.copy(
                    errorMessage = "${error.message ?: "Foto belum berhasil diproses."} Coba lagi untuk menghapus background.",
                )) }
            }
        }
    }

    fun closePhotoEditor() {
        processJob?.cancel()
        state.update { it.copy(photoEditor = null) }
        mask = null
    }

    fun usePhoto() {
        val editor = state.value.photoEditor ?: return
        if (editor.isProcessing) return
        state.update { it.copy(photoEditor = editor.copy(isProcessing = true)) }
        processJob = viewModelScope.launch {
            try {
                val photo = photoStore.persist(editor.originalPath, editor.preview)
                edit(state.value.draft.copy(
                    imagePath = photo.imagePath, originalImagePath = photo.originalPath,
                    photoAdjustments = PhotoAdjustments(
                        editor.balance.red, editor.balance.green, editor.balance.blue, editor.backgroundRemoved,
                    ),
                ))
                state.update { it.copy(photoEditor = null) }
                mask = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(photoEditor = editor.copy(errorMessage = error.message ?: "Foto gagal disimpan.")) }
            }
        }
    }

    fun removePhoto() = edit(state.value.draft.copy(
        imagePath = null, originalImagePath = null, photoAdjustments = PhotoAdjustments(),
    ))

    fun save() {
        if (state.value.isSaving || state.value.isLoading || state.value.isImporting || state.value.photoEditor != null) return
        state.update { it.copy(attemptedSave = true) }
        val draft = try {
            state.value.draft.validated()
        } catch (error: IllegalArgumentException) {
            state.update { it.copy(errorMessage = error.message) }
            return
        }
        state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                if (itemId == null) repository.createItem(requestId, draft) else repository.updateItem(itemId, draft)
                savedState.remove<String>("draft")
                state.update { it.copy(saved = true, hasChanges = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(errorMessage = error.message ?: "Pakaian belum tersimpan. Coba lagi.") }
            } finally {
                state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun neutralSample(bitmap: Bitmap, x: Int, y: Int): IntArray {
        var red = 0
        var green = 0
        var blue = 0
        var count = 0
        for (row in (y - 3).coerceAtLeast(0)..(y + 3).coerceAtMost(bitmap.height - 1)) {
            for (column in (x - 3).coerceAtLeast(0)..(x + 3).coerceAtMost(bitmap.width - 1)) {
                val pixel = bitmap[column, row]
                if ((pixel ushr 24) < 200) continue
                red += (pixel ushr 16) and 255
                green += (pixel ushr 8) and 255
                blue += pixel and 255
                count++
            }
        }
        require(count > 0) { "Pilih area putih/abu yang terlihat pada foto." }
        return intArrayOf(red / count, green / count, blue / count)
    }

    companion object {
        fun factory(repository: WardrobeRepository, store: WardrobePhotoStore, processor: WardrobePhotoProcessor, itemId: String?) =
            viewModelFactory {
                initializer { WardrobeFormViewModel(repository, store, processor, itemId, createSavedStateHandle()) }
            }
    }
}
