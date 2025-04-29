package org.example.event.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.event.model.participant;
import org.example.event.utils.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class participantService implements Iparticipant<participant>{
    Connection cnx;

    public participantService() {
        cnx = database.getInstance().getCnx();
    }

    @Override
    public void ajouter(participant participant) throws SQLException {
        String sql = "INSERT INTO participant(nom, prenom, email, numtel, event_id) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, participant.getNom());
            ps.setString(2, participant.getPrenom());
            ps.setString(3, participant.getEmail());
            ps.setInt(4, participant.getNumtel());
            ps.setInt(5, participant.getEvent_id());
            ps.executeUpdate();
            System.out.println("Participant ajouté");
        }
    }

    @Override
    public void supprimer(participant participant) throws SQLException {
        String sql = "DELETE FROM participant WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, participant.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(participant participant) throws SQLException {
        String sql = "UPDATE participant SET nom=?, prenom=?, email=?, numtel=?, event_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, participant.getNom());
            ps.setString(2, participant.getPrenom());
            ps.setString(3, participant.getEmail());
            ps.setInt(4, participant.getNumtel());
            ps.setInt(5, participant.getEvent_id());
            ps.setInt(6, participant.getId());
            ps.executeUpdate();
            System.out.println("Participant modifié");
        }
    }
    public void exportToExcel(List<participant> participants, String filePath)
            throws IOException, IllegalArgumentException {

        // Validation des paramètres
        if (participants == null) {
            throw new IllegalArgumentException("La liste des participants ne peut pas être null");
        }

        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }

        if (!filePath.toLowerCase().endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // Création de la feuille Excel
            Sheet sheet = workbook.createSheet("Participants");

            // Style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // En-têtes de colonnes
            String[] headers = {"ID", "Nom", "Prénom", "Email", "Téléphone", "ID Événement"};
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplissage des données
            int rowNum = 1;
            for (participant p : participants) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNom());
                row.createCell(2).setCellValue(p.getPrenom());
                row.createCell(3).setCellValue(p.getEmail());
                row.createCell(4).setCellValue(p.getNumtel());
                row.createCell(5).setCellValue(p.getEvent_id());
            }

            // Ajustement automatique des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écriture du fichier
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
                System.out.println("Fichier Excel généré avec succès: " + filePath);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du fichier Excel: " + e.getMessage());
            throw new IOException("Échec de l'export Excel: " + e.getMessage(), e);
        }
    }
    @Override
    public List<participant> getList() throws SQLException {
        List<participant> participants = new ArrayList<>();
        String sql = "SELECT * FROM participant";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                participant p = new participant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getInt("numtel"),
                        rs.getInt("event_id")
                );
                participants.add(p);
                System.out.println("Participant chargé: " + p.getNom());
            }
            System.out.println("Total participants: " + participants.size());
        }
        return participants;
    }
    public int countParticipantsByEvent(int eventId) throws SQLException {
        String query = "SELECT COUNT(*) FROM participant WHERE event_id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, eventId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public List<participant> getParticipantByEvent(int eventId) throws SQLException {
        System.out.println("Exécution de getParticipantByEvent()...");
        List<participant> participants = new ArrayList<>();
        String query = "SELECT * FROM participant WHERE event_id = ?";
        System.out.println("Requête SQL: " + query);

        try (PreparedStatement st = cnx.prepareStatement(query)) {
            st.setInt(1, eventId); // Paramètre défini AVANT l'exécution
            System.out.println("Paramètre participantid_id défini à: " + eventId);

            System.out.println("Exécution de la requête...");
            try (ResultSet rs = st.executeQuery()) { // Exécution après paramétrage
                int count = 0;

                while (rs.next()) {
                    count++;
                    participant m = new participant(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getInt("numtel"),
                            rs.getInt("event_id")
                    );

                    participants.add(m);
                    System.out.println("participant ajouté: " + m.getNom());
                }
                System.out.println("Total participants chargés: " + count); // Correction du message
            }
        } catch (SQLException e) {
            System.err.println("ERREUR lors du chargement: " + e.getMessage());
            throw e;
        }
        return participants;
    }
}
