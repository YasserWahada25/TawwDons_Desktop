package models;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public class ChatController {

    @FXML private VBox chatBox;
    @FXML private TextField chatInput;
    @FXML private ScrollPane scrollPane;

    @FXML
    private void envoyer() {
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        // Affichage utilisateur
        ajouterBulle(userMessage, "#ecf0f1", Pos.BASELINE_RIGHT);

        // Réponse
        String reponse = genererReponse(userMessage.toLowerCase());
        ajouterBulle(reponse, "#dff9fb", Pos.BASELINE_LEFT);

        chatInput.clear();

        // Scroll automatique
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    private void ajouterBulle(String message, String couleur, Pos align) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10; -fx-padding: 10;");

        HBox hbox = new HBox(label);
        hbox.setAlignment(align);
        chatBox.getChildren().add(hbox);
    }

    private String genererReponse(String question) {
        Map<String, String> reponses = Map.ofEntries(
                Map.entry("stress", "Respire, médite un peu, ou fais une pause 🎧"),
                Map.entry("anxieux", "Parle à quelqu’un, respire profondément 🤝"),
                Map.entry("sommeil", "Essaie d’éteindre les écrans 1h avant de dormir 😴"),
                Map.entry("concentration", "Fais des pauses régulières pour rester focus 🎯"),
                Map.entry("sport", "Un peu d’exercice stimule le cerveau 🏃‍♂️"),
                Map.entry("eau", "Boire de l’eau t’aide à rester concentré 💧"),
                Map.entry("motivation", "Tu es capable de grandes choses 💪"),
                Map.entry("bonjour", "Salut ! Pose-moi une question sur ta santé ou ton étude 😊"),
                Map.entry("salut", "Hello ! Je suis là pour t’aider ✨"),
                Map.entry("merci", "Avec plaisir ! 😊"),
                Map.entry("question", "Lis-la attentivement et identifie les mots clés !"),
                Map.entry("calme", "Le calme est la clé de la réussite 🌿"),
                Map.entry("révision", "Révise avec méthode et régularité 📘"),
                Map.entry("examens", "Prépare un planning et reste confiant 📅"),
                Map.entry("fatigue", "Prends une pause, hydrate-toi et dors bien 😌")
        );


        return reponses.entrySet().stream()
                .filter(entry -> question.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Je ne suis pas sûr de comprendre... peux-tu reformuler ?");
    }
}
