import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import view.SeminarView;
import view.UserView;

public class UserDAO {
    private Scanner sc = new Scanner(System.in);
    private SeminarDAO seminarDAO = new SeminarDAO();
    private UndoStack undoStack = new UndoStack();

    public List<UserView> getUsersForView() {
        List<UserView> userList = new ArrayList<>();
        
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
            
            while (rs.next()) {
                UserView user = new UserView(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("email"),
                    rs.getString("nama_role")
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data user untuk tampilan:");
            e.printStackTrace();
        }
        return userList;
    }

    public List<SeminarView> getSeminarsForView(){
        List<SeminarView> seminarList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.id_seminar, s.tema, s.tanggal, l.nama_lokasi " +
                 "FROM seminar s " +
                 "LEFT JOIN lokasi l ON s.id_lokasi = l.id_lokasi")) {

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
        return seminarList;
    }

    public List<UserView> getPesertaBySeminarForView(int seminarId) {
        List<UserView> pesertaList = new ArrayList<>();
        String query = "SELECT u.id_user, u.nama, u.email, r.nama_role " +
                       "FROM user u " +
                       "JOIN role r ON u.id_role = r.id_role " +
                       "JOIN pendaftaran p ON u.id_user = p.id_user " +
                       "WHERE p.id_seminar = ? " +
                       "ORDER BY u.nama";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, seminarId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UserView user = new UserView(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("email"),
                    rs.getString("nama_role")
                );
                pesertaList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data peserta berdasarkan seminar untuk tampilan:");
            e.printStackTrace();
        }
        return pesertaList;
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
            // 1. Tampilkan daftar seminar
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

            System.out.print("Masukkan ID Seminar yang ingin diikuti: ");
            int idSeminar = Integer.parseInt(sc.nextLine());

            // 2. Tampilkan daftar peserta yang punya role "peserta"
            System.out.println("\n=== DAFTAR PESERTA YANG BISA MENDAFTAR ===");
            List<UserView> pesertaList = getPesertaByRoleForView("peserta");

            if (pesertaList.isEmpty()) {
                System.out.println("Tidak ada peserta terdaftar.");
                return; // Keluar jika tidak ada peserta
            } else {
                // Cetak header tabel peserta
                System.out.printf("%-5s | %-20s | %-30s | %-10s%n",
                    "ID", "Nama", "Email", "Role");
                System.out.println("------------------------------------------------------------------");
                
                // Loop untuk mencetak setiap objek UserView (peserta)
                for (UserView peserta : pesertaList) {
                    System.out.printf("%-5d | %-20s | %-30s | %-10s%n",
                            peserta.getIdUser(),
                            peserta.getNama(),
                            peserta.getEmail(),
                            peserta.getNamaRole()); // Akan menampilkan 'peserta'
                }
                System.out.println("------------------------------------------------------------------");
            }

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

    public List<UserView> getPesertaByRoleForView(String roleName) {
        List<UserView> userList = new ArrayList<>();
        String query = "SELECT u.id_user, u.nama, u.email, r.nama_role " +
                       "FROM user u JOIN role r ON u.id_role = r.id_role " +
                       "WHERE r.nama_role = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, roleName);
            ResultSet rs = ps.executeQuery();
                        
            while (rs.next()) {
                UserView user = new UserView(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("email"),
                    rs.getString("nama_role")
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data peserta berdasarkan role untuk tampilan:");
            e.printStackTrace();
        }
        return userList;
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

    public void generateLoyalParticipantsReport() {
        System.out.println("\n=== LAPORAN PESERTA SETIA (SUBQUERY) ===");
        System.out.println("Daftar peserta yang terdaftar di lebih dari satu seminar\n");

        String query = "SELECT " +
                       "    u.id_user, " +
                       "    u.nama, " +
                       "    u.email " +
                       "FROM " +
                       "    user u " +
                       "WHERE " +
                       "    u.id_role = (SELECT id_role FROM role WHERE nama_role = 'peserta') " +
                       "    AND u.id_user IN ( " +
                       "        SELECT " +
                       "            p.id_user " +
                       "        FROM " +
                       "            pendaftaran p " +
                       "        GROUP BY " +
                       "            p.id_user " +
                       "        HAVING " +
                       "            COUNT(p.id_seminar) > 1 " +
                       "    ) " +
                       "ORDER BY " +
                       "    u.nama;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Tidak ada peserta yang terdaftar di lebih dari satu seminar.");
                return;
            }

            System.out.printf("%-5s | %-25s | %-30s%n",
                              "ID", "Nama Peserta", "Email");
            System.out.println("----------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d | %-25s | %-30s%n",
                                  rs.getInt("id_user"),
                                  rs.getString("nama"),
                                  rs.getString("email"));
            }
            System.out.println("----------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Error saat membuat laporan SubQuery peserta setia:");
            e.printStackTrace();
        }
    }
}