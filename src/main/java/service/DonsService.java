package service;

import models.Dons;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonsService {
    private final Connection connection;

    public DonsService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public boolean ajouterDon(Dons don) {
        String req = "INSERT INTO dons (titre, description, date_creation, categorie, image_url, valide, donneur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, don.getTitre());
            ps.setString(2, don.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(don.getDateCreation()));
            ps.setString(4, don.getCategorie());
            ps.setString(5, don.getImageUrl());
            ps.setBoolean(6, don.isValide());
            ps.setInt(7, don.getDonneurId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout Don: " + e.getMessage());
            return false;
        }
    }

    // Retourner tous les dons en attente
    public List<Dons> getDonsNonValidés() {
        List<Dons> list = new ArrayList<>();
        String sql = "SELECT * FROM dons WHERE valide = false";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Dons don = new Dons();
                don.setId(rs.getInt("id"));
                don.setTitre(rs.getString("titre"));
                don.setDescription(rs.getString("description"));
                don.setDateCreation(rs.getDate("date_creation").toLocalDate());
                don.setCategorie(rs.getString("categorie"));
                don.setImageUrl(rs.getString("image_url"));
                list.add(don);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return list;
    }

    // Accepter un don
    public void accepterDon(int id) {
        String sql = "UPDATE dons SET valide = true WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur validation don : " + e.getMessage());
        }
    }

    // Refuser un don (supprimer)
    public void refuserDon(int id) {
        String sql = "DELETE FROM dons WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression don : " + e.getMessage());
        }
    }


    public List<Dons> getDonsValidés() {
        List<Dons> list = new ArrayList<>();
        String sql = "SELECT * FROM dons WHERE valide = true";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Dons don = new Dons();
                don.setId(rs.getInt("id"));
                don.setTitre(rs.getString("titre"));
                don.setDescription(rs.getString("description"));
                don.setDateCreation(rs.getDate("date_creation").toLocalDate());
                don.setCategorie(rs.getString("categorie"));
                don.setImageUrl(rs.getString("image_url"));
                list.add(don);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des dons validés : " + e.getMessage());
        }
        return list;
    }


}