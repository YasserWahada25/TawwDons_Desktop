package org.example.event.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class participant {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final IntegerProperty numtel = new SimpleIntegerProperty();
    private final IntegerProperty event_id = new SimpleIntegerProperty();

    public participant(int id, String nom,String prenom, String email, int numtel, int event_id) {

        setId(id);
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setNumtel(numtel);
        setEvent_id(event_id);

    }

    public participant(String nom, String prenom, String email, int numtel, int event_id) {
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setNumtel(numtel);
        setEvent_id(event_id);

    }

    // Getters
    public int getId() { return id.get(); }
    public String getNom() { return nom.get(); }
    public String getPrenom() { return prenom.get(); }
    public String getEmail() { return email.get(); }
    public int getNumtel() { return numtel.get(); }
    public int getEvent_id() { return event_id.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setNom(String nom) { this.nom.set(nom); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setEmail(String email) { this.email.set(email); }
    public void setNumtel(int numtel) { this.numtel.set(numtel); }
    public void setEvent_id(int event_id) { this.event_id.set(event_id); }


    @Override
    public String toString() {
        return "participant{" +
                "id=" + id +
                ", nom=" + nom +
                ", prenom=" + prenom +
                ", email=" + email +
                ", numtel=" + numtel +
                ", event_id=" + event_id +
                '}';
    }
}
