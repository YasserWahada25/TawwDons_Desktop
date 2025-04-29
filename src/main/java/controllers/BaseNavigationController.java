package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import utils.Router;

public class BaseNavigationController {
    @FXML protected Button btnDashboard;
    @FXML protected Button btnDonsRequests;
    @FXML protected MenuItem menuArticleList;
    @FXML protected MenuItem menuArticleForm;
    @FXML protected Button btnOffre;
    @FXML protected Button btnCondidat;
    @FXML protected Button btnEvenement;
    @FXML protected Button btnEvaluation;
    @FXML protected Button btnParticipant;
    @FXML protected Button btnCategorie;
    @FXML protected Button btnNotifications;
    @FXML protected Button btnProfile;

    @FXML
    public void initialize() {
        setupNavigation();
    }

    protected void setupNavigation() {
        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> Router.navigateTo("/Admin/Dashboard.fxml"));
        
        if (btnDonsRequests != null)
            btnDonsRequests.setOnAction(e -> Router.navigateTo("/Admin/RequestAddDons.fxml"));
        
        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));
        
        if (menuArticleForm != null)
            menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/ajouterArticle.fxml"));
        
        if (btnOffre != null)
            btnOffre.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        
        if (btnCondidat != null)
            btnCondidat.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        
        if (btnEvenement != null)
            btnEvenement.setOnAction(e -> Router.navigateTo("/evenement/ListEvenement.fxml"));
        
        if (btnEvaluation != null)
            btnEvaluation.setOnAction(e -> Router.navigateTo("/evaluation/ListEvaluation.fxml"));
        
        if (btnParticipant != null)
            btnParticipant.setOnAction(e -> Router.navigateTo("/participant/ListParticipant.fxml"));
        
        if (btnCategorie != null)
            btnCategorie.setOnAction(e -> Router.navigateTo("/categorie/ListCategorie.fxml"));
        
        if (btnNotifications != null)
            btnNotifications.setOnAction(e -> Router.navigateTo("/notifications/ListNotifications.fxml"));
        
        if (btnProfile != null)
            btnProfile.setOnAction(e -> Router.navigateTo("/profile/Profile.fxml"));
    }
} 