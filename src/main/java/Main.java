import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import utils.MyDataBase;
import utils.Router;

import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {
        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("connecter a la base de données");
        System.out.println(connection);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Stocker le stage principal dans Router
        Router.setMainStage(primaryStage);

        // 2. Charger la première page via le Router
        Router.navigateTo("/Admin/ajouterArticle.fxml"); // Chemin relatif dans resources

        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();





































































    }
}
