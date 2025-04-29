package utils;

import models.User;

public class SessionManager {
    private User currentUser;

    public SessionManager() {
        this.currentUser = null;
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
} 