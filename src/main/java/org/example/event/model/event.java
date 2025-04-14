package org.example.event.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class event {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final IntegerProperty cateegorie_id = new SimpleIntegerProperty();

    public event(int id, String nom, String description, String image, int cateegorie_id) {
        setId(id);
        setNom(nom);
        setDescription(description);
        setImage(image);
    }
    public event(int id, String nom, String description, String image) {
        setId(id);
        setNom(nom);
        setDescription(description);
        setImage(image);
    }
    public int getId() {
        return id.get();
    }
    public void setId(int id) {
        this.id.set(id);
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getImage() {
        return image.get();
    }

    public void setImage(String image) {
        this.image.set(image);
    }

    public int getCateegorie_id() {
        return cateegorie_id.get();
    }
    public void setCateegorie_id(int cateegorie_id) {
        this.cateegorie_id.set(cateegorie_id);
    }

    @Override
    public String toString() {
        return "event{" +
                "id=" + id +
                ", nom=" + nom +
                ", description=" + description +
                ", image=" + image +
                ", cateegorie_id=" + cateegorie_id +
                '}';
    }
}
