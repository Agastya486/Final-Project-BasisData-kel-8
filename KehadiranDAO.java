import java.sql.*;
import java.util.List;
import java.util.Scanner;
import view.SeminarView;
import view.SesiSeminarView;
import view.UserView;

public class KehadiranDAO {
        private Scanner sc = new Scanner(System.in);
        SeminarDAO seminarDAO = new SeminarDAO();
        SesiSeminarDAO sesiSeminarDAO = new SesiSeminarDAO();
        UserDAO userDAO = new UserDAO();
        UndoStack undoStack = new UndoStack();

        public void inputKehadiran() {
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

                        System.out.print("Masukkan ID Seminar: ");
                        int idSeminar = Integer.parseInt(sc.nextLine());

                        // Pilih Sesi
                        System.out.println("\n");
                        List<SesiSeminarView> sesis = sesiSeminarDAO.getSessionsBySeminarForView(idSeminar);

                        if (sesis.isEmpty()) {
                                System.out.println("Tidak ada sesi seminar ditemukan untuk ID Seminar " + idSeminar + ".");
                                return;
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
                        System.out.print("Masukkan ID Sesi Seminar: ");
                        int idSesi = Integer.parseInt(sc.nextLine());

                        // Pilih Peserta
                        System.out.println("\n");
                        List<UserView> pesertaSeminarList = userDAO.getPesertaBySeminarForView(idSeminar);

                        if (pesertaSeminarList.isEmpty()) {
                                System.out.println("Tidak ada peserta terdaftar untuk seminar ID " + idSeminar + ".");
                                return;
                        } else {
                                // Cetak header tabel peserta
                                System.out.printf("%-5s | %-20s | %-30s | %-10s%n",
                                "ID", "Nama", "Email", "Role");
                                System.out.println("------------------------------------------------------------------");
                                
                                // Loop untuk mencetak setiap objek UserView (peserta)
                                for (UserView peserta : pesertaSeminarList) {
                                System.out.printf("%-5d | %-20s | %-30s | %-10s%n",
                                        peserta.getIdUser(),
                                        peserta.getNama(),
                                        peserta.getEmail(),
                                        peserta.getNamaRole());
                                }
                                System.out.println("------------------------------------------------------------------");
                        }

                        System.out.print("Masukkan ID Peserta: ");
                        int idPeserta = Integer.parseInt(sc.nextLine());

                        // Input Status Hadir
                        System.out.print("Hadir? (true/false): ");
                        boolean hadir = Boolean.parseBoolean(sc.nextLine());

                        // Query Insert Kehadiran
                        // Cek apakah peserta sudah terdaftar di seminar (ambil id_pendaftaran)
                        String sqlCheckPendaftaran = "SELECT id_pendaftaran FROM pendaftaran WHERE id_user = ? AND id_seminar = ?";
                        int idPendaftaran = -1;

                        try (PreparedStatement stmt = conn.prepareStatement(sqlCheckPendaftaran, Statement.RETURN_GENERATED_KEYS)) {
                                stmt.setInt(1, idPeserta);
                                stmt.setInt(2, idSeminar);
                                ResultSet rs = stmt.executeQuery();

                                if (rs.next()) {
                                        idPendaftaran = rs.getInt("id_pendaftaran");
                                } else {
                                        System.err.println("Peserta tidak terdaftar di seminar ini!");
                                        return;
                                }
                                }

                                // Insert ke tabel kehadiran
                                String sqlInsertKehadiran = "INSERT INTO kehadiran (id_pendaftaran, id_sesi, hadir) VALUES (?, ?, ?)";
                                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertKehadiran, Statement.RETURN_GENERATED_KEYS)) {
                                stmt.setInt(1, idPendaftaran);
                                stmt.setInt(2, idSesi);
                                stmt.setBoolean(3, hadir);
                                int rowsAffected = stmt.executeUpdate();

                                if (rowsAffected == 0) {
                                        System.err.println("Gagal mencatat kehadiran.");
                                }
                                
                                ResultSet rs = stmt.getGeneratedKeys();
                                int idKehadiran = rs.next() ? rs.getInt(1) : -1;
                                
                                String action = "ADD_ATTENDANCE";
                                int undoData = idKehadiran;
                                undoStack.push(action, undoData);
                                System.out.println("Kehadiran berhasil dicatat");
                        }

                } catch (SQLException e) {
                        System.err.println("Error: " + e.getMessage());
                } catch (NumberFormatException e) {
                        System.err.println("Input harus berupa angka!");
                }
        }

        @SuppressWarnings("unchecked")
        public void undoKehadiran(){
                Node lastAction = undoStack.pop();
                if(lastAction == null){
                        System.out.println("Tidak ada aksi untuk undo!");
                        return;
                }

                try(Connection conn = DBConnection.getConnection()){
                        switch (lastAction.getAction()) {
                            case "ADD_ATTENDANCE":
                                int idKehadiran = (int) lastAction.getData();

                                String sql = "DELETE FROM kehadiran k WHERE id_kehadiran = ?";

                                PreparedStatement stmt = conn.prepareStatement(sql);
                                stmt.setInt(1, idKehadiran);
                                int rowsAffected = stmt.executeUpdate();

                                if (rowsAffected > 0) {
                                        System.out.println("Undo berhasil! Kehadiran dihapus.");
                                } else {
                                        System.out.println("Gagal undo: Data kehadiran tidak ditemukan.");
                                }
                                break;
                            default:
                                throw new AssertionError();
                        }
                } catch(SQLException e){
                        System.err.println("Gagal undo: " + e.getMessage());
                }

        }

        public void generateAttendanceCrossTabReport() {
                System.out.println("\n=== LAPORAN KEHADIRAN (CROSSTAB) ===");
                System.out.println("Ringkasan jumlah Hadir dan Tidak Hadir per Sesi Seminar\n");

                String query = "SELECT " +
                        "s.tema AS Tema_Seminar, " +
                        "ss.judul_sesi AS Judul_Sesi, " +
                        "COUNT(CASE WHEN k.hadir = TRUE THEN 1 END) AS Jumlah_Hadir, " +
                        "COUNT(CASE WHEN k.hadir = FALSE THEN 1 END) AS Jumlah_Tidak_Hadir, " +
                        "COUNT(k.id_kehadiran) AS Total_Peserta_Sesi " +
                        "FROM " +
                        "kehadiran k " +
                        "JOIN " +
                        "sesi_seminar ss ON k.id_sesi = ss.id_sesi " +
                        "JOIN " +
                        "seminar s ON ss.id_seminar = s.id_seminar " +
                        "GROUP BY " +
                        "s.tema, ss.judul_sesi " +
                        "ORDER BY " +
                        "s.tema, ss.judul_sesi;";

                try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

                if (!rs.isBeforeFirst()) { // Cek apakah ResultSet kosong
                        System.out.println("Tidak ada data kehadiran untuk ditampilkan.");
                        return;
                }

                System.out.printf("%-30s | %-30s | %-12s | %-16s | %-20s%n",
                                "Tema Seminar", "Judul Sesi", "Jml Hadir", "Jml Tdk Hadir", "Total Peserta Sesi");
                System.out.println("--------------------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                        System.out.printf("%-30s | %-30s | %-12d | %-16d | %-20d%n",
                                        rs.getString("Tema_Seminar"),
                                        rs.getString("Judul_Sesi"),
                                        rs.getInt("Jumlah_Hadir"),
                                        rs.getInt("Jumlah_Tidak_Hadir"),
                                        rs.getInt("Total_Peserta_Sesi"));
                }
                System.out.println("--------------------------------------------------------------------------------------------------------------");

                } catch (SQLException e) {
                System.err.println("Error saat membuat laporan CrossTab kehadiran:");
                e.printStackTrace();
                }
        }
}
