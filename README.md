# Tax Calculator

Program konsol Java untuk menghitung pajak pada skema progresif sederhana. Tarif latihan dipertahankan dari source awal, tanpa penetapan negara atau tahun aturan pajak. Simbol `$` pada keluaran mempertahankan format nominal program lama.

Program memakai Java Standard Library tanpa dependency tambahan atau database.

## Skema perhitungan

Tarif berlaku pada bagian pendapatan di setiap lapisan:

| Bagian pendapatan | Tarif |
| --- | --- |
| `0` sampai `50000` | 5% |
| Di atas `50000` sampai `100000` | 10% |
| Di atas `100000` | 15% |

Contoh pendapatan `120000`: `50000 × 5% + 50000 × 10% + 20000 × 15% = 10500.00`.

## Kebutuhan

- JDK 11 atau lebih baru untuk script build. Atur `JAVA_HOME` atau sediakan `java`, `javac`, dan `jar` di PATH.
- PowerShell untuk menjalankan `build.ps1` di Windows.
- NetBeans atau Ant bersifat opsional; metadata project disertakan.

Build menargetkan Java 8 melalui `--release 8`.

## Build dan jalankan

```powershell
git clone https://github.com/zalamanga/Tax-calculator.git
Set-Location -LiteralPath '.\Tax-calculator'
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1 -Task Build
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1 -Task Test
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1 -Task Run
```

Jika project sudah ada di komputer, jalankan perintah dari folder yang berisi `src/` dan `build.ps1`. Task Build membuat `dist/tax-calculator.jar`; Test menjalankan tes mandiri tanpa JUnit, dan Run membangun lalu menjalankan program. Pengaturan execution policy pada contoh hanya berlaku untuk proses PowerShell tersebut.

JAR yang sudah dibangun juga dapat dijalankan langsung:

```powershell
java -jar .\dist\tax-calculator.jar
```

Di NetBeans, pilih Open Project, buka folder project ini, lalu jalankan Clean and Build atau Run. Main class adalah `taxcalculator.TaxCalculator`. Jika Ant tersedia, gunakan:

```powershell
ant test jar
```

## Masukan dan keluaran

Contoh sesi interaktif; angka setelah prompt adalah masukan pengguna:

```text
Masukkan jumlah pendapatan Anda: 120000
Pajak pada skema ini: $10500.00
```

- Pendapatan harus berupa angka tidak negatif; `0` diterima dan menghasilkan `$0.00`.
- Gunakan digit `0` sampai `9`, maksimal 18 digit pada bagian bulat. Pecahan desimal opsional berupa titik diikuti 1 atau 2 digit. Contoh: `120000.50`.
- Nilai terbesar yang diterima konsol adalah `999999999999999999.99`.
- Koma, pemisah ribuan, tanda `+`, bilangan negatif, dan notasi ilmiah tidak diterima.
- Program menampilkan pesan saat masukan tidak valid, lalu meminta masukan ulang. Setelah satu perhitungan berhasil, program selesai.
- Akhir masukan (EOF) mengakhiri program tanpa mencetak hasil yang belum dihitung.

Nilai pajak ditampilkan dengan dua digit desimal menggunakan `RoundingMode.HALF_UP`. Contoh: pendapatan `0.10` menghasilkan nilai pajak eksak `0.005`, lalu ditampilkan sebagai `$0.01`.

## API perhitungan

Kelas berikut berada dalam package `taxcalculator`:

```java
TaxRules.calculateTax(new BigDecimal("120000")); // nilai eksak 10500
TaxRules.calculateTax(new BigDecimal("0.10"));   // nilai eksak 0.005
TaxCalculator.calculateTax(120000.0);             // 10500.0
```

`TaxRules.calculateTax(BigDecimal)` mengembalikan `BigDecimal` eksak yang belum dibulatkan. Batas 18 digit dan 2 angka desimal berlaku pada masukan konsol; helper ini menerima nilai nonnegatif dengan presisi lebih besar. Nilai `null` atau negatif ditolak dengan `IllegalArgumentException`.

API lama `TaxCalculator.calculateTax(double)` tetap tersedia sebagai adapter tanpa pembulatan dua desimal. Masukan negatif, `NaN`, dan infinity ditolak. Gunakan API `BigDecimal` saat membutuhkan perhitungan desimal eksak.

## Struktur repository

```text
src/taxcalculator/TaxCalculator.java  Konsol dan adapter API lama
src/taxcalculator/TaxRules.java       Aturan perhitungan desimal
test/                                Tes aturan dan perilaku konsol
build.ps1                            Task Build, Test, dan Run
build.xml                            Build Ant
nbproject/                           Metadata NetBeans
manifest.mf                          Metadata JAR
```

Source dan file build berada langsung di root repository. Hasil build pada `build/`, `dist/`, serta pengaturan pribadi `nbproject/private/` tidak dimasukkan ke Git.

## Pengujian

Jalankan task Test untuk memeriksa lapisan tarif, nilai batas, perhitungan desimal, pembulatan keluaran, dan penanganan masukan konsol. Tes dapat dijalankan tanpa akses jaringan.

Verifikasi pada JDK 17 dengan target Java 8: 70 pemeriksaan pada `TaxCalculatorTest` dan 31 pemeriksaan pada `TaxRulesTest` lulus melalui task Test PowerShell serta `ant test jar`, dengan total 101 pemeriksaan.
