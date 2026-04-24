package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class ExcelDisponibiliteService {

    @Autowired
    private DisponibiliteService disponibiliteService;

    // Format fixe: 12 créneaux d'1h de 8h à 20h
    private static final int[] HEURES = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    private static final String[] JOURS_COURTS = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

    /**
     * Génère un modèle Excel vide pour une semaine donnée
     */
    public byte[] genererModeleExcel(LocalDate dateDebut) throws IOException {
        LocalDate debutSemaine = dateDebut.with(DayOfWeek.MONDAY);
        return genererExcel(debutSemaine, null);
    }

    /**
     * Exporte une disponibilité existante vers Excel
     */
    public byte[] exporterDisponibilite(UUID disponibiliteId) throws IOException {
        DisponibiliteEnseignant dispo = disponibiliteService.getDisponibiliteById(disponibiliteId)
            .orElseThrow(() -> new IllegalArgumentException("Disponibilité non trouvée"));
        
        Map<LocalDate, List<PlageHoraire>> emploiDuTemps = disponibiliteService.getEmploiDuTempsSemaine(disponibiliteId);
        return genererExcel(dispo.getDateDebut(), emploiDuTemps);
    }

    /**
     * Génère le fichier Excel (modèle vide ou avec données)
     */
    private byte[] genererExcel(LocalDate debutSemaine, Map<LocalDate, List<PlageHoraire>> emploiDuTemps) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Disponibilites");

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle markedStyle = workbook.createCellStyle();
            markedStyle.setAlignment(HorizontalAlignment.CENTER);
            markedStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            markedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            markedStyle.setBorderBottom(BorderStyle.THIN);
            markedStyle.setBorderTop(BorderStyle.THIN);
            markedStyle.setBorderLeft(BorderStyle.THIN);
            markedStyle.setBorderRight(BorderStyle.THIN);

            // Ligne 0: DATE_DEBUT (pour l'import)
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("DATE_DEBUT");
            row0.createCell(1).setCellValue(debutSemaine.toString()); // Format: 2026-01-06

            // Ligne 1: vide (séparation)
            sheet.createRow(1);

            // Ligne 2: En-têtes des jours
            Row headerRow = sheet.createRow(2);
            Cell heureHeader = headerRow.createCell(0);
            heureHeader.setCellValue("Heure");
            heureHeader.setCellStyle(headerStyle);

            for (int j = 0; j < 7; j++) {
                LocalDate jour = debutSemaine.plusDays(j);
                Cell cell = headerRow.createCell(j + 1);
                cell.setCellValue(JOURS_COURTS[j] + " " + jour.format(DateTimeFormatter.ofPattern("dd/MM")));
                cell.setCellStyle(headerStyle);
            }

            // Lignes 3-14: Créneaux horaires (8h-20h)
            for (int h = 0; h < HEURES.length; h++) {
                Row row = sheet.createRow(3 + h);
                
                // Colonne 0: Heure
                Cell heureCell = row.createCell(0);
                heureCell.setCellValue(String.format("%02d:00-%02d:00", HEURES[h], HEURES[h] + 1));
                heureCell.setCellStyle(headerStyle);

                // Colonnes 1-7: Jours
                for (int j = 0; j < 7; j++) {
                    Cell cell = row.createCell(j + 1);
                    
                    // Vérifier si ce créneau est marqué
                    boolean isMarked = false;
                    if (emploiDuTemps != null) {
                        LocalDate jour = debutSemaine.plusDays(j);
                        List<PlageHoraire> plages = emploiDuTemps.get(jour);
                        if (plages != null) {
                            int heure = HEURES[h];
                            for (PlageHoraire plage : plages) {
                                int debut = plage.getHeureDebut().getHour();
                                int fin = plage.getHeureFin().getHour();
                                if (heure >= debut && heure < fin) {
                                    isMarked = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    cell.setCellValue(isMarked ? "X" : "");
                    cell.setCellStyle(isMarked ? markedStyle : cellStyle);
                }
            }

            // Ajuster largeur des colonnes
            sheet.setColumnWidth(0, 3500);
            for (int i = 1; i <= 7; i++) {
                sheet.setColumnWidth(i, 3000);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Importe les disponibilités depuis un fichier Excel
     */
    public DisponibiliteEnseignant importerExcel(MultipartFile file, Enseignant enseignant) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Ligne 0: Lire DATE_DEBUT
            Row row0 = sheet.getRow(0);
            if (row0 == null) {
                throw new IllegalArgumentException("Format invalide: ligne DATE_DEBUT manquante");
            }

            Cell labelCell = row0.getCell(0);
            Cell dateCell = row0.getCell(1);
            
            if (labelCell == null || dateCell == null) {
                throw new IllegalArgumentException("Format invalide: DATE_DEBUT non trouvée");
            }

            String label = getCellValue(labelCell);
            if (!"DATE_DEBUT".equals(label)) {
                throw new IllegalArgumentException("Format invalide: la cellule A1 doit contenir 'DATE_DEBUT'");
            }

            String dateStr = getCellValue(dateCell);
            LocalDate dateDebut;
            try {
                dateDebut = LocalDate.parse(dateStr);
            } catch (Exception e) {
                throw new IllegalArgumentException("Format de date invalide en B1. Attendu: YYYY-MM-DD (ex: 2026-01-06)");
            }

            log.info("Import Excel - Date début: {}", dateDebut);

            // Créer la disponibilité
            DisponibiliteEnseignant disponibilite = disponibiliteService.creerDisponibiliteSemaine(enseignant, dateDebut);
            LocalDate debutSemaine = disponibilite.getDateDebut();

            // Lire les créneaux (lignes 3 à 14)
            int creneauxAjoutes = 0;
            for (int h = 0; h < HEURES.length; h++) {
                Row row = sheet.getRow(3 + h);
                if (row == null) continue;

                for (int j = 0; j < 7; j++) {
                    Cell cell = row.getCell(j + 1);
                    if (cell != null && isMarked(cell)) {
                        LocalDate jour = debutSemaine.plusDays(j);
                        LocalTime heureDebut = LocalTime.of(HEURES[h], 0);
                        LocalTime heureFin = LocalTime.of(HEURES[h] + 1, 0);
                        
                        disponibiliteService.ajouterCreneau(disponibilite.getId(), jour, heureDebut, heureFin);
                        creneauxAjoutes++;
                    }
                }
            }

            log.info("Import terminé: {} créneaux ajoutés", creneauxAjoutes);
            return disponibilite;
        }
    }

    private boolean isMarked(Cell cell) {
        String value = getCellValue(cell).trim().toUpperCase();
        return "X".equals(value) || "1".equals(value) || "OUI".equals(value) || "O".equals(value);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "1" : "";
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}
