package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.User;
import services.UserService;

import java.util.List;
import java.util.Optional;

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
    private Button deleteButton;
    @FXML
    private Button adminBtn;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Set up the table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));




        adminBtn.setOnAction(this::goToAdmin);

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
    private void goToAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Admin.fxml"));
            Stage stage = (Stage) adminBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Admin");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du chargement de l'interface Admin.");
        }
    }
} 