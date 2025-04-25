package controllers;

import dao.BanDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        utilisateurCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().utilisateur));
        motCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().mot));
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().date));

        loadBannis();
    }

    private void loadBannis() {
        List<BanDAO.BanInfo> list = banDAO.getAllBannis();
        System.out.println("🔍 Nombre de bannis récupérés : " + list.size());
        banTable.getItems().setAll(list);
    }

    @FXML
    private void debannirSelection() {
        BanDAO.BanInfo selected = banTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            banDAO.debannir(selected.utilisateur);
            loadBannis();
        } else {
            System.out.println("❗ Aucun utilisateur sélectionné.");
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
            e.printStackTrace();
        }
    }
}
