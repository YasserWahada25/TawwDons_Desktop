package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BanRow {
    private final StringProperty utilisateur;
    private final StringProperty mot;
    private final StringProperty date;

    public BanRow(String utilisateur, String mot, String date) {
        this.utilisateur = new SimpleStringProperty(utilisateur);
        this.mot = new SimpleStringProperty(mot);
        this.date = new SimpleStringProperty(date);
    }

    public String getUtilisateur() {
        return utilisateur.get();
    }

    public StringProperty utilisateurProperty() {
        return utilisateur;
    }

    public String getMot() {
        return mot.get();
    }

    public StringProperty motProperty() {
        return mot;
    }

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }
}
