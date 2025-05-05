package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Dons;
import services.DonsService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListDonsController {

    @FXML private FlowPane donsContainer;
    @FXML private StackPane popupPane;
    @FXML private VBox popupContent;
    @FXML private Label popupTitle, popupCategorie, popupDescription, popupDate, popupUser, popupEmail;
    @FXML private ImageView popupImage;

    private final DonsService donsService = new DonsService();

    @FXML
    public void initialize() {
        afficherDons();
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

        try {
            String imagePath = "/images/" + don.getImageUrl();
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            popupImage.setImage(img);
        } catch (Exception e) {
            popupImage.setImage(null);
        }

        popupPane.setVisible(true);
    }

    @FXML
    private void closePopup() {
        popupPane.setVisible(false);
    }
}
