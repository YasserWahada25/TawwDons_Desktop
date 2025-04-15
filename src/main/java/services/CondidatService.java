package services;

import models.Condidat;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CondidatService {

    // Récupérer tous les candidats
    public List<Condidat> getAllCondidats() {
        List<Condidat> condidats = new ArrayList<>();
        String query = "SELECT * FROM condidat";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Condidat condidat = new Condidat();
                condidat.setId(rs.getInt("id"));
                condidat.setOffreId(rs.getObject("offre_id", Integer.class));
                condidat.setNom(rs.getString("nom"));
                condidat.setPrenom(rs.getString("prenom"));
                condidat.setEmail(rs.getString("email"));
                condidat.setTelephone(rs.getInt("telephone"));
                condidat.setCv(rs.getString("cv"));
                
                condidats.add(condidat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return condidats;
    }
    
    // Récupérer un candidat par son ID
    public Condidat getCondidatById(int id) {
        String query = "SELECT * FROM condidat WHERE id = ?";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Condidat condidat = new Condidat();
                condidat.setId(rs.getInt("id"));
                condidat.setOffreId(rs.getObject("offre_id", Integer.class));
                condidat.setNom(rs.getString("nom"));
                condidat.setPrenom(rs.getString("prenom"));
                condidat.setEmail(rs.getString("email"));
                condidat.setTelephone(rs.getInt("telephone"));
                condidat.setCv(rs.getString("cv"));
                
                return condidat;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Ajouter un nouveau candidat
    public boolean ajouterCondidat(Condidat condidat) {
        String query = "INSERT INTO condidat (offre_id, nom, prenom, email, telephone, cv) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            System.out.println("Tentative d'ajout du candidat dans la base de données :");
            System.out.println("Offre ID : " + condidat.getOffreId());
            System.out.println("Nom : " + condidat.getNom());
            System.out.println("Prénom : " + condidat.getPrenom());
            System.out.println("Email : " + condidat.getEmail());
            System.out.println("Téléphone : " + condidat.getTelephone());
            System.out.println("CV : " + condidat.getCv());
            
            pstmt.setObject(1, condidat.getOffreId());
            pstmt.setString(2, condidat.getNom());
            pstmt.setString(3, condidat.getPrenom());
            pstmt.setString(4, condidat.getEmail());
            pstmt.setInt(5, condidat.getTelephone());
            pstmt.setString(6, condidat.getCv());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Nombre de lignes affectées : " + rowsAffected);
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du candidat : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Mettre à jour un candidat existant
    public boolean updateCondidat(Condidat condidat) {
        String query = "UPDATE condidat SET offre_id = ?, nom = ?, prenom = ?, email = ?, telephone = ?, cv = ? WHERE id = ?";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setObject(1, condidat.getOffreId());
            pstmt.setString(2, condidat.getNom());
            pstmt.setString(3, condidat.getPrenom());
            pstmt.setString(4, condidat.getEmail());
            pstmt.setInt(5, condidat.getTelephone());
            pstmt.setString(6, condidat.getCv());
            pstmt.setInt(7, condidat.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Supprimer un candidat
    public boolean deleteCondidat(int id) {
        String query = "DELETE FROM condidat WHERE id = ?";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Récupérer les candidats par offre
    public List<Condidat> getCondidatsByOffre(int offreId) {
        List<Condidat> condidats = new ArrayList<>();
        String query = "SELECT * FROM condidat WHERE offre_id = ?";
        
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, offreId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Condidat condidat = new Condidat();
                condidat.setId(rs.getInt("id"));
                condidat.setOffreId(rs.getObject("offre_id", Integer.class));
                condidat.setNom(rs.getString("nom"));
                condidat.setPrenom(rs.getString("prenom"));
                condidat.setEmail(rs.getString("email"));
                condidat.setTelephone(rs.getInt("telephone"));
                condidat.setCv(rs.getString("cv"));
                
                condidats.add(condidat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return condidats;
    }
} 