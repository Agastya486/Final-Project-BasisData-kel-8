package view;
public class SesiSeminarView {
    private int idSesi;
    private String temaSeminar;
    private String judulSesi;
    private String tanggalSesi;
    private String waktuMulai;  
    private String waktuSelesai; 
    private String namaPemateri; 

    // Constructor
    public SesiSeminarView(int idSesi, String temaSeminar, String judulSesi,
                           String tanggalSesi, String waktuMulai, String waktuSelesai, String namaPemateri) {
        this.idSesi = idSesi;
        this.temaSeminar = temaSeminar;
        this.judulSesi = judulSesi;
        this.tanggalSesi = tanggalSesi;
        this.waktuMulai = waktuMulai;
        this.waktuSelesai = waktuSelesai;
        this.namaPemateri = namaPemateri;
    }

    // Getters
    public int getIdSesi() { return idSesi; }
    public String getTemaSeminar() { return temaSeminar; }
    public String getJudulSesi() { return judulSesi; }
    public String getTanggalSesi() { return tanggalSesi; }
    public String getWaktuMulai() { return waktuMulai; }
    public String getWaktuSelesai() { return waktuSelesai; }
    public String getNamaPemateri() { return namaPemateri; }

}