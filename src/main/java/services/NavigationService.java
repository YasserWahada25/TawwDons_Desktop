package services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class NavigationService {

    /**
     * Charge la vue FXML placée à la racine de resources/
     * Exemple d'appel : load("Home.fxml") ou load("article/articleList.fxml")
     */
    public static void load(String fxmlFile) {
        try {
            // On retire "/fxml/" : le FXML est à la racine de resources/
            FXMLLoader loader = new FXMLLoader(
                    NavigationService.class.getResource("/" + fxmlFile)
            );
            Parent root = loader.load();

            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
