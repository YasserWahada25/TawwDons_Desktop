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

    @FXML private Label evalCountLabel, userCountLabel, accuracyLabel, avgScoreLabel;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart, barChartScore;


    private final EvaluationDAO evalDAO = new EvaluationDAO();
    private final ReponseDAO repDAO = new ReponseDAO();

    @FXML
    public void initialize() {
        List<Evaluation> evaluations = evalDAO.getAll();
        List<Reponse> reponses = repDAO.getAll();

        // 1. Total des évaluations
        evalCountLabel.setText("📘 Évaluations : " + evaluations.size());

        // 2. Utilisateurs uniques
        long utilisateurs = reponses.stream()
                .map(Reponse::getUtilisateur)
                .distinct()
                .count();
        userCountLabel.setText("👤 Utilisateurs : " + utilisateurs);

        // 3. Taux global de bonnes réponses
        long bonne = reponses.stream().filter(Reponse::isBonne).count();
        long total = reponses.size();
        double tauxBonnes = total == 0 ? 0 : (bonne * 100.0 / total);
        accuracyLabel.setText("✅ Réussite Globale : " + String.format("%.1f", tauxBonnes) + "%");

        // 4. Score moyen par utilisateur (optionnel)
        double avgScore = utilisateurs == 0 ? 0 : (bonne * 1.0 / utilisateurs);
        avgScoreLabel.setText("📈 Score Moyen : " + String.format("%.2f", avgScore));

        // 5. PieChart Bonnes vs Mauvaises réponses
        pieChart.getData().clear();
        pieChart.getData().addAll(
                new PieChart.Data("✅ Bonnes réponses", bonne),
                new PieChart.Data("❌ Mauvaises réponses", total - bonne)
        );
        pieChart.setTitle("Distribution des Réponses");

        // 6. BarChart - Activité (nombre de réponses) par évaluation
        Map<Integer, Long> repParEval = reponses.stream()
                .collect(Collectors.groupingBy(Reponse::getEvaluationId, Collectors.counting()));

        XYChart.Series<String, Number> seriesActivite = new XYChart.Series<>();
        seriesActivite.setName("Réponses par Évaluation");

        for (Evaluation eval : evaluations) {
            long count = repParEval.getOrDefault(eval.getId(), 0L);
            seriesActivite.getData().add(new XYChart.Data<>(eval.getName(), count));
        }

        barChart.getData().clear();
        barChart.getData().add(seriesActivite);
        barChart.setTitle("Activité des Utilisateurs");

        // 7. BarChart - Score par évaluation (taux de bonnes réponses)
        Map<Integer, List<Reponse>> repGroupes = reponses.stream()
                .collect(Collectors.groupingBy(Reponse::getEvaluationId));

        XYChart.Series<String, Number> seriesScore = new XYChart.Series<>();
        seriesScore.setName("% Réponses Correctes");

        for (Evaluation eval : evaluations) {
            List<Reponse> repEval = repGroupes.getOrDefault(eval.getId(), List.of());
            long totalRep = repEval.size();
            long bonnesRep = repEval.stream().filter(Reponse::isBonne).count();
            double score = totalRep == 0 ? 0 : (bonnesRep * 100.0 / totalRep);
            seriesScore.getData().add(new XYChart.Data<>(eval.getName(), score));
        }

        barChartScore.getData().clear();
        barChartScore.getData().add(seriesScore);
        barChartScore.setTitle("Score par Évaluation (%)");
    }

    @FXML
    private void retourAdmin() {
        Navigation.goTo("Admin.fxml");
    }
}
