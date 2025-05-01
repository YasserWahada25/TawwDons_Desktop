package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import models.DemandeDons;
import models.Dons;
import services.DonsService;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

public class RecuPDFGenerator {

    public static boolean generateRecu(DemandeDons demande, String signaturePath) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le Reçu PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
            fileChooser.setInitialFileName("Recu_Don_" + demande.getId() + ".pdf");

            Stage stage = new Stage();
            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile == null) return false;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(selectedFile));
            document.open();

            // 🔷 Ajouter logo de la plateforme
            try {
                Image logo = Image.getInstance("src/main/resources/images/tawwa don 3.png");
                logo.scaleToFit(130, 130);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception ex) {
                System.err.println("⚠️ Logo non chargé : " + ex.getMessage());
            }

            // 🔷 Titre du document
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Reçu de Don", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // 🔷 Informations principales
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 12);

            document.add(new Paragraph("Titre du Don : " + demande.getDonTitre(), labelFont));
            document.add(new Paragraph("Bénéficiaire : " + demande.getBeneficiaireNom(), valueFont));
            document.add(new Paragraph("Date de Demande : " + demande.getDateDemande(), valueFont));
            document.add(new Paragraph("Statut : " + demande.getStatut(), valueFont));
            document.add(new Paragraph(" "));

            // 🔷 Image du don
            DonsService donsService = new DonsService();
            Dons don = donsService.getDonById(demande.getDonsId());
            if (don != null && don.getImageUrl() != null) {
                try {
                    Image donImage = Image.getInstance(don.getImageUrl());
                    donImage.scaleToFit(250, 180);
                    donImage.setAlignment(Element.ALIGN_CENTER);
                    document.add(donImage);
                    document.add(new Paragraph(" "));
                } catch (Exception e) {
                    System.err.println("❌ Erreur chargement image don : " + e.getMessage());
                }
            }

            // 🔷 Signature du donneur
            if (signaturePath != null) {
                try {
                    document.add(new Paragraph(" "));
                    document.add(new Paragraph("Signature du Donneur :", labelFont));
                    Image signature = Image.getInstance(signaturePath);
                    signature.scaleToFit(250, 150); // ✅ Agrandie
                    signature.setAlignment(Element.ALIGN_LEFT); // ✅ Bien alignée
                    document.add(signature);
                } catch (Exception e) {
                    System.err.println("❌ Erreur chargement signature : " + e.getMessage());
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
