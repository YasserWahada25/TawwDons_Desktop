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

    // 🧭 Routage via sidebar
    @FXML private MenuItem menuArticleForm;
    @FXML private MenuItem menuArticleList;
    @FXML private Button btnDonsRequests;
    @FXML private Button btnDashboard;

    private File selectedFile;
    private final ArticleService articleService = new ArticleService();

    @FXML
    public void initialize() {
        categorieField.getItems().clear();
        categorieField.getItems().addAll("dons", "evenement", "recrutement");
        categorieField.setPromptText("Sélectionner une catégorie");
        chooseFileButton.setOnAction(event -> handleChooseFile());
        ajouterBtn.setOnAction(event -> handleAjoutArticle());

        // Routage sidebar
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
        String categorie = categorieField.getValue();

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

        if (selectedFile == null) {
            errorLabel.setText("❌ Vous devez choisir une image.");
            return;
        }

        String fileName = selectedFile.getName().toLowerCase();
        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
            errorLabel.setText("❌ Format d’image invalide (seuls JPG/PNG sont acceptés).");
            return;
        }

        // ✅ Tous les champs sont valides
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
        categorieField.getSelectionModel().clearSelection();
        fileNameLabel.setText("Aucun fichier choisi");
        imagePreview.setImage(null);
        selectedFile = null;
    }
}
