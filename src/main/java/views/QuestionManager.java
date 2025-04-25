package views;

import dao.QuestionDAO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Evaluation;
import models.Question;

import java.util.List;

public class QuestionManager {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private TableView<Question> table;
    private TextField contenuField;
    private TextField reponseField;
    private ComboBox<String> typeBox;

    private Evaluation currentEvaluation;

    public void show(Evaluation evaluation) {
        this.currentEvaluation = evaluation;

        Stage stage = new Stage();
        stage.setTitle("Questions - " + evaluation.getNom());

        contenuField = new TextField();
        contenuField.setPromptText("Contenu de la question");

        reponseField = new TextField();
        reponseField.setPromptText("Bonne réponse");

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("qcm", "numeric");
        typeBox.setPromptText("Type");

        Button addBtn = new Button("Ajouter");
        addBtn.setOnAction(e -> ajouterQuestion());

        Button updateBtn = new Button("Modifier");
        updateBtn.setOnAction(e -> modifierQuestion());

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setOnAction(e -> supprimerQuestion());

        HBox form = new HBox(10, contenuField, reponseField, typeBox, addBtn, updateBtn, deleteBtn);
        form.setPadding(new Insets(10));

        table = new TableView<>();
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Question, String> contenuCol = new TableColumn<>("Contenu");
        contenuCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));

        TableColumn<Question, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Question, String> repCol = new TableColumn<>("Bonne réponse");
        repCol.setCellValueFactory(new PropertyValueFactory<>("bonneReponse"));

        table.getColumns().addAll(idCol, contenuCol, typeCol, repCol);
        table.setPrefHeight(300);

        // Remplir les champs quand une question est sélectionnée
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                contenuField.setText(newSel.getContenu());
                reponseField.setText(newSel.getBonneReponse());
                typeBox.setValue(newSel.getType());
            }
        });

        VBox layout = new VBox(10, form, table);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 800, 450);
        stage.setScene(scene);
        stage.show();

        loadQuestions();
    }

    private void ajouterQuestion() {
        String contenu = contenuField.getText();
        String bonneRep = reponseField.getText();
        String type = typeBox.getValue();

        if (contenu.isEmpty() || type == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        Question q = new Question();
        q.setContenu(contenu);
        q.setBonneReponse(bonneRep);
        q.setType(type);
        q.setEvaluationId(currentEvaluation.getId());

        if (questionDAO.add(q)) {
            showAlert("Succès", "Question ajoutée !");
            clearForm();
            loadQuestions();
        } else {
            showAlert("Erreur", "Échec de l'ajout.");
        }
    }

    private void modifierQuestion() {
        Question selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Veuillez sélectionner une question.");
            return;
        }

        selected.setContenu(contenuField.getText());
        selected.setBonneReponse(reponseField.getText());
        selected.setType(typeBox.getValue());

        if (questionDAO.update(selected)) {
            showAlert("Succès", "Question mise à jour !");
            clearForm();
            loadQuestions();
        } else {
            showAlert("Erreur", "Échec de la mise à jour.");
        }
    }

    private void supprimerQuestion() {
        Question selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Veuillez sélectionner une question à supprimer.");
            return;
        }

        if (questionDAO.delete(selected.getId())) {
            showAlert("Succès", "Question supprimée !");
            clearForm();
            loadQuestions();
        } else {
            showAlert("Erreur", "Échec de la suppression.");
        }
    }

    private void loadQuestions() {
        List<Question> questions = questionDAO.getByEvaluationId(currentEvaluation.getId());
        table.getItems().setAll(questions);
    }

    private void clearForm() {
        contenuField.clear();
        reponseField.clear();
        typeBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
