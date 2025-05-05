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
import services.UserService;                 // ← import ajouté
import utils.RecuPDFGenerator;
import utils.Router;
import utils.SessionManager;
import models.User;

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
    private final UserService userService = new UserService();   // ← service pour récupérer un User
    private int currentUserId;

    @FXML
    public void initialize() {
        var user = SessionManager.getCurrentUser();
        if (user != null) {
            currentUserId = user.getId();
            System.out.println("✅ Bénéficiaire connecté : " + user.getNom() + " | ID = " + currentUserId);
        } else {
            currentUserId = -1;
            System.out.println("❌ Aucun utilisateur connecté !");
            return;
        }

        btnRetour.setOnAction(event -> Router.navigateTo("/Dons/ListDons.fxml"));

        List<DemandeDons> demandes = donsService.getDemandesByBeneficiaire(currentUserId);
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
            private final Button btnChat = new Button("💬 Chat");
            private final Button btnRecu = new Button("📥 Reçu");
            private final HBox box = new HBox(8);

            {
                box.setStyle("-fx-alignment: center;");

                btnChat.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 20;");
                btnChat.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    openChatWindow(
                            demande.getId(),
                            currentUserId,
                            "beneficiaire",
                            donsService.getDonneurIdByDonId(demande.getDonsId())
                    );
                });

                btnRecu.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 20;");
                btnRecu.setOnAction(e -> {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    boolean success = RecuPDFGenerator.generateRecu(demande, null);
                    Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    alert.setTitle(success ? "Téléchargement réussi" : "Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText(success
                            ? "✅ Votre reçu a été généré avec succès !"
                            : "❌ Échec du téléchargement du reçu !");
                    alert.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                box.getChildren().clear();

                if (!empty && getTableView().getItems().get(getIndex()) != null) {
                    DemandeDons demande = getTableView().getItems().get(getIndex());
                    switch (demande.getStatut()) {
                        case "Acceptée" -> box.getChildren().add(btnChat);
                        case "Validée"  -> box.getChildren().add(btnRecu);
                    }
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        };
    }

    private void openChatWindow(int demandeId,
                                int currentUserId,
                                String role,
                                int autreUserId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dons/chat.fxml"));
            Parent root = loader.load();
            ChatController chatController = loader.getController();

            // On récupère le nom de l’interlocuteur avant d’appeler setParticipants
            User interlocuteur = userService.findById(autreUserId);
            String nomInterlocuteur = interlocuteur != null ? interlocuteur.getNom() : "Inconnu";

            // Appel avec 5 arguments
            chatController.setParticipants(
                    demandeId,
                    currentUserId,
                    role,
                    autreUserId,
                    nomInterlocuteur
            );

            Stage chatStage = new Stage();
            chatStage.setTitle("💬 " + (role.equals("donneur") ? "Donneur" : "Bénéficiaire"));
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
