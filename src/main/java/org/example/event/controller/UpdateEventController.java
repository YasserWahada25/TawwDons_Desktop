package org.example.event.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.event.model.event;
import org.example.event.service.eventService;

import java.io.File;

public class UpdateEventController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView eventImageView;





    private eventService eventService = new eventService();
    private Stage dialogStage;
    private event currentEvent;
    private String newImagePath;


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEvent(event event) {
        this.currentEvent = event;
        populateFields();
    }

    private void populateFields() {
        nomField.setText(currentEvent.getNom());
        descriptionField.setText(currentEvent.getDescription());

        if (currentEvent.getImage() != null && !currentEvent.getImage().isEmpty()) {
            try {
                Image image = new Image(currentEvent.getImage());
                eventImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            newImagePath = selectedFile.toURI().toString();
            eventImageView.setImage(new Image(newImagePath));
        }
    }

    @FXML
    private void handleUpdate() {
        if (isInputValid()) {
            try {
                // Mise à jour de l'événement
                currentEvent.setNom(nomField.getText());
                currentEvent.setDescription(descriptionField.getText());

                if (newImagePath != null) {
                    currentEvent.setImage(newImagePath);
                }

                eventService.modifier(currentEvent);
                dialogStage.close();
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de la mise à jour", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().isEmpty()) {
            errorMessage.append("Nom invalide!\n");
        }
        if (descriptionField.getText() == null || descriptionField.getText().isEmpty()) {
            errorMessage.append("Description invalide!\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showErrorAlert("Champs invalides","Veuillez corriger les champs invalides", errorMessage.toString());
            return false;
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