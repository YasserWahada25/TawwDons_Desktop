package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import models.User;
import utils.SessionManager;

public class MainInterfaceController {

    @FXML
    private MenuItem menuListeDons;

    @FXML
    private MenuItem menuPosterDon;

    @FXML
    private Button deconnexionButton;

    @FXML
    private Label userNameLabel;

    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        // Initialize any necessary event handlers or setup here
        menuListeDons.setOnAction(event -> handleListeDons());
        menuPosterDon.setOnAction(event -> handlePosterDon());
        deconnexionButton.setOnAction(event -> handleDeconnexion());

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
} 