package models;

public class Reaction {
    private int userId;
    private int articleId;
    private boolean isLike;

    public Reaction() {}

    public Reaction(int userId, int articleId, boolean isLike) {
        this.userId    = userId;
        this.articleId = articleId;
        this.isLike    = isLike;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "userId=" + userId +
                ", articleId=" + articleId +
                ", isLike=" + isLike +
                '}';
    }
}
