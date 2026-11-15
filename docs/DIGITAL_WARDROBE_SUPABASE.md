# Integrasi Digital Wardrobe

Branch: `feature/digital-wardrobe`. Data koleksi akun yang login menggunakan `public.clothing_items` melalui shared Supabase client aplikasi. Konfigurasi tetap `SUPABASE_URL` dan `SUPABASE_ANON_KEY` di `secrets.properties`; jangan gunakan service-role key di Android.

## Database dan foto

Schema dan policy mengikuti hasil inspeksi pemilik project pada 14 November 2026, dengan metadata tambahan di migration `20261114010000_extend_clothing_items_for_wardrobe.sql`. Pemilik mengonfirmasi SQL berhasil dijalankan. Aplikasi menggunakan `archived_at`, bukan kolom `is_archived`.

Tambah, baca, edit, favorit, arsip, dan pulihkan langsung memakai Supabase. Request tambah mempertahankan UUID selama retry agar respons yang hilang setelah commit tidak membuat baris baru. Edit metadata tidak menimpa foto atau favorit yang berubah dari perangkat lain. Kategori lama yang belum dikenali tetap dipertahankan saat diedit.

Foto kamera/galeri, kalibrasi white balance, dan penghapusan background tetap diproses di perangkat. Foto asli dan hasil disimpan sebagai PNG sRGB di bucket private `closetly-images`, maksimal 5 MiB per file. Path berformat `<uid>/wardrobe/<item-id>/<digest>-original.png` dan `-processed.png`. Database menyimpan path Storage, sedangkan UI memakai cache file private per akun dari download authenticated. PNG mempertahankan transparansi; foto diperkecil jika ukuran melampaui batas bucket.

Menghapus pakaian berarti mengarsipkan, sehingga referensi outfit dan foto tetap ada. Object foto lama juga dipertahankan; pembersihan object yang tidak lagi direferensikan belum diimplementasikan. Upload yang terputus setelah satu file tersimpan dapat meninggalkan object tanpa referensi jika pengguna membatalkan retry.

## Pembacaan dan pemulihan error

Koleksi mulai dimuat saat halaman Lemari aktif, tanpa request tambahan dari inisialisasi ViewModel. Hasil list disimpan di memori selama 60 detik, termasuk hasil kosong. Refresh yang bertumpuk memakai hasil pemuatan pertama yang berhasil. Search dan filter bekerja pada data yang sudah dimuat. Tombol muat ulang dan tombol Coba lagi melewati cache. Perubahan dari perangkat lain terlihat setelah refresh saat cache kedaluwarsa, atau langsung setelah muat ulang manual; tidak ada langganan realtime.

Response tambah/edit/favorit/arsip langsung memperbarui daftar di memori tanpa menunggu download foto. Refresh hanya membaca metadata dan path foto yang sudah ada di cache. Foto hasil diminta ketika kartu masuk komposisi lazy grid atau detail dibuka, dengan maksimal empat pemuatan foto bersamaan. Prefetch Compose dapat mencakup beberapa kartu di dekat viewport. Search/filter tidak melakukan request tabel. Foto asli baru diambil ketika pengguna membuka Atur foto, sehingga edit metadata tidak mengunduh foto asli.

Cache remote baru berada di `cacheDir/wardrobe-remote/<scope-akun>` dan boleh dibuang Android; pemuatan berikutnya dapat mengunduhnya ulang. Lokasi ini dikecualikan dari [Auto Backup Android](https://developer.android.com/identity/data/autobackup). Foto hasil edit yang belum diunggah tetap berada di filesDir agar draft dapat dipulihkan. File cache dari instalasi versi lama belum dimigrasikan atau dihapus otomatis.

Pembacaan objek foto yang sama berbagi satu download dan file ditulis secara atomik. Hasil download yang terlambat hanya memperbarui foto jika record dan path-nya masih sesuai; metadata terbaru dan item yang sudah dihapus tidak boleh ditimpa. Jika refresh gagal, data yang sudah tampil tetap tersedia dan percobaan berikutnya tetap dapat mengambil data lagi. Urutan pagination dan pemilik path foto diperiksa sebelum daftar baru dipublikasikan.

Menekan aksi pakaian yang sama berulang kali selama proses tidak menambah antrean write. Tombol favorit/edit/arsip/pulihkan yang berkonflik dinonaktifkan selama write untuk item tersebut. Detail yang dipulihkan setelah recreation kembali menjalankan refresh dengan cache yang sama dan menyediakan tombol retry ketika gagal.

Setiap request memiliki batas waktu: 30 detik untuk tabel, 90 detik upload, dan 60 detik download foto. Error jaringan, sesi, izin, konflik, batas ukuran, rate limit, response yang tidak dapat dibaca, dan gangguan server diterjemahkan ke pesan Indonesia tanpa detail request mentah. Retry dilakukan oleh pengguna; tidak ada loop retry otomatis. Pembatalan saat meninggalkan layar tetap membatalkan coroutine.

Form edit yang gagal dimuat menampilkan Coba muat lagi dan memblokir simpan. Gagal menyimpan mempertahankan draft agar bisa dicoba lagi. Kegagalan pemrosesan foto mengembalikan kontrol editor sehingga pengguna bisa mencoba lagi atau memakai foto asli. Modal arsip, konfirmasi keluar, editor foto, dan bottom sheet filter memakai latar putih dengan teks tema terang, termasuk saat aplikasi memakai dark mode.

Reload form mempertahankan draft terbaru dan form yang sudah berhasil disimpan menolak simpan ulang. Jika pratinjau foto hilang tetapi foto asli tersedia, editor dapat memakai foto asli dengan penyesuaian direset dan pesan penjelasan. File capture milik aplikasi dibersihkan setelah impor, pembatalan, atau kegagalan membuka kamera; file galeri tidak dihapus. Pembatalan dari timeout parent tetap diteruskan sebagai cancellation sesuai [kontrak coroutine Kotlin](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-timeout.html).

## Pengujian dengan akun project

1. Jalankan build dengan konfigurasi project yang sama, lalu login akun A. Profil akun harus tersedia karena `clothing_items.user_id` mereferensikan `profiles.id`.
2. Buka Lemari → Koleksi. Tambahkan pakaian dengan nama, kategori, warna, ukuran, dan foto galeri. Pastikan nama/warna/ukuran wajib diisi.
3. Periksa Table Editor: hanya satu baris dengan `user_id` akun A; path foto berupa path Storage, bukan path file perangkat atau URL publik.
4. Periksa bucket: dua PNG berada di folder UID akun A, subfolder `wardrobe`. Ulangi lewat kamera dan kalibrasi; background hasil harus transparan.
5. Tutup dan buka aplikasi, lalu edit metadata, ganti foto, hapus foto, dan ubah favorit. Pastikan perubahan tetap ada setelah aplikasi dibuka ulang atau dibaca dari perangkat lain.
6. Arsipkan pakaian: `archived_at` terisi. Pulihkan dari filter arsip: nilainya kembali null. Baris dan referensi outfit tetap ada.
7. Putuskan jaringan saat menyimpan, sambungkan kembali, lalu ulangi simpan. Pastikan UUID pakaian dan jumlah baris tidak bertambah ganda.
8. Login akun B dan pastikan koleksi A tidak terlihat. Uji pula request dengan sesi B terhadap ID dan path milik A: RLS harus menolak baca/tulis foto dan perubahan baris. Menyembunyikan data di UI saja belum membuktikan RLS.

Pengujian unit memakai remote palsu untuk skenario retry, pagination, dan pergantian sesi; ini tidak membuktikan policy project live. Uji end-to-end dengan dua akun di atas tetap diperlukan.

## Hasil verifikasi implementasi

Pada 14 November 2026, build debug dan APK tes berhasil, 37 unit test lulus, dan enam tes emulator Wardrobe lulus. Tes mencakup alur CRUD melalui repository lokal pengujian, impor foto kamera dengan orientasi EXIF, segmentasi background, serta resize PNG di bawah 5 MiB dengan alpha tetap ada. Tes resize dijalankan ulang pada APK akhir dan lulus. Lint tidak melaporkan error; 19 warning terkait konfigurasi/dependency/resource yang sudah ada tetap tercatat.

Sesudah perbaikan cache dan penanganan error, 43 unit test lulus. Tes membuktikan 20 pemanggilan refresh bersamaan menghasilkan satu pemuatan, cache daftar kosong tetap berlaku, expiry/force reload mengambil data baru, dan refresh gagal mempertahankan daftar serta dapat diulang. Delapan skenario emulator Wardrobe telah lulus, termasuk blokir simpan saat detail gagal dimuat, retry tanpa kehilangan draft, dan pencegahan write favorit berulang. Alur koleksi diuji kembali dengan dark theme; screenshot filter, arsip, dan konfirmasi keluar diperiksa dan latarnya putih. Fixture PNG serta screenshot tes memakai direktori files agar tidak terhapus oleh cache eviction Android pada emulator yang penyimpanannya menipis. Build dan lint akhir lulus tanpa error baru.

Probe read-only dengan publishable key tanpa sesi menerima HTTP 401, kode PostgreSQL `42501` (`permission denied for table clothing_items`). Ini mengonfirmasi akses tabel anonymous ditolak, tetapi belum memverifikasi grant authenticated, kolom migration melalui endpoint, CRUD akun login, atau isolasi RLS dua akun. Tidak ada data project yang ditambah atau diubah selama probe ini.

Koleksi lokal dari versi sebelum integrasi tidak otomatis diunggah. File lokal tetap tersimpan; akun yang login mulai membaca koleksi Supabase. Operasi CRUD membutuhkan jaringan. Mode demo lokal hanya digunakan saat Supabase tidak dikonfigurasi dan tidak ada akun.
