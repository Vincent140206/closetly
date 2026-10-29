package vin.pam.closetly.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vin.pam.closetly.feature.auth.AuthRepository
import vin.pam.closetly.feature.auth.AuthViewModel
import vin.pam.closetly.feature.auth.DemoAuthRepository
import vin.pam.closetly.feature.inspiration.FakeInspirationRepository
import vin.pam.closetly.feature.inspiration.InspirationRepository
import vin.pam.closetly.feature.inspiration.InspirationViewModel
import vin.pam.closetly.feature.outfit.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.OutfitRepository
import vin.pam.closetly.feature.outfit.OutfitViewModel
import vin.pam.closetly.feature.planner.FakePlannerRepository
import vin.pam.closetly.feature.planner.PlannerRepository
import vin.pam.closetly.feature.planner.PlannerViewModel
import vin.pam.closetly.feature.wardrobe.FakeWardrobeRepository
import vin.pam.closetly.feature.wardrobe.WardrobeRepository
import vin.pam.closetly.feature.wardrobe.WardrobeViewModel
import vin.pam.closetly.feature.wearhistory.FakeWearHistoryRepository
import vin.pam.closetly.feature.wearhistory.WearHistoryRepository
import vin.pam.closetly.feature.wearhistory.WearHistoryViewModel
import vin.pam.closetly.feature.wishlist.FakeWishlistRepository
import vin.pam.closetly.feature.wishlist.WishlistRepository
import vin.pam.closetly.feature.wishlist.WishlistViewModel

class AppContainer {
    val authRepository: AuthRepository = DemoAuthRepository()
    val wardrobeRepository: WardrobeRepository = FakeWardrobeRepository()
    val outfitRepository: OutfitRepository = FakeOutfitRepository()
    val plannerRepository: PlannerRepository = FakePlannerRepository()
    val inspirationRepository: InspirationRepository = FakeInspirationRepository()
    val wishlistRepository: WishlistRepository = FakeWishlistRepository()
    val wearHistoryRepository: WearHistoryRepository = FakeWearHistoryRepository()
    val viewModelFactory: ViewModelProvider.Factory = ClosetlyViewModelFactory(this)
}

class ClosetlyApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }
}

private class ClosetlyViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(container.authRepository)
        modelClass.isAssignableFrom(WardrobeViewModel::class.java) -> WardrobeViewModel(container.wardrobeRepository)
        modelClass.isAssignableFrom(OutfitViewModel::class.java) -> OutfitViewModel(container.outfitRepository)
        modelClass.isAssignableFrom(PlannerViewModel::class.java) -> PlannerViewModel(container.plannerRepository)
        modelClass.isAssignableFrom(InspirationViewModel::class.java) -> InspirationViewModel(container.inspirationRepository)
        modelClass.isAssignableFrom(WishlistViewModel::class.java) -> WishlistViewModel(container.wishlistRepository)
        modelClass.isAssignableFrom(WearHistoryViewModel::class.java) -> WearHistoryViewModel(container.wearHistoryRepository)
        else -> error("Unknown ViewModel: ${modelClass.name}")
    } as T
}
