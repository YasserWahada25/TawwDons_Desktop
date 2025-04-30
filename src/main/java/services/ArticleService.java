package services;

import interfaces.CrudArticle;
import models.Article;
import utils.MyDataBase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService implements CrudArticle<Article> {

    private final String imageDir = "images";

    public void create(Article article, File selectedImage) throws Exception {
        String filename = null;

        if (selectedImage != null) {
            String extension = selectedImage.getName()
                    .substring(selectedImage.getName().lastIndexOf("."));
            filename = System.currentTimeMillis() + extension;

            File destinationFolder = new File(imageDir);
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            File destFile = new File(destinationFolder, filename);
            Files.copy(selectedImage.toPath(),
                    destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            article.setImage(filename);
        }

        Connection cnx = MyDataBase.getInstance().getConnection();
        String sql = """
            INSERT INTO article
              (titre, description, categorie, image, nombre_commentaire, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, article.getTitre());
        ps.setString(2, article.getDescription());
        ps.setString(3, article.getCategorie());
        ps.setString(4, article.getImage());
        ps.setInt(5, article.getNombre_commentaire());
        ps.setDate(6, new java.sql.Date(article.getCreated_at().getTime()));
        ps.executeUpdate();
    }

    @Override
    public void create(Article t) { /* non utilisé */ }

    public void update(Article article) {
        try (Connection conn = MyDataBase.getInstance().getConnection()) {
            String query = """
                UPDATE article
                SET titre = ?, categorie = ?, description = ?
                WHERE id = ?
                """;
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, article.getTitre());
            ps.setString(2, article.getCategorie());
            ps.setString(3, article.getDescription());
            ps.setInt(4, article.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM article WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Article supprimé avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Article> getAll() {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM article";
        try (Connection cnx = MyDataBase.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setDescription(rs.getString("description"));
                a.setCategorie(rs.getString("categorie"));
                a.setImage(rs.getString("image"));
                a.setNombre_commentaire(rs.getInt("nombre_commentaire"));
                a.setCreated_at(rs.getDate("created_at"));
                // 📈 Nouvelles colonnes :
                a.setLikes(rs.getInt("likes"));
                a.setDislikes(rs.getInt("dislikes"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Incrémente simplement le compteur de likes. */
    public void incrementLikes(int articleId) {
        String sql = "UPDATE article SET likes = likes + 1 WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Incrémente simplement le compteur de dislikes. */
    public void incrementDislikes(int articleId) {
        String sql = "UPDATE article SET dislikes = dislikes + 1 WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateLikes(int articleId, int newLikes) {
        String sql = "UPDATE article SET likes = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newLikes);
            ps.setInt(2, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateDislikes(int articleId, int newDislikes) {
        String sql = "UPDATE article SET dislikes = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newDislikes);
            ps.setInt(2, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void decrementLikes(int articleId) {
        String sql = "UPDATE article SET likes = GREATEST(likes - 1, 0) WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Décrémente le compteur de dislikes d’un article.
     * @param articleId L’ID de l’article
     */
    public void decrementDislikes(int articleId) {
        String sql = "UPDATE article SET dislikes = GREATEST(dislikes - 1, 0) WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
