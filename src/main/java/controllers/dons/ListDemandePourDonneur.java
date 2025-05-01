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
import utils.RecuPDFGenerator;
import utils.Router;

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
    @FXML private TableColumn<DemandeDons, Void> colAction;
    @FXML private Button btnRetour;

    private final DonsService donsService = new DonsService();

    @FXML
    public void initialize() {
        refreshTable();
        btnRetour.setOnAction(event -> Router.navigateTo("/Dons/ListDons.fxml"));

        colDon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDonTitre()));
        colBeneficiaire.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBeneficiaireNom()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDemande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));

        colAction.setCellFactory(getActionCellFactory());
    }

    private Callback<TableColumn<DemandeDons, Void>, TableCell<DemandeDons, Void>> getActionCellFactory() {
        return column -> new TableCell<>() {
            private final Button btnAccepter = new Button("✅ Accepter");
            private final Button btnRefuser = new Button("❌ Refuser");
            private final Button btnChat = new Button("\uD83D\uDCAC Chat");
            private final Button btnValider = new Button("✔️ Valider");
            private final Button btnTelecharger = new Button("📄 Télécharger Reçu");
            private final HBox box = new HBox(8);

            {
                box.setStyle("-fx-alignment: center;");

                btnAccepter.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 8;");
                btnRefuser.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 8;");
                btnChat.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 8;");
                btnValider.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-background-radius: 8;");
                btnTelecharger.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 8;");

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
                    openChatWindow(demande.getId(), 1, "donneur", demande.getBeneficiaireId());
                });

                btnValider.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dons/SignaturePopup.fxml"));
                        Parent root = loader.load();
                        controllers.dons.SignaturePopupController controller = loader.getController();

                        Stage popup = new Stage();
                        popup.setScene(new Scene(root));
                        popup.setTitle("Signature du Donneur");
                        popup.showAndWait();

                        File signatureFile = controller.getSignatureFile();
                        if (signatureFile != null && signatureFile.exists()) {
                            boolean validated = donsService.validerDemande(demande.getId());
                            if (validated) {
                                boolean success = RecuPDFGenerator.generateRecu(demande, signatureFile.getAbsolutePath());
                                showAlert(success, "✅ Demande validée et reçu généré !", "❌ PDF non généré !");
                                refreshTable();
                            } else {
                                showAlert(false, "", "❌ Échec validation de la demande !");
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        showAlert(false, "", "❌ Erreur lors de l'ouverture de la signature !");
                    }
                });

                btnTelecharger.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    boolean success = RecuPDFGenerator.generateRecu(demande, null);
                    showAlert(success, "✅ Reçu téléchargé avec succès !", "❌ Échec du téléchargement !");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                box.getChildren().clear();

                if (!empty && getTableView().getItems().get(getIndex()) != null) {
                    DemandeDons demande = getTableView().getItems().get(getIndex());

                    switch (demande.getStatut()) {
                        case "En attente" -> box.getChildren().addAll(btnAccepter, btnRefuser);
                        case "Acceptée" -> box.getChildren().addAll(btnChat, btnValider);
                        case "Validée" -> box.getChildren().add(btnTelecharger);
                        default -> {}
                    }
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        };
    }

    private void refreshTable() {
        tableDemandes.getItems().clear();
        List<DemandeDons> toutesDemandes = donsService.getDemandesReçuesParDonneur(1);
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
