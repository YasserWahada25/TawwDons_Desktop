package models;

public class Condidat {
    private int id;
    private Integer offreId;  // Peut être null selon la structure
    private String nom;
    private String prenom;
    private String email;
    private int telephone;
    private String cv;

    // Constructeur par défaut
    public Condidat() {
    }

    // Constructeur avec tous les paramètres
    public Condidat(int id, Integer offreId, String nom, String prenom, String email, int telephone, String cv) {
        this.id = id;
        this.offreId = offreId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.cv = cv;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getOffreId() {
        return offreId;
    }

    public void setOffreId(Integer offreId) {
        this.offreId = offreId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    @Override
    public String toString() {
        return "Condidat{" +
                "id=" + id +
                ", offreId=" + offreId +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone=" + telephone +
                ", cv='" + cv + '\'' +
                '}';
    }
} 