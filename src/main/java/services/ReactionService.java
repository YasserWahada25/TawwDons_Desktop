package services;

import models.Reaction;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReactionService {

    /**
     * Récupère une réaction (like/dislike) d’un utilisateur pour un article donné.
     */
    public Reaction findByUserAndArticle(int userId, int articleId) {
        String sql = "SELECT is_like FROM reaction WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Reaction(userId, articleId, rs.getBoolean("is_like"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réaction : " + e.getMessage());
        }
        return null;
    }

    /**
     * Ajoute une nouvelle réaction.
     */
    public void add(Reaction reaction) {
        String sql = "INSERT INTO reaction(user_id, article_id, is_like) VALUES (?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reaction.getUserId());
            ps.setInt(2, reaction.getArticleId());
            ps.setBoolean(3, reaction.isLike());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réaction : " + e.getMessage());
        }
    }

    /**
     * Met à jour une réaction existante.
     */
    public void update(Reaction reaction) {
        String sql = "UPDATE reaction SET is_like = ? WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, reaction.isLike());
            ps.setInt(2, reaction.getUserId());
            ps.setInt(3, reaction.getArticleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la réaction : " + e.getMessage());
        }
    }

    /**
     * Supprime une réaction.
     */
    public void remove(int userId, int articleId) {
        String sql = "DELETE FROM reaction WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réaction : " + e.getMessage());
        }
    }
}
