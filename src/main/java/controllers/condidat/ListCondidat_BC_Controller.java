package controllers.condidat;

import controllers.BaseNavigationController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import models.Condidat;
import models.Offre;
import services.CondidatService;
import services.OffreService;
import services.EmailService;
import utils.Router;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCondidat_BC_Controller extends BaseNavigationController {

    @FXML private TableView<Condidat> condidatTable;
    @FXML private TableColumn<Condidat, String> nomColumn;
    @FXML private TableColumn<Condidat, String> prenomColumn;
    @FXML private TableColumn<Condidat, String> emailColumn;
    @FXML private TableColumn<Condidat, String> telephoneColumn;
    @FXML private TableColumn<Condidat, String> offreColumn;
    @FXML private TableColumn<Condidat, Void> cvColumn;
    @FXML private TableColumn<Condidat, Void> editColumn;
    @FXML private TableColumn<Condidat, Void> acceptColumn;
    @FXML private MenuItem menuListeCondidats;
    @FXML private MenuItem menuAjouterCondidat;
    @FXML private Button addCondidatBtn;
    @FXML private TextField searchField;

    private final CondidatService condidatService = new CondidatService();
    private final OffreService   offreService    = new OffreService();
    private final EmailService   emailService    = new EmailService();

    private Map<Integer, String> offreTitres     = new HashMap<>();
    private ObservableList<Condidat> condidatsList;
    private FilteredList<Condidat>   filteredCondidats;

    @FXML
    public void initialize() {
        super.initialize();
        loadOffreTitres();
        setupTable();
        setupSearch();
        loadCondidats();

        if (addCondidatBtn != null) {
            addCondidatBtn.setOnAction(e ->
                    Router.navigateTo("/condidat/AddCondidat.fxml")
            );
        }
    }

    @Override
    protected void setupNavigation() {
        super.setupNavigation();
        if (menuListeCondidats != null) {
            menuListeCondidats.setOnAction(e ->
                    Router.navigateTo("/condidat/ListCondidat_BC.fxml")
            );
        }
        if (menuAjouterCondidat != null) {
            menuAjouterCondidat.setOnAction(e ->
                    Router.navigateTo("/condidat/AddCondidat.fxml")
            );
        }
    }

    private void loadOffreTitres() {
        List<Offre> offres = offreService.getAllOffres();
        for (Offre offre : offres) {
            offreTitres.put(offre.getId(), offre.getTitreOffre());
        }
    }

    private void setupTable() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        offreColumn.setCellValueFactory(cellData -> {
            Condidat c = cellData.getValue();
            Integer id = c.getOffreId();
            return new SimpleStringProperty(
                    id != null && offreTitres.containsKey(id)
                            ? offreTitres.get(id)
                            : "N/A"
            );
        });

        nomColumn.setPrefWidth(150);
        prenomColumn.setPrefWidth(150);
        emailColumn.setPrefWidth(200);
        telephoneColumn.setPrefWidth(150);
        offreColumn.setPrefWidth(200);
        cvColumn.setPrefWidth(100);
        editColumn.setPrefWidth(200);
        acceptColumn.setPrefWidth(100);

        addActionButtonsToTable();

        condidatTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        condidatTable.setPlaceholder(new Label("Aucun candidat disponible"));
        condidatTable.getSortOrder().add(nomColumn);
        nomColumn.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            filteredCondidats.setPredicate(c -> {
                if (newV == null || newV.isEmpty()) return true;
                String filter = newV.toLowerCase();
                return c.getNom().toLowerCase().contains(filter)
                        || c.getPrenom().toLowerCase().contains(filter)
                        || c.getEmail().toLowerCase().contains(filter)
                        || String.valueOf(c.getTelephone()).contains(filter)
                        || offreTitres.getOrDefault(c.getOffreId(), "").toLowerCase().contains(filter);
            });
        });
    }

    private void loadCondidats() {
        condidatsList = FXCollections.observableArrayList(condidatService.getAllCondidats());
        filteredCondidats = new FilteredList<>(condidatsList, p -> true);
        SortedList<Condidat> sorted = new SortedList<>(filteredCondidats);
        sorted.comparatorProperty().bind(condidatTable.comparatorProperty());
        condidatTable.setItems(sorted);
    }

    private void addActionButtonsToTable() {
        // Voir CV
        cvColumn.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Voir CV");
            {
                btn.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;");
                btn.setOnAction(e -> {
                    Condidat c = getTableView().getItems().get(getIndex());
                    if (c != null && c.getCv() != null) {
                        openCvFile(c.getCv());
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Attention", "Aucun CV disponible.");
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Modifier / Supprimer
        editColumn.setCellFactory(tc -> new TableCell<>() {
            private final Button edit = new Button("Modifier");
            private final Button del  = new Button("Supprimer");
            private final HBox box    = new HBox(5, edit, del);
            {
                edit.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;");
                del.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;");

                edit.setOnAction(e -> {
                    Condidat c = getTableView().getItems().get(getIndex());
                    if (c != null) {
                        try {
                            Router.navigateTo("/condidat/UpdateCondidat.fxml?id=" + c.getId());
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la modification.");
                        }
                    }
                });
                del.setOnAction(e -> {
                    Condidat c = getTableView().getItems().get(getIndex());
                    if (c != null) {
                        try {
                            Router.navigateTo("/condidat/DeleteCondidat.fxml?id=" + c.getId());
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la suppression.");
                        }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Accepter
        acceptColumn.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Accepter");
            {
                btn.setStyle("-fx-background-color:#2ecc71;-fx-text-fill:white;");
                btn.setOnAction(e -> {
                    Condidat c = getTableView().getItems().get(getIndex());
                    if (c != null) handleAcceptance(c);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void handleAcceptance(Condidat condidat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation d'acceptation");
        alert.setHeaderText("Accepter le candidat");
        alert.setContentText("Voulez-vous accepter " + condidat.getPrenom() + " " + condidat.getNom() + " ?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String poste = offreTitres.get(condidat.getOffreId());
                    // <-- appel via l'instance emailService, pas statique
                    emailService.sendAcceptanceEmail(
                            condidat.getEmail(),
                            condidat.getNom(),
                            condidat.getPrenom(),
                            poste
                    );
                    condidatService.updateCondidat(condidat);
                    showAlert(Alert.AlertType.INFORMATION,
                            "Succès",
                            "Le candidat a été accepté et un email de félicitations a été envoyé.");
                    loadCondidats();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR,
                            "Erreur",
                            "Erreur lors de l'acceptation : " + ex.getMessage());
                }
            }
        });
    }

    private void openCvFile(String path) {
        try {
            File f = new File(path);
            if (f.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier CV.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier CV : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
