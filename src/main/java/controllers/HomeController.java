package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import models.User;
import utils.Router;
import utils.SessionManager;

public class HomeController {

    @FXML
    private MenuItem menuListeDons;

    @FXML
    private MenuItem menuPosterDon;

    @FXML
    private Button btnHome;

    @FXML
    private MenuItem menuArticleList;

    @FXML
    private MenuItem menuOffreList;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        System.out.println("👤 Utilisateur en session : " + user.getPrenom() + " " + user.getNom());
    }

    @FXML
    public void initialize() {
        System.out.println("✅ HomeController chargé");

        menuListeDons.setOnAction(event -> Router.navigateTo("/ListDons.fxml"));
        menuPosterDon.setOnAction(event -> Router.navigateTo("/AddDons.fxml"));
        btnHome.setOnAction(event -> Router.navigateTo("/Home.fxml"));
        menuArticleList.setOnAction(event -> Router.navigateTo("/article/articleList.fxml"));
        menuOffreList.setOnAction(event -> Router.navigateTo("/offre/ListOffres.fxml"));
    }
}
