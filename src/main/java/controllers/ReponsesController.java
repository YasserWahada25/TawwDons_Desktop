package controllers;

import dao.BanDAO;
import dao.QuestionDAO;
import dao.ReponseDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Evaluation;
import models.Question;
import models.Reponse;
import models.ReponseDisplay;
import utils.ProfanityFilter;

import java.util.*;

public class ReponsesController {

    @FXML private Label evalLabel;
    @FXML private TableView<ReponseDisplay> tableView;
    @FXML private TableColumn<ReponseDisplay, String> utilisateurCol, questionCol, reponseCol, bonneCol, statutCol;
    @FXML private VBox statsBox;

    private Evaluation evaluation;
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ReponseDAO reponseDAO = new ReponseDAO();
    private final BanDAO banDAO = BanDAO.getInstance(); // ✅ Singleton DB

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        if (evaluation == null) {
            alert("Erreur", "Aucune évaluation sélectionnée.");
            return;
        }

        evalLabel.setText("Réponses pour : " + evaluation.getNom());
        loadReponses();
    }

    @FXML
    public void initialize() {
        utilisateurCol.setCellValueFactory(data -> data.getValue().utilisateurProperty());
        questionCol.setCellValueFactory(data -> data.getValue().questionProperty());
        reponseCol.setCellValueFactory(data -> data.getValue().reponseProperty());
        bonneCol.setCellValueFactory(data -> data.getValue().bonneProperty());
        statutCol.setCellValueFactory(data -> data.getValue().statutProperty());
    }

    private void loadReponses() {
        tableView.getItems().clear();
        statsBox.getChildren().clear();

        if (evaluation == null) return;

        List<Question> questions = questionDAO.getByEvaluationId(evaluation.getId());
        Map<Integer, Question> questionMap = new HashMap<>();
        for (Question q : questions) {
            questionMap.put(q.getId(), q);
        }

        Map<String, Integer> total = new HashMap<>();
        Map<String, Integer> correct = new HashMap<>();

        // ⚠️ Nouvelle méthode avec base de données
        Set<String> bannis = banDAO.getUtilisateursBannis();
        List<Reponse> reponses = reponseDAO.getAll();

        for (Reponse r : reponses) {
            Question q = questionMap.get(r.getQuestionId());
            if (q == null) continue;

            String attendu = Optional.ofNullable(q.getBonneReponse()).orElse("").trim().toLowerCase();
            String donne = Optional.ofNullable(r.getReponse()).orElse("").trim().toLowerCase();
            boolean estCorrect = attendu.equals(donne);

            // ✅ Profanity filter + ban automatique
            List<String> motsInterdits = ProfanityFilter.extractBadWords(donne);
            if (!motsInterdits.isEmpty()) {
                if (!bannis.contains(r.getUtilisateur())) {
                    for (String mot : motsInterdits) {
                        banDAO.ban(r.getUtilisateur(), mot);
                    }
                    bannis.add(r.getUtilisateur()); // mise à jour locale
                }
            }


            String statut = bannis.contains(r.getUtilisateur())
                    ? "❌ Banni"
                    : estCorrect ? "✅ Correct" : "⛔ Incorrect";

            tableView.getItems().add(new ReponseDisplay(
                    r.getUtilisateur(),
                    q.getContenu(),
                    r.getReponse(),
                    q.getBonneReponse(),
                    statut
            ));

            total.put(r.getUtilisateur(), total.getOrDefault(r.getUtilisateur(), 0) + 1);
            if (estCorrect) {
                correct.put(r.getUtilisateur(), correct.getOrDefault(r.getUtilisateur(), 0) + 1);
            }
        }

        for (String user : total.keySet()) {
            int totalRep = total.get(user);
            int bonnes = correct.getOrDefault(user, 0);
            double percent = totalRep > 0 ? ((double) bonnes / totalRep) * 100 : 0;

            Label stat = new Label(user + " : " + bonnes + "/" + totalRep + " (" + String.format("%.0f", percent) + "%)");
            stat.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 4;");
            statsBox.getChildren().add(stat);
        }
    }

    @FXML
    private void supprimerToutesReponses() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer toutes les réponses ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                reponseDAO.deleteByEvaluationId(evaluation.getId());
                loadReponses();
            }
        });
    }

    @FXML
    private void retourAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Admin.fxml"));
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Admin");
        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur", "Impossible de revenir à l'accueil admin.");
        }
    }

    private void alert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
