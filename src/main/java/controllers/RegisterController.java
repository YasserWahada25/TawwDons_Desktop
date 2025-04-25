package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;
import models.User;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ToggleGroup userTypeGroup;

    @FXML
    private RadioButton radioDonneur;

    @FXML
    private RadioButton radioBeneficiaire;

    @FXML
    private RadioButton radioProfessionnel;

    @FXML
    private Hyperlink loginLink;

    private UserService userService = new UserService();

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    // Name validation pattern (letters, spaces, hyphens)
    private static final Pattern NAME_PATTERN = 
        Pattern.compile("^[a-zA-ZÀ-ÿ\\s-]+$");

    @FXML
    private void handleRegisterButtonAction() {
        // Clear previous styles
        nomField.setStyle("");
        prenomField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        
        // Get form data
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validate all fields
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Erreurs de validation:\n");
        
        // Validate nom
        if (nom.isEmpty()) {
            errorMessage.append("- Le nom est requis\n");
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(nom).matches()) {
            errorMessage.append("- Le nom ne doit contenir que des lettres, espaces et tirets\n");
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate prenom
        if (prenom.isEmpty()) {
            errorMessage.append("- Le prénom est requis\n");
            prenomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(prenom).matches()) {
            errorMessage.append("- Le prénom ne doit contenir que des lettres, espaces et tirets\n");
            prenomField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate email
        if (email.isEmpty()) {
            errorMessage.append("- L'email est requis\n");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorMessage.append("- Format d'email invalide\n");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (userService.emailExists(email)) {
            errorMessage.append("- Cet email est déjà utilisé\n");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validate password
        if (password.isEmpty()) {
            errorMessage.append("- Le mot de passe est requis\n");
            passwordField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (password.length() < 6) {
            errorMessage.append("- Le mot de passe doit contenir au moins 6 caractères\n");
            passwordField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // If validation fails, show error and return
        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return;
        }
        
        // Determine selected user type
        String userType;
        String role;
        
        if (radioDonneur.isSelected()) {
            userType = "donneur";
            role = "[ROLE_DONNEUR]";
        } else if (radioBeneficiaire.isSelected()) {
            userType = "beneficiaire";
            role = "[ROLE_BENEFICIAIRE]";
        } else if (radioProfessionnel.isSelected()) {
            userType = "professionnel";
            role = "[ROLE_PROFESSIONNEL]";
        } else {
            // Default to donneur if somehow none selected
            userType = "donneur";
            role = "[ROLE_DONNEUR]";
        }
        
        // Create user object
        User newUser = new User();
        newUser.setEmail(email);
        
        // Hash the password before storing (like in Symfony)
        newUser.setPassword(hashPassword(password));
        
        newUser.setRoles(role);
        newUser.setNom(nom);
        newUser.setPrenom(prenom);
        newUser.setType_utilisateur(userType);
        newUser.setEtat_compte("verrouillé");
        
        // Register the user
        boolean success = userService.registerUser(newUser);
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Inscription réussie", 
                    "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
            navigateToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", 
                    "Une erreur s'est produite lors de l'inscription. Veuillez réessayer.");
        }
    }
    
    /**
     * Simple password hashing using SHA-256
     * In production, you would use BCrypt or similar
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback if hashing fails
            return "$2y$13$" + password.hashCode();
        }
    }
    
    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", 
                    "Impossible de charger la page de connexion.");
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        // Initialization logic here
        System.out.println("RegisterController initialized");
    }

    @FXML
    public void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load Register.fxml");
        }
    }
} 