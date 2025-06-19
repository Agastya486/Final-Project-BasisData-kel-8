import java.util.List;
import java.util.Scanner;
import view.SeminarView;
import view.SertifikatView;
import view.SesiSeminarView;
import view.UserView;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static SeminarDAO seminarDAO = new SeminarDAO();
    private static UserDAO userDAO = new UserDAO();
    private static SesiSeminarDAO sesiSeminarDAO = new SesiSeminarDAO();
    private static KehadiranDAO kehadiranDAO = new KehadiranDAO();
    private static SertifikatDAO sertifikatDAO = new SertifikatDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== MENU UTAMA ===");
            System.out.println("1. Manajemen User");
            System.out.println("2. Manajemen Seminar");
            System.out.println("3. Manajemen Sesi Seminar");
            System.out.println("4. Manajemen Kehadiran");
            System.out.println("5. Manajemen Sertifikat");
            System.out.println("6. Laporan");
            System.out.println("7. Keluar");
            System.out.print("Pilih menu: ");
            int choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (choice) {
                case 1:
                    System.out.println("\n=== MANAJEMEN USER ===");
                    System.out.println("1. Lihat user");
                    System.out.println("2. Tambah user");
                    System.out.println("3. Edit user");
                    System.out.println("4. Hapus user");
                    System.out.println("5. Daftarkan peserta");
                    System.out.println("6. Exit");

                    System.out.print("Pilihan: ");
                    int choice1 = sc.nextInt();
                    sc.nextLine();

                    switch (choice1){
                        case 1:
                            System.out.println("\n=== DAFTAR USER ===\n");
                            List<UserView> users = userDAO.getUsersForView(); 

                            if (users.isEmpty()) {
                                System.out.println("Tidak ada user yang ditemukan.");
                            } else {
                                // Cetak header tabel
                                System.out.println("------------------------------------------------------------------");
                                System.out.printf("| %-5s | %-20s | %-30s | %-10s |%n", "ID", "Nama", "Email", "Role");
                                System.out.println("------------------------------------------------------------------");
                                
                                // Loop setiap objek UserView dalam list dan cetak datanya
                                for (UserView user : users) {
                                    System.out.printf("| %-5d | %-20s | %-30s | %-10s |%n",
                                            user.getIdUser(),
                                            user.getNama(),
                                            user.getEmail(),
                                            user.getNamaRole());
                                }
                                System.out.println("------------------------------------------------------------------");
                            }
                            break;
                        case 2:
                            userDAO.addUser();
                            break;
                        case 3:
                            userDAO.editUser();
                            break;
                        case 4:
                            userDAO.deleteUser();
                            break;
                        case 5:
                            userDAO.daftarSeminar();
                            break;
                        case 6:
                            System.out.println("Keluar....");
                            break;
                        default:
                            System.out.println("Pilihan tidak valid!");
                    }
                    break;
                case 2:
                    System.out.println("\n=== MANAJEMEN SEMINAR ===");
                    System.out.println("1. Lihat seminar");
                    System.out.println("2. Tambah seminar");
                    System.out.println("3. Edit seminar");
                    System.out.println("4. Hapus seminar");
                    System.out.println("5. Exit");

                    System.out.print("Pilihan: ");
                    int choice2 = sc.nextInt();
                    sc.nextLine();

                    switch (choice2){
                        case 1:
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
                            break; 
                        case 2:
                            seminarDAO.addSeminar();
                            break;
                        case 3:
                            seminarDAO.editSeminar();
                            break;
                        case 4:
                            seminarDAO.deleteSeminar();
                            break;
                        case 5:
                            System.out.println("Keluar....");
                            break;
                        default:
                            System.out.println("Pilihan tidak valid!");
                    }
                    break;
                case 3:
                    System.out.println("\n=== MANAJEMEN SESI SEMINAR ===");
                    System.out.println("1. Lihat sesi seminar");
                    System.out.println("2. Tambah sesi seminar");
                    System.out.println("3. Edit sesi seminar");
                    System.out.println("4. Hapus sesi seminar");
                    System.out.println("5. Exit");

                    System.out.print("Pilihan: ");
                    int choice3 = sc.nextInt();
                    sc.nextLine();

                    switch (choice3){
                        case 1:
                                System.out.println("\n=== DAFTAR SEMINAR UNTUK PILIHAN SESI ===\n");
                                List<SeminarView> seminarsToPick = seminarDAO.getSeminarsForView();

                                if (seminarsToPick.isEmpty()) {
                                    System.out.println("Tidak ada seminar yang ditemukan untuk melihat sesinya.");
                                    break;
                                } else {
                                    System.out.printf("%-5s | %-40s | %-15s | %-20s%n",
                                            "ID", "Tema", "Tanggal", "Lokasi");
                                    System.out.println("--------------------------------------------------------------------------------");

                                    for (SeminarView seminar : seminarsToPick) {
                                        System.out.printf("%-5d | %-40s | %-15s | %-20s%n",
                                                seminar.getIdSeminar(),
                                                seminar.getTema(),
                                                seminar.getTanggal(),
                                                seminar.getLokasi());
                                    }
                                    System.out.println("--------------------------------------------------------------------------------");
                                }

                                //  Minta ID Seminar
                                System.out.print("Pilih ID Seminar yang ingin dilihat sesinya: ");
                                int seminarIdChoice = Integer.parseInt(sc.nextLine());

                                // Panggil method baru di SesiSeminarDAO
                                List<SesiSeminarView> sesis = sesiSeminarDAO.getSesiSeminarsForView(seminarIdChoice);

                                if (sesis.isEmpty()) {
                                    System.out.println("Tidak ada sesi seminar ditemukan untuk ID Seminar " + seminarIdChoice + ".");
                                } else {
                                    System.out.println("\n=== DAFTAR SESI SEMINAR ===\n");
                                    // Cetak header tabel
                                    System.out.printf("%-5s | %-30s | %-25s | %-12s | %-8s | %-8s | %-20s%n",
                                        "ID Sesi", "Tema Seminar", "Judul Sesi", "Tanggal", "Mulai", "Selesai", "Pemateri");
                                    System.out.println("-----------------------------------------------------------------------------------------------------------------");
                                    
                                    // Loop melalui setiap objek SesiSeminarView dan cetak datanya
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
                            break; 
                        case 2:
                            sesiSeminarDAO.addSesiSeminar();
                            break;
                        case 3:
                            sesiSeminarDAO.editSesiSeminar();
                            break;
                        case 4:
                            sesiSeminarDAO.deleteSesiSeminar();
                            break;
                        case 5:
                            System.out.println("Keluar....");
                            break;
                        default:
                            System.out.println("Pilihan tidak valid!");
                    }
                    break;
                case 4:
                    System.out.println("\n=== MANAJEMEN KEHADIRAN ===");
                    System.out.println("1. Input Kehadiran");
                    System.out.println("2. Undo Kehadiran");
                    System.out.println("3. Exit");

                    System.out.print("Pilihan: ");
                    int choice4 = sc.nextInt();
                    sc.nextLine();

                    switch (choice4){
                        case 1:
                            kehadiranDAO.inputKehadiran();
                            break;
                        case 2:
                            kehadiranDAO.undoKehadiran();
                        case 3:
                            System.out.println("Keluar....");
                            break;
                        default:
                            System.out.println("Pilihan tidak valid!");
                    }
                    break;
                case 5:
                    System.out.println("\n=== MANAJEMEN SERTIFIKAT ===");
                    System.out.println("1. Cetak dan lihat sertifikat");
                    System.out.println("2. Exit");

                    System.out.print("Pilihan: ");
                    int choice5 = sc.nextInt();
                    sc.nextLine();

                    switch(choice5){
                        case 1:
                        // 1. Tampilkan daftar seminar terlebih dahulu
                        System.out.println("\n=== DAFTAR SEMINAR ===\n");
                        List<SeminarView> seminars = seminarDAO.getSeminarsForView();

                        if (seminars.isEmpty()) {
                            System.out.println("Tidak ada seminar yang ditemukan.");
                        } else {
                            System.out.printf("%-5s | %-40s | %-15s | %-20s%n",
                                    "ID", "Tema", "Tanggal", "Lokasi");
                            System.out.println("--------------------------------------------------------------------------------");

                            for (SeminarView seminar : seminars) {
                                System.out.printf("%-5d | %-40s | %-15s | %-20s%n",
                                        seminar.getIdSeminar(),
                                        seminar.getTema(),
                                        seminar.getTanggal(),
                                        seminar.getLokasi());
                            }
                            System.out.println("--------------------------------------------------------------------------------");
                        }
                        
                        // 2. Meminta input ID seminar yang ingin dilihat sertifikatnya
                        System.out.print("Masukkan ID Seminar untuk melihat sertifikat (0 untuk semua seminar): ");
                        int idSeminar = Integer.parseInt(sc.nextLine());
                        
                        // 3. Panggil method baru di SertifikatDAO
                        List<SertifikatView> sertifikatList = sertifikatDAO.getSertifikatForView(idSeminar);

                        if (sertifikatList.isEmpty()) {
                            System.out.println("Tidak ada sertifikat ditemukan untuk pilihan Anda.");
                        } else {
                            System.out.println("\n=== DAFTAR SERTIFIKAT ===");
                            System.out.println("-------------------------------------------------------------------------------------------");
                            System.out.printf("| %-5s | %-30s | %-40s | %-12s |%n", "ID", "Nama Peserta", "Tema Seminar", "Tgl Cetak");
                            System.out.println("-------------------------------------------------------------------------------------------");
                            
                            for (SertifikatView sertifikat : sertifikatList) {
                                System.out.printf("| %-5d | %-30s | %-40s | %-12s |%n",
                                        sertifikat.getIdSertifikat(),
                                        sertifikat.getNamaPeserta(),
                                        sertifikat.getTemaSeminar(),
                                        sertifikat.getTanggalCetak());
                                
                                System.out.println("--------------------------------------------------");
                                System.out.println("Mencetak sertifikat untuk:");
                                System.out.println("ID Sertifikat: " + sertifikat.getIdSertifikat());
                                System.out.println("Nama Peserta : " + sertifikat.getNamaPeserta());
                                System.out.println("Tema Seminar : " + sertifikat.getTemaSeminar());
                                System.out.println("--------------------------------------------------");
                                try {
                                    Thread.sleep(500);
                                    System.out.println("Sertifikat berhasil dicetak!");
                                } catch (InterruptedException e) {
                                    System.err.println("Gangguan saat mencetak");
                                }
                                System.out.println();
                            }
                            System.out.println("-------------------------------------------------------------------------------------------");
                        }                            
                        break;
                        case 2:
                            System.out.println("Keluar....");
                            break;
                        default:
                        System.out.println("Pilihan tidak valid");

                    }
                    break;
                    case 6:
                    System.out.println("\n=== MENU LAPORAN ===");
                    System.out.println("1. Laporan Kehadiran (CrossTab)");
                    System.out.println("2. Laporan Seminar & Pendaftar (CTE)");
                    System.out.println("3. Laporan Peserta Setia (SubQuery)");
                    System.out.println("4. Kembali ke Menu Utama");
                    System.out.print("Pilih laporan: ");
                    int reportChoice = sc.nextInt();
                    sc.nextLine();

                    switch (reportChoice) {
                        case 1:
                            kehadiranDAO.generateAttendanceCrossTabReport();
                            break;
                        case 2:
                            seminarDAO.generateSeminarParticipantsReport();
                            break;
                        case 3:
                            userDAO.generateLoyalParticipantsReport(); // Ini menggunakan userDAO, pastikan namanya benar
                            break;
                        case 4:
                            // Kembali ke menu utama
                            break;
                        default:
                            System.out.println("Pilihan tidak valid!");
                    }
                    break;
                    case 7:                 
                    System.out.println("Keluar...");
                    System.exit(0);
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }
}
