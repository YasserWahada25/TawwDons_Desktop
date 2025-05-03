package websocket;

import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class WebSocketLauncher {

    private static final int START_PORT = 8095;
    private static final int MAX_TRIES = 10;

    public static int usedPort; // ✅ Port utilisé exposé publiquement

    public static void main(String[] args) {
        int port = findAvailablePort(START_PORT, MAX_TRIES);
        usedPort = port;

        if (port == -1) {
            System.err.println("❌ Aucun port disponible entre " + START_PORT + " et " + (START_PORT + MAX_TRIES));
            return;
        }

        Server server = new Server("localhost", port, "/", null, WebSocketServer.class);

        try {
            server.start();
            System.out.println("✅ WebSocket lancé sur ws://localhost:" + port + "/chat");

            // Stop automatique à la fermeture
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            Thread.currentThread().join(); // Maintient le thread principal
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur WebSocket : " + e.getMessage());
        }
    }

    private static int findAvailablePort(int startPort, int maxTries) {
        for (int i = 0; i < maxTries; i++) {
            int portToTry = startPort + i;
            if (isPortAvailable(portToTry)) {
                return portToTry;
            }
        }
        return -1;
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public static String getWebSocketUri() {
        return "ws://localhost:" + usedPort + "/chat";
    }

}
