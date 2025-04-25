package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Navigation {

    public static void goTo(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("/views/" + fxmlFileName));
            Parent root = loader.load();
            Stage stage = (Stage) Window.getWindows().filtered(Window::isShowing).get(0);
            stage.setScene(new Scene(root));
            stage.setTitle(fxmlFileName.replace(".fxml", ""));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur navigation vers : " + fxmlFileName);
        }
    }
}
