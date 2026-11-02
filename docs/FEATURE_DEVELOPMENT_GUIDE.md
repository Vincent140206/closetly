# Panduan Pengembangan Fitur

Dokumen ini adalah standar kerja enam fitur Closetly. Folder fitur sudah disiapkan, tetapi sengaja belum berisi implementasi Kotlin. Setiap anggota bertanggung jawab membuat source code di package miliknya setelah menyinkronkan branch dengan `develop`.

## Mulai bekerja

1. Jalankan `git fetch origin`.
2. Switch ke branch yang sudah ditentukan; jangan membuat branch duplikat.
3. Merge `origin/develop` ke feature branch.
4. Kerjakan hanya package milikmu kecuali perubahan shared sudah dikoordinasikan.
5. Build, test, lalu buat Pull Request ke `develop`.

## Struktur folder

Setiap package fitur sudah memiliki struktur berikut:

```text
feature/<feature>/
|-- data/
|   |-- dto/
|   `-- repository/
|-- model/
`-- ui/
    `-- components/
```

File `.gitkeep` hanya menjaga folder tetap tercatat Git. Hapus `.gitkeep` dari folder yang sudah memiliki source file.

File yang dibuat oleh pemilik fitur saat implementasi:

```text
feature/<feature>/
|-- data/
|   |-- dto/<Feature>Dto.kt
|   `-- repository/
|       |-- <Feature>Repository.kt
|       `-- Supabase<Feature>Repository.kt
|-- model/<FeatureModel>.kt
|-- ui/
|   |-- components/
|   |-- <Feature>Screen.kt
|   `-- <Feature>UiState.kt
`-- <Feature>ViewModel.kt
```

Jangan membuat DTO atau Supabase repository sebelum tabel aktif diverifikasi. Domain model boleh berbeda dari DTO jika bentuk data aplikasi berbeda dari database.

## Tanggung jawab layer

- `data/dto`: bentuk serializable yang mengikuti kolom Supabase terverifikasi.
- `data/repository`: interface use-case fitur dan implementasi Supabase/fake untuk test.
- `model`: model domain yang dipakai ViewModel dan UI.
- `ui`: screen, UI state, dan komponen khusus fitur.
- `<Feature>ViewModel`: state dan aksi fitur; tidak membuat `SupabaseClient` sendiri.

Alur dependensi tetap:

```text
Compose Screen -> ViewModel -> Feature Repository -> shared SupabaseClient
```

## Repository dan Supabase

- Buat interface khusus fitur; jangan memakai generic CRUD repository.
- Nama operasi harus mewakili use case, misalnya `createPlan`, `markAsWorn`, atau `getPublicFeed`.
- Ambil user ID dari sesi Auth, bukan parameter UI yang dapat dipalsukan.
- Gunakan `AppContainer.supabaseClient` melalui factory/dependency lokal fitur. Jangan membuat client baru.
- Hormati RLS dan tangani empty result, network error, serta constraint conflict.
- Simpan Storage path, bukan signed URL.
- Jangan mendaftarkan semua dependency masa depan ke `AppContainer` sebelum implementasinya benar-benar digunakan.

## ViewModel dan Compose

- Gunakan `MutableStateFlow` privat dan `StateFlow` publik melalui `asStateFlow()`.
- Jalankan operasi suspend di `viewModelScope` dan rethrow `CancellationException`.
- UI mengumpulkan state dengan `collectAsStateWithLifecycle()`.
- Composable menerima state dan callback; jangan menerima `NavController` atau memanggil Supabase langsung.
- Gunakan `ClosetlyScaffold`, `LoadingState`, `EmptyState`, dan `ErrorState` jika sesuai.

## Navigation

Enam route utama sudah terdaftar dan masih membuka placeholder bersama. Pemilik fitur mengganti placeholder dengan root screen setelah implementasi siap.

- Gunakan serializable object untuk destination tanpa argument.
- Gunakan serializable data class dengan ID ringan untuk detail/edit.
- Jangan mengirim seluruh model melalui navigation.
- Perubahan `Routes.kt` dan `AppNavHost.kt` wajib dikoordinasikan karena dipakai semua anggota.
- Pertahankan callback `onBack` dan perilaku back stack.

## Pengujian

Minimal sebelum Pull Request:

- Unit test ViewModel/repository mapping yang mengandung logic.
- Compose test untuk state penting jika screen sudah dibuat.
- Verifikasi empty, loading, error, dan success state.
- Jalankan `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
- Jalankan `gradlew.bat :app:lintDebug` jika memungkinkan.
- Uji Supabase hanya terhadap project yang benar-benar dikonfigurasi; jangan mengklaim integration test jika tidak dijalankan.

## Pull Request

- Arah PR: `feature/* -> develop`.
- Jelaskan scope, tabel/RPC/Storage yang dipakai, hasil test, dan limitation.
- Jangan sertakan `secrets.properties`, `local.properties`, `.env`, token, atau service-role key.
- Perubahan schema, navigation, Gradle, Auth/Profile, dan shared UI membutuhkan review tim.

Detail scope setiap fitur tersedia di [`FEATURE_REQUIREMENTS.md`](FEATURE_REQUIREMENTS.md).
