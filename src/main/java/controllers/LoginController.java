package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import utils.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLoginButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();

        System.out.println("Email: " + email);
        System.out.println("Password: " + password);

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("❌ Login failed: Email or password is empty");
            return;
        }

        String hashedPassword = UserService.hashPassword(password);

        if (userService.validateUser(email, hashedPassword)) {
            System.out.println("✅ Login successful");
            User user = userService.getUserByEmailAndPassword(email, hashedPassword);

            if (user != null) {
                SessionManager.setCurrentUser(user);
                System.out.println("➡️ Utilisateur connecté : " + user.getPrenom() + " " + user.getNom());
                navigateToMainInterface();
            } else {
                System.out.println("⚠️ Utilisateur introuvable malgré un login valide !");
            }
        } else {
            System.out.println("❌ Login failed: Incorrect email or password");
        }
    }

    private void navigateToMainInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load Home.fxml");
        }
    }

    @FXML
    private void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load register.fxml");
        }
    }
}
