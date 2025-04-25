package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import services.UserService;
import java.io.IOException;
import models.User;
import utils.MyDataBase;
import utils.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    private void handleLoginButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Validate email and password
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showAlert("Erreur", "Email invalide", "Veuillez entrer une adresse email valide.");
            return;
        }

        // Authenticate user
        User user = userService.authenticate(email, password);
        if (user != null) {
            System.out.println("Login successful");
            // Store user in session
            sessionManager.setCurrentUser(user);
            
            // Navigate to the home interface
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
                Parent root = loader.load();
                
                // Get the controller and update UI
                HomeController homeController = loader.getController();
                homeController.updateUI(user);
                
                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec du chargement", "Impossible de charger la page d'accueil.");
            }
        } else {
            showAlert("Erreur", "Échec de la connexion", "Email ou mot de passe incorrect.");
        }
    }

    @FXML
    private void navigateToRegister() {
        try {
            // Load the register.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec du chargement", "Impossible de charger la page d'inscription.");
        }
    }

    // Helper method to show alerts
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        // Simple regex for email validation
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
} 
