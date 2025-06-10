import java.sql.*;
import java.util.Scanner;

public class PesertaDAO {
    private Scanner sc = new Scanner(System.in);
    private SeminarDAO seminarDAO = new SeminarDAO();
    private UndoStack undoStack = new UndoStack();

    public void viewUser() {
        // Build SQL query using String.format()
        String query = String.format(
            "%s %s %s %s",
            "SELECT u.id_user, u.nama, u.email, r.nama_role",
            "FROM user u",
            "JOIN role r ON u.id_role = r.id_role",
            "ORDER BY u.id_user"
        );

        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            // Print table header
            System.out.println("\n=== DAFTAR USER ===");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("| %-5s | %-20s | %-30s | %-10s |\n", "ID", "Nama", "Email", "Role");
            System.out.println("------------------------------------------------------------------");
            
            // Print each user record
            while (rs.next()) {
                System.out.printf("| %-5d | %-20s | %-30s | %-10s |\n",
                        rs.getInt("id_user"), 
                        rs.getString("nama"), 
                        rs.getString("email"),
                        rs.getString("nama_role"));
            }
            
            // Print table footer
            System.out.println("------------------------------------------------------------------");
            
        } catch (SQLException e) {
            System.err.println("\nError saat menampilkan data user:");
            System.err.println("----------------------------------");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            System.err.println("----------------------------------");
            System.err.println("Query yang gagal: " + query);
        }
    }

    public void viewPeserta(){
        String query = String.format(
            "%s %s %s %s",
            "SELECT u.id_user, u.nama",
            "FROM user u",
            "JOIN role r ON u.id_role = r.id_role",
            "WHERE nama_role = 'peserta'"
        );

        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            // Print table header
            System.out.println("\n=== DAFTAR PESERTA ===");
            System.out.println("-----------------------------------");
            System.out.printf("| %-5s | %-20s |\n", "ID", "Nama");
            System.out.println("-----------------------------------");
            
            // Print each user record
            while (rs.next()) {
                System.out.printf("| %-5d | %-20s |\n",
                        rs.getInt("id_user"), 
                        rs.getString("nama"));
            }
            
            // Print table footer
            System.out.println("------------------------------------");
            
        } catch (SQLException e) {
            System.err.println("\nError saat menampilkan data user:");
            System.err.println("----------------------------------");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            System.err.println("----------------------------------");
            System.err.println("Query yang gagal: " + query);
        }
    }

    public void viewPesertaBySeminar(Connection conn, int idPs) throws SQLException {
        String query = "SELECT u.id_user, u.nama, s.tema " +
                    "FROM pendaftaran p " +
                    "JOIN user u ON p.id_user = p.id_user " +
                    "JOIN seminar s ON p.id_seminar = s.id_seminar " +
                    "WHERE u.id_role = '1' AND s.id_seminar = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idPs);
        ResultSet rs = ps.executeQuery();
        
        System.out.println("ID\tNama\t\tTema");
        System.out.println("--------------------------------------------------------------");
        while (rs.next()) {
            System.out.printf("%d\t%s\t%s%n",
                rs.getInt("id_user"),
                rs.getString("nama"),
                rs.getString("tema"));
        }
    }

    public void addUser() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("\n=== TAMBAH USER BARU ===");
            
            System.out.print("\nNama: ");
            String nama = sc.nextLine();
            
            System.out.print("Email: ");
            String email = sc.nextLine();
            
            System.out.println("\nPilih Role:");
            System.out.println("1. peserta");
            System.out.println("2. panitia");
            System.out.println("3. pemateri");
            System.out.print("\nMasukkan pilihan (1-3): ");
            int roleChoice = Integer.parseInt(sc.nextLine());
            
            if (roleChoice < 1 || roleChoice > 3) {
                System.out.println("\nError: Pilihan role tidak valid!\n");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user (nama, email, id_role) VALUES (?, ?, ?)");
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setInt(3, roleChoice);
            ps.executeUpdate();
            
            
            System.out.println("\nUser berhasil ditambahkan!\n");
            
        } catch (SQLException e) {
            System.out.println("\nError: " + e.getMessage());
            if (e.getSQLState().equals("23000")) {
                System.out.println("Email sudah terdaftar!\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("\nError: Input angka tidak valid!\n");
        }
    }

    public void editUser() {
        try (Connection conn = DBConnection.getConnection()) {
            // Input ID user
            System.out.print("Masukkan ID user yang ingin diedit: ");
            int id = Integer.parseInt(sc.nextLine());

            // Cek apakah user ada
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id_user, nama, email, id_role FROM user WHERE id_user = ?");
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("User dengan ID " + id + " tidak ditemukan.");
                return;
            }

            // Tampilkan data lama
            System.out.println("\nData saat ini:");
            System.out.println("Nama: " + rs.getString("nama"));
            System.out.println("Email: " + rs.getString("email"));
            System.out.println("Role: " + getRoleName(conn, rs.getInt("id_role")));

            // Input data baru
            System.out.print("\nNama baru (kosongkan jika tidak ingin mengubah): ");
            String nama = sc.nextLine();
            System.out.print("Email baru (kosongkan jika tidak ingin mengubah): ");
            String email = sc.nextLine();

            // Validasi email unik jika diubah
            if (!email.isEmpty()) {
                PreparedStatement emailCheck = conn.prepareStatement(
                    "SELECT id_user FROM user WHERE email = ? AND id_user != ?");
                emailCheck.setString(1, email);
                emailCheck.setInt(2, id);
                if (emailCheck.executeQuery().next()) {
                    System.out.println("Email sudah digunakan oleh user lain!");
                    return;
                }
            }

            // Update data
            String sql = "UPDATE user SET nama = COALESCE(NULLIF(?, ''), nama), " +
                        "email = COALESCE(NULLIF(?, ''), email) WHERE id_user = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama.isEmpty() ? null : nama);
            ps.setString(2, email.isEmpty() ? null : email);
            ps.setInt(3, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Data user berhasil diupdate.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("ID harus berupa angka!");
        }
    }

    private String getRoleName(Connection conn, int idRole) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT nama_role FROM role WHERE id_role = ?");
        ps.setInt(1, idRole);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString("nama_role") : "Tidak diketahui";
    }

    public void deleteUser() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.print("Masukkan ID user yang ingin dihapus: ");
            int id = Integer.parseInt(sc.nextLine());

            // Check if user exists and is not a speaker with active sessions
            try (PreparedStatement usCheck = conn.prepareStatement(
                    "SELECT u.id_user, r.nama_role, " +
                    "(SELECT COUNT(*) FROM sesi_seminar WHERE id_pemateri = u.id_user) as active_sessions " +
                    "FROM user u JOIN role r ON u.id_role = r.id_role WHERE u.id_user = ?")) {
                
                usCheck.setInt(1, id);
                ResultSet rs = usCheck.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("User dengan ID tersebut tidak ditemukan.");
                    return;
                }
                
                if ("pemateri".equalsIgnoreCase(rs.getString("nama_role")) && rs.getInt("active_sessions") > 0) {
                    System.out.println("Gagal menghapus! User ini adalah pemateri yang masih terdaftar dalam sesi seminar.");
                    return;
                }
            }

            // Disable foreign key checks temporarily for atomic deletion
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS=0");
                
                // Delete user and all related data in one transaction
                try {
                    conn.setAutoCommit(false);
                    
                    // Delete related records
                    deleteRelatedRecords(conn, "DELETE FROM sertifikat WHERE id_pendaftaran IN (SELECT id_pendaftaran FROM pendaftaran WHERE id_user = ?)", id);
                    deleteRelatedRecords(conn, "DELETE FROM kehadiran WHERE id_pendaftaran IN (SELECT id_pendaftaran FROM pendaftaran WHERE id_user = ?)", id);
                    deleteRelatedRecords(conn, "DELETE FROM pendaftaran WHERE id_user = ?", id);
                    
                    // Delete user
                    int affectedRows = deleteRelatedRecords(conn, "DELETE FROM user WHERE id_user = ?", id);
                    
                    conn.commit();
                    System.out.println(affectedRows > 0 ? "User berhasil dihapus beserta semua data terkait." : "Gagal menghapus user.");
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    stmt.execute("SET FOREIGN_KEY_CHECKS=1");
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat menghapus user: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Masukkan ID yang valid (angka).");
        }
    }

    private int deleteRelatedRecords(Connection conn, String sql, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    public void daftarSeminar() {
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Tampilkan daftar seminar biar user bisa milih
            seminarDAO.viewSeminars();
            System.out.print("Masukkan ID Seminar yang ingin diikuti: ");
            int idSeminar = Integer.parseInt(sc.nextLine());

            // 2. Tampilkan daftar peserta yang punya role "peserta"
            System.out.println("\n=== DAFTAR PESERTA YANG BISA MENDAFTAR ===");
            viewPesertaByRole(conn, "peserta"); // Method baru untuk melihat peserta berdasarkan role
            System.out.print("Masukkan ID Peserta yang ingin mendaftar: ");
            int idPeserta = Integer.parseInt(sc.nextLine());

            // Cek apakah peserta sudah terdaftar di seminar ini
            String checkSql = "SELECT COUNT(*) FROM pendaftaran WHERE id_user = ? AND id_seminar = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, idPeserta);
                checkPs.setInt(2, idSeminar);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Peserta dengan ID " + idPeserta + " sudah terdaftar di seminar ini.");
                    return; // Langsung keluar kalau sudah terdaftar
                }
            }

            // 3. Masukkan data pendaftaran ke tabel pendaftaran
            String insertSql = "INSERT INTO pendaftaran (id_user, id_seminar) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, idPeserta);
                ps.setInt(2, idSeminar);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Peserta berhasil didaftarkan ke seminar!");
                } else {
                    System.out.println("Gagal mendaftarkan peserta.");
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("Error: ID harus berupa angka!");
        } catch (SQLException e) {
            System.err.println("Error database saat mendaftarkan peserta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void viewPesertaByRole(Connection conn, String roleName) throws SQLException {
        String query = "SELECT u.id_user, u.nama, u.email FROM user u JOIN role r ON u.id_role = r.id_role WHERE r.nama_role = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, roleName);
            ResultSet rs = ps.executeQuery();

            System.out.println("------------------------------------------------------------------");
            System.out.printf("| %-5s | %-20s | %-30s |%n", "ID", "Nama", "Email");
            System.out.println("------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-5d | %-20s | %-30s |%n",
                        rs.getInt("id_user"),
                        rs.getString("nama"),
                        rs.getString("email"));
            }
            System.out.println("------------------------------------------------------------------");
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