package view;
public class SeminarView {
    private int idSeminar;
    private String tema;
    private String tanggal;
    private String lokasi;

    // Constructor
    public SeminarView(int idSeminar, String tema, String tanggal, String lokasi) {
        this.idSeminar = idSeminar;
        this.tema = tema;
        this.tanggal = tanggal;
        this.lokasi = lokasi;
    }

    // Getter
    public int getIdSeminar() {
        return idSeminar;
    }

    public String getTema() {
        return tema;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getLokasi() {
        return lokasi;
    }
}