package service;

import models.User;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    
    // The correct table name from your database
    private static final String TABLE_NAME = "user"; // Changed from "users" to "user"
    
    // Test mode in-memory storage
    private static List<User> testUsers = new ArrayList<>();
    
    static {
        // Initialize some test users
        testUsers.add(new User(1, "admin@gmail.com", "password", "[ROLE_ADMIN]", "Admin", "Admin", 
                     "verrouillé", "admin", null, null));
        testUsers.add(new User(2, "donneur@gmail.com", "password", "[ROLE_DONNEUR]", "Donneur", "Test", 
                     "verrouillé", "donneur", null, null));
        testUsers.add(new User(3, "beneficiaire@gmail.com", "password", "[ROLE_BENEFICIAIRE]", "Beneficiaire", "Test", 
                     "verrouillé", "beneficiaire", null, null));
    }

    public User authenticate(String email, String password) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            // First, fetch the user by email
            String query = "SELECT * FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                
                String storedPassword = rs.getString("password");
                user.setPassword(storedPassword);
                
                user.setRoles(rs.getString("roles"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEtat_compte(rs.getString("etat_compte"));
                user.setType_utilisateur(rs.getString("type_utilisateur"));
                user.setReset_token(rs.getString("reset_token"));
                user.setGoogle_id(rs.getString("google_id"));
                
                rs.close();
                stmt.close();
                
                // Verify the password using SHA-256
                if (verifyPassword(password, storedPassword)) {
                    System.out.println("Password matched!");
                    return user;
                } else {
                    System.out.println("Password does not match");
                    return null;
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("SQL error in authenticate: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fall back to test users if needed
        for (User testUser : testUsers) {
            if (testUser.getEmail().equals(email) && testUser.getPassword().equals(password)) {
                return testUser;
            }
        }
        
        return null;
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SHA-256 algorithm not found");
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean emailExists(String email) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String query = "SELECT COUNT(*) FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                
                rs.close();
                stmt.close();
                
                return count > 0;
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error checking if email exists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean registerUser(User user) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            // Format roles as valid JSON array
            String role = user.getRoles();
            String jsonRole;
            
            // Ensure role is in valid JSON format for the database
            if (role.equals("[ROLE_DONNEUR]")) {
                jsonRole = "[\"ROLE_DONNEUR\"]";
            } else if (role.equals("[ROLE_BENEFICIAIRE]")) {
                jsonRole = "[\"ROLE_BENEFICIAIRE\"]";
            } else if (role.equals("[ROLE_PROFESSIONNEL]")) {
                jsonRole = "[\"ROLE_PROFESSIONNEL\"]";
            } else if (role.equals("[ROLE_ADMIN]")) {
                jsonRole = "[\"ROLE_ADMIN\"]";
            } else {
                // Default fallback
                jsonRole = "[\"" + role.replace("[", "").replace("]", "") + "\"]";
            }
            
            System.out.println("Using JSON role format: " + jsonRole);
            
            String query = "INSERT INTO user (email, password, roles, nom, prenom, etat_compte, type_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, jsonRole); // Use the proper JSON format
            stmt.setString(4, user.getNom());
            stmt.setString(5, user.getPrenom());
            stmt.setString(6, user.getEtat_compte());
            stmt.setString(7, user.getType_utilisateur());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // For testing purposes when database is not available
    public User authenticateTest(String email, String password) {
        // Hardcoded user for testing
        if ("admin@gmail.com".equals(email) && "password".equals(password)) {
            return new User(14, "admin@gmail.com", "password", "[ROLE_ADMIN]", "Admin", "Admin", 
                           "verrouillé", "donneur", null, null);
        }
        return null;
    }

    public static boolean verifyPassword(String enteredPassword, String storedPassword) {
        String hashedEnteredPassword = hashPassword(enteredPassword);
        return hashedEnteredPassword.equals(storedPassword);
    }

    public static void main(String[] args) {
        String password = "test123"; // Sample password
        UserService userService = new UserService();
        String hashedPassword = userService.hashPassword(password); // Hash the password
        System.out.println("Hashed Password: " + hashedPassword);

        // Verify the password
        boolean isMatch = verifyPassword(password, hashedPassword);
        System.out.println("Password Match: " + isMatch);
    }

    public boolean validateUser(String email, String hashedPassword) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String query = "SELECT password FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                rs.close();
                stmt.close();
                return storedHash.equals(hashedPassword);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error validating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
} 