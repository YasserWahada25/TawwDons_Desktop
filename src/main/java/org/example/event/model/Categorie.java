package org.example.event.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Categorie   {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();

    public int getId() {
        return id.get();
    }
    public void setId(int id) {
        this.id.set(id);

    }
    public String getType() {
        return type.get();
    }
    public void setType(String type) {
        this.type.set(type);
    }


    public Categorie(int id, String type) {
        setId(id);
        setType(type);
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", type=" + type +
                '}';
    }
}

