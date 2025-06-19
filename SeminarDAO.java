import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import view.SeminarView;

public class SeminarDAO {
    private Scanner sc = new Scanner(System.in);

    public List<SeminarView> getSeminarsForView() {
        List<SeminarView> seminarList = new ArrayList<>();
        
        String query = "SELECT s.id_seminar, s.tema, s.tanggal, l.nama_lokasi " +
                       "FROM seminar s " +
                       "LEFT JOIN lokasi l ON s.id_lokasi = l.id_lokasi";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            // Loop melalui setiap baris hasil dari ResultSet
            while (rs.next()) {
                SeminarView seminar = new SeminarView(
                    rs.getInt("id_seminar"),
                    rs.getString("tema"),
                    rs.getDate("tanggal") != null ? rs.getDate("tanggal").toString() : "N/A", 
                    rs.getString("nama_lokasi")
                );
                seminarList.add(seminar);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data seminar untuk tampilan:");
            e.printStackTrace();
        }
        return seminarList; // Kembalikan list of SeminarView
    }

    public void addSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            // Input tema
            System.out.print("Tema: ");
            String tema = sc.nextLine();
            
            // Input dan validasi tanggal SEBELUM lanjut ke lokasi
            Date tanggalSeminar = null;
            while (tanggalSeminar == null) {
                System.out.print("Tanggal (YYYY-MM-DD): ");
                String inputTanggal = sc.nextLine();
                try {
                    tanggalSeminar = Date.valueOf(inputTanggal);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: Format tanggal tidak valid. Gunakan format YYYY-MM-DD");
                }
            }

            // Tampilkan lokasi hanya jika tanggal valid
            System.out.println("\nDaftar Lokasi Tersedia:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_lokasi, nama_lokasi FROM lokasi");
            while (rs.next()) {
                System.out.println(rs.getInt("id_lokasi") + ". " + rs.getString("nama_lokasi"));
            }

            // Validasi ID lokasi
            int idLokasi = -1;
            while (idLokasi <= 0) {
                System.out.print("\nPilih ID Lokasi: ");
                try {
                    idLokasi = Integer.parseInt(sc.nextLine());
                    
                    // Cek apakah ID lokasi ada di database
                    PreparedStatement checkLokasi = conn.prepareStatement(
                        "SELECT 1 FROM lokasi WHERE id_lokasi = ?");
                    checkLokasi.setInt(1, idLokasi);
                    if (!checkLokasi.executeQuery().next()) {
                        System.out.println("Error: ID Lokasi tidak ditemukan!");
                        idLokasi = -1; // Reset untuk loop ulang
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: Masukkan angka ID yang valid");
                }
            }

            // Insert seminar
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO seminar (tema, tanggal, id_lokasi) VALUES (?, ?, ?)");
            ps.setString(1, tema);
            ps.setDate(2, tanggalSeminar);
            ps.setInt(3, idLokasi);
            ps.executeUpdate();
            
            System.out.println("Seminar berhasil ditambahkan!");

        } catch (SQLException e) {
            System.out.println("Error Database: " + e.getMessage());
        }
    }
    
    public void editSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            // Get seminar ID to edit
            System.out.print("Enter Seminar ID to edit: ");
            int id = Integer.parseInt(sc.nextLine());

            // Get new values
            System.out.print("New theme (leave blank to keep current): ");
            String tema = sc.nextLine();
            
            System.out.print("New date (YYYY-MM-DD, leave blank to keep current): ");
            String tanggalStr = sc.nextLine();
            Date tanggal = tanggalStr.isEmpty() ? null : Date.valueOf(tanggalStr);
            
            // Show available locations
            System.out.println("Available locations:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM lokasi");
            while (rs.next()) {
                System.out.println(rs.getInt("id_lokasi") + ". " + rs.getString("nama_lokasi"));
            }
            
            System.out.print("New location ID (leave blank to keep current): ");
            String lokasiIdStr = sc.nextLine();
            Integer lokasiId = lokasiIdStr.isEmpty() ? null : Integer.parseInt(lokasiIdStr);

            // Prepare update statement
            String sql = "UPDATE seminar SET tema=COALESCE(?, tema), " +
                        "tanggal=COALESCE(?, tanggal), " +
                        "id_lokasi=COALESCE(?, id_lokasi) " +
                        "WHERE id_seminar=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tema.isEmpty() ? null : tema);
            ps.setDate(2, tanggal);
            ps.setObject(3, lokasiId);  // Handles null value
            ps.setInt(4, id);

            int updated = ps.executeUpdate();
            System.out.println(updated > 0 ? "Seminar updated successfully!" : "Seminar not found");
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD");
        }
    }

    public void deleteSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.print("ID seminar yang ingin dihapus: ");
            int id = Integer.parseInt(sc.nextLine());
            PreparedStatement ps = conn.prepareStatement("DELETE FROM seminar WHERE id_seminar=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Seminar dihapus.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void generateSeminarParticipantsReport() {
        System.out.println("\n=== LAPORAN SEMINAR DAN JUMLAH PENDAFTAR (CTE) ===");
        System.out.println("Daftar seminar beserta jumlah total pendaftar\n");

        String query = "WITH SeminarPendaftar AS ( " +
                       "    SELECT " +
                       "        s.id_seminar, " +
                       "        s.tema, " +
                       "        COUNT(p.id_pendaftaran) AS JumlahPendaftar " +
                       "    FROM " +
                       "        seminar s " +
                       "    LEFT JOIN " +
                       "        pendaftaran p ON s.id_seminar = p.id_seminar " +
                       "    GROUP BY " +
                       "        s.id_seminar, s.tema " +
                       ") " +
                       "SELECT " +
                       "    sp.id_seminar, " +
                       "    sp.tema, " +
                       "    sp.JumlahPendaftar " +
                       "FROM " +
                       "    SeminarPendaftar sp " +
                       "ORDER BY " +
                       "sp.JumlahPendaftar DESC, sp.tema;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Tidak ada data seminar untuk ditampilkan.");
                return;
            }

            System.out.printf("%-5s | %-40s | %-15s%n",
                              "ID", "Tema Seminar", "Jumlah Pendaftar");
            System.out.println("---------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d | %-40s | %-15d%n",
                                  rs.getInt("id_seminar"),
                                  rs.getString("tema"),
                                  rs.getInt("JumlahPendaftar"));
            }
            System.out.println("---------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Error saat membuat laporan CTE seminar:");
            e.printStackTrace();
        }
    }
}