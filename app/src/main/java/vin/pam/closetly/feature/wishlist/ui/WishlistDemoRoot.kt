package vin.pam.closetly.feature.wishlist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

private data class WishlistDemoEntry(
    val id: String,
    val formData: WishlistFormData,
    val isPurchased: Boolean = false,
) {
    fun toCardData(): WishlistCardData {
        val priceLabel = formData.estimatedPrice?.let { price ->
            val formatter = NumberFormat.getIntegerInstance(
                Locale.forLanguageTag("id-ID"),
            )
            "Rp${formatter.format(price)}"
        }

        return WishlistCardData(
            id = id,
            name = formData.name,
            priceLabel = priceLabel,
            store = formData.store,
            priorityLabel = formData.priority.label,
            isPurchased = isPurchased,
            category = formData.category,
            imageModel = formData.imageModel,
        )
    }
}


@Composable
fun WishlistDemoRoot(
    modifier: Modifier = Modifier.safeDrawingPadding(),
    collectionContent: (@Composable () -> Unit)? = null,
) {
    var showAddForm by rememberSaveable { mutableStateOf(false) }
    var showEditForm by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var wishlistTab by rememberSaveable { mutableStateOf(false) }

    val tabStateHolder = rememberSaveableStateHolder()

    var entries by remember {
        mutableStateOf(
            listOf(
                WishlistDemoEntry(
                    id = "denim-1",
                    formData = WishlistFormData(
                        name = "Jaket denim biru",
                        category = WishlistCategory.Clothing,
                        estimatedPrice = 499_000L,
                        store = "Erigo",
                        priority = WishlistPriority.High,
                    ),
                ),
                WishlistDemoEntry(
                    id = "sneakers-1",
                    formData = WishlistFormData(
                        name = "Sneakers putih",
                        category = WishlistCategory.Shoes,
                        estimatedPrice = 459_000L,
                        store = "Aerostreet",
                        priority = WishlistPriority.Medium,
                    ),
                    isPurchased = true,
                ),
            ),
        )
    }

    val selectedItem = entries.firstOrNull { entry ->
        entry.id == selectedItemId
    }

    BackHandler(enabled = showAddForm || selectedItem != null) {
        when {
            showAddForm -> showAddForm = false
            showEditForm -> showEditForm = false
            else -> selectedItemId = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            showAddForm -> {
                WishlistFormScreen(
                    initialData = null,
                    onCancel = { showAddForm = false },
                    onSave = { data ->
                        entries = entries + WishlistDemoEntry(
                            id = UUID.randomUUID().toString(),
                            formData = data,
                        )
                        showAddForm = false
                    },
                )
            }

            selectedItem != null && showEditForm -> {
                WishlistFormScreen(
                    initialData = selectedItem.formData,
                    onCancel = { showEditForm = false },
                    onSave = { editedData ->
                        entries = entries.map { entry ->
                            if (entry.id == selectedItem.id) {
                                entry.copy(formData = editedData)
                            } else {
                                entry
                            }
                        }
                        showEditForm = false
                    },
                )
            }

            selectedItem != null -> {
                WishlistDetailScreen(
                    item = selectedItem.toCardData(),
                    onBack = { selectedItemId = null },
                    onEdit = { showEditForm = true },
                    onDelete = {
                        entries = entries.filterNot { entry ->
                            entry.id == selectedItem.id
                        }
                        selectedItemId = null
                    },
                )
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (collectionContent != null) {
                        SecondaryTabRow(selectedTabIndex = if (wishlistTab) 1 else 0) {
                            Tab(
                                selected = !wishlistTab,
                                onClick = { wishlistTab = false },
                                text = { Text("Koleksi") },
                            )
                            Tab(
                                selected = wishlistTab,
                                onClick = { wishlistTab = true },
                                text = { Text("Wishlist") },
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        if (collectionContent != null && !wishlistTab) {
                            tabStateHolder.SaveableStateProvider("closet-collection") {
                                collectionContent()
                            }
                        } else {
                            tabStateHolder.SaveableStateProvider("closet-wishlist") {
                                WishlistScreen(
                                    entries = entries.map { it.toCardData() },
                                    onOpenItem = { clickedId ->
                                        selectedItemId = clickedId
                                        showEditForm = false
                                    },
                                    onAddItem = { showAddForm = true },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}