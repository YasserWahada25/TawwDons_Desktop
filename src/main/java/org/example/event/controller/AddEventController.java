package org.example.event.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import okhttp3.*;
import org.example.event.model.Categorie;
import org.example.event.model.event;
import org.example.event.service.CategorieService;
import org.example.event.service.eventService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddEventController {

    @FXML private TextField nomField;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private TextArea descriptionField;
    @FXML private ImageView eventImageView;
    @FXML private DatePicker datePicker;
    @FXML private TextField locationField;
    @FXML private Button generateButton; // Bind via FXML avec onAction="#handleGenerateDescription"

    private eventService eventService = new eventService();
    private CategorieService categorieService = new CategorieService();
    private Stage dialogStage;
    private String imagePath;

    @FXML
    public void initialize() {
        chargerCategories();
        datePicker.setValue(LocalDate.now());
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
            imagePath = selectedFile.toURI().toString();
            eventImageView.setImage(new Image(imagePath));
        }
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.getList();
            categorieComboBox.setItems(FXCollections.observableArrayList(categories));
            categorieComboBox.setConverter(new StringConverter<>() {
                @Override public String toString(Categorie c) { return c != null ? c.getType() : ""; }
                @Override public Categorie fromString(String s) { return null; }
            });
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
            Categorie selected = categorieComboBox.getSelectionModel().getSelectedItem();
            event nouvelEvent = new event(
                    0,
                    nomField.getText(),
                    descriptionField.getText(),
                    imagePath,
                    selected.getId(),
                    datePicker.getValue(),
                    locationField.getText()
            );
            try {
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
        StringBuilder msg = new StringBuilder();
        if (nomField.getText().isBlank()) msg.append("Nom invalide!\n");
        if (imagePath == null || imagePath.isEmpty()) msg.append("Image requise!\n");
        if (descriptionField.getText().isBlank()) msg.append("Description invalide!\n");
        if (categorieComboBox.getValue() == null) msg.append("Catégorie manquante!\n");
        if (datePicker.getValue() == null) msg.append("Date manquante!\n");
        if (locationField.getText().isBlank()) msg.append("Lieu invalide!\n");

        if (msg.length() == 0) return true;

        afficherAlerteErreur("Champs invalides", "Veuillez corriger :", msg.toString());
        return false;
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

    @FXML
    private void handleMapSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/MapPopup.fxml"));
            Parent root = loader.load();
            MapPopupController controller = loader.getController();
            controller.setDialogStage(new Stage());
            controller.setAddressConsumer(address -> locationField.setText(address));

            Stage mapStage = new Stage();
            mapStage.initModality(Modality.APPLICATION_MODAL);
            mapStage.initOwner(dialogStage);
            mapStage.setScene(new Scene(root));
            mapStage.setTitle("Sélectionner un emplacement");
            mapStage.showAndWait();
        } catch (IOException e) {
            afficherAlerteErreur("Erreur", "Chargement de la carte impossible", e.getMessage());
        }
    }

    @FXML
    private void handleGenerateDescription() {
        String nom = nomField.getText();
        if (nom == null || nom.isBlank()) {
            afficherAlerteErreur("Nom requis", null, "Veuillez d'abord saisir un nom d'événement.");
            return;
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return callGeminiAPI(nom);
            }
        };

        task.setOnSucceeded(e -> descriptionField.setText(task.getValue()));
        task.setOnFailed(e -> descriptionField.setText("Erreur lors de la génération."));

        new Thread(task).start();
    }

    private String callGeminiAPI(String prompt) {
        String apiKey = "AIzaSyCGvIXkpsIwFejR_3h9W_aqz20WFaVqwzc";  // Remplacez ceci par une variable sécurisée dans un vrai projet
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + apiKey;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("parts", new JSONArray().put(new JSONObject().put("text",
                        "Génère une brève description pour un événement nommé \"" + prompt + "\".")));

        JSONObject json = new JSONObject().put("contents", new JSONArray().put(message));
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request.Builder().url(endpoint).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "Erreur: " + response.code() + " - " + response.message();
            JSONObject result = new JSONObject(response.body().string());

            return result.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        } catch (SocketTimeoutException e) {
            return "Temps dépassé. Veuillez réessayer.";
        } catch (IOException e) {
            return "Erreur réseau: " + e.getMessage();
        } catch (Exception e) {
            return "Erreur inattendue: " + e.getMessage();
        }
    }
}
