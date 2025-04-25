package dao;

import models.Question;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO extends BaseDAO implements ICRUD<Question> {

    @Override
    public boolean add(Question question) {
        String sql = "INSERT INTO question (evaluation_id, contenu, type, bonne_reponse) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, question.getEvaluationId());
            stmt.setString(2, question.getContenu());
            stmt.setString(3, question.getType());
            stmt.setString(4, question.getBonneReponse());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Question question) {
        String sql = "UPDATE question SET contenu=?, type=?, bonne_reponse=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, question.getContenu());
            stmt.setString(2, question.getType());
            stmt.setString(3, question.getBonneReponse());
            stmt.setInt(4, question.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM question WHERE id=?";
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
    public Question getById(int id) {
        String sql = "SELECT * FROM question WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Question(
                        rs.getInt("id"),
                        rs.getInt("evaluation_id"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getString("bonne_reponse")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Question> getAll() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM question";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Question(
                        rs.getInt("id"),
                        rs.getInt("evaluation_id"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getString("bonne_reponse")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Question> getByEvaluationId(int evalId) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM question WHERE evaluation_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, evalId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Question(
                        rs.getInt("id"),
                        rs.getInt("evaluation_id"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getString("bonne_reponse")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
