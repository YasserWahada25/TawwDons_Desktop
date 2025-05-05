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
        // 1) Connexion à la base (idem)
        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("connecté à la base de données");

        // 2) Démarrer le WS **SEULEMENT** si le port 8030 est libre
        try (ServerSocket probe = new ServerSocket(8020)) {
            // port libre → on démarre le serveur
            Thread socketThread = new Thread(() -> WebSocketLauncher.main(null));
            socketThread.setDaemon(true);
            socketThread.start();
        } catch (IOException e) {
            // port occupé → on suppose que le WS tourne déjà, on ne relance pas
            System.out.println("ℹ️ Serveur WS déjà actif sur le port 8030, pas de redémarrage");
        }

        // 3) Lancer l'app JavaFX
        launch(args);
    }



    // 3) (Commenté) ancienne connexion WS générique
        /*
        try {
            Thread.sleep(1000);
            int usedPort = WebSocketLauncher.usedPort;
            if (usedPort != -1) {
                ChatSocketClient socketClient = new ChatSocketClient();
                socketClient.connect(WebSocketLauncher.getWebSocketUri(0));
            } else {
                System.err.println("❌ Impossible de récupérer le port du WebSocket.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        // 4) Lancer l'application JavaFX


    @Override
    public void start(Stage primaryStage) {
        Router.setMainStage(primaryStage);
        Router.navigateTo("/login.fxml");
        primaryStage.setTitle("TawwaDon App");
        primaryStage.show();
    }
}
