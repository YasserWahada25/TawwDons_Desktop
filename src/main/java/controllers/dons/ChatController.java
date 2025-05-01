package controllers.dons;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Messagerie;
import services.MessagerieService;
import websocket.ChatSocketClient; // CORRIGÉ

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatController {

    @FXML private VBox messagesBox;
    @FXML private TextField messageField;
    @FXML private Button btnEnvoyer;

    private int demandeId;
    private int expediteurId;
    private int destinataireId;
    private String role;

    private final MessagerieService messagerieService = new MessagerieService();
    private ChatSocketClient socketClient;

    @FXML
    public void initialize() {
        Image sendIcon = new Image(getClass().getResourceAsStream("/images/send-message.png"));
        ImageView imageView = new ImageView(sendIcon);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        btnEnvoyer.setGraphic(imageView);
        btnEnvoyer.setText("");

        btnEnvoyer.setOnAction(e -> envoyerMessage());
    }

    public void setParticipants(int demandeId, int userId, String role, int autreUserId) {
        this.demandeId = demandeId;
        this.expediteurId = userId;
        this.role = role;
        this.destinataireId = autreUserId;

        // Nouvelle instance avec URI passé dans constructeur personnalisé
        socketClient = new ChatSocketClient();
        socketClient.connect("ws://localhost:8080/chat");

        socketClient.setOnMessageReceived(message -> Platform.runLater(this::afficherMessages));

        afficherMessages();
    }

    private void envoyerMessage() {
        String contenu = messageField.getText().trim();
        if (!contenu.isEmpty()) {
            Messagerie message = new Messagerie(expediteurId, destinataireId, demandeId, contenu);
            messagerieService.envoyerMessage(message);
            socketClient.send("update"); // Notifie les autres clients de recharger
            afficherMessages();
            messageField.clear();
        }
    }

    private void afficherMessages() {
        messagesBox.getChildren().clear();
        List<Messagerie> messages = messagerieService.getMessagesByDemandeId(demandeId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Messagerie msg : messages) {
            Label label = new Label();
            label.setWrapText(true);
            label.setStyle("-fx-padding: 10; -fx-background-radius: 15; -fx-font-size: 14px; -fx-text-fill: white;");
            label.setText((msg.getExpediteurId() == expediteurId ? "Vous" : "Lui") + " : " +
                    msg.getContenu() + "\n" + msg.getDateEnvoi().format(formatter));

            HBox messageContainer = new HBox(label);
            messageContainer.setPrefWidth(Double.MAX_VALUE);
            messageContainer.setFillHeight(true);

            if (msg.getExpediteurId() == expediteurId) {
                label.setStyle(label.getStyle() + "-fx-background-color: #0084FF;");
                messageContainer.setStyle("-fx-alignment: CENTER-RIGHT;");
            } else {
                label.setStyle(label.getStyle() + "-fx-background-color: #E4E6EB; -fx-text-fill: black;");
                messageContainer.setStyle("-fx-alignment: CENTER-LEFT;");
            }

            messagesBox.getChildren().add(messageContainer);
        }
    }
}
