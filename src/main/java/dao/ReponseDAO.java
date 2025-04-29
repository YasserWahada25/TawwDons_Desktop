package dao;

import models.Reponse;
import utils.ProfanityFilter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReponseDAO extends BaseDAO implements ICRUD<Reponse> {

    @Override
    public boolean add(Reponse reponse) {
        String sql = "INSERT INTO reponse (question_id, utilisateur, reponse, bonne, date_reponse) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reponse.getQuestionId());
            stmt.setString(2, reponse.getUtilisateur());
            stmt.setString(3, reponse.getReponse());
            stmt.setBoolean(4, reponse.isBonne());
            stmt.executeUpdate();

            // ✅ Vérifie et bannit si nécessaire
            List<String> motsInterdits = ProfanityFilter.extractBadWords(reponse.getReponse());
            if (!motsInterdits.isEmpty()) {
                BanDAO banDAO = BanDAO.getInstance();
                for (String mot : motsInterdits) {
                    if (!banDAO.isBanni(reponse.getUtilisateur())) {
                        banDAO.ban(reponse.getUtilisateur(), mot);
                    }
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Reponse reponse) {
        String sql = "UPDATE reponse SET reponse = ?, bonne = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reponse.getReponse());
            stmt.setBoolean(2, reponse.isBonne());
            stmt.setInt(3, reponse.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM reponse WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteByEvaluationId(int evalId) {
        String sql = "DELETE FROM reponse WHERE question_id IN (SELECT id FROM question WHERE evaluation_id = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, evalId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Reponse getById(int id) {
        String sql = "SELECT * FROM reponse WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Reponse(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getString("utilisateur"),
                        rs.getString("reponse"),
                        rs.getTimestamp("date_reponse").toLocalDateTime(),
                        rs.getBoolean("bonne")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Reponse(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getString("utilisateur"),
                        rs.getString("reponse"),
                        rs.getTimestamp("date_reponse").toLocalDateTime(),
                        rs.getBoolean("bonne")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reponse> getByUtilisateur(String utilisateur) {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse WHERE utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Reponse(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getString("utilisateur"),
                        rs.getString("reponse"),
                        rs.getTimestamp("date_reponse").toLocalDateTime(),
                        rs.getBoolean("bonne")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
