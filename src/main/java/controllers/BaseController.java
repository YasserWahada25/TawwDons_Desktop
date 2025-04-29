package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import utils.Router;

public class BaseController {

    @FXML protected Button dashboardBtn;
    @FXML protected Button donsRequestsBtn;
    @FXML protected MenuItem articleListMenuItem;
    @FXML protected MenuItem articleFormMenuItem;
    @FXML protected Button offreBtn;
    @FXML protected Button condidatBtn;
    @FXML protected Button evenementBtn;
    @FXML protected Button evaluationBtn;
    @FXML protected Button participantBtn;
    @FXML protected Button categorieBtn;
    @FXML protected Button notificationsBtn;
    @FXML protected Button profileBtn;

    @FXML
    public void initialize() {
        setupNavigation();
    }

    protected void setupNavigation() {
        // Dashboard
        if (dashboardBtn != null) {
            dashboardBtn.setOnAction(e -> Router.navigateTo("/Dashboard.fxml"));
        }

        // Dons Requests
        if (donsRequestsBtn != null) {
            donsRequestsBtn.setOnAction(e -> Router.navigateTo("/DonsRequests.fxml"));
        }

        // Article List
        if (articleListMenuItem != null) {
            articleListMenuItem.setOnAction(e -> Router.navigateTo("/article/ListArticle.fxml"));
        }

        // Article Form
        if (articleFormMenuItem != null) {
            articleFormMenuItem.setOnAction(e -> Router.navigateTo("/article/AddArticle.fxml"));
        }

        // Offre
        if (offreBtn != null) {
            offreBtn.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        }

        // Condidat
        if (condidatBtn != null) {
            condidatBtn.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        }

        // Evenement
        if (evenementBtn != null) {
            evenementBtn.setOnAction(e -> Router.navigateTo("/evenement/ListEvenement.fxml"));
        }

        // Evaluation
        if (evaluationBtn != null) {
            evaluationBtn.setOnAction(e -> Router.navigateTo("/evaluation/ListEvaluation.fxml"));
        }

        // Participant
        if (participantBtn != null) {
            participantBtn.setOnAction(e -> Router.navigateTo("/participant/ListParticipant.fxml"));
        }

        // Catégorie
        if (categorieBtn != null) {
            categorieBtn.setOnAction(e -> Router.navigateTo("/categorie/ListCategorie.fxml"));
        }

        // Notifications
        if (notificationsBtn != null) {
            notificationsBtn.setOnAction(e -> Router.navigateTo("/notifications/ListNotifications.fxml"));
        }

        // Profile
        if (profileBtn != null) {
            profileBtn.setOnAction(e -> Router.navigateTo("/profile/Profile.fxml"));
        }
    }
} 