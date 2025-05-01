package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import utils.Router;

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



    @FXML
    public void initialize() {
        System.out.println("✅ HomeController chargé");

        // Redirection vers la page Liste des Dons
        menuListeDons.setOnAction(event -> Router.navigateTo("/Dons/ListDons.fxml"));

        // Redirection vers la page Ajouter un Don
        menuPosterDon.setOnAction(event -> Router.navigateTo("/Dons/AddDons.fxml"));

        btnHome.setOnAction(event -> Router.navigateTo("/Home.fxml"));

        menuArticleList.setOnAction(event -> Router.navigateTo("/articleList.fxml"));

        menuOffreList.setOnAction(event -> Router.navigateTo("/offre/ListOffres.fxml"));





    }

}
