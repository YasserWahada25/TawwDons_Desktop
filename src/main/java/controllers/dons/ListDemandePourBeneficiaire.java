package controllers.dons;

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
import utils.RecuPDFGenerator; // ✅ pour générer le PDF
import utils.Router;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListDemandePourBeneficiaire {






    @FXML private TableView<DemandeDons> tableDemandes;
    @FXML private TableColumn<DemandeDons, String> colDon;
    @FXML private TableColumn<DemandeDons, String> colDate;
    @FXML private TableColumn<DemandeDons, String> colStatut;
    @FXML private TableColumn<DemandeDons, Void> colAction;
    @FXML private Button btnRetour;


    private final DonsService donsService = new DonsService();

    @FXML
    public void initialize() {
        List<DemandeDons> demandes = donsService.getDemandesByBeneficiaire(2);
        btnRetour.setOnAction(event -> Router.navigateTo("/Dons/ListDons.fxml"));

        tableDemandes.setItems(FXCollections.observableArrayList(demandes));

        colDon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDonTitre()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDemande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));

        colAction.setCellFactory(getActionCellFactory());
    }

    private Callback<TableColumn<DemandeDons, Void>, TableCell<DemandeDons, Void>> getActionCellFactory() {
        return column -> new TableCell<>() {
            private final Button btnChat = new Button("\uD83D\uDCAC Chat");
            private final Button btnRecu = new Button("\uD83D\uDDB6 Télécharger Reçu");
            private final HBox box = new HBox(8);

            {
                box.setStyle("-fx-alignment: center;");

                // Bouton Chat
                btnChat.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 20;");
                btnChat.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    openChatWindow(demande.getId(), 2, "beneficiaire", donsService.getDonneurIdByDonId(demande.getDonsId()));
                });

                // Bouton Télécharger Reçu
                btnRecu.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 20;");
                btnRecu.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    boolean success = RecuPDFGenerator.generateRecu(demande,null);

                    Alert alert;
                    if (success) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Téléchargement réussi");
                        alert.setContentText("✅ Votre reçu a été généré avec succès !");
                    } else {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setContentText("❌ Échec du téléchargement du reçu !");
                    }
                    alert.setHeaderText(null);
                    alert.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                box.getChildren().clear();
                if (!empty && getTableView().getItems().get(getIndex()) != null) {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    if ("Acceptée".equals(demande.getStatut())) {
                        box.getChildren().add(btnChat);
                    } else if ("Validée".equals(demande.getStatut())) {
                        box.getChildren().add(btnRecu);
                    }
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        };
    }

    private void openChatWindow(int demandeId, int currentUserId, String role, int autreUserId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dons/chat.fxml"));
            Parent root = loader.load();
            ChatController chatController = loader.getController();
            chatController.setParticipants(demandeId, currentUserId, role, autreUserId);

            Stage chatStage = new Stage();
            chatStage.setTitle(role.equals("donneur") ? "\uD83D\uDCAC Donneur" : "\uD83D\uDCAC Bénéficiaire");
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


