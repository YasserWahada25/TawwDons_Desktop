package services;

import dao.CommentReactionDAO;
import models.CommentReaction;
import utils.MyDataBase;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentReactionService {
    // DAO pour accéder aux données
    private final CommentReactionDAO dao = new CommentReactionDAO();

/***
    public CommentReaction find(int userId, int commentaireId) {
        String sql = """
            SELECT id, `type`, created_at
            FROM comment_reaction
            WHERE user_id = ? AND commentaire_id = ?
            """;
        try (Connection c = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, commentaireId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CommentReaction cr = new CommentReaction();
                    cr.setId(rs.getInt("id"));
                    cr.setUserId(userId);
                    cr.setCommentaireId(commentaireId);
                    cr.setType(rs.getString("type"));
                    cr.setCreatedAt(rs.getTimestamp("created_at")
                            .toLocalDateTime());
                    return cr;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
***/
public CommentReaction find(int userId, int commentaireId) {
    String sql = """
        SELECT id, `type`, created_at
        FROM comment_reaction
        WHERE user_id = ? AND commentaire_id = ?
        """;
    try (Connection c = MyDataBase.getInstance().getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, userId);
        ps.setInt(2, commentaireId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                CommentReaction cr = new CommentReaction();
                cr.setId(rs.getInt("id"));
                cr.setUserId(userId);
                cr.setCommentaireId(commentaireId);
                cr.setType(rs.getString("type"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    cr.setCreatedAt(ts.toLocalDateTime());
                } else {
                    cr.setCreatedAt(null); // ou LocalDateTime.now()
                }

                return cr;
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return null;
}


    public void react(int userId, int commentaireId, String type) {
        CommentReaction exist = find(userId, commentaireId);

        if (exist == null) {
            // 1) Pas de réaction → insertion
            String insert = """
                INSERT INTO comment_reaction
                  (user_id, commentaire_id, `type`)
                VALUES (?,?,?)
                """;
            try (Connection c = MyDataBase.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement(insert)) {

                ps.setInt(1, userId);
                ps.setInt(2, commentaireId);
                ps.setString(3, type);
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } else if (exist.getType().equals(type)) {
            // 2) Même réaction recliquée → suppression
            String delete = "DELETE FROM comment_reaction WHERE id = ?";
            try (Connection c = MyDataBase.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement(delete)) {

                ps.setInt(1, exist.getId());
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } else {
            // 3) Réaction différente → mise à jour
            String upd = """
                UPDATE comment_reaction
                SET `type` = ?
                WHERE id = ?
                """;
            try (Connection c = MyDataBase.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement(upd)) {

                ps.setString(1, type);
                ps.setInt(2, exist.getId());
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Compte, pour un commentaire donné, le nombre de réactions par type.
     */
    public Map<String, Long> countReactionsByComment(int commentaireId) {
        List<CommentReaction> list = dao.findByCommentId(commentaireId);
        return list.stream()
                .collect(Collectors.groupingBy(
                        CommentReaction::getType,
                        Collectors.counting()
                ));
    }

    /**
     * Indique s'il existe au moins une réaction pour ce commentaire.
     * @return true si une ou plusieurs réactions existent, false sinon
     */
    public boolean hasReactions(int commentaireId) {
        List<CommentReaction> list = dao.findByCommentId(commentaireId);
        return !list.isEmpty();
    }
}
