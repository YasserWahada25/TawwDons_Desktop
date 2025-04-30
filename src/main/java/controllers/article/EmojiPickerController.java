package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Window;
import services.CommentReactionService;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class EmojiPickerController implements Initializable {

    @FXML
    private FlowPane flow;

    private int userId;
    private int commentId;
    private CommentReactionService commentReactionService;
    private Runnable onClose; // callback pour mettre à jour l'interface

    public void init(int userId, int commentId, CommentReactionService service, Runnable onClose) {
        this.userId = userId;
        this.commentId = commentId;
        this.commentReactionService = service;
        this.onClose = onClose;

        flow.getChildren().clear();
        flow.setHgap(10);
        flow.setVgap(10);
        flow.setAlignment(Pos.CENTER);
        flow.setPadding(new Insets(10));

        String[] emojiFiles = {
                "like.png", "love.png", "haha.png", "wow.png", "sad.png"
        };

        for (String fileName : emojiFiles) {
            InputStream is = getClass().getResourceAsStream("/emojis/" + fileName);
            if (is == null) {
                System.err.println("❌ Image non trouvée : /emojis/" + fileName);
                continue;
            }

            Image emojiImage = new Image(is);
            ImageView emojiView = new ImageView(emojiImage);
            emojiView.setFitWidth(32);
            emojiView.setFitHeight(32);
            emojiView.setPreserveRatio(true);

            Button btn = new Button();
            btn.setGraphic(emojiView);
            btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                commentReactionService.react(userId, commentId, fileName);
                if (onClose != null) onClose.run();

                Window window = btn.getScene().getWindow();
                if (window != null) window.hide();
            });

            flow.getChildren().add(btn);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // L'initialisation dynamique se fait dans `init()`
    }
}
