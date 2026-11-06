# Closetly

Closetly adalah aplikasi Android untuk membantu pengguna mengatur pakaian dan aktivitas berpakaian dalam satu tempat. Pengguna nantinya dapat menyimpan koleksi pakaian, membuat kombinasi outfit, merencanakan outfit untuk tanggal tertentu, mencatat riwayat pemakaian, menyimpan wishlist fashion, dan berbagi inspirasi melalui Community.

Proyek ini dikembangkan sebagai tugas akhir kelompok mata kuliah Pemrograman Aplikasi Mobile. Repository saat ini berada pada tahap **application foundation dan feature standardization**: fondasi aplikasi, Auth, dan Profile sudah tersedia, sedangkan enam fitur utama disiapkan untuk dikerjakan secara paralel oleh anggota tim.

## Gambaran Aplikasi

Closetly memiliki enam fitur utama:

| Fitur | Fungsi |
|---|---|
| Digital Wardrobe | Menyimpan dan mengelola koleksi pakaian pribadi. |
| Mix & Match Outfit | Membuat kombinasi outfit dari item wardrobe. |
| Outfit Planner | Menjadwalkan outfit untuk tanggal dan aktivitas tertentu. |
| Community | Menampilkan feed publik dan membagikan inspirasi outfit. |
| Fashion Wishlist | Mencatat item fashion yang ingin dibeli. |
| Wear History | Melihat outfit yang direncanakan maupun benar-benar sudah dipakai. |

Alur Planner dan Wear History saling terhubung. Saat sebuah plan dibuat, sistem nantinya membuat entry Wear History berstatus pending. Entry tersebut baru menjadi worn setelah pengguna menekan **Mark as Worn** dan memilih tanggal pemakaian aktual; tanggal rencana yang lewat tidak mengubah status secara otomatis.

Community menggunakan feed publik, tetapi hanya pengguna terautentikasi yang dapat membuat post. Author dapat mengubah atau menghapus post miliknya sendiri. Asset Community akan disimpan terpisah dari gambar private Wardrobe dan Outfit.

## Kondisi Proyek Saat Ini

| Bagian | Status | Keterangan |
|---|---|---|
| Android application shell | Tersedia | Compose theme, shared components, Home, dan centralized navigation. |
| Authentication | Tersedia | Register, login, session restoration, dan logout melalui Supabase Auth. |
| Profile | Tersedia | Membaca dan memperbarui basic profile pengguna terautentikasi. |
| Enam fitur utama | Belum diimplementasikan | Route masih membuka placeholder dan package hanya berisi struktur folder. |
| Database feature CRUD | Belum diimplementasikan | Logical schema sudah disepakati, tetapi schema aktif masih harus diverifikasi. |
| Image Storage | Belum diimplementasikan | SDK tersedia; bucket dan policy final masih harus diverifikasi. |

Home sengaja dapat dibuka tanpa login selama development. Profile membutuhkan sesi pengguna, sedangkan screen bertuliskan **Not implemented yet** merupakan placeholder yang disengaja.

Tidak ada full feature implementation di dalam folder enam fitur. Folder tersebut hanya menjadi batas ownership dan standar awal agar setiap anggota dapat mengembangkan fiturnya tanpa mengubah package anggota lain.

## Arsitektur Singkat

Setiap fitur mengikuti alur MVVM sederhana:

```text
Compose Screen -> ViewModel -> Feature Repository -> Shared Supabase Client
```

- Compose merender state dan mengirim aksi melalui callback.
- ViewModel mengelola UI state dan menjalankan use case.
- Repository dibuat khusus sesuai kebutuhan fitur; proyek tidak memakai generic base repository.
- Supabase client dibuat satu kali melalui application container dan digunakan bersama.
- DTO, PostgREST, Storage, serta mapping data berada di data layer.
- Row Level Security Supabase tetap menjadi batas keamanan utama.

Target logical schema menggunakan delapan tabel:

```text
profiles
clothing_items
outfits
outfit_items
outfit_plans
wear_history
community_posts
fashion_wishlist
```

Schema tersebut adalah kontrak pengembangan, bukan bukti bahwa migration sudah diterapkan. Existing Supabase schema harus diperiksa sebelum membuat migration, dan data lama tidak boleh dihapus otomatis.

## Tech Stack

| Teknologi | Kegunaan |
|---|---|
| Kotlin | Bahasa pemrograman utama. |
| Jetpack Compose dan Material 3 | Declarative UI dan design system. |
| Navigation Compose | Type-safe navigation antarscreen. |
| ViewModel dan StateFlow | State management dan lifecycle-aware UI. |
| Kotlin Serialization | Route dan data serialization. |
| Supabase Auth, PostgREST, dan Storage | Authentication serta backend aplikasi. |
| Coil | Memuat gambar pada Compose. |
| Gradle Kotlin DSL | Build configuration dan dependency management. |

Konfigurasi Android saat ini menggunakan min SDK 26, target SDK 36, dan compile SDK 36.1. Gradle membutuhkan JDK 17 atau versi yang kompatibel dengan Android Gradle Plugin yang digunakan.

## Tim dan Ownership

| Anggota | Fitur | Package | Branch |
|---|---|---|---|
| Vincent | Digital Wardrobe | `feature/wardrobe` | `feature/digital-wardrobe` |
| Janice | Mix & Match Outfit | `feature/outfit` | `feature/mix-and-match` |
| Vara | Outfit Planner | `feature/planner` | `feature/outfit-planner` |
| Sela | Community | `feature/inspiration` | `feature/inspiration-board` |
| Gres | Fashion Wishlist | `feature/wishlist` | `feature/fashion-wishlist` |
| Aurel | Wear History | `feature/wearhistory` | `feature/wear-history` |

Community tetap memakai package `feature/inspiration` dan branch `feature/inspiration-board` untuk kompatibilitas. Branch `feature/auth` dan `feature/profile` digunakan untuk shared Auth/Profile.

## Struktur Proyek

```text
app/src/main/java/vin/pam/closetly/
|-- MainActivity.kt
|-- ClosetlyApp.kt
|-- ClosetlyApplication.kt
|-- core/
|   |-- config/          # BuildConfig dan konfigurasi aplikasi
|   |-- data/            # Shared data provider
|   |-- navigation/      # Route, module metadata, dan NavHost
|   `-- ui/
|       |-- components/  # Reusable app-level UI
|       `-- shell/       # Development Home dan feature placeholder
|-- di/                  # Application dependency container
|-- feature/
|   |-- auth/            # Shared authentication implementation
|   |-- profile/         # Shared profile implementation
|   |-- wardrobe/
|   |-- outfit/
|   |-- planner/
|   |-- inspiration/     # Community; nama lama dipertahankan
|   |-- wishlist/
|   `-- wearhistory/
`-- ui/theme/            # Material 3 theme
```

Masing-masing dari enam package fitur hanya memiliki struktur kosong berikut:

```text
feature/<feature>/
|-- data/
|   |-- dto/.gitkeep
|   `-- repository/.gitkeep
|-- model/.gitkeep
`-- ui/
    `-- components/.gitkeep
```

Pemilik fitur membuat model, repository, ViewModel, UI state, screen, dan test ketika implementasi dimulai. Hapus `.gitkeep` hanya dari folder yang sudah memiliki source file.

## Menjalankan Proyek

### Prasyarat

- Android Studio
- Git
- JDK 17 atau lebih baru
- Android SDK 36 dengan platform/API yang dibutuhkan proyek
- Emulator Android atau perangkat fisik

### Clone dan buka proyek

```bash
git clone https://github.com/Vincent140206/closetly.git
cd closetly
```

Buka folder proyek melalui Android Studio, tunggu Gradle Sync selesai, pilih device, kemudian jalankan konfigurasi `app`.

Debug build dan unit test dapat dijalankan dengan:

```cmd
gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Untuk lint:

```cmd
gradlew.bat :app:lintDebug
```

Pada macOS/Linux, gunakan `./gradlew` sebagai pengganti `gradlew.bat`.

## Konfigurasi Supabase

Buat `secrets.properties` di root proyek:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-publishable-or-anon-key
```

Nilai juga dapat diberikan melalui Gradle property atau environment variable dengan nama yang sama. Jika credential tidak tersedia, aplikasi tetap dapat membuka development Home dan menampilkan status bahwa layanan Auth/Profile belum tersedia.

Aturan keamanan:

- Jangan commit `secrets.properties`, `local.properties`, `.env`, token, atau keystore.
- Aplikasi Android hanya boleh memakai publishable/anon key.
- Jangan pernah memasukkan secret/service-role key ke aplikasi atau repository.
- Jangan mengandalkan filter di client sebagai pengganti Row Level Security.

## Workflow Pengembangan

- `main`: versi terintegrasi yang sudah diterima.
- `develop`: branch integrasi pekerjaan tim.
- `feature/*`: branch kerja tiap fitur.

Mulai pekerjaan dari branch yang sudah ditentukan:

```bash
git fetch origin
git switch feature/nama-fitur
git merge origin/develop
```

Setelah implementasi:

```bash
git add <file-yang-diubah>
git commit -m "feat(scope): jelaskan perubahan"
git push origin feature/nama-fitur
```

Buat Pull Request dari `feature/*` menuju `develop`. Perubahan pada navigation, shared UI, Gradle, Auth/Profile, database contract, atau application container harus dikoordinasikan karena memengaruhi beberapa anggota. `main` diperbarui dari `develop` setelah hasil integrasi diuji.

Sebelum Pull Request:

- Pastikan scope hanya mencakup fitur yang dimiliki.
- Verifikasi loading, empty, error, dan success state yang relevan.
- Tambahkan unit test untuk logic ViewModel/repository.
- Jalankan debug build, unit test, dan lint.
- Jangan menyertakan credential atau file konfigurasi lokal.

## Dokumentasi Teknis

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md): batas layer, package, dan keputusan arsitektur.
- [`docs/FEATURE_DEVELOPMENT_GUIDE.md`](docs/FEATURE_DEVELOPMENT_GUIDE.md): standar implementasi fitur.
- [`docs/FEATURE_REQUIREMENTS.md`](docs/FEATURE_REQUIREMENTS.md): scope dan aturan tiap fitur.
- [`docs/DATABASE_CONTRACT.md`](docs/DATABASE_CONTRACT.md): delapan tabel, RLS, Storage, dan Planner/Wear History lifecycle.
- [`docs/REPOSITORY_CONTRACTS.md`](docs/REPOSITORY_CONTRACTS.md): tanggung jawab repository per fitur.
- [`docs/NAVIGATION_CONTRACT.md`](docs/NAVIGATION_CONTRACT.md): aturan route dan integrasi screen.
- [`docs/UI_GUIDELINES.md`](docs/UI_GUIDELINES.md): panduan Compose dan shared components.
- [`docs/DESIGN_SYSTEM.md`](docs/DESIGN_SYSTEM.md): token visual, typography, spacing, button, card, dan bottom navigation.
- [`docs/TEAM_ONBOARDING.md`](docs/TEAM_ONBOARDING.md): langkah singkat untuk anggota tim.

## Fokus Pengembangan Berikutnya

1. Verifikasi schema, RPC, RLS, dan Storage bucket pada project Supabase aktif.
2. Implementasikan enam fitur pada branch pemilik masing-masing.
3. Hubungkan root screen fitur ke centralized navigation setelah screen siap.
4. Tambahkan migration additive dan aman setelah existing database diperiksa.
5. Jalankan integration test dengan beberapa akun untuk memverifikasi ownership dan public Community feed.
