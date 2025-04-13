
package models;

import java.time.LocalDateTime;

public class Messagerie {
    private int id;
    private int expediteurId;
    private int destinataireId;
    private int demandeDonId;
    private String contenu;
    private LocalDateTime dateEnvoi;

    public Messagerie() {
        this.dateEnvoi = LocalDateTime.now();
    }

    public Messagerie(int id, int expediteurId, int destinataireId, int demandeDonId, String contenu, LocalDateTime dateEnvoi) {
        this.id = id;
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.demandeDonId = demandeDonId;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExpediteurId() {
        return expediteurId;
    }

    public void setExpediteurId(int expediteurId) {
        this.expediteurId = expediteurId;
    }

    public int getDestinataireId() {
        return destinataireId;
    }

    public void setDestinataireId(int destinataireId) {
        this.destinataireId = destinataireId;
    }

    public int getDemandeDonId() {
        return demandeDonId;
    }

    public void setDemandeDonId(int demandeDonId) {
        this.demandeDonId = demandeDonId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }
}
