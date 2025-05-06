package models;


import java.time.LocalDate;

public class Dons {
    private int id;
    private String titre;
    private String description;
    private LocalDate dateCreation;
    private int donneurId;  // yresferenciii 3al User ID (donneur)
    private boolean valide;
    private String imageUrl;
    private String categorie;
    private String nomDonneur;
    private String emailDonneur;

    public Dons() {
        this.dateCreation = LocalDate.now();
    }


    public Dons(int id, String titre, String description, LocalDate dateCreation,
                int donneurId, boolean valide, String imageUrl, String categorie) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateCreation = dateCreation;
        this.donneurId = donneurId;
        this.valide = valide;
        this.imageUrl = imageUrl;
        this.categorie = categorie;
    }

    public String validate() {
        if (titre == null || titre.trim().isEmpty()) {
            return "Le titre est obligatoire.";
        }

        if (!Character.isUpperCase(titre.charAt(0))) {
            return "Le titre doit commencer par une lettre majuscule.";
        }


        if (titre.length() < 3 || titre.length() > 100) {
            return "Le titre doit contenir entre 3 et 100 caractères.";
        }

        if (description == null || description.trim().isEmpty()) {
            return "La description est obligatoire.";
        }

        if (description.length() < 7 || description.length() > 255) {
            return "La description doit contenir entre 10 et 255 caractères.";
        }



        if (dateCreation == null) {
            return "La date de création est obligatoire.";
        }

        if (categorie == null || categorie.trim().isEmpty()) {
            return "La catégorie est obligatoire.";
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "Veuillez sélectionner une image.";
        }

        return null; // null = pas d’erreur
    }


    // Getters et Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getDonneurId() {
        return donneurId;
    }

    public void setDonneurId(int donneurId) {
        this.donneurId = donneurId;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getNomDonneur() { return nomDonneur; }
    public void setNomDonneur(String nomDonneur) { this.nomDonneur = nomDonneur; }

    public String getEmailDonneur() { return emailDonneur; }
    public void setEmailDonneur(String emailDonneur) { this.emailDonneur = emailDonneur; }
}

