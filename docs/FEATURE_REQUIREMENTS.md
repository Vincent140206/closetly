# Panduan Scope Enam Fitur

Semua package di bawah ini masih kosong dan belum merupakan implementasi fitur. Nama package/branch dipertahankan untuk kompatibilitas.

## Digital Wardrobe — Vincent

- Package: `feature/wardrobe`
- Branch: `feature/digital-wardrobe`
- Tujuan: mengelola koleksi pakaian pribadi.
- Tabel: `clothing_items`.
- Screen yang disarankan: daftar wardrobe dan form create/edit.
- Model utama: `ClothingItem`.
- Repository: list/detail, create/update, serta archive; hard delete harus mempertimbangkan referensi `outfit_items`.
- Dependency: datanya dikonsumsi Mix & Match.
- Test: mapping DTO, filter/archive, empty/error state, dan ownership/RLS.

## Mix & Match Outfit — Janice

- Package: `feature/outfit`
- Branch: `feature/mix-and-match`
- Tujuan: membuat kombinasi dari item wardrobe yang sudah tersimpan.
- Tabel: `outfits`, `outfit_items`.
- Screen yang disarankan: daftar outfit, detail, dan form pemilihan pakaian.
- Model utama: `Outfit`, `OutfitItem`.
- Repository: list/detail, create/update kombinasi, dan delete yang aman.
- Dependency: Digital Wardrobe; dipakai Planner dan Community.
- Test: urutan item, referensi pakaian milik user, empty/error state, dan constraint conflict.

## Outfit Planner — Vara

- Package: `feature/planner`
- Branch: `feature/outfit-planner`
- Tujuan: membuat dan mengelola jadwal outfit.
- Tabel: `outfit_plans` dan integrasi transaksional ke `wear_history`.
- Screen yang disarankan: kalender/list plan, form plan, dan detail dengan aksi **Mark as Worn**.
- Model utama: `OutfitPlan`.
- Repository: list, create, update, cancel, dan `markAsWorn(actualDate)`.
- Dependency: Mix & Match dan Wear History.

Aturan final:

- Create plan harus menghasilkan satu `wear_history` pending dengan `is_worn = false`.
- Tanggal lewat tidak boleh otomatis menandai outfit sudah dipakai.
- **Mark as Worn** mengubah entry terkait menjadi `is_worn = true`, mengisi tanggal aktual, dan menyelesaikan plan.
- Update plan menyinkronkan entry pending selama belum worn.
- Cancel plan membatalkan plan dan menangani entry pending secara konsisten; history yang sudah worn tidak boleh dihapus.
- `source_plan_id` harus unik saat tidak null agar retry tidak membuat duplikat.
- Test wajib mencakup retry/idempotency, update, cancel, dan manual Mark as Worn.

Implementasi membutuhkan RPC/database transaction yang sudah diverifikasi. Jangan melakukan dua write terpisah dari UI.

## Community — Sela

- Package tetap: `feature/inspiration`
- Branch tetap: `feature/inspiration-board`
- Tujuan: public feed inspirasi outfit.
- Tabel: `community_posts`.
- Screen yang disarankan: public feed, create post, dan edit post milik sendiri.
- Model utama: `CommunityPost`.
- Repository: public feed; create dari upload; create dari saved outfit; edit/delete milik author.
- Dependency: Auth/Profile dan optional Mix & Match untuk sumber outfit.

Aturan MVP:

- Feed dapat dibaca publik.
- Create memerlukan user terautentikasi.
- Post berisi image dan caption.
- Saat memakai saved outfit, buat/copy asset publik khusus post; jangan mengekspos object private wardrobe/outfit.
- Hanya author dapat update/delete post.
- Tidak ada likes, comments, follow, atau notifications.
- Gunakan bucket Community publik yang terpisah dengan write policy berdasarkan prefix user ID.
- Test mencakup anonymous read, authenticated create, ownership update/delete, dan isolasi private Storage.

## Fashion Wishlist — Gres

- Package: `feature/wishlist`
- Branch: `feature/fashion-wishlist`
- Tujuan: mengelola rencana pembelian fashion.
- Tabel: `fashion_wishlist`.
- Screen yang disarankan: daftar wishlist dan form create/edit.
- Model utama: `WishlistItem`.
- Repository: list, create/update, mark purchased, dan delete.
- Dependency: tidak bergantung pada fitur lain selain Auth.
- Test: validasi harga/prioritas, purchased state, empty/error state, dan ownership/RLS.

## Wear History — Aurel

- Package: `feature/wearhistory`
- Branch: `feature/wear-history`
- Tujuan: menampilkan pemakaian aktual dan pending entry dari Planner.
- Tabel: `wear_history`.
- Screen yang disarankan: daftar/history, detail, form manual entry, dan aksi **Mark as Worn** untuk pending entry.
- Model utama: `WearRecord`.
- Repository: list, create manual worn entry, dan mark pending entry as worn.
- Dependency: Mix & Match dan Outfit Planner.

Aturan final:

- Entry dari Planner dimulai dengan `is_worn = false`.
- Manual entry menggunakan `source_plan_id = null` dan mencatat tanggal aktual.
- Satu planner record tidak boleh memiliki lebih dari satu Wear History entry.
- Tidak ada global worn status pada `clothing_items`.
- Test mencakup pending/worn filter, manual entry, duplicate prevention, update/cancel sync, dan RLS.
