package websocket;

import org.glassfish.tyrus.server.Server;

import java.net.BindException;

public class WebSocketLauncher {

    private static final String HOST = "localhost";
    private static final int PORT = 8020;
    public static int usedPort = PORT;

    public static void main(String[] args) {
        Server server = new Server(HOST, PORT, "/", null, WebSocketServer.class);
        try {
            server.start();
            System.out.println("✅ WS lancé sur ws://" + HOST + ":" + PORT + "/chat/{demandeId}");
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            Thread.currentThread().join();
        } catch (Exception e) {
            // BindException n'est pas déclaré, on le détecte via getCause()
            Throwable cause = e.getCause();
            if (cause instanceof BindException) {
                System.out.println("ℹ️ Serveur WS déjà actif sur le port " + PORT);
            } else {
                e.printStackTrace();
            }
        }
    }

    /** URI fixe vers la room pour cette demande */
    public static String getWebSocketUri(int demandeId) {
        return "ws://" + HOST + ":" + PORT + "/chat/" + demandeId;
    }
}
