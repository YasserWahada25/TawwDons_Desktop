package controllers.dons;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Dons;
import services.DonsService;
import utils.Router;
import utils.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

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

        // 🔥 2. Affichage dynamique des boutons
        btnVoirDemandes.setVisible(isBeneficiaire);
        btnVoirDemandes.setManaged(isBeneficiaire);
        btnVoirDemandes.setOnAction(e -> Router.navigateTo("/Dons/ListDemandePourBeneficiaire.fxml"));

        btngererDemandeRecu.setVisible(!isBeneficiaire);
        btngererDemandeRecu.setManaged(!isBeneficiaire);
        btngererDemandeRecu.setOnAction(e -> Router.navigateTo("/Dons/ListDemandePourDonneur.fxml"));

        setupNavigation();
        afficherDons();
    }



    private void setupNavigation() {
        menuListeDons.setOnAction(e -> Router.navigateTo("/Dons/ListDons.fxml"));
        menuPosterDon.setOnAction(e -> Router.navigateTo("/Dons/AddDons.fxml"));
        menuListeArticles.setOnAction(e -> Router.navigateTo("/articleList.fxml"));
        btnHome.setOnAction(e -> Router.navigateTo("/Home.fxml"));
        btnVoirDemandes.setOnAction(e -> Router.navigateTo("/Dons/ListDemandePourBeneficiaire.fxml"));
        btngererDemandeRecu.setOnAction(e -> Router.navigateTo("/Dons/ListDemandePourDonneur.fxml"));

    }

    private void afficherDons() {
        donsContainer.getChildren().clear();
        List<Dons> donsList = donsService.getDonsValidés();

        for (Dons don : donsList) {
            VBox card = new VBox(10);
            card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
            card.setPrefWidth(200);

            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setFitWidth(150);

            try {
                String imagePath = "/images/" + don.getImageUrl();
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                imageView.setImage(img);
            } catch (Exception e) {
                System.err.println("Image non trouvée pour : " + don.getImageUrl());
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
        popupUser.setText("Ajouté par : user N/A");
        popupEmail.setText("Email : email@example.com");
        //   popupUser.setText("Ajouté par : " + don.getNomDonneur());
        // popupEmail.setText("Email : " + don.getEmailDonneur());


        try {
            String imagePath = "/images/" + don.getImageUrl();
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            popupImage.setImage(img);
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
