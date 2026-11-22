# Review branch untuk PR ke develop — 22 November 2026

Baseline review: `develop` di `660cdd0f83f4c2a3f3692f6586b7d74c89ecea87`. Semua branch remote di-fetch dan diperiksa ulang setelah pengujian. Tanggal di bawah adalah tanggal commit, bukan waktu event push GitHub.

## Hasil pemeriksaan semua branch

| Branch | Tip yang diperiksa | Kesimpulan |
|---|---|---|
| `develop` | `660cdd0` | Baseline terbaru; sudah berisi panduan pemakaian data Wardrobe dari PR #18. |
| `feature/fashion-wishlist` | `db338c6` | Ada perubahan belum masuk: commit UI 20 November dan merge develop lama `5f85070`; tertinggal 30 commit dari develop. |
| `feature/digital-wardrobe` | `1f60466` | Sudah tercakup di develop. |
| `feature/wear-history` | `c6bf1ca` | Sudah tercakup di develop. |
| `feature/outfit-planner` | `b082e20` | Sudah tercakup di develop. |
| `feature/home-dashboard` | `ce63007` | Sudah tercakup di develop. |
| `feature/mix-and-match` | `5843e31` | Sudah tercakup di develop. |
| `feature/inspiration-board` | `55108b0` | Sudah tercakup di develop. |
| `feature/auth` | `eabec14` | Sudah tercakup di develop. |
| `feature/profile` | `eabec14` | Sudah tercakup di develop. |
| `feature/splash-screen` | `eabec14` | Sudah tercakup di develop. |
| `integration/develop-review-20261116` | `8145153` | Sudah tercakup di develop. |
| `integration/feature-compat` | `dfc27c4` | Sudah tercakup di develop. |
| `main` | `21e24ef` | Masih memiliki dua merge commit khusus main; tidak ada pekerjaan fitur baru untuk PR ini. |

Branch `integration/wishlist-pr-20261122` dibuat dari baseline tersebut untuk menyiapkan PR Wishlist. Branch sumber teman tidak ditulis ulang dan develop tidak di-merge/push pada review ini.

## Perubahan Wishlist

Update menambah tampilan ringkasan, estimasi belanja, pencarian/filter, detail barang, tautan produk, serta toggle sudah/belum dibeli. Form tambah/edit dan tampilan kartu diperbarui. Koleksi dan Wishlist ditampilkan sebagai dua tab di Closet; bottom bar disembunyikan saat form Wishlist dibuka.

Riwayat branch mempunyai lebih dari satu merge base. Diff tiga titik dari branch sumber dapat menampilkan implementasi lama seolah baru; review ini memakai hasil merge aktual ke develop terbaru.

## Masalah yang diselesaikan sebelum PR

1. Lima konflik ada di `AppNavHost`, `WardrobeScreen`, `WishlistDemoRoot`, `WishlistFilterSheet`, dan `WishlistFormScreen`.
2. Callback `loadPhoto` dan guard tombol untuk write yang masih berjalan dipertahankan. Mengambil versi lama dari branch Wishlist saja akan menghilangkan perilaku Wardrobe terbaru.
3. Data dan ViewModel tetap mengikuti akun aktif. Destination Wear History yang sudah berjalan dipertahankan.
4. Daftar demo Wishlist tetap memakai saved state; ID detail dan status dibeli tidak bergantung pada daftar yang direset setelah recreation.
5. Pintasan Fashion Wishlist dari Home membuka tab Wishlist, bukan tab Koleksi. Permintaan pembukaan tab dikonsumsi sekali supaya tidak menimpa pilihan tab berikutnya.
6. Header/tab tidak dibuat ganda. Satu root Closet menahan state Wishlist saat berpindah ke Koleksi dan kembali.
7. Filter tetap menggunakan satu bottom sheet putih. Perubahan format file lisensi font yang tidak terkait dikeluarkan dari diff.

## Validasi

- `:app:testDebugUnitTest`: **69 test lulus**, 0 failure, 0 error.
- `:app:assembleDebug` dan `:app:assembleDebugAndroidTest`: berhasil.
- `:app:lintDebug`: 0 error, 10 warning. Warning mencakup SDK/dependency, modifier default, resource, dan saran `toUri`; bukan error build.
- Tes Android disesuaikan untuk form baru, navigasi Closet, filter tunggal, pemulihan item/status dibeli, dan permintaan tab sekali pakai. Tes tersebut berhasil dikompilasi, tetapi tidak dijalankan karena tidak ada perangkat terhubung.
- Tidak ada status CI atau workflow PR yang dikembalikan untuk tip sumber; hasil di atas berasal dari pengujian lokal checkout review.

Sebelum merge, lakukan smoke test di perangkat untuk tambah/edit/hapus, status dibeli, galeri, tautan produk, pergantian tab, serta foto koleksi Supabase. Wishlist masih demo lokal dengan pemulihan saved state, belum penyimpanan Supabase permanen. Review ini tidak menambah write Supabase, migration, atau perubahan RLS.

## PR yang disiapkan

- Base: `develop`
- Head: `integration/wishlist-pr-20261122`
- Title: **Integrate the latest Wishlist UI while preserving Closet functionality**

Deskripsi singkat untuk PR:

> Integrate the latest Wishlist UI, filters, product links, and purchased-status controls from db338c6 into current develop. Resolve five merge conflicts while preserving account scoping, saved-state restoration, Wardrobe photo loading, pending-write guards, and Wear History navigation. Home opens the correct Wishlist tab and the bottom bar hides during add/edit forms. Validation: 69 unit tests passed; debug APK, Android test APK, and lint passed (0 errors, 10 warnings). Device UI tests remain pending. Wishlist is still a local demo; no Supabase writes or schema changes are added.
