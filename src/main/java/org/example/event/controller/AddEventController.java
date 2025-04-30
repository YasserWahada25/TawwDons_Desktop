package org.example.event.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.event.model.categorie;
import org.example.event.model.event;
import org.example.event.model.categorie;
import org.example.event.model.event;
import org.example.event.service.categorieService;
import org.example.event.service.eventService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AddEventController {
    @FXML private TextField nomField;
    @FXML private ComboBox<categorie> categorieComboBox;
    @FXML private TextArea descriptionField;
    @FXML private ImageView eventImageView;

    private eventService eventService = new eventService();
    private categorieService categorieService = new categorieService();
    private Stage dialogStage;
    private String imagePath;

    @FXML
    public void initialize() {
        chargerCategories();
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
                imagePath = selectedFile.toURI().toString();
                Image image = new Image(imagePath);
                eventImageView.setImage(image);
            } catch (Exception e) {
                afficherAlerteErreur("Erreur", "Impossible de charger l'image", e.getMessage());
            }
        }
    }


    private void chargerCategories() {
        try {
            List<categorie> categories = categorieService.getList();

            // Vérifiez que les catégories ont des IDs valides
            System.out.println("Catégories chargées:");
            for (categorie c : categories) {
                System.out.println("ID: " + c.getId() + ", Nom: " + c.getType());
            }

            // Peupler la ComboBox
            categorieComboBox.setItems(FXCollections.observableArrayList(categories));

            // Configurer l'affichage pour ne montrer que le type
            categorieComboBox.setConverter(new StringConverter<categorie>() {
                @Override
                public String toString(categorie categorie) {
                    return categorie != null ? categorie.getType() : "";
                }

                @Override
                public categorie fromString(String string) {
                    return null; // Non nécessaire pour la sélection seule
                }
            });

            // Sélectionner la première valeur par défaut
            if (!categories.isEmpty()) {
                categorieComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            afficherAlerteErreur("Erreur", "Échec du chargement", e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        if (estDonneesValides()) {
            categorie selected = categorieComboBox.getSelectionModel().getSelectedItem();

            // Debug: Afficher la catégorie sélectionnée
            System.out.println("Catégorie sélectionnée - ID: " + selected.getId() + ", type: " + selected.getType());

            event nouvelEvent = new event(
                    0, // ID auto-généré
                    nomField.getText(),
                    descriptionField.getText(),
                    imagePath,
                    selected.getId() // Utilisation directe de l'ID
            );
  nouvelEvent.setCategorie_id (selected.getId());
            try {
                System.out.println("categorie_id"+ nouvelEvent.getCateegorie_id());
                eventService.ajouter(nouvelEvent);
                dialogStage.close();
            } catch (Exception e) {
                afficherAlerteErreur("Erreur", "Échec de l'ajout", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean estDonneesValides() {
        StringBuilder messageErreur = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            messageErreur.append("Nom invalide!\n");
        }
        if (imagePath == null || imagePath.isEmpty()) {
            messageErreur.append("Image requise!\n");
        }
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            messageErreur.append("Description invalide!\n");
        }
        if (categorieComboBox.getValue() == null) {
            messageErreur.append("Veuillez sélectionner une catégorie!\n");
        }

        if (messageErreur.length() == 0) {
            return true;
        } else {
            afficherAlerteErreur("Champs invalides", "Veuillez corriger les erreurs suivantes", messageErreur.toString());
            return false;
        }
    }

    private void afficherAlerteErreur(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}