package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

enum class WishlistCategory(val label: String) {
    Clothing("Pakaian"),
    Shoes("Sepatu"),
    Accessories("Aksesori"),
}

enum class WishlistPriority(val label: String) {
    High("Tinggi"),
    Medium("Sedang"),
    Low("Rendah"),
}

data class WishlistFormData(
    val name: String,
    val category: WishlistCategory,
    val estimatedPrice: Long? = null,
    val store: String? = null,
    val productUrl: String? = null,
    val priority: WishlistPriority = WishlistPriority.Medium,
    val imageModel: String? = null,

)

@Composable
fun WishlistFormScreen(
    initialData: WishlistFormData?,
    onSave: (WishlistFormData) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditing = initialData != null

    var name by rememberSaveable(initialData) {
        mutableStateOf(initialData?.name.orEmpty())
    }
    var category by rememberSaveable(initialData) {
        mutableStateOf(initialData?.category)
    }
    var priceInput by rememberSaveable(initialData) {
        mutableStateOf(initialData?.estimatedPrice?.toString().orEmpty())
    }
    var store by rememberSaveable(initialData) {
        mutableStateOf(initialData?.store.orEmpty())
    }
    var productUrl by rememberSaveable(initialData) {
        mutableStateOf(initialData?.productUrl.orEmpty())
    }
    var priority by rememberSaveable(initialData) {
        mutableStateOf(initialData?.priority ?: WishlistPriority.Medium)
    }
    var imageModel by rememberSaveable(initialData) {
        mutableStateOf(initialData?.imageModel)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            imageModel = uri.toString()
        }
    }

    val trimmedPrice = priceInput.trim()
    val estimatedPrice = trimmedPrice.toLongOrNull()
    val isPriceInvalid = trimmedPrice.isNotEmpty() &&
            (estimatedPrice == null || estimatedPrice < 0L)

    val canSave = name.isNotBlank() && category != null && !isPriceInvalid

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ClosetlySpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        Text(
            text = if (isEditing) "Edit barang" else "Tambah barang",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = if (isEditing) {
                "Perbarui detail dan prioritas incaranmu di Wishlist."
            } else {
                "Catat incaran baru untuk Wishlist-mu."
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "* Wajib diisi. Informasi lain bisa ditambahkan nanti.",
            style = MaterialTheme.typography.bodySmall,
        )

        ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Foto barang · Opsional",
                style = MaterialTheme.typography.titleSmall,
            )

            val selectedPhoto = imageModel

            if (selectedPhoto != null) {
                AsyncImage(
                    model = selectedPhoto,
                    contentDescription = "Foto barang yang dipilih",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = "Simpan foto sebagai referensi barangmu.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            TextButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        ),
                    )
                },
            ) {
                Text(if (selectedPhoto == null) "Pilih dari galeri" else "Ganti foto")
            }

            if (selectedPhoto != null) {
                TextButton(onClick = { imageModel = null }) {
                    Text("Hapus foto")
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama barang *") },
            placeholder = { Text("Contoh: Jaket denim biru") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Kategori *", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs)) {
            WishlistCategory.entries.forEach { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { category = option },
                    label = { Text(option.label) },
                )
            }
        }

        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Perkiraan harga · Opsional") },
            prefix = { Text("Rp ") },
            placeholder = { Text("499000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isPriceInvalid,
            supportingText = {
                if (isPriceInvalid) {
                    Text("Masukkan angka utuh 0 atau lebih, tanpa titik.")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = store,
            onValueChange = { store = it },
            label = { Text("Nama toko · Opsional") },
            placeholder = { Text("Contoh: Erigo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = productUrl,
            onValueChange = { productUrl = it },
            label = { Text("Tautan produk · Opsional") },
            placeholder = { Text("https://") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Prioritas pembelian *", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs)) {
            WishlistPriority.entries.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { priority = option },
                    label = { Text(option.label) },
                )
            }
        }

        ClosetlyButton(
            text = if (isEditing) "Simpan perubahan" else "Simpan ke Wishlist",
            enabled = canSave,
            onClick = {
                val selectedCategory = category
                if (canSave && selectedCategory != null) {
                    onSave(
                        WishlistFormData(
                            name = name.trim(),
                            category = selectedCategory,
                            estimatedPrice = estimatedPrice,
                            store = store.trim().takeIf { it.isNotEmpty() },
                            productUrl = productUrl.trim().takeIf { it.isNotEmpty() },
                            priority = priority,
                            imageModel = imageModel,
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Batal, kembali ke Wishlist")
        }
    }
}

@Preview(name = "Tambah barang", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun AddWishlistFormPreview() {
    WishlistFormDemo(initialData = null)
}

@Preview(name = "Edit barang", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun EditWishlistFormPreview() {
    WishlistFormDemo(
        initialData = WishlistFormData(
            name = "Jaket denim biru",
            category = WishlistCategory.Clothing,
            estimatedPrice = 499_000L,
            store = "Erigo",
            productUrl = "https://example.com/jaket",
            priority = WishlistPriority.High,
        ),
    )
}

@Composable
private fun WishlistFormDemo(initialData: WishlistFormData?) {
    var result by rememberSaveable { mutableStateOf<String?>(null) }

    ClosetlyTheme {
        val message = result

        if (message == null) {
            WishlistFormScreen(
                initialData = initialData,
                onSave = { data ->
                    result = "Diterima dari form: ${data.name} · " +
                            "${data.category.label} · Prioritas ${data.priority.label}"
                },
                onCancel = { result = "Form dibatalkan." },
            )
        } else {
            Column(
                modifier = Modifier.padding(ClosetlySpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
            ) {
                Text(message)
                ClosetlyButton(
                    text = "Coba lagi",
                    onClick = { result = null },
                )
            }
        }
    }
}