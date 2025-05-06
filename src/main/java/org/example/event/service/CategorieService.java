package org.example.event.service;

import org.example.event.model.Categorie;
import org.example.event.model.Categorie;
import org.example.event.utils.database;
import utils.MyDataBase;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements Icategorie<Categorie> {
    Connection cnx;
    public CategorieService() {cnx= MyDataBase.getInstance().getConnection();}

    @Override
    public void ajouter(Categorie categorie) throws SQLException {
        String query = "INSERT INTO categorie (type) VALUES (?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, categorie.getType());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Categorie categorie) throws SQLException {
        String query = "DELETE FROM categorie WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, categorie.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        String query = "UPDATE categorie SET type = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, categorie.getType());
            ps.setInt(2, categorie.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Categorie > getList() throws SQLException {
        List<Categorie > categories = new ArrayList<>();
        String query = "SELECT id, type FROM categorie";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categories.add(new Categorie(
                        rs.getInt("id"),
                        rs.getString("type")
                ));
            }
        }
        return categories;

    }
}
