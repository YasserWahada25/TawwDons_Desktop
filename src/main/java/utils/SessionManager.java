package utils;

import models.User;


public class SessionManager {
    private static SessionManager instance;
    private static User currentUser;

    private SessionManager() {
        // Private constructor to enforce singleton pattern
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public static void setCurrentUser(User user) {
        currentUser = user;
    }


    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
