package models;

import java.time.LocalDateTime;


public class Commentaire {
    private int id;
    private String content;
    private String etat;
    private LocalDateTime createdAt;
    private Article article;
    private User user;

    public Commentaire() {

    }

    public Commentaire(int id,
                       String content,
                       String etat,
                       LocalDateTime createdAt,
                       Article article,
                       User user) {
        this.id = id;
        this.content = content;
        this.etat = etat;
        this.createdAt = createdAt;
        this.article = article;
        this.user = user;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getEtat() {
        return etat;
    }
    public void setEtat(String etat) {
        this.etat = etat;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Article getArticle() {
        return article;
    }
    public void setArticle(Article article) {
        this.article = article;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
