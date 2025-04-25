package views;

import dao.QuestionDAO;
import dao.ReponseDAO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Evaluation;
import models.Question;
import models.Reponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReponsesView {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ReponseDAO reponseDAO = new ReponseDAO();

    public void show(Evaluation evaluation) {
        Stage stage = new Stage();
        stage.setTitle("Réponses - " + evaluation.getNom());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label titre = new Label("Réponses des utilisateurs - Évaluation : " + evaluation.getNom());

        TableView<ReponseDisplay> table = new TableView<>();
        TableColumn<ReponseDisplay, String> utilisateurCol = new TableColumn<>("Utilisateur");
        utilisateurCol.setCellValueFactory(new PropertyValueFactory<>("utilisateur"));

        TableColumn<ReponseDisplay, String> questionCol = new TableColumn<>("Question");
        questionCol.setCellValueFactory(new PropertyValueFactory<>("question"));

        TableColumn<ReponseDisplay, String> reponseCol = new TableColumn<>("Réponse");
        reponseCol.setCellValueFactory(new PropertyValueFactory<>("reponse"));

        TableColumn<ReponseDisplay, String> bonneCol = new TableColumn<>("Bonne Réponse");
        bonneCol.setCellValueFactory(new PropertyValueFactory<>("bonneReponse"));

        TableColumn<ReponseDisplay, String> etatCol = new TableColumn<>("Statut");
        etatCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        table.getColumns().addAll(utilisateurCol, questionCol, reponseCol, bonneCol, etatCol);
        table.setPrefHeight(400);

        // Statistiques
        Map<String, Integer> total = new HashMap<>();
        Map<String, Integer> correct = new HashMap<>();

        List<Question> questions = questionDAO.getByEvaluationId(evaluation.getId());
        Map<Integer, Question> questionMap = new HashMap<>();
        for (Question q : questions) {
            questionMap.put(q.getId(), q);
        }

        List<Reponse> reponses = reponseDAO.getAll();
        List<ReponseDisplay> displayList = new ArrayList<>();

        for (Reponse r : reponses) {
            Question q = questionMap.get(r.getQuestionId());
            if (q != null) {
                String expected = q.getBonneReponse() != null ? q.getBonneReponse().trim().toLowerCase() : "";
                String actual = r.getReponse() != null ? r.getReponse().trim().toLowerCase() : "";

                boolean correctAnswer = expected.equals(actual);

                displayList.add(new ReponseDisplay(
                        r.getUtilisateur(),
                        q.getContenu(),
                        r.getReponse(),
                        q.getBonneReponse(),
                        correctAnswer ? "✅ Correct" : "❌ Faux"
                ));

                total.put(r.getUtilisateur(), total.getOrDefault(r.getUtilisateur(), 0) + 1);
                if (correctAnswer) {
                    correct.put(r.getUtilisateur(), correct.getOrDefault(r.getUtilisateur(), 0) + 1);
                }
            }
        }

        table.getItems().setAll(displayList);

        // Résumé
        VBox statsBox = new VBox(5);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-border-color: #ddd; -fx-background-color: #f9f9f9;");

        for (String utilisateur : total.keySet()) {
            int totalRep = total.get(utilisateur);
            int bonnes = correct.getOrDefault(utilisateur, 0);
            double pourcentage = totalRep > 0 ? ((double) bonnes / totalRep) * 100 : 0;
            Label stat = new Label(utilisateur + " : " + bonnes + "/" + totalRep + " (" + String.format("%.0f", pourcentage) + "%)");
            statsBox.getChildren().add(stat);
        }

        layout.getChildren().addAll(titre, table, new Label("Statistiques :"), statsBox);

        stage.setScene(new Scene(layout, 800, 600));
        stage.show();
    }

    // Classe interne pour afficher dans la table
    public static class ReponseDisplay {
        private String utilisateur, question, reponse, bonneReponse, statut;

        public ReponseDisplay(String utilisateur, String question, String reponse, String bonneReponse, String statut) {
            this.utilisateur = utilisateur;
            this.question = question;
            this.reponse = reponse;
            this.bonneReponse = bonneReponse;
            this.statut = statut;
        }

        public String getUtilisateur() { return utilisateur; }
        public String getQuestion() { return question; }
        public String getReponse() { return reponse; }
        public String getBonneReponse() { return bonneReponse; }
        public String getStatut() { return statut; }
    }
}
