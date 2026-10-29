# Shared UI guidelines

- Use the neutral Closetly Material 3 theme, rounded cards, 12–20 dp content spacing, and readable contrast.
- Start screens with `ClosetlyScaffold`; use its title, Back action, optional action, and Add floating action.
- Reuse `LoadingState`, `EmptyState`, `ErrorState`, and `ConfirmDeleteDialog` for common states.
- Wardrobe, Outfit, Inspiration, Wishlist, and Wear History use a lazy grid. Planner uses a lazy column.
- Screen functions accept state/data and callbacks. Navigation stays in `AppNavHost`; storage/network calls stay in repositories.
- Collect ViewModel flows with `collectAsStateWithLifecycle` and use `rememberSaveable` only for temporary form state.
- Provide a meaningful content description for informative images. Decorative visuals can use null.
- Keep touch targets at least 48 dp and do not communicate status through color alone.
- Add previews for reusable components and high-value screens when introducing visually distinct UI.

The current generic form intentionally proves create/edit flow. Feature owners should replace it with validated, feature-specific fields while retaining the same repository and navigation callbacks.
