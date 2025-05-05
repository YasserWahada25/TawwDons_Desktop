package org.example.event;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.MyDataBase;
import utils.Router;
import websocket.WebSocketLauncher;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {
        // Connexion à la base de données
        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("✅ Connecté à la base de données");

        // Lancement du serveur WebSocket sur port 8020 (évite double lancement)
        try (ServerSocket probe = new ServerSocket(8020)) {
            Thread socketThread = new Thread(() -> WebSocketLauncher.main(null));
            socketThread.setDaemon(true);
            socketThread.start();
            System.out.println("🚀 WebSocket Server démarré sur le port 8020");
        } catch (IOException e) {
            System.out.println("ℹ️ Serveur WS déjà actif sur le port 8020, pas de redémarrage");
        }

        // Lancement de l'application JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Configuration de la scène principale
        Router.setMainStage(primaryStage);
        Router.navigateTo("/Login.fxml");
        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();
    }
}
