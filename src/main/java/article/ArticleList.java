package article;

import controllers.article.ArticleDetailsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Article;
import services.ArticleService;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.StackPane;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ArticleList implements Initializable {

    @FXML
    private FlowPane articlesFlowPane;
    @FXML
    private HBox paginationBox;
    @FXML
    private ComboBox<String> categorieFilterCombo;

    private final ArticleService articleService = new ArticleService();
    private List<Article> allArticles = new ArrayList<>();
    private final int articlesPerPage = 6;
    private int currentPage = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allArticles = articleService.getAll();

        categorieFilterCombo.getItems().addAll("Tous", "dons", "recrutement", "evenement");
        categorieFilterCombo.setValue("Tous");
        categorieFilterCombo.setOnAction(e -> {
            currentPage = 1;
            showCurrentPage();
            buildPagination();
        });

        showCurrentPage();
        buildPagination();
    }

    private void showCurrentPage() {
        articlesFlowPane.getChildren().clear();

        String selectedCategorie = categorieFilterCombo.getValue();
        List<Article> filteredArticles;

        if (selectedCategorie.equals("Tous")) {
            filteredArticles = allArticles;
        } else {
            filteredArticles = allArticles.stream()
                    .filter(a -> a.getCategorie().equalsIgnoreCase(selectedCategorie))
                    .toList();
        }

        int start = (currentPage - 1) * articlesPerPage;
        int end = Math.min(start + articlesPerPage, filteredArticles.size());

        List<Article> articlesToShow = filteredArticles.subList(start, end);

        for (Article article : articlesToShow) {
            VBox card = createArticleCard(article);
            articlesFlowPane.getChildren().add(card);
        }
    }

    private void buildPagination() {
        paginationBox.getChildren().clear();

        String selectedCategorie = categorieFilterCombo.getValue();
        List<Article> filteredArticles = selectedCategorie.equals("Tous")
                ? allArticles
                : allArticles.stream()
                .filter(a -> a.getCategorie().equalsIgnoreCase(selectedCategorie))
                .toList();

        int totalPages = (int) Math.ceil((double) filteredArticles.size() / articlesPerPage);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #2979ff; -fx-text-fill: white; -fx-font-weight: bold;");
            }

            final int page = i;
            pageBtn.setOnAction(e -> {
                currentPage = page;
                showCurrentPage();
                buildPagination();
            });

            paginationBox.getChildren().add(pageBtn);
        }
    }

    private VBox createArticleCard(Article article) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(140);
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
        if (desc.length() > 50) {
            desc = desc.substring(0, 50) + "...";
        }
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #444;");

        Button readMoreBtn = new Button("Read More");
        readMoreBtn.setStyle("-fx-text-fill: #2979ff; -fx-background-color: transparent; -fx-font-weight: bold;");

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
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        return card;
    }


}
