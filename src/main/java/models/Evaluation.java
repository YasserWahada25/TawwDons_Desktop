package models;

import java.time.LocalDateTime;

public class Evaluation {
    private int id;
    private String nom;
    private String description;
    private String type;
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public Evaluation() {
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur complet
    public Evaluation(int id, String nom, String description, String type, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.dateCreation = dateCreation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Affichage personnalisé dans les ComboBox
    @Override
    public String toString() {
        return nom;
    }
}
