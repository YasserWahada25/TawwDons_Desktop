package controllers.offre;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Offre;
import services.OffreService;
import utils.Router;

public class ListOffre_BC_Controller {

    @FXML private TableView<Offre> offreTable;
    @FXML private TableColumn<Offre, String> titreColumn;
    @FXML private TableColumn<Offre, String> descriptionColumn;
    @FXML private TableColumn<Offre, Void> editColumn;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;
    @FXML private Button addOffreBtn;
    @FXML private Button offreBtn;

    private final OffreService offreService = new OffreService();

    @FXML
    public void initialize() {
        // Configuration de la navigation
        setupNavigation();
        
        // Configuration du tableau
        setupTable();
        
        // Charger les offres
        loadOffres();
    }

    private void setupNavigation() {
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
        addOffreBtn.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
    }
    
    @FXML
    private void handleOffreButtonClick() {
        System.out.println("Bouton Offre cliqué - Méthode handleOffreButtonClick");
        // Recharger les données
        loadOffres();
        // Afficher un message de confirmation
        showAlert(Alert.AlertType.INFORMATION, "Information", "Liste des offres actualisée.");
    }
    
    private void setupTable() {
        // Configuration des colonnes
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titreOffre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descriptionOffre"));
        
        // Ajuster la taille des colonnes
        titreColumn.setPrefWidth(200);
        descriptionColumn.setPrefWidth(600);
        editColumn.setPrefWidth(200);
        
        // Permettre à la colonne description de s'étendre
        descriptionColumn.setMinWidth(300);
        
        // Ajouter les boutons d'action
        addActionButtonsToTable();
        
        // Configurer le tableau pour qu'il s'étende
        offreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Ajouter un message si le tableau est vide
        offreTable.setPlaceholder(new Label("Aucune offre disponible"));
    }

    private void loadOffres() {
        ObservableList<Offre> offres = FXCollections.observableArrayList(offreService.getAllOffres());
        offreTable.setItems(offres);
    }

    private void addActionButtonsToTable() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton); // 5 est l'espacement entre les boutons

            {
                // Style du bouton Modifier
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                
                // Style du bouton Supprimer
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                
                // Action du bouton Modifier
                editButton.setOnAction(event -> {
                    Offre selectedOffre = getTableView().getItems().get(getIndex());
                    if (selectedOffre != null) {
                        try {
                            Router.navigateTo("/offre/UpdateOffre.fxml?id=" + selectedOffre.getId());
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de modification.");
                        }
                    }
                });

                // Action du bouton Supprimer
                deleteButton.setOnAction(event -> {
                    Offre selectedOffre = getTableView().getItems().get(getIndex());
                    if (selectedOffre != null) {
                        try {
                            Router.navigateTo("/offre/DeleteOffre.fxml?id=" + selectedOffre.getId());
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
