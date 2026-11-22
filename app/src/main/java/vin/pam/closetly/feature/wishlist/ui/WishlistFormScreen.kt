package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.serialization.Serializable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding

@Serializable
enum class WishlistCategory(val label: String) {
    Clothing("Pakaian"),
    Shoes("Sepatu"),
    Accessories("Aksesori"),
}

@Serializable
enum class WishlistPriority(val label: String) {
    High("Tinggi"),
    Medium("Sedang"),
    Low("Rendah"),
}

@Serializable
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
    var categoryMenuExpanded by rememberSaveable(initialData) {
        mutableStateOf(false)
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
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(ClosetlySpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        Text(
            text = if (isEditing) {
                "Wishlist / Edit barang"
            } else {
                "Closet › Wishlist"
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                ClosetlySpacing.Sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedIconButton(
                onClick = onCancel,
                shape = CircleShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = if (isEditing) {
                        "Kembali ke detail barang"
                    } else {
                        "Kembali ke Wishlist"
                    },
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = if (isEditing) "Edit barang" else "Tambah barang",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
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

        val selectedPhoto = imageModel

        val onChoosePhoto: () -> Unit = {
            photoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                ),
            )
        }

        Text(
            text = "Foto barang · Opsional",
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = if (selectedPhoto.isNullOrBlank()) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            if (selectedPhoto.isNullOrBlank()) {
                Column(
                    modifier = Modifier.padding(ClosetlySpacing.Lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Sm,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                    )

                    Text(
                        text = "Simpan foto sebagai referensi barangmu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    ClosetlyButton(
                        text = "Pilih dari galeri",
                        onClick = onChoosePhoto,
                        modifier = Modifier.fillMaxWidth(),
                        style = ClosetlyButtonStyle.Secondary,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(ClosetlySpacing.Md),
                    horizontalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Md,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = selectedPhoto,
                        contentDescription = "Foto barang yang dipilih",
                        modifier = Modifier
                            .width(104.dp)
                            .aspectRatio(0.85f)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(
                            ClosetlySpacing.Sm,
                        ),
                    ) {
                        Text(
                            text = "1 foto dipilih",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        ClosetlyButton(
                            text = "Ganti foto",
                            onClick = onChoosePhoto,
                            modifier = Modifier.fillMaxWidth(),
                            style = ClosetlyButtonStyle.Secondary,
                        )

                        TextButton(
                            onClick = { imageModel = null },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text("Hapus foto")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text(
                text = "Nama barang *",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Contoh: Jaket denim biru") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "Kategori *",
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { categoryMenuExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                contentPadding = PaddingValues(
                    horizontal = ClosetlySpacing.Lg,
                    vertical = ClosetlySpacing.Sm,
                ),
            ) {
                Text(
                    text = category?.label ?: "Pilih kategori",
                    modifier = Modifier.weight(1f),
                    color = if (category == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                )
            }

            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = {
                    categoryMenuExpanded = false
                },
            ) {
                WishlistCategory.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            category = option
                            categoryMenuExpanded = false
                        },
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text(
                text = "Perkiraan harga · Opsional",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it },
                prefix = { Text("Rp ") },
                placeholder = { Text("Contoh: 499000") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                isError = isPriceInvalid,
                supportingText = if (isPriceInvalid) {
                    {
                        Text("Masukkan angka utuh 0 atau lebih, tanpa titik.")
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text(
                text = "Nama toko · Opsional",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            OutlinedTextField(
                value = store,
                onValueChange = { store = it },
                placeholder = { Text("Contoh: Erigo") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text(
                text = "Tautan produk · Opsional",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            OutlinedTextField(
                value = productUrl,
                onValueChange = { productUrl = it },
                placeholder = { Text("https://") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "Prioritas pembelian *",
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            WishlistPriority.entries.forEach { option ->
                val isSelected = priority == option

                FilterChip(
                    selected = isSelected,
                    onClick = { priority = option },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    label = {
                        Text(
                            text = option.label,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                            ),
                        )
                    },
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                text = if (isEditing) {
                    "Batal, kembali ke detail"
                } else {
                    "Batal, kembali ke Wishlist"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
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
