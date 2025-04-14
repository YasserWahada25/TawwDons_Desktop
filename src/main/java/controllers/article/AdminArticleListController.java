package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;
import utils.Router;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminArticleListController implements Initializable {

    @FXML private TableView<Article> tableView;
    @FXML private TableColumn<Article, Integer> colId;
    @FXML private TableColumn<Article, String> colTitre;
    @FXML private TableColumn<Article, String> colImage;
    @FXML private TableColumn<Article, String> colDescription;
    @FXML private TableColumn<Article, String> colCategorie;
    @FXML private TableColumn<Article, Void> colActions;
    @FXML private TextField searchField;

    // Sidebar routage
    @FXML private MenuItem menuArticleList;
    @FXML private MenuItem menuArticleForm;
    @FXML private Button btnDonsRequests;
    @FXML private Button btnDashboard;

    private final ArticleService articleService = new ArticleService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        // Routage sidebar
        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));
        if (menuArticleForm != null)
            menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/ajouterArticle.fxml"));
        if (btnDonsRequests != null)
            btnDonsRequests.setOnAction(e -> Router.navigateTo("/Admin/RequestAddDons.fxml"));
        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> System.out.println("Dashboard clické ! (à implémenter)"));

        // Charger données
        loadArticles();
        addActionButtonsToTable();

        // Filtre recherche
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            tableView.getItems().setAll(
                    articleService.getAll().stream()
                            .filter(a -> a.getTitre().toLowerCase().contains(newText.toLowerCase()))
                            .toList()
            );
        });
    }

    private void loadArticles() {
        List<Article> articles = articleService.getAll();
        tableView.getItems().setAll(articles);
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
            private final FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
            private final FontAwesomeIconView viewIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
            private final HBox pane = new HBox(10, editIcon, deleteIcon, viewIcon);

            {
                editIcon.setStyle("-fx-fill: #2980b9;");
                deleteIcon.setStyle("-fx-fill: #c0392b;");
                viewIcon.setStyle("-fx-fill: #2ecc71;");
                pane.setAlignment(Pos.CENTER);

                editIcon.setOnMouseClicked(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin/modifierArticle.fxml"));
                        Parent root = loader.load();
                        ModifierArticleController controller = loader.getController();
                        controller.setArticle(article);

                        Stage stage = new Stage();
                        stage.setTitle("Modifier l'article");
                        stage.setScene(new Scene(root));
                        stage.showAndWait();

                        tableView.refresh();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                deleteIcon.setOnMouseClicked(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Suppression de l'article");
                    confirm.setContentText("Voulez-vous vraiment supprimer l'article : " + article.getTitre() + " ?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            articleService.delete(article.getId());
                            tableView.getItems().remove(article);
                        }
                    });
                });

                viewIcon.setOnMouseClicked(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    System.out.println("View clicked for article: " + article.getTitre());
                    // Affichage ou détail à implémenter plus tard
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }
}
