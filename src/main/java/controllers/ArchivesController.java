package controllers;

import dao.EvaluationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Evaluation;
import utils.Navigation;

import java.util.List;

public class ArchivesController {

    @FXML private TableView<Evaluation> archivesTable;
    @FXML private TableColumn<Evaluation, Integer> idCol;
    @FXML private TableColumn<Evaluation, String> nomCol;
    @FXML private TableColumn<Evaluation, String> typeCol;
    @FXML private TableColumn<Evaluation, Object> dateCol;
    @FXML private Button retourAdminBtn;

    private final EvaluationDAO dao = new EvaluationDAO();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadArchives();
    }

    private void loadArchives() {
        List<Evaluation> archives = dao.getArchives();
        archivesTable.getItems().setAll(archives);
    }

    @FXML
    private void retourAdmin() {
        Navigation.goTo("Admin.fxml");
    }

    @FXML
    private void restaurerEvaluation() {
        Evaluation selected = archivesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une évaluation à restaurer.");
            return;
        }

        selected.setArchived(false);
        dao.update(selected);
        loadArchives();
        showAlert("Évaluation restaurée avec succès !");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
