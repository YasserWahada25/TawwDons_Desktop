package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import utils.Router;

public class NavbarController {

    @FXML private Button btnHome;
    @FXML private MenuItem miListeDons;
    @FXML private MenuItem miPosterDon;
    @FXML private MenuItem miArticleList;
    @FXML private MenuItem miListeOffres;
    @FXML private MenuItem miPosterOffre;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;

    @FXML
    public void initialize() {
        btnHome.setOnAction(e       -> Router.navigateTo("/Home.fxml"));
        miListeDons.setOnAction(e   -> Router.navigateTo("/ListDons.fxml"));
        miPosterDon.setOnAction(e   -> Router.navigateTo("/AddDons.fxml"));
        miArticleList.setOnAction(e -> Router.navigateTo("/article/articleList.fxml"));
        miListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffres.fxml"));
        miPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
        btnLogin.setOnAction(e      -> Router.navigateTo("/Login.fxml"));
        btnRegister.setOnAction(e   -> Router.navigateTo("/Register.fxml"));
    }
}
