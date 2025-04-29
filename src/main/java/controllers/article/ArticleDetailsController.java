package controllers.article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import models.Article;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ArticleDetailsController implements Initializable {

    @FXML
    private VBox container;

    @FXML
    private Label titreLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private Label categorieLabel;

    @FXML
    private Label descriptionLabel;

    private Article currentArticle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Optionnel : tu peux afficher un message de debug si tu veux
        // System.out.println("ArticleDetailsController chargé !");
    }

    public void setArticle(Article article) {
        this.currentArticle = article;

        if (article != null) {
            titreLabel.setText(article.getTitre());
            categorieLabel.setText("Catégorie : " + article.getCategorie());
            descriptionLabel.setText(article.getDescription());

            if (article.getImage() != null) {
                File imageFile = new File("images/" + article.getImage());
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    imageView.setImage(null);
                    System.err.println("Image introuvable : " + imageFile.getAbsolutePath());
                }
            } else {
                imageView.setImage(null);
            }
        }
    }
}
