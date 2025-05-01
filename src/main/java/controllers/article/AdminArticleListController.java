package controllers.article;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;
import services.CommentReportService;
import utils.Router;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminArticleListController implements Initializable {

    @FXML private TableView<Article> tableView;
    @FXML private TableColumn<Article, String> colTitre;
    @FXML private TableColumn<Article, String> colImage;
    @FXML private TableColumn<Article, String> colDescription;
    @FXML private TableColumn<Article, String> colCategorie;
    @FXML private TableColumn<Article, Void> colActions;
    @FXML private TextField searchField;

    @FXML private MenuItem menuArticleList;
    @FXML private MenuItem menuArticleForm;
    @FXML private Button   btnDonsRequests;
    @FXML private Button   btnDashboard;

    private final ArticleService       articleService       = new ArticleService();
    private final CommentReportService commentReportService = new CommentReportService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));
        if (menuArticleForm != null)
            menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/ajouterArticle.fxml"));
        if (btnDonsRequests != null)
            btnDonsRequests.setOnAction(e -> Router.navigateTo("/Admin/RequestAddDons.fxml"));
        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> System.out.println("Dashboard clické ! (à implémenter)"));

        loadArticles();
        addActionButtonsToTable();

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            List<Article> filtered = articleService.getAll().stream()
                    .filter(a -> a.getTitre().toLowerCase().contains(newText.toLowerCase()))
                    .toList();
            tableView.getItems().setAll(filtered);
        });
    }

    private void loadArticles() {
        tableView.getItems().setAll(articleService.getAll());
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit   = new Button();
            private final Button btnDelete = new Button();
            private final Button btnView   = new Button();
            private final HBox   pane      = new HBox(10, btnEdit, btnDelete, btnView);

            {
                // --- Edit ---
                btnEdit.setGraphic(new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/edit.png"), 22, 22, true, true)
                ));
                btnEdit.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
                btnEdit.setOnAction(ev -> {
                    Article art = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/Admin/modifierArticle.fxml")
                        );
                        Parent root = loader.load();
                        ModifierArticleController ctrl = loader.getController();
                        ctrl.setArticle(art);
                        Stage st = new Stage();
                        st.setTitle("Modifier l'article");
                        st.setScene(new Scene(root));
                        st.showAndWait();
                        loadArticles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // --- Delete ---
                btnDelete.setGraphic(new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/supprimer.png"), 18, 18, true, true)
                ));
                btnDelete.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
                btnDelete.setOnAction(ev -> {
                    Article art = getTableView().getItems().get(getIndex());
                    // 1) Popup de confirmation
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Suppression de l'article");
                    confirm.setContentText("Voulez-vous vraiment supprimer : « " + art.getTitre() + " » ?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // 2) Suppression async avec loader
                        deleteWithProgress(art);
                    }
                });

                // --- View Comments ---
                btnView.setGraphic(new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/comment.png"), 20, 20, true, true)
                ));
                btnView.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
                btnView.setOnAction(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/commentaire/popupCommentaires.fxml"));
                        Parent root = loader.load();
                        PopupCommentairesController ctrl = loader.getController();
                        ctrl.setArticleId(article.getId());
                        Stage stage = new Stage();
                        stage.setTitle("Commentaires de l'article");
                        stage.setScene(new Scene(root));
                        stage.initOwner(tableView.getScene().getWindow());
                        stage.showAndWait();
                        loadArticles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Supprime l'article en tâche de fond, affiche un loader modal et notifie l'utilisateur.
     */
    private void deleteWithProgress(Article art) {
        // 1) Création du loader modal
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(80, 80);
        Stage loaderStage = new Stage();
        loaderStage.initOwner(tableView.getScene().getWindow());
        loaderStage.initModality(Modality.APPLICATION_MODAL);
        loaderStage.setScene(new Scene(pi));
        loaderStage.setResizable(false);
        loaderStage.setTitle("Suppression en cours...");
        loaderStage.show();

        // 2) Tâche de suppression
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                articleService.delete(art.getId());
                return null;
            }
        };

        // 3) En cas de succès
        task.setOnSucceeded(evt -> {
            loaderStage.close();
            tableView.getItems().remove(art);
            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Article supprimé avec succès.")
                    .showAndWait();
        });

        // 4) En cas d’échec
        task.setOnFailed(evt -> {
            loaderStage.close();
            new Alert(Alert.AlertType.ERROR,
                    "❌ Échec de la suppression.")
                    .showAndWait();
        });

        // 5) Lancer la tâche
        new Thread(task, "Delete-Thread").start();
    }
}
