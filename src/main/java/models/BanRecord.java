package models;

public class BanRecord {
    private String utilisateur;
    private String mot;
    private String date;

    public BanRecord(String utilisateur, String mot, String date) {
        this.utilisateur = utilisateur;
        this.mot = mot;
        this.date = date;
    }

    public String getUtilisateur() { return utilisateur; }
    public String getMot() { return mot; }
    public String getDate() { return date; }

    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
    public void setMot(String mot) { this.mot = mot; }
    public void setDate(String date) { this.date = date; }
}
