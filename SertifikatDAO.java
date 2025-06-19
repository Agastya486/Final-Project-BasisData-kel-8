import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import view.SertifikatView;

public class SertifikatDAO {
    private Scanner sc = new Scanner(System.in);
    private SeminarDAO seminarDAO = new SeminarDAO();

    public List<SertifikatView> getSertifikatForView(int seminarId) {
        List<SertifikatView> sertifikatList = new ArrayList<>();
        String query;
        PreparedStatement ps;

        try (Connection conn = DBConnection.getConnection()) {
            if (seminarId == 0) {
                query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar, " +
                        "s.tanggal_cetak " +
                        "FROM sertifikat s " +
                        "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                        "JOIN user u ON p.id_user = u.id_user " +
                        "JOIN seminar sm ON p.id_seminar = sm.id_seminar " +
                        "ORDER BY sm.id_seminar, u.nama";
                ps = conn.prepareStatement(query);
            } else {
                query = "SELECT s.id_sertifikat, u.nama AS nama_peserta, sm.tema AS tema_seminar, " +
                        "s.tanggal_cetak " +
                        "FROM sertifikat s " +
                        "JOIN pendaftaran p ON s.id_pendaftaran = p.id_pendaftaran " +
                        "JOIN user u ON p.id_user = u.id_user " +
                        "JOIN seminar sm ON p.id_seminar = sm.id_seminar " +
                        "WHERE sm.id_seminar = ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, seminarId);
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                SertifikatView sertifikat = new SertifikatView(
                    rs.getInt("id_sertifikat"),
                    rs.getString("nama_peserta"),
                    rs.getString("tema_seminar"),
                    rs.getDate("tanggal_cetak") != null ? rs.getDate("tanggal_cetak").toString() : "N/A"
                );
                sertifikatList.add(sertifikat);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil data sertifikat:");
            e.printStackTrace();
        }
        return sertifikatList;
    }
    
}