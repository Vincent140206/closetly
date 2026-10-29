# Navigation contract

Navigation is centralized in `core/navigation/AppNavHost.kt`. Routes are serializable objects in `Routes.kt`. Screens receive callbacks and must not receive a `NavController`.

## Current routes

- `HomeRoute` is the development start destination.
- `LoginRoute`, `RegisterRoute`, and `ProfileRoute` reserve shared account entry points.
- `WardrobeRoute`, `OutfitRoute`, `PlannerRoute`, `InspirationRoute`, `WishlistRoute`, and `WearHistoryRoute` preserve stable module identities.

The six module routes currently render the same app-level `ModulePlaceholderScreen`, using metadata from `AppModule`. There are no feature List, Detail, Form, or ID-argument routes on `develop`.

## Adding feature routes

Feature owners should define only routes needed by their implemented screens. Prefer serializable objects for argument-free destinations and serializable data classes for lightweight IDs. Never pass full models through navigation.

Coordinate changes to `Routes.kt` and `AppNavHost.kt` with the navigation maintainer to minimize merge conflicts. Keep feature navigation callbacks at the graph boundary and preserve functional Back navigation.
