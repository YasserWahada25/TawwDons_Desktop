import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override

    public void start(Stage primaryStage) {
        Router.setMainStage(primaryStage);
        Router.navigateTo("/home.fxml");

        Scene scene = primaryStage.getScene();
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MainInterface.fxml"));
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("TawwDons - Donation Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }
}
