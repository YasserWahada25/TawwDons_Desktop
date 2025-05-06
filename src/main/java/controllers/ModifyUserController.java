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
        // 1) initialisation de la combo des rôles
        roleComboBox.getItems().addAll(
                "ROLE_DONNEUR",
                "ROLE_BENEFICIAIRE",
                "ROLE_PROFESSIONNEL"
        );

        // 2) setup validation et handlers
        setupValidation();
        saveButton.setOnAction(evt -> handleModifyButtonAction());
        cancelButton.setOnAction(evt -> navigateToHome());

        // 3) pré-remplissage automatique avec l'utilisateur en session
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            setUser(user);
        }
    }

    private void setupValidation() {
        nomField.textProperty().addListener((obs, oldV, newV) -> {
            nomField.setStyle(newV.length() < 2 ? "-fx-border-color: red;" : "");
        });

        prenomField.textProperty().addListener((obs, oldV, newV) -> {
            prenomField.setStyle(newV.length() < 2 ? "-fx-border-color: red;" : "");
        });

        emailField.textProperty().addListener((obs, oldV, newV) -> {
            emailField.setStyle(isValidEmail(newV) ? "" : "-fx-border-color: red;");
        });

        confirmPasswordField.textProperty().addListener((obs, oldV, newV) -> {
            confirmPasswordField.setStyle(
                    !newV.equals(passwordField.getText()) ? "-fx-border-color: red;" : ""
            );
        });

        passwordField.textProperty().addListener((obs, oldV, newV) -> {
            if (!confirmPasswordField.getText().isEmpty()) {
                confirmPasswordField.setStyle(
                        newV.equals(confirmPasswordField.getText()) ? "" : "-fx-border-color: red;"
                );
            }
        });
    }

    public void setUser(User user) {
        this.currentUser = user;
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());

        String userRole = user.getRoles();
        if (userRole != null) {
            userRole = userRole.replace("[", "").replace("]", "").replace("\"", "");
            for (String role : roleComboBox.getItems()) {
                if (userRole.contains(role)) {
                    roleComboBox.setValue(role);
                    break;
                }
            }
        }

        // On ne pré-remplit pas le mot de passe
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleModifyButtonAction() {
        String nom    = nomField.getText();
        String prenom = prenomField.getText();
        String email  = emailField.getText();
        String pwd    = passwordField.getText();
        String cpwd   = confirmPasswordField.getText();
        String role   = roleComboBox.getValue();

        errorLabel.setText("");

        // validations
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || role == null) {
            errorLabel.setText("Les champs nom, prénom, email et rôle doivent être remplis.");
            return;
        }
        if (!isValidEmail(email)) {
            errorLabel.setText("Format d'email invalide.");
            return;
        }
        if (!pwd.isEmpty() && !pwd.equals(cpwd)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        // création de l'objet User à mettre à jour
        User updated = new User();
        updated.setId(currentUser.getId());
        updated.setNom(nom);
        updated.setPrenom(prenom);
        updated.setEmail(email);
        updated.setRoles(role);
        if (!pwd.isEmpty()) {
            updated.setPassword(pwd);
        }

        boolean ok = userService.updateUser(updated);
        if (ok) {
            // on met à jour la session
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setRoles(role);
            sessionManager.setCurrentUser(currentUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour avec succès.");
            navigateToHome();
        } else {
            errorLabel.setText("Erreur lors de la mise à jour du profil.");
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@"
                + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(regex);
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            HomeController hc = loader.getController();
            hc.updateUI(sessionManager.getCurrentUser());

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la navigation vers la page d'accueil.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String txt) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(txt);
        a.showAndWait();
    }

    @FXML
    public void navigateToConnectionHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConnectionHistory.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la navigation vers l'historique de connexion.");
        }
    }
}
