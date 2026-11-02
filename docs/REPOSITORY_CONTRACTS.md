# Kontrak Repository

Folder `data/repository` sudah tersedia tetapi sengaja kosong. Pemilik fitur membuat interface saat implementasi dimulai dan baru membuat Supabase implementation setelah tabel/RPC aktif diverifikasi.

## Aturan umum

- Satu interface repository per fitur; tidak ada generic CRUD/base repository.
- ViewModel bergantung pada interface, bukan `SupabaseClient` atau implementation konkret.
- Supabase implementation menggunakan shared client dari application container/factory lokal fitur.
- Repository tidak bergantung pada Composable, `NavController`, atau UI state.
- Operasi memakai nama use case, bukan sekadar `insertRow`/`updateRow`.
- User ID berasal dari authenticated session; jangan percaya user ID dari input screen.
- DTO, mapping, PostgREST, dan Storage call berada di data layer.
- Signed URL bersifat sementara dan tidak disimpan permanen.
- Rethrow `CancellationException`; terjemahkan network/constraint failure menjadi error domain yang jelas.
- Fake repository hanya dibuat untuk test atau UI development yang nyata, bukan sekadar memenuhi struktur.

## Scope operasi per repository

### `WardrobeRepository`

- List/detail clothing item.
- Create/update.
- Archive item; hard delete harus menangani referensi outfit.

### `OutfitRepository`

- List/detail outfit dengan ordered item IDs.
- Create/update kombinasi.
- Delete hanya jika tidak merusak Planner/Wear History.

### `PlannerRepository`

- List plan.
- Create plan + pending history secara atomik.
- Update plan + linked pending history.
- Cancel plan dengan aturan history yang konsisten.
- Mark as Worn dengan tanggal aktual.

Workflow lintas `outfit_plans` dan `wear_history` harus menggunakan RPC/database transaction yang sudah diverifikasi, bukan dua request terpisah.

### `CommunityRepository`

- Public feed tanpa sesi.
- Create post dari image upload.
- Create post dari saved outfit sambil membuat asset publik terpisah.
- Update/delete post milik author.

Repository tidak boleh membuat private Wardrobe/Outfit Storage object menjadi publik.

### `WishlistRepository`

- List, create, update, mark purchased, dan delete wishlist item milik user.

### `WearHistoryRepository`

- List pending dan worn entry.
- Create manual worn entry.
- Mark pending entry as worn dengan tanggal aktual.
- Mencegah duplicate `source_plan_id`.

## Dependency wiring

Auth dan Profile tetap memakai binding `AppContainer` yang sudah bekerja. Enam feature repository tidak didaftarkan secara prematur. Saat implementasi dimulai, pemilik fitur dapat membuat factory lokal yang menerima shared `SupabaseClient`; pindahkan binding ke `AppContainer` hanya jika benar-benar menjadi dependency aplikasi lintas fitur.

Target schema dan requirement RLS tersedia di [`DATABASE_CONTRACT.md`](DATABASE_CONTRACT.md).
