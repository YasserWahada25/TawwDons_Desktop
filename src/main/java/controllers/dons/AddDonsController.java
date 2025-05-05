package controllers.dons;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Dons;
import models.User;
import services.DonsService;
import utils.Router;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AddDonsController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private Label imageLabel;
    @FXML private Button choisirImageBtn;
    @FXML private Button creerDonBtn;
    @FXML private MenuItem menuListeDons;
    @FXML private MenuItem menuPosterDon;
    @FXML private MenuItem menuListeArticles;
    @FXML private Button btnHome;

    @FXML private File imageFile;
    @FXML private final DonsService donsService = new DonsService();

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null || !currentUser.getRoles().contains("DONNEUR")) {
            showAlert("Accès refusé", "Vous devez être connecté en tant que donneur.");
            Router.navigateTo("/Home.fxml");
            return;
        }

        categorieCombo.getItems().addAll("vetements", "nourriture", "electronique", "meubles", "autre");

        choisirImageBtn.setOnAction(e -> choisirImage());
        creerDonBtn.setOnAction(e -> creerDon());

        // Routage via Router
        menuListeDons.setOnAction(e -> Router.navigateTo("/Dons/ListDons.fxml"));
        menuPosterDon.setOnAction(e -> Router.navigateTo("/Dons/AddDons.fxml"));
        menuListeArticles.setOnAction(e -> Router.navigateTo("/articleList.fxml"));
        btnHome.setOnAction(e -> Router.navigateTo("/Home.fxml"));
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            try {
                String destinationDir = "src/main/resources/images";
                File dir = new File(destinationDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File dest = new File(dir, fileName);
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imageFile = dest;
                imageLabel.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            imageLabel.setText("Aucun fichier choisi");
        }
    }

    @FXML
    private void creerDon() {
        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté.");
            return;
        }

        Dons don = new Dons();
        don.setTitre(titreField.getText());
        don.setDescription(descriptionArea.getText());
        don.setDateCreation(datePicker.getValue());
        don.setCategorie(categorieCombo.getValue());
        don.setImageUrl(imageFile != null ? imageFile.getName() : "");
        don.setDonneurId(currentUser.getId());
        don.setValide(false);

        String erreur = don.validate();

        if (erreur != null) {
            showAlert("Erreur de validation", erreur);
            return;
        }

        if (donsService.ajouterDon(don)) {
            showAlert("Succès", "Don ajouté avec succès !");
            clearForm();
        } else {
            showAlert("Erreur", "Échec lors de l'ajout du don.");
        }
    }

    @FXML
    private void clearForm() {
        titreField.clear();
        descriptionArea.clear();
        datePicker.setValue(null);
        categorieCombo.setValue(null);
        imageLabel.setText("Aucun fichier choisi");
        imageFile = null;
    }

    @FXML
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
