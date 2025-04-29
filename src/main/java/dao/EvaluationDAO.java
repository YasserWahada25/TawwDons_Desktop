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
        String sql = "INSERT INTO evaluation (name, description, type, is_archived) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getName());
            stmt.setString(2, evaluation.getDescription());
            stmt.setString(3, evaluation.getType());
            stmt.setBoolean(4, evaluation.isArchived());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Evaluation evaluation) {
        String sql = "UPDATE evaluation SET name=?, description=?, type=?, is_archived=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getName());
            stmt.setString(2, evaluation.getDescription());
            stmt.setString(3, evaluation.getType());
            stmt.setBoolean(4, evaluation.isArchived());
            stmt.setInt(5, evaluation.getId());
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
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getBoolean("is_archived")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluation WHERE is_archived = FALSE"; // ⬅️ on ignore les archivées
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Evaluation(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getBoolean("is_archived")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public int getEvaluationIdFromQuestionId(int questionId) {
        String sql = "SELECT evaluation_id FROM question WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("evaluation_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public List<Evaluation> getArchives() {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluation WHERE is_archived = TRUE";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Evaluation(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getBoolean("is_archived")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean archiverEvaluation(int id) {
        String sql = "UPDATE evaluation SET is_archived = TRUE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}