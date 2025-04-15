package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import service.UserService;
import java.io.IOException;
import models.User;
import utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();

    @FXML
    private void handleLoginButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();

        System.out.println("Email: " + email);
        System.out.println("Password: " + password);

        // Validate email and password
        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Login failed: Email or password is empty");
            return;
        }

        // Hash the entered password
        String hashedPassword = UserService.hashPassword(password);

        // Check if the email exists and the password is correct
        if (userService.validateUser(email, hashedPassword)) {
            System.out.println("Login successful");
            // Navigate to the main interface
            navigateToMainInterface();
        } else {
            System.out.println("Login failed: Incorrect email or password");
        }
    }

    @FXML
    private void navigateToRegister() {
        try {
            // Load the register.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load register.fxml");
        }
    }

    private void navigateToMainInterface() {
        try {
            // Load the maininterface.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/maininterface.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load maininterface.fxml");
        }
    }
} 
