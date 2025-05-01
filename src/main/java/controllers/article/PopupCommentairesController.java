package controllers.article;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Commentaire;
import services.CommentaireService;
import services.CommentReportService;
import services.EmailService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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

            // Désactiver
            ImageView offIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/off1.png"), 25, 20, true, true)
            );
            Button btnDisable = new Button("", offIcon);
            btnDisable.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btnDisable.setVisible("valide".equalsIgnoreCase(c.getEtat()));
            btnDisable.setOnAction(ev -> {
                c.setEtat("non valide");
                commentaireService.modifier(c);
                emailService.sendCommentDisabled(
                        c.getUser().getEmail(),
                        c.getUser().getPrenom() + " " + c.getUser().getNom(),
                        c.getContent()
                );
                loadComments();
            });

            ImageView delIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/trash.png"), 20, 20, true, true)
            );
            Button btnDelete = new Button("", delIcon);
            btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            boolean isFlagged = reportService.hasReportForComment(c.getId());
            btnDelete.setVisible(isFlagged);
            btnDelete.setOnAction(ev -> {
                // 1) Confirmation
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation de suppression");
                confirm.setHeaderText("Suppression du commentaire");
                confirm.setContentText("Voulez-vous vraiment supprimer ce commentaire ?");
                confirm.initOwner(commentList.getScene().getWindow());
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // 2) Suppression async
                    deleteCommentWithProgress(c);
                }
            });

            VBox icons = new VBox(2, btnDisable, btnDelete);
            icons.setAlignment(Pos.CENTER_RIGHT);

            HBox commentLine = new HBox(5, body, hSpacer, icons);
            commentLine.setAlignment(Pos.CENTER_LEFT);

            Label dateLbl = new Label(
                    c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            dateLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

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
/***
    private void deleteCommentWithProgress(Commentaire c) {
        // 1) loader modal
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(80, 80);
        Stage loaderStage = new Stage();
        loaderStage.initOwner(commentList.getScene().getWindow());
        loaderStage.initModality(Modality.APPLICATION_MODAL);
        loaderStage.setScene(new Scene(new StackPane(pi)));
        loaderStage.setResizable(false);
        loaderStage.setTitle("Suppression en cours…");
        loaderStage.show();

        // 2) tâche
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                reportService.deleteReportsForComment(c.getId());
                commentaireService.supprimer(c.getId());
                emailService.sendCommentDeleted(
                        c.getUser().getEmail(),
                        c.getUser().getPrenom() + " " + c.getUser().getNom(),
                        c.getContent()
                );
                return null;
            }
        };

        // 3) succès
        task.setOnSucceeded(evt -> {
            loaderStage.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "✅ Commentaire supprimé avec succès.");
            alert.initOwner(commentList.getScene().getWindow());
            alert.showAndWait();
            loadComments();
        });

        // 4) échec
        task.setOnFailed(evt -> {
            loaderStage.close();
            Alert alertErr = new Alert(Alert.AlertType.ERROR,
                    "❌ Échec de la suppression du commentaire.");
            alertErr.initOwner(commentList.getScene().getWindow());
            alertErr.showAndWait();
        });

        // 5) lancer
        new Thread(task, "DeleteComment-Thread").start();
    }
***/
    private void deleteCommentWithProgress(Commentaire c) {
        // 1) Loader stylé
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(60, 60);

        Label lbl = new Label("Suppression en cours...");
        lbl.setStyle("-fx-text-fill: #333; -fx-font-size: 14px;");

        VBox box = new VBox(15, pi, lbl);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 8;
        -fx-effect: dropshadow(
            gaussian, rgba(0,0,0,0.25), 10, 0, 0, 4
        );
    """);

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        overlay.getChildren().add(box);

        Scene loaderScene = new Scene(overlay);
        Stage loaderStage = new Stage();
        loaderStage.initOwner(commentList.getScene().getWindow());
        loaderStage.initModality(Modality.APPLICATION_MODAL);
        loaderStage.setResizable(false);
        loaderStage.setScene(loaderScene);
        loaderStage.show();

        // 2) Tâche de suppression
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                reportService.deleteReportsForComment(c.getId());
                commentaireService.supprimer(c.getId());
                emailService.sendCommentDeleted(
                        c.getUser().getEmail(),
                        c.getUser().getPrenom() + " " + c.getUser().getNom(),
                        c.getContent()
                );
                return null;
            }
        };

        // 3) Succès
        task.setOnSucceeded(evt -> {
            loaderStage.close();
            Alert info = new Alert(Alert.AlertType.INFORMATION,
                    "✅ Commentaire supprimé avec succès.");
            info.initOwner(commentList.getScene().getWindow());
            info.showAndWait();
            loadComments();
        });

        // 4) Échec
        task.setOnFailed(evt -> {
            loaderStage.close();
            Alert err = new Alert(Alert.AlertType.ERROR,
                    "❌ Échec de la suppression du commentaire.");
            err.initOwner(commentList.getScene().getWindow());
            err.showAndWait();
        });

        // 5) Lancement
        new Thread(task, "DeleteComment-Thread").start();
    }

}
