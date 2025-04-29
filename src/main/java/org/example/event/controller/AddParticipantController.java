package org.example.event.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.event.model.participant;
import org.example.event.service.participantService;

import java.sql.SQLException;

public class AddParticipantController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    private int eventId;

    private Stage dialogStage;
    private final participantService participantService = new participantService();
public void setEventId(int eventId) {
    this.eventId=eventId;
}
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                participant newParticipant = createParticipantFromInput();
                newParticipant.setId(eventId);
                participantService.ajouter(newParticipant);
                dialogStage.close();
            } catch (SQLException e) {
                showErrorAlert("Erreur de sauvegarde", "Échec de l'ajout du participant", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private participant createParticipantFromInput() {
        return new participant(
                nomField.getText(),
                prenomField.getText(),
                emailField.getText(),
                Integer.parseInt(numTelField.getText()),
                eventId
        );
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        validateField(nomField, "Nom", errors);
        validateField(prenomField, "Prénom", errors);
        validateEmailField(emailField, "Email", errors);
        validatePhoneField(numTelField, "Téléphone", errors);

        if (errors.length() > 0) {
            showErrorAlert("Champs invalides", "Veuillez corriger les erreurs :", errors.toString());
            return false;
        }
        return true;
    }

    private void validateField(TextField field, String fieldName, StringBuilder errors) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            errors.append("- ").append(fieldName).append(" est requis\n");
        }
    }

    private void validateEmailField(TextField field, String fieldName, StringBuilder errors) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            errors.append("- ").append(fieldName).append(" est requis\n");
            return;
        }

        if (!field.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errors.append("- ").append(fieldName).append(" doit être une adresse email valide\n");
        }
    }

    private void validatePhoneField(TextField field, String fieldName, StringBuilder errors) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            errors.append("- ").append(fieldName).append(" est requis\n");
            return;
        }

        if (!field.getText().matches("^[1-9]{8}$")) {
            errors.append("- ").append(fieldName).append(" doit être un numéro de téléphone valide (8-15 chiffres)\n");
        }
    }

    private void validateNumericField(TextField field, String fieldName, StringBuilder errors, boolean isInteger) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            errors.append("- ").append(fieldName).append(" est requis\n");
            return;
        }

        try {
            if (isInteger) {
                Integer.parseInt(field.getText());
            } else {
                Double.parseDouble(field.getText());
            }
        } catch (NumberFormatException e) {
            errors.append("- ").append(fieldName).append(" doit être un nombre valide\n");
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}