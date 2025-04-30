package services;

import models.CommentReply;
import models.Commentaire;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentReplyService {

    public List<CommentReply> getRepliesByCommentId(int commentId) {
        List<CommentReply> replies = new ArrayList<>();
        String sql = """
            SELECT cr.id,
                   cr.content,
                   cr.created_at,
                   cr.etat,
                   cr.comment_id,
                   u.id        AS user_id,
                   u.nom       AS user_nom,
                   u.prenom    AS user_prenom,
                   u.email     AS user_email
              FROM comment_reply cr
              JOIN user u ON u.id = cr.user_id
             WHERE cr.comment_id = ?
          ORDER BY cr.created_at ASC
        """;

        try ( Connection conn = MyDataBase.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) ) {

            ps.setInt(1, commentId);
            try ( ResultSet rs = ps.executeQuery() ) {
                while (rs.next()) {
                    CommentReply r = new CommentReply();

                    r.setId(rs.getInt("id"));
                    r.setContent(rs.getString("content"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    r.setEtat(rs.getString("etat"));

                    Commentaire parent = new Commentaire();
                    parent.setId(rs.getInt("comment_id"));
                    r.setCommentaire(parent);

                    User u = new User(1, "Degani", "Omar", "dagnouchamroush@gmail.com");
                    u.setId(rs.getInt("user_id"));
                    u.setNom(rs.getString("user_nom"));
                    u.setPrenom(rs.getString("user_prenom"));
                    u.setEmail(rs.getString("user_email"));
                    r.setUser(u);

                    replies.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }

    /**
     * Ajoute une nouvelle réponse en base.
     */
    public void add(CommentReply reply) {
        String sql = "INSERT INTO comment_reply (comment_id, user_id, content, created_at, etat) VALUES (?, ?, ?, ?, ?)";
        try ( Connection conn = MyDataBase.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) ) {

            ps.setInt(1, reply.getCommentaire().getId());
            ps.setInt(2, reply.getUser().getId());
            ps.setString(3, reply.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(reply.getCreatedAt()));
            ps.setString(5, reply.getEtat());
            ps.executeUpdate();

            try ( ResultSet keys = ps.getGeneratedKeys() ) {
                if (keys.next()) {
                    reply.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Met à jour le contenu d’une réponse existante.
     */
    public void update(CommentReply reply) {
        String sql = "UPDATE comment_reply SET content = ?, etat = ? WHERE id = ?";
        try ( Connection conn = MyDataBase.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) ) {

            ps.setString(1, reply.getContent());
            ps.setString(2, reply.getEtat());
            ps.setInt(3, reply.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprime une réponse par son ID.
     */
    public void delete(int replyId) {
        String sql = "DELETE FROM comment_reply WHERE id = ?";
        try ( Connection conn = MyDataBase.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) ) {

            ps.setInt(1, replyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compte le nombre de réponses d’un utilisateur sur un même commentaire.
     */
    public int countByUserAndComment(int userId, int commentId) {
        String sql = "SELECT COUNT(*) FROM comment_reply WHERE user_id = ? AND comment_id = ?";
        try ( Connection conn = MyDataBase.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) ) {

            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            try ( ResultSet rs = ps.executeQuery() ) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
