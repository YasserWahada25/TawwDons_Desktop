package controllers;

import dao.EvaluationDAO;
import dao.QuestionDAO;
import dao.ReponseDAO;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
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
    private final Map<Question, Control> reponsesMap = new HashMap<>();

    private Timeline timer;
    private int secondes = 0;

    @FXML
    public void initialize() {
        try {
            List<Evaluation> evaluations = new EvaluationDAO().getAll();
            if (evaluationBox != null) {
                evaluationBox.getItems().addAll(evaluations);
                evaluationBox.setPromptText("Sélectionnez une évaluation");
                evaluationBox.setOnAction(e -> {
                    Evaluation selectedEval = evaluationBox.getSelectionModel().getSelectedItem();
                    if (selectedEval != null) {
                        loadQuestions(selectedEval);
                    }
                });
            }
            if (formView != null) formView.setVisible(false);
            if (progressBar != null) progressBar.setProgress(0);
            if (progressLabel != null) progressLabel.setText("Progression : 0%");
        } catch (Exception e) {
            System.err.println("⚠ Erreur d'initialisation : " + e.getMessage());
        }
    }

    // ========================== CHATBOT ==========================
    @FXML
    private void envoyerMessage() {
        String question = chatInput.getText().trim();
        if (question.isEmpty()) return;

        afficherMessage("👤 Vous : " + question, "#ecf0f1");

        String reponse = repondre(question.toLowerCase());
        afficherMessage("🤖 ChatBot : " + reponse, "#dff9fb");

        chatInput.clear();
    }

    private void afficherMessage(String text, String bgColor) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 13;");
        chatBox.getChildren().add(label);
    }

    private String repondre(String question) {
        if (question.contains("stress") || question.contains("anxieux")) return "Respire profondément, fais une pause 😊";
        if (question.contains("eau") || question.contains("boire")) return "Boire de l’eau améliore la concentration 💧";
        if (question.contains("conseil") || question.contains("astuce")) return "Lis bien chaque question, fais confiance à ton instinct !";
        if (question.contains("question")) return "Cherche les mots-clés dans l'énoncé.";
        if (question.contains("bonjour") || question.contains("salut")) return "Salut ! Je suis ton assistant 😊";
        return "Désolé, je ne suis pas sûr. Peux-tu reformuler ?";
    }

    @FXML
    private void ouvrirChat() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Chatbot.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Assistant ChatBot");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Erreur de chargement du ChatBot.", "#e74c3c");
        }
    }

    // ===================== MINI JEU DE MÉMOIRE ====================
    @FXML
    private void ouvrirMiniJeu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/MiniJeu.fxml"));
            Stage stage = new Stage();
            stage.setTitle("🧠 Jeu de Santé");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Erreur d'ouverture du jeu", "#e74c3c");
        }
    }


    // ===================== RÉPONSES AUX QUESTIONS =====================
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
            label.setStyle("-fx-text-fill: white;");
            questionsBox.getChildren().add(label);
            return;
        }

        int delay = 0;
        for (Question q : questions) {
            VBox bloc = new VBox(5);
            bloc.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-padding: 15;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0.3, 0, 2);
            """);

            Label qLabel = new Label(q.getContenu());
            qLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");

            Control input;
            if ("qcm".equalsIgnoreCase(q.getType())) {
                ComboBox<String> choix = new ComboBox<>();
                choix.getItems().addAll("vrai", "faux");
                choix.setPromptText("Choisissez");
                choix.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 6;");
                choix.setOnAction(e -> updateProgress());
                input = choix;
            } else {
                TextField champ = new TextField();
                champ.setPromptText("Votre réponse");
                champ.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 6;");
                champ.textProperty().addListener((obs, oldText, newText) -> updateProgress());
                input = champ;
            }

            bloc.getChildren().addAll(qLabel, input);
            questionsBox.getChildren().add(bloc);
            reponsesMap.put(q, input);

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), bloc);
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

        if (progress < 0.5) {
            progressBar.setStyle("-fx-accent: red;");
        } else if (progress < 0.8) {
            progressBar.setStyle("-fx-accent: orange;");
        } else {
            progressBar.setStyle("-fx-accent: #2ecc71;");
        }
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

        if (!nom.matches("^[A-Z][a-zA-Z0-9\\s]*$")) {
            showToast("Le nom doit commencer par une majuscule.", "#f39c12");
            return;
        }

        boolean erreur = false;

        for (Map.Entry<Question, Control> entry : reponsesMap.entrySet()) {
            Question q = entry.getKey();
            Control input = entry.getValue();
            String value = "";

            if (input instanceof TextField tf) value = tf.getText();
            else if (input instanceof ComboBox<?> cb && cb.getValue() != null) value = cb.getValue().toString();

            if (value == null || value.isBlank()) {
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

        if (erreur) {
            showToast("Certaines réponses sont incomplètes.", "#f39c12");
        } else {
            showToast("Réponses enregistrées avec succès ✅", "#2ecc71");
        }

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

    @FXML
    private void retourAccueil() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Acceuil.fxml"));
            Stage stage = (Stage) utilisateurField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void ouvrirBrainChess() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BrainChess.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("🧠 Brain Chess");
        stage.setScene(new Scene(root));
        stage.show();
    }

}

