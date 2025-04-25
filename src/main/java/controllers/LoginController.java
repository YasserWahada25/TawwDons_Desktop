package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.UserService;
import services.GoogleAuthService;
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
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Map;
import java.util.UUID;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button googleSignInButton;

    private final UserService userService = new UserService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
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
    private void handleGoogleSignIn() {
        // Show a loading indicator
        googleSignInButton.setDisable(true);
        
        // Use the GoogleAuthService to authenticate with Google
        googleAuthService.signInWithGoogle()
            .thenAccept(userData -> {
                Platform.runLater(() -> {
                    try {
                        // Re-enable the button
                        googleSignInButton.setDisable(false);
                        
                        System.out.println("Google authentication successful!");
                        System.out.println("Email: " + userData.get("email"));
                        System.out.println("Name: " + userData.get("name"));
                        System.out.println("Google ID: " + userData.get("googleId"));
                        
                        // Check if the user already exists by email or Google ID
                        User existingUser = userService.getUserByEmail(userData.get("email"));
                        
                        if (existingUser != null) {
                            System.out.println("User already exists, updating Google ID if necessary");
                            // Update Google ID if not already set
                            if (existingUser.getGoogle_id() == null || existingUser.getGoogle_id().isEmpty()) {
                                existingUser.setGoogle_id(userData.get("googleId"));
                                userService.updateUser(existingUser);
                            }
                            
                            // Log the user in
                            loginUser(existingUser);
                        } else {
                            System.out.println("Creating new user from Google data");
                            // Create a new user with the Google data
                            User newUser = new User();
                            newUser.setEmail(userData.get("email"));
                            newUser.setNom(userData.get("familyName"));
                            newUser.setPrenom(userData.get("givenName"));
                            newUser.setGoogle_id(userData.get("googleId"));
                            
                            // Generate a random password for users who sign in with Google
                            String randomPassword = UUID.randomUUID().toString();
                            newUser.setPassword(UserService.hashPassword(randomPassword));
                            
                            // Show role selection dialog
                            showRoleSelectionDialog(newUser);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Erreur de traitement",
                                 "Une erreur s'est produite lors du traitement des données utilisateur: " + e.getMessage());
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    googleSignInButton.setDisable(false);
                    ex.printStackTrace();
                    showAlert("Erreur", "Échec de la connexion Google", 
                        "Une erreur s'est produite lors de la connexion avec Google: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Shows the role selection dialog and registers the user with the selected role
     */
    private void showRoleSelectionDialog(User newUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RoleSelectionDialog.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Sélection du rôle");
            dialogStage.setScene(new Scene(root));
            
            RoleSelectionController controller = loader.getController();
            controller.setData(newUser, role -> {
                // Set the selected role and register the user
                newUser.setRoles(role);
                newUser.setType_utilisateur(roleToUserType(role));
                newUser.setEtat_compte("actif");
                
                if (userService.registerUser(newUser)) {
                    // Get the full user object with ID
                    User registeredUser = userService.getUserByEmail(newUser.getEmail());
                    
                    if (registeredUser != null) {
                        // Login the user
                        loginUser(registeredUser);
                    } else {
                        showAlert("Erreur", "Erreur de création de compte", 
                            "Le compte a été créé mais l'utilisateur n'a pas pu être récupéré.");
                    }
                } else {
                    showAlert("Erreur", "Erreur de création de compte", 
                        "Une erreur s'est produite lors de la création du compte.");
                }
            });
            
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur d'interface", 
                "Une erreur s'est produite lors de l'affichage du sélecteur de rôle.");
        }
    }
    
    /**
     * Converts a role to a user type
     */
    private String roleToUserType(String role) {
        switch (role) {
            case "ROLE_DONNEUR": return "donneur";
            case "ROLE_BENEFICIAIRE": return "beneficiaire";
            case "ROLE_PROFESSIONNEL": return "professionnel";
            default: return "donneur"; // Default to donneur
        }
    }
    
    /**
     * Logs the user in and navigates to the home page
     */
    private void loginUser(User user) {
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
        showAlert(AlertType.ERROR, title, header, content);
    }

    // Overloaded helper method to show alerts with custom alert type
    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
} 
