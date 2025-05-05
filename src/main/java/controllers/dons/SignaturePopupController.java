package controllers.dons;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SignaturePopupController {

    @FXML
    private Canvas signatureCanvas;
    @FXML
    private Button btnValider;
    @FXML
    private Button btnEffacer;

    private GraphicsContext gc;
    private double lastX, lastY;
    private boolean drawing = false;

    private File signatureFile;

    @FXML
    public void initialize() {
        gc = signatureCanvas.getGraphicsContext2D();

        signatureCanvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
            drawing = true;
        });

        signatureCanvas.setOnMouseDragged(e -> {
            if (drawing) {
                double x = e.getX();
                double y = e.getY();
                gc.strokeLine(lastX, lastY, x, y);
                lastX = x;
                lastY = y;
            }
        });

        signatureCanvas.setOnMouseReleased(e -> drawing = false);
    }

    @FXML
    private void clearCanvas() {
        gc.clearRect(0, 0, signatureCanvas.getWidth(), signatureCanvas.getHeight());
    }

    @FXML
    private void saveSignature() {
        try {
            WritableImage image = signatureCanvas.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            signatureFile = new File("signature_" + System.currentTimeMillis() + ".png");
            ImageIO.write(bufferedImage, "png", signatureFile);

            System.out.println("✅ Signature enregistrée dans : " + signatureFile.getAbsolutePath());

            // Fermer la fenêtre après enregistrement
            Stage stage = (Stage) signatureCanvas.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getSignatureFile() {
        return signatureFile;
    }
}
