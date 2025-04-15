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
<<<<<<< HEAD
        Router.navigateTo("/login.fxml"); // Chemin relatif dans resources
=======
        Router.navigateTo("/articleList.fxml"); // Chemin relatif dans resources

>>>>>>> b746a46617506d31aa2e579a9777e962266bdcc8

        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();





































































    }
}
