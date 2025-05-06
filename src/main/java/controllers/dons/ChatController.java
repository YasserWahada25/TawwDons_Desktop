package controllers.dons;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;              // ← import ajouté
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Messagerie;
import services.MessagerieService;
import websocket.ChatSocketClient;
import websocket.WebSocketLauncher;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ChatController {

    @FXML private Label lblInterlocuteur;       // ← nouveau Label
    @FXML private VBox messagesBox;
    @FXML private TextField messageField;
    @FXML private Button btnEnvoyer;

    private int demandeId;
    private int expediteurId;
    private int destinataireId;
    private ChatSocketClient socketClient;
    private final MessagerieService messagerieService = new MessagerieService();

    @FXML
    public void initialize() {
        Image ivImg = new Image(getClass().getResourceAsStream("/images/send-message.png"));
        ImageView iv = new ImageView(ivImg);
        iv.setFitWidth(24); iv.setFitHeight(24);
        btnEnvoyer.setGraphic(iv);
        btnEnvoyer.setText("");
        btnEnvoyer.setOnAction(e -> envoyerMessage());
    }

    /**
     * À appeler après chargement du FXML :
     *    - ajout du paramètre `nomInterlocuteur`
     */
    public void setParticipants(int demandeId,
                                int expediteurId,
                                String role,
                                int destinataireId,
                                String nomInterlocuteur) {
        this.demandeId      = demandeId;
        this.expediteurId   = expediteurId;
        this.destinataireId = destinataireId;

        // ← on positionne dynamiquement le nom
        lblInterlocuteur.setText(nomInterlocuteur);

        socketClient = new ChatSocketClient();
        socketClient.setOnMessageReceived(raw ->
                Platform.runLater(this::afficherMessages)
        );

        socketClient.connect(WebSocketLauncher.getWebSocketUri(demandeId));
        afficherMessages();
    }

    private void envoyerMessage() {
        String contenu = messageField.getText().trim();
        if (contenu.isEmpty()) return;

        // 1) Sauvegarde en base
        messagerieService.envoyerMessage(
                new Messagerie(expediteurId, destinataireId, demandeId, contenu)
        );
        // 2) Diffusion dans la room
        socketClient.send(contenu);
        // 3) Mise à jour locale
        afficherMessages();
        messageField.clear();
    }

    private void afficherMessages() {
        messagesBox.getChildren().clear();
        List<Messagerie> msgs = messagerieService.getMessagesByDemandeId(demandeId);
        msgs.sort(Comparator.comparing(Messagerie::getDateEnvoi));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (Messagerie msg : msgs) {
            boolean sent = msg.getExpediteurId() == expediteurId;
            Label bubble = new Label(msg.getContenu() + "\n" + msg.getDateEnvoi().format(fmt));
            bubble.getStyleClass().addAll("bubble", sent? "bubble-sent":"bubble-received");
            bubble.setMaxWidth(300);

            ImageView avatar = new ImageView(new Image(
                    getClass().getResourceAsStream(
                            sent? "/images/avatar1.png":"/images/avatar2.png"
                    )
            ));
            avatar.setFitWidth(24); avatar.setFitHeight(24);

            HBox container = new HBox(8);
            container.getStyleClass().add(
                    sent? "message-container-sent":"message-container-received"
            );
            if (sent) container.getChildren().setAll(bubble, avatar);
            else      container.getChildren().setAll(avatar, bubble);

            messagesBox.getChildren().add(container);
        }
    }
}
