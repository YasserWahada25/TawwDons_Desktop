package controllers.article;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;

public class ModifierArticleController {

    @FXML private TextField titreField;
    @FXML private TextField categorieField;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;

    private Article article;
    private final ArticleService articleService = new ArticleService();

    public void setArticle(Article article) {
        this.article = article;
        titreField.setText(article.getTitre());
        categorieField.setText(article.getCategorie());
        descriptionField.setText(article.getDescription());
    }

    @FXML
    private void handleUpdate() {
        String titre = titreField.getText();
        String categorie = categorieField.getText();
        String description = descriptionField.getText();

        if (titre == null || titre.trim().isEmpty()) {
            errorLabel.setText("❌ Le titre est obligatoire.");
            return;
        }
        if (titre.length() < 3 || titre.length() > 255) {
            errorLabel.setText("❌ Le titre doit contenir entre 3 et 255 caractères.");
            return;
        }
        if (!titre.matches("^[A-Za-z0-9\\s'.]+$")) {
            errorLabel.setText("❌ Le titre contient des caractères invalides.");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            errorLabel.setText("❌ La description est obligatoire.");
            return;
        }
        if (description.length() < 10) {
            errorLabel.setText("❌ La description doit contenir au moins 10 caractères.");
            return;
        }
        if (!description.matches("^[A-Za-z0-9\\s'.]+$")) {
            errorLabel.setText("❌ La description contient des caractères invalides.");
            return;
        }

        if (categorie == null || categorie.trim().isEmpty()) {
            errorLabel.setText("❌ La catégorie est obligatoire.");
            return;
        }
        if (!categorie.matches("^[A-Za-z0-9\\s'.]+$")) {
            errorLabel.setText("❌ Catégorie invalide.");
            return;
        }

        article.setTitre(titre);
        article.setCategorie(categorie);
        article.setDescription(description);
        articleService.update(article); // Méthode existante dans ton service
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }
}
