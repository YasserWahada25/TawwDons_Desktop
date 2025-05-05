package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Evaluation;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    public static void exportEvaluation(List<Evaluation> evaluations, String filePath) {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Couleurs personnalisées
            BaseColor headerColor = new BaseColor(52, 152, 219);   // Bleu
            BaseColor tableHeaderText = BaseColor.WHITE;
            BaseColor rowColor = new BaseColor(245, 245, 245);     // Gris clair

            // Titre stylisé
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(44, 62, 80));
            Paragraph title = new Paragraph("📝 Liste des Évaluations", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(25);
            document.add(title);

            // Table avec colonnes
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);
            table.setWidths(new float[]{1.5f, 4, 3, 4});

            // Entête stylisée
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, tableHeaderText);
            String[] headers = {"ID", "Nom", "Type", "Date de création"};
            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Format date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Lignes
            Font rowFont = new Font(Font.FontFamily.HELVETICA, 11);
            boolean alternate = false;
            for (Evaluation eval : evaluations) {
                BaseColor bgColor = alternate ? BaseColor.WHITE : rowColor;
                table.addCell(createCell(String.valueOf(eval.getId()), rowFont, bgColor));
                table.addCell(createCell(eval.getName(), rowFont, bgColor));
                table.addCell(createCell(eval.getType(), rowFont, bgColor));
                table.addCell(createCell(eval.getCreatedAt().format(formatter), rowFont, bgColor));
                alternate = !alternate;
            }

            document.add(table);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private static PdfPCell createCell(String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}
