package controllers.offre;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Offre;
import services.OffreService;
import utils.Router;

import java.util.List;
import java.util.stream.Collectors;

public class ListOffresController {
    @FXML private FlowPane offresContainer;
    @FXML private StackPane popupPane;
    @FXML private VBox popupContent;
    @FXML private Label popupTitle;
    @FXML private Label popupDescription;
    @FXML private TextField searchField;

    // Navigation elements
    @FXML private Button btnHome;
    @FXML private MenuItem menuListeDons;
    @FXML private MenuItem menuPosterDon;
    @FXML private MenuItem menuListeEvaluations;
    @FXML private MenuItem menuArticleList;
    @FXML private MenuItem menuOffreList;
    @FXML private MenuItem menuPosterOffre;
    @FXML private MenuItem menuListeEvenements;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;

    private final OffreService offreService = new OffreService();
    private List<Offre> allOffres;

    @FXML
    public void initialize() {
        setupNavigation();
        setupSearch();
        afficherOffres();
    }

    @FXML
    private void navigateToHome() {
        Router.navigateTo("/Home.fxml");
    }

    @FXML
    private void navigateToLogin() {
        Router.navigateTo("/Login.fxml");
    }

    @FXML
    private void navigateToRegister() {
        Router.navigateTo("/Register.fxml");
    }

    private void setupNavigation() {
        if (btnHome != null)
            btnHome.setOnAction(e -> navigateToHome());

        if (menuListeDons != null)
            menuListeDons.setOnAction(e -> Router.navigateTo("/ListDons.fxml"));

        if (menuPosterDon != null)
            menuPosterDon.setOnAction(e -> Router.navigateTo("/AddDons.fxml"));

        if (menuListeEvaluations != null)
            menuListeEvaluations.setOnAction(e -> Router.navigateTo("/evaluation/ListEvaluation.fxml"));

        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/articleList.fxml"));

        if (menuOffreList != null)
            menuOffreList.setOnAction(e -> Router.navigateTo("/offre/ListOffres.fxml"));

        if (menuPosterOffre != null)
            menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));

        if (menuListeEvenements != null)
            menuListeEvenements.setOnAction(e -> Router.navigateTo("/evenement/ListEvenement.fxml"));

        if (btnLogin != null)
            btnLogin.setOnAction(e -> navigateToLogin());

        if (btnRegister != null)
            btnRegister.setOnAction(e -> navigateToRegister());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterOffres(newValue);
        });
    }

    private void filterOffres(String searchText) {
        if (allOffres == null) return;

        List<Offre> filteredOffres = allOffres.stream()
                .filter(offre ->
                        offre.getTitreOffre().toLowerCase().contains(searchText.toLowerCase()) ||
                                offre.getDescriptionOffre().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        afficherOffres(filteredOffres);
    }

    private void afficherOffres() {
        allOffres = offreService.getAllOffres();
        afficherOffres(allOffres);
    }

    private void afficherOffres(List<Offre> offres) {
        offresContainer.getChildren().clear();

        for (Offre o : offres) {
            VBox card = new VBox(10);
            card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
            card.setPrefWidth(200);

            Label titre = new Label(o.getTitreOffre());
            titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label desc = new Label(o.getDescriptionOffre());
            desc.setWrapText(true);

            HBox buttonBox = new HBox(10);
            Button btnDetails = new Button("Voir détails");
            btnDetails.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
            btnDetails.setOnAction(e -> showPopup(o));

            Button btnPostuler = new Button("Postuler");
            btnPostuler.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            btnPostuler.setOnAction(e -> postulerOffre(o));

            buttonBox.getChildren().addAll(btnDetails, btnPostuler);

            card.getChildren().addAll(titre, desc, buttonBox);
            offresContainer.getChildren().add(card);
        }
    }

    private void postulerOffre(Offre offre) {
        Router.navigateTo("/condidat/AddCondidat_fr.fxml?id=" + offre.getId());
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
