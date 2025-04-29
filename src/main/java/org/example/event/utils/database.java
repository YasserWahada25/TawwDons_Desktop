package org.example.event.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    private static final String URL = "jdbc:mysql://localhost:3306/tawadon";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static database instance;
    private Connection cnx;

    private database() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public static database getInstance() {
        if (instance == null) {
            instance = new database();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}