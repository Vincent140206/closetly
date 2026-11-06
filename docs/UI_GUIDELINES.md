# Panduan UI Closetly

Closetly Design System v0.2 memakai aksen kuning, background off-white, card putih, heading near-black, spacing lega, dan rounded surface yang lembut. Detail seluruh token dan contoh penggunaan tersedia di [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md).

## Komponen bersama

- `ClosetlyScaffold` dan `ClosetlyTopBar`
- `ClosetlyButton` dan `ClosetlyTextButton`
- `ClosetlyCard`
- `ClosetlyBottomBar`
- `LoadingState`, `EmptyState`, dan `ErrorState`
- `ConfirmDialog`

Gunakan komponen bersama untuk pola app-level yang sama. Komponen khusus pakaian, outfit, planner, wishlist, Community, atau history tetap berada di package fitur masing-masing.

## Aturan Compose

- UI menerima state dan callback; data access dan business operation tidak berada di Composable.
- Kumpulkan state ViewModel secara lifecycle-aware.
- Gunakan `MaterialTheme.colorScheme`, `MaterialTheme.typography`, dan `MaterialTheme.shapes`.
- Gunakan `ClosetlySpacing`, `ClosetlyRadius`, dan `ClosetlyDimensions`; hindari hardcoded color/spacing berulang.
- Gunakan `Modifier` parameter pada komponen reusable.
- Pertahankan touch target minimal 48dp.
- Berikan content description pada informative image/icon.
- Pastikan loading, empty, error, disabled, dan success state terlihat jelas.
- Hindari gradient, shadow berlebihan, dekorasi yang tidak membantu, dan fixed screen width.

## Navigation dan screen

- Screen tidak menerima `NavController`; gunakan callback dari central navigation graph.
- Bottom bar hanya menampilkan Beranda, Lemari, Outfit, Planner, dan Komunitas.
- Wishlist dan Wear History tetap diakses melalui Home atau entry point yang dikoordinasikan.
- Login, Register, dan Profile tidak menampilkan bottom bar.
- Package `feature/inspiration` memakai nama user-facing **Community/Komunitas** tanpa rename package otomatis.

## Feature package

Folder `ui/components` pada enam fitur hanya berisi `.gitkeep` selama standardisasi. Pemilik fitur membuat root screen, UI state, dan komponen ketika implementasi dimulai. Hapus `.gitkeep` hanya setelah folder memiliki source file.

Planner dan Wear History harus menampilkan status pending/worn secara eksplisit; tanggal yang lewat tidak boleh tampak otomatis sebagai worn. Community feed boleh dibaca publik, tetapi create/edit/delete harus menyatakan kebutuhan autentikasi dengan jelas.
