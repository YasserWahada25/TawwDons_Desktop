package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.Router;
import utils.SessionManager;

public class NavbarController {

    // Boutons principaux
    @FXML private Button btnHome;
    @FXML private Button btnEvaluations;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Button btnNotif; // ✅ Bouton notification

    // Menus
    @FXML private MenuItem miListeDons;
    @FXML private MenuItem miPosterDon;
    @FXML private MenuItem miArticleList;
    @FXML private MenuItem miListeOffres;

    // Menu utilisateur (visible uniquement si connecté)
    @FXML private MenuButton userMenu;
    @FXML private MenuItem miEditProfile;
    @FXML private MenuItem miLogout;

    @FXML
    public void initialize() {
        // Navigation générale
        btnHome.setOnAction(e       -> Router.navigateTo("/Home.fxml"));
        miListeDons.setOnAction(e   -> Router.navigateTo("/Dons/ListDons.fxml"));
        miPosterDon.setOnAction(e   -> Router.navigateTo("/Dons/AddDons.fxml"));
        miArticleList.setOnAction(e -> Router.navigateTo("/article/articleList.fxml"));
        miListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffres.fxml"));
        btnEvaluations.setOnAction(e -> Router.navigateTo("/views/user.fxml"));

        // Notification (🔔)
        btnNotif.setOnAction(e -> {
            // 👉 Rediriger vers une page notifications ou afficher un popup
            Router.navigateTo("/notifications/Notifications.fxml");
            // OU : afficher une alerte temporaire
            // new Alert(Alert.AlertType.INFORMATION, "Vous n'avez pas de nouvelles notifications.").showAndWait();
        });

        // Navigation login/register
        btnLogin.setOnAction(e    -> Router.navigateTo("/Login.fxml"));
        btnRegister.setOnAction(e -> Router.navigateTo("/Register.fxml"));

        // Gestion de session utilisateur
        if (SessionManager.getInstance().isLoggedIn()) {
            String nomUtilisateur = SessionManager.getCurrentUser().getNom();
            userMenu.setText(nomUtilisateur);
            userMenu.setVisible(true);
            btnLogin.setVisible(false);
            btnRegister.setVisible(false);

            miEditProfile.setOnAction(e -> Router.navigateTo("/ModifyUser.fxml"));
            miLogout.setOnAction(e -> {
                SessionManager.clearSession();
                Router.navigateTo("/Home.fxml");
            });
        } else {
            userMenu.setVisible(false);
        }
    }
}
