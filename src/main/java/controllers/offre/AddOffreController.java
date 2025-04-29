// src/main/java/controller/offre/AddOffreController.java
package controllers.offre;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Offre;
import services.OffreService;
import utils.Router;
import java.util.List;

public class AddOffreController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private Button creerOffreBtn;
    @FXML private Button assistantBtn;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;
    @FXML private Button offreBtn;

    private final OffreService offreService = new OffreService();

    @FXML
    public void initialize() {
        setupNavigation();
        setupValidators();
    }

    private void setupNavigation() {
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
        creerOffreBtn.setOnAction(e -> creerOffre());
    }

    @FXML
    private void handleAssistantButtonClick() {
        Router.navigateTo("/ChatBot.fxml");
    }

    @FXML
    private void handleOffreButtonClick() {
        System.out.println("Bouton Offre cliqué - Redirection vers la liste des offres");
        Router.navigateTo("/offre/ListOffre_BC.fxml");
    }

    @FXML
    private void creerOffre() {
        String titre = titreField.getText().trim();
        String desc = descriptionArea.getText().trim();

        // Validation du titre
        if (titre.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le titre est obligatoire.");
            titreField.requestFocus();
            return;
        }

        if (titre.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le titre doit contenir au moins 3 caractères.");
            titreField.requestFocus();
            return;
        }

        // Validation de la description
        if (desc.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La description est obligatoire.");
            descriptionArea.requestFocus();
            return;
        }

        if (desc.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La description doit contenir au moins 10 caractères.");
            descriptionArea.requestFocus();
            return;
        }

        // Vérifier si une offre similaire existe déjà
        List<Offre> offres = offreService.getAllOffres();
        for (Offre offre : offres) {
            if (offre.getTitreOffre().equalsIgnoreCase(titre)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Une offre avec ce titre existe déjà.");
                titreField.requestFocus();
                return;
            }
        }

        Offre offre = new Offre();
        offre.setTitreOffre(titre);
        offre.setDescriptionOffre(desc);

        boolean ok = offreService.ajouterOffre(offre);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'offre a été créée avec succès.");
            Router.navigateTo("/offre/ListOffre_BC.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la création de l'offre.");
        }
    }

    private void setupValidators() {
        // Limiter la longueur du titre
        titreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 100) {
                titreField.setText(oldValue);
                showAlert(Alert.AlertType.WARNING, "Attention", "Le titre ne doit pas dépasser 100 caractères.");
            }
        });

        // Limiter la longueur de la description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1000) {
                descriptionArea.setText(oldValue);
                showAlert(Alert.AlertType.WARNING, "Attention", "La description ne doit pas dépasser 1000 caractères.");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

