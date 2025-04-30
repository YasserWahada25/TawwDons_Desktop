package services;

import models.Reaction;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReactionService {

    public Reaction findByUserAndArticle(int userId, int articleId) {
        String sql = "SELECT is_like FROM reaction WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isLike = rs.getBoolean("is_like");
                    return new Reaction(userId, articleId, isLike);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void add(Reaction reaction) {
        String sql = "INSERT INTO reaction(user_id, article_id, is_like) VALUES (?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reaction.getUserId());
            ps.setInt(2, reaction.getArticleId());
            ps.setBoolean(3, reaction.isLike());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Reaction reaction) {
        String sql = "UPDATE reaction SET is_like = ? WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, reaction.isLike());
            ps.setInt(2, reaction.getUserId());
            ps.setInt(3, reaction.getArticleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(int userId, int articleId) {
        String sql = "DELETE FROM reaction WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
