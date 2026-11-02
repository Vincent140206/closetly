# Onboarding Tim

## Mulai mengembangkan fitur

1. Clone repository dan buka proyek di Android Studio.
2. Fetch remote, lalu checkout branch fitur yang sudah ditentukan; jangan membuat branch duplikat.
3. Merge perubahan terbaru dari `develop` ke branch fitur dan selesaikan conflict sebelum menambah functionality.
4. Kerjakan package `feature/<nama>` milikmu. Struktur folder standar sudah tersedia; hapus `.gitkeep` hanya dari folder yang sudah memiliki source file.
5. Ikuti [`FEATURE_DEVELOPMENT_GUIDE.md`](FEATURE_DEVELOPMENT_GUIDE.md) dan scope di [`FEATURE_REQUIREMENTS.md`](FEATURE_REQUIREMENTS.md).
6. Buat model, repository interface, ViewModel, UI state, screen, dan test sesuai kebutuhan nyata fitur.
7. Gunakan shared theme dan app-level UI primitives jika sesuai; simpan komponen khusus fitur di package fitur.
8. Koordinasikan route type-safe baru dengan navigation maintainer sebelum mengubah `Routes.kt` atau `AppNavHost.kt`.
9. Jalankan `gradlew.bat :app:assembleDebug :app:testDebugUnitTest` di Windows.
10. Push branch fitur dan buat Pull Request menuju `develop`.

## Aturan ownership

- Ubah hanya package fitur milikmu, kecuali shared change sudah disepakati.
- Jangan membuat `SupabaseClient` baru di setiap fitur atau mengeksposnya langsung ke Composable.
- Jangan commit `local.properties`, `secrets.properties`, `.env`, keystore, atau service-role key.
- Jaga shared change tetap kecil agar aman di-merge oleh branch lain.
- `feature/inspiration` dan branch `feature/inspiration-board` sekarang dimiliki fitur Community dan tidak perlu di-rename.
