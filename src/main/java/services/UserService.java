package services;

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
    public boolean updateUser(User user) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            // Format roles as valid JSON array
            String role = user.getRoles();
            String jsonRole;
            
            // Ensure role is in valid JSON format for the database
            if (role.equals("ROLE_DONNEUR") || role.equals("[ROLE_DONNEUR]")) {
                jsonRole = "[\"ROLE_DONNEUR\"]";
            } else if (role.equals("ROLE_BENEFICIAIRE") || role.equals("[ROLE_BENEFICIAIRE]")) {
                jsonRole = "[\"ROLE_BENEFICIAIRE\"]";
            } else if (role.equals("ROLE_PROFESSIONNEL") || role.equals("[ROLE_PROFESSIONNEL]")) {
                jsonRole = "[\"ROLE_PROFESSIONNEL\"]";
            } else if (role.equals("ROLE_ADMIN") || role.equals("[ROLE_ADMIN]")) {
                jsonRole = "[\"ROLE_ADMIN\"]";
            } else {
                // Default fallback
                jsonRole = "[\"" + role.replace("[", "").replace("]", "").replace("\"", "") + "\"]";
            }
            
            StringBuilder queryBuilder = new StringBuilder("UPDATE user SET nom = ?, prenom = ?, email = ?, roles = ?");
            
            // Check if password should be updated
            boolean updatePassword = user.getPassword() != null && !user.getPassword().isEmpty();
            if (updatePassword) {
                queryBuilder.append(", password = ?");
            }
            
            queryBuilder.append(" WHERE id = ?");
            
            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            
            // Set the parameters
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, jsonRole);
            
            if (updatePassword) {
                // Hash the password before storing it
                String hashedPassword = hashPassword(user.getPassword());
                stmt.setString(5, hashedPassword);
                stmt.setInt(6, user.getId());
            } else {
                stmt.setInt(5, user.getId());
            }
            
            System.out.println("Executing update query: " + stmt.toString());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            System.out.println("Update user result: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to fetch all users from the database
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT nom, prenom, email, roles FROM user"; // Use the correct table name

        try (Connection connection = MyDataBase.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                User user = new User();
                user.setNom(resultSet.getString("nom"));
                user.setPrenom(resultSet.getString("prenom"));
                user.setEmail(resultSet.getString("email"));
                user.setRoles(resultSet.getString("roles"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    // Method to delete a user from the database
    public boolean deleteUser(String email) {
        String query = "DELETE FROM user WHERE email = ?"; // Use the correct table name

        try (Connection connection = MyDataBase.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the deletion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to get a user by email
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRoles(rs.getString("roles"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEtat_compte(rs.getString("etat_compte"));
                user.setType_utilisateur(rs.getString("type_utilisateur"));
                user.setReset_token(rs.getString("reset_token"));
                user.setGoogle_id(rs.getString("google_id"));
                
                return user;
            }
        } catch (SQLException e) {
            System.out.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Store a password reset code for a user
     * @param email The user's email
     * @param resetCode The reset code to store
     * @return true if successfully stored, false otherwise
     */
    public boolean storeResetCode(String email, String resetCode) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String query = "UPDATE user SET reset_token = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, resetCode);
            stmt.setString(2, email);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error storing reset code: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verify a password reset code
     * @param email The user's email
     * @param resetCode The reset code to verify
     * @return true if the code is valid, false otherwise
     */
    public boolean verifyResetCode(String email, String resetCode) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String query = "SELECT reset_token FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedCode = rs.getString("reset_token");
                rs.close();
                stmt.close();
                
                return storedCode != null && storedCode.equals(resetCode);
            }
            rs.close();
            stmt.close();
            return false;
        } catch (SQLException e) {
            System.out.println("Error verifying reset code: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update a user's password after reset
     * @param email The user's email
     * @param newPassword The new password (unhashed)
     * @return true if successfully updated, false otherwise
     */
    public boolean updatePasswordAfterReset(String email, String newPassword) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String hashedPassword = hashPassword(newPassword);
            
            String query = "UPDATE user SET password = ?, reset_token = NULL WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // For more complex business logic
    // Additional methods as needed
} 