package services;

import models.ConnectionHistory;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConnectionHistoryService {
    
    private static final String TABLE_NAME = "connection_history";
    private final UserService userService = new UserService();
    
    /**
     * Create the connection_history table if it doesn't exist
     */
    public void createTableIfNotExists() {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            // Check if table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, TABLE_NAME, null);
            
            if (!tables.next()) {
                // Table doesn't exist, create it
                Statement stmt = conn.createStatement();
                String createTableSQL = "CREATE TABLE " + TABLE_NAME + " (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "connection_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES user(id)" +
                        ")";
                stmt.executeUpdate(createTableSQL);
                System.out.println("Created connection_history table");
                stmt.close();
            }
            
            tables.close();
        } catch (SQLException e) {
            System.out.println("Error creating connection_history table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Record a new connection for a user
     * @param userId The user's ID
     * @return true if successfully recorded, false otherwise
     */
    public boolean recordConnection(Integer userId) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            String query = "INSERT INTO " + TABLE_NAME + " (user_id) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error recording connection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get connection history for a specific user
     * @param userId The user's ID
     * @return List of connection history records
     */
    public List<ConnectionHistory> getConnectionHistoryForUser(Integer userId) {
        List<ConnectionHistory> connections = new ArrayList<>();
        
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY connection_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ConnectionHistory connection = new ConnectionHistory();
                connection.setId(rs.getInt("id"));
                connection.setUserId(rs.getInt("user_id"));
                
                Timestamp connectionDate = rs.getTimestamp("connection_date");
                connection.setConnectionDate(new Date(connectionDate.getTime()));
                
                connections.add(connection);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error getting connection history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connections;
    }
    
    /**
     * Get all connection history with user details
     * @return List of connection history records with user details
     */
    public List<ConnectionHistory> getAllConnectionHistory() {
        List<ConnectionHistory> connections = new ArrayList<>();
        
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            String query = "SELECT ch.id, ch.user_id, ch.connection_date, u.* " +
                          "FROM " + TABLE_NAME + " ch " +
                          "JOIN user u ON ch.user_id = u.id " +
                          "ORDER BY ch.connection_date DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                ConnectionHistory connection = new ConnectionHistory();
                connection.setId(rs.getInt("id"));
                connection.setUserId(rs.getInt("user_id"));
                
                Timestamp connectionDate = rs.getTimestamp("connection_date");
                connection.setConnectionDate(new Date(connectionDate.getTime()));
                
                // Create complete user object with all info
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setType_utilisateur(rs.getString("type_utilisateur"));
                user.setRoles(rs.getString("roles"));
                
                connection.setUser(user);
                System.out.println("Loaded user: " + user.getNom() + " " + user.getPrenom() + ", Type: " + user.getType_utilisateur());
                
                connections.add(connection);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error getting all connection history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connections;
    }
    
    /**
     * Get the total number of connections
     * @return Total number of connections
     */
    public int getTotalConnectionsCount() {
        int count = 0;
        
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error getting total connections count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return count;
    }
    
    /**
     * Get the number of connections since a specific date
     * @param date The date to count connections from
     * @return Number of connections since the specified date
     */
    public int getConnectionsCountSince(Date date) {
        int count = 0;
        
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE connection_date >= ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error getting connections count since date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return count;
    }
    
    /**
     * Get connection history for users that a specific user has connected with
     * This shows all users who have connected to the system when a specific user was connected
     * @param userId The user's ID
     * @return List of connection history records
     */
    public List<User> getConnectedUsersForUser(Integer userId) {
        List<User> connectedUsers = new ArrayList<>();
        
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            
            // Get the user's connection dates
            String userConnectionsQuery = "SELECT connection_date FROM " + TABLE_NAME + 
                                         " WHERE user_id = ? ORDER BY connection_date DESC";
            
            PreparedStatement userStmt = conn.prepareStatement(userConnectionsQuery);
            userStmt.setInt(1, userId);
            ResultSet userRs = userStmt.executeQuery();
            
            // For each connection, find other users who were connected around the same time
            // (within a 15-minute window)
            while (userRs.next()) {
                Timestamp connectionDate = userRs.getTimestamp("connection_date");
                
                // Find other users who connected within 15 minutes of this connection
                String otherUsersQuery = "SELECT DISTINCT u.* FROM user u " +
                                        "JOIN " + TABLE_NAME + " ch ON u.id = ch.user_id " +
                                        "WHERE ch.connection_date BETWEEN ? AND ? " +
                                        "AND u.id != ?";
                
                PreparedStatement otherUsersStmt = conn.prepareStatement(otherUsersQuery);
                
                // 15 minutes before
                Timestamp before = new Timestamp(connectionDate.getTime() - (15 * 60 * 1000));
                // 15 minutes after
                Timestamp after = new Timestamp(connectionDate.getTime() + (15 * 60 * 1000));
                
                otherUsersStmt.setTimestamp(1, before);
                otherUsersStmt.setTimestamp(2, after);
                otherUsersStmt.setInt(3, userId);
                
                ResultSet otherUsersRs = otherUsersStmt.executeQuery();
                
                while (otherUsersRs.next()) {
                    User user = new User();
                    user.setId(otherUsersRs.getInt("id"));
                    user.setEmail(otherUsersRs.getString("email"));
                    user.setNom(otherUsersRs.getString("nom"));
                    user.setPrenom(otherUsersRs.getString("prenom"));
                    user.setType_utilisateur(otherUsersRs.getString("type_utilisateur"));
                    
                    // Check if this user is already in our list
                    boolean alreadyAdded = false;
                    for (User existingUser : connectedUsers) {
                        if (existingUser.getId().equals(user.getId())) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    
                    if (!alreadyAdded) {
                        connectedUsers.add(user);
                    }
                }
                
                otherUsersRs.close();
                otherUsersStmt.close();
            }
            
            userRs.close();
            userStmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error getting connected users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connectedUsers;
    }
}
