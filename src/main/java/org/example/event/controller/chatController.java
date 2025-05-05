package org.example.event.controller;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;


public class chatController {

    @FXML
    private VBox chatBox;

    @FXML
    private TextField userInput;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    public void handleSend() {
        String message = userInput.getText();
        if (message == null || message.isBlank()) return;

        chatBox.getChildren().add(createMessageBubble("You: " + message));
        userInput.clear();
        scrollPane.setVvalue(1.0);

        new Thread(() -> {
            String response = callGeminiAPI(message);
            javafx.application.Platform.runLater(() -> {
                chatBox.getChildren().add(createMessageBubble("Gemini: " + response));
                scrollPane.setVvalue(1.0);
            });
        }).start();
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

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());


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
    public String generateEventDescription(String eventName) {
        if (eventName == null || eventName.isBlank()) return "Nom d'événement invalide.";
        String prompt = "Tu es un assistant d'organisation d'événements. Génère une description tres courte de 2 lignes  attrayante, concise et professionnelle pour un événement nommé : \"" + eventName + "\".";
        return callGeminiAPI(prompt);
    }

}