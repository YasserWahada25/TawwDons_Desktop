package services;

import models.DemandeDons;
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
        String sql = """
        SELECT d.*, u.nom AS nomDonneur, u.email AS emailDonneur
        FROM dons d
        JOIN user u ON d.donneur_id = u.id
        WHERE d.valide = true
    """;

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
                don.setDonneurId(rs.getInt("donneur_id"));
                don.setValide(rs.getBoolean("valide"));

                // 🔥 Récupération du nom et de l'email du donneur
                don.setNomDonneur(rs.getString("nomDonneur"));
                don.setEmailDonneur(rs.getString("emailDonneur"));

                list.add(don);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des dons validés : " + e.getMessage());
        }
        return list;
    }

    public boolean existeDemandeEnAttente(int donId, int userId) {
        String query = "SELECT COUNT(*) FROM demande_dons WHERE dons_id = ? AND beneficiaire_id = ? AND statut IN ('En attente', 'Acceptée')";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, donId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean ajouterDemandeDon(int donId, int userId) {
        String sql = "INSERT INTO demande_dons (dons_id, beneficiaire_id, date_demande, statut, chat_actif) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, donId);
            ps.setInt(2, userId);
            ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            ps.setString(4, "En attente");
            ps.setBoolean(5, false); // ou true selon ton besoin
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<DemandeDons> getDemandesByBeneficiaire(int userId) {
        List<DemandeDons> list = new ArrayList<>();
        String sql = "SELECT dd.*, d.titre FROM demande_dons dd " +
                "JOIN dons d ON dd.dons_id = d.id " +
                "WHERE dd.beneficiaire_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DemandeDons d = new DemandeDons();
                d.setId(rs.getInt("id"));
                d.setDonsId(rs.getInt("dons_id"));
                d.setBeneficiaireId(rs.getInt("beneficiaire_id"));
                d.setStatut(rs.getString("statut"));
                d.setDateDemande(rs.getDate("date_demande").toLocalDate());
                d.setChatActif(rs.getBoolean("chat_actif"));

                // Ajouter le titre récupéré de la jointure
                Dons don = new Dons();
                don.setTitre(rs.getString("titre"));
                d.setDonTitre(don.getTitre()); // ✅ Correct

                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<DemandeDons> getDemandesReçuesParDonneur(int donneurId) {
        List<DemandeDons> list = new ArrayList<>();
        String sql = """
        SELECT dd.*, d.titre AS donTitre, u.nom AS beneficiaireNom
        FROM demande_dons dd
        JOIN dons d ON dd.dons_id = d.id
        JOIN user u ON dd.beneficiaire_id = u.id
        WHERE d.donneur_id = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, donneurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DemandeDons d = new DemandeDons();
                d.setId(rs.getInt("id"));
                d.setDonsId(rs.getInt("dons_id"));
                d.setBeneficiaireId(rs.getInt("beneficiaire_id"));
                d.setDateDemande(rs.getDate("date_demande").toLocalDate());
                d.setStatut(rs.getString("statut"));
                d.setChatActif(rs.getBoolean("chat_actif"));
                d.setDonTitre(rs.getString("donTitre"));
                d.setBeneficiaireNom(rs.getString("beneficiaireNom")); // 🔥 IMPORTANT
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public boolean modifierStatutDemande(int demandeId, String statut, boolean chatActif) {
        String sql = "UPDATE demande_dons SET statut = ?, chat_actif = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setBoolean(2, chatActif);
            ps.setInt(3, demandeId);
            return ps.executeUpdate() > 0; // retourne true si au moins 1 ligne modifiée
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public int getDonneurIdByDonId(int donId) {
        String sql = "SELECT donneur_id FROM dons WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, donId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("donneur_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // -1 pour indiquer une erreur ou ID introuvable
    }


    public boolean validerDemande(int demandeId) {
        String sql = "UPDATE demande_dons SET statut = 'Validée' WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, demandeId);
            return ps.executeUpdate() > 0; // ✅ Retourne true si au moins une ligne a été modifiée
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // ❌ Retourne false en cas d'erreur
        }
    }



    public Dons getDonById(int donId) {
        String sql = "SELECT * FROM dons WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, donId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Dons don = new Dons();
                don.setId(rs.getInt("id"));
                don.setTitre(rs.getString("titre"));
                don.setDescription(rs.getString("description"));
                don.setDateCreation(rs.getDate("date_creation").toLocalDate());
                don.setCategorie(rs.getString("categorie"));
                don.setImageUrl(rs.getString("image_url"));
                don.setDonneurId(rs.getInt("donneur_id"));
                don.setValide(rs.getBoolean("valide"));
                return don;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }









}