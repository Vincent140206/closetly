# Cara memakai data Digital Wardrobe

Panduan singkat buat fitur yang membutuhkan pakaian dari lemari pengguna, misalnya Mix & Match. Data pakaian sudah disimpan di tabel Supabase `clothing_items`; foto berada di bucket private `closetly-images`.

## 1. Pakai repository yang sudah ada

Merge `develop` terbaru ke branch fiturmu, pakai konfigurasi Supabase project yang sama, lalu login. Tanpa konfigurasi dan tanpa akun, aplikasi memakai data demo lokal.

Di `AppNavHost`, `wardrobeGraph` sudah tersedia. Ambil repository-nya:

```kotlin
val pakaianRepository = wardrobeGraph.repository
```

Teruskan ke constructor/factory ViewModel fiturmu sebagai `WardrobeRepository`. Kalau wiring dilakukan di tempat lain, ambil instance yang sama lewat:

```kotlin
val pakaianRepository = WardrobeGraph.forAccount(
    context = context.applicationContext,
    accountId = authenticatedUser?.id, // akun yang sedang login
    supabase = container.supabaseClient,
).repository
```

Gunakan client dari `AppContainer`, bukan membuat client baru. Saat akun berubah, gunakan graph dan ViewModel untuk akun baru juga. Contoh wiring yang sudah berjalan ada di [`AppNavHost.kt`](../app/src/main/java/vin/pam/closetly/core/navigation/AppNavHost.kt).

## 2. Muat data, lalu dengarkan perubahannya

Contoh sederhana di ViewModel; import kelas bisa dibantu Android Studio:

```kotlin
class PilihPakaianViewModel(
    private val wardrobe: WardrobeRepository,
) : ViewModel() {
    val pakaian = wardrobe.items
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun muat(ulang: Boolean = false) = viewModelScope.launch {
        _error.value = null
        try {
            wardrobe.refresh(force = ulang)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            _error.value = error.message ?: "Pakaian belum dapat dimuat. Coba lagi."
        }
    }
}
```

Di Compose, baca `pakaian` dan `error` dengan `collectAsStateWithLifecycle()`. Panggil `muat()` saat halaman dibuka, lalu tampilkan pesan error jika gagal. Untuk tombol **Coba lagi**, panggil `muat(ulang = true)`.

`items` awalnya bisa kosong karena belum dimuat. Isinya akan diperbarui setelah refresh, perubahan pakaian, atau pemuatan foto. Untuk pilihan pakaian baru, saring dengan `items.filterNot { it.isArchived }`. Search/filter cukup dilakukan pada daftar ini.

Refresh biasa memakai cache 60 detik. Jangan panggil refresh di setiap kartu atau setiap pengguna mengetik. Tidak ada realtime otomatis; perubahan dari perangkat lain terlihat setelah refresh.

## 3. Data mana yang dipakai?

| Field `ClothingItem` | Kegunaan |
|---|---|
| `id` | ID pakaian asli. Simpan ini saat pakaian dipilih ke outfit; jangan membuat ID pengganti. |
| `name`, `color`, `size`, `style` | Informasi untuk tampilan dan filter. |
| `category.key` | Nilai kategori: `atasan`, `bawahan`, `sepatu`, `aksesori`, `lainnya`. |
| `categoryLabel` | Teks kategori untuk ditampilkan ke pengguna. |
| `isArchived` | Jangan tawarkan item arsip untuk pilihan baru. Di database, arsip memakai `archived_at`. |
| `imagePath` | Foto hasil edit di perangkat; bisa kosong sebelum dimuat atau jika pakaian tanpa foto. |

Menghapus pakaian dari koleksi berarti mengarsipkan. Referensi outfit lama tetap perlu menyimpan ID pakaian itu. Jika menyimpan relasi outfit ke Supabase, gunakan ID asli sebagai `outfit_items.clothing_item_id`, mengikuti schema yang sudah diverifikasi.

## 4. Cara menampilkan foto

Refresh daftar tidak langsung mengunduh semua foto. Saat kartu/detail tampil, panggil `wardrobe.loadPhoto(item.id)` dari coroutine ViewModel dan tangani error seperti contoh di atas. Setelah selesai, baca item terbaru dari flow `items`, karena `imagePath` dapat berubah.

UI dapat memakai komponen yang sudah tersedia:

```kotlin
WardrobePhoto(item.imagePath, item.name, Modifier.size(96.dp))
```

Foto ini sudah memakai hasil edit/kalibrasi dari Wardrobe, jadi tidak perlu diproses ulang di fitur lain.

`image_path` di database adalah path Storage, bukan URL publik. `imagePath` pada model aplikasi adalah file lokal hasil download. Jangan simpan file lokal tersebut ke database fitur lain. Simpan ID pakaian, lalu muat kembali data/fotonya lewat repository.

`getItem(id)` mengambil detail pakaian. `loadPhotoForEditing(id)` baru diperlukan kalau membuat editor foto, karena ikut mengambil foto asli. Foto private Wardrobe tidak langsung dipakai sebagai asset publik Komunitas; buat asset publik terpisah untuk publikasi.

## Catatan buat pemilik fitur

**Mix & Match belum otomatis memakai koleksi Supabase.** `OutfitGraph` masih memakai `FakeOutfitRepository` dengan `SampleWardrobe`. Pemilik fitur perlu mengganti sumber pilihan pakaiannya dengan data dari repository di atas. Jika model tujuan hanya mengenali empat kategori, tentukan penanganan kategori `lainnya`; jangan diam-diam mengubahnya menjadi atasan.

Planner dan Wear History biasanya membutuhkan **ID outfit**, bukan ID pakaian. Ambil pakaian untuk membentuk outfit terlebih dahulu; keduanya adalah data yang berbeda.

Cara mencoba: login, tambah satu pakaian dari Digital Wardrobe, buka pemilih di fiturmu dan refresh. Pastikan ID/nama sesuai, foto muncul setelah dimuat, dan pakaian yang diarsipkan tidak muncul sebagai pilihan baru.

Referensi API: [`WardrobeRepository.kt`](../app/src/main/java/vin/pam/closetly/feature/wardrobe/data/repository/WardrobeRepository.kt). Detail schema, CRUD, dan foto: [`DIGITAL_WARDROBE_SUPABASE.md`](DIGITAL_WARDROBE_SUPABASE.md).
