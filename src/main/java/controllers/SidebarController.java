package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import utils.Router;
import utils.SessionManager;

public class SidebarController {
    @FXML private Button btnDashboard;
    @FXML private Button btnDonsRequests;
    @FXML private MenuItem menuArticleList;
    @FXML private MenuItem menuArticleForm;
    @FXML private Button btnOffre;
    @FXML private Button btnCondidat;
    @FXML private Button btnEvenement;
    @FXML private Button btnEvaluation;
    @FXML private Button btnParticipant;
    @FXML private Button btnCategorie;
    @FXML private Button btnNotifications;
    @FXML private Button btnProfile;
    @FXML private Button btnUserName;
    @FXML private Button btnStatistics;
    @FXML private Button logoutButton;
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        logoutButton.setOnAction(e -> {
            sessionManager.logout();
            Router.navigateTo("/login.fxml");
        });
        btnUserName.setOnAction(e -> Router.navigateTo("/Admin/UserList.fxml"));
        btnDashboard.setOnAction(e -> Router.navigateTo("/views/Admin.fxml"));
        btnDonsRequests.setOnAction(e -> Router.navigateTo("/views/DonsRequests.fxml"));
        menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));
        menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/AjouterArticle.fxml"));
        btnOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
        btnCondidat.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        btnEvenement.setOnAction(e -> Router.navigateTo("/org/example/event/add-event.fxml"));
        btnEvaluation.setOnAction(e -> Router.navigateTo("/views/Dashboard.fxml"));
        btnParticipant.setOnAction(e -> Router.navigateTo("/org/example/event/view-participant.fxml"));
        btnCategorie.setOnAction(e -> Router.navigateTo("/views/Categorie.fxml"));
        btnNotifications.setOnAction(e -> Router.navigateTo("/views/Notifications.fxml"));
        btnProfile.setOnAction(e -> Router.navigateTo("/views/Profile.fxml"));
        btnStatistics.setOnAction(e -> Router.navigateTo("/Admin/Statistics.fxml"));
    }
}
