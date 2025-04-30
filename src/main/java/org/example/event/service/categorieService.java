package org.example.event.service;

import org.example.event.model.categorie;
import org.example.event.utils.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class categorieService implements Icategorie <categorie> {
    Connection cnx;
    public categorieService() {cnx= database.getInstance().getCnx();}

    @Override
    public void ajouter(categorie categorie) throws SQLException, IOException {

    }

    @Override
    public void supprimer(categorie categorie) throws SQLException {

    }

    @Override
    public void modifier(categorie categorie) throws SQLException {

    }

    @Override
    public List<categorie> getList() throws SQLException {
            List<categorie> categories = new ArrayList<>();
            String query = "SELECT id, type FROM categorie";

            try (Statement stmt = cnx.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    categories.add(new categorie(
                            rs.getInt("id"),
                            rs.getString("type")
                    ));
                }
            }
            return categories;

    }
}
