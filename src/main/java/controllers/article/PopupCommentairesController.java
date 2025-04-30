// src/main/java/controllers/article/PopupCommentairesController.java
package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Commentaire;
import services.CommentaireService;
import services.CommentReportService;
import services.EmailService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PopupCommentairesController implements Initializable {

    @FXML private VBox commentList;
    @FXML private Button closeBtn;

    private int articleId;
    private final CommentaireService commentaireService   = new CommentaireService();
    private final CommentReportService reportService      = new CommentReportService();
    private final EmailService emailService               = new EmailService();


    public void setArticleId(int articleId) {
        this.articleId = articleId;
        loadComments();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeBtn.setOnAction(e ->
                ((Stage) closeBtn.getScene().getWindow()).close()
        );
    }


    private void loadComments() {
        commentList.getChildren().clear();

        List<Commentaire> list = commentaireService.getCommentairesByArticleId(articleId);

        for (Commentaire c : list) {

            Label authorLbl = new Label(c.getUser().getPrenom() + " " + c.getUser().getNom());
            authorLbl.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

            Label body = new Label(c.getContent());
            body.setWrapText(true);
            body.setStyle("-fx-text-fill: #444; -fx-font-size: 13px;");

            Region hSpacer = new Region();
            HBox.setHgrow(hSpacer, Priority.ALWAYS);

            ImageView offIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/off1.png"), 25, 20, true, true)
            );
            Button btnDisable = new Button();
            btnDisable.setGraphic(offIcon);
            btnDisable.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btnDisable.setVisible("valide".equalsIgnoreCase(c.getEtat()));
            btnDisable.setOnAction(ev -> {
                c.setEtat("non valide");
                commentaireService.modifier(c);
                emailService.sendCommentDisabled(
                        c.getUser().getEmail(),
                        c.getArticle().getTitre(),
                        c.getContent()
                );
                loadComments();
            });

            boolean isFlagged = reportService.hasReportForComment(c.getId());
            ImageView delIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/trash.png"), 20, 20, true, true)
            );
            Button btnDelete = new Button();
            btnDelete.setGraphic(delIcon);
            btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btnDelete.setVisible(isFlagged);
            btnDelete.setOnAction(ev -> {

                emailService.sendCommentDeleted(
                        c.getUser().getEmail(),
                        c.getArticle().getTitre(),
                        c.getContent()
                );
                reportService.deleteReportsForComment(c.getId());
                commentaireService.supprimer(c.getId());

                loadComments();
            });
            VBox icons = new VBox(2, btnDisable, btnDelete);
            icons.setAlignment(Pos.CENTER_RIGHT);
            HBox commentLine = new HBox(5, body, hSpacer, icons);
            commentLine.setAlignment(Pos.CENTER_LEFT);

            Label dateLbl = new Label(
                    c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            dateLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

            // 8) Assemblage final
            VBox item = new VBox(2, authorLbl, commentLine, dateLbl);
            item.setPadding(new Insets(5));
            item.setStyle("""
                -fx-background-color: #f9f9f9;
                -fx-border-color: #ddd;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
            """);
            commentList.getChildren().add(item);
        }
    }
}
