package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;
import utils.Router;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ArticleListController implements Initializable {

    @FXML
    private FlowPane articlesFlowPane;

    @FXML
    private MenuItem menuListeDons;
    @FXML
    private MenuItem menuPosterDon;
    @FXML
    private MenuItem menuListeArticles;

    private final ArticleService articleService = new ArticleService();
    @FXML
    private Button btnHome;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupNavigation();

        List<Article> articles = articleService.getAll();
        for (Article article : articles) {
            VBox card = createArticleCard(article);
            articlesFlowPane.getChildren().add(card);
        }
    }

    private void setupNavigation() {
        if (menuListeDons != null)
            menuListeDons.setOnAction(e -> Router.navigateTo("/Dons/ListDons.fxml"));
        if (menuPosterDon != null)
            menuPosterDon.setOnAction(e -> Router.navigateTo("/Dons/AddDons.fxml"));
        if (menuListeArticles != null)
            menuListeArticles.setOnAction(e -> Router.navigateTo("/articleList.fxml"));
        if (btnHome != null)
            btnHome.setOnAction(e -> Router.navigateTo("/Home.fxml"));
    }
    private VBox createArticleCard(Article article) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        if (article.getImage() != null) {
            File imageFile = new File("images/" + article.getImage());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            }
        }

        Label titreLabel = new Label(article.getTitre().toUpperCase());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String desc = article.getDescription();
        if (desc.length() > 100) {
            desc = desc.substring(0, 100) + "...";
        }
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #444;");

        Button readMoreBtn = new Button("🔍 Read More");
        readMoreBtn.setStyle("-fx-text-fill: #2979ff; -fx-background-color: transparent; -fx-font-weight: bold;");

        // 👇 Action pour ouvrir les détails
        readMoreBtn.setOnAction(e -> {
            System.out.println("✅ Bouton 'Read More' cliqué !");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/articleDetails.fxml"));
                Parent root = loader.load();

                ArticleDetailsController controller = loader.getController();
                controller.setArticle(article);

                Stage stage = new Stage();
                stage.setTitle("Détails de l'article");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VBox card = new VBox(10, imageView, titreLabel, descLabel, readMoreBtn);
        card.setPadding(new Insets(10));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        return card;
    }

}
