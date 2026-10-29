# Navigation contract

All destinations are declared as `@Serializable` route objects or data classes in `core/navigation/Routes.kt`. `AppNavHost.kt` is the only place that translates UI callbacks into navigation operations. Reusable screens and components must not receive a `NavController`.

## Shared routes

`LoginRoute` → `RegisterRoute` or `HomeRoute`; `HomeRoute` → `ProfileRoute` → `EditProfileRoute`.

## Feature routes

| Feature list | Detail route and argument | Add/edit route and argument |
|---|---|---|
| `WardrobeRoute` | `ClothingDetailRoute(clothingId)` | `ClothingFormRoute(clothingId?)` |
| `OutfitCollectionRoute` | `OutfitDetailRoute(outfitId)` | `OutfitFormRoute(outfitId?)` |
| `PlannerRoute` | `PlanDetailRoute(planId)` | `PlanFormRoute(planId?)` |
| `InspirationRoute` | `InspirationDetailRoute(inspirationId)` | `InspirationFormRoute(inspirationId?)` |
| `WishlistRoute` | `WishlistDetailRoute(wishlistId)` | `WishlistFormRoute(wishlistId?)` |
| `WearHistoryRoute` | `WearHistoryDetailRoute(historyId)` | `WearHistoryFormRoute(historyId?)` |

Only IDs cross navigation boundaries. A nullable form ID means create mode; a non-null ID means edit mode. Saving pops one destination so add returns to the list and edit returns to detail. Delete returns from detail to list. System back and the shared Back action both use `popBackStack()`.

When adding a destination, define its serializable route, add one `composable<Route>` entry, reconstruct arguments with `toRoute<Route>()`, and pass callbacks to the screen.
