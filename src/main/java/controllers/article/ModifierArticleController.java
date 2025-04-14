package controllers.article;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;

public class ModifierArticleController {

    @FXML private TextField titreField;
    @FXML private TextField categorieField;
    @FXML private TextArea descriptionField;

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
        article.setTitre(titreField.getText());
        article.setCategorie(categorieField.getText());
        article.setDescription(descriptionField.getText());
        articleService.update(article); // à implémenter
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

