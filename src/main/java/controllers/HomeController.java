package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import models.User;
import utils.SessionManager;

public class HomeController {

    @FXML private Button btnLogin;
    @FXML private Button btnRegister;

    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        System.out.println("✅ HomeController chargé");

        // Mise à jour de l'affichage des boutons selon l'état de connexion
        User currentUser = sessionManager.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(User user) {
        if (btnLogin != null && btnRegister != null) {
            if (user != null) {
                btnLogin.setText(user.getNom());
                btnRegister.setText("Logout");
            } else {
                btnLogin.setText("Login");
                btnRegister.setText("Register");
            }
        }
    }
}
