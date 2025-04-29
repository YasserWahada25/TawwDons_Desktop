package controllers;

import dao.BanDAO;
import dao.EvaluationDAO;
import dao.QuestionDAO;
import dao.ReponseDAO;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Evaluation;
import models.Question;
import models.Reponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {

    @FXML private ComboBox<Evaluation> evaluationBox;
    @FXML private TextField utilisateurField;
    @FXML private VBox questionsBox;
    @FXML private VBox formView;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private StackPane toastContainer;
    @FXML private Label toastMessage;
    @FXML private VBox chatBox;
    @FXML private TextField chatInput;

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ReponseDAO reponseDAO = new ReponseDAO();
    private final BanDAO banDAO = BanDAO.getInstance();
    private final Map<Question, Control> reponsesMap = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            List<Evaluation> evaluations = new EvaluationDAO().getAll();
            if (evaluationBox != null) {
                evaluationBox.getItems().addAll(evaluations);
                evaluationBox.setPromptText("Sélectionnez une évaluation");
                evaluationBox.setOnAction(e -> {
                    Evaluation selectedEval = evaluationBox.getSelectionModel().getSelectedItem();
                    if (selectedEval != null) loadQuestions(selectedEval);
                });
            }
            if (formView != null) formView.setVisible(false);
            if (progressBar != null) progressBar.setProgress(0);
            if (progressLabel != null) progressLabel.setText("Progression : 0%");
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation : " + e.getMessage());
        }
    }

    @FXML
    private void envoyerMessage() {
        String question = chatInput.getText().trim();
        if (question.isEmpty()) return;

        afficherMessage("\uD83D\uDC64 Vous : " + question, "#ecf0f1");
        String reponse = repondre(question.toLowerCase());
        afficherMessage("\uD83E\uDD16 ChatBot : " + reponse, "#dff9fb");
        chatInput.clear();
    }

    @FXML
    private void ouvrirMiniJeu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/MiniJeu.fxml"));
            Stage stage = new Stage();
            stage.setTitle("\uD83E\uDDEA Jeu de Santé");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherMessage(String text, String bgColor) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 13;");
        chatBox.getChildren().add(label);
    }

    private String repondre(String question) {
        if (question.contains("stress") || question.contains("anxieux")) return "Respire profondément, fais une pause \uD83D\uDE0A";
        if (question.contains("eau") || question.contains("boire")) return "Boire de l'eau améliore la concentration \uD83D\uDCA7";
        if (question.contains("conseil") || question.contains("astuce")) return "Lis bien chaque question, fais confiance à ton instinct !";
        if (question.contains("question")) return "Cherche les mots-clés dans l'énoncé.";
        if (question.contains("bonjour") || question.contains("salut")) return "Salut ! Je suis ton assistant \uD83D\uDE0A";
        return "Désolé, je ne suis pas sûr. Peux-tu reformuler ?";
    }

    @FXML
    private void montrerFormulaire() {
        formView.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(600), formView);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void loadQuestions(Evaluation evaluation) {
        questionsBox.getChildren().clear();
        reponsesMap.clear();

        List<Question> questions = questionDAO.getByEvaluationId(evaluation.getId());
        if (questions.isEmpty()) {
            Label label = new Label("Aucune question trouvée.");
            label.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-font-weight: bold;");
            questionsBox.getChildren().add(label);
            return;
        }

        int delay = 0;
        for (Question q : questions) {
            VBox questionCard = new VBox(10);
            questionCard.setStyle("""
                -fx-background-color: #ffffff;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-color: #dfe6e9;
                -fx-border-width: 1;
                -fx-padding: 20;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 4);
            """);

            Label qLabel = new Label(q.getContenu());
            qLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 15px; -fx-font-weight: bold;");

            Control input;
            if ("qcm".equalsIgnoreCase(q.getType())) {
                ComboBox<String> choix = new ComboBox<>();
                choix.getItems().addAll("Vrai", "Faux");
                choix.setPromptText("Choisissez");
                choix.setOnAction(e -> updateProgress());
                input = choix;
            } else {
                TextField champ = new TextField();
                champ.setPromptText("Votre réponse...");
                champ.textProperty().addListener((obs, oldText, newText) -> updateProgress());
                input = champ;
            }

            questionCard.getChildren().addAll(qLabel, input);
            questionsBox.getChildren().add(questionCard);
            reponsesMap.put(q, input);

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), questionCard);
            tt.setFromX(100);
            tt.setToX(0);
            tt.setDelay(Duration.millis(delay));
            tt.play();
            delay += 100;
        }

        updateProgress();
    }

    private void updateProgress() {
        int total = reponsesMap.size();
        if (total == 0) {
            progressBar.setProgress(0);
            progressLabel.setText("Progression : 0%");
            return;
        }

        long remplis = reponsesMap.values().stream().filter(c -> {
            if (c instanceof TextField tf) return !tf.getText().isBlank();
            if (c instanceof ComboBox<?> cb) return cb.getValue() != null;
            return false;
        }).count();

        double progress = (double) remplis / total;
        progressBar.setProgress(progress);
        progressLabel.setText("Progression : " + (int) (progress * 100) + "%");
    }

    @FXML
    private void envoyerReponses() {
        Evaluation evaluation = evaluationBox.getSelectionModel().getSelectedItem();
        if (evaluation == null) {
            showToast("Veuillez sélectionner une évaluation.", "#e74c3c");
            return;
        }

        String nom = utilisateurField.getText().trim();
        if (nom.isEmpty()) {
            showToast("Veuillez entrer votre nom.", "#e74c3c");
            return;
        }

        if (banDAO.isBanni(nom)) {
            showAlertAndRetourAccueil("Vous êtes banni. Vous ne pouvez pas envoyer de réponses.");
            return;
        }

        boolean erreur = false;

        for (Map.Entry<Question, Control> entry : reponsesMap.entrySet()) {
            Question q = entry.getKey();
            Control input = entry.getValue();
            String value = (input instanceof TextField tf) ? tf.getText() : (input instanceof ComboBox<?> cb && cb.getValue() != null) ? cb.getValue().toString() : "";

            if (value.isBlank()) {
                erreur = true;
                continue;
            }

            Reponse r = new Reponse();
            r.setUtilisateur(nom);
            r.setReponse(value);
            r.setQuestionId(q.getId());
            r.setDateReponse(LocalDateTime.now());

            reponseDAO.add(r);
        }

        if (erreur) showToast("Certaines réponses sont incomplètes.", "#f39c12");
        else showToast("Réponses enregistrées avec succès ✅", "#2ecc71");

        utilisateurField.clear();
        evaluationBox.getSelectionModel().clearSelection();
        questionsBox.getChildren().clear();
        progressBar.setProgress(0);
        progressLabel.setText("Progression : 0%");
    }

    private void showToast(String message, String bgColor) {
        toastMessage.setText(message);
        toastMessage.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold;");
        toastMessage.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            PauseTransition wait = new PauseTransition(Duration.seconds(3));
            wait.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastMessage);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(evt -> toastMessage.setVisible(false));
                fadeOut.play();
            });
            wait.play();
        });
        fadeIn.play();
    }

    private void showAlertAndRetourAccueil(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Banni");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        retourAccueil();
    }

    @FXML
    private void retourAccueil() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) utilisateurField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirBrainChess() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/BrainChess.fxml"));
        Stage stage = new Stage();
        stage.setTitle("\uD83E\uDDEA Brain Chess");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void ouvrirChat() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/Chatbot.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Assistant Santé");
        stage.setScene(new Scene(root));
        stage.show();
    }
}