package org.example.event.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.event.utils.database;

public class main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            database db = database.getInstance();

            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/example/event/event-view.fxml")
            );

            // Chargement sans taille fixe
            Scene scene = new Scene(fxmlLoader.load());

            // Configuration de la fenêtre
            stage.setTitle("Event App");
            stage.setScene(scene);

            // Plein écran
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("Appuyez sur ESC pour quitter le mode plein écran");

            // Redimensionnement automatique
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            stage.show();
        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}