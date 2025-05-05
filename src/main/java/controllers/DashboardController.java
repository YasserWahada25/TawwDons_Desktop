package controllers;

import javafx.fxml.FXML;
import utils.Navigation;

public class DashboardController {

    @FXML
    public void initialize() {
        // Simple initialization - no statistics to load
        System.out.println("Dashboard initialized");
    }
    
    @FXML
    private void retourUserList() {
        Navigation.goTo("/Admin/UserList.fxml");
    }
}
