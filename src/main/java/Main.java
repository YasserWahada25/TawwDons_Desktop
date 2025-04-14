// src/main/java/Main.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import utils.MyDataBase;
import utils.Router;

import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {
        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("connecté à la base de données");
        System.out.println(connection);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Router.setMainStage(primaryStage);


        // 2. Charger la première page via le Router
        Router.navigateTo("/Admin/ajouterArticle.fxml"); // Chemin relatif dans resources


        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();





































































    }
}
