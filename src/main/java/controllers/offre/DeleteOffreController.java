package controllers.offre;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Offre;
import services.OffreService;
import utils.Router;

public class DeleteOffreController {

    @FXML private Label titreLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button confirmDeleteBtn;
    @FXML private Button cancelBtn;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;

    private final OffreService offreService = new OffreService();
    private int offreId;

    @FXML
    public void initialize() {
        // Configuration de la navigation
        setupNavigation();

        // Récupérer l'ID passé dans l'URL et charger l'offre correspondante
        String url = Router.getCurrentUrl();
        this.offreId = extractIdFromUrl(url);

        System.out.println("URL: " + url);
        System.out.println("ID extrait: " + offreId);

        // Charger l'offre dans le formulaire
        if (offreId > 0) {
            Offre offre = offreService.getOffreById(offreId);
            if (offre != null) {
                titreLabel.setText(offre.getTitreOffre());
                descriptionLabel.setText(offre.getDescriptionOffre());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Offre non trouvée.");
                Router.navigateTo("/offre/ListOffre_BC.fxml");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "ID d'offre invalide.");
            Router.navigateTo("/offre/ListOffre_BC.fxml");
        }

        // Configuration des boutons
        confirmDeleteBtn.setOnAction(e -> deleteOffre());
        cancelBtn.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
    }

    private void setupNavigation() {
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
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

    private void deleteOffre() {
        boolean deleted = offreService.deleteOffre(offreId);
        if (deleted) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'offre a été supprimée avec succès.");
            Router.navigateTo("/offre/ListOffre_BC.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'offre.");
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