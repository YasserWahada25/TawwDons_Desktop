package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String url = "jdbc:mysql://localhost:3306/tawadon";
    private final String user = "root";
    private final String pws = "";

    private Connection connection;
    private static MyDataBase instance;

    private MyDataBase() {
        connect();
    }

    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, pws);
                System.out.println("Connecté à la base de données");
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la connexion: " + e.getMessage());
        }
        return connection;
    }
}
