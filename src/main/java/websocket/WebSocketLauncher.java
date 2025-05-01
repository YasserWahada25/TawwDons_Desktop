package websocket;

import org.glassfish.tyrus.server.Server;

public class WebSocketLauncher {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8080, "/", null, WebSocketServer.class);
        try {
            server.start();
            System.out.println("✅ WebSocket lancé sur ws://localhost:8080/chat");
            Thread.currentThread().join(); // boucle pour maintenir le serveur en vie
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur WebSocket : " + e.getMessage());
        } finally {
            server.stop();
        }
    }
}




