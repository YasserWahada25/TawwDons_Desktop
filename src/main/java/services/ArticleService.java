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

    // Dossier relatif dans lequel les images seront stockées
    private final String imageDir = "images";

    public void create(Article article, File selectedImage) throws Exception {
        String filename = null;

        // Vérifie si une image est sélectionnée
        if (selectedImage != null) {
            String extension = selectedImage.getName().substring(selectedImage.getName().lastIndexOf("."));
            filename = System.currentTimeMillis() + extension;

            // Crée le dossier images s’il n’existe pas
            File destinationFolder = new File(imageDir);
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            File destFile = new File(destinationFolder, filename);
            Files.copy(selectedImage.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            article.setImage(filename);
        }

        // Enregistrement dans la base de données
        Connection cnx = MyDataBase.getInstance().getConnection();
        String sql = "INSERT INTO article (titre, description, categorie, image, nombre_commentaire, created_at) VALUES (?, ?, ?, ?, ?, ?)";
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
    public void create(Article t) {
        // Cette version ne fait rien : obligatoire à cause de l'interface
    }

    public void update(Article article) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String query = "UPDATE article SET titre = ?, categorie = ?, description = ? WHERE id = ?";
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
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM article");
            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setDescription(rs.getString("description"));
                a.setCategorie(rs.getString("categorie"));
                a.setImage(rs.getString("image"));
                a.setNombre_commentaire(rs.getInt("nombre_commentaire"));
                a.setCreated_at(rs.getDate("created_at"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
