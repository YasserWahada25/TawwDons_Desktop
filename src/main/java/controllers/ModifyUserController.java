package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;
import models.User;
import utils.SessionManager;
import utils.Router;
import java.io.IOException;

public class ModifyUserController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        // Initialize the role ComboBox with roles
        roleComboBox.getItems().addAll("ROLE_DONNEUR", "ROLE_BENEFICIAIRE", "ROLE_PROFESSIONNEL");
        
        // Setup button actions
        saveButton.setOnAction(event -> handleModifyButtonAction());
        cancelButton.setOnAction(event -> navigateToHome());
        
        // Setup field validation
        setupValidation();
    }
    
    private void setupValidation() {
        // Nom validation
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 2) {
                nomField.setStyle("-fx-border-color: red;");
            } else {
                nomField.setStyle("");
            }
        });
        
        // Prenom validation
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 2) {
                prenomField.setStyle("-fx-border-color: red;");
            } else {
                prenomField.setStyle("");
            }
        });
        
        // Email validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidEmail(newValue)) {
                emailField.setStyle("-fx-border-color: red;");
            } else {
                emailField.setStyle("");
            }
        });
        
        // Password confirmation validation
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(passwordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: red;");
            } else {
                confirmPasswordField.setStyle("");
            }
        });
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!confirmPasswordField.getText().isEmpty() && !newValue.equals(confirmPasswordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: red;");
            } else if (!confirmPasswordField.getText().isEmpty()) {
                confirmPasswordField.setStyle("");
            }
        });
    }
    
    // Method to set the user and populate fields
    public void setUser(User user) {
        this.currentUser = user;
        
        if (user != null) {
            nomField.setText(user.getNom());
            prenomField.setText(user.getPrenom());
            emailField.setText(user.getEmail());
            
            // Set the role in the combo box
            String userRole = user.getRoles();
            if (userRole != null && !userRole.isEmpty()) {
                // Clean up the role format if needed (remove brackets, quotes, etc.)
                userRole = userRole.replace("[", "").replace("]", "").replace("\"", "");
                // Find the matching role in the combobox
                for (String role : roleComboBox.getItems()) {
                    if (userRole.contains(role)) {
                        roleComboBox.setValue(role);
                        break;
                    }
                }
            }
            
            // Don't populate password fields for security
            passwordField.clear();
            confirmPasswordField.clear();
        }
    }

    @FXML
    private void handleModifyButtonAction() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        // Reset error message
        errorLabel.setText("");
        
        // Validate input fields
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || role == null) {
            errorLabel.setText("Les champs nom, prénom, email et rôle doivent être remplis.");
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            errorLabel.setText("Format d'email invalide.");
            return;
        }
        
        // Validate passwords
        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }
        
        // Create a user object with the current user's ID
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setNom(nom);
        updatedUser.setPrenom(prenom);
        updatedUser.setEmail(email);
        updatedUser.setRoles(role);
        
        // Only set password if it's not empty
        if (!password.isEmpty()) {
            updatedUser.setPassword(password);
        }
        
        // Add debugging
        System.out.println("Updating user with ID: " + currentUser.getId());
        System.out.println("User details - Nom: " + nom + ", Prénom: " + prenom + ", Email: " + email + ", Role: " + role);

        // Call the service to update the user
        boolean success = userService.updateUser(updatedUser);
        System.out.println("Update result: " + success);

        if (success) {
            // Update the session with the new user info
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setRoles(role);
            
            sessionManager.setCurrentUser(currentUser);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour avec succès.");
            
            // Navigate back to home
            navigateToHome();
        } else {
            errorLabel.setText("Erreur lors de la mise à jour du profil.");
        }
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            // Get the controller and update UI with current user
            HomeController homeController = loader.getController();
            homeController.updateUI(sessionManager.getCurrentUser());
            
            Stage stage = (Stage) nomField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la navigation vers la page d'accueil.");
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    public void navigateToConnectionHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConnectionHistory.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la navigation vers l'historique de connexion.");
        }
    }
}