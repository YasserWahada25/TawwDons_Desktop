package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.Dons;
import services.DonsService;
import utils.Router;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RequestDonsController {

    @FXML private TableView<Dons> donsTable;
    @FXML private TableColumn<Dons, Integer> colId;
    @FXML private TableColumn<Dons, String> colTitre;
    @FXML private TableColumn<Dons, String> colDescription;
    @FXML private TableColumn<Dons, String> colDate;
    @FXML private TableColumn<Dons, Void> colActions;

    // Routage via sidebar
    @FXML private MenuItem menuArticleList;
    @FXML private MenuItem menuArticleForm;
    @FXML private Button btnDonsRequests;
    @FXML private Button btnDashboard;

    private final DonsService donsService = new DonsService();

    @FXML
    public void initialize() {
        // Charger la table des dons
        loadDons();

        // Routage sidebar
        if (menuArticleList != null)
            menuArticleList.setOnAction(e -> Router.navigateTo("/Admin/adminArticleList.fxml"));

        if (menuArticleForm != null)
            menuArticleForm.setOnAction(e -> Router.navigateTo("/Admin/ajouterArticle.fxml"));

        if (btnDonsRequests != null)
            btnDonsRequests.setOnAction(e -> Router.navigateTo("/Admin/RequestAddDons.fxml"));

        if (btnDashboard != null)
            btnDashboard.setOnAction(e -> Router.navigateTo("/views/Dashboard.fxml"));
    }

    private void loadDons() {
        List<Dons> donsList = donsService.getDonsNonValidés();
        ObservableList<Dons> observableList = FXCollections.observableArrayList(donsList);

        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colTitre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre()));

        colDescription.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
        );

        colActions.setCellFactory(getActionsCellFactory());

        donsTable.setItems(observableList);
    }

    private Callback<TableColumn<Dons, Void>, TableCell<Dons, Void>> getActionsCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Dons, Void> call(final TableColumn<Dons, Void> param) {
                return new TableCell<>() {
                    private final Button btnAccept = new Button("Accepter");
                    private final Button btnRefuse = new Button("Refuser");
                    private final HBox pane = new HBox(10, btnAccept, btnRefuse);

                    {
                        btnAccept.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                        btnRefuse.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                        btnAccept.setOnAction(event -> {
                            Dons don = getTableView().getItems().get(getIndex());
                            donsService.accepterDon(don.getId());
                            loadDons();
                        });

                        btnRefuse.setOnAction(event -> {
                            Dons don = getTableView().getItems().get(getIndex());
                            donsService.refuserDon(don.getId());
                            loadDons();
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
    }
}
