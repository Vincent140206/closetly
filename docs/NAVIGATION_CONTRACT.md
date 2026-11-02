# Kontrak Navigation

Navigation tetap terpusat di `core/navigation/AppNavHost.kt`. Route adalah type-safe serializable object/data class di `Routes.kt`. Screen menerima callback dan tidak menerima `NavController`.

## Route saat ini

- `HomeRoute` adalah development start destination.
- `LoginRoute`, `RegisterRoute`, dan `ProfileRoute` menjalankan flow Auth/Profile yang sudah ada.
- `WardrobeRoute`, `OutfitRoute`, `PlannerRoute`, `InspirationRoute`, `WishlistRoute`, dan `WearHistoryRoute` menjaga identitas enam modul.
- Enam route fitur masih membuka `ModulePlaceholderScreen` bersama karena folder standardisasi sengaja belum berisi screen implementation.

`InspirationRoute` sekarang mewakili **Community**. Nama route, package `feature/inspiration`, dan branch `feature/inspiration-board` tidak diubah untuk kompatibilitas.

## Root screen yang akan dihubungkan

| Route | Root screen yang dibuat pemilik fitur |
|---|---|
| `WardrobeRoute` | `WardrobeScreen` |
| `OutfitRoute` | `OutfitScreen` |
| `PlannerRoute` | `PlannerScreen` |
| `InspirationRoute` | `CommunityScreen` |
| `WishlistRoute` | `WishlistScreen` |
| `WearHistoryRoute` | `WearHistoryScreen` |

Pemilik fitur mengganti placeholder hanya setelah screen dan ViewModel-nya compile. Auth/Profile navigation dan session behavior tidak boleh ikut ditulis ulang.

## Route lanjutan

- List/root screen tanpa argument menggunakan `data object`.
- Detail/create/edit hanya ditambahkan saat screen benar-benar dibuat.
- Detail/edit memakai serializable data class berisi ID ringan, misalnya `outfitId: String`.
- Jangan mengirim model, byte image, URL bertanda tangan, atau credential melalui navigation.
- Feature screen menerima callback `onBack`, `onOpenDetail(id)`, atau `onCreate`; central graph memegang aksi navigate.
- Back dari form/detail kembali ke list. Logout tetap membersihkan destination authenticated yang obsolete.

Perubahan `Routes.kt` dan `AppNavHost.kt` harus dikoordinasikan untuk mencegah duplicate route dan merge conflict.
