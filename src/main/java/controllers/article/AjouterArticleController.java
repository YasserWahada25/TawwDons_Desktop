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
    @FXML private ComboBox<String> categorieField;
    @FXML private Button chooseFileButton;
    @FXML private Label fileNameLabel;
    @FXML private Label errorLabel;
    @FXML private Button ajouterBtn;
    @FXML private ImageView imagePreview;

    private File selectedFile;
    private final ArticleService articleService = new ArticleService();

    @FXML
    public void initialize() {
        // Préparation des catégories
        categorieField.getItems().setAll("dons", "evenement", "recrutement");
        categorieField.setPromptText("Sélectionner une catégorie");

        // Choix de fichier
        chooseFileButton.setOnAction(event -> handleChooseFile());

        // Ajout article
        ajouterBtn.setOnAction(event -> handleAjoutArticle());
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
        errorLabel.setStyle("-fx-text-fill: red;");

        String titre = titreField.getText().trim();
        String description = contenuField.getText().trim();
        String categorie = categorieField.getValue();

        // Validations
        if (titre.isEmpty()) {
            errorLabel.setText("❌ Le titre est obligatoire.");
            return;
        }
        if (titre.length() < 3 || titre.length() > 255) {
            errorLabel.setText("❌ Le titre doit contenir entre 3 et 255 caractères.");
            return;
        }
        if (!titre.matches("^[A-Za-z0-9\\s'.-]+$")) {
            errorLabel.setText("❌ Le titre contient des caractères invalides.");
            return;
        }

        if (description.isEmpty()) {
            errorLabel.setText("❌ La description est obligatoire.");
            return;
        }
        if (description.length() < 10) {
            errorLabel.setText("❌ La description doit contenir au moins 10 caractères.");
            return;
        }
        if (!description.matches("^[A-Za-z0-9\\s'.-]+$")) {
            errorLabel.setText("❌ La description contient des caractères invalides.");
            return;
        }

        if (categorie == null || categorie.trim().isEmpty()) {
            errorLabel.setText("❌ La catégorie est obligatoire.");
            return;
        }

        if (selectedFile == null) {
            errorLabel.setText("❌ Vous devez choisir une image.");
            return;
        }

        String fileName = selectedFile.getName().toLowerCase();
        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
            errorLabel.setText("❌ Format d’image invalide (seuls JPG/PNG sont acceptés).");
            return;
        }

        // Création de l'article
        Article article = new Article();
        article.setTitre(titre);
        article.setDescription(description);
        article.setCategorie(categorie);
        article.setNombre_commentaire(0);
        article.setCreated_at(new Date());

        try {
            // Appel du service pour enregistrer en DB + uploader l’image
            articleService.create(article, selectedFile);

            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("✅ Article ajouté avec succès !");
            resetForm();

        } catch (Exception e) {
            errorLabel.setText("Erreur lors de l’ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetForm() {
        titreField.clear();
        contenuField.clear();
        categorieField.getSelectionModel().clearSelection();
        fileNameLabel.setText("Aucun fichier choisi");
        imagePreview.setImage(null);
        selectedFile = null;
    }
}
