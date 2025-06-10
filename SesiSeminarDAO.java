import java.sql.*;
import java.util.Scanner;

public class SesiSeminarDAO {
    private Scanner sc = new Scanner(System.in);
    private static SeminarDAO seminarDAO = new SeminarDAO();

    public void viewSesiSeminars() {
        System.out.print("Pilih ID Seminar yang ingin diliat sesinya: ");
        int sesiChoice = Integer.parseInt(sc.nextLine());

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             PreparedStatement ps = conn.prepareStatement(
                    "SELECT ss.id_sesi, s.tema, ss.judul_sesi, ss.tanggal_sesi, ss.waktu_mulai, ss.waktu_selesai, u.nama FROM sesi_seminar ss " +
                    "JOIN user u ON ss.id_pemateri = u.id_role " +
                    "JOIN seminar s ON ss.id_seminar = s.id_seminar " +
                    "WHERE u.id_role = 3 AND s.id_seminar = ?"
                );
            ) {
                ps.setInt(1, sesiChoice);
                ResultSet rs = ps.executeQuery();

                boolean hasRes = false;

                while (rs.next()) {
                    hasRes = true;                
                    System.out.printf(
                        "ID: %d, Tema: %s, Judul: %s, Tanggal_sesi: %s, Waktu_Mulai: %s, Waktu_selesai: %s, Pemateri: %s\n",
                        rs.getInt("id_sesi"), 
                        rs.getString("tema"),
                        rs.getString("judul_sesi"),
                        rs.getDate("tanggal_sesi").toString(),
                        rs.getTime("waktu_mulai").toString(),
                        rs.getTime("waktu_selesai").toString(),
                        rs.getString("nama")
                    );
 
                }

                if(!hasRes){
                    System.out.println("ID tidak ditemukan");
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSesiSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            seminarDAO.viewSeminars();
            
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
            System.out.println("\nDaftar Seminar:");
            seminarDAO.viewSeminars();

            System.out.print("\nMasukkan ID Seminar: ");
            int idSeminar = Integer.parseInt(sc.nextLine());

            // Tampilkan sesi yang ada
            System.out.println("\nDaftar Sesi untuk Seminar ID " + idSeminar + ":");
            viewSessionsBySeminar(conn, idSeminar);

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
            viewPemateri(conn);

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
    public void viewSessionsBySeminar(Connection conn, int idSesi) throws SQLException {
        String query = "SELECT ss.id_sesi, ss.judul_sesi, ss.tanggal_sesi, ss.waktu_mulai, ss.waktu_selesai, u.nama AS pemateri " +
                    "FROM sesi_seminar ss LEFT JOIN user u ON ss.id_pemateri = u.id_user " +
                    "WHERE ss.id_seminar = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idSesi);
        ResultSet rs = ps.executeQuery();
        
        System.out.println("ID\tJudul Sesi\t\tTanggal\t\tWaktu\t\tPemateri");
        System.out.println("--------------------------------------------------------------");
        while (rs.next()) {
            System.out.printf("%d\t%-20s\t%s\t%s-%s\t%s%n",
                rs.getInt("id_sesi"),
                rs.getString("judul_sesi"),
                rs.getDate("tanggal_sesi"),
                rs.getString("waktu_mulai"),
                rs.getString("waktu_selesai"),
                rs.getString("pemateri"));
        }
    }

    // Method untuk menampilkan daftar pemateri
    private void viewPemateri(Connection conn) throws SQLException {
        String query = "SELECT u.id_user, u.nama FROM user u JOIN role r ON u.id_role = r.id_role WHERE r.nama_role = 'pemateri'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        System.out.println("ID\tNama Pemateri");
        System.out.println("------------------");
        while (rs.next()) {
            System.out.printf("%d\t%s%n",
                rs.getInt("id_user"),
                rs.getString("nama"));
        }
    }
    
    public void deleteSesiSeminar() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            seminarDAO.viewSeminars();
            System.out.print("Masukkan ID Seminar yang ingin dihapus: ");
            int idSeminar = Integer.parseInt(sc.nextLine());

            System.out.println("\n");
            viewSessionsBySeminar(conn, idSeminar);
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