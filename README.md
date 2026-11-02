# Closetly

Closetly adalah aplikasi Android untuk mengelola koleksi pakaian, membuat kombinasi outfit, merencanakan outfit, berbagi inspirasi melalui Community, mencatat wishlist, dan melihat riwayat pemakaian. Proyek ini dibuat sebagai tugas akhir kelompok untuk mata kuliah Pemrograman Aplikasi Mobile.

## Tech Stack

| Teknologi | Kegunaan |
|---|---|
| Kotlin | Bahasa pemrograman utama aplikasi |
| Jetpack Compose | Membuat tampilan Android secara deklaratif |
| Material 3 | Theme dan komponen UI dasar |
| Navigation Compose | Mengatur perpindahan screen dengan type-safe route |
| ViewModel dan StateFlow | State management Auth/Profile dan standar fitur |
| Supabase | Auth/Profile aktif; SDK PostgREST dan Storage tersedia |
| Gradle Kotlin DSL | Konfigurasi build dan dependency |

Auth dan Profile sudah memakai satu shared Supabase client. Enam fitur individual belum memiliki repository/CRUD dan foldernya sengaja hanya berupa struktur kosong untuk diisi pemilik fitur.

## Status Proyek

Branch `develop` saat ini berisi shared application foundation, Auth/Profile, dan struktur folder standar enam fitur.

Yang sudah tersedia:

- Inisialisasi aplikasi Android dan Compose
- Shared theme dan reusable components
- Navigation yang terpusat
- Home screen dengan enam pilihan fitur
- Placeholder untuk setiap fitur
- Email/password Auth, session restoration, logout, dan basic Profile
- Fondasi konfigurasi Supabase, PostgREST, dan Storage
- Folder `data/dto`, `data/repository`, `model`, dan `ui/components` untuk tiap fitur
- Kontrak final delapan tabel dan panduan pengembangan fitur

Yang belum diimplementasikan:

- Enam fitur utama dan root screen masing-masing
- ViewModel dan repository khusus fitur
- Supabase CRUD
- Image upload dan Storage integration

Screen bertuliskan **Not implemented yet** adalah placeholder yang disengaja, bukan error aplikasi. Struktur folder bukan bukti bahwa fitur sudah diimplementasikan.

## Tim dan Pembagian Fitur

| Member | Feature | Branch |
|---|---|---|
| Vincent | Digital Wardrobe | `feature/digital-wardrobe` |
| Janice | Mix & Match Outfit | `feature/mix-and-match` |
| Vara | Outfit Planner | `feature/outfit-planner` |
| Sela | Community | `feature/inspiration-board` |
| Gres | Fashion Wishlist | `feature/fashion-wishlist` |
| Aurel | Wear History | `feature/wear-history` |

Branch `feature/auth` digunakan untuk shared authentication. Branch `feature/profile` digunakan untuk shared profile functionality.

## Getting Started

### Prasyarat

Siapkan:

- Android Studio
- Git
- JDK 17 atau lebih baru; JDK bawaan Android Studio dapat digunakan
- Android SDK 36
- Android emulator atau perangkat Android fisik

Proyek menggunakan Gradle 9.4.1, compileSdk 36.1, targetSdk 36, dan minSdk 26. Source compatibility aplikasi diatur ke Java 11.

### Clone repository

Buka Windows CMD, lalu jalankan setiap command secara terpisah:

```cmd
git clone https://github.com/Vincent140206/closetly.git
```

```cmd
cd closetly
```

### Buka di Android Studio

1. Buka Android Studio.
2. Pilih **Open**.
3. Pilih folder `closetly`.
4. Tunggu Gradle Sync selesai.
5. Pilih emulator atau perangkat fisik.
6. Jalankan konfigurasi `app` dengan tombol **Run**.

Untuk memeriksa debug build dari Windows CMD:

```cmd
gradlew.bat :app:assembleDebug
```

Boilerplate terakhir telah diverifikasi dengan `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:assembleDebugAndroidTest`, dan `:app:lintDebug`.

## Git Branch Workflow

- `main`: versi aplikasi yang stabil.
- `develop`: branch integrasi pekerjaan seluruh tim.
- `feature/*`: branch pengembangan masing-masing fitur.

Semua feature branch pada tabel pembagian tim sudah tersedia. Jangan membuat branch duplikat.

Contoh memulai pekerjaan Digital Wardrobe:

```cmd
git fetch origin
```

```cmd
git switch feature/digital-wardrobe
```

```cmd
git merge origin/develop
```

Anggota lain harus mengganti `feature/digital-wardrobe` dengan branch yang sesuai pada tabel pembagian fitur.

Jika branch belum tersedia secara lokal setelah clone, track branch remote yang sudah ada:

```cmd
git fetch origin
```

```cmd
git switch --track origin/feature/digital-wardrobe
```

Command `--track` hanya diperlukan jika branch tersebut belum ada secara lokal.

## Cara Mengembangkan Fitur

1. Switch ke branch fitur yang sudah ditentukan.
2. Merge perubahan terbaru dari `origin/develop`.
3. Baca [`docs/FEATURE_DEVELOPMENT_GUIDE.md`](docs/FEATURE_DEVELOPMENT_GUIDE.md) dan [`docs/FEATURE_REQUIREMENTS.md`](docs/FEATURE_REQUIREMENTS.md).
4. Buka package fitur milikmu di `app/src/main/java/vin/pam/closetly/feature/`.
5. Implementasikan model, repository, ViewModel, UI state, screen, dan komponen yang memang dibutuhkan fitur.
6. Gunakan shared theme dan components jika sesuai.
7. Test perubahan dan pastikan aplikasi dapat di-build.
8. Commit dan push pekerjaanmu.
9. Buat Pull Request ke `develop`.

Contoh commit dan push untuk Digital Wardrobe:

```cmd
git add .
```

```cmd
git commit -m "feat(wardrobe): implement wardrobe screen"
```

```cmd
git push origin feature/digital-wardrobe
```

Pesan commit harus menjelaskan perubahan yang dibuat. Hindari mengubah package fitur milik anggota lain tanpa koordinasi.

## Aturan Pull Request

- Semua Pull Request fitur harus menargetkan `develop`.
- Jangan merge feature branch langsung ke `main`.
- `main` hanya diperbarui dari `develop` setelah versi terintegrasi selesai diuji.
- Pastikan proyek dapat di-build sebelum membuat Pull Request.
- Selesaikan merge conflict sebelum perubahan digabungkan.
- Koordinasikan perubahan pada shared navigation, Gradle, konfigurasi, dan shared components.

Contoh arah Pull Request:

```text
feature/digital-wardrobe -> develop
```

## Struktur Proyek

```text
app/src/main/java/vin/pam/closetly/
|-- MainActivity.kt
|-- ClosetlyApp.kt
|-- ClosetlyApplication.kt
|-- core/
|   |-- config/
|   |-- data/
|   |-- navigation/
|   `-- ui/
|       |-- components/
|       `-- shell/
|-- di/
|-- feature/
|   |-- auth/
|   |-- profile/
|   |-- wardrobe/
|   |-- outfit/
|   |-- planner/
|   |-- inspiration/     # Community; nama dipertahankan untuk kompatibilitas
|   |-- wishlist/
|   `-- wearhistory/
`-- ui/
    `-- theme/
```

Kegunaan folder penting:

- `core/config`: konfigurasi aplikasi, termasuk nilai Supabase dari BuildConfig.
- `core/data`: penyedia shared data service seperti `SupabaseProvider`.
- `core/navigation`: daftar route, module metadata, dan `AppNavHost`.
- `core/ui/components`: komponen UI yang dapat digunakan oleh beberapa fitur.
- `core/ui/shell`: Home dan module placeholder untuk development shell.
- `di`: application-level dependency container.
- `feature/auth` dan `feature/profile`: shared authentication dan profile.
- Enam package fitur: area kerja masing-masing anggota. Masing-masing sudah memiliki folder `data/dto`, `data/repository`, `model`, dan `ui/components` yang masih kosong.
- `ui/theme`: warna, typography, dan Material 3 theme.

Di dalam package fitur milikmu, struktur folder standarnya adalah:

```text
feature/wardrobe/
|-- data/
|   |-- dto/
|   `-- repository/
|-- model/
`-- ui/
    `-- components/
```

Source file belum dibuat. Pemilik fitur menambahkan model, repository, `<Feature>ViewModel`, `<Feature>UiState`, dan `<Feature>Screen` ketika mulai implementasi.

## Konfigurasi Supabase

Supabase diperlukan untuk Auth/Profile dan kelak untuk data fitur. Aplikasi tetap menyediakan recoverable state jika credential lokal belum ada.

Saat konfigurasi sudah dibutuhkan, buat file `secrets.properties` di root repository:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-publishable-or-anon-key
```

Perhatikan aturan berikut:

- Jangan commit `secrets.properties`.
- Jangan membagikan service-role key atau secret key.
- Gunakan hanya publishable/anon key pada aplikasi Android.
- Aplikasi saat ini tetap dapat berjalan tanpa credential Supabase.
- Auth/Profile sudah diimplementasikan; CRUD enam fitur dan image Storage belum.

## Masalah Umum

### Gradle Sync gagal

Pastikan koneksi internet aktif, lalu pilih **Sync Project with Gradle Files**. Periksa juga apakah Android SDK 36 sudah terpasang melalui SDK Manager.

### Versi Android SDK atau Java tidak cocok

Atur **Gradle JDK** di Android Studio ke JDK 17 atau lebih baru. Gunakan JDK bawaan Android Studio jika ragu. Pastikan compile SDK yang diperlukan sudah terpasang.

### Feature branch tidak ditemukan secara lokal

Jalankan:

```cmd
git fetch origin
```

```cmd
git switch --track origin/nama-feature-branch
```

Ganti `nama-feature-branch` dengan branch milikmu.

### Terjadi merge conflict

Buka file yang konflik, pilih perubahan yang benar bersama pemilik file, lalu jalankan:

```cmd
git add .
```

```cmd
git commit
```

Jangan menghapus perubahan anggota lain tanpa koordinasi.

### Fitur hanya menampilkan "Not implemented yet"

Itu adalah kondisi yang benar untuk standardisasi saat ini. Implementasi fitur harus dibuat pada branch masing-masing.

### Supabase belum dikonfigurasi

Tidak masalah untuk tahap UI awal. Aplikasi dirancang agar dapat berjalan tanpa `secrets.properties`.

## Aturan Penting Tim

- Kerjakan fitur hanya pada branch yang sudah ditentukan.
- Selalu sinkronkan branch dengan `develop`.
- Jangan push langsung ke `main`.
- Jangan mengubah fitur anggota lain tanpa koordinasi.
- Koordinasikan perubahan pada shared files.
- Jangan commit credential, `.env`, `local.properties`, atau `secrets.properties`.
- Test dan build proyek sebelum membuat Pull Request.

Dokumentasi teknis tambahan tersedia di folder [`docs/`](docs/), terutama [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), panduan fitur, kontrak repository/navigation, dan [`docs/DATABASE_CONTRACT.md`](docs/DATABASE_CONTRACT.md). Jaga branch tetap rapi dan saling koordinasi agar pengembangan Closetly berjalan lancar.
