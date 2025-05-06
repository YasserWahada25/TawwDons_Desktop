package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.DemandeDons;
import services.DonsService;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

public class RecuPDFGenerator {

    private static String lastSignaturePath = null; // Signature persistante

    public static boolean generateRecu(DemandeDons demande, String signaturePath) {
        try {
            if (signaturePath != null) {
                lastSignaturePath = signaturePath;
            }

            if (lastSignaturePath == null) {
                System.err.println("⚠️ Aucune signature disponible.");
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le Reçu PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
            fileChooser.setInitialFileName("Recu_Don_" + demande.getId() + ".pdf");

            File selectedFile = fileChooser.showSaveDialog(new Stage());
            if (selectedFile == null) return false;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(selectedFile));
            document.open();

            // 🔷 Logo aligné à droite
            try {
                Image logo = Image.getInstance("src/main/resources/images/queen.png");
                logo.scaleToFit(80, 80);
                logo.setAlignment(Image.ALIGN_RIGHT);
                document.add(logo);
            } catch (Exception ex) {
                System.err.println("⚠️ Logo non chargé : " + ex.getMessage());
            }

            // 🔷 Titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Reçu de Don", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // 🔷 Infos principales
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 12);

            document.add(new Paragraph("Titre du Don : " + demande.getDonTitre(), labelFont));
            document.add(new Paragraph("Bénéficiaire : " + (demande.getBeneficiaireNom() != null ? demande.getBeneficiaireNom() : "Inconnu"), valueFont));
            document.add(new Paragraph("Date de Demande : " + demande.getDateDemande(), valueFont));
            document.add(new Paragraph("Statut : " + demande.getStatut(), valueFont));
            document.add(new Paragraph(" "));

            // 🔷 Signature du donneur
            if (lastSignaturePath != null) {
                File signatureFile = new File(lastSignaturePath);
                if (signatureFile.exists()) {
                    try {
                        document.add(new Paragraph("Signature du Donneur :", labelFont));
                        Image signature = Image.getInstance(signatureFile.getAbsolutePath());
                        signature.scaleToFit(250, 150);
                        signature.setAlignment(Image.ALIGN_LEFT);
                        document.add(signature);
                        document.add(new Paragraph(" "));
                    } catch (Exception e) {
                        System.err.println("❌ Erreur chargement signature : " + e.getMessage());
                    }
                } else {
                    System.err.println("⚠️ Signature introuvable : " + lastSignaturePath);
                }
            }

            document.close();
            Desktop.getDesktop().open(selectedFile);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
