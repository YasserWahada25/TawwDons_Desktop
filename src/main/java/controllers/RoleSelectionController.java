package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import models.User;
import java.util.function.Consumer;

public class RoleSelectionController {

    @FXML private RadioButton donorRadio;
    @FXML private RadioButton beneficiaryRadio;
    @FXML private RadioButton professionalRadio;
    @FXML private ToggleGroup roleGroup;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    
    private Consumer<String> roleCallback;
    private User userToCreate;
    
    @FXML
    public void initialize() {
        // Set donor as default selection
        donorRadio.setSelected(true);
    }
    
    /**
     * Sets the callback to be called when a role is selected
     * 
     * @param userToCreate The user data to associate with the role
     * @param callback The callback to call with the selected role
     */
    public void setData(User userToCreate, Consumer<String> callback) {
        this.userToCreate = userToCreate;
        this.roleCallback = callback;
    }
    
    /**
     * Handles the confirm button click
     */
    @FXML
    private void handleConfirm() {
        if (roleGroup.getSelectedToggle() == null) {
            errorLabel.setText("Veuillez sélectionner un rôle.");
            return;
        }
        
        String selectedRole;
        if (donorRadio.isSelected()) {
            selectedRole = "ROLE_DONNEUR";
        } else if (beneficiaryRadio.isSelected()) {
            selectedRole = "ROLE_BENEFICIAIRE"; 
        } else if (professionalRadio.isSelected()) {
            selectedRole = "ROLE_PROFESSIONNEL";
        } else {
            errorLabel.setText("Veuillez sélectionner un rôle.");
            return;
        }
        
        // Call the callback with the selected role
        if (roleCallback != null) {
            roleCallback.accept(selectedRole);
        }
        
        // Close the dialog
        closeDialog();
    }
    
    /**
     * Handles the cancel button click
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    /**
     * Closes the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
} 