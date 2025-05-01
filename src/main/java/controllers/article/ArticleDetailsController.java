package controllers.article;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Commentaire;
import models.CommentReaction;
import models.Reaction;
import models.User;
import services.*;
import utils.SessionManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ArticleDetailsController implements Initializable {

    @FXML private VBox container;
    @FXML private Label titreLabel;
    @FXML private ImageView imageView;
    @FXML private Label categorieLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label descriptionLabel;

    @FXML private VBox commentsContainer;
    @FXML private TextArea commentInput;
    @FXML private Button btnPosterComment;

    @FXML private Label likesLabel;
    @FXML private Label dislikesLabel;
    @FXML private Button btnLike;
    @FXML private Button btnDislike;

    private models.Article currentArticle;
    private User currentUser;

    private final ArticleService articleService = new ArticleService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final ReactionService reactionService = new ReactionService();
    private final CommentReactionService commentReactionService = new CommentReactionService();
    private final CommentReplyService commentReplyService = new CommentReplyService();
    private final CommentReportService commentReportService = new CommentReportService();
    private final EmailService emailService = new EmailService();
    private final CommentAnalyzer commentAnalyzer =
            new CommentAnalyzer("1608670126", "Az9aNCSbfTmc56AZNQbnjWzTMcbx9WgN");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> container.requestFocus());
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            System.out.println("👤 Utilisateur connecté : "
                    + currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            System.out.println("❗ Aucun utilisateur connecté !");
        }
        btnPosterComment.setOnAction(e -> ajouterCommentaire());
        btnLike.setOnAction(e -> handleLike());
        btnDislike.setOnAction(e -> handleDislike());
    }

    public void setArticle(models.Article article) {
        this.currentArticle = article;
        if (article == null) return;
        titreLabel.setText(article.getTitre());
        categorieLabel.setText(article.getCategorie());
        descriptionLabel.setText(article.getDescription());
        authorLabel.setText("Degani Omar");
        dateLabel.setText(article.getCreated_at().toString());
        likesLabel.setText(String.valueOf(article.getLikes()));
        dislikesLabel.setText(String.valueOf(article.getDislikes()));
        if (article.getImage() != null) {
            File f = new File("images/" + article.getImage());
            imageView.setImage(f.exists() ? new Image(f.toURI().toString()) : null);
        } else {
            imageView.setImage(null);
        }
        afficherCommentaires();
    }

    private void afficherCommentaires() {
        commentsContainer.getChildren().clear();
        List<Commentaire> commentaires = commentaireService
                .getCommentairesByArticleId(currentArticle.getId())
                .stream()
                .filter(c -> "valide".equalsIgnoreCase(c.getEtat()))
                .toList();
        for (Commentaire c : commentaires) {
            commentsContainer.getChildren().add(createCommentNode(c));
        }
    }

    private String toRelative(LocalDateTime dt) {
        Duration dur = Duration.between(dt, LocalDateTime.now());
        long m = dur.toMinutes();
        if (m < 1) return "now";
        if (m < 60) return m + "m";
        long h = m / 60;
        if (h < 24) return h + "h";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private VBox createCommentNode(Commentaire c) {
        ImageView avatar = new ImageView(new Image("file:images/1744625675981.jpg"));
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setClip(new javafx.scene.shape.Circle(20,20,20));
        Label nomPrenom = new Label(c.getUser().getPrenom() + " " + c.getUser().getNom());
        nomPrenom.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        ImageView reactionImg = new ImageView();
        reactionImg.setFitWidth(20);
        reactionImg.setFitHeight(20);
        if (currentUser != null) {
            Optional<CommentReaction> crOpt = Optional.ofNullable(
                    commentReactionService.find(currentUser.getId(), c.getId())
            );
            crOpt.ifPresent(cr -> {
                var is = getClass().getResourceAsStream("/emojis/"+cr.getType());
                if (is != null) reactionImg.setImage(new Image(is));
            });
        }
        ImageView reportIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/emojis/signaler.png"))
        );
        reportIcon.setFitWidth(20);
        reportIcon.setFitHeight(20);
        Button btnReport = new Button();
        btnReport.setGraphic(reportIcon);
        btnReport.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

        boolean isAuthor = currentUser!=null && c.getUser().getId()==currentUser.getId();
        btnReport.setVisible(!isAuthor);
        btnReport.setOnAction(ev -> {
            if (currentUser==null) return;
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Signaler le commentaire");
            dlg.setHeaderText("Pourquoi ce commentaire ?");
            dlg.setContentText("Raison (facultatif):");
            dlg.showAndWait()
                    .filter(r->!r.trim().isEmpty())
                    .ifPresent(r->{
                        commentReportService.report(currentUser.getId(), c.getId(), r.trim());
                        new Alert(Alert.AlertType.INFORMATION,
                                "Commentaire signalé. Merci.")
                                .showAndWait();
                    });
        });

        HBox header = new HBox(10, avatar, nomPrenom, reactionImg);
        Region spacerH = new Region(); HBox.setHgrow(spacerH,Priority.ALWAYS);
        header.getChildren().addAll(spacerH, btnReport);
        header.setAlignment(Pos.CENTER_LEFT);

        Label contenu = new Label(c.getContent());
        contenu.setWrapText(true);
        contenu.setMaxWidth(container.getWidth()-40);
        contenu.setTextOverrun(OverrunStyle.CLIP);
        contenu.setStyle("-fx-text-fill:#444; -fx-font-size:13px;");
        Label timeLabel = new Label(toRelative(c.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
        Button btnMod = new Button("Modifier");
        btnMod.setStyle("-fx-background-color:transparent; -fx-text-fill:#2980b9;");
        /***
        btnMod.setOnAction(e->{
            TextInputDialog dlg = new TextInputDialog(c.getContent());
            dlg.setTitle("Modifier le commentaire");
            dlg.setContentText("Votre commentaire:");
            dlg.showAndWait()
                    .filter(nv->nv.trim().length()>=3)
                    .ifPresent(nv->{
                        c.setContent(nv.trim());
                        commentaireService.modifier(c);
                        afficherCommentaires();
                    });
        });
***/
        btnMod.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/article/editComment.fxml"));
                Parent root = loader.load();

                // récupérez le controller et initialisez-le
                EditCommentController ctrl = loader.getController();
                ctrl.init(c, this::afficherCommentaires);

                // créez et affichez le dialog
                Stage dialog = new Stage();
                dialog.initOwner(container.getScene().getWindow());
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Modifier le commentaire");
                dialog.setScene(new Scene(root));
                dialog.setResizable(false);
                dialog.showAndWait();
            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le formulaire d'édition.").showAndWait();
            }
        });

        Button btnDel = new Button("Supprimer");
        btnDel.setStyle("-fx-background-color:transparent; -fx-text-fill:#c0392b;");
        btnDel.setOnAction(e->{
            commentaireService.supprimer(c.getId());
            afficherCommentaires();
        });
        Button btnReact = new Button("Réagir");
        btnReact.setStyle("-fx-background-color:transparent; -fx-text-fill:#555;");
        btnReact.setOnAction(e-> {
            if (currentUser!=null) showEmojiPopup(btnReact,c,reactionImg);
        });
        Button btnReply = new Button("Répondre");
        btnReply.setStyle("-fx-background-color:transparent; -fx-text-fill:#555;");
        btnReply.setOnAction(e->showReplyPopup(c.getId()));
        Region spacerAction = new Region(); HBox.setHgrow(spacerAction,Priority.ALWAYS);
        HBox actions;
        if (isAuthor) {
            actions = new HBox(10, timeLabel, spacerAction, btnMod, btnDel, btnReact, btnReply);
        } else {
            actions = new HBox(10, timeLabel, spacerAction, btnReact, btnReply);
        }
        actions.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(5, header, contenu, actions);
        box.setPadding(new Insets(8));
        box.setStyle("""
            -fx-background-color: #f9f9f9;
            -fx-border-color: #ddd;
            -fx-border-radius:6;
            -fx-background-radius:6;
        """);
        return box;
    }

    private void showEmojiPopup(Button anchor, Commentaire cmt, ImageView reactionImg) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/emojiPicker.fxml"));
            Parent pane = loader.load();
            EmojiPickerController ctrl = loader.getController();
            ctrl.init(currentUser.getId(), cmt.getId(),
                    commentReactionService, () -> {
                        var cr = commentReactionService
                                .find(currentUser.getId(), cmt.getId());
                        if (cr!=null) {
                            var is = getClass()
                                    .getResourceAsStream("/emojis/"+cr.getType());
                            if (is!=null) reactionImg.setImage(new Image(is));
                        }
                    });
            Stage pop = new Stage();
            pop.initOwner(container.getScene().getWindow());
            pop.setScene(new Scene(pane));
            pop.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showReplyPopup(int commentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/replyPopup.fxml"));
            Parent pane = loader.load();
            ReplyPopupController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.init(commentId, commentReplyService, this::afficherCommentaires);
            Stage pop = new Stage();
            pop.initOwner(container.getScene().getWindow());
            pop.setScene(new Scene(pane));
            pop.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
/***
    private void ajouterCommentaire() {
        String txt = commentInput.getText().trim();
        if (txt.length() < 3) {
            new Alert(Alert.AlertType.WARNING, "Votre commentaire est trop court.").showAndWait();
            return;
        }
        if (currentUser == null) {
            new Alert(Alert.AlertType.WARNING, "Vous devez être connecté pour commenter.").showAndWait();
            return;
        }

        try {
            // 1) Afficher le loader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/article/loading.fxml"));
            Parent loadingRoot = loader.load();
            Stage loadingStage = new Stage();
            loadingStage.initOwner(container.getScene().getWindow());
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.setScene(new Scene(loadingRoot));
            loadingStage.setResizable(false);
            loadingStage.show();


            Task<Boolean> checkTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    JsonNode analysis = commentAnalyzer.analyze(txt);
                    return !commentAnalyzer.shouldReject(analysis);
                }
            };

            // 3) En cas de succès
            checkTask.setOnSucceeded(evt -> {
                loadingStage.close();
                boolean isValid = checkTask.getValue();
                if (isValid) {
                    // enregistrer
                    Commentaire c = new Commentaire();
                    c.setContent(txt);
                    c.setArticle(currentArticle);
                    c.setUser(currentUser);
                    c.setEtat("valide");
                    c.setCreatedAt(LocalDateTime.now());
                    commentaireService.ajouter(c);
                    commentInput.clear();
                    afficherCommentaires();
                    new Alert(Alert.AlertType.INFORMATION, "✅ Commentaire enregistré.").showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR,
                            "❌ Votre commentaire a été rejeté (contenu inapproprié).")
                            .showAndWait();
                }
            });

            // 4) En cas d’erreur technique
            checkTask.setOnFailed(evt -> {
                loadingStage.close();
                new Alert(Alert.AlertType.ERROR,
                        "⚠️ Impossible de vérifier votre commentaire.").showAndWait();
            });

            // 5) Lancer la tâche
            new Thread(checkTask, "CommentCheckThread").start();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "⚠️ Impossible d’afficher le loader.").showAndWait();
        }
    }
***/

    private void ajouterCommentaire() {
        String txt = commentInput.getText().trim();
        if (txt.length() < 3) {
            new Alert(Alert.AlertType.WARNING, "Votre commentaire est trop court.")
                    .showAndWait();
            return;
        }
        if (currentUser == null) {
            new Alert(Alert.AlertType.WARNING, "Vous devez être connecté pour commenter.")
                    .showAndWait();
            return;
        }

        try {
            // ── 1) Charger le loader.fxml et appliquer le CSS ──────────────────────
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/article/loading.fxml")
            );
            Parent loadingRoot = loader.load();

            Scene loaderScene = new Scene(loadingRoot);
            loaderScene.setFill(Color.TRANSPARENT);
            loaderScene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );

            // ── 2) Stage transparent, pas de décor natif ──────────────────────────
            Stage loaderStage = new Stage(StageStyle.TRANSPARENT);
            loaderStage.initOwner(container.getScene().getWindow());
            loaderStage.initModality(Modality.APPLICATION_MODAL);
            loaderStage.setScene(loaderScene);
            loaderStage.centerOnScreen();
            loaderStage.show();

            // ── 3) Tâche de vérification en background ────────────────────────────
            Task<Boolean> checkTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    JsonNode analysis = commentAnalyzer.analyze(txt);
                    return !commentAnalyzer.shouldReject(analysis);
                }
            };

            // ── 4) Quand c'est validé ou rejeté ─────────────────────────────────
            checkTask.setOnSucceeded(evt -> {
                loaderStage.close();
                boolean isValid = checkTask.getValue();
                if (isValid) {
                    Commentaire c = new Commentaire();
                    c.setContent(txt);
                    c.setArticle(currentArticle);
                    c.setUser(currentUser);
                    c.setEtat("valide");
                    c.setCreatedAt(LocalDateTime.now());
                    commentaireService.ajouter(c);
                    commentInput.clear();
                    afficherCommentaires();
                    new Alert(Alert.AlertType.INFORMATION, "✅ Commentaire enregistré.")
                            .showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR,
                            "❌ Votre commentaire a été rejeté (contenu inapproprié).")
                            .showAndWait();
                }
            });

            checkTask.setOnFailed(evt -> {
                loaderStage.close();
                new Alert(Alert.AlertType.ERROR,
                        "⚠️ Impossible de vérifier votre commentaire.")
                        .showAndWait();
            });

            // ── 5) On démarre tout ça dans un nouveau thread ───────────────────────
            new Thread(checkTask, "CommentCheckThread").start();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "⚠️ Impossible d’afficher le loader.")
                    .showAndWait();
        }
    }


    private void handleLike() {
        if (currentUser == null) return;
        Reaction ex = reactionService
                .findByUserAndArticle(currentUser.getId(),
                        currentArticle.getId());
        if (ex == null) {
            reactionService.add(new Reaction(
                    currentUser.getId(), currentArticle.getId(), true));
            articleService.incrementLikes(currentArticle.getId());
            currentArticle.setLikes(currentArticle.getLikes()+1);
        } else if (ex.isLike()) {
            reactionService.remove(currentUser.getId(),
                    currentArticle.getId());
            articleService.decrementLikes(currentArticle.getId());
            currentArticle.setLikes(currentArticle.getLikes()-1);
        } else {
            reactionService.update(new Reaction(
                    currentUser.getId(), currentArticle.getId(), true));
            articleService.decrementDislikes(currentArticle.getId());
            articleService.incrementLikes(currentArticle.getId());
            currentArticle.setDislikes(currentArticle.getDislikes()-1);
            currentArticle.setLikes(currentArticle.getLikes()+1);
        }
        likesLabel.setText(String.valueOf(currentArticle.getLikes()));
        dislikesLabel.setText(String.valueOf(currentArticle.getDislikes()));
    }

    private void handleDislike() {
        if (currentUser == null) return;
        Reaction ex = reactionService
                .findByUserAndArticle(currentUser.getId(),
                        currentArticle.getId());
        if (ex == null) {
            reactionService.add(new Reaction(
                    currentUser.getId(), currentArticle.getId(), false));
            articleService.incrementDislikes(currentArticle.getId());
            currentArticle.setDislikes(currentArticle.getDislikes()+1);
        } else if (!ex.isLike()) {
            reactionService.remove(currentUser.getId(),
                    currentArticle.getId());
            articleService.decrementDislikes(currentArticle.getId());
            currentArticle.setDislikes(currentArticle.getDislikes()-1);
        } else {
            reactionService.update(new Reaction(
                    currentUser.getId(), currentArticle.getId(), false));
            articleService.decrementLikes(currentArticle.getId());
            articleService.incrementDislikes(currentArticle.getId());
            currentArticle.setLikes(currentArticle.getLikes()-1);
            currentArticle.setDislikes(currentArticle.getDislikes()+1);
        }
        likesLabel.setText(String.valueOf(currentArticle.getLikes()));
        dislikesLabel.setText(String.valueOf(currentArticle.getDislikes()));
    }
}
