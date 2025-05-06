package org.example.event.controller;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import org.example.event.service.participantService;
import org.example.event.service.WeatherService;
import org.example.event.service.WeatherService.WeatherData;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class eventController {

    @FXML private TableView<event> eventTable;
    @FXML private TableColumn<event, Integer> idColumn;
    @FXML private TableColumn<event, String> nomColumn;
    @FXML private TableColumn<event, String> imageColumn;
    @FXML private TableColumn<event, String> descriptionColumn;
    @FXML private TableColumn<event, Integer> categorieColumn;
    @FXML private TableColumn<event, String> dateColumn;
    @FXML private TableColumn<event, String> locationColumn;

    @FXML private TableColumn<event, Void> actionsColumn;
    @FXML private ImageView imageView;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField searchField;
    @FXML private TableColumn<event, String> participantsCountColumn;
    @FXML private Label temperatureLabel;

    private final ObservableList<event> eventData = FXCollections.observableArrayList();
    private final eventService eventService = new eventService();
    private final WeatherService weatherService = new WeatherService();
    private final participantService participantService = new participantService();

    @FXML
    public void initialize() {
        configureColumns();
        setupActionsColumn();
        loadEvents();
        setupSelectionListener();
        setupDoubleClickHandler();
        setupSearchListener(); // Configuration de l'écouteur de recherche
        setupStatisticsColumn(); // Nouvelle méthode pour configurer la colonne stats


    }

    private void configureColumns() {
        // Configuration standard
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie_id"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = cellData.getValue().getDate();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "Non définie");
        });
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

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
    @FXML
    private void checkWeatherForAllEvents(ActionEvent event) {
        event selectedEvent = eventTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un événement dans le tableau.");
            return;
        }

        LocalDate eventDate = selectedEvent.getDate();
        String location = selectedEvent.getLocation();

        // Validate required fields
        if (eventDate == null) {
            showErrorAlert("Erreur de date", "L'événement sélectionné n'a pas de date définie.");
            return;
        }

        if (location == null || location.trim().isEmpty()) {
            showErrorAlert("Erreur de lieu", "L'événement sélectionné n'a pas de lieu défini.");
            return;
        }

        try {
            String[] coords = selectedEvent.getLocation().split(",");
            double lat = Double.parseDouble(coords[0].trim());
            double lng = Double.parseDouble(coords[1].trim());

            WeatherService.WeatherData weatherData = weatherService.getWeatherData(lat, lng, eventDate);
            String formattedDate = eventDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String weatherInfo = String.format(
                    "Événement: %s\nDate: %s\nLieu: %s\nTempérature: %.1f°C\nConditions: %s",
                    selectedEvent.getNom(),
                    formattedDate,
                    location,
                    weatherData.temperature,
                    weatherData.condition
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Météo de l'événement");
            alert.setHeaderText("Conditions actuelles");
            alert.setContentText(weatherInfo);
            alert.getDialogPane().setPrefSize(350, 200);
            alert.showAndWait();

        } catch (Exception e) {
            showErrorAlert("Erreur technique", "Impossible de récupérer les données météo", e.getMessage());
        }

    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupStatisticsColumn() {
        participantsCountColumn.setCellValueFactory(cellData -> {
            try {
                int count = new participantService().countParticipantsByEvent(cellData.getValue().getId());
                return new SimpleStringProperty(String.valueOf(count));
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        participantsCountColumn.setCellFactory(column -> new TableCell<event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

                    // Optionnel: coloration en fonction du nombre
                    try {
                        int count = Integer.parseInt(item);
                        if (count > 50) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        } else if (count > 20) {
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        }
                    } catch (NumberFormatException e) {
                        // Ne rien faire si ce n'est pas un nombre
                    }
                }
            }
        });
    }

    @FXML
    private void handleShowStatistics() {
        try {
            // Créer une nouvelle fenêtre
            Stage statsStage = new Stage();
            statsStage.setTitle("Statistiques des Participants");

            // Créer un BarChart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Participants par Événement");
            xAxis.setLabel("Événements");
            yAxis.setLabel("Nombre de Participants");

            // Préparer les données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Participants");

            participantService pService = new participantService();
            for (event e : eventData) {
                try {
                    int count = pService.countParticipantsByEvent(e.getId());
                    series.getData().add(new XYChart.Data<>(e.getNom(), count));
                } catch (SQLException ex) {
                    series.getData().add(new XYChart.Data<>(e.getNom(), 0));
                }
            }

            barChart.getData().add(series);

            // Configurer la scène
            Scene scene = new Scene(barChart, 800, 600);
            statsStage.setScene(scene);
            statsStage.show();

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible d'afficher les statistiques", e.getMessage());
        }
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
    // Ajout des méthodes pour la recherche
    private void setupSearchListener() {
        // Recherche en temps réel lors de la saisie
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEventsByName(newValue);
        });
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
    @FXML
    private void handleSearch() {
        filterEventsByName(searchField.getText());
    }
    @FXML
    private void handleOpenMap(ActionEvent event) {
        event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            openMap(selectedEvent);
        } else {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un événement.");
        }
    }

    private void openMap(event event) {
        try {
            String encodedAddress = URLEncoder.encode(event.getLocation(), StandardCharsets.UTF_8.toString());
            String url = "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;

            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();
            engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

            Stage mapStage = new Stage();
            mapStage.setTitle("Localisation: " + event.getNom());
            webView.setPrefSize(800, 600);
            mapStage.setScene(new Scene(webView));
            engine.load(url);
            mapStage.show();

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir la carte", e.getMessage());
        }
    }
    private void filterEventsByName(String name) {
        if (name == null || name.isEmpty()) {
            eventTable.setItems(eventData);
            return;
        }

        ObservableList<event> filteredList = FXCollections.observableArrayList();
        String lowerCaseFilter = name.toLowerCase();

        for (event e : eventData) {
            if (e.getNom().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(e);
            }
        }

        eventTable.setItems(filteredList);
    }


    private void openParticipantsWindow(event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/event/view-participant.fxml"));
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
        try {
            WeatherService.WeatherData weather = weatherService.getWeatherData(
                    event.getLocation(),
                    event.getDate()
            );

            String tempText = (weather.temperature != -1)
                    ? String.format("🌡 %.1f°C | ☁ %s", weather.temperature, weather.condition)
                    : "Météo non disponible";

            temperatureLabel.setText(tempText);

        } catch (Exception e) {
            temperatureLabel.setText("Erreur de chargement météo");
        }

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
    @FXML
    private void handleSortByParticipants() {
        try {
            // Créer une liste observable triée
            ObservableList<event> sortedList = FXCollections.observableArrayList();
            participantService pService = new participantService();

            // Créer une liste avec les événements et leur nombre de participants
            List<EventWithCount> eventsWithCount = new ArrayList<>();
            for (event e : eventData) {
                int count = pService.countParticipantsByEvent(e.getId());
                eventsWithCount.add(new EventWithCount(e, count));
            }

            // Trier la liste par nombre de participants (décroissant)
            eventsWithCount.sort((e1, e2) -> Integer.compare(e2.count, e1.count));

            // Récupérer les événements triés
            for (EventWithCount ew : eventsWithCount) {
                sortedList.add(ew.event);
            }

            // Mettre à jour la TableView
            eventTable.setItems(sortedList);

        } catch (SQLException e) {
            showErrorAlert("Erreur", "Échec du tri", e.getMessage());
        }
    }

    // Classe helper pour stocker temporairement les événements avec leur count
    private static class EventWithCount {
        event event;
        int count;

        public EventWithCount(event event, int count) {
            this.event = event;
            this.count = count;
        }
    }
    @FXML
    private void handleResetSort() {
        eventTable.setItems(eventData); // Réinitialise à la liste originale
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
