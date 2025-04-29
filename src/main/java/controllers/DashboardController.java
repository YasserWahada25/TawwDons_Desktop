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

        // 📘 Nombre total d'évaluations
        evalCountLabel.setText("📘 Évaluations : " + evaluations.size());

        // 👤 Nombre d'utilisateurs uniques
        long utilisateurs = reponses.stream()
                .map(Reponse::getUtilisateur)
                .distinct()
                .count();
        userCountLabel.setText("👤 Utilisateurs : " + utilisateurs);

        // ✅ Taux de bonnes réponses
        long bonnes = reponses.stream().filter(Reponse::isBonne).count();
        long total = reponses.size();
        double pourcentage = total == 0 ? 0 : (bonnes * 100.0 / total);
        accuracyLabel.setText("✅ Réussite : " + String.format("%.1f", pourcentage) + "%");

        // 🍰 PieChart Bonnes vs Mauvaises
        pieChart.getData().clear();
        pieChart.getData().addAll(
                new PieChart.Data("✅ Bonnes réponses", bonnes),
                new PieChart.Data("❌ Mauvaises réponses", total - bonnes)
        );
        pieChart.setTitle("Distribution des Réponses");

        // 📊 BarChart Activité par Évaluation
        // Regrouper par ID d'évaluation (pas question !)
        Map<Integer, Long> repParEval = reponses.stream()
                .collect(Collectors.groupingBy(Reponse::getEvaluationId, Collectors.counting()));

        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activité");

        for (Evaluation eval : evaluations) {
            long count = repParEval.getOrDefault(eval.getId(), 0L);
            series.getData().add(new XYChart.Data<>(eval.getName(), count));
        }

        barChart.getData().add(series);
        barChart.setTitle("Activité des Utilisateurs");
    }

    @FXML
    private void retourAdmin() {
        Navigation.goTo("Admin.fxml");
    }
}
