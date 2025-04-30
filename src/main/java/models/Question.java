package models;

public class Question {
    private int id;
    private int evaluationId;
    private String contenu;
    private String type; // qcm ou numeric
    private String bonneReponse;

    public Question() {}

    public Question(int id, int evaluationId, String contenu, String type, String bonneReponse) {
        this.id = id;
        this.evaluationId = evaluationId;
        this.contenu = contenu;
        this.type = type;
        this.bonneReponse = bonneReponse;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBonneReponse() { return bonneReponse; }
    public void setBonneReponse(String bonneReponse) { this.bonneReponse = bonneReponse; }
}
