package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {
    private Integer id;
    private String email;
    private String password;
    private String roles;           // stocké en base sous forme de chaîne
    private String nom;
    private String prenom;
    private String etat_compte;
    private String type_utilisateur;
    private String reset_token;
    private String google_id;

    /**
     * Constructeur par défaut : on verrouille par défaut les nouveaux comptes.
     */
    public User() {
        this.etat_compte = "verrouillé";
    }

    /**
     * Constructeur complet.
     */
    public User(Integer id,
                String email,
                String password,
                String roles,
                String nom,
                String prenom,
                String etat_compte,
                String type_utilisateur,
                String reset_token,
                String google_id) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.nom = nom;
        this.prenom = prenom;
        this.etat_compte = etat_compte;
        this.type_utilisateur = type_utilisateur;
        this.reset_token = reset_token;
        this.google_id = google_id;
    }

    // --- Getters & Setters ---

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Récupère la chaîne brute des rôles.
     */
    public String getRoles() {
        return roles;
    }
    /**
     * Stocke la chaîne brute des rôles (ex. "[ROLE_USER,ROLE_ADMIN]").
     */
    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEtat_compte() {
        return etat_compte;
    }
    public void setEtat_compte(String etat_compte) {
        this.etat_compte = etat_compte;
    }

    public String getType_utilisateur() {
        return type_utilisateur;
    }
    public void setType_utilisateur(String type_utilisateur) {
        this.type_utilisateur = type_utilisateur;
    }

    public String getReset_token() {
        return reset_token;
    }
    public void setReset_token(String reset_token) {
        this.reset_token = reset_token;
    }

    public String getGoogle_id() {
        return google_id;
    }
    public void setGoogle_id(String google_id) {
        this.google_id = google_id;
    }

    // --- Méthodes utilitaires ---

    /**
     * Convertit la chaîne de rôles stockée en base en liste de rôles.
     * Toujours ajoute ROLE_USER si absent.
     */
    public List<String> getRolesAsList() {
        List<String> rolesList = new ArrayList<>();
        if (roles != null && !roles.isBlank()) {
            String cleaned = roles.replace("[", "").replace("]", "");
            String[] parts = cleaned.split("\\s*,\\s*");
            rolesList.addAll(Arrays.asList(parts));
        }
        if (!rolesList.contains("ROLE_USER")) {
            rolesList.add("ROLE_USER");
        }
        return rolesList;
    }

    /**
     * Stocke une liste de rôles au format chaîne (ex. "[ROLE_USER, ROLE_ADMIN]").
     */
    public void setRolesFromList(List<String> rolesList) {
        this.roles = rolesList.toString();
    }

    // --- Validation sommaire ---

    public boolean isValidEmail() {
        return email != null && email.contains("@") && !email.isBlank();
    }

    public boolean isValidPassword() {
        return password != null && password.length() >= 6;
    }

    public boolean isValidUserType() {
        return type_utilisateur != null &&
                (type_utilisateur.equals("donneur") ||
                        type_utilisateur.equals("beneficiaire") ||
                        type_utilisateur.equals("professionnel"));
    }
}
