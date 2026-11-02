# Remaining app-level TODOs

- Smoke-test Auth/Profile terhadap Supabase aktif pada emulator/perangkat dengan akun uji.
- Decide when Home should become auth-gated; it intentionally remains open for current development.
- Inventory schema/RPC/RLS/Storage Supabase aktif memakai akses maintainer database.
- Siapkan migration additive untuk delapan tabel final, Community public bucket, dan Planner/Wear History pending workflow; jangan drop data lama otomatis.
- Implementasikan enam fitur pada branch pemilik masing-masing; folder saat ini hanya struktur kosong.
- Add navigation instrumentation tests when an emulator or managed device is available in CI.
- Add a release build configuration, signing strategy, and production shrinker rules before distribution.
- Add automated formatting/static analysis if the team agrees on a tool and configuration.
- Review app-level dependency bindings after the first two feature repositories are implemented; avoid adding a framework prematurely.

Individual feature work is tracked by each feature owner and is not an app-shell TODO.
