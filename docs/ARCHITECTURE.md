# Arsitektur Closetly

Closetly memakai satu Android application module dengan Jetpack Compose, MVVM per fitur, repository khusus use case, dan satu shared Supabase client. Standardisasi ini hanya menyiapkan batas package dan kontrak; enam fitur belum memiliki source implementation.

## Alur dependensi

```text
Compose screen -> ViewModel -> feature repository -> shared Supabase client
```

- Composable hanya merender state dan mengirim event melalui callback.
- ViewModel memiliki UI state dan mengorkestrasi use case.
- Repository interface mengikuti kebutuhan fitur, bukan generic CRUD abstraction.
- Supabase implementation menangani DTO, mapping, PostgREST, Storage, dan error data layer.
- Database RLS tetap menjadi security boundary utama.

## Batas package

- `core/navigation`: route dan navigation graph terpusat.
- `core/ui`: app shell dan komponen yang benar-benar dipakai lintas fitur.
- `di`: dependency aplikasi yang sudah dipakai bersama.
- `feature/auth` dan `feature/profile`: implementasi shared yang sudah ada dan harus dipertahankan.
- Enam package fitur: area kerja pemilik fitur masing-masing.
- `ui/theme`: Material 3 theme aplikasi.

Setiap package dari enam fitur hanya berisi struktur kosong berikut:

```text
feature/<feature>/
|-- data/
|   |-- dto/
|   `-- repository/
|-- model/
`-- ui/
    `-- components/
```

File `.gitkeep` adalah satu-satunya isi selama tahap standardisasi. Pemilik fitur membuat source file ketika implementasi dimulai.

## Ownership enam fitur

| Fitur | Package | Branch |
|---|---|---|
| Digital Wardrobe | `feature/wardrobe` | `feature/digital-wardrobe` |
| Mix & Match Outfit | `feature/outfit` | `feature/mix-and-match` |
| Outfit Planner | `feature/planner` | `feature/outfit-planner` |
| Community | `feature/inspiration` | `feature/inspiration-board` |
| Fashion Wishlist | `feature/wishlist` | `feature/fashion-wishlist` |
| Wear History | `feature/wearhistory` | `feature/wear-history` |

Nama package dan branch Community dipertahankan untuk kompatibilitas. Nama UI dan domain baru menggunakan istilah Community.

## Navigation

Navigation tetap dimiliki `core/navigation`. Enam root route saat ini menuju placeholder bersama. Pemilik fitur menghubungkan root screen setelah implementasinya compile dan mengoordinasikan perubahan graph agar tidak menimbulkan duplicate route atau conflict lintas branch.

## Data dan integrasi

Delapan tabel logical schema dan aturan RLS/Storage tercatat di [`DATABASE_CONTRACT.md`](DATABASE_CONTRACT.md). Schema Supabase aktif belum dapat diintrospeksi dengan publishable key, sehingga belum ada migration yang dibuat atau dijalankan. Implementasi data harus menunggu inventory schema/RPC/policy oleh maintainer database.

Planner dan Wear History membutuhkan operasi atomik lintas tabel yang diverifikasi. Community menggunakan public feed dan bucket publik terpisah; object private Wardrobe/Outfit tidak boleh diekspos.

## Keputusan yang sengaja ditunda

- Bentuk DTO final sampai kolom database aktif diverifikasi.
- Nama/signature RPC Planner dan Wear History.
- Nama bucket Community dan policy path final.
- Binding repository enam fitur ke application container sampai implementation benar-benar dipakai.
- Route detail/create/edit sampai screen terkait dibuat.

Panduan implementasi anggota tersedia di [`FEATURE_DEVELOPMENT_GUIDE.md`](FEATURE_DEVELOPMENT_GUIDE.md), sedangkan scope tiap fitur tersedia di [`FEATURE_REQUIREMENTS.md`](FEATURE_REQUIREMENTS.md).
