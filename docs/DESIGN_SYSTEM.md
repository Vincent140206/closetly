# Closetly Design System v0.2

Design system ini membentuk identitas visual bersama untuk seluruh fitur Closetly. Karakternya modern, editorial, hangat, dan sederhana: aksen kuning yang kuat, canvas off-white, kartu putih, heading mendekati hitam, whitespace yang lega, serta dekorasi seminimal mungkin.

Implementasi berada di `ui/theme` dan `core/ui/components`. Feature developer harus memakai `MaterialTheme` dan komponen bersama tersebut agar warna, typography, spacing, radius, serta interaction state tetap konsisten.

## Warna

| Token Kotlin | Nilai | Peran |
|---|---|---|
| `GoldenPrimary` | `#FACC15` | Brand accent dan primary action. |
| `GoldenPressed` | `#EAB308` | Feedback saat primary action ditekan. |
| `SoftYellow` | `#FDE68A` | Primary container dan disabled surface. |
| `Vanilla` | `#FFFBEB` | Warm secondary surface dan bottom navigation. |
| `OffWhite` | `#FAFAF7` | Background utama light theme. |
| `CardWhite` | `#FFFFFF` | Content card dan elevated surface. |
| `Heading` | `#171717` | Heading dan teks primary button. |
| `Body` | `#262626` | Body text dan dark action. |
| `Muted` | `#62625F` | Supporting text dan metadata. |
| `Border` | `#E8E5DC` | Border kartu dan outline lembut. |

Semua warna aktif dipetakan ke `MaterialTheme.colorScheme`. Dark theme merupakan counterpart aksesibel yang diturunkan dari identitas kuning yang sama; warna dark bukan bagian dari referensi visual asli.

Jangan menulis hex color langsung di feature screen. Gunakan peran seperti `MaterialTheme.colorScheme.primary`, `surface`, `background`, `onSurface`, `onSurfaceVariant`, dan `outline`.

## Typography

Typography memakai **DM Sans** untuk seluruh role Material 3. Font variable dibundel di `app/src/main/res/font/dm_sans.ttf` sehingga tersedia offline tanpa dependency tambahan. Bobot Regular (400), Medium (500), SemiBold (600), dan Bold (700) dikonfigurasi di `ui/theme/Type.kt`; ukuran, line height, dan letter spacing tetap mengikuti token yang ada.

Untuk mengganti font aplikasi secara global, ubah keluarga `ClosetlySans` di `ui/theme/Type.kt`. `ClosetlyTheme` menerapkan typography tersebut ke seluruh aplikasi. Sumber font: [Google Fonts — DM Sans](https://github.com/google/fonts/tree/main/ofl/dmsans). Lisensi SIL Open Font License disertakan di `app/src/main/assets/licenses/dm_sans_OFL.txt`.

| Peran | Material style | Ukuran | Line height |
|---|---|---:|---:|
| Display | `displaySmall` | 36sp | 44sp |
| Heading | `headlineMedium` | 28sp | 36sp |
| Section | `titleLarge` | 22sp | 30sp |
| Subheading | `titleMedium` | 17sp | 24sp |
| Body | `bodyMedium` | 14sp | 22sp |
| Caption | `bodySmall` / `labelSmall` | 12sp | 18sp |

Heading memakai weight kuat. Body dan supporting text menjaga keterbacaan tanpa dekorasi tambahan. Gunakan style theme; jangan menetapkan `fontSize` per screen kecuali ada kebutuhan aksesibilitas yang sudah disepakati.

## Spacing dan Radius

`ClosetlySpacing` menyediakan skala `4dp`, `8dp`, `12dp`, `16dp`, `24dp`, `32dp`, dan `48dp` melalui `Xs` sampai `Xxxl`.

`ClosetlyRadius` menyediakan:

- `Sm`: 8dp
- `Md`: 12dp
- `Lg`: 16dp
- `Xl`: 22dp
- `ClosetlyFullShape`: pill/full rounded shape

Gunakan `ClosetlyDimensions.MinimumTouchTarget` untuk minimum control 48dp, `ScreenHorizontalPadding` untuk padding screen standar, dan `CardPadding` untuk content card.

## Komponen Bersama

### `ClosetlyButton`

Varian melalui `ClosetlyButtonStyle`:

- `Primary`: kuning dengan teks gelap; digunakan untuk aksi utama.
- `Secondary`: surface putih dengan outline; digunakan untuk aksi alternatif.
- `Dark`: mendekati hitam dengan teks putih; digunakan untuk aksi berkontras tinggi yang memang membutuhkan penekanan berbeda.

Semua varian mendukung `enabled` dan `isLoading`. Disabled primary memakai soft yellow dan tidak menerima input. Untuk aksi tanpa container gunakan `ClosetlyTextButton`.

```kotlin
ClosetlyButton(
    text = "Save outfit",
    onClick = onSave,
    modifier = Modifier.fillMaxWidth(),
    enabled = uiState.canSave,
    isLoading = uiState.isSaving,
)
```

### `ClosetlyCard`

Wrapper kartu netral dengan surface putih, border warm gray, radius besar, tanpa shadow berlebihan, dan padding standar. Gunakan hanya untuk grouping content. Clothing card, wishlist card, atau Community post card tetap dibuat di package fitur masing-masing.

### State dan scaffolding

- `ClosetlyScaffold`: background dan top bar konsisten.
- `ClosetlyTopBar`: title, optional Back, dan optional action.
- `LoadingState`, `EmptyState`, `ErrorState`: state generik aplikasi.
- `ConfirmDialog`: konfirmasi berbasis Material 3.

Jaga API screen tetap state-hoisted. Komponen bersama tidak menerima repository, ViewModel, `SupabaseClient`, atau `NavController`.

## Bottom Navigation

Lima destination utama adalah:

1. Beranda
2. Lemari
3. Outfit
4. Planner
5. Komunitas

Selected item memakai indicator kuning dan icon/label gelap. Unselected item memakai warna muted pada surface Vanilla. `InspirationRoute` tetap dipakai untuk Komunitas demi kompatibilitas.

Login, Register, Profile, Wishlist, dan Wear History bukan bottom tab. Wishlist dan Wear History tetap dapat dibuka dari Home. Central `AppNavHost` mengatur selected state, single-top navigation, state restoration, dan back stack; feature screen tidak mengelola bottom bar sendiri.

## Contoh Feature Screen

```kotlin
@Composable
fun WardrobeScreen(
    uiState: WardrobeUiState,
    onAddItem: () -> Unit,
) {
    ClosetlyScaffold(title = "Digital Wardrobe") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(ClosetlySpacing.Lg),
        ) {
            Text(
                text = "Your wardrobe",
                style = MaterialTheme.typography.headlineMedium,
            )
            ClosetlyButton(
                text = "Add clothing item",
                onClick = onAddItem,
            )
        }
    }
}
```

## Aturan Implementasi

- Gunakan `MaterialTheme` untuk warna, typography, dan shapes.
- Gunakan token spacing/radius; hindari angka dp acak yang berulang.
- Pertahankan minimum touch target 48dp.
- Gunakan Card White untuk content card dan Off White sebagai canvas.
- Jangan memakai kuning sebagai surface besar tanpa alasan visual yang jelas.
- Hindari gradient, shadow berat, animasi dekoratif, dan fixed screen width.
- Pastikan informative image memiliki content description.
- Feature-specific component tetap berada di package fiturnya.
- Preview design system tersedia di `DesignSystemPreview.kt` dan bukan start destination aplikasi.
