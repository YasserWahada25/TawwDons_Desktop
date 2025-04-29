package models;

import java.time.LocalDateTime;

public class Evaluation {
    private int id;
    private String name;
    private String description;
    private String type;
    private LocalDateTime createdAt;
    private boolean isArchived; // <<=== AJOUT

    public Evaluation() {}

    // ✅ Le constructeur actuel (5 paramètres)
    public Evaluation(int id, String name, String description, String type, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.createdAt = createdAt;
        this.isArchived = false; // par défaut non archivé
    }

    // ✅ NOUVEAU constructeur (6 paramètres)
    public Evaluation(int id, String name, String description, String type, LocalDateTime createdAt, boolean isArchived) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.createdAt = createdAt;
        this.isArchived = isArchived;
    }

    // === GETTERS & SETTERS ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
    @Override
    public String toString() {
        return name; // ou getName() si ton champ est privé
    }

}
