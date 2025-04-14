package service;

import models.Offre;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreService {
    private final Connection connection;

    public OffreService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public boolean ajouterOffre(Offre offre) {
        String sql = "INSERT INTO offre (titre_offre, description_offre) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, offre.getTitreOffre());
            ps.setString(2, offre.getDescriptionOffre());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return false;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    offre.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout Offre: " + e.getMessage());
            return false;
        }
    }

    public Offre getOffreById(int id) {
        String sql = "SELECT * FROM offre WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Offre(
                            rs.getInt("id"),
                            rs.getString("titre_offre"),
                            rs.getString("description_offre")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture Offre: " + e.getMessage());
        }
        return null;
    }

    public List<Offre> getAllOffres() {
        List<Offre> offres = new ArrayList<>();
        String sql = "SELECT * FROM offre";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                offres.add(new Offre(
                        rs.getInt("id"),
                        rs.getString("titre_offre"),
                        rs.getString("description_offre")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur liste Offres: " + e.getMessage());
        }
        return offres;
    }

    public boolean updateOffre(Offre offre) {
        String sql = "UPDATE offre SET titre_offre = ?, description_offre = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, offre.getTitreOffre());
            ps.setString(2, offre.getDescriptionOffre());
            ps.setInt(3, offre.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour Offre: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteOffre(int id) {
        String sql = "DELETE FROM offre WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression Offre: " + e.getMessage());
            return false;
        }
    }
}
