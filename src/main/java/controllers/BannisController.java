package controllers;

import dao.BanDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class BannisController {

    @FXML private TableView<BanDAO.BanInfo> banTable;
    @FXML private TableColumn<BanDAO.BanInfo, String> utilisateurCol, motCol;
    @FXML private TableColumn<BanDAO.BanInfo, LocalDateTime> dateCol;

    private final BanDAO banDAO = BanDAO.getInstance();

    @FXML
    public void initialize() {
        setupColumns();
        loadBannis();
    }

    private void setupColumns() {
        // Utiliser les getters !
        utilisateurCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUtilisateur()));
        motCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMot()));
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate()));
    }

    private void loadBannis() {
        try {
            List<BanDAO.BanInfo> list = banDAO.getAllBannis();
            banTable.getItems().setAll(list);
            System.out.println("🔍 Nombre de bannis récupérés : " + list.size());
        } catch (Exception e) {
            showAlert("Erreur de chargement", "Impossible de récupérer la liste des utilisateurs bannis.");
            e.printStackTrace();
        }
    }

    @FXML
    private void debannirSelection() {
        BanDAO.BanInfo selected = banTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un utilisateur à débannir.");
            return;
        }

        try {
            // Utiliser getUtilisateur()
            banDAO.debannir(selected.getUtilisateur());
            loadBannis();
            showAlert("Succès", "Utilisateur débanni avec succès !");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de débannir cet utilisateur.");
            e.printStackTrace();
        }
    }

    @FXML
    private void retour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Admin.fxml"));
            Stage stage = (Stage) banTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Admin");
        } catch (Exception e) {
            showAlert("Erreur de navigation", "Impossible de retourner à l'accueil admin.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
