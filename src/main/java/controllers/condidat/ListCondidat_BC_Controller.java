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
    private final OffreService offreService = new OffreService();
    private Map<Integer, String> offreTitres = new HashMap<>();
    private ObservableList<Condidat> condidatsList;
    private FilteredList<Condidat> filteredCondidats;

    @FXML
    public void initialize() {
        super.initialize();
        loadOffreTitres();
        setupTable();
        setupSearch();
        loadCondidats();
        
        if (addCondidatBtn != null)
            addCondidatBtn.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
    }

    @Override
    protected void setupNavigation() {
        super.setupNavigation();
        if (menuListeCondidats != null)
            menuListeCondidats.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        if (menuAjouterCondidat != null)
            menuAjouterCondidat.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
    }
    
    private void loadOffreTitres() {
        List<Offre> offres = offreService.getAllOffres();
        for (Offre offre : offres) {
            offreTitres.put(offre.getId(), offre.getTitreOffre());
        }
    }
    
    private void setupTable() {
        // Configuration des colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        
        // Colonne pour afficher le titre de l'offre
        offreColumn.setCellValueFactory(cellData -> {
            Condidat condidat = cellData.getValue();
            Integer offreId = condidat.getOffreId();
            if (offreId != null && offreTitres.containsKey(offreId)) {
                return new SimpleStringProperty(offreTitres.get(offreId));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        
        // Ajuster la taille des colonnes
        nomColumn.setPrefWidth(150);
        prenomColumn.setPrefWidth(150);
        emailColumn.setPrefWidth(200);
        telephoneColumn.setPrefWidth(150);
        offreColumn.setPrefWidth(200);
        cvColumn.setPrefWidth(100);
        editColumn.setPrefWidth(200);
        acceptColumn.setPrefWidth(100);
        
        // Ajouter les boutons d'action
        addActionButtonsToTable();
        
        // Configurer le tableau pour qu'il s'étende
        condidatTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Ajouter un message si le tableau est vide
        condidatTable.setPlaceholder(new Label("Aucun candidat disponible"));

        // Activer le tri sur les colonnes
        condidatTable.getSortOrder().add(nomColumn);
        nomColumn.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCondidats.setPredicate(condidat -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return condidat.getNom().toLowerCase().contains(lowerCaseFilter) ||
                       condidat.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                       condidat.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                       String.valueOf(condidat.getTelephone()).contains(lowerCaseFilter) ||
                       offreTitres.get(condidat.getOffreId()).toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void loadCondidats() {
        condidatsList = FXCollections.observableArrayList(condidatService.getAllCondidats());
        filteredCondidats = new FilteredList<>(condidatsList, p -> true);
        SortedList<Condidat> sortedCondidats = new SortedList<>(filteredCondidats);
        sortedCondidats.comparatorProperty().bind(condidatTable.comparatorProperty());
        condidatTable.setItems(sortedCondidats);
    }

    private void addActionButtonsToTable() {
        // Colonne pour le bouton CV
        cvColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewCvButton = new Button("Voir CV");

            {
                viewCvButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                
                viewCvButton.setOnAction(event -> {
                    Condidat selectedCondidat = getTableView().getItems().get(getIndex());
                    if (selectedCondidat != null && selectedCondidat.getCv() != null) {
                        openCvFile(selectedCondidat.getCv());
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Attention", 
                                "Aucun CV disponible pour ce candidat.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewCvButton);
                }
            }
        });

        // Colonne pour les boutons d'édition et suppression
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                
                editButton.setOnAction(event -> {
                    Condidat selectedCondidat = getTableView().getItems().get(getIndex());
                    if (selectedCondidat != null) {
                        try {
                            Router.navigateTo("/condidat/UpdateCondidat.fxml?id=" + selectedCondidat.getId());
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de modification.");
                        }
                    }
                });

                deleteButton.setOnAction(event -> {
                    Condidat selectedCondidat = getTableView().getItems().get(getIndex());
                    if (selectedCondidat != null) {
                        try {
                            Router.navigateTo("/condidat/DeleteCondidat.fxml?id=" + selectedCondidat.getId());
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de suppression.");
                        }
                    }
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

        // Colonne pour le bouton d'acceptation
        acceptColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accepter");

            {
                acceptButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5 10;");
                
                acceptButton.setOnAction(event -> {
                    Condidat selectedCondidat = getTableView().getItems().get(getIndex());
                    if (selectedCondidat != null) {
                        handleAcceptance(selectedCondidat);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(acceptButton);
                }
            }
        });
    }

    private void handleAcceptance(Condidat condidat) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation d'acceptation");
        confirmAlert.setHeaderText("Accepter le candidat");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir accepter " + condidat.getPrenom() + " " + condidat.getNom() + " ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Envoyer l'email de félicitations
                    String poste = offreTitres.get(condidat.getOffreId());
                    EmailService.sendAcceptanceEmail(condidat.getEmail(), condidat.getNom(), condidat.getPrenom(), poste);
                    
                    // Mettre à jour le statut du candidat dans la base de données
                    //condidat.setStatut("Accepté");
                    condidatService.updateCondidat(condidat);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                            "Le candidat a été accepté et un email de félicitations a été envoyé.");
                    
                    // Recharger la liste des candidats
                    loadCondidats();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Erreur lors de l'acceptation du candidat : " + e.getMessage());
                }
            }
        });
    }

    private void openCvFile(String cvPath) {
        try {
            File cvFile = new File(cvPath);
            if (cvFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(cvFile);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Impossible d'ouvrir le fichier PDF. Votre système ne supporte pas cette fonctionnalité.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Le fichier CV n'existe pas à l'emplacement spécifié.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible d'ouvrir le fichier CV : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 