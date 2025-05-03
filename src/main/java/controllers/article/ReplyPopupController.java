package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.CommentReply;
import models.Commentaire;
import models.User;
import services.CommentReplyService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class ReplyPopupController implements Initializable {
    @FXML private ListView<CommentReply> repliesList;
    @FXML private TextField replyInput;
    @FXML private Button btnSend, btnClose;

    private int commentId;
    private CommentReplyService replyService;
    private Runnable onUpdate;
    private User currentUser;

    /**
     * À appeler avant init(...) pour injecter l’utilisateur courant
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Initialise le popup avec l’ID du commentaire, le service et un callback de mise à jour.
     * currentUser doit avoir été injecté préalablement via setCurrentUser().
     */
    public void init(int commentId,
                     CommentReplyService service,
                     Runnable onUpdate) {
        this.commentId    = commentId;
        this.replyService = service;
        this.onUpdate     = onUpdate;

        // Configuration de l’affichage des replies
        repliesList.setCellFactory(lv -> new ListCell<>() {
            private final Label name    = new Label();
            private final Label content = new Label();
            private final VBox box      = new VBox(2, name, content);
            {
                name.setStyle("-fx-font-weight:bold;");
                content.setWrapText(true);
            }
            @Override
            protected void updateItem(CommentReply item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    User u = item.getUser();
                    name.setText(u.getPrenom() + " " + u.getNom());
                    content.setText(item.getContent());
                    setGraphic(box);
                }
            }
        });

        loadReplies();

        btnSend.setOnAction(e -> {
            String txt = replyInput.getText().trim();
            if (txt.isEmpty()) return;

            // Création de la reply
            CommentReply r = new CommentReply();
            Commentaire parent = new Commentaire();
            parent.setId(commentId);
            r.setCommentaire(parent);

            // Utiliser l’utilisateur injecté
            r.setUser(currentUser);

            r.setContent(txt);
            r.setCreatedAt(LocalDateTime.now());
            r.setEtat("valide");

            replyService.add(r);

            replyInput.clear();
            loadReplies();

            if (onUpdate != null) onUpdate.run();
        });

        btnClose.setOnAction(e ->
                ((Stage) btnClose.getScene().getWindow()).close()
        );
    }

    private void loadReplies() {
        List<CommentReply> list = replyService.getRepliesByCommentId(commentId);
        repliesList.getItems().setAll(list);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // pas d’action particulière au chargement FXML
    }
}
