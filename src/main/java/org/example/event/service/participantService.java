package org.example.event.service;

import org.example.event.model.participant;
import org.example.event.utils.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class participantService implements Iparticipant<participant>{
    Connection cnx;

    public participantService() {
        cnx = database.getInstance().getCnx();
    }

    @Override
    public void ajouter(participant participant) throws SQLException {
        String sql = "INSERT INTO participant(nom, prenom, email, numtel, event_id) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, participant.getNom());
            ps.setString(2, participant.getPrenom());
            ps.setString(3, participant.getEmail());
            ps.setInt(4, participant.getNumtel());
            ps.setInt(5, participant.getEvent_id());
            ps.executeUpdate();
            System.out.println("Participant ajouté");
        }
    }

    @Override
    public void supprimer(participant participant) throws SQLException {
        String sql = "DELETE FROM participant WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participant.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(participant participant) throws SQLException {
        String sql = "UPDATE participant SET nom=?, prenom=?, email=?, numtel=?, event_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, participant.getNom());
            ps.setString(2, participant.getPrenom());
            ps.setString(3, participant.getEmail());
            ps.setInt(4, participant.getNumtel());
            ps.setInt(5, participant.getEvent_id());
            ps.setInt(6, participant.getId());
            ps.executeUpdate();
            System.out.println("Participant modifié");
        }
    }

    @Override
    public List<participant> getList() throws SQLException {
        List<participant> participants = new ArrayList<>();
        String sql = "SELECT * FROM participant";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                participant p = new participant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getInt("numtel"),
                        rs.getInt("event_id")
                );
                participants.add(p);
                System.out.println("Participant chargé: " + p.getNom());
            }
            System.out.println("Total participants: " + participants.size());
        }
        return participants;
    }
    public List<participant> getParticipantByEvent(int eventId) throws SQLException {
        System.out.println("Exécution de getParticipantByEvent()...");
        List<participant> participants = new ArrayList<>();
        String query = "SELECT * FROM participant WHERE event_id = ?";
        System.out.println("Requête SQL: " + query);

        try (PreparedStatement st = cnx.prepareStatement(query)) {
            st.setInt(1, eventId); // Paramètre défini AVANT l'exécution
            System.out.println("Paramètre participantid_id défini à: " + eventId);

            System.out.println("Exécution de la requête...");
            try (ResultSet rs = st.executeQuery()) { // Exécution après paramétrage
                int count = 0;

                while (rs.next()) {
                    count++;
                    participant m = new participant(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getInt("numtel"),
                            rs.getInt("event_id")
                    );

                    participants.add(m);
                    System.out.println("participant ajouté: " + m.getNom());
                }
                System.out.println("Total participants chargés: " + count); // Correction du message
            }
        } catch (SQLException e) {
            System.err.println("ERREUR lors du chargement: " + e.getMessage());
            throw e;
        }
        return participants;
    }
}
