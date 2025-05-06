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
            WebSocketContainer c = ContainerProvider.getWebSocketContainer();
            c.connectToServer(this, new URI(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("[Socket] Ouverte");
    }

    @OnMessage
    public void onMessage(String message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
        System.out.println("[Socket] Fermée: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }

    public void send(String msg) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnMessageReceived(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void disconnect() {
        try {
            if (session != null && session.isOpen()) session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
