package org.example.event.service;

import org.example.event.model.event;
import org.example.event.utils.database;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class eventService implements Ievent <event> {
    Connection cnx;
    public eventService() {cnx= database.getInstance().getCnx();}

    @Override
    public void ajouter(event event) throws SQLException, IOException {
        // Vérifier d'abord si la catégorie existe
        String checkCategorySql = "SELECT id FROM categorie WHERE id = ?";
        try (PreparedStatement checkStmt = cnx.prepareStatement(checkCategorySql)) {
            checkStmt.setInt(1, event.getCategorie_id());
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("La catégorie avec l'ID " + event.getCategorie_id() + " n'existe pas");
            }
        }
        String sql ="insert into event(nom,description,image,categorie_id,date,location) values(?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, event.getNom());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getImage());
        ps.setInt(4,event.getCategorie_id());
        ps.setDate(5, Date.valueOf(event.getDate()));
        ps.setString(6, event.getLocation());

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
        String sql = "UPDATE event SET nom=?,description=?,image=? WHERE id=?,date=?,location=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, event.getNom());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getImage());
        ps.setInt(4, event.getId());
        ps.setDate(5, Date.valueOf(event.getDate()));
        ps.setString(6, event.getLocation());

        ps.executeUpdate();
        System.out.println("modifier event");

    }

    @Override
    public List<event> getList() throws SQLException {
        List<event> events = new ArrayList<>();
        String sql = "SELECT * FROM event";

        try (Statement ps = cnx.createStatement();
             ResultSet rs = ps.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;

                // Gestion de la date qui peut être null
                java.sql.Date sqlDate = rs.getDate("date");
                LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                event e = new event(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getInt("categorie_id"),
                        localDate, // ta date
                        rs.getString("location") // AJOUT de la location ici
                );

                events.add(e);
                System.out.println("Événement ajouté: " + e.getNom() +
                        " | Date: " + (localDate != null ? localDate : "[null]"));
            }

            System.out.println("Total événements chargés: " + count);

        } catch (SQLException e) {
            System.err.println("ERREUR lors du chargement: " + e.getMessage());
            throw e;
        }

        return events;
    }
}
