package org.example.event;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import utils.Router;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charge la vue de login ou directement Home selon votre flux
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Tawwa Dons");

        // Enregistre le stage principal dans Router
        Router.setMainStage(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
