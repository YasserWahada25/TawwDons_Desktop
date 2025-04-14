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

public class AddEventController {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView eventImageView;

    private eventService eventService = new eventService();
    private Stage dialogStage;
    private String imagePath;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Stocker le chemin de l'image
                imagePath = selectedFile.toURI().toString();

                // Afficher l'aperçu
                Image image = new Image(imagePath);
                eventImageView.setImage(image);
            } catch (Exception e) {
                showErrorAlert("Erreur", "Impossible de charger l'image", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAdd() {
        if (isInputValid()) {
            event newEvent = new event(
                    0, // ID auto-généré
                    nomField.getText(),
                    descriptionField.getText(),
                    imagePath
            );

            try {
                eventService.ajouter(newEvent);
                dialogStage.close();
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de l'ajout", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (nomField.getText() == null || nomField.getText().isEmpty()) {
            errorMessage += "Nom invalide!\n";
        }
        if (imagePath == null || imagePath.isEmpty()) {
            errorMessage += "Image requise!\n";
        }
        if (descriptionField.getText() == null || descriptionField.getText().isEmpty()) {
            errorMessage += "Description invalide!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showErrorAlert("Champs invalides", "Veuillez corriger les champs", errorMessage);
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