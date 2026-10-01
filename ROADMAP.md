# Roadmap OmniNote Connect

## Keputusan yang sudah diambil

- Konsep: berbagi catatan biasa. Lokasi kegiatan eksak, ditandai sendiri oleh user (bukan GPS live).
- Semua catatan terlihat oleh teman (tidak ada catatan pribadi). Struktur data tetap disiapkan
  supaya pilihan Pribadi/Teman bisa ditambah nanti.
- Pertemanan saling setuju (kirim permintaan -> diterima).
- Tidak ada chat. Diganti komentar di catatan, dengan balasan 1 tingkat.
- Backend: Firebase (Auth + Firestore). Firestore jadi satu-satunya sumber data mulai Fase 2.
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

Catatan sisa Fase 0 (dikerjakan di Fase 1):
- Klik catatan membuka `NoteDetailActivity` yang masih kosong.
- `QueryUtil.nearestQuery()` masih query tabel `course` dari template lama dan belum dipakai.

## Fase 1: Inti to-do + alarm (lokal)

- [ ] Edit catatan dan tanda selesai.
- [ ] Pilih lokasi dengan tap di peta (ganti input lat/lon manual), simpan nama tempat.
- [ ] Alarm: pengingat + notifikasi, izin POST_NOTIFICATIONS (Android 13+), izin exact alarm (Android 12+),
      jadwal ulang setelah HP restart, klik notifikasi buka detail.
- [ ] Setting: dark mode, nyala/mati pengingat, berapa menit sebelum kegiatan.
- [ ] Home: "Hari ini", "Pengingat berikutnya", Insight dari data asli.

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
- [ ] Notifikasi dalam aplikasi untuk permintaan pertemanan.
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

## Fase 6: Polish

- [ ] Tampilan kosong, loading skeleton, animasi transisi, pesan error, ikon aplikasi, onboarding.

## Risiko (perlu verifikasi saat sampai di fasenya)

- Push notification ke HP lain butuh Cloud Functions -> kemungkinan butuh paket Firebase Blaze (berbayar).
- Upload foto profil (Cloud Storage) kemungkinan juga butuh paket Blaze. Alternatif: avatar inisial.
- Google Maps API key butuh billing aktif di Google Cloud.
- Query Firestore `in` dibatasi sekitar 30 nilai -> feed teman perlu desain khusus kalau teman > 30.
