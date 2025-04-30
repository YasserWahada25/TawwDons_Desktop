package models;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String content;
    private String etat;
    private LocalDateTime createdAt;
    private Article article;

    private static final User staticUser = new User(1, "omar", "degani", "dagnouchamroush@gmail.com");

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public User getUser() {
        return staticUser;
    }

    public void setUser(User user) {
    }
}
