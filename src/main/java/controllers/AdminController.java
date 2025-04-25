package controllers;

import dao.EvaluationDAO;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Evaluation;
import utils.PDFExporter;

import java.io.File;
import java.util.List;

public class AdminController {

    @FXML private TextField nomField, descriptionField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TableView<Evaluation> tableView;
    @FXML private TableColumn<Evaluation, Integer> idCol;
    @FXML private TableColumn<Evaluation, String> nomCol, typeCol, dateCol;
    @FXML private TextField searchField;
    @FXML private StackPane rootPane;

    private final EvaluationDAO dao = new EvaluationDAO();
    private boolean darkMode = true;

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("qcm", "numeric");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nomField.setText(newVal.getNom());
                descriptionField.setText(newVal.getDescription());
                typeBox.setValue(newVal.getType());
            }
        });

        loadEvaluations();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Evaluation> all = dao.getAll();
            String lower = newVal.toLowerCase();
            tableView.getItems().setAll(
                    all.stream()
                            .filter(e -> e.getNom().toLowerCase().contains(lower)
                                    || e.getType().toLowerCase().contains(lower))
                            .toList()
            );
        });
    }

    @FXML
    private void exporterPDF() {
        try {
            List<Evaluation> evaluations = dao.getAll();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter vers PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

            if (file != null) {
                PDFExporter.exportEvaluations(evaluations, file.getAbsolutePath());
                showToast("✅ PDF exporté avec succès !");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌ Erreur lors de l’export.");
        }
    }

    @FXML
    private void toggleTheme() {
        Scene scene = tableView.getScene();
        Parent root = scene.getRoot();

        if (darkMode) {
            root.setStyle("-fx-background-color: white;");
            tableView.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            nomField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            descriptionField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            typeBox.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            searchField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            darkMode = false;
        } else {
            root.setStyle("-fx-background-color: #121212;");
            tableView.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
            nomField.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
            descriptionField.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
            typeBox.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
            searchField.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
            darkMode = true;
        }
    }

    private void loadEvaluations() {
        List<Evaluation> list = dao.getAll();
        tableView.getItems().setAll(list);
        animateTable();
    }

    private void animateTable() {
        tableView.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), tableView);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private boolean nomValide(String nom) {
        return nom.matches("^[A-Z][a-zA-Z0-9\\s]*$");
    }

    @FXML
    private void ajouterEvaluation() {
        String nom = nomField.getText().trim();
        String desc = descriptionField.getText().trim();
        String type = typeBox.getValue();

        if (nom.isEmpty() || desc.isEmpty() || type == null || type.isEmpty()) {
            showToast("❌ Tous les champs doivent être remplis.");
            return;
        }

        if (!nomValide(nom)) {
            showToast("⚠️ Nom invalide. Il doit commencer par une majuscule.");
            return;
        }

        Evaluation e = new Evaluation();
        e.setNom(nom);
        e.setDescription(desc);
        e.setType(type);

        dao.add(e);
        clearForm();
        loadEvaluations();
        showToast("✅ Évaluation ajoutée !");
    }

    @FXML
    private void modifierEvaluation() {
        Evaluation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showToast("❌ Veuillez sélectionner une évaluation.");
            return;
        }

        selected.setNom(nomField.getText().trim());
        selected.setDescription(descriptionField.getText().trim());
        selected.setType(typeBox.getValue());

        dao.update(selected);
        clearForm();
        loadEvaluations();
        showToast("✏️ Évaluation modifiée.");
    }

    @FXML
    private void supprimerEvaluation() {
        Evaluation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Supprimer cette évaluation ?");
            confirm.initModality(Modality.APPLICATION_MODAL);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dao.delete(selected.getId());
                    clearForm();
                    loadEvaluations();
                    showToast("🗑️ Évaluation supprimée.");
                }
            });
        } else {
            showToast("❌ Sélectionnez une évaluation.");
        }
    }

    private void clearForm() {
        nomField.clear();
        descriptionField.clear();
        typeBox.setValue(null);
    }

    private void showToast(String message) {
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold;");
        rootPane.getChildren().add(toast);

        FadeTransition fade = new FadeTransition(Duration.seconds(3), toast);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> rootPane.getChildren().remove(toast));
        fade.play();
    }

    private void alert(String titre, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
    @FXML
    private void ouvrirListeBannis() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Bannis.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Utilisateurs Bannis");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur", "Impossible d’ouvrir la liste des bannis.");
        }
    }

    @FXML
    private void ouvrirQuestions() {
        Evaluation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showToast("❗ Sélectionnez une évaluation.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Questions.fxml"));
            Parent root = loader.load();
            QuestionsController controller = loader.getController();
            controller.setEvaluation(selected);
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Questions - " + selected.getNom());
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Erreur d'ouverture Questions.");
        }
    }

    @FXML
    private void ouvrirReponses() {
        Evaluation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showToast("❗ Sélectionnez une évaluation.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Reponses.fxml"));
            Parent root = loader.load();
            ReponsesController controller = loader.getController();
            controller.setEvaluation(selected);
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réponses - " + selected.getNom());
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Erreur d'ouverture Réponses.");
        }
    }

    @FXML
    private void ouvrirArchives() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Archives.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("🗃️ Archives");
        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌ Erreur lors de l'ouverture des archives.");
        }
    }
    @FXML
    private void ouvrirDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("📊 Tableau de Bord");
        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌ Erreur lors de l'ouverture du Dashboard.");
        }
    }


    @FXML private void retourAccueil() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Acceuil.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur", "Retour impossible.");
        }
    }
}
