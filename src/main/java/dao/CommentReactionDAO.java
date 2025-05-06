package dao;

import models.CommentReaction;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentReactionDAO {

    /**
     * Retourne la liste de toutes les réactions pour un commentaire donné.
     * @param commentId l'ID du commentaire
     * @return List<CommentReaction>
     */
    public List<CommentReaction> findByCommentId(int commentId) {
        List<CommentReaction> list = new ArrayList<>();
        String sql = """
            SELECT id, user_id, `type`, created_at
            FROM comment_reaction
            WHERE commentaire_id = ?
            """;

        try (Connection c = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommentReaction cr = new CommentReaction();
                    cr.setId(rs.getInt("id"));
                    cr.setUserId(rs.getInt("user_id"));
                    cr.setCommentaireId(commentId);
                    cr.setType(rs.getString("type"));
                    cr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(cr);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }
}
