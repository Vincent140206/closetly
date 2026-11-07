# Kontrak Navigation

Navigation tetap terpusat di `core/navigation/AppNavHost.kt`. Route adalah type-safe serializable object/data class di `Routes.kt`. Screen menerima callback dan tidak menerima `NavController`.

`AppNavHost` juga menjadi pemilik satu `ClosetlyBottomBar`. Bottom bar tidak dibuat ulang oleh feature screen.

## Route saat ini

- Setelah splash selesai dan sesi dipulihkan, circular reveal membuka `HomeRoute` untuk sesi authenticated atau `OnboardingRoute` bila belum login. Kegagalan pemulihan/configuration unavailable juga menuju Onboarding.
- `OnboardingRoute` membuka welcome screen di `feature/onboarding/OnboardingScreen.kt`, tanpa bottom bar. Tombol **Mulai Sekarang** membuka `RegisterRoute`, sedangkan **Sudah punya akun? Masuk** membuka `LoginRoute`. Hero/statistik dan kelompok tombol berada di `feature/onboarding/ui/components`; screen menerima callback dari graph.
- `ClosetlyApp` memakai satu `AuthViewModel` yang sama dengan `AppNavHost`. Destination awal dipilih sekali; perubahan sesi berikutnya ditangani navigation effect, tanpa mengganti graph saat login/logout.
- `HomeRoute` tetap menjadi default saat `AppNavHost` digunakan langsung untuk development atau tes app shell. Login berhasil, logout, dan sesi yang menjadi `NotAuthenticated` membersihkan back stack sebelumnya; pengguna yang keluar dari sesi kembali ke Onboarding. Login dan Register tetap dapat dibuka sebagai bagian flow Auth.
- `LoginRoute`, `RegisterRoute`, dan `ProfileRoute` menjalankan flow Auth/Profile yang sudah ada.
- `WardrobeRoute`, `OutfitRoute`, `PlannerRoute`, `InspirationRoute`, `WishlistRoute`, dan `WearHistoryRoute` menjaga identitas enam modul.
- Enam route fitur masih membuka `ModulePlaceholderScreen` bersama karena folder standardisasi sengaja belum berisi screen implementation.

`InspirationRoute` sekarang mewakili **Community**. Nama route, package `feature/inspiration`, dan branch `feature/inspiration-board` tidak diubah untuk kompatibilitas.

## Bottom navigation

Lima destination utama:

| Label | Route |
|---|---|
| Beranda | `HomeRoute` |
| Lemari | `WardrobeRoute` |
| Outfit | `OutfitRoute` |
| Planner | `PlannerRoute` |
| Komunitas | `InspirationRoute` |

- Selected item mengikuti current destination.
- Tab navigation memakai `launchSingleTop`, state restoration, dan `popUpTo<HomeRoute>` agar tidak menumpuk destination duplikat.
- Bottom bar hanya tampil pada lima route utama.
- Onboarding, Login, Register, Profile, Wishlist, dan Wear History tidak menampilkan bottom bar.
- Wishlist dan Wear History tetap dapat diakses dari Home.

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
