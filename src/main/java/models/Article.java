package models;

import java.util.Date;

public class Article {
    private int id;
    private int nombre_commentaire;
    private String titre;
    private String description;
    private String image;
    private String categorie;
    private Date created_at;
    private int likes;
    private int dislikes;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNombre_commentaire() { return nombre_commentaire; }
    public void setNombre_commentaire(int nombre_commentaire) { this.nombre_commentaire = nombre_commentaire; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    // Getters & Setters
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }
}
