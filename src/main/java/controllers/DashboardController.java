package controllers;

import dao.EvaluationDAO;
import dao.ReponseDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import models.Evaluation;
import models.Reponse;
import utils.Navigation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label evalCountLabel, userCountLabel, accuracyLabel;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    private final EvaluationDAO evalDAO = new EvaluationDAO();
    private final ReponseDAO repDAO = new ReponseDAO();

    @FXML
    public void initialize() {
        List<Evaluation> evaluations = evalDAO.getAll();
        List<Reponse> reponses = repDAO.getAll();

        // Nombre total d'évaluations
        evalCountLabel.setText("📘 Évaluations : " + evaluations.size());

        // Utilisateurs uniques
        long utilisateurs = reponses.stream()
                .map(Reponse::getUtilisateur)
                .distinct()
                .count();
        userCountLabel.setText("👤 Utilisateurs : " + utilisateurs);

        // Taux de bonnes réponses
        long bonnes = reponses.stream().filter(Reponse::isBonne).count();
        long total = reponses.size();
        double pourcentage = total == 0 ? 0 : (bonnes * 100.0 / total);
        accuracyLabel.setText("✅ Réussite : " + String.format("%.1f", pourcentage) + "%");

        // PieChart bonnes vs mauvaises
        pieChart.getData().addAll(
                new PieChart.Data("Bonnes", bonnes),
                new PieChart.Data("Mauvaises", total - bonnes)
        );
        pieChart.setTitle("Distribution des Réponses");

        // BarChart réponses par évaluation
        Map<Integer, Long> repParEval = reponses.stream()
                .collect(Collectors.groupingBy(Reponse::getQuestionId, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Evaluation eval : evaluations) {
            long count = repParEval.entrySet().stream()
                    .filter(e -> evalDAO.getEvaluationIdFromQuestionId(e.getKey()) == eval.getId())
                    .mapToLong(Map.Entry::getValue)
                    .sum();
            series.getData().add(new XYChart.Data<>(eval.getNom(), count));
        }
        barChart.getData().add(series);
    }
    @FXML
    private void retourAdmin() {
        Navigation.goTo("Admin.fxml");
    }
}
