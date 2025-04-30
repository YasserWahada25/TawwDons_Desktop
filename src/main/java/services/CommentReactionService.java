package services;

import models.CommentReaction;
import utils.MyDataBase;

import java.sql.*;

public class CommentReactionService {
    public CommentReaction find(int userId, int commentaireId) {
        String sql = "SELECT id, `type`, created_at FROM comment_reaction "
                + "WHERE user_id=? AND commentaire_id=?";
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
                    cr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
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
            String insert = "INSERT INTO comment_reaction(user_id, commentaire_id, `type`) VALUES (?,?,?)";
            try (Connection c = MyDataBase.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement(insert)) {
                ps.setInt(1, userId);
                ps.setInt(2, commentaireId);
                ps.setString(3, type);
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }

        } else {
            String upd = "UPDATE comment_reaction SET `type`=? WHERE id=?";
            try (Connection c = MyDataBase.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, type);
                ps.setInt(2, exist.getId());
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}
