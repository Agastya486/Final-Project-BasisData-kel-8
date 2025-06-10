import java.sql.*;
import java.util.Scanner;

public class SertifikatDAO {
    private Scanner sc = new Scanner(System.in);
    private SeminarDAO seminarDAO = new SeminarDAO();

    public void viewKelulusan() {
        try (Connection conn = DBConnection.getConnection()) {
            // Tampilkan daftar seminar terlebih dahulu
            seminarDAO.viewSeminars();
            
            // Minta input ID seminar yang ingin dilihat sertifikatnya
            System.out.print("Masukkan ID Seminar untuk melihat sertifikat (0 untuk semua seminar): ");
            int idSeminar = Integer.parseInt(sc.nextLine());
                        
            // Query untuk menampilkan sertifikat berdasarkan pilihan
            String query;
            PreparedStatement ps;
            
            if (idSeminar == 0) {
                query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar, " +
                       "sm.id_seminar, s.tanggal_cetak " +
                       "FROM sertifikat s " +
                       "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                       "JOIN user u ON p.id_user = u.id_user " +
                       "JOIN seminar sm ON p.id_seminar = sm.id_seminar " +
                       "ORDER BY sm.id_seminar, u.nama";
                ps = conn.prepareStatement(query);
            } else {
                query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar, " +
                       "sm.id_seminar, s.tanggal_cetak " +
                       "FROM sertifikat s " +
                       "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                       "JOIN user u ON p.id_user = u.id_user " +
                       "JOIN seminar sm ON p.id_seminar = sm.id_seminar " +
                       "WHERE sm.id_seminar = ? " +
                       "ORDER BY u.nama";
                ps = conn.prepareStatement(query);
                ps.setInt(1, idSeminar);
            }
            
            // Tampilkan hasil
            System.out.println("\n=== DAFTAR SERTIFIKAT ===");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-8s | %-20s | %-30s | %-12s |\n", 
                            "ID", "Seminar", "Nama Peserta", "Tema Seminar", "Tanggal Cetak");
            System.out.println("--------------------------------------------------------------------------------");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("| %-5d | %-8d | %-20s | %-30s | %-12s |\n",
                        rs.getInt("id_sertifikat"),
                        rs.getInt("id_seminar"),
                        rs.getString("nama_peserta"),
                        rs.getString("tema_seminar"),
                        rs.getDate("tanggal_cetak"));
            }
            System.out.println("--------------------------------------------------------------------------------");
            
            // Tanya apakah ingin mencetak sertifikat
            System.out.print("\nCetak sertifikat? (y/n): ");
            String choice = sc.nextLine().toLowerCase();
            
            if (choice.equals("y")) {
                System.out.print("Masukkan ID Sertifikat yang akan dicetak (0 untuk semua): ");
                int idSertifikat = Integer.parseInt(sc.nextLine());
                cetakSertifikat(conn, idSertifikat, idSeminar);
            }
            
        } catch (SQLException e) {
            System.err.println("Error database: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Input harus berupa angka!");
        }
    }
    
    private void cetakSertifikat(Connection conn, int idSertifikat, int idSeminar) throws SQLException {
        String query;
        PreparedStatement ps;
        boolean found = false;

        if (idSertifikat == 0) {
            // Cetak semua sertifikat dari seminar tertentu atau semua seminar
            if (idSeminar == 0) {
                query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar " +
                       "FROM sertifikat s " +
                       "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                       "JOIN user u ON p.id_user = u.id_user " +
                       "JOIN seminar sm ON p.id_seminar = sm.id_seminar";
                ps = conn.prepareStatement(query);
            } else {
                query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar " +
                       "FROM sertifikat s " +
                       "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                       "JOIN user u ON p.id_user = u.id_user " +
                       "JOIN seminar sm ON p.id_seminar = sm.id_seminar " +
                       "WHERE sm.id_seminar = ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, idSeminar);
            }
        } else {
            // Cetak sertifikat tertentu
            query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar " +
                   "FROM sertifikat s " +
                   "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                   "JOIN user u ON p.id_user = u.id_user " +
                   "JOIN seminar sm ON p.id_seminar = sm.id_seminar " +
                   "WHERE s.id_sertifikat = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idSertifikat);
        }
        
        ResultSet rs = ps.executeQuery();
        System.out.println("\n=== PROSES CETAK SERTIFIKAT ===");
        
        while (rs.next()) {
            found = true;
            System.out.println("--------------------------------------------------");
            System.out.println("Mencetak sertifikat untuk:");
            System.out.println("ID Sertifikat: " + rs.getInt("id_sertifikat"));
            System.out.println("Nama Peserta : " + rs.getString("nama_peserta"));
            System.out.println("Tema Seminar : " + rs.getString("tema_seminar"));
            System.out.println("--------------------------------------------------");
            
            // Simulasikan proses cetak
            try {
                Thread.sleep(1000); // Delay untuk simulasi
                System.out.println("Sertifikat berhasil dicetak!");
            } catch (InterruptedException e) {
                System.err.println("Gangguan saat mencetak");
            }
        }
        
        if (idSertifikat != 0 && !found) {
            System.out.println("Sertifikat dengan ID " + idSertifikat + " tidak ditemukan!");
        }
    }
}