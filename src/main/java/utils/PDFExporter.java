package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import models.Evaluation;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    public static void exportEvaluation(List<Evaluation> Evaluation, String filePath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Liste des Évaluations", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            table.addCell("ID");
            table.addCell("Nom");
            table.addCell("Type");
            table.addCell("Date");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Evaluation eval : Evaluation) {
                table.addCell(String.valueOf(eval.getId()));
                table.addCell(eval.getName());
                table.addCell(eval.getType());
                table.addCell(eval.getCreatedAt().format(formatter));
            }

            document.add(table);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
