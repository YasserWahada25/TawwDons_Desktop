package websocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{demandeId}")
public class WebSocketServer {

    // on garde une room (set de sessions) pour chaque demandeId
    private static final Map<Integer, Set<Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("demandeId") int demandeId) {
        rooms
                .computeIfAbsent(demandeId, id -> ConcurrentHashMap.newKeySet())
                .add(session);
        System.out.println("[WS] Ouverture session pour demande " + demandeId);
    }

    @OnClose
    public void onClose(Session session,
                        @PathParam("demandeId") int demandeId) {
        Set<Session> room = rooms.get(demandeId);
        if (room != null) room.remove(session);
        System.out.println("[WS] Fermeture session pour demande " + demandeId);
    }

    @OnMessage
    public void onMessage(Session session,
                          String message,
                          @PathParam("demandeId") int demandeId) {
        System.out.println("[WS] Reçu dans room " + demandeId + ": " + message);
        Set<Session> room = rooms.get(demandeId);
        if (room != null) {
            room.stream()
                    .filter(s -> s.isOpen() && !s.equals(session))
                    .forEach(s -> s.getAsyncRemote().sendText(message));
        }
    }
}
