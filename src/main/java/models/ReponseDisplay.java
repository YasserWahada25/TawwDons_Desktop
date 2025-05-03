package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReponseDisplay {
    private final StringProperty utilisateur, question, reponse, bonne, statut;

    public ReponseDisplay(String utilisateur, String question, String reponse, String bonne, String statut) {
        this.utilisateur = new SimpleStringProperty(utilisateur);
        this.question = new SimpleStringProperty(question);
        this.reponse = new SimpleStringProperty(reponse);
        this.bonne = new SimpleStringProperty(bonne);
        this.statut = new SimpleStringProperty(statut);
    }

    public StringProperty utilisateurProperty() { return utilisateur; }
    public StringProperty questionProperty() { return question; }
    public StringProperty reponseProperty() { return reponse; }
    public StringProperty bonneProperty() { return bonne; }
    public StringProperty statutProperty() { return statut; }
}
