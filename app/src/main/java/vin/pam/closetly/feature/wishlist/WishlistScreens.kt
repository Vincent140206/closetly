package vin.pam.closetly.feature.wishlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.FeatureDetailScreen
import vin.pam.closetly.core.ui.components.FeatureFormScreen
import vin.pam.closetly.core.ui.components.FeatureListScreen

@Composable
fun WishlistScreen(viewModel: WishlistViewModel, onItemClick: (String) -> Unit, onAddClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureListScreen("Fashion Wishlist", "Your wishlist is empty.", true, state, onItemClick, onAddClick, onBack)
}

@Composable
fun WishlistDetailScreen(viewModel: WishlistViewModel, wishlistId: String, onEditClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.items.firstOrNull { it.id == wishlistId }
    FeatureDetailScreen(
        "Wishlist details", item,
        item?.let { listOf("Store" to it.store, "Estimated price" to "Rp${it.estimatedPrice}", "Priority" to it.priority, "Status" to if (it.purchased) "Purchased" else "Wanted") }.orEmpty(),
        onEditClick, { viewModel.delete(wishlistId, onBack) }, onBack,
    )
}

@Composable
fun WishlistFormScreen(viewModel: WishlistViewModel, wishlistId: String?, onSave: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val existing = wishlistId?.let(viewModel::item)
    FeatureFormScreen(
        if (existing == null) "Add wishlist item" else "Edit wishlist item",
        existing?.name.orEmpty(), existing?.category.orEmpty(), existing?.imageUrl, true, state.isSubmitting,
        onSave = { name, category, image ->
            viewModel.save(WishlistItem(existing?.id ?: "wish-${System.currentTimeMillis()}", name, category.ifBlank { "Fashion" }, existing?.estimatedPrice ?: 0, existing?.store ?: "", existing?.productUrl ?: "", existing?.priority ?: "Medium", existing?.purchased ?: false, image), onSave)
        },
        onBack = onBack,
    )
}
