package dao;

import models.Evaluation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO extends BaseDAO implements ICRUD<Evaluation> {

    @Override
    public boolean add(Evaluation evaluation) {
        String sql = "INSERT INTO evaluation (nom, description, type) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getNom());
            stmt.setString(2, evaluation.getDescription());
            stmt.setString(3, evaluation.getType());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Evaluation evaluation) {
        String sql = "UPDATE evaluation SET nom=?, description=?, type=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getNom());
            stmt.setString(2, evaluation.getDescription());
            stmt.setString(3, evaluation.getType());
            stmt.setInt(4, evaluation.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM evaluation WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Evaluation getById(int id) {
        String sql = "SELECT * FROM evaluation WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Evaluation(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getTimestamp("date_creation").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int getEvaluationIdFromQuestionId(int questionId) {
        String sql = "SELECT evaluation_id FROM question WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("evaluation_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluation";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Evaluation(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getTimestamp("date_creation").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
