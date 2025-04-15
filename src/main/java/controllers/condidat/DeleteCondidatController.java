package controllers.condidat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Condidat;
import models.Offre;
import services.CondidatService;
import services.OffreService;
import utils.Router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteCondidatController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label offreLabel;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;
    @FXML private MenuItem menuListeCondidats;
    @FXML private MenuItem menuAjouterCondidat;
    @FXML private Button offreBtn;
    @FXML private Button condidatBtn;

    private final CondidatService condidatService = new CondidatService();
    private final OffreService offreService = new OffreService();
    private Map<Integer, String> offreTitres = new HashMap<>();
    private Condidat currentCondidat;

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur de suppression");
        
        // Configuration de la navigation
        setupNavigation();
        
        // Charger les titres des offres
        loadOffreTitres();
        
        // Charger les données du candidat
        loadCondidatData();
    }

    private void setupNavigation() {
        menuListeCondidats.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        menuAjouterCondidat.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
        cancelButton.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
    }
    
    private void loadOffreTitres() {
        System.out.println("Chargement des titres des offres");
        List<Offre> offres = offreService.getAllOffres();
        System.out.println("Nombre d'offres chargées : " + offres.size());
        
        for (Offre offre : offres) {
            offreTitres.put(offre.getId(), offre.getTitreOffre());
            System.out.println("Offre chargée - ID: " + offre.getId() + ", Titre: " + offre.getTitreOffre());
        }
    }
    
    private void loadCondidatData() {
        try {
            // Récupérer l'ID du candidat depuis l'URL
            String url = Router.getCurrentUrl();
            System.out.println("URL actuelle : " + url);
            
            if (!url.contains("=")) {
                throw new IllegalArgumentException("ID du candidat non trouvé dans l'URL");
            }
            
            String idParam = url.substring(url.indexOf("=") + 1);
            int condidatId = Integer.parseInt(idParam);
            System.out.println("ID du candidat à supprimer : " + condidatId);
            
            // Charger les données du candidat
            currentCondidat = condidatService.getCondidatById(condidatId);
            if (currentCondidat != null) {
                System.out.println("Données du candidat chargées :");
                System.out.println("Nom : " + currentCondidat.getNom());
                System.out.println("Prénom : " + currentCondidat.getPrenom());
                System.out.println("Email : " + currentCondidat.getEmail());
                System.out.println("Téléphone : " + currentCondidat.getTelephone());
                System.out.println("Offre ID : " + currentCondidat.getOffreId());
                
                // Afficher les informations du candidat
                nomLabel.setText(currentCondidat.getNom());
                prenomLabel.setText(currentCondidat.getPrenom());
                emailLabel.setText(currentCondidat.getEmail());
                telephoneLabel.setText(String.valueOf(currentCondidat.getTelephone()));
                
                // Afficher le titre de l'offre
                if (currentCondidat.getOffreId() != null && offreTitres.containsKey(currentCondidat.getOffreId())) {
                    String titreOffre = offreTitres.get(currentCondidat.getOffreId());
                    offreLabel.setText(titreOffre);
                    System.out.println("Titre de l'offre : " + titreOffre);
                } else {
                    offreLabel.setText("N/A");
                    System.out.println("Aucune offre associée");
                }
            } else {
                System.err.println("Candidat non trouvé avec l'ID : " + condidatId);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Candidat non trouvé.");
                Router.navigateTo("/condidat/ListCondidat_BC.fxml");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données du candidat : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Une erreur est survenue lors du chargement des données du candidat.");
            Router.navigateTo("/condidat/ListCondidat_BC.fxml");
        }
    }

    @FXML
    private void handleDeleteButtonClick() {
        if (currentCondidat != null) {
            System.out.println("Tentative de suppression du candidat : " + currentCondidat.getId());
            
            // Demander confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce candidat ?");
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                // Supprimer le candidat
                if (condidatService.deleteCondidat(currentCondidat.getId())) {
                    System.out.println("Candidat supprimé avec succès");
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                             "Le candidat a été supprimé avec succès.");
                    Router.navigateTo("/condidat/ListCondidat_BC.fxml");
                } else {
                    System.err.println("Échec de la suppression du candidat");
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                             "Une erreur est survenue lors de la suppression du candidat.");
                }
            } else {
                System.out.println("Suppression annulée par l'utilisateur");
            }
        } else {
            System.err.println("Aucun candidat sélectionné pour la suppression");
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Aucun candidat sélectionné pour la suppression.");
        }
    }

    @FXML
    private void handleOffreButtonClick() {
        Router.navigateTo("/offre/ListOffre_BC.fxml");
    }
    
    @FXML
    private void handleCondidatButtonClick() {
        Router.navigateTo("/condidat/ListCondidat_BC.fxml");
    }

    @FXML
    private void handleCancelButtonClick() {
        Router.navigateTo("/condidat/ListCondidat_BC.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 