package controller.offre;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Offre;
import service.OffreService;
import utils.Router;

import java.util.List;

public class ListOffresController {

    @FXML private FlowPane offresContainer;
    @FXML private StackPane popupPane;
    @FXML private VBox popupContent;
    @FXML private Label popupTitle;
    @FXML private Label popupDescription;

    @FXML private MenuItem menuListeDons;
    @FXML private MenuItem menuPosterDon;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;

    private final OffreService offreService = new OffreService();

    @FXML
    public void initialize() {
        setupNavigation();
        afficherOffres();
    }

    private void setupNavigation() {
        menuListeDons.setOnAction(e -> Router.navigateTo("/ListDons.fxml"));
        menuPosterDon.setOnAction(e -> Router.navigateTo("/AddDons.fxml"));
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffres.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
    }

    private void afficherOffres() {
        offresContainer.getChildren().clear();
        List<Offre> offres = offreService.getAllOffres();

        for (Offre o : offres) {
            VBox card = new VBox(10);
            card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
            card.setPrefWidth(200);

            Label titre = new Label(o.getTitreOffre());
            titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label desc = new Label(o.getDescriptionOffre());
            desc.setWrapText(true);

            Button btnDetails = new Button("Voir détails");
            btnDetails.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
            btnDetails.setOnAction(e -> showPopup(o));

            card.getChildren().addAll(titre, desc, btnDetails);
            offresContainer.getChildren().add(card);
        }
    }

    private void showPopup(Offre o) {
        popupTitle.setText(o.getTitreOffre());
        popupDescription.setText("Description : " + o.getDescriptionOffre());
        popupPane.setVisible(true);
    }

    @FXML
    private void closePopup() {
        popupPane.setVisible(false);
    }
}
