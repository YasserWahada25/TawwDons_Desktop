package utils;

import models.User;

/**
 * Gère la session utilisateur à travers l'application.
 * Utilise des méthodes statiques pour stocker l'utilisateur connecté.
 */
public class SessionManager {

    private static User currentUser;

    /**
     * Définit l'utilisateur actuellement connecté.
     *
     * @param user l'utilisateur connecté
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     *
     * @return l'utilisateur connecté ou null si aucune session active
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Réinitialise la session utilisateur.
     */
    public static void clearSession() {
        currentUser = null;
    }
}
