package controllers.article;

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
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;
import services.CommentReportService;
import utils.Router;
import org.kordamp.ikonli.javafx.FontIcon;


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminArticleListController implements Initializable {

    @FXML private TableView<Article>    tableView;
    @FXML private TableColumn<Article,String> colTitre;
    @FXML private TableColumn<Article,String> colImage;
    @FXML private TableColumn<Article,String> colDescription;
    @FXML private TableColumn<Article,String> colCategorie;
    @FXML private TableColumn<Article,Void>   colActions;
    @FXML private TextField                 searchField;


    // Sidebar routing

    @FXML private MenuItem menuArticleList;
    @FXML private MenuItem menuArticleForm;
    @FXML private Button   btnDonsRequests;
    @FXML private Button   btnDashboard;

    private final ArticleService       articleService       = new ArticleService();
    private final CommentReportService commentReportService = new CommentReportService();

    @Override

    public void initialize(URL url, ResourceBundle rb) {
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));


        // Sidebar navigation

        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));
        if (menuArticleForm != null)
            menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/ajouterArticle.fxml"));
        if (btnDonsRequests != null)
            btnDonsRequests.setOnAction(e -> Router.navigateTo("/Admin/RequestAddDons.fxml"));
        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> System.out.println("Dashboard clicked! (to implement)"));


        // Load data

        loadArticles();

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Article item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    boolean flagged = commentReportService.hasPendingReportForArticle(item.getId());
                    if (flagged) {
                        // fond rouge clair
                        setStyle("-fx-background-color: rgba(255,0,0,0.1);");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        addActionButtonsToTable();


        // Search filter

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
        colActions.setCellFactory(param -> new TableCell<Article,Void>() {
            private final Button btnEdit   = new Button();
            private final Button btnDelete = new Button();
            private final Button btnView   = new Button();
            private final HBox   pane      = new HBox(10, btnEdit, btnDelete, btnView);

            {
                btnEdit.setGraphic(new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/edit.png"),22,22,true,true)
                ));
                btnEdit.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

                btnDelete.setGraphic(new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/supprimer.png"),18,18,true,true)
                ));
                btnDelete.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

                btnView.setGraphic(new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/comment.png"),20,20,true,true)
                ));
                btnView.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

                btnEdit.setOnAction(ev -> {
                    Article art = getTableView().getItems().get(getIndex());
        colActions.setCellFactory(param -> new TableCell<>() {
            private final FontIcon editIcon = new FontIcon("fa-pencil");
            private final FontIcon deleteIcon = new FontIcon("fa-trash");
            private final FontIcon viewIcon = new FontIcon("fa-eye");
            private final HBox pane = new HBox(10, editIcon, deleteIcon, viewIcon);

            {
                editIcon.setStyle("-fx-icon-color: #2980b9;");
                deleteIcon.setStyle("-fx-icon-color: #c0392b;");
                viewIcon.setStyle("-fx-icon-color: #2ecc71;");
                pane.setAlignment(Pos.CENTER);

                editIcon.setOnMouseClicked(event -> {
                    Article article = getTableView().getItems().get(getIndex());
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
                btnDelete.setOnAction(ev -> {
                    Article art = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Suppression de l'article");
                    confirm.setContentText("Voulez-vous vraiment supprimer : " + art.getTitre() + " ?");
                    confirm.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.OK) {
                            articleService.delete(art.getId());
                            loadArticles();
                        }
                    });
                });


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
                        stage.showAndWait();      // ← on attend la fermeture
                        loadArticles();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("View clicked for article: " + article.getTitre());
                    // Détail à implémenter plus tard
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


}
