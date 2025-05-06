package dao;

import models.MiniJeuScore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class MiniJeuScoreDAO extends BaseDAO {

    // Enregistre un score de mini jeu
    public void enregistrer(MiniJeuScore score) {
        String sql = "INSERT INTO mini_jeu_score (joueur, score, niveau, date_jeu) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, score.getJoueur());
            stmt.setInt(2, score.getScore());
            stmt.setString(3, score.getNiveau());
            stmt.setTimestamp(4, Timestamp.valueOf(score.getDate()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement du score : " + e.getMessage());
        }
    }

    // Vérifie si l'utilisateur a déjà joué un niveau
    public boolean aDejaJoueCeNiveau(String joueur, String niveau) {
        String sql = "SELECT COUNT(*) FROM mini_jeu_score WHERE joueur = ? AND niveau = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, joueur);
            stmt.setString(2, niveau);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de rejouabilité : " + e.getMessage());
            return false;
        }
    }

    // Récupère le meilleur score pour un niveau donné
    public int getMeilleurScore(String niveau) {
        String sql = "SELECT MAX(score) FROM mini_jeu_score WHERE niveau = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, niveau);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération meilleur score : " + e.getMessage());
            return 0;
        }
    }

    // Calcule la moyenne des scores pour un niveau
    public double getMoyenneScore(String niveau) {
        String sql = "SELECT AVG(score) FROM mini_jeu_score WHERE niveau = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, niveau);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération moyenne score : " + e.getMessage());
            return 0.0;
        }
    }

    // Récupère l'objet complet du meilleur score (facultatif)
    public Optional<MiniJeuScore> meilleurScore(String niveau) {
        String sql = "SELECT * FROM mini_jeu_score WHERE niveau = ? ORDER BY score DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, niveau);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new MiniJeuScore(
                        rs.getString("joueur"),
                        rs.getInt("score"),
                        rs.getString("niveau"),
                        rs.getTimestamp("date_jeu").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération meilleur score détaillé : " + e.getMessage());
        }
        return Optional.empty();
    }
}
