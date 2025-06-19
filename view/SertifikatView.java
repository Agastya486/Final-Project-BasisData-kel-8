package view;
public class SertifikatView {
    private int idSertifikat;
    private String namaPeserta;
    private String temaSeminar;
    private String tanggalCetak;

    // Constructor
    public SertifikatView(int idSertifikat, String namaPeserta, String temaSeminar, String tanggalCetak) {
        this.idSertifikat = idSertifikat;
        this.namaPeserta = namaPeserta;
        this.temaSeminar = temaSeminar;
        this.tanggalCetak = tanggalCetak;
    }

    // Getters
    public int getIdSertifikat() {
        return idSertifikat;
    }

    public String getNamaPeserta() {
        return namaPeserta;
    }

    public String getTemaSeminar() {
        return temaSeminar;
    }

    public String getTanggalCetak() {
        return tanggalCetak;
    }
}