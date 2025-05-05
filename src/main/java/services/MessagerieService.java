package services;

import models.Messagerie;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessagerieService {
    private Connection connection;

    public MessagerieService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    // Vérifie et rétablit la connexion si besoin
    private void ensureConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = MyDataBase.getInstance().getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Messagerie> getMessagesByDemandeId(int demandeDonId) {
        ensureConnection();
        List<Messagerie> list = new ArrayList<>();
        // Ajout de id dans ORDER BY pour total déterminisme
        String sql = "SELECT * FROM messagerie " +
                "WHERE demande_don_id = ? " +
                "ORDER BY date_envoi ASC, id ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, demandeDonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Messagerie m = new Messagerie();
                    m.setId(rs.getInt("id"));
                    m.setExpediteurId(rs.getInt("expediteur_id"));
                    m.setDestinataireId(rs.getInt("destinataire_id"));
                    m.setDemandeDonId(rs.getInt("demande_don_id"));
                    m.setContenu(rs.getString("contenu"));
                    m.setDateEnvoi(rs.getTimestamp("date_envoi").toLocalDateTime());
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean envoyerMessage(Messagerie m) {
        ensureConnection();
        String sql = "INSERT INTO messagerie " +
                "(expediteur_id, destinataire_id, demande_don_id, contenu, date_envoi) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, m.getExpediteurId());
            ps.setInt(2, m.getDestinataireId());
            ps.setInt(3, m.getDemandeDonId());
            ps.setString(4, m.getContenu());
            ps.setTimestamp(5, Timestamp.valueOf(m.getDateEnvoi()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
