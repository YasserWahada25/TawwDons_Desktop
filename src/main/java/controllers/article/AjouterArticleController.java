package controllers.article;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Article;
import services.ArticleService;
import utils.Router;

import java.io.File;
import java.util.Date;

public class AjouterArticleController {

    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField categorieField;
    @FXML private Button chooseFileButton;
    @FXML private Label fileNameLabel;
    @FXML private Label errorLabel;
    @FXML private Button ajouterBtn;
    @FXML private ImageView imagePreview;

    // 🧭 Routage via sidebar
    @FXML private MenuItem menuArticleForm;
    @FXML private MenuItem menuArticleList;
    @FXML private Button btnDonsRequests;
    @FXML private Button btnDashboard;

    private File selectedFile;
    private final ArticleService articleService = new ArticleService();

    @FXML
    public void initialize() {
        // Choix du fichier image
        chooseFileButton.setOnAction(event -> handleChooseFile());

        // Action bouton ajout
        ajouterBtn.setOnAction(event -> handleAjoutArticle());

        // ✅ Routage sidebar
        if (menuArticleForm != null)
            menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/ajouterArticle.fxml"));

        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));

        if (btnDonsRequests != null)
            btnDonsRequests.setOnAction(e -> Router.navigateTo("/Admin/RequestAddDons.fxml"));

        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> System.out.println("Dashboard (à venir)"));
    }

    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());

        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
            errorLabel.setText("");
        } else {
            fileNameLabel.setText("Aucun fichier choisi");
            imagePreview.setImage(null);
        }
    }

    private void handleAjoutArticle() {
        String titre = titreField.getText();
        String description = contenuField.getText();
        String categorie = categorieField.getText();

        if (titre.isEmpty() || description.isEmpty() || categorie.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        Article article = new Article();
        article.setTitre(titre);
        article.setDescription(description);
        article.setCategorie(categorie);
        article.setNombre_commentaire(0);
        article.setCreated_at(new Date());

        try {
            articleService.create(article, selectedFile);
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("✅ Article ajouté avec succès !");
            resetForm();
        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetForm() {
        titreField.clear();
        contenuField.clear();
        categorieField.clear();
        fileNameLabel.setText("Aucun fichier choisi");
        imagePreview.setImage(null);
        selectedFile = null;
    }
}
