package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BanDAO extends BaseDAO {

    private static final BanDAO instance = new BanDAO();

    private BanDAO() {}

    public static BanDAO getInstance() {
        return instance;
    }

    // ✅ Bannir un utilisateur
    public void ban(String utilisateur, String motInterdit) {
        if (isBanni(utilisateur)) return;

        String sql = "INSERT INTO ban (utilisateur, mot_interdit, date_ban) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur);
            stmt.setString(2, motInterdit);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors du bannissement : " + e.getMessage());
        }
    }

    // 🔎 Vérifie si un utilisateur est banni
    public boolean isBanni(String utilisateur) {
        String sql = "SELECT COUNT(*) FROM ban WHERE utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de bannissement : " + e.getMessage());
            return false;
        }
    }

    // 📋 Récupérer tous les utilisateurs bannis
    public List<BanInfo> getAllBannis() {
        List<BanInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM ban";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String utilisateur = rs.getString("utilisateur");
                String mot = rs.getString("mot_interdit");
                LocalDateTime date = rs.getTimestamp("date_ban").toLocalDateTime();
                list.add(new BanInfo(utilisateur, mot, date));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des bannis : " + e.getMessage());
        }
        return list;
    }

    // 🔎 Obtenir uniquement la liste des utilisateurs bannis (pour check rapide)
    public Set<String> getUtilisateursBannis() {
        Set<String> set = new HashSet<>();
        for (BanInfo b : getAllBannis()) {
            set.add(b.utilisateur);
        }
        return set;
    }

    // ✅ Débannir un utilisateur
    public void debannir(String utilisateur) {
        String sql = "DELETE FROM ban WHERE utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors du débannissement : " + e.getMessage());
        }
    }

    // ✅ Modèle de données pour stocker les infos d'un bannissement
    public static class BanInfo {
        public final String utilisateur;
        public final String mot;
        public final LocalDateTime date;

        public BanInfo(String utilisateur, String mot, LocalDateTime date) {
            this.utilisateur = utilisateur;
            this.mot = mot;
            this.date = date;
        }
    }
}
