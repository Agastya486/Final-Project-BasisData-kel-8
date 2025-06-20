# 📚 Sistem Manajemen Seminar

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Console App](https://img.shields.io/badge/Aplikasi%20Konsol-blueviolet?style=for-the-badge&logo=terminal&logoColor=white)

## Program apakah ini?
Program ini merupakan program sederhana berbasis konsol yang dibuat menggunakan Java dan MySQL. Tujuannya untuk mengatur semua hal yang berhubungan dengan seminar, mulai dari peserta, jadwal, kehadiran, hingga bikin sertifikat otomatis.

## ✨ Fitur-Fitur Andalan

* **Seminar Management**: Untuk menambah, mengedit, atau menghapus data seminar.
* **Session Management**: Mengatur sesi-sesi di tiap seminar, lengkap dengan info pematerinya.
* **User Management**: Mengurus data pengguna dengan berbagai peran (peserta, panitia, pemateri).
* **Participant Registration**: Daftarin peserta ke seminar yang mereka mau.
* **Attendance Tracking**: Catat kehadiran peserta di setiap sesi.
* **Automatic Certificate Generation**: Sertifikat bakal otomatis dibuat buat peserta yang lulus (dengan minimal 80% kehadiran). Jadi, gak perlu ribet lagi! 📜
* **Undo Functionality**: Salah input? Tenang, ada fitur undo buat balikin aksi terakhir.

## 📊 Struktur Database

Berikut merupakan gambaran tabel-tabel di database MySQL kami:

```sql
-- Tabel `role` untuk peran pengguna (misal: Peserta, Panitia, Pemateri)
CREATE TABLE role (
    id_role INT PRIMARY KEY AUTO_INCREMENT,
    nama_role VARCHAR(50) NOT NULL UNIQUE
);

-- Tabel `lokasi` untuk lokasi seminar
CREATE TABLE lokasi (
    id_lokasi INT PRIMARY KEY AUTO_INCREMENT,
    nama_lokasi VARCHAR(200) NOT NULL
);

-- Tabel `seminar` untuk detail seminar
CREATE TABLE seminar (
    id_seminar INT PRIMARY KEY AUTO_INCREMENT,
    tema VARCHAR(255) NOT NULL,
    tanggal DATE NOT NULL,
    id_lokasi INT,
    FOREIGN KEY (id_lokasi) REFERENCES lokasi(id_lokasi)
);

-- Tabel `user` untuk informasi pengguna
CREATE TABLE user (
    id_user INT PRIMARY KEY AUTO_INCREMENT,
    nama VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    id_role INT,
    FOREIGN KEY (id_role) REFERENCES role(id_role)
);

-- Tabel `sesi_seminar` untuk sesi-sesi di setiap seminar
CREATE TABLE sesi_seminar (
    id_sesi INT PRIMARY KEY AUTO_INCREMENT,
    id_seminar INT NOT NULL,
    judul_sesi VARCHAR(255) NOT NULL,
    tanggal_sesi DATE NOT NULL,
    waktu_mulai TIME NOT NULL,
    waktu_selesai TIME NOT NULL,
    id_pemateri INT,
    FOREIGN KEY (id_seminar) REFERENCES seminar(id_seminar),
    FOREIGN KEY (id_pemateri) REFERENCES user(id_user)
);

-- Tabel `pendaftaran` untuk mencatat pendaftaran peserta ke seminar
CREATE TABLE pendaftaran (
    id_pendaftaran INT PRIMARY KEY AUTO_INCREMENT,
    id_user INT,
    id_seminar INT,
    status VARCHAR(50) DEFAULT 'belum lulus', -- 'lulus' atau 'belum lulus'
    FOREIGN KEY (id_user) REFERENCES user(id_user),
    FOREIGN KEY (id_seminar) REFERENCES seminar(id_seminar)
);

-- Tabel `kehadiran` untuk mencatat kehadiran peserta di sesi seminar
CREATE TABLE kehadiran (
    id_kehadiran INT PRIMARY KEY AUTO_INCREMENT,
    id_pendaftaran INT NOT NULL,
    id_sesi INT NOT NULL,
    hadir BOOLEAN NOT NULL, -- TRUE jika hadir, FALSE jika tidak
    FOREIGN KEY (id_pendaftaran) REFERENCES pendaftaran(id_pendaftaran),
    FOREIGN KEY (id_sesi) REFERENCES sesi_seminar(id_sesi)
);

-- Tabel `sertifikat` untuk menyimpan data sertifikat yang sudah dibuat
CREATE TABLE sertifikat (
    id_sertifikat INT PRIMARY KEY AUTO_INCREMENT,
    id_pendaftaran INT,
    nama_peserta VARCHAR(100),
    tema_seminar VARCHAR(255),
    tanggal_cetak DATE DEFAULT (CURRENT_DATE()),
    FOREIGN KEY (id_pendaftaran) REFERENCES pendaftaran(id_pendaftaran)
);
```

## 🖥️ Cara menjalankan
### Compile file menggunakan ``Javac``
```cmd
> javac -cp ".;lib/mysql-connector-j-9.3.0.jar" *.java
```
Tujuannya adalah untuk melakukan compile pada setiap file

### Jalankan program:
```cmd
> java -cp ".;lib/mysql-connector-j-9.3.0.jar" Main
```

## Program dibuat oleh 
|Nama|	NIM |
|----|------|
|Muhammad Akbar Prayoga	|24082010103|
|Muhammad Farros Nidji	|24082010109|
|Devano Agastya H	|24082010133|
|Razan Muhammad R	|24082010134|
## Catatan 📝: file dengan ekstensi .class adalah file yang dibuat oleh compiler, sehingga tidak bisa dihapus. Jika dihapus, maka program tidak dapat berjalan 🙏
