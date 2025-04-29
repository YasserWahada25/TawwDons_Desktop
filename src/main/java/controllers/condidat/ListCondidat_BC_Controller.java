package controllers.condidat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import models.Condidat;
import models.Offre;
import services.CondidatService;
import services.OffreService;
import utils.Router;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCondidat_BC_Controller {

    @FXML private TableView<Condidat> condidatTable;
    @FXML private TableColumn<Condidat, String> nomColumn;
    @FXML private TableColumn<Condidat, String> prenomColumn;
    @FXML private TableColumn<Condidat, String> emailColumn;
    @FXML private TableColumn<Condidat, String> telephoneColumn;
    @FXML private TableColumn<Condidat, String> offreColumn;
    @FXML private TableColumn<Condidat, Void> cvColumn;
    @FXML private TableColumn<Condidat, Void> editColumn;
    @FXML private MenuItem menuListeCondidats;
    @FXML private MenuItem menuAjouterCondidat;
    @FXML private Button addCondidatBtn;
    @FXML private Button offreBtn;
    @FXML private Button condidatBtn;

    private final CondidatService condidatService = new CondidatService();
    private final OffreService offreService = new OffreService();
    private Map<Integer, String> offreTitres = new HashMap<>();

    @FXML
    public void initialize() {
        // Configuration de la navigation
        setupNavigation();
        
        // Charger les titres des offres
        loadOffreTitres();
        
        // Configuration du tableau
        setupTable();
        
        // Charger les candidats
        loadCondidats();
    }

    private void setupNavigation() {
        menuListeCondidats.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        menuAjouterCondidat.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
        addCondidatBtn.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
        offreBtn.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        condidatBtn.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
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
        
        // Ajouter les boutons d'action
        addActionButtonsToTable();
        
        // Configurer le tableau pour qu'il s'étende
        condidatTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Ajouter un message si le tableau est vide
        condidatTable.setPlaceholder(new Label("Aucun candidat disponible"));
    }

    private void loadCondidats() {
        ObservableList<Condidat> condidats = FXCollections.observableArrayList(condidatService.getAllCondidats());
        condidatTable.setItems(condidats);
    }

    private void addActionButtonsToTable() {
        // Colonne pour le bouton CV
        cvColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewCvButton = new Button("Voir CV");

            {
                // Style du bouton Voir CV
                viewCvButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                
                // Action du bouton Voir CV
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
                // Style du bouton Modifier
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                
                // Style du bouton Supprimer
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                
                // Action du bouton Modifier
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

                // Action du bouton Supprimer
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

    @FXML
    private void handleOffreButtonClick() {
        Router.navigateTo("/offre/ListOffre_BC.fxml");
    }
    
    @FXML
    private void handleCondidatButtonClick() {
        // Recharger les données au lieu de naviguer vers la même page
        loadOffreTitres();
        loadCondidats();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 