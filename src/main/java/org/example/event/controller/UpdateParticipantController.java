package org.example.event.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.event.model.participant;
import org.example.event.service.participantService;

import java.sql.SQLException;

public class UpdateParticipantController {
    @FXML
    private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;

    private Stage dialogStage;
    private participant currentParticipant;
    private final org.example.event.service.participantService participantService = new participantService();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParticipant(participant participant) {
        this.currentParticipant = participant;
        populateFields();
    }

    private void populateFields() {
        if (currentParticipant != null) {
            nomField.setText(currentParticipant.getNom());
            prenomField.setText(currentParticipant.getPrenom());
            emailField.setText(currentParticipant.getEmail());
            numTelField.setText(String.valueOf(currentParticipant.getNumtel()));
        }
    }

    @FXML
    private void handleUpdate() {
        if (validateInput()) {
            try {
                updateParticipantFromInput();
                participantService.modifier(currentParticipant);
                dialogStage.close();
            } catch (SQLException e) {
                showAlert("Erreur de base de données",
                        "Échec de la mise à jour du participant",
                        e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void updateParticipantFromInput() {
        if (currentParticipant != null) {
            currentParticipant.setNom(nomField.getText().trim());
            currentParticipant.setPrenom(prenomField.getText().trim());
            currentParticipant.setEmail(emailField.getText().trim());
            currentParticipant.setNumtel(Integer.parseInt( numTelField.getText().trim()));
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        validateField(nomField, "Nom", errors);
        validateField(prenomField, "Prénom", errors);
        validateEmail(emailField, "Email", errors);
        validatePhoneNumber(numTelField, "Numéro de téléphone", errors);

        if (errors.length() > 0) {
            showAlert("Champs invalides",
                    "Veuillez corriger les erreurs suivantes",
                    errors.toString(),
                    Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void validateField(TextField field, String fieldName, StringBuilder errors) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            errors.append("- ").append(fieldName).append(" est obligatoire\n");
        }
    }

    private void validateEmail(TextField emailField, String fieldName, StringBuilder errors) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errors.append("- ").append(fieldName).append(" est obligatoire\n");
        } else if (!email.matches("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$")) {
            errors.append("- Format d'email invalide\n");
        }
    }

    private void validatePhoneNumber(TextField phoneField, String fieldName, StringBuilder errors) {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            errors.append("- ").append(fieldName).append(" est obligatoire\n");
        } else if (!phone.matches("^[0-9]{8,15}$")) {
            errors.append("- Le numéro doit contenir 8 à 15 chiffres\n");
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
