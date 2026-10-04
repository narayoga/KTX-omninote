# Roadmap OmniNote Connect

## Keputusan yang sudah diambil

- Konsep: berbagi catatan biasa. Lokasi kegiatan eksak, ditandai sendiri oleh user (bukan GPS live).
- Semua catatan terlihat oleh teman (tidak ada catatan pribadi). Struktur data tetap disiapkan
  supaya pilihan Pribadi/Teman bisa ditambah nanti.
- Pertemanan saling setuju (kirim permintaan -> diterima).
- Tidak ada chat. Diganti komentar di catatan, dengan balasan 1 tingkat.
- Backend: Firebase (Auth + Firestore). Firestore jadi satu-satunya sumber data mulai Fase 2.
- Firebase tetap di paket gratis **Spark**, di project Firebase **terpisah** dari project Google Maps
  (project Maps sudah punya billing, jadi kalau Firebase dipasang di sana otomatis jadi paket Blaze).
- Notifikasi dari teman (permintaan teman, komentar) **tidak** memakai push FCM + Cloud Functions (butuh Blaze).
  Diganti **WorkManager**: aplikasi mengecek Firestore berkala (minimal tiap 15 menit) lalu menampilkan
  notifikasi lokal. Notifikasi bisa telat >= 15 menit. Jangan menanam kunci service account di APK.
- Foto profil diganti avatar inisial (Cloud Storage hanya tersedia di paket Blaze sejak Feb 2026).
- UI tetap XML. Gaya kode pemula (lihat `CLAUDE.md`).
- Alarm hanya untuk catatan sendiri.

## Struktur navigasi

**Home | Map | [ + ] | Notifikasi | Profile**

- Home: tab "Catatan Saya" dan "Teman", ringkasan "Hari ini", Insight dengan data asli.
- Map: pin catatan saya dan teman, klik pin -> detail (siapa, apa, kapan, di mana).
- [ + ]: tambah catatan.
- Notifikasi: permintaan pertemanan, komentar/balasan baru, pengingat.
- Profile: info saya, daftar teman, cari teman, ikon gear -> Setting.
- Dibuang: Search di header, Wallet, kartu Favorites/Message/Social, data dummy.

## Fase 0: Fondasi dan bersih-bersih (lokal)

- [x] Satu Activity + Fragment, bottom bar pindah tab secara manual (replace fragment).
- [x] Buang Search, Wallet, kartu template, dummy Insight, gambar template yang tidak dipakai.
- [x] Design system: warna (`values/colors.xml` + `values-night/colors.xml`), gaya teks (`values/styles.xml`),
      ukuran jarak (`values/dimens.xml`), ikon vektor, tema terang/gelap.
- [x] Setting: pilihan mode gelap (Ikuti sistem / Gelap / Terang).
- [x] Perbaiki bug: crash `toDouble()` saat lat/lon kosong (`NoteAddActivity`), query sort `ORDER BY :param` (`NoteDao`),
      jam yang dipilih tertulis ke label bukan ke `tv_start_time`/`tv_end_time`.

Selesai kalau: 5 tab bisa dibuka, tombol back wajar, dark mode jalan.

Catatan sisa Fase 0 (sudah dikerjakan di Fase 1):
- [x] Klik catatan membuka `NoteDetailActivity` yang masih kosong.
- [x] `QueryUtil.nearestQuery()` masih query tabel `course` dari template lama (dihapus).

## Fase 1: Inti to-do + alarm (lokal)

- [x] Edit catatan dan tanda selesai (checkbox di daftar + tombol di detail). Layar detail catatan.
- [x] Pilih lokasi dengan menggeser peta (`LocationPickerActivity`), nama tempat terisi otomatis dari alamat
      dan bisa diubah. Database naik ke versi 2 (kolom `placeName`, `isDone`) dengan migrasi, data lama aman.
- [x] Alarm: pengingat + notifikasi (`alarm/`), izin POST_NOTIFICATIONS (Android 13+), izin exact alarm (Android 12+,
      kalau belum diizinkan tetap jalan tapi bisa telat), jadwal ulang setelah HP restart, klik notifikasi buka detail.
- [x] Setting: dark mode, nyala/mati pengingat, berapa menit sebelum kegiatan, status izin alarm tepat waktu.
- [x] Home: "Kegiatan berikutnya", ringkasan (hari ini / selesai / akan datang) dari data asli.

Catatan sisa Fase 1:
- Lokasi masih wajib diisi. Catatan tanpa tempat (mis. "bayar listrik") tetap harus pilih lokasi.
- Data yang sudah diisi di layar tambah catatan hilang kalau layar diputar.
- Belum diuji di HP sungguhan (hanya Robolectric + render Paparazzi).

Selesai kalau: aplikasi to-do + alarm utuh tanpa internet.

## Fase 2: Firebase dan akun

- [ ] Setup Firebase, login/daftar (email + Google).
- [ ] Welcome -> login/daftar -> isi profil (nama, username unik).
- [ ] Catatan pindah ke Firestore (Room dipensiunkan, tombol sync dibuang).
- [ ] Alarm dijadwalkan dari data Firestore.
- [ ] Security rules: hanya pemilik yang bisa mengubah catatannya.

Selesai kalau: login di HP lain, catatan muncul, alarm terjadwal.

## Fase 3: Teman

- [ ] Cari user berdasarkan username.
- [ ] Kirim/terima/tolak/hapus teman.
- [ ] Tab "Teman" di Home (feed catatan teman).
- [ ] Notifikasi permintaan pertemanan: tampil di tab Notifikasi, plus notifikasi HP lewat WorkManager
      (cek Firestore berkala, minimal tiap 15 menit) saat aplikasi tertutup.
- [ ] Security rules: catatan hanya bisa dibaca pemilik dan temannya.

Selesai kalau: dua akun berteman saling lihat catatan, akun ketiga tidak bisa.

## Fase 4: Map

- [ ] Pin catatan saya dan teman (beda warna/avatar).
- [ ] Filter waktu: hari ini / minggu ini / semua.
- [ ] Klik pin -> panel bawah detail -> buka catatan.
- [ ] Clustering pin yang berdekatan.

## Fase 5: Komentar dan balasan

- [ ] Komentar di detail catatan, balasan 1 tingkat.
- [ ] Tab Notifikasi: "X mengomentari catatanmu", "Y membalas komentarmu".
- [ ] Komentar/balasan baru ikut dicek oleh WorkManager yang sama dari Fase 3.

## Fase 6: Polish

- [ ] Tampilan kosong, loading skeleton, animasi transisi, pesan error, ikon aplikasi, onboarding.

## Risiko (perlu verifikasi saat sampai di fasenya)

- Push notification asli (instan) butuh Cloud Functions -> paket Blaze. Untuk sekarang diganti WorkManager
  (lihat Keputusan). Kalau nanti telat 15 menit terasa mengganggu, baru pertimbangkan Blaze + budget alert.
- Paket Blaze tidak punya batas pengeluaran otomatis; budget alert hanya mengirim email.
- Upload foto profil (Cloud Storage) hanya di paket Blaze. Pakai avatar inisial.
- Google Maps API key sudah memakai billing di project Google Cloud tersendiri (key dibatasi package + SHA-1).
- Query Firestore `in` dibatasi sekitar 30 nilai -> feed teman perlu desain khusus kalau teman > 30.
- Kuota Spark Firestore: 50.000 baca / 20.000 tulis per hari. Hati-hati listener yang terus membaca ulang.
