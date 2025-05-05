package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Router {

    private static Stage mainStage;
    private static String currentUrl;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        // gère les paramètres après “?”
        String[] parts = fxmlPath.split("\\?");
        String path = parts[0];
        currentUrl = fxmlPath;

        try {
            Parent root = FXMLLoader.load(Router.class.getResource(path));
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentUrl() {
        return currentUrl;
    }
}
