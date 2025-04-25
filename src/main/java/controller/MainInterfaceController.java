package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import models.User;
import utils.Router;
import utils.SessionManager;

public class MainInterfaceController {

    @FXML
    private MenuItem menuListeDons;

    @FXML
    private MenuItem menuPosterDon;

    @FXML
    private Button deconnexionButton;

    @FXML
    private MenuItem menuListeArticles;
    @FXML
    private Button btnHome;

    @FXML
    private Label userNameLabel;
    @FXML
    private Button userBtn;

    private SessionManager sessionManager;

    public MainInterfaceController() {
        this.sessionManager = new SessionManager();
    }

    @FXML
    public void initialize() {
        // Initialize any necessary event handlers or setup here
        menuListeDons.setOnAction(event -> handleListeDons());
        menuPosterDon.setOnAction(event -> handlePosterDon());
        deconnexionButton.setOnAction(event -> handleDeconnexion());
        userBtn.setOnAction(this::goToUser);


        // Display the logged-in user's name
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        }

        // Initialization logic here
        System.out.println("MainInterfaceController initialized");
    }

    private void handleListeDons() {
        // Logic to handle the "Liste des dons" menu item
        System.out.println("Liste des dons clicked");
    }

    private void handlePosterDon() {
        // Logic to handle the "Poster un don" menu item
        System.out.println("Poster un don clicked");
    }

    private void handleDeconnexion() {
        // Logic to handle the "Déconnexion" button
        sessionManager.clearSession();
        System.out.println("Déconnexion clicked");
        // Navigate to the login screen or perform other actions
    }
    @FXML
    private void goToUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Utilisateur");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du chargement de l'interface Utilisateur.");
        }
    }
    private void setupNavigation() {
        if (menuListeDons != null)
            menuListeDons.setOnAction(e -> Router.navigateTo("/ListDons.fxml"));
        if (menuPosterDon != null)
            menuPosterDon.setOnAction(e -> Router.navigateTo("/AddDons.fxml"));
        if (menuListeArticles != null)
            menuListeArticles.setOnAction(e -> Router.navigateTo("/articleList.fxml"));
        if (btnHome != null)
            btnHome.setOnAction(e -> Router.navigateTo("/Home.fxml"));
    }
} 