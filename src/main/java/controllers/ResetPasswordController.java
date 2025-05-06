package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.EmailService;
import services.UserService;

import java.io.IOException;
import java.util.regex.Pattern;

public class ResetPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private String storedEmail;
    private boolean codeVerified = false;

    private final UserService userService = new UserService();

    @FXML
    public void sendVerificationCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email invalide",
                    "Veuillez entrer une adresse email valide.");
            return;
        }

        // Check if user exists
        if (userService.getUserByEmail(email) == null) {
            // For security, we don't tell the user if the email exists or not
            showAlert(Alert.AlertType.INFORMATION, "Code envoyé", null,
                    "Si un compte est associé à cette adresse, un code de vérification sera envoyé.");
            return;
        }

        // Generate code and send email
        String resetCode = LoginController.generateVerificationCode();
        boolean codeStored = userService.storeResetCode(email, resetCode);

        if (codeStored) {
            this.storedEmail = email;

            // Send email in background thread
            new Thread(() -> {
                try {
                    EmailService.sendPasswordResetEmail(email, resetCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            showAlert(Alert.AlertType.INFORMATION, "Code envoyé", null,
                    "Un code de vérification a été envoyé à " + email + ". Veuillez vérifier votre boîte de réception et vos spams.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", null,
                    "Impossible de traiter la demande. Veuillez réessayer plus tard.");
        }
    }

    @FXML
    public void verifyCode() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Veuillez entrer le code de vérification.");
            return;
        }

        if (storedEmail == null || storedEmail.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null,
                    "Veuillez d'abord entrer votre email et demander un code de vérification.");
            return;
        }

        boolean isValid = userService.verifyResetCode(storedEmail, code);

        if (isValid) {
            codeVerified = true;
            showAlert(Alert.AlertType.INFORMATION, "Code vérifié", null,
                    "Code validé. Vous pouvez maintenant définir un nouveau mot de passe.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Code incorrect", null,
                    "Le code entré est incorrect ou expiré. Veuillez réessayer.");
        }
    }

    @FXML
    public void resetPassword() {
        if (!codeVerified) {
            showAlert(Alert.AlertType.ERROR, "Étape manquante", null,
                    "Veuillez d'abord vérifier le code reçu par email.");
            return;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", null,
                    "Veuillez remplir tous les champs du mot de passe.");
            return;
        }

        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe trop court", null,
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Mots de passe différents", null,
                    "Les mots de passe ne correspondent pas.");
            return;
        }

        boolean success = userService.updatePasswordAfterReset(storedEmail, newPassword);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", null,
                    "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
            navigateToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", null,
                    "Une erreur s'est produite lors de la réinitialisation du mot de passe. Veuillez réessayer.");
        }
    }

    @FXML
    public void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", null,
                    "Impossible de charger la page de connexion.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
    }
}