package controllers;

import dao.QuestionDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Evaluation;
import models.Question;

import java.util.List;

public class QuestionsController {

    @FXML private Label titreLabel;
    @FXML private TextField contenuField, bonneReponseField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TableView<Question> tableView;
    @FXML private TableColumn<Question, Integer> idCol;
    @FXML private TableColumn<Question, String> contenuCol, typeCol, bonneCol;

    private final QuestionDAO dao = new QuestionDAO();
    private Evaluation currentEvaluation;

    public void setEvaluation(Evaluation evaluation) {
        this.currentEvaluation = evaluation;
        titreLabel.setText("Questions pour : " + evaluation.getName());
        loadQuestions();
    }

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("qcm", "numeric");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        contenuCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        bonneCol.setCellValueFactory(new PropertyValueFactory<>("bonneReponse"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                contenuField.setText(newVal.getContenu());
                bonneReponseField.setText(newVal.getBonneReponse());
                typeBox.setValue(newVal.getType());
            }
        });
    }

    private void loadQuestions() {
        if (currentEvaluation != null) {
            List<Question> list = dao.getByEvaluationId(currentEvaluation.getId());
            tableView.getItems().setAll(list);
        }
    }

    @FXML
    private void ajouterQuestion() {
        if (!validerChamps()) return;

        Question q = new Question();
        q.setEvaluationId(currentEvaluation.getId());
        q.setContenu(contenuField.getText().trim());
        q.setType(typeBox.getValue());
        q.setBonneReponse(bonneReponseField.getText().trim());

        dao.add(q);
        clearForm();
        loadQuestions();
    }

    @FXML
    private void modifierQuestion() {
        Question selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            selected.setContenu(contenuField.getText().trim());
            selected.setType(typeBox.getValue());
            selected.setBonneReponse(bonneReponseField.getText().trim());

            dao.update(selected);
            clearForm();
            loadQuestions();
        }
    }

    @FXML
    private void supprimerQuestion() {
        Question selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dao.delete(selected.getId());
            clearForm();
            loadQuestions();
        }
    }

    @FXML
    private void retourAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Admin.fxml"));
            Stage stage = (Stage) contenuField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur", "Impossible de revenir à l'accueil.");
        }
    }

    private void clearForm() {
        contenuField.clear();
        bonneReponseField.clear();
        typeBox.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    private boolean validerChamps() {
        String contenu = contenuField.getText().trim();
        String bonneReponse = bonneReponseField.getText().trim();
        String type = typeBox.getValue();

        if (contenu.isEmpty() || bonneReponse.isEmpty() || type == null) {
            alert("Erreur", "Tous les champs sont obligatoires.");
            return false;
        }

        if (!contenu.matches("^[A-Z][a-zA-Z0-9\\s]*$")) {
            alert("Erreur", "Le contenu doit commencer par une majuscule et ne pas contenir de caractères spéciaux.");
            return false;
        }

        if (!bonneReponse.matches("^[A-Z]?[a-zA-Z0-9\\s]+$")) {
            alert("Erreur", "La bonne réponse ne doit pas contenir de caractères spéciaux.");
            return false;
        }

        return true;
    }

    private void alert(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
