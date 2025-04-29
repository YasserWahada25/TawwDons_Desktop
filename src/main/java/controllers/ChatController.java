package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import utils.HttpUtils;
import javafx.application.Platform;

import java.io.IOException;

public class ChatController {

    @FXML
    private VBox chatBox;


    @FXML
    private TextField chatInput;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void envoyer() {
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        ajouterBulle(userMessage, "#ecf0f1", Pos.BASELINE_RIGHT); // Affiche le message de l'utilisateur

        // Afficher "Chargement..."
        Label loadingLabel = new Label("⏳ Chargement...");
        loadingLabel.setWrapText(true);
        loadingLabel.setMaxWidth(380);
        loadingLabel.setStyle("-fx-background-color: #ffeaa7; -fx-background-radius: 10; -fx-padding: 10; -fx-font-style: italic;");
        HBox loadingBox = new HBox(loadingLabel);
        loadingBox.setAlignment(Pos.BASELINE_LEFT);
        chatBox.getChildren().add(loadingBox);

        scrollPane.layout();
        scrollPane.setVvalue(1.0);

        // Lancer appel API
        new Thread(() -> {
            try {
                String reponse = genererReponse(userMessage);
                javafx.application.Platform.runLater(() -> {
                    chatBox.getChildren().remove(loadingBox); // Supprimer "Chargement..."
                    ajouterBulle(reponse, "#dff9fb", Pos.BASELINE_LEFT); // Ajouter réponse
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    chatBox.getChildren().remove(loadingBox);
                    ajouterBulle("Erreur de communication avec le serveur.", "#ffcccc", Pos.BASELINE_LEFT);
                });
            }
        }).start();

        chatInput.clear();
    }

    @FXML
    private void retour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/User.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ajouterBulle(String message, String couleur, Pos align) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 14px;");

        HBox hbox = new HBox(label);
        hbox.setAlignment(align);
        hbox.setSpacing(5);

        chatBox.getChildren().add(hbox);

        // -- Animation FadeIn + SlideIn --
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), hbox);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(500), hbox);
        slide.setFromX(align == Pos.BASELINE_RIGHT ? 100 : -100); // à droite ou à gauche
        slide.setToX(0);

        javafx.animation.ParallelTransition animation = new javafx.animation.ParallelTransition(fade, slide);
        animation.play();

        // Scroll automatique vers le bas
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }


    private String genererReponse(String question) throws IOException {
        return HttpUtils.post(question);
    }
}
