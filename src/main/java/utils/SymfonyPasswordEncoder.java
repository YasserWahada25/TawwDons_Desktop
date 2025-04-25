package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SymfonyPasswordEncoder {
    
    public static String encodePassword(String plainPassword) {
        try {
            // Generate a random salt (22 characters)
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt).substring(0, 22);
            
            // Format: $2y$13$salt....hash
            String fullSalt = "$2y$13$" + saltBase64;
            
            // Hash the password with the salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((plainPassword + fullSalt).getBytes());
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            // Return the complete hashed password in Symfony format
            return fullSalt + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean isPasswordValid(String hashedPassword, String plainPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2y$13$")) {
            return false;
        }
        
        try {
            // Extract the salt (everything between $2y$13$ and the hash)
            String salt = hashedPassword.substring(0, hashedPassword.indexOf('$', 7) + 22);
            
            // Hash the provided password with the same salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((plainPassword + salt).getBytes());
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            // Compare the generated hash with the stored hash
            String expectedHash = salt + hashBase64;
            return hashedPassword.equals(expectedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 