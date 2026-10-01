# OmniNote Connect

Aplikasi to-do + alarm yang dikembangkan jadi to-do berbasis sosial (catatan bisa dilihat teman, ada lokasi di peta, ada komentar).
Plan tahapan ada di `ROADMAP.md`. Selalu cek fase yang sedang dikerjakan di sana sebelum mulai.

## Gaya kode (WAJIB diikuti)

Pemilik proyek masih belajar Kotlin. Kode harus terlihat natural seperti ditulis pemula dan mudah dipahami,
mengikuti gaya kode yang sudah ada di repo ini.

- Pakai pola yang sudah ada: Activity/Fragment + ViewBinding, `setOnClickListener` biasa,
  fungsi `setupView()` / `setupAction()`, `lateinit var` untuk view dan viewModel.
- ViewModel + LiveData + ViewModelFactory manual. JANGAN pakai Hilt/Koin, Flow/StateFlow, Compose.
- Pakai `if/else`, `when` sederhana, dan `for` biasa. JANGAN merangkai `let/apply/also/run/map/filter/fold`.
- JANGAN pakai sealed class, extension function, generic buatan sendiri, delegate buatan sendiri, DSL,
  atau abstraksi "pintar" lainnya.
- Kode yang berulang di beberapa layar boleh dibiarkan berulang (misalnya `setupView()` di tiap Activity).
  Jangan dipaksa jadi base class.
- Bottom navigation pakai cara manual: `supportFragmentManager.beginTransaction().replace(...)`.
  JANGAN pakai Navigation Component / nav graph.
- UI tetap XML (bukan Jetpack Compose).
- Komentar pendek dalam Bahasa Indonesia, hanya di bagian yang konsepnya baru (alarm, fragment, firebase, dll).
- "Gaya pemula" BUKAN berarti sengaja salah: tetap cek input kosong sebelum `toDouble()`,
  hindari `!!` yang bisa crash, tangani izin (permission) dengan benar. Tulis pengecekannya dengan `if` biasa.

## Build

- Proyek memakai Gradle 8.0 + AGP 8.1.1, butuh **JDK 17** (tidak jalan di JDK 21).
- Butuh Android SDK (platform android-34, build-tools 34). Lokasi SDK dari env `ANDROID_HOME`.
- Cek compile: `bash ./gradlew assembleDebug` (file `gradlew` belum punya izin eksekusi, jadi panggil lewat `bash`).
- Di cloud session: set `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` dan `ANDROID_HOME=/opt/android-sdk`.
  Kalau download dependency kena HTTP 429 dari Maven Central, ulangi build dengan `--max-workers=1` setelah jeda.
- `MAPS_API_KEY` dibaca dari `local.properties` (boleh kosong untuk compile).
