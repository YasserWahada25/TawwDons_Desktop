package services;

import models.Article;
import models.Commentaire;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private final UserService userService = new UserService();

    public void ajouter(Commentaire c) {
        String sql = "INSERT INTO commentaire (content, created_at, etat, article_id, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getContent());
            stmt.setTimestamp(2, Timestamp.valueOf(c.getCreatedAt()));
            stmt.setString(3, c.getEtat());
            stmt.setInt(4, c.getArticle().getId());
            stmt.setInt(5, c.getUser().getId());

            stmt.executeUpdate();
            System.out.println("✅ Commentaire ajouté avec succès.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifier(Commentaire c) {
        String sql = "UPDATE commentaire SET content = ?, etat = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getContent());
            stmt.setString(2, c.getEtat());
            stmt.setInt(3, c.getId());

            stmt.executeUpdate();
            System.out.println("✅ Commentaire modifié avec succès.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Commentaire supprimé.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Commentaire> getCommentairesByArticleId(int articleId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = """
            SELECT c.*, u.id as uid, u.nom, u.prenom, u.email
              FROM commentaire c
              JOIN user u ON c.user_id = u.id
             WHERE c.article_id = ?
        """;

        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setContent(rs.getString("content"));
                    c.setEtat(rs.getString("etat"));
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    User u = new User();
                    u.setId(rs.getInt("uid"));
                    u.setNom(rs.getString("nom"));
                    u.setPrenom(rs.getString("prenom"));
                    u.setEmail(rs.getString("email")); // ✅ nécessaire pour l’envoi email
                    c.setUser(u);

                    Article a = new Article();
                    a.setId(articleId);
                    c.setArticle(a); // éviter le NullPointer dans getTitre()

                    commentaires.add(c);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaires;
    }



    public int countByUserAndArticle(int userId, int articleId) {
        String sql = "SELECT COUNT(*) FROM commentaire WHERE user_id = ? AND article_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void desactiver(int commentaireId) {
        String sql = "UPDATE commentaire SET etat = 'non valide' WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, commentaireId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
