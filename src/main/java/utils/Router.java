package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class Router {

    private static Stage mainStage;
    private static String currentUrl;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    // ✅ Méthode originale inchangée
    public static void navigateTo(String fxmlPath) {
        String[] parts = fxmlPath.split("\\?");
        String path = parts[0];
        String params = parts.length > 1 ? parts[1] : "";

        currentUrl = fxmlPath;

        try {
            Parent root = FXMLLoader.load(Router.class.getResource(path));
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ NOUVELLE méthode avec accès au contrôleur
    public static void navigateTo(String fxmlPath, Consumer<Object> controllerInitializer) {
        currentUrl = fxmlPath;

        try {
            FXMLLoader loader = new FXMLLoader(Router.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            controllerInitializer.accept(controller); // On passe le contrôleur

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
