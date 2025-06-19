import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import view.SeminarView;
import view.SesiSeminarView;
import view.UserView;

public class SesiSeminarDAO {
    private Scanner sc = new Scanner(System.in);
    private static SeminarDAO seminarDAO = new SeminarDAO();
    private UserDAO userDAO = new UserDAO();

    public List<SesiSeminarView> getSesiSeminarsForView(int seminarId) { // Ubah nama dan tipe kembalian
        List<SesiSeminarView> sesiList = new ArrayList<>();
        
        // Query SQL yang sudah ada
        String query = "SELECT ss.id_sesi, s.tema, ss.judul_sesi, ss.tanggal_sesi, ss.waktu_mulai, ss.waktu_selesai, u.nama AS nama_pemateri " +
                       "FROM sesi_seminar ss " +
                       "JOIN user u ON ss.id_pemateri = u.id_user " +
                                                                     
                       "JOIN seminar s ON ss.id_seminar = s.id_seminar " +
                       "WHERE u.id_role = 3 AND s.id_seminar = ?"; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, seminarId); // Set parameter ID Seminar
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                SesiSeminarView sesi = new SesiSeminarView(
                    rs.getInt("id_sesi"),
                    rs.getString("tema"),
                    rs.getString("judul_sesi"),
                    rs.getDate("tanggal_sesi") != null ? rs.getDate("tanggal_sesi").toString() : "N/A",
                    rs.getTime("waktu_mulai") != null ? rs.getTime("waktu_mulai").toString() : "N/A",
                    rs.getTime("waktu_selesai") != null ? rs.getTime("waktu_selesai").toString() : "N/A",
                    rs.getString("nama_pemateri")
                );
                sesiList.add(sesi);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data sesi seminar untuk tampilan:");
            e.printStackTrace();
        }
        return sesiList;
    }

    public void addSesiSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            // List seminar
            System.out.println("\n=== DAFTAR SEMINAR ===\n");
            List<SeminarView> seminars = seminarDAO.getSeminarsForView(); 

            if (seminars.isEmpty()) {
                System.out.println("Tidak ada seminar yang ditemukan.");
            } else {
                // Cetak header tabel
                System.out.printf("%-5s | %-40s | %-15s | %-20s%n",
                        "ID", "Tema", "Tanggal", "Lokasi");
                System.out.println("--------------------------------------------------------------------------------");
                
                // Loop melalui setiap objek SeminarView dalam list dan cetak datanya
                for (SeminarView seminar : seminars) {
                        System.out.printf("%-5d | %-40s | %-15s | %-20s%n",
                        seminar.getIdSeminar(),
                        seminar.getTema(),
                        seminar.getTanggal(),
                        seminar.getLokasi());
                }
                System.out.println("--------------------------------------------------------------------------------");
            }
            
            System.out.print("Pilih ID Seminar: ");
            int idSesi = Integer.parseInt(sc.nextLine());
            
            System.out.print("Judul Sesi: ");
            String judulSesi = sc.nextLine();
            
            System.out.print("Tanggal Sesi (YYYY-MM-DD): ");
            String tanggalSesi = sc.nextLine();
            
            System.out.print("Waktu Mulai (HH:MM:SS): ");
            String waktuMulai = sc.nextLine();
            
            System.out.print("Waktu Selesai (HH:MM:SS): ");
            String waktuSelesai = sc.nextLine();
            
            // Show available pemateri
            System.out.println("\nDaftar Pemateri:");
            PreparedStatement pemateriPs = conn.prepareStatement(
                "SELECT u.id_user, u.nama FROM user u JOIN role r ON u.id_role = r.id_role WHERE r.nama_role = 'pemateri'");
            ResultSet pemateriRs = pemateriPs.executeQuery();
            while (pemateriRs.next()) {
                System.out.println(pemateriRs.getInt("id_user") + ". " + pemateriRs.getString("nama"));
            }
            
            System.out.print("Pilih ID Pemateri: ");
            int idPemateri = Integer.parseInt(sc.nextLine());
            
            // Insert the session
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sesi_seminar (id_seminar, judul_sesi, tanggal_sesi, waktu_mulai, waktu_selesai, id_pemateri) " +
                "VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, idSesi);
            ps.setString(2, judulSesi);
            ps.setDate(3, Date.valueOf(tanggalSesi));
            ps.setTime(4, Time.valueOf(waktuMulai));
            ps.setTime(5, Time.valueOf(waktuSelesai));
            ps.setInt(6, idPemateri);
            
            ps.executeUpdate();
            System.out.println("\nSesi seminar berhasil ditambahkan!");
            
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Format tanggal/waktu salah. Gunakan format yang benar (YYYY-MM-DD untuk tanggal, HH:MM:SS untuk waktu)");
        }
    }
    
    public void editSesiSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            // Tampilkan daftar seminar
            System.out.println("\n=== DAFTAR SEMINAR ===\n");
            List<SeminarView> seminars = seminarDAO.getSeminarsForView(); 

            if (seminars.isEmpty()) {
                System.out.println("Tidak ada seminar yang ditemukan.");
            } else {
                // Cetak header tabel
                System.out.printf("%-5s | %-40s | %-15s | %-20s%n",
                        "ID", "Tema", "Tanggal", "Lokasi");
                System.out.println("--------------------------------------------------------------------------------");
                
                // Loop melalui setiap objek SeminarView dalam list dan cetak datanya
                for (SeminarView seminar : seminars) {
                        System.out.printf("%-5d | %-40s | %-15s | %-20s%n",
                        seminar.getIdSeminar(),
                        seminar.getTema(),
                        seminar.getTanggal(),
                        seminar.getLokasi());
                }
                System.out.println("--------------------------------------------------------------------------------");
            }

            System.out.print("\nMasukkan ID Seminar: ");
            int idSeminar = Integer.parseInt(sc.nextLine());

            // Tampilkan sesi yang ada
            System.out.println("\n=== DAFTAR SESI UNTUK SEMINAR ID " + idSeminar + " ===\n");
            List<SesiSeminarView> sesis = getSessionsBySeminarForView(idSeminar); // Variabel diganti jadi 'sesis' biar lebih jelas

            if (sesis.isEmpty()) {
                System.out.println("Tidak ada sesi seminar ditemukan untuk ID Seminar " + idSeminar + ".");
                return; // Keluar jika tidak ada sesi untuk diedit
            } else {
                // Cetak header tabel sesi
                System.out.printf("%-5s | %-30s | %-25s | %-12s | %-8s | %-8s | %-20s%n",
                    "ID Sesi", "Tema Seminar", "Judul Sesi", "Tanggal", "Mulai", "Selesai", "Pemateri");
                System.out.println("-----------------------------------------------------------------------------------------------------------------");
                
                // Loop untuk mencetak setiap objek SesiSeminarView
                for (SesiSeminarView sesi : sesis) {
                    System.out.printf("%-5d | %-30s | %-25s | %-12s | %-8s | %-8s | %-20s%n",
                            sesi.getIdSesi(),
                            sesi.getTemaSeminar(),
                            sesi.getJudulSesi(),
                            sesi.getTanggalSesi(),
                            sesi.getWaktuMulai(),
                            sesi.getWaktuSelesai(),
                            sesi.getNamaPemateri());
                }
                System.out.println("-----------------------------------------------------------------------------------------------------------------");
            }

            System.out.print("\nMasukkan ID Sesi yang ingin diedit: ");
            int idSesi = Integer.parseInt(sc.nextLine());

            // Input data baru
            System.out.print("Judul sesi baru (kosongkan jika tidak ingin diubah): ");
            String judul = sc.nextLine();

            System.out.print("Tanggal sesi baru (YYYY-MM-DD, kosongkan jika tidak ingin diubah): ");
            String tanggalStr = sc.nextLine();
            Date tanggal = tanggalStr.isEmpty() ? null : Date.valueOf(tanggalStr);

            System.out.print("Waktu mulai baru (HH:MM:SS, kosongkan jika tidak ingin diubah): ");
            String waktuMulai = sc.nextLine();

            System.out.print("Waktu selesai baru (HH:MM:SS, kosongkan jika tidak ingin diubah): ");
            String waktuSelesai = sc.nextLine();

            // Tampilkan daftar pemateri
            System.out.println("\nDaftar Pemateri:");
            List<UserView> pemateriList = userDAO.getPesertaByRoleForView("pemateri");

            if (pemateriList.isEmpty()) {
                System.out.println("Tidak ada pemateri ditemukan. Harap tambahkan user dengan role 'pemateri' terlebih dahulu.");
                return;
            } else {
                // Cetak header tabel
                System.out.printf("%-5s | %-20s | %-30s | %-10s%n",
                    "ID", "Nama", "Email", "Role");
                System.out.println("------------------------------------------------------------------");

                // Loop untuk mencetak setiap objek UserView (pemateri)
                for (UserView pemateri : pemateriList) {
                    System.out.printf("%-5d | %-20s | %-30s | %-10s%n",
                            pemateri.getIdUser(),
                            pemateri.getNama(),
                            pemateri.getEmail(),
                            pemateri.getNamaRole());
                }
                System.out.println("------------------------------------------------------------------");
            }

            System.out.print("ID Pemateri baru (kosongkan jika tidak ingin diubah): ");
            String pemateriStr = sc.nextLine();
            Integer idPemateri = pemateriStr.isEmpty() ? null : Integer.parseInt(pemateriStr);

            // Validasi pemateri jika diisi
            if (idPemateri != null && !isPemateriExists(conn, idPemateri)) {
                System.out.println("ID pemateri tidak valid. Perubahan pemateri dibatalkan.");
                idPemateri = null;
            }

            // SQL UPDATE pakai COALESCE
            String sql = "UPDATE sesi_seminar SET " +
                        "judul_sesi = COALESCE(?, judul_sesi), " +
                        "tanggal_sesi = COALESCE(?, tanggal_sesi), " +
                        "waktu_mulai = COALESCE(?, waktu_mulai), " +
                        "waktu_selesai = COALESCE(?, waktu_selesai), " +
                        "id_pemateri = COALESCE(?, id_pemateri) " +
                        "WHERE id_sesi = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, judul.isEmpty() ? null : judul);
            ps.setDate(2, tanggal);
            ps.setString(3, waktuMulai.isEmpty() ? null : waktuMulai);
            ps.setString(4, waktuSelesai.isEmpty() ? null : waktuSelesai);
            ps.setObject(5, idPemateri);
            ps.setInt(6, idSesi);

            int updated = ps.executeUpdate();
            System.out.println(updated > 0
                ? "Sesi seminar berhasil diupdate."
                : "Sesi tidak ditemukan atau tidak ada perubahan.");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Format tanggal salah. Gunakan format YYYY-MM-DD.");
        }
    }

    // Method helper untuk mengecek keberadaan pemateri
    private boolean isPemateriExists(Connection conn, int idUser) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT 1 FROM user WHERE id_user = ? AND id_role = (SELECT id_role FROM role WHERE nama_role = 'pemateri')");
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // Method untuk menampilkan daftar sesi berdasarkan seminar
    public List<SesiSeminarView> getSessionsBySeminarForView(int seminarId) {
        List<SesiSeminarView> sesiList = new ArrayList<>();
        
        // Query dimodifikasi: tambahkan JOIN ke tabel seminar untuk mengambil tema_seminar
        String query = "SELECT ss.id_sesi, ss.judul_sesi, ss.tanggal_sesi, ss.waktu_mulai, ss.waktu_selesai, u.nama AS pemateri, s.tema AS tema_seminar " +
                       "FROM sesi_seminar ss " +
                       "LEFT JOIN user u ON ss.id_pemateri = u.id_user " + 
                       "JOIN seminar s ON ss.id_seminar = s.id_seminar " + // Tambahkan JOIN ini
                       "WHERE ss.id_seminar = ?";

        try (Connection conn = DBConnection.getConnection(); // Dapatkan koneksi di sini
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, seminarId); // Set parameter id_seminar
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                SesiSeminarView sesi = new SesiSeminarView(
                    rs.getInt("id_sesi"),
                    rs.getString("tema_seminar"), // Ambil tema_seminar dari alias SQL
                    rs.getString("judul_sesi"),
                    rs.getDate("tanggal_sesi") != null ? rs.getDate("tanggal_sesi").toString() : "N/A",
                    rs.getTime("waktu_mulai") != null ? rs.getTime("waktu_mulai").toString() : "N/A",
                    rs.getTime("waktu_selesai") != null ? rs.getTime("waktu_selesai").toString() : "N/A",
                    rs.getString("pemateri") // Ambil nama_pemateri dari alias SQL
                );
                sesiList.add(sesi);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data sesi seminar untuk tampilan (by seminar):");
            e.printStackTrace();
        }
        return sesiList; // Kembalikan list of SesiSeminarView
    }
    
    public void deleteSesiSeminar() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // List seminar
            System.out.println("\n=== DAFTAR SEMINAR ===\n");
            List<SeminarView> seminars = seminarDAO.getSeminarsForView(); 

            if (seminars.isEmpty()) {
                System.out.println("Tidak ada seminar yang ditemukan.");
            } else {
                // Cetak header tabel
                System.out.printf("%-5s | %-40s | %-15s | %-20s%n",
                        "ID", "Tema", "Tanggal", "Lokasi");
                System.out.println("--------------------------------------------------------------------------------");
                
                // Loop melalui setiap objek SeminarView dalam list dan cetak datanya
                for (SeminarView seminar : seminars) {
                        System.out.printf("%-5d | %-40s | %-15s | %-20s%n",
                        seminar.getIdSeminar(),
                        seminar.getTema(),
                        seminar.getTanggal(),
                        seminar.getLokasi());
                }
                System.out.println("--------------------------------------------------------------------------------");
            }

            System.out.print("Masukkan ID Seminar yang ingin dihapus: ");
            int idSeminar = Integer.parseInt(sc.nextLine());

            System.out.println("\n");
            System.out.println("\n=== DAFTAR SESI UNTUK SEMINAR ID " + idSeminar + " ===\n");
            List<SesiSeminarView> sesis = getSessionsBySeminarForView(idSeminar); // Variabel diganti jadi 'sesis' biar lebih jelas

            if (sesis.isEmpty()) {
                System.out.println("Tidak ada sesi seminar ditemukan untuk ID Seminar " + idSeminar + ".");
                return; // Keluar jika tidak ada sesi untuk diedit
            } else {
                // Cetak header tabel sesi
                System.out.printf("%-5s | %-30s | %-25s | %-12s | %-8s | %-8s | %-20s%n",
                    "ID Sesi", "Tema Seminar", "Judul Sesi", "Tanggal", "Mulai", "Selesai", "Pemateri");
                System.out.println("-----------------------------------------------------------------------------------------------------------------");
                
                // Loop untuk mencetak setiap objek SesiSeminarView
                for (SesiSeminarView sesi : sesis) {
                    System.out.printf("%-5d | %-30s | %-25s | %-12s | %-8s | %-8s | %-20s%n",
                            sesi.getIdSesi(),
                            sesi.getTemaSeminar(),
                            sesi.getJudulSesi(),
                            sesi.getTanggalSesi(),
                            sesi.getWaktuMulai(),
                            sesi.getWaktuSelesai(),
                            sesi.getNamaPemateri());
                }
                System.out.println("-----------------------------------------------------------------------------------------------------------------");
            }

            System.out.print("Masukkan ID Sesi yang ingin dihapus: ");
            int idSesi = Integer.parseInt(sc.nextLine());

            // 1. Validasi apakah seminar ada
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id_sesi FROM sesi_seminar WHERE id_sesi = ?");
            checkStmt.setInt(1, idSesi);
            if (!checkStmt.executeQuery().next()) {
                System.out.println("Seminar dengan ID " + idSesi + " tidak ditemukan!");
                return;
            }

            // 2. Hapus kehadiran terkait
            PreparedStatement ps2 = conn.prepareStatement(
                "DELETE FROM kehadiran WHERE id_pendaftaran IN " +
                "(SELECT id_pendaftaran FROM pendaftaran WHERE id_sesi = ?)");
            ps2.setInt(1, idSesi);
            int deletedKehadiran = ps2.executeUpdate();
            System.out.println(deletedKehadiran + " data kehadiran dihapus");

            // 5. Hapus sesi seminar terkait
            PreparedStatement ps4 = conn.prepareStatement(
                "DELETE FROM sesi_seminar WHERE id_sesi = ?");
            ps4.setInt(1, idSesi);
            int deletedSesi = ps4.executeUpdate();
            System.out.println(deletedSesi + " sesi seminar dihapus");

            conn.commit(); // Commit transaction jika semua berhasil
            
            if (deletedSesi > 0) {
                System.out.println("Seminar dan semua data terkait berhasil dihapus!");
            } else {
                System.out.println("Gagal menghapus seminar.");
            }
            
        } catch (NumberFormatException e) {
            System.err.println("Error: ID harus berupa angka!");
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to error");
                }
            } catch (SQLException ex) {
                System.err.println("Error saat rollback:");
                ex.printStackTrace();
            }
            System.err.println("Error database saat menghapus seminar:");
            System.err.println("Pesan error: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error saat menutup koneksi:");
                e.printStackTrace();
            }
        }
    }
}