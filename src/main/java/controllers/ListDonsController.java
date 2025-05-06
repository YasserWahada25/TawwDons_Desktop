package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Dons;
import models.User;
import services.DonsService;
import services.UserService;
import utils.Router;
import utils.SessionManager;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ListDonsController {

    @FXML private FlowPane donsContainer;
    @FXML private StackPane popupPane;
    @FXML private VBox popupContent;
    @FXML private Label popupTitle, popupCategorie, popupDescription, popupDate, popupUser, popupEmail;
    @FXML private ImageView popupImage;

    @FXML private MenuItem menuListeDons;
    @FXML private MenuItem menuPosterDon;
    @FXML private MenuItem menuListeArticles;
    @FXML private Button btnHome;
    @FXML private Button btnDemanderDon;
    @FXML private Button btnVoirDemandes;
    @FXML private Button btngererDemandeRecu;
    @FXML private ComboBox<String> categorieFilter;
    @FXML private TextField searchField;


    private final DonsService donsService = new DonsService();
    // private final boolean isBeneficiaire = true;
    private boolean isBeneficiaire;
    private int currentUserId;


    @FXML
    public void initialize() {
        // 🔥 1. Récupérer l'utilisateur connecté
        var user = SessionManager.getCurrentUser();

        if (user != null) {
            currentUserId = user.getId();
            isBeneficiaire = user.getRoles().toUpperCase().contains("BENEFICIAIRE");

            System.out.println("🔐 Utilisateur connecté : " + user.getNom() + " (ID=" + user.getId() + ")");
            System.out.println("🎭 Rôle : " + user.getRoles());
            System.out.println("✅ isBeneficiaire = " + isBeneficiaire);
        } else {
            // Gérer le cas non connecté (optionnel)
            isBeneficiaire = true; // Par défaut
            currentUserId = 0;

            System.out.println("❌ Aucun utilisateur connecté. isBeneficiaire = true par défaut");
        }

        categorieFilter.getItems().addAll("Toutes les catégories", "vetements", "nourriture", "electronique", "meubles", "autre");
        categorieFilter.setValue("Toutes les catégories");

        categorieFilter.setOnAction(e -> afficherDons());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> afficherDons());

        // 🔥 2. Affichage dynamique des boutons
        btnVoirDemandes.setVisible(isBeneficiaire);
        btnVoirDemandes.setManaged(isBeneficiaire);
        btnVoirDemandes.setOnAction(e -> Router.navigateTo("/Dons/ListDemandePourBeneficiaire.fxml"));

        btngererDemandeRecu.setVisible(!isBeneficiaire);
        btngererDemandeRecu.setManaged(!isBeneficiaire);
        btngererDemandeRecu.setOnAction(e -> Router.navigateTo("/Dons/ListDemandePourDonneur.fxml"));


        afficherDons();
    }





    private void afficherDons() {
        donsContainer.getChildren().clear();
        List<Dons> donsList = donsService.getDonsValidés();

        String selectedCat = categorieFilter.getValue();
        String search = searchField.getText().toLowerCase().trim();

        if (!"Toutes les catégories".equals(selectedCat)) {
            donsList = donsList.stream()
                    .filter(d -> d.getCategorie().equalsIgnoreCase(selectedCat))
                    .collect(Collectors.toList());
        }

        if (!search.isEmpty()) {
            donsList = donsList.stream()
                    .filter(d -> d.getTitre().toLowerCase().contains(search))
                    .collect(Collectors.toList());
        }

        for (Dons don : donsList) {
            VBox card = new VBox(10);
            card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
            card.setPrefWidth(200);

            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setFitWidth(150);

            try {
                File imageFile = new File("src/main/resources/images/" + don.getImageUrl());
                if (imageFile.exists()) {
                    Image img = new Image(imageFile.toURI().toString());
                    imageView.setImage(img);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image : " + e.getMessage());
            }

            Label titre = new Label(don.getTitre());
            titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label categorie = new Label("Catégorie : " + don.getCategorie());
            Label description = new Label(don.getDescription());
            description.setWrapText(true);

            Button btnDetails = new Button("Voir détails");
            btnDetails.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
            btnDetails.setOnAction(e -> showPopup(don));

            card.getChildren().addAll(imageView, titre, categorie, description, btnDetails);
            donsContainer.getChildren().add(card);
        }
    }
    private void showPopup(Dons don) {
        popupTitle.setText(don.getTitre());
        popupCategorie.setText("Catégorie : " + don.getCategorie());
        popupDescription.setText("Description : " + don.getDescription());
        popupDate.setText("Date de Création : " + don.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        UserService userService = new UserService();
        User donneur = userService.findById(don.getDonneurId());
        if (donneur != null) {
            popupUser.setText("Ajouté par : " + donneur.getNom());
            popupEmail.setText("Email : " + donneur.getEmail());
        } else {
            popupUser.setText("Ajouté par : Inconnu");
            popupEmail.setText("Email : N/A");
        }

        try {
            File imageFile = new File("src/main/resources/images/" + don.getImageUrl());
            if (imageFile.exists()) {
                Image img = new Image(imageFile.toURI().toString());
                popupImage.setImage(img);
            } else {
                popupImage.setImage(null);
            }
        } catch (Exception e) {
            popupImage.setImage(null);
        }

        popupPane.setVisible(true);

        btnDemanderDon.setVisible(isBeneficiaire);
        btnDemanderDon.setManaged(isBeneficiaire);

        btnDemanderDon.setOnAction(e -> {
            if (donsService.existeDemandeEnAttente(don.getId(), currentUserId)) {
                showAlert("Demande existante", "Vous avez déjà une demande en attente ou acceptée pour ce don.");
            } else {
                if (donsService.ajouterDemandeDon(don.getId(), currentUserId)) {
                    showAlert("Succès", "Votre demande pour le don \"" + don.getTitre() + "\" a été envoyée avec succès.");
                } else {
                    showAlert("Erreur", "Erreur lors de l'envoi de votre demande de don.");
                }
            }
        });
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void closePopup() {
        popupPane.setVisible(false);
    }
}
