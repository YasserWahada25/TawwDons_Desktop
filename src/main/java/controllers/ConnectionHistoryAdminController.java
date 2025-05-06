package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import models.ConnectionHistory;
import models.User;
import services.ConnectionHistoryService;
import services.UserService;
import utils.Router;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConnectionHistoryAdminController {

    @FXML private TableView<ConnectionHistory> connectionHistoryTable;
    @FXML private TableColumn<ConnectionHistory, String> userNameColumn;
    @FXML private TableColumn<ConnectionHistory, String> userEmailColumn;
    @FXML private TableColumn<ConnectionHistory, String> userTypeColumn;
    @FXML private TableColumn<ConnectionHistory, String> dateColumn;

    @FXML private ComboBox<User> userFilterComboBox;
    @FXML private TextField userSearchField;
    @FXML private Label totalConnectionsLabel;

    private ObservableList<User> allUserOptions;

    private final ConnectionHistoryService connectionHistoryService = new ConnectionHistoryService();
    private final UserService userService = new UserService();

    private List<ConnectionHistory> allConnections;

    @FXML
    public void initialize() {
        // Set up table columns
        userNameColumn.setCellValueFactory(cellData -> {
            ConnectionHistory connection = cellData.getValue();
            User user = connection.getUser();
            if (user != null) {
                return new SimpleStringProperty(user.getNom() + " " + user.getPrenom());
            } else {
                return new SimpleStringProperty("--");
            }
        });

        userEmailColumn.setCellValueFactory(cellData -> {
            ConnectionHistory connection = cellData.getValue();
            User user = connection.getUser();
            if (user != null) {
                return new SimpleStringProperty(user.getEmail());
            } else {
                return new SimpleStringProperty("--");
            }
        });

        userTypeColumn.setCellValueFactory(cellData -> {
            ConnectionHistory connection = cellData.getValue();
            User user = connection.getUser();
            if (user != null) {
                return new SimpleStringProperty(user.getType_utilisateur());
            } else {
                return new SimpleStringProperty("--");
            }
        });

        dateColumn.setCellValueFactory(cellData -> {
            ConnectionHistory connection = cellData.getValue();
            Date connectionDate = connection.getConnectionDate();
            if (connectionDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                return new SimpleStringProperty(dateFormat.format(connectionDate));
            } else {
                return new SimpleStringProperty("--");
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
            if (newVal == null || newVal.getId() == -1) {
                // Show all connections
                connectionHistoryTable.setItems(FXCollections.observableArrayList(allConnections));
            } else {
                // Filter to show only the selected user's connections
                List<ConnectionHistory> filteredConnections = allConnections.stream()
                        .filter(conn -> conn.getUserId().equals(newVal.getId()))
                        .toList();
                connectionHistoryTable.setItems(FXCollections.observableArrayList(filteredConnections));
            }
        });

        // Setup dynamic search functionality
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsersBySearchText(newValue);
        });

        // Load data
        loadData();
    }

    private void loadData() {
        // Create table if it doesn't exist
        connectionHistoryService.createTableIfNotExists();

        // Get all connection history with user details
        allConnections = connectionHistoryService.getAllConnectionHistory();

        // Debug output to console
        System.out.println("Loaded " + allConnections.size() + " connection records");
        for (ConnectionHistory conn : allConnections) {
            System.out.println("Connection ID: " + conn.getId() + ", User ID: " + conn.getUserId() + ", Date: " + conn.getConnectionDate());
            if (conn.getUser() != null) {
                System.out.println("  User: " + conn.getUser().getNom() + " " + conn.getUser().getPrenom());
            } else {
                System.out.println("  User object is null");
            }
        }

        // Update UI
        connectionHistoryTable.setItems(FXCollections.observableArrayList(allConnections));
        totalConnectionsLabel.setText("Total des connexions: " + allConnections.size());

        // Setup filter combobox
        List<User> allUsers = userService.getAllUsers();

        // Add "All Users" option at the top
        User allUsersOption = new User();
        allUsersOption.setId(-1); // Special ID to indicate "All Users"
        allUsersOption.setNom("Tous");
        allUsersOption.setPrenom("les utilisateurs");
        allUsersOption.setEmail("");

        allUserOptions = FXCollections.observableArrayList();
        allUserOptions.add(allUsersOption);
        allUserOptions.addAll(allUsers);

        userFilterComboBox.setItems(allUserOptions);
        userFilterComboBox.getSelectionModel().selectFirst();

        // Ensure the table is properly showing all connections
        connectionHistoryTable.refresh();
    }

    @FXML
    private void refreshData() {
        loadData();
    }


    /**
     * Filter users in the combo box based on search text
     * @param searchText The text to search for in user names and emails
     */
    private void filterUsersBySearchText(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search field is empty, show all users
            userFilterComboBox.setItems(allUserOptions);
            return;
        }

        // Convert search text to lowercase for case-insensitive comparison
        String lowerCaseSearchText = searchText.toLowerCase();

        // Filter users based on search text
        ObservableList<User> filteredUsers = allUserOptions.filtered(user -> {
            // Always include the "All Users" option
            if (user.getId() == -1) return true;

            // Check if user name or email contains the search text
            String fullName = (user.getNom() + " " + user.getPrenom()).toLowerCase();
            String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

            return fullName.contains(lowerCaseSearchText) || email.contains(lowerCaseSearchText);
        });

        userFilterComboBox.setItems(filteredUsers);

        // If there are filtered results, show the dropdown
        if (!filteredUsers.isEmpty()) {
            userFilterComboBox.show();
        }
    }

    @FXML
    private void navigateToUserList() {
        Router.navigateTo("/Admin/UserList.fxml");
    }
}
