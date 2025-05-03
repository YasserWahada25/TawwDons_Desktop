import javafx.application.Application;
import javafx.stage.Stage;
import utils.MyDataBase;
import utils.Router;
import websocket.ChatSocketClient;
import websocket.WebSocketLauncher;

import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {
        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("connecté à la base de données");
        System.out.println(connection);

        // 1. Lancer le serveur WebSocket dans un thread à part
        Thread socketThread = new Thread(() -> WebSocketLauncher.main(null));
        socketThread.start();

        // 2. Attendre un petit moment que le serveur démarre
        try {
            Thread.sleep(1000); // pour laisser le temps au serveur de se lancer
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Connexion du client sur le port utilisé
        int usedPort = WebSocketLauncher.usedPort;
        if (usedPort != -1) {
            ChatSocketClient socketClient = new ChatSocketClient();
            socketClient.connect("ws://localhost:" + usedPort + "/chat");
        } else {
            System.err.println("❌ Impossible de récupérer le port du WebSocket.");
        }

        // 4. Lancer l'app JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Router.setMainStage(primaryStage);
        Router.navigateTo("/Dons/AddDons.fxml");
        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();
    }
}
