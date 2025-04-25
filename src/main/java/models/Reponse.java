package models;

import java.time.LocalDateTime;

public class Reponse {
    private int id;
    private int questionId;
    private String utilisateur;
    private String reponse;
    private LocalDateTime dateReponse;
    private boolean bonne;

    public Reponse() {}

    public Reponse(int id, int questionId, String utilisateur, String reponse, LocalDateTime dateReponse, boolean bonne) {
        this.id = id;
        this.questionId = questionId;
        this.utilisateur = utilisateur;
        this.reponse = reponse;
        this.dateReponse = dateReponse;
        this.bonne = bonne;
    }

    public boolean isBonne() {
        return bonne;
    }

    public void setBonne(boolean bonne) {
        this.bonne = bonne;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }
}
