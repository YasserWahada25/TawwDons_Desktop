package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Router;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;


public class ChatBotController {

    @FXML
    private VBox chatBox;

    @FXML
    private TextField chatInput;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button sendButton;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        // Initialisation du chat
        setupChat();
    }

    private void setupChat() {
        // Ajouter un message de bienvenue
        addMessage("Assistant", "Bonjour ! Je suis votre assistant AI. Comment puis-je vous aider ?");
    }

    @FXML
    public void envoyer() {
        String message = chatInput.getText();
        if (message == null || message.isBlank()) return;

        chatBox.getChildren().add(createMessageBubble("You: " + message));
        chatInput.clear();
        scrollPane.setVvalue(1.0);

        new Thread(() -> {
            String response = callGeminiAPI(message);
            javafx.application.Platform.runLater(() -> {
                chatBox.getChildren().add(createMessageBubble("Gemini: " + response));
                scrollPane.setVvalue(1.0);
            });
        }).start();
    }

    @FXML
    private void handleSendButtonClick() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            // Ajouter le message de l'utilisateur
            addMessage("Vous", message);
            
            // Simuler une réponse de l'assistant
            String response = "Je suis désolé, je ne peux pas encore répondre à vos questions. Cette fonctionnalité est en cours de développement.";
            addMessage("Assistant", response);
            
            // Vider le champ de message
            chatInput.clear();
        }
    }

    @FXML
    private void handleBackButtonClick() {
        // Retourner à la page précédente
        Router.navigateTo("/offre/AddOffre.fxml");
    }

    private TextFlow createMessageBubble(String msg) {
        Text text = new Text(msg);
        TextFlow bubble = new TextFlow(text);
        bubble.setStyle("-fx-background-color: #e1e1e1; -fx-padding: 8; -fx-background-radius: 10;");
        return bubble;
    }

    private String callGeminiAPI(String prompt) {
        String apiKey = "AIzaSyCGvIXkpsIwFejR_3h9W_aqz20WFaVqwzc"; // sécurise dans un vrai projet !
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + apiKey;

        // Configuration des timeouts pour OkHttp
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Création du corps JSON
        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));

        JSONObject json = new JSONObject()
                .put("contents", new JSONArray().put(message));

        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString());

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Erreur: " + response.code() + " - " + response.message();
            }

            String responseBody = response.body().string();
            JSONObject result = new JSONObject(responseBody);

            // Vérification de la présence des champs attendus
            if (!result.has("candidates") || result.getJSONArray("candidates").length() == 0) {
                return "Erreur: Format de réponse inattendu";
            }

            String answer = result
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            return answer;
        } catch (SocketTimeoutException e) {
            return "Erreur: Délai d'attente dépassé. Veuillez réessayer.";
        } catch (IOException e) {
            return "Erreur réseau: " + e.getMessage();
        } catch (Exception e) {
            return "Erreur API: " + e.getMessage();
        }
    }

    private void addMessage(String sender, String message) {
        // Créer un message dans le chat
        javafx.scene.control.Label messageLabel = new javafx.scene.control.Label(sender + ": " + message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-padding: 10; -fx-background-color: " + 
                            (sender.equals("Vous") ? "#e3f2fd" : "#f5f5f5") + 
                            "; -fx-background-radius: 5;");
        
        // Ajouter le message au conteneur
        chatBox.getChildren().add(messageLabel);
        
        // Faire défiler vers le bas
        chatBox.setPrefHeight(chatBox.getHeight() + messageLabel.getHeight());
    }
}