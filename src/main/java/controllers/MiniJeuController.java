package controllers;

import dao.MiniJeuScoreDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import models.MiniJeuScore;
import models.QuestionQCM;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiniJeuController {

    @FXML private Label questionLabel, chronoLabel, titreJeu, scoreLabel;
    @FXML private TextField joueurField;
    @FXML private ComboBox<String> niveauBox;
    @FXML private Button commencerButton, validerButton;
    @FXML private GridPane choixBox;

    private final List<QuestionQCM> toutesQuestions = new ArrayList<>();
    private final MiniJeuScoreDAO scoreDAO = new MiniJeuScoreDAO();

    private List<QuestionQCM> questions;
    private int currentIndex = 0, score = 0, secondesRestantes = 60;
    private String niveau;
    private ToggleGroup choixGroup;
    private Timeline timer;

    @FXML
    public void initialize() {
        niveauBox.getItems().addAll("Facile", "Moyen", "Difficile");
        validerButton.setDisable(true);
        chronoLabel.setText("");
        scoreLabel.setText("\uD83E\uDDE0 Score : 0");
        chargerQuestionsStatiques();
    }

    @FXML
    private void demarrerJeu() {
        niveau = niveauBox.getValue();
        if (niveau == null || joueurField.getText().isBlank()) {
            showAlert("⚠ Veuillez entrer votre nom et choisir un niveau !");
            return;
        }

        questions = toutesQuestions.stream()
                .filter(q -> q.getNiveau().equals(niveau))
                .toList();
        questions = new ArrayList<>(questions);
        Collections.shuffle(questions);
        questions = questions.subList(0, Math.min(10, questions.size()));

        score = 0;
        currentIndex = 0;
        scoreLabel.setText("\uD83E\uDDE0 Score : 0");
        afficherQuestion();
    }

    private void afficherQuestion() {
        if (currentIndex >= questions.size()) {
            finDuJeu();
            return;
        }

        QuestionQCM q = questions.get(currentIndex);
        questionLabel.setText("Question " + (currentIndex + 1) + " : " + q.getQuestion());

        choixGroup = new ToggleGroup();
        choixBox.getChildren().clear();
        choixBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < q.getChoix().size(); i++) {
            String opt = q.getChoix().get(i);
            RadioButton rb = new RadioButton(opt);
            rb.setToggleGroup(choixGroup);
            rb.getStyleClass().add("choice-button");

            int row = i / 2;
            int col = i % 2;
            choixBox.add(rb, col, row);
        }

        validerButton.setDisable(false);
        lancerChrono();
    }

    @FXML
    private void validerReponse() {
        RadioButton selected = (RadioButton) choixGroup.getSelectedToggle();

        if (selected == null) {
            showAlert("⚠ Veuillez sélectionner une réponse avant de valider !");
            return;
        }

        if (timer != null) timer.stop();

        if (selected.getText().equals(questions.get(currentIndex).getBonneReponse())) {
            score++;
        }

        scoreLabel.setText("\uD83E\uDDE0 Score : " + score);
        currentIndex++;
        afficherQuestion();
    }

    private void lancerChrono() {
        secondesRestantes = 60;
        chronoLabel.setText("⏳ Temps restant : 60s");

        if (timer != null) timer.stop();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondesRestantes--;
            chronoLabel.setText("⏳ Temps restant : " + secondesRestantes + "s");

            if (secondesRestantes <= 0) {
                timer.stop();
                showAlert("⏳ Temps écoulé pour cette question !");
                validerReponse();
            }
        }));
        timer.setCycleCount(60);
        timer.play();
    }

    private void finDuJeu() {
        String nom = joueurField.getText().trim();
        MiniJeuScore result = new MiniJeuScore(nom, score, niveau, LocalDateTime.now());
        scoreDAO.enregistrer(result);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("\uD83C\uDF89 Fin du Jeu");
        alert.setHeaderText("Bravo " + nom + " !");
        alert.setContentText("Ton score est : " + score + " / " + questions.size());
        alert.showAndWait();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

private void chargerQuestionsStatiques() {
        // ✅ Facile
        toutesQuestions.add(new QuestionQCM("Combien de verres d'eau faut-il boire par jour ?", List.of("4", "6", "8", "12"), "8", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quel organe pompe le sang ?", List.of("Foie", "Cerveau", "Cœur", "Estomac"), "Cœur", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quel est l'organe principal de la respiration ?", List.of("Poumons", "Estomac", "Cœur", "Reins"), "Poumons", "Facile"));
        toutesQuestions.add(new QuestionQCM("Combien de doigts a une main ?", List.of("4", "5", "6", "7"), "5", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quel est le principal nutriment dans les fruits ?", List.of("Sucres", "Graisses", "Protéines", "Fibres"), "Sucres", "Facile"));
        toutesQuestions.add(new QuestionQCM("Combien d’heures de sommeil faut-il pour un adulte ?", List.of("4", "6", "8", "10"), "8", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quelle vitamine est produite par le soleil ?", List.of("A", "C", "D", "K"), "D", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quelle boisson est la plus recommandée ?", List.of("Coca", "Café", "Eau", "Jus de fruit"), "Eau", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quel est l’organe digestif principal ?", List.of("Estomac", "Foie", "Intestin", "Cerveau"), "Estomac", "Facile"));
        toutesQuestions.add(new QuestionQCM("Quel est l’aliment le plus riche en calcium ?", List.of("Viande", "Lait", "Riz", "Poisson"), "Lait", "Facile"));

        // ✅ Moyen
        toutesQuestions.add(new QuestionQCM("Quel est le rôle des globules blancs ?", List.of("Transporter l'oxygène", "Combattre les infections", "Digérer les aliments", "Réguler le sommeil"), "Combattre les infections", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quel est le nom scientifique du sucre dans le sang ?", List.of("Glucose", "Fructose", "Lactose", "Saccharose"), "Glucose", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Combien de calories par jour pour un adulte ?", List.of("1200", "2000", "3000", "4000"), "2000", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quelle partie du cerveau contrôle l’équilibre ?", List.of("Cervelet", "Hippocampe", "Tronc cérébral", "Amygdale"), "Cervelet", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quel est le rôle du foie ?", List.of("Pomper le sang", "Filtrer le sang", "Produire du mucus", "Contrôler la respiration"), "Filtrer le sang", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quel est l’organe qui produit l’insuline ?", List.of("Estomac", "Pancréas", "Foie", "Reins"), "Pancréas", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quelle est la durée moyenne d’un cycle de sommeil ?", List.of("30 min", "60 min", "90 min", "120 min"), "90 min", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Combien de dents a un adulte ?", List.of("28", "30", "32", "34"), "32", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quelle est la température corporelle normale ?", List.of("35°C", "36.5°C", "37°C", "38°C"), "37°C", "Moyen"));
        toutesQuestions.add(new QuestionQCM("Quelle maladie est causée par une carence en fer ?", List.of("Anémie", "Diabète", "Asthme", "Migraine"), "Anémie", "Moyen"));

        // ✅ Difficile
        toutesQuestions.add(new QuestionQCM("Combien d'os dans le corps humain ?", List.of("206", "198", "223", "150"), "206", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quelle hormone est responsable du sommeil ?", List.of("Insuline", "Sérotonine", "Mélatonine", "Adrénaline"), "Mélatonine", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quelle est la plus grande artère du corps humain ?", List.of("Carotide", "Veine cave", "Aorte", "Radiale"), "Aorte", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Combien de lobes a le foie humain ?", List.of("2", "3", "4", "5"), "4", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quelle est la durée moyenne d’un battement de cœur ?", List.of("0.2s", "0.5s", "0.8s", "1.2s"), "0.8s", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quel est l’acide produit dans l’estomac ?", List.of("Acide lactique", "Acide sulfurique", "Acide chlorhydrique", "Acide citrique"), "Acide chlorhydrique", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Combien de muscles dans le corps humain ?", List.of("450", "550", "600", "650"), "600", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quel est le pH du sang humain ?", List.of("6.5", "7.0", "7.4", "8.0"), "7.4", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quelle glande contrôle la croissance ?", List.of("Thyroïde", "Hypophyse", "Surrénale", "Pancréas"), "Hypophyse", "Difficile"));
        toutesQuestions.add(new QuestionQCM("Quel est le plus long os du corps ?", List.of("Humérus", "Fémur", "Tibia", "Radius"), "Fémur", "Difficile"));
    }

}
