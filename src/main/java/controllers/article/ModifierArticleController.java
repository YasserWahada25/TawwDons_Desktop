package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ModifierArticleController implements Initializable {

    @FXML private TextField    titreField;
    @FXML private ComboBox<String> categorieField;
    @FXML private TextArea     descriptionField;
    @FXML private Label        errorLabel;

    private Article article;
    private final ArticleService articleService = new ArticleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) Remplir la combo avec vos catégories
        categorieField.getItems().setAll(
                "dons", "evenement", "recrutement"
        );
    }

    // Appelé par l'écran parent avant d'afficher la fenêtre
    public void setArticle(Article article) {
        this.article = article;

        // 2) Initialiser les champs
        titreField.setText(article.getTitre());
        descriptionField.setText(article.getDescription());
        categorieField.setValue(article.getCategorie());
    }

    @FXML
    private void handleUpdate() {
        String titre       = titreField.getText();
        String description = descriptionField.getText();
        String categorie   = categorieField.getValue();

        // 3) Validation (idem)
        if (titre == null || titre.trim().isEmpty()) {
            errorLabel.setText("❌ Le titre est obligatoire."); return;
        }
        if (titre.length() < 3 || titre.length() > 255) {
            errorLabel.setText("❌ Le titre doit contenir entre 3 et 255 caractères."); return;
        }
        if (!titre.matches("^[A-Za-z0-9\\s'.]+$")) {
            errorLabel.setText("❌ Caractères invalides dans le titre."); return;
        }

        if (description == null || description.trim().isEmpty()) {
            errorLabel.setText("❌ La description est obligatoire."); return;
        }
        if (description.length() < 10) {
            errorLabel.setText("❌ La description doit faire au moins 10 caractères."); return;
        }

        if (categorie == null || categorie.trim().isEmpty()) {
            errorLabel.setText("❌ La catégorie est obligatoire."); return;
        }
        if (!Arrays.asList("dons","evenement","recrutement")
                .contains(categorie.toLowerCase())) {
            errorLabel.setText("❌ Catégorie invalide."); return;
        }

        // 4) Mise à jour de l'objet et en base
        article.setTitre(titre.trim());
        article.setDescription(description.trim());
        article.setCategorie(categorie);
        articleService.update(article);

        // 5) Fermer la fenêtre
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
