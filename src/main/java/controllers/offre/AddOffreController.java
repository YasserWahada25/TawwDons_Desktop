package controllers.offre;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import models.Offre;
import services.OffreService;
import utils.Router;

import java.util.List;

public class AddOffreController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private Button creerOffreBtn;
    @FXML private Button offreBtn;  // bouton de la sidebar

    private final OffreService offreService = new OffreService();

    @FXML
    public void initialize() {
        System.out.println("✅ AddOffreController chargé");

        // Navigation via le bouton “Offre” de la sidebar
        offreBtn.setOnAction(e ->
                Router.navigateTo("/offre/ListOffres.fxml")
        );

        // Validateurs temps réel
        setupValidators();

        // Création de l’offre
        creerOffreBtn.setOnAction(e ->
                creerOffre()
        );
    }

    private void setupValidators() {
        // Limite du titre à 100 caractères
        titreField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 100) {
                titreField.setText(oldV);
                showAlert(Alert.AlertType.WARNING,
                        "Attention",
                        "Le titre ne doit pas dépasser 100 caractères.");
            }
        });

        // Limite de la description à 1000 caractères
        descriptionArea.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 1000) {
                descriptionArea.setText(oldV);
                showAlert(Alert.AlertType.WARNING,
                        "Attention",
                        "La description ne doit pas dépasser 1000 caractères.");
            }
        });
    }

    private void creerOffre() {
        String titre = titreField.getText().trim();
        String desc   = descriptionArea.getText().trim();

        // Validation basique
        if (titre.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "Le titre est obligatoire.");
            titreField.requestFocus();
            return;
        }
        if (titre.length() < 3) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "Le titre doit contenir au moins 3 caractères.");
            titreField.requestFocus();
            return;
        }
        if (desc.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "La description est obligatoire.");
            descriptionArea.requestFocus();
            return;
        }
        if (desc.length() < 10) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "La description doit contenir au moins 10 caractères.");
            descriptionArea.requestFocus();
            return;
        }

        // Vérifier unicité du titre
        List<Offre> offres = offreService.getAllOffres();
        for (Offre o : offres) {
            if (o.getTitreOffre().equalsIgnoreCase(titre)) {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur de validation",
                        "Une offre avec ce titre existe déjà.");
                titreField.requestFocus();
                return;
            }
        }

        // Création et persistance
        Offre offre = new Offre();
        offre.setTitreOffre(titre);
        offre.setDescriptionOffre(desc);

        boolean ok = offreService.ajouterOffre(offre);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Offre ajoutée avec succès !");
            clearForm();
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Échec lors de l'ajout de l'offre.");
        }
    }

    private void clearForm() {
        titreField.clear();
        descriptionArea.clear();
        titreField.requestFocus();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
