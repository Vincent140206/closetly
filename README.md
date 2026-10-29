# Closetly

Closetly adalah aplikasi Android untuk mengelola koleksi pakaian, membuat kombinasi outfit, merencanakan outfit, menyimpan inspirasi fashion, mencatat wishlist, dan melihat riwayat pemakaian. Proyek ini dibuat sebagai tugas akhir kelompok untuk mata kuliah Pemrograman Aplikasi Mobile.

## Tech Stack

| Teknologi | Kegunaan |
|---|---|
| Kotlin | Bahasa pemrograman utama aplikasi |
| Jetpack Compose | Membuat tampilan Android secara deklaratif |
| Material 3 | Theme dan komponen UI dasar |
| Navigation Compose | Mengatur perpindahan screen dengan type-safe route |
| ViewModel dan StateFlow | Direncanakan untuk state management di setiap fitur |
| Supabase | SDK sudah disiapkan untuk Auth, database, dan Storage |
| Gradle Kotlin DSL | Konfigurasi build dan dependency |

Supabase SDK sudah ada di proyek, tetapi aplikasi belum terhubung ke database Supabase yang aktif.

## Status Proyek

Branch `develop` saat ini hanya berisi application-level boilerplate.

Yang sudah tersedia:

- Inisialisasi aplikasi Android dan Compose
- Shared theme dan reusable components
- Navigation yang terpusat
- Home screen dengan enam pilihan fitur
- Placeholder untuk setiap fitur
- Fondasi konfigurasi Supabase

Yang belum diimplementasikan:

- Enam fitur utama
- Authentication dan profile management yang nyata
- ViewModel dan repository khusus fitur
- Supabase CRUD
- Image upload dan Storage integration

Screen bertuliskan **Not implemented yet** adalah placeholder yang disengaja, bukan error aplikasi.

## Tim dan Pembagian Fitur

| Member | Feature | Branch |
|---|---|---|
| Vincent | Digital Wardrobe | `feature/digital-wardrobe` |
| Janice | Mix & Match Outfit | `feature/mix-and-match` |
| Vara | Outfit Planner | `feature/outfit-planner` |
| Sela | Style Inspiration Board | `feature/inspiration-board` |
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
3. Buka package fitur milikmu di `app/src/main/java/vin/pam/closetly/feature/`.
4. Implementasikan model, repository, ViewModel, UI state, screen, dan komponen yang memang dibutuhkan fitur.
5. Gunakan shared theme dan components jika sesuai.
6. Test perubahan dan pastikan aplikasi dapat di-build.
7. Commit dan push pekerjaanmu.
8. Buat Pull Request ke `develop`.

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
|   |-- inspiration/
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
- Enam package fitur: area kerja masing-masing anggota.
- `ui/theme`: warna, typography, dan Material 3 theme.

Di dalam package fitur milikmu, kamu dapat membuat struktur sederhana seperti:

```text
feature/wardrobe/
|-- data/          # Model, DTO, dan repository
|-- ui/            # Screen, UI state, dan components
`-- WardrobeViewModel.kt
```

Struktur tersebut adalah panduan. Sesuaikan dengan kebutuhan fitur dan jangan membuat file kosong yang tidak digunakan.

## Konfigurasi Supabase

Supabase bersifat opsional untuk boilerplate saat ini dan akan diperlukan pada milestone database integration.

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
- Authentication, CRUD, dan Storage integration belum diimplementasikan.

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

### Aplikasi hanya menampilkan "Not implemented yet"

Itu adalah kondisi yang benar untuk boilerplate saat ini. Implementasi fitur harus dibuat pada branch masing-masing.

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

Dokumentasi teknis tambahan tersedia di folder [`docs/`](docs/). Jaga branch tetap rapi dan saling koordinasi agar pengembangan Closetly berjalan lancar.
