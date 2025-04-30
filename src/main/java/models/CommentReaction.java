package models;

import java.time.LocalDateTime;

public class CommentReaction {
    private int id;
    private int userId;
    private int commentaireId;
    private String type;             // p.ex. "👍", "😂", ...
    private LocalDateTime createdAt;

    public CommentReaction() {}
    public CommentReaction(int userId, int commentaireId, String type) {
        this.userId = userId;
        this.commentaireId = commentaireId;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCommentaireId() {
        return commentaireId;
    }

    public void setCommentaireId(int commentaireId) {
        this.commentaireId = commentaireId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
