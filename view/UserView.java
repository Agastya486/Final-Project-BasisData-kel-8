package view;
public class UserView {
    private int idUser;
    private String nama;
    private String email;
    private String namaRole;

    // Constructor
    public UserView(int idUser, String nama, String email, String namaRole) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.namaRole = namaRole;
    }

    // Getter
    public int getIdUser() {
        return idUser;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getNamaRole() {
        return namaRole;
    }
}