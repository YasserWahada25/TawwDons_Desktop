package controllers.offre;

import controllers.BaseNavigationController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Offre;
import services.OffreService;
import utils.Router;

public class ListOffre_BC_Controller extends BaseNavigationController {

    @FXML private TableView<Offre> offreTable;
    @FXML private TableColumn<Offre, String> titreColumn;
    @FXML private TableColumn<Offre, String> descriptionColumn;
    @FXML private TableColumn<Offre, String> dateColumn;
    @FXML private TableColumn<Offre, Void> actionsColumn;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;
    @FXML private Button addOffreBtn;
    @FXML private TextField searchField;

    private final OffreService offreService = new OffreService();
    private ObservableList<Offre> offresList;
    private FilteredList<Offre> filteredOffres;

    @FXML
    public void initialize() {
        super.initialize();
        setupTable();
        setupSearch();
        loadOffres();
        
        if (addOffreBtn != null)
            addOffreBtn.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
    }

    @Override
    protected void setupNavigation() {
        super.setupNavigation();
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filteredOffres.setPredicate(offre -> {
                if (newText == null || newText.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newText.toLowerCase();
                return offre.getTitreOffre().toLowerCase().contains(lowerCaseFilter) ||
                       offre.getDescriptionOffre().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }
    
    @FXML
    private void handleOffreButtonClick() {
        System.out.println("Bouton Offre cliqué - Méthode handleOffreButtonClick");
        loadOffres();
        showAlert(Alert.AlertType.INFORMATION, "Information", "Liste des offres actualisée.");
    }
    
    private void setupTable() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titreOffre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descriptionOffre"));

        
        titreColumn.setPrefWidth(200);
        descriptionColumn.setPrefWidth(600);

        actionsColumn.setPrefWidth(200);
        
        descriptionColumn.setMinWidth(300);
        
        addActionButtonsToTable();
        
        offreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        offreTable.setPlaceholder(new Label("Aucune offre disponible"));

        offreTable.getSortOrder().add(titreColumn);
        titreColumn.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void loadOffres() {
        offresList = FXCollections.observableArrayList(offreService.getAllOffres());
        filteredOffres = new FilteredList<>(offresList, p -> true);
        SortedList<Offre> sortedOffres = new SortedList<>(filteredOffres);
        sortedOffres.comparatorProperty().bind(offreTable.comparatorProperty());
        offreTable.setItems(sortedOffres);
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(10, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                
                editButton.setOnAction(event -> {
                    Offre offre = getTableView().getItems().get(getIndex());
                    Router.navigateTo("/offre/UpdateOffre.fxml?id=" + offre.getId());
                });

                deleteButton.setOnAction(event -> {
                    Offre offre = getTableView().getItems().get(getIndex());
                    Router.navigateTo("/offre/DeleteOffre.fxml?id=" + offre.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
