package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import models.User;
import services.ConnectionHistoryService;
import services.UserService;
import utils.Router;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label adminUsersLabel;
    
    @FXML private Label totalConnectionsLabel;
    @FXML private Label todayConnectionsLabel;
    @FXML private Label weekConnectionsLabel;
    @FXML private Label monthConnectionsLabel;
    
    @FXML private BarChart<String, Number> connectionsChart;
    
    private final UserService userService = new UserService();
    private final ConnectionHistoryService connectionHistoryService = new ConnectionHistoryService();
    
    @FXML
    public void initialize() {
        // Load data when the view is initialized
        loadData();
    }
    
    @FXML
    private void refreshData() {
        loadData();
    }
    
    private void loadData() {
        loadUserStatistics();
        loadConnectionStatistics();
        createConnectionsChart();
    }
    
    private void loadUserStatistics() {
        // Get all users
        List<User> allUsers = userService.getAllUsers();
        
        // Count total users
        int totalUsers = allUsers.size();
        totalUsersLabel.setText(String.valueOf(totalUsers));
        
        // Count active users (not banned)
        int activeUsers = 0;
        int bannedUsers = 0;
        int adminUsers = 0;
        
        for (User user : allUsers) {
            if (user.isBanned()) {
                bannedUsers++;
            } else {
                activeUsers++;
            }
            
            // Check if user is admin
            if (user.getRoles() != null && 
                (user.getRoles().contains("ADMIN") || 
                 user.getRoles().contains("ROLE_ADMIN") || 
                 "admin".equalsIgnoreCase(user.getType_utilisateur()))) {
                adminUsers++;
            }
        }
        
        activeUsersLabel.setText(String.valueOf(activeUsers));
        bannedUsersLabel.setText(String.valueOf(bannedUsers));
        adminUsersLabel.setText(String.valueOf(adminUsers));
    }
    
    private void loadConnectionStatistics() {
        // Get all connections
        int totalConnections = connectionHistoryService.getTotalConnectionsCount();
        totalConnectionsLabel.setText(String.valueOf(totalConnections));
        
        // Get today's date
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        
        // Get connections for today
        int todayConnections = connectionHistoryService.getConnectionsCountSince(today);
        todayConnectionsLabel.setText(String.valueOf(todayConnections));
        
        // Get connections for this week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        Date startOfWeek = calendar.getTime();
        int weekConnections = connectionHistoryService.getConnectionsCountSince(startOfWeek);
        weekConnectionsLabel.setText(String.valueOf(weekConnections));
        
        // Get connections for this month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = calendar.getTime();
        int monthConnections = connectionHistoryService.getConnectionsCountSince(startOfMonth);
        monthConnectionsLabel.setText(String.valueOf(monthConnections));
    }
    
    private void createConnectionsChart() {
        // Clear previous data
        connectionsChart.getData().clear();
        
        // Create a series for the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Connexions");
        
        // Add data points for today, this week, and this month
        series.getData().add(new XYChart.Data<>("Aujourd'hui", Integer.parseInt(todayConnectionsLabel.getText())));
        series.getData().add(new XYChart.Data<>("Cette semaine", Integer.parseInt(weekConnectionsLabel.getText())));
        series.getData().add(new XYChart.Data<>("Ce mois", Integer.parseInt(monthConnectionsLabel.getText())));
        series.getData().add(new XYChart.Data<>("Total", Integer.parseInt(totalConnectionsLabel.getText())));
        
        // Add the series to the chart
        connectionsChart.getData().add(series);
    }
    
    @FXML
    private void navigateToUserList() {
        Router.navigateTo("/Admin/UserList.fxml");
    }
}
