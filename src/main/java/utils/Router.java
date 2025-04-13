package utils;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Router {

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Router.class.getResource(fxmlPath));
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

