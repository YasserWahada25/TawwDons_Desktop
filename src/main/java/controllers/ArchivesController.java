package controllers;

import dao.EvaluationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Evaluation;
import utils.Navigation;

import java.util.List;
import java.util.stream.Collectors;

public class ArchivesController {

    @FXML private TableView<Evaluation> archivesTable;
    @FXML private TableColumn<Evaluation, Integer> idCol;
    @FXML private TableColumn<Evaluation, String> nomCol;
    @FXML private TableColumn<Evaluation, String> typeCol;
    @FXML private TableColumn<Evaluation, String> dateCol;

    private final EvaluationDAO dao = new EvaluationDAO();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        List<Evaluation> archives = dao.getAll().stream()
                .filter(e -> e.getNom().toLowerCase().contains("archive")) // ou utilise une colonne booléenne is_archived
                .collect(Collectors.toList());

        archivesTable.getItems().setAll(archives);
    }

    @FXML
    private void retourAdmin() {
        Navigation.goTo("Admin.fxml");
    }
}
