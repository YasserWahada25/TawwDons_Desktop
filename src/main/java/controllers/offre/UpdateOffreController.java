package controllers.offre;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Offre;
import services.OffreService;
import utils.Router;
import java.util.List;

public class UpdateOffreController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private Button updateOffreBtn;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;

    private final OffreService offreService = new OffreService();

    private int offreId;

    @FXML
    public void initialize() {
        // Configuration de la navigation
        setupNavigation();

        // Ajouter des validateurs en temps réel
        setupValidators();

        // Récupérer l'ID passé dans l'URL et charger l'offre correspondante
        String url = Router.getCurrentUrl();
        this.offreId = extractIdFromUrl(url);

        System.out.println("URL: " + url);
        System.out.println("ID extrait: " + offreId);

        // Charger l'offre dans le formulaire
        if (offreId > 0) {
            Offre offre = offreService.getOffreById(offreId);
            if (offre != null) {
                titreField.setText(offre.getTitreOffre());
                descriptionArea.setText(offre.getDescriptionOffre());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Offre non trouvée.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "ID d'offre invalide.");
        }

        // Bouton de mise à jour
        updateOffreBtn.setOnAction(e -> updateOffre());
    }

    private void setupNavigation() {
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
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

    private int extractIdFromUrl(String url) {
        if (url == null || !url.contains("?")) {
            return -1;
        }

        // Extraire l'ID du paramètre "id" dans l'URL
        String[] parts = url.split("\\?");
        if (parts.length < 2) {
            return -1;
        }

        String[] params = parts[1].split("&");
        for (String param : params) {
            if (param.startsWith("id=")) {
                try {
                    return Integer.parseInt(param.substring(3));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private void updateOffre() {
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

        // Vérifier si une autre offre avec le même titre existe déjà
        List<Offre> offres = offreService.getAllOffres();
        for (Offre offre : offres) {
            if (offre.getTitreOffre().equalsIgnoreCase(titre) && offre.getId() != offreId) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Une autre offre avec ce titre existe déjà.");
                titreField.requestFocus();
                return;
            }
        }

        Offre offre = new Offre();
        offre.setId(offreId);
        offre.setTitreOffre(titre);
        offre.setDescriptionOffre(desc);

        boolean ok = offreService.updateOffre(offre);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre mise à jour avec succès !");
            // Retourner à la liste des offres
            Router.navigateTo("/offre/ListOffre_BC.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec lors de la mise à jour de l'offre.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
