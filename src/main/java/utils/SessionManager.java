package utils;

import models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor to enforce singleton pattern
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
} 