import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static SeminarDAO seminarDAO = new SeminarDAO();
    private static PesertaDAO pesertaDAO = new PesertaDAO();
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
            System.out.println("6. Exit");
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
                            pesertaDAO.viewUser();
                            break; 
                        case 2:
                            pesertaDAO.addUser();
                            break;
                        case 3:
                            pesertaDAO.editUser();
                            break;
                        case 4:
                            pesertaDAO.deleteUser();
                            break;
                        case 5:
                            pesertaDAO.daftarSeminar();
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
                            seminarDAO.viewSeminars();
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
                            sesiSeminarDAO.viewSesiSeminars();
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
                            sertifikatDAO.viewKelulusan();
                            break;
                        case 2:
                            System.out.println("Keluar....");
                            break;
                        default:
                        System.out.println("Pilihan tidak valid");

                    }
                    break;
                case 6:                 
                    System.out.println("Keluar...");
                    System.exit(0);
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }
}
