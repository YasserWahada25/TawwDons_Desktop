package views;

import dao.QuestionDAO;
import dao.ReponseDAO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Evaluation;
import models.Question;
import models.Reponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserEvaluationView {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ReponseDAO reponseDAO = new ReponseDAO();

    public void show(Evaluation evaluation) {
        Stage stage = new Stage();
        stage.setTitle("Évaluation : " + evaluation.getNom());

        Label titre = new Label("Évaluation : " + evaluation.getNom());
        Label description = new Label("Description : " + evaluation.getDescription());

        TextField utilisateurField = new TextField();
        utilisateurField.setPromptText("Entrez votre nom");

        VBox questionsContainer = new VBox(15);
        questionsContainer.setPadding(new Insets(10));

        Map<Question, Control> champsReponses = new HashMap<>();

        List<Question> questions = questionDAO.getByEvaluationId(evaluation.getId());

        for (Question q : questions) {
            VBox bloc = new VBox(5);
            bloc.setPadding(new Insets(5));
            bloc.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 5;");

            Label questionLabel = new Label(q.getContenu());
            bloc.getChildren().add(questionLabel);

            if (q.getType().equalsIgnoreCase("qcm")) {
                ComboBox<String> choix = new ComboBox<>();
                choix.getItems().addAll("vrai", "faux");
                choix.setPromptText("Choisissez votre réponse");
                bloc.getChildren().add(choix);
                champsReponses.put(q, choix);
            } else {
                TextField reponseField = new TextField();
                reponseField.setPromptText("Votre réponse");
                bloc.getChildren().add(reponseField);
                champsReponses.put(q, reponseField);
            }

            questionsContainer.getChildren().add(bloc);
        }

        Button envoyerBtn = new Button("Envoyer les réponses");
        envoyerBtn.setOnAction(e -> {
            String nom = utilisateurField.getText().trim();
            if (nom.isEmpty()) {
                showAlert("Nom requis", "Veuillez entrer votre nom.");
                return;
            }

            for (Map.Entry<Question, Control> entry : champsReponses.entrySet()) {
                String valeur = "";

                if (entry.getValue() instanceof TextField tf) {
                    valeur = tf.getText();
                } else if (entry.getValue() instanceof ComboBox<?> cb && cb.getValue() != null) {
                    valeur = cb.getValue().toString();
                }

                Reponse r = new Reponse();
                r.setUtilisateur(nom);
                r.setReponse(valeur);
                r.setQuestionId(entry.getKey().getId());
                r.setDateReponse(LocalDateTime.now());
                reponseDAO.add(r);
            }

            showAlert("Succès", "Réponses enregistrées !");
            stage.close();
        });

        VBox root = new VBox(20, titre, description, utilisateurField, questionsContainer, envoyerBtn);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 600, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
