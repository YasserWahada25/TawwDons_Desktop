package models;

import java.time.LocalDate;

public class DemandeDons {
    private int id;
    private int beneficiaireId;  // Représente l'ID de l'utilisateur (User)
    private int donsId;          // Représente l'ID du don
    private String statut;       // En attente, Acceptée, Refusée, Validée
    private LocalDate dateDemande;
    private boolean chatActif;

    public DemandeDons() {
        this.dateDemande = LocalDate.now();
        this.statut = "En attente";
        this.chatActif = false;
    }

    public DemandeDons(int id, int beneficiaireId, int donsId, String statut, LocalDate dateDemande, boolean chatActif) {
        this.id = id;
        this.beneficiaireId = beneficiaireId;
        this.donsId = donsId;
        this.statut = statut;
        this.dateDemande = dateDemande;
        this.chatActif = chatActif;
    }

    // Getters et Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBeneficiaireId() {
        return beneficiaireId;
    }

    public void setBeneficiaireId(int beneficiaireId) {
        this.beneficiaireId = beneficiaireId;
    }

    public int getDonsId() {
        return donsId;
    }

    public void setDonsId(int donsId) {
        this.donsId = donsId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDate dateDemande) {
        this.dateDemande = dateDemande;
    }

    public boolean isChatActif() {
        return chatActif;
    }

    public void setChatActif(boolean chatActif) {
        this.chatActif = chatActif;
    }

    public void activerChat() {
        this.chatActif = true;
    }
}

