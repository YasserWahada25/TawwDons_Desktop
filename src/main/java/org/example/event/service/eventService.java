package org.example.event.service;

import org.example.event.model.event;
import org.example.event.utils.database;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class eventService implements Ievent <event> {
    Connection cnx;
    public eventService() {cnx= database.getInstance().getCnx();}

    @Override
    public void ajouter(event event) throws SQLException, IOException {
        String sql ="insert into event(nom,description,image,categorie_id) values(?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, event.getNom());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getImage());
        ps.setInt(4,event.getCateegorie_id());
        ps.executeUpdate();
        System.out.println("event ajoute");
    }

    @Override
    public void supprimer(event event) throws SQLException {
        String sql ="delete from event where id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, event.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(event event) throws SQLException {
        String sql = "UPDATE event SET nom=?,description=?,image=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, event.getNom());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getImage());
        ps.setInt(4, event.getId());
        ps.executeUpdate();
        System.out.println("modifier event");

    }

    @Override
    public List<event> getList() throws SQLException {
        List<event> events = new ArrayList<>();
        String sql = "select * from event";
        try (Statement ps = cnx.createStatement();
            ResultSet rs = ps.executeQuery(sql)) {
            int count =0;
            while (rs.next()) {
                count++;
                event e =new event(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getInt("categorie_id")

                );
                events.add(e);
                System.out.println("event ajoutée: " + e.getNom());
            }
            System.out.println("Total events chargées: " + count);

        }catch (SQLException e) {
            System.err.println("ERREUR lors du chargement: " + e.getMessage());
            throw e;
        }

        return events;
    }
}
