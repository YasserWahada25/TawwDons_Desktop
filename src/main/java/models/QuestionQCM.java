package models;

import java.util.List;

public class QuestionQCM {
    private String question;
    private List<String> choix;
    private String bonneReponse;
    private String niveau;

    public QuestionQCM(String question, List<String> choix, String bonneReponse, String niveau) {
        this.question = question;
        this.choix = choix;
        this.bonneReponse = bonneReponse;
        this.niveau = niveau;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getChoix() {
        return choix;
    }

    public String getBonneReponse() {
        return bonneReponse;
    }

    public String getNiveau() {
        return niveau;
    }
}
