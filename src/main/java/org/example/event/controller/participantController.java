package org.example.event.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.event.model.event;
import org.example.event.model.participant;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.example.event.service.participantService;


public class participantController {
    @FXML private TableView<participant> participantTable;
    @FXML private TableColumn<participant, Integer> idColumn;
    @FXML private TableColumn<participant, String> nomColumn;
    @FXML private TableColumn<participant, String> prenomColumn;
    @FXML private TableColumn<participant, String> emailColumn;
    @FXML private TableColumn<participant, Integer> numtelColumn;
    @FXML private TableColumn<participant, Void> actionsColumn;
    @FXML private TableColumn<participant, Integer> eventIdColumn;

    private final ObservableList<participant> participantData = FXCollections.observableArrayList();
    private final participantService participantService = new participantService();
    private event currentEvent;

    @FXML
    public void initialize() {
        configureTableColumns();
        setupActionsColumn();
    }

    public void initData(event event) {
        this.currentEvent = event;
        loadParticipants();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numtelColumn.setCellValueFactory(new PropertyValueFactory<>("numtel"));

    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsContainer = new HBox(editBtn, deleteBtn);

            {
                buttonsContainer.setSpacing(8);
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");

                editBtn.setOnAction(event -> {
                    participant p = getTableView().getItems().get(getIndex());
                    handleUpdateParticipant(p);
                });

                deleteBtn.setOnAction(event -> {
                    participant p = getTableView().getItems().get(getIndex());
                    handleDeleteParticipant(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsContainer);
            }
        });
    }

    private void loadParticipants() {
        try {
            List<participant> participants = participantService.getParticipantByEvent(  currentEvent.getId());
            Platform.runLater(() -> {
                participantData.setAll(participants);
                participantTable.setItems(participantData);
            });
        } catch (SQLException e) {
            showErrorAlert("Erreur BDD", "Échec chargement", e.getMessage());
        }
    }


    @FXML
    private void handleAddParticipant() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/add-participant.fxml"));
            Parent root = loader.load();

            AddParticipantController controller = loader.getController();
            controller.setEventId(currentEvent.getId());

            Stage stage = new Stage();
            controller.setDialogStage(stage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(participantTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadParticipants();
        } catch (IOException e) {
            showErrorAlert("Erreur UI", "Ouverture impossible", e.getMessage());
        }
    }


    private void handleDeleteParticipant(participant p) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation suppression");
        confirmation.setHeaderText("Supprimer " + p.getNom());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce participant?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                participantService.supprimer(p);
                loadParticipants();
                showSuccessAlert("Succès", "Participant supprimé", p.getNom() + " a été supprimé");
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Échec de suppression", e.getMessage());
            }
        }
    }

    private void handleUpdateParticipant(participant p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/update-participant.fxml"));
            Parent root = loader.load();

            UpdateParticipantController controller = loader.getController();
            controller.setParticipant(p);

            Stage stage = new Stage();
            controller.setDialogStage(stage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(participantTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadParticipants();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Formulaire inaccessible", e.getMessage());
        }
    }
    @FXML
    private void handleExportExcel() {
        // 1. Configurer la boîte de dialogue de sauvegarde
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer fichier Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));

        String defaultFileName = "participants_event" +
                (currentEvent != null ? currentEvent.getId() : "") +
                "_" + System.currentTimeMillis() + ".xlsx";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(participantTable.getScene().getWindow());

        if (file != null) {
            try {
                // . Récupérer la liste actuelle des participants
                List<participant> participants = participantTable.getItems();

                if (participants == null || participants.isEmpty()) {
                    showErrorAlert("Erreur", "Export impossible",
                            "Aucun participant à exporter dans le tableau.");
                    return;
                }

                // . Effectuer l'export
                participantService.exportToExcel(participants, file.getAbsolutePath());

                // . Afficher la confirmation
                showSuccessAlert("Export réussi",
                        "Fichier créé : " + file.getName(),
                        participants.size() + " participants ont été exportés.");

            } catch (IOException e) {
                showErrorAlert("Erreur fichier",
                        "Échec d'écriture",
                        "Impossible d'écrire le fichier Excel : " + e.getMessage());
            } catch (Exception e) {
                showErrorAlert("Erreur",
                        "Export impossible",
                        "Erreur inattendue : " + e.getMessage());
            }
        }
    }


    private void showSuccessAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
