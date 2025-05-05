package controllers.dons;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.DemandeDons;
import services.DonsService;
import services.UserService;                    // ← ajout
import utils.RecuPDFGenerator;
import utils.Router;
import utils.SessionManager;
import models.User;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListDemandePourDonneur {

    @FXML private TableView<DemandeDons> tableDemandes;
    @FXML private TableColumn<DemandeDons, String> colDon;
    @FXML private TableColumn<DemandeDons, String> colBeneficiaire;
    @FXML private TableColumn<DemandeDons, String> colDate;
    @FXML private TableColumn<DemandeDons, String> colStatut;
    @FXML private TableColumn<DemandeDons, DemandeDons> colAction;
    @FXML private Button btnRetour;

    private final DonsService donsService = new DonsService();
    private final UserService userService = new UserService();  // ← ajout
    private int currentUserId;
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
        } else {
            currentUserId = 0;
        }

        refreshTable();

        btnRetour.setOnAction(event -> Router.navigateTo("/Dons/ListDons.fxml"));
        colDon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDonTitre()));
        colBeneficiaire.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBeneficiaireNom()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDemande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));

        colAction.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        colAction.setCellFactory(getActionCellFactory());
    }

    private Callback<TableColumn<DemandeDons, DemandeDons>, TableCell<DemandeDons, DemandeDons>> getActionCellFactory() {
        return column -> new TableCell<>() {
            private final Button btnAccepter = new Button("✅ Accepter");
            private final Button btnRefuser  = new Button("❌ Refuser");
            private final Button btnChat     = new Button("💬 Chat");
            private final Button btnValider  = new Button("✔️ Valider");
            private final Button btnTelecharger = new Button("📄 Télécharger Reçu");
            private final HBox box = new HBox(8);

            {
                box.setStyle("-fx-alignment: center;");
                btnAccepter.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnRefuser .setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                btnChat    .setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
                btnValider .setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white;");
                btnTelecharger.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

                btnAccepter.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    boolean success = donsService.modifierStatutDemande(demande.getId(), "Acceptée", true);
                    showAlert(success, "✅ Demande acceptée !", "❌ Erreur d'acceptation !");
                    refreshTable();
                });

                btnRefuser.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    boolean success = donsService.modifierStatutDemande(demande.getId(), "Refusée", false);
                    showAlert(success, "✅ Demande refusée !", "❌ Erreur de refus !");
                    refreshTable();
                });

                btnChat.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    openChatWindow(
                            demande.getId(),
                            currentUserId,
                            "donneur",
                            demande.getBeneficiaireId()
                    );
                });

                btnValider.setOnAction(e -> {
                    // ... inchangé ...
                });

                btnTelecharger.setOnAction(e -> {
                    // ... inchangé ...
                });
            }

            @Override
            protected void updateItem(DemandeDons demande, boolean empty) {
                super.updateItem(demande, empty);
                box.getChildren().clear();
                if (empty || demande == null) {
                    setGraphic(null);
                } else {
                    switch (demande.getStatut()) {
                        case "En Attente" -> box.getChildren().addAll(btnAccepter, btnRefuser);
                        case "Acceptée"   -> box.getChildren().addAll(btnChat, btnValider);
                        case "Validée"    -> box.getChildren().add(btnTelecharger);
                    }
                    setGraphic(box);
                }
            }
        };
    }

    private void refreshTable() {
        tableDemandes.getItems().clear();
        List<DemandeDons> toutesDemandes = donsService.getDemandesReçuesParDonneur(currentUserId);
        List<DemandeDons> demandesFiltrées = toutesDemandes.stream()
                .filter(d -> !"Refusée".equals(d.getStatut()))
                .toList();
        tableDemandes.setItems(FXCollections.observableArrayList(demandesFiltrées));
    }

    private void showAlert(boolean success, String successMessage, String errorMessage) {
        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(success ? "Succès" : "Erreur");
        alert.setHeaderText(null);
        alert.setContentText(success ? successMessage : errorMessage);
        alert.showAndWait();
    }

    private void openChatWindow(int demandeId,
                                int currentUserId,
                                String role,
                                int autreUserId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dons/chat.fxml"));
            Parent root = loader.load();
            ChatController chatController = loader.getController();

            // ← récupération du nom de l’interlocuteur
            User interlocuteur = userService.findById(autreUserId);
            String nomInterlocuteur = interlocuteur != null ? interlocuteur.getNom() : "Inconnu";

            chatController.setParticipants(
                    demandeId,
                    currentUserId,
                    role,
                    autreUserId,
                    nomInterlocuteur      // ← argument supplémentaire
            );

            Stage chatStage = new Stage();
            chatStage.setTitle(role.equals("donneur") ? "💬 Donneur" : "💬 Bénéficiaire");
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
