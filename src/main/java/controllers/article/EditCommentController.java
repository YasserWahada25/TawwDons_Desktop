package controllers.article;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import models.Commentaire;
import services.CommentaireService;

public class EditCommentController {

    @FXML private TextArea txtContent;
    @FXML private Button btnCancel, btnSave;

    private Commentaire commentaire;
    private Runnable onUpdated;


    public void init(Commentaire c, Runnable onUpdated) {
        this.commentaire = c;
        this.onUpdated = onUpdated;
        txtContent.setText(c.getContent());

        btnCancel.setOnAction(evt -> {
            ((Stage) btnCancel.getScene().getWindow()).close();
        });

        btnSave.setOnAction(evt -> {
            String nouveauTexte = txtContent.getText().trim();
            if (nouveauTexte.length() < 3) {
                // on pourrait afficher une alerte, ici on ne ferme pas
                txtContent.getStyleClass().add("error");
                return;
            }


            commentaire.setContent(nouveauTexte);
            new CommentaireService().modifier(commentaire);

            if (onUpdated != null) {
                onUpdated.run();
            }

            ((Stage) btnSave.getScene().getWindow()).close();
        });
    }
}
