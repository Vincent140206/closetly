# Shared UI guidelines

The shared design system is deliberately small. It provides:

- `ClosetlyScaffold`
- `ClosetlyTopBar`
- `LoadingState`
- `EmptyState`
- `ErrorState`
- `ConfirmDialog`
- Neutral Material 3 light/dark color schemes and typography

Feature owners may compose these primitives while retaining control of their module's screens and components.

Folder `ui/components` pada enam fitur hanya berisi `.gitkeep` selama standardisasi. Pemilik fitur membuat root screen, UI state, dan komponen ketika implementasi dimulai; placeholder navigation bersama tetap digunakan sampai screen tersebut compile.

## Rules

- Keep navigation outside reusable components.
- Keep data access and business operations outside Composables.
- Prefer state hoisting and lifecycle-aware ViewModel state collection in implemented features.
- Use consistent 12–24 dp spacing, readable contrast, rounded surfaces, and at least 48 dp touch targets.
- Provide content descriptions for informative images.
- Add a shared component only when it solves an application-level need or is reused by multiple features.
- Do not place clothing, outfit, planner, wishlist, inspiration, or history-specific UI in `core/ui`.
- Package `feature/inspiration` menampilkan fitur Community; pertahankan nama package untuk kompatibilitas, tetapi gunakan nama UI `CommunityScreen` dan istilah Community pada teks pengguna.
- Planner dan Wear History harus menampilkan status pending/worn secara eksplisit. Tanggal yang lewat tidak boleh tampak otomatis sebagai worn.
- Community feed boleh dibaca publik, tetapi create/edit/delete harus menampilkan kebutuhan autentikasi dengan jelas.
