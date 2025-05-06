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
import services.ConnectionHistoryService;
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
import java.util.Random;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import services.EmailService;
import java.security.SecureRandom;
import javafx.scene.control.DialogPane;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button googleSignInButton;
    @FXML private VBox loginVBox;

    private TextField captchaInputField;
    private String captchaText;
    private ImageView captchaImageView;
    private VBox captchaContainer;

    private int failedLoginAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private boolean captchaRequired = false;

    private final UserService userService = new UserService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final ConnectionHistoryService connectionHistoryService = new ConnectionHistoryService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    private void handleLoginButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Validate inputs
        if (email == null || email.trim().isEmpty()) {
            showAlert("Erreur", "Email invalide", "Veuillez saisir votre adresse email.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showAlert("Erreur", "Mot de passe invalide", "Veuillez saisir votre mot de passe.");
            return;
        }

        // If CAPTCHA is shown, validate it
        if (captchaRequired && !validateCaptcha()) {
            showAlert("Erreur", "CAPTCHA incorrect", "Veuillez saisir correctement le texte du CAPTCHA.");
            generateNewCaptcha(); // Generate a new CAPTCHA after failed attempt
            return;
        }

        try {
            // Initialize connection history table if needed
            connectionHistoryService.createTableIfNotExists();

            // Authenticate the user
            User user = userService.authenticate(email, password);

            if (user != null) {
                // Check if the user is banned
                if (user.isBanned()) {
                    showAlert(AlertType.ERROR, "Accès refusé", "Compte banni",
                            "Votre compte est actuellement banni. Date d'expiration: " + user.getBlock_expiration());
                    return;
                }

                // Update login history/stats
                userService.updateLastLoginDate(email);

                // Record connection in connection history
                connectionHistoryService.recordConnection(user.getId());

                // Reset failed login counter on success
                failedLoginAttempts = 0;
                captchaRequired = false;

                // Store current user in session and navigate to home
                sessionManager.setCurrentUser(user);

                // Navigate to the appropriate page based on role
                // If user is admin, go to admin panel, otherwise go to home
                if (user.getRoles().contains("ADMIN") ||
                        user.getRoles().contains("ROLE_ADMIN") ||
                        "admin".equalsIgnoreCase(user.getType_utilisateur())) {

                    navigateToAdminPanel();
                } else {
                    navigateToHome(user);
                }
            } else {
                // Invalid credentials
                failedLoginAttempts++;

                if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
                    captchaRequired = true;
                    showCaptcha();
                    showAlert("Erreur", "Plusieurs tentatives échouées",
                            "Après plusieurs échecs, vous devez compléter un CAPTCHA pour continuer.");
                } else {
                    showAlert("Erreur", "Authentification échouée",
                            "Email ou mot de passe incorrect. Tentative " + failedLoginAttempts + "/" + MAX_FAILED_ATTEMPTS);
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'authentification",
                    "Une erreur s'est produite lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates and displays a captcha verification
     */
    private void showCaptcha() {
        try {
            // Create the captcha container if it doesn't exist
            if (captchaContainer == null) {
                captchaContainer = new VBox(10);
                captchaContainer.setPadding(new Insets(10, 0, 10, 0));
            } else {
                captchaContainer.getChildren().clear();
            }

            // Create the captcha components
            Label captchaLabel = new Label("Veuillez entrer les caractères ci-dessous:");
            captchaLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            // Generate the captcha
            generateNewCaptcha();

            // Create input field for user
            captchaInputField = new TextField();
            captchaInputField.setPromptText("Entrez le texte du captcha");

            // Create a refresh button
            Button refreshButton = new Button("Actualiser");
            refreshButton.setOnAction(e -> generateNewCaptcha());

            // Add components to the container
            HBox captchaImageContainer = new HBox(10);
            captchaImageContainer.getChildren().addAll(captchaImageView, refreshButton);
            captchaContainer.getChildren().addAll(captchaLabel, captchaImageContainer, captchaInputField);

            // Add the container to the login form at the correct position (right after password field)
            if (!loginVBox.getChildren().contains(captchaContainer)) {
                // Find the position of the login button
                int insertPosition = -1;

                // Find the HBox containing the password field
                for (int i = 0; i < loginVBox.getChildren().size(); i++) {
                    if (loginVBox.getChildren().get(i) instanceof HBox) {
                        HBox hbox = (HBox) loginVBox.getChildren().get(i);
                        for (javafx.scene.Node node : hbox.getChildren()) {
                            if (node instanceof PasswordField && node.equals(passwordField)) {
                                insertPosition = i + 1; // Insert right after the password HBox
                                break;
                            }
                        }
                    }

                    if (insertPosition != -1) {
                        break;
                    }
                }

                // If we couldn't find by password field, look for the login button
                if (insertPosition == -1) {
                    for (int i = 0; i < loginVBox.getChildren().size(); i++) {
                        if (loginVBox.getChildren().get(i) instanceof Button) {
                            Button btn = (Button) loginVBox.getChildren().get(i);
                            if (btn.getId() != null && btn.getId().equals("loginButton")) {
                                insertPosition = i;
                                break;
                            }
                        }
                    }
                }

                // Insert captcha container at the found position or at the end as fallback
                if (insertPosition != -1) {
                    loginVBox.getChildren().add(insertPosition, captchaContainer);
                } else {
                    // Fallback - add to the end
                    loginVBox.getChildren().add(captchaContainer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Captcha", "Impossible de générer le captcha: " + e.getMessage());
        }
    }

    /**
     * Generates a new captcha image and text
     */
    private void generateNewCaptcha() {
        try {
            // Generate random text for captcha
            Random random = new Random();
            StringBuilder captchaBuilder = new StringBuilder();
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

            for (int i = 0; i < 6; i++) {
                captchaBuilder.append(chars.charAt(random.nextInt(chars.length())));
            }
            captchaText = captchaBuilder.toString();

            // Create the captcha image
            BufferedImage bufferedImage = new BufferedImage(200, 80, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufferedImage.createGraphics();

            // Set background
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, 200, 80);

            // Set rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Add noise (random lines)
            g2d.setColor(java.awt.Color.LIGHT_GRAY);
            for (int i = 0; i < 20; i++) {
                int x1 = random.nextInt(200);
                int y1 = random.nextInt(80);
                int x2 = random.nextInt(200);
                int y2 = random.nextInt(80);
                g2d.drawLine(x1, y1, x2, y2);
            }

            // Draw the text with different colors and rotations
            for (int i = 0; i < captchaText.length(); i++) {
                int fontSize = 28 + random.nextInt(10);
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, fontSize));

                // Random color
                g2d.setColor(new java.awt.Color(
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100)
                ));

                // Position and slight rotation
                double rotationAngle = -0.2 + random.nextDouble() * 0.4; // -0.2 to 0.2 radians
                g2d.rotate(rotationAngle, 30 + i * 30, 40);
                g2d.drawString(String.valueOf(captchaText.charAt(i)), 30 + i * 30, 40 + random.nextInt(10));
                g2d.rotate(-rotationAngle, 30 + i * 30, 40);
            }

            g2d.dispose();

            // Convert BufferedImage to JavaFX Image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            Image captchaImage = new Image(bais);

            // Create or update ImageView
            if (captchaImageView == null) {
                captchaImageView = new ImageView(captchaImage);
            } else {
                captchaImageView.setImage(captchaImage);
            }

            captchaImageView.setFitHeight(80);
            captchaImageView.setFitWidth(200);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Génération de captcha", "Impossible de générer l'image du captcha: " + e.getMessage());
        }
    }

    /**
     * Validates the user input against the captcha
     */
    private boolean validateCaptcha() {
        if (captchaText == null || captchaInputField == null) {
            return false;
        }
        return captchaInputField.getText().equals(captchaText);
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

                                // Update last login date
                                userService.updateLastLoginDate(existingUser.getEmail());

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
                        // Update last login date for the new user
                        userService.updateLastLoginDate(registeredUser.getEmail());

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

        // Record connection in connection history
        connectionHistoryService.recordConnection(user.getId());

        // Check if user is admin
        boolean isAdmin = false;

        // Different ways admin role might be stored in the database
        if (user.getRoles() != null) {
            String roles = user.getRoles().toLowerCase();
            isAdmin = roles.contains("admin") ||
                    roles.contains("role_admin") ||
                    roles.contains("\"admin\"") ||
                    roles.contains("\"role_admin\"");
        }

        // Also check type_utilisateur for admin
        if (user.getType_utilisateur() != null) {
            String userType = user.getType_utilisateur().toLowerCase();
            isAdmin = isAdmin || userType.contains("admin");
        }

        System.out.println("Google login - User roles: " + user.getRoles());
        System.out.println("Google login - User type: " + user.getType_utilisateur());
        System.out.println("Google login - Is admin? " + isAdmin);

        // Navigate to the appropriate interface based on user role
        try {
            FXMLLoader loader;
            Parent root;

            if (isAdmin) {
                // If admin, navigate to UserList
                System.out.println("Google login - Navigating to Admin/ajouterArticle.fxml");
                loader = new FXMLLoader(getClass().getResource("/Admin/ajouterArticle.fxml"));
                root = loader.load();

                // No need to update UI as UserListController handles this
            } else {
                // If regular user, navigate to Home
                System.out.println("Google login - Navigating to Home.fxml");
                loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
                root = loader.load();

                // Get the controller and update UI
                HomeController homeController = loader.getController();
                homeController.updateUI(user);
            }

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

    @FXML
    private void navigateToForgotPassword() {
        try {
            // Load the forgot password file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        showAlert(AlertType.INFORMATION, title, header, content);
    }

    private void showAlert(AlertType alertType, String title, String header, String content) {
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

    // Generate a random verification code for password reset
    public static String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    // Method to handle password reset requests
    public void requestPasswordReset(String email) {
        if (email == null || email.isEmpty() || !isValidEmail(email)) {
            showAlert(AlertType.ERROR, "Error", "Invalid Email", "Please enter a valid email address.");
            return;
        }

        // Generate verification code
        String resetCode = generateVerificationCode();

        // Store the reset code in the user's account (implementation depends on your user service)
        boolean codeStored = userService.storeResetCode(email, resetCode);

        if (codeStored) {
            // Send reset email in a new thread to avoid freezing the UI
            new Thread(() -> {
                try {
                    EmailService.sendPasswordResetEmail(email, resetCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            showAlert(AlertType.INFORMATION, "Reset Email Sent",
                    "Password reset instructions",
                    "If an account exists with that email, we've sent password reset instructions.");
        } else {
            // Still show success to prevent email enumeration attacks
            showAlert(AlertType.INFORMATION, "Reset Email Sent",
                    "Password reset instructions",
                    "If an account exists with that email, we've sent password reset instructions.");
        }
    }

    /**
     * Navigate to admin panel
     */
    private void navigateToAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin/ajouterArticle.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec du chargement", "Impossible de charger le panneau d'administration.");
        }
    }

    /**
     * Navigate to home page
     */
    private void navigateToHome(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Get the controller and update UI if needed
            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.updateUI(user);
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec du chargement", "Impossible de charger la page d'accueil.");
        }
    }
}
