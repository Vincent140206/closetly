# Kontrak Database Closetly

Dokumen ini menetapkan target logical schema minimal. Ini bukan bukti bahwa schema sudah terpasang dan bukan migration yang boleh langsung dijalankan.

Pada 2 November 2026, introspeksi read-only melalui endpoint OpenAPI dicoba memakai publishable key lokal. Supabase menolak schema introspection karena endpoint tersebut membutuhkan secret key. Secret/service-role key tidak digunakan. Karena schema aktif belum terverifikasi, task standardisasi ini tidak membuat atau menjalankan migration SQL.

Sebelum migration berikutnya, maintainer database harus membandingkan target ini dengan schema aktif melalui Dashboard/SQL Editor atau koneksi database berizin. Migration harus additive, mempertahankan data, dan tidak boleh melakukan `drop table`, truncate, atau destructive rewrite otomatis.

## Delapan tabel aplikasi

### `profiles`

- `id uuid primary key references auth.users(id) on delete cascade`
- `display_name text not null`
- `avatar_path text null`
- `created_at timestamptz`, `updated_at timestamptz`

### `clothing_items`

- `id uuid primary key`, `user_id uuid not null`
- `name`, `category`, `color`, `size` sebagai text
- `image_path text null`
- `is_archived boolean not null default false`
- `created_at`, `updated_at`

Tidak ada kolom global worn status. Status pemakaian berada di `wear_history`.

### `outfits`

- `id uuid primary key`, `user_id uuid not null`
- `name text not null`, `description text null`
- `cover_image_path text null`
- `created_at`, `updated_at`

### `outfit_items`

- `id uuid primary key`, `user_id uuid not null`
- `outfit_id uuid not null references outfits(id) on delete cascade`
- `clothing_item_id uuid not null references clothing_items(id) on delete restrict`
- `position integer not null`
- unique `(outfit_id, clothing_item_id, position)`

### `outfit_plans`

- `id uuid primary key`, `user_id uuid not null`
- `outfit_id uuid not null references outfits(id) on delete restrict`
- `planned_date date not null`
- `activity text not null`, `notes text null`
- `status text not null`: `planned`, `completed`, atau `cancelled`
- `created_at`, `updated_at`

### `wear_history`

- `id uuid primary key`, `user_id uuid not null`
- `outfit_id uuid not null references outfits(id) on delete restrict`
- `source_plan_id uuid null references outfit_plans(id)`
- `planned_date date null`
- `worn_date date null`
- `is_worn boolean not null default false`
- `notes text null`
- `created_at`, `updated_at`
- partial unique index pada `source_plan_id` saat nilainya tidak null

Constraint target:

- Pending planner entry: `source_plan_id` terisi, `is_worn = false`, `worn_date = null`.
- Worn entry: `is_worn = true` dan `worn_date` wajib terisi.
- Manual entry: `source_plan_id = null`, `is_worn = true`, dan `worn_date` terisi.

### `community_posts`

- `id uuid primary key`, `user_id uuid not null`
- `caption text not null`
- `image_path text not null` menuju bucket publik Community
- `source_outfit_id uuid null references outfits(id) on delete set null`
- `created_at`, `updated_at`

`image_path` tidak boleh menunjuk object private Wardrobe/Outfit. Post yang bersumber dari saved outfit harus memiliki asset publik tersendiri.

### `fashion_wishlist`

- `id uuid primary key`, `user_id uuid not null`
- `name`, `category` sebagai text wajib
- `estimated_price numeric(12,2) null` dengan nilai non-negatif
- `store`, `product_url`, `image_path` sebagai text nullable
- `priority text null`: `low`, `medium`, atau `high`
- `is_purchased boolean not null default false`
- `created_at`, `updated_at`

## Lifecycle Planner dan Wear History

Operasi lintas tabel harus dilakukan secara transaksional melalui RPC/database function yang diverifikasi:

1. Create plan membuat `outfit_plans` dan tepat satu pending `wear_history`.
2. Update plan menyinkronkan outfit/planned date pada linked history selama `is_worn = false`.
3. Mark as Worn membutuhkan tanggal aktual, mengubah history menjadi worn, dan status plan menjadi `completed`.
4. Tanggal rencana yang lewat tidak menjalankan perubahan otomatis.
5. Cancel mengubah status plan menjadi `cancelled`; pending history boleh dihapus/ditandai batal sesuai migration final, tetapi history yang sudah worn tidak boleh dihapus.
6. Unique `source_plan_id` menjadikan retry idempotent dan mencegah duplikat.

Nama/signature RPC belum dikunci sampai fungsi aktif di database diperiksa. Jangan membuat dua write terpisah dari Android untuk workflow ini.

## Row Level Security

- `profiles`: user hanya mengubah row dengan `id = auth.uid()`.
- Tabel private: user hanya CRUD row dengan `user_id = auth.uid()`.
- Referensi child harus memverifikasi parent dimiliki user yang sama.
- `community_posts`: public dapat select; authenticated user dapat insert dengan `user_id = auth.uid()`; update/delete hanya author.
- Policy database adalah security boundary. Filter client bukan pengganti RLS.

## Storage

- Private bucket `closetly-images`: avatar, wardrobe, outfit, wishlist, dan asset private lainnya.
- Separate public Community bucket: public read, authenticated write/update/delete hanya pada path berprefix user ID.
- Saat membuat Community post dari saved outfit, salin/render image ke bucket Community; jangan mengubah visibility object private.
- Database menyimpan path, bukan signed URL.

Nama final bucket Community dan policy aktif harus diverifikasi sebelum implementasi repository.

## Checklist migration aman

- Ambil inventory tabel, kolom, constraint, index, trigger, RPC, RLS, dan bucket aktif.
- Buat backup dan uji migration di environment non-production.
- Gunakan `alter table ... add column if not exists` dan backfill bertahap bila sesuai.
- Tambahkan constraint setelah data lama tervalidasi.
- Jangan drop tabel/data lama otomatis.
- Uji dua akun untuk ownership dan anonymous user untuk public Community feed.
- Uji retry Create Plan agar tidak menghasilkan duplicate history.
