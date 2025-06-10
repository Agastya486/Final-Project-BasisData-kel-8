import java.sql.*;
import java.util.Scanner;

public class KehadiranDAO {
        private Scanner sc = new Scanner(System.in);
        SeminarDAO seminarDAO = new SeminarDAO();
        SesiSeminarDAO sesiSeminarDAO = new SesiSeminarDAO();
        PesertaDAO pesertaDAO = new PesertaDAO();
        UndoStack undoStack = new UndoStack();

        public void inputKehadiran() {
                try (Connection conn = DBConnection.getConnection()) {
                        // 1. Pilih Seminar
                        seminarDAO.viewSeminars();
                        System.out.print("Masukkan ID Seminar: ");
                        int idSeminar = Integer.parseInt(sc.nextLine());

                        // 2. Pilih Sesi
                        System.out.println("\n");
                        sesiSeminarDAO.viewSessionsBySeminar(conn, idSeminar);
                        System.out.print("Masukkan ID Sesi Seminar: ");
                        int idSesi = Integer.parseInt(sc.nextLine());

                        // 3. Pilih Peserta
                        System.out.println("\n");
                        pesertaDAO.viewPesertaBySeminar(conn, idSeminar);  // Pastikan hanya tampilkan peserta yang mendaftar di seminar ini
                        System.out.print("Masukkan ID Peserta: ");
                        int idPeserta = Integer.parseInt(sc.nextLine());

                        // 4. Input Status Hadir
                        System.out.print("Hadir? (true/false): ");
                        boolean hadir = Boolean.parseBoolean(sc.nextLine());

                        // 5. Query Insert Kehadiran
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
}
