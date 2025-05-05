package org.example.event.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class event {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final IntegerProperty categorie_id = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    private final BooleanProperty cancelled = new SimpleBooleanProperty(false);

    public event(int id, String nom, String description, String image, int cateegorie_id, LocalDate date, String location) {
        setId(id);
        setNom(nom);
        setDescription(description);
        setImage(image);
        setCategorie_id(cateegorie_id);
        setDate(date);
        setLocation(location);
    }
    public event(int id, String nom, String description, String image, int cateegorie_id, LocalDate date, String location, boolean cancelled) {
        setId(id);
        setNom(nom);
        setDescription(description);
        setImage(image);
        setCategorie_id(cateegorie_id);
        setDate(date);
        setLocation(location);
        setCancelled(cancelled);

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

    public int getCategorie_id() {
        return categorie_id.get();
    }
    public void setCategorie_id(int categorie_id) {
        this.categorie_id.set(categorie_id);
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    // Getter pour la propriété date
    public LocalDate getDate() {
        return date.get();
    }

    // Property getter pour le binding JavaFX
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public StringProperty locationProperty() {
        return location;
    }

    // Méthodes pour l'annulation
    public boolean isCancelled() {
        return cancelled.get();
    }

    public BooleanProperty cancelledProperty() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled.set(cancelled);
    }

    @Override
    public String toString() {
        return "event{" +
                "id=" + id +
                ", nom=" + nom +
                ", description=" + description +
                ", image=" + image +
                ", categorie_id=" + categorie_id +
                ", date=" + date +
                ", location=" + location +
                ", cancelled=" + cancelled +
                '}';
    }
}
