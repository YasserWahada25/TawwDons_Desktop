package org.example.event.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.event.model.event;
import org.example.event.service.eventService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class eventController {

    @FXML private TableView<event> eventTable;
    @FXML private TableColumn<event, Integer> idColumn;
    @FXML private TableColumn<event, String> nomColumn;
    @FXML private TableColumn<event, String> imageColumn;
    @FXML private TableColumn<event, String> descriptionColumn;
    @FXML private TableColumn<event, Void> actionsColumn;
    @FXML private ImageView imageView;
    @FXML private TextArea descriptionTextArea;

    private final ObservableList<event> eventData = FXCollections.observableArrayList();
    private final eventService eventService = new eventService();

    @FXML
    public void initialize() {
        configureColumns();
        setupActionsColumn();
        loadEvents();
        setupSelectionListener();
        setupDoubleClickHandler();
    }

    private void configureColumns() {
        // Configuration standard
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Configuration personnalisée pour l'image
        imageColumn.setCellFactory(column -> new TableCell<event, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(imagePath, true); // True pour le chargement asynchrone
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.err.println("Erreur de chargement de l'image: " + imagePath);
                        setGraphic(null);
                    }
                }
            }
        });
    }


    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsContainer = new HBox(editButton, deleteButton);

            {
                buttonsContainer.setSpacing(10);
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> {
                    event e = getTableView().getItems().get(getIndex());
                    handleUpdateEvent(e);
                });

                deleteButton.setOnAction(event -> {
                    event e = getTableView().getItems().get(getIndex());
                    handleDeleteEvent(e);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsContainer);
            }
        });
    }

    private void loadEvents() {
        try {
            eventData.clear();
            eventData.addAll(eventService.getList());
            eventTable.setItems(eventData);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load events", e.getMessage());
        }
    }
    private void setupDoubleClickHandler() {
        eventTable.setRowFactory(tv -> {
            TableRow<event> row = new TableRow<>();
            row.setOnMouseClicked(aa -> {
                if (aa.getClickCount() == 2 && !row.isEmpty()) {
                    openParticipantsWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void openParticipantsWindow(event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/participant-view.fxml"));
            Parent root = loader.load();

            participantController controller = loader.getController();
            controller.initData(event);

            Stage stage = new Stage();
            stage.setTitle("Participant  - " + event.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir la vue", e.getMessage());
        }
    }
    private void setupSelectionListener() {
        eventTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        updateEventDetails(newVal);
                    }
                }
        );
    }

    private void updateEventDetails(event event) {
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            imageView.setImage(new Image(event.getImage()));
        }
        descriptionTextArea.setText(event.getDescription());
    }

    @FXML
    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/add-event.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un événement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(eventTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            AddEventController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
            loadEvents();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    private void handleDeleteEvent(event event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer " + event.getNom());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet événement?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                eventService.supprimer(event);
                loadEvents();
                showAlert("Succès", "Suppression effectuée", "Événement supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Échec de suppression", e.getMessage());
            }
        }
    }

    private void handleUpdateEvent(event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/update-event.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Événement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(eventTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            UpdateEventController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setEvent(event);

            dialogStage.showAndWait();
            loadEvents();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
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