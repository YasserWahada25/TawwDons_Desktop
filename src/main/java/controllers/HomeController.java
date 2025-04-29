package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import utils.Router;
import utils.SessionManager;
import models.User;
import java.io.IOException;

public class HomeController {

    @FXML
    private MenuItem menuListeDons;

    @FXML
    private MenuItem menuPosterDon;

    @FXML
    private Button btnHome;

    @FXML
    private MenuItem menuArticleList;

    @FXML
    private MenuItem menuOffreList;
    @FXML
    private Button userBtn;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnLogin;

    private User loggedInUser;
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        System.out.println("✅ HomeController chargé");

        // Check if user is already logged in
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
        userBtn.setOnAction(this::goToUser);

        // Redirection vers la page Liste des Dons
        // menuListeDons.setOnAction(event -> Router.navigateTo("/ListDons.fxml"));

        // Redirection vers la page Ajouter un Don
        menuPosterDon.setOnAction(event -> Router.navigateTo("/AddDons.fxml"));

        btnHome.setOnAction(event -> Router.navigateTo("/Home.fxml"));

        menuArticleList.setOnAction(event -> Router.navigateTo("/articleList.fxml"));

        menuOffreList.setOnAction(event -> Router.navigateTo("/offre/ListOffres.fxml"));

        // Redirection vers la page d'inscription/déconnexion
        btnRegister.setOnAction(event -> {
            if (sessionManager.isLoggedIn()) {
                logout(); // Logout if user is logged in
            } else {
                navigateToRegister(); // Navigate to register if user is not logged in
            }
        });

        // Redirection vers la page de connexion ou profil
        btnLogin.setOnAction(event -> {
            if (sessionManager.isLoggedIn()) {
                navigateToProfilePage(); // Navigate to profile page if user is logged in
            } else {
                navigateToLogin(); // Navigate to login if user is not logged in
            }
        });
    }

    // Method to update the UI based on the user's login state
    public void updateUI(User user) {
        this.loggedInUser = user;
        if (user != null) {
            btnLogin.setText(user.getNom()); // Change "Login" to user's name
            btnRegister.setText("Logout"); // Change "Register" to "Logout"
        } else {
            btnLogin.setText("Login"); // Reset to "Login"
            btnRegister.setText("Register"); // Reset to "Register"
        }
    }

    // Method to handle logout
    private void logout() {
        sessionManager.clearSession();
        this.loggedInUser = null;
        updateUI(null); // Reset the UI
        navigateToLogin(); // Redirect to login page
    }

    // Method to navigate to the profile page
    private void navigateToProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyUser.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the current user
            ModifyUserController controller = loader.getController();
            controller.setUser(sessionManager.getCurrentUser());
            
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load ModifyUser.fxml");
        }
    }
    @FXML
    private void goToUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Utilisateur");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du chargement de l'interface Utilisateur.");
        }
    }

    @FXML
    public void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load Register.fxml");
        }
    }

    @FXML
    public void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load Login.fxml");
        }
    }
}
