package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

public class User {
    private Integer id;
    private String email;
    private String password;
    private String roles; // In MySQL we're storing roles as a string, but will handle conversion
    private String nom;
    private String prenom;
    private String etat_compte;
    private String type_utilisateur;
    private String reset_token;
    private String google_id;
    private Date block_expiration;
    private Date last_login_date;

    public User() {
        this.etat_compte = "verrouillé"; // Default state for all accounts
    }

    public User(Integer id, String email, String password, String roles, String nom, String prenom, 
               String etat_compte, String type_utilisateur, String reset_token, String google_id) {
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
        this.block_expiration = null;
        this.last_login_date = null;
    }

    public User(Integer id, String email, String password, String roles, String nom, String prenom, 
               String etat_compte, String type_utilisateur, String reset_token, String google_id, 
               Date block_expiration) {
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
        this.block_expiration = block_expiration;
        this.last_login_date = null;
    }

    public User(Integer id, String email, String password, String roles, String nom, String prenom, 
               String etat_compte, String type_utilisateur, String reset_token, String google_id, 
               Date block_expiration, Date last_login_date) {
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
        this.block_expiration = block_expiration;
        this.last_login_date = last_login_date;
    }

    // Getters and setters
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

    // For database storage - we'll store as a string
    public String getRoles() {
        return roles;
    }

    // For database storage - storing as a string
    public void setRoles(String roles) {
        this.roles = roles;
    }

    // Helper method to get roles as array (like in Symfony)
    public List<String> getRolesAsList() {
        List<String> rolesList = new ArrayList<>();
        
        if (roles != null) {
            // Convert stored roles string to list
            // Format may be like "[ROLE_DONNEUR]" or "ROLE_DONNEUR"
            String processed = roles.replace("[", "").replace("]", "");
            String[] rolesArray = processed.split(",");
            rolesList.addAll(Arrays.asList(rolesArray));
        }
        
        // Always add ROLE_USER like in Symfony
        if (!rolesList.contains("ROLE_USER")) {
            rolesList.add("ROLE_USER");
        }
        
        return rolesList;
    }

    // Helper method to set roles from array/list (like in Symfony)
    public void setRolesFromList(List<String> rolesList) {
        // Convert list to string for storage
        this.roles = rolesList.toString();
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

    public Date getBlock_expiration() {
        return block_expiration;
    }

    public void setBlock_expiration(Date block_expiration) {
        this.block_expiration = block_expiration;
    }
    
    public Date getLast_login_date() {
        return last_login_date;
    }
    
    public void setLast_login_date(Date last_login_date) {
        this.last_login_date = last_login_date;
    }
    
    // Helper method to check if user is currently banned
    public boolean isBanned() {
        if (block_expiration == null) {
            return false;
        }
        
        // Check if ban has expired
        Date now = new Date();
        return block_expiration.after(now);
    }

    // Similar to Symfony's validation
    public boolean isValidEmail() {
        return email != null && email.contains("@") && !email.isEmpty();
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