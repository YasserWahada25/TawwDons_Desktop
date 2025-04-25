package dao;

import models.MiniJeuScore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class MiniJeuScoreDAO extends BaseDAO {

    public void enregistrer(MiniJeuScore score) {
        String sql = "INSERT INTO mini_jeu_score (joueur, score, niveau, date_jeu) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, score.getJoueur());
            stmt.setInt(2, score.getScore());
            stmt.setString(3, score.getNiveau());
            stmt.setTimestamp(4, Timestamp.valueOf(score.getDate()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
