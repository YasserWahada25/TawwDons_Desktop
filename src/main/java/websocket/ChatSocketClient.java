package websocket;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class ChatSocketClient {

    private Session session;
    private Consumer<String> messageHandler;

    public void connect(String uri) {
        try {

            System.out.println("Tentative de connexion à : " + uri);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(uri));
        } catch (Exception e) {
            System.err.println("Erreur WebSocket : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("[Socket] Connexion ouverte avec le serveur");
    }

    @OnMessage
    public void onMessage(String message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("[Socket] Connexion fermée: " + reason);
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[Socket] Erreur: " + throwable.getMessage());
    }

    public void send(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnMessageReceived(Consumer<String> handler) {
        this.messageHandler = handler;
    }
}
