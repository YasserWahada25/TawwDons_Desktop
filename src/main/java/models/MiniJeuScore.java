package models;

import java.time.LocalDateTime;

public class MiniJeuScore {
    private int id;
    private String joueur;
    private int score;
    private String niveau;
    private LocalDateTime date;

    public MiniJeuScore(String joueur, int score, String niveau, LocalDateTime date) {
        this.joueur = joueur;
        this.score = score;
        this.niveau = niveau;
        this.date = date;
    }

    public String getJoueur() { return joueur; }
    public int getScore() { return score; }
    public String getNiveau() { return niveau; }
    public LocalDateTime getDate() { return date; }
}
