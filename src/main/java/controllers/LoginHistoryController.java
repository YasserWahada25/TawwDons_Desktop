package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.User;
import services.UserService;
import utils.Router;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import java.text.SimpleDateFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;

public class LoginHistoryController {

    @FXML private TableView<User> historyTable;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> typeColumn;
    @FXML private TableColumn<User, String> lastLoginColumn;

    @FXML private Label totalLoginsLabel;
    @FXML private Label lastLoginDateLabel;
    @FXML private Label inactiveUsersLabel;

    @FXML private ComboBox<User> userFilterComboBox;

    private final UserService userService = new UserService();
    private List<User> allUsers;

    @FXML
    public void initialize() {
        // Set up table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type_utilisateur"));

        // Format the last login date
        lastLoginColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            Date lastLogin = user.getLast_login_date();
            if (lastLogin != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                return new SimpleStringProperty(dateFormat.format(lastLogin));
            } else {
                return new SimpleStringProperty("⚠️ Jamais connecté");
            }
        });

        // Setup user filter combo box
        userFilterComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return null;
                return user.getNom() + " " + user.getPrenom() + " (" + user.getEmail() + ")";
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        userFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Show all users
                historyTable.setItems(FXCollections.observableArrayList(allUsers));
            } else {
                // Filter to show only the selected user
                historyTable.setItems(FXCollections.observableArrayList(
                        allUsers.stream()
                                .filter(user -> user.getId().equals(newVal.getId()))
                                .collect(Collectors.toList())
                ));
            }
        });

        // Load data
        loadData();
    }

    private void loadData() {
        try {
            // Get all users with login history
            allUsers = userService.getUserLoginHistory();

            if (allUsers == null) {
                allUsers = new ArrayList<>();
            }

            if (allUsers.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucune donnée",
                        "Aucun utilisateur trouvé",
                        "Il semble qu'il n'y ait aucun utilisateur dans la base de données.");
                return;
            }

            // Always display all users, even if they have never logged in
            ObservableList<User> usersData = FXCollections.observableArrayList(allUsers);
            historyTable.setItems(usersData);

            // Configure filter combobox (add option for "All Users" at the top)
            User allUsersOption = new User();
            allUsersOption.setNom("Tous");
            allUsersOption.setPrenom("les utilisateurs");
            allUsersOption.setEmail("");

            ObservableList<User> filterOptions = FXCollections.observableArrayList();
            filterOptions.add(allUsersOption);
            filterOptions.addAll(allUsers);
            userFilterComboBox.setItems(filterOptions);
            userFilterComboBox.getSelectionModel().selectFirst();

            // Update statistics
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la liste des utilisateurs.",
                    "Détails de l'erreur: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (allUsers == null || allUsers.isEmpty()) {
            totalLoginsLabel.setText("Total des connexions: 0");
            lastLoginDateLabel.setText("Dernière connexion: --");
            inactiveUsersLabel.setText("Utilisateurs inactifs: 0");
            return;
        }

        // Count users that have logged in at least once
        long loggedInUsers = allUsers.stream()
                .filter(user -> user.getLast_login_date() != null)
                .count();

        totalLoginsLabel.setText("Total des connexions: " + loggedInUsers);

        // Find most recent login
        Date mostRecentLogin = allUsers.stream()
                .map(User::getLast_login_date)
                .filter(date -> date != null)
                .max(Date::compareTo)
                .orElse(null);

        if (mostRecentLogin != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lastLoginDateLabel.setText("Dernière connexion: " + dateFormat.format(mostRecentLogin));
        } else {
            lastLoginDateLabel.setText("Dernière connexion: --");
        }

        // Count inactive users (never logged in)
        long inactiveUsers = allUsers.stream()
                .filter(user -> user.getLast_login_date() == null)
                .count();

        inactiveUsersLabel.setText("Utilisateurs inactifs: " + inactiveUsers);

        // Debug output
        System.out.println("Statistics: " + loggedInUsers + " users logged in, " +
                inactiveUsers + " inactive users, latest login: " +
                (mostRecentLogin != null ? mostRecentLogin : "none"));
    }

    @FXML
    private void refreshData() {
        loadData();
    }

    @FXML
    private void navigateToDashboard() {
        Router.navigateTo("/views/Dashboard.fxml");
    }

    @FXML
    private void navigateToDonsRequests() {
        Router.navigateTo("/Admin/RequestAddDons.fxml");
    }

    @FXML
    private void navigateToUserList() {
        Router.navigateTo("/Admin/UserList.fxml");
    }

    @FXML
    private void goBack() {
        try {
            // Use Router for consistent navigation
            Router.navigateTo("/Admin/UserList.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la liste des utilisateurs.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}