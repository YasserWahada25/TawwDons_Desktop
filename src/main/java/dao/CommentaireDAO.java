package dao;

import models.Article;
import models.Commentaire;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    /**
     * Insère un nouveau commentaire en base et met à jour l'ID généré.
     */
    public void insert(Commentaire c) {
        String sql = "INSERT INTO commentaire (content, created_at, etat, article_id, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getContent());
            ps.setTimestamp(2, Timestamp.valueOf(c.getCreatedAt()));
            ps.setString(3, c.getEtat());
            ps.setInt(4, c.getArticle().getId());
            ps.setInt(5, c.getUser().getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Met à jour le contenu et l'état d'un commentaire existant.
     */
    public void update(Commentaire c) {
        String sql = "UPDATE commentaire SET content = ?, etat = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getContent());
            ps.setString(2, c.getEtat());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprime un commentaire par son ID.
     */
    public void deleteById(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recherche un commentaire par son ID.
     */
    public Commentaire findById(int id) {
        String sql = "SELECT content, created_at, etat, article_id, user_id FROM commentaire WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(id);
                    c.setContent(rs.getString("content"));
                    c.setEtat(rs.getString("etat"));
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    User u = new User();
                    u.setId(rs.getInt("user_id"));
                    c.setUser(u);

                    Article a = new Article();
                    a.setId(rs.getInt("article_id"));
                    c.setArticle(a);

                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère tous les commentaires liés à un article.
     */
    public List<Commentaire> findByArticleId(int articleId) {
        List<Commentaire> list = new ArrayList<>();
        String sql = """
            SELECT c.id, c.content, c.created_at, c.etat,
                   u.id AS uid, u.nom, u.prenom, u.email
              FROM commentaire c
              JOIN user u ON c.user_id = u.id
             WHERE c.article_id = ?
             ORDER BY c.created_at ASC
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
                    u.setEmail(rs.getString("email"));
                    c.setUser(u);

                    Article a = new Article();
                    a.setId(articleId);
                    c.setArticle(a);

                    list.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}