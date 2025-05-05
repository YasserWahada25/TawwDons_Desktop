package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.ConnectionHistory;
import models.User;
import services.ConnectionHistoryService;
import services.UserService;
import utils.Router;
import utils.SessionManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConnectionHistoryController {

    @FXML private TableView<ConnectionHistory> connectionHistoryTable;
    @FXML private TableColumn<ConnectionHistory, String> dateColumn;
    
    @FXML private TableView<User> connectedUsersTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userTypeColumn;
    
    @FXML private Label userInfoLabel;
    @FXML private Label totalConnectionsLabel;
    
    private final ConnectionHistoryService connectionHistoryService = new ConnectionHistoryService();
    private final UserService userService = new UserService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    @FXML
    public void initialize() {
        // Set up connection history table columns
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
        
        // Set up connected users table columns
        userNameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.getNom() + " " + user.getPrenom());
        });
        
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type_utilisateur"));
        
        // Load data for current user
        loadDataForCurrentUser();
    }
    
    private void loadDataForCurrentUser() {
        User currentUser = sessionManager.getCurrentUser();
        
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté", 
                     "Veuillez vous connecter pour voir votre historique de connexion.");
            return;
        }
        
        // Update user info label
        userInfoLabel.setText("Historique de connexion pour: " + currentUser.getNom() + " " + 
                             currentUser.getPrenom() + " (" + currentUser.getEmail() + ")");
        
        // Load connection history for current user
        List<ConnectionHistory> connections = connectionHistoryService.getConnectionHistoryForUser(currentUser.getId());
        
        if (connections.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune connexion", 
                     "Aucun historique de connexion trouvé pour cet utilisateur.");
        }
        
        connectionHistoryTable.setItems(FXCollections.observableArrayList(connections));
        totalConnectionsLabel.setText("Total des connexions: " + connections.size());
        
        // Load connected users for current user
        List<User> connectedUsers = connectionHistoryService.getConnectedUsersForUser(currentUser.getId());
        
        if (connectedUsers.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun utilisateur connecté", 
                     "Aucun autre utilisateur n'était connecté en même temps que vous.");
        }
        
        connectedUsersTable.setItems(FXCollections.observableArrayList(connectedUsers));
    }
    
    @FXML
    private void refreshData() {
        loadDataForCurrentUser();
    }
    
    @FXML
    private void navigateToHome() {
        User currentUser = sessionManager.getCurrentUser();
        
        if (currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
                Parent root = loader.load();
                
                HomeController homeController = loader.getController();
                homeController.updateUI(currentUser);
                
                Stage stage = (Stage) userInfoLabel.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement", 
                         "Impossible de charger la page d'accueil.");
            }
        } else {
            Router.navigateTo("/login.fxml");
        }
    }
    
    @FXML
    private void navigateToDashboard() {
        Router.navigateTo("/views/Dashboard.fxml");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
