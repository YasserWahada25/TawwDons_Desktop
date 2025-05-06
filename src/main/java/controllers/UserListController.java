package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import utils.SessionManager;
import utils.Router;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;

public class UserListController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> nomColumn;

    @FXML
    private TableColumn<User, String> prenomColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, String> blockExpirationColumn;

    @FXML
    private TableColumn<User, String> lastLoginColumn;

    @FXML
    private Button deleteButton;

    @FXML
    private Button banButton;

    @FXML
    private Button unbanButton;

    @FXML
    private Button loginHistoryButton;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Button logoutButton;

    private UserService userService = new UserService();
    private boolean showingLoginHistory = false;
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        // Set up the table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));

        // Configure status column to show if user is banned
        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String status = user.isBanned() ? "Banned" : "Active";
            return new SimpleStringProperty(status);
        });

        // Configure block expiration column
        blockExpirationColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            Date blockExpiration = user.getBlock_expiration();
            return new SimpleStringProperty(blockExpiration != null ? blockExpiration.toString() : "N/A");
        });

        // Configure last login date column
        if (lastLoginColumn != null) {
            lastLoginColumn.setCellValueFactory(cellData -> {
                User user = cellData.getValue();
                Date lastLogin = user.getLast_login_date();
                if (lastLogin != null) {
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    return new SimpleStringProperty(dateFormat.format(lastLogin));
                } else {
                    return new SimpleStringProperty("Jamais connecté");
                }
            });
        }

        // Display current admin user information
        User currentAdmin = sessionManager.getCurrentUser();
        if (currentAdmin != null && currentUserLabel != null) {
            currentUserLabel.setText("Connecté: " + currentAdmin.getNom() + " " + currentAdmin.getPrenom());
        }

        // Set up logout button if it exists
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogout());
        }

        // Load the user data into the table
        loadUserData();
    }

    private void loadUserData() {
        // Fetch all users from the database
        List<User> users = userService.getAllUsers();

        // Add the users to the table
        userTable.getItems().setAll(users);
    }

    @FXML
    private void handleDeleteButtonAction() {
        // Get the selected user from the table
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun utilisateur sélectionné", "Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        // Show a confirmation dialog
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Confirmation de suppression");
        confirmationDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");
        confirmationDialog.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the selected user from the database
            boolean success = userService.deleteUser(selectedUser.getEmail());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Utilisateur supprimé", "L'utilisateur a été supprimé avec succès.");
                // Refresh the table
                loadUserData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression", "Une erreur s'est produite lors de la suppression de l'utilisateur.");
            }
        }
    }

    @FXML
    private void handleBanButtonAction() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun utilisateur sélectionné", "Veuillez sélectionner un utilisateur à bannir.");
            return;
        }

        // Dialog to select ban duration
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Bannir l'utilisateur");
        dialog.setHeaderText("Sélectionnez la durée du bannissement");

        // Set the button types
        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Create a combo box for duration selection
        ComboBox<String> durationComboBox = new ComboBox<>();
        durationComboBox.getItems().addAll(
                "1 jour",
                "3 jours",
                "7 jours",
                "30 jours",
                "Permanent (10 ans)"
        );
        durationComboBox.setValue("7 jours"); // Default value

        dialog.getDialogPane().setContent(durationComboBox);

        // Convert the result to duration in days when the confirm button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String selected = durationComboBox.getValue();
                if (selected.equals("1 jour")) return 1;
                if (selected.equals("3 jours")) return 3;
                if (selected.equals("7 jours")) return 7;
                if (selected.equals("30 jours")) return 30;
                if (selected.equals("Permanent (10 ans)")) return 3650; // ~10 years
                return 7; // Default
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        if (result.isPresent()) {
            // Calculate expiration date
            int days = result.get();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, days);
            Date expirationDate = calendar.getTime();

            // Ban the user
            boolean success = userService.banUser(selectedUser.getEmail(), expirationDate);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Utilisateur banni",
                        "L'utilisateur a été banni avec succès jusqu'au " + expirationDate.toString());
                loadUserData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de bannir l'utilisateur.");
            }
        }
    }

    @FXML
    private void handleUnbanButtonAction() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun utilisateur sélectionné", "Veuillez sélectionner un utilisateur à débannir.");
            return;
        }

        if (!selectedUser.isBanned()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Cet utilisateur n'est pas banni.");
            return;
        }

        // Show confirmation dialog
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Débannir l'utilisateur");
        confirmationDialog.setHeaderText("Êtes-vous sûr de vouloir débannir cet utilisateur ?");

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userService.unbanUser(selectedUser.getEmail());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Utilisateur débanni", "L'utilisateur a été débanni avec succès.");
                loadUserData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de débannir l'utilisateur.");
            }
        }
    }

    @FXML
    private void handleLoginHistoryButtonAction() {
        try {
            // Navigate to the admin connection history view
            Router.navigateTo("/Admin/ConnectionHistoryAdmin.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'historique des connexions.");
        }
    }

    @FXML
    private void handleLogout() {
        sessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter correctement.");
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
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin/UserList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load UserList.fxml");
        }
    }

    @FXML
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load Dashboard.fxml");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le tableau de bord.");
        }
    }

    @FXML
    private void navigateToStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin/Statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load Statistics.fxml");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les statistiques.");
        }
    }
}
