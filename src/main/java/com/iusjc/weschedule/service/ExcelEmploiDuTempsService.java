package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.EmploiDuTempsClasse;
import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.repositories.EmploiDuTempsClasseRepository;
import com.iusjc.weschedule.repositories.SeanceClasseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelEmploiDuTempsService {

    @Autowired
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;

    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private static final LocalTime[] HEURES = {
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0),
            LocalTime.of(16, 0), LocalTime.of(17, 0)
    };

    public byte[] exporterEmploiDuTemps(UUID emploiDuTempsId) throws Exception {
        EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));

        List<SeanceClasse> seances = seanceRepository.findByEmploiDuTempsOrderByDateAscHeureDebutAsc(emploiDuTemps);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Emploi du Temps");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle timeStyle = createTimeStyle(workbook);
            CellStyle cmStyle = createSeanceStyle(workbook, IndexedColors.LIGHT_BLUE);
            CellStyle tdStyle = createSeanceStyle(workbook, IndexedColors.LIGHT_YELLOW);
            CellStyle tpStyle = createSeanceStyle(workbook, IndexedColors.LIGHT_GREEN);

            // Titre
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Emploi du Temps - " + emploiDuTemps.getClasse().getNom());
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Sous-titre
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Semaine " + emploiDuTemps.getSemaine() + " - " + emploiDuTemps.getAnnee() +
                    " (du " + emploiDuTemps.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " au " + emploiDuTemps.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            // En-têtes
            Row headerRow = sheet.createRow(3);
            Cell cornerCell = headerRow.createCell(0);
            cornerCell.setCellValue("Heure");
            cornerCell.setCellStyle(headerStyle);

            for (int i = 0; i < JOURS.length; i++) {
                Cell cell = headerRow.createCell(i + 1);
                LocalDate date = emploiDuTemps.getDateDebut().plusDays(i);
                cell.setCellValue(JOURS[i] + "\n" + date.format(DateTimeFormatter.ofPattern("dd/MM")));
                cell.setCellStyle(headerStyle);
            }

            // Grille horaire
            Map<String, SeanceClasse> seancesMap = new HashMap<>();
            for (SeanceClasse seance : seances) {
                String key = seance.getDate() + "|" + seance.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm"));
                seancesMap.put(key, seance);
            }

            int rowNum = 4;
            for (LocalTime heure : HEURES) {
                Row row = sheet.createRow(rowNum++);
                Cell timeCell = row.createCell(0);
                timeCell.setCellValue(heure.format(DateTimeFormatter.ofPattern("HH:mm")));
                timeCell.setCellStyle(timeStyle);

                for (int i = 0; i < 6; i++) {
                    LocalDate date = emploiDuTemps.getDateDebut().plusDays(i);
                    String key = date + "|" + heure.format(DateTimeFormatter.ofPattern("HH:mm"));
                    Cell cell = row.createCell(i + 1);

                    SeanceClasse seance = seancesMap.get(key);
                    if (seance != null) {
                        String content = seance.getCours().getIntitule() + "\n";
                        if (seance.getEnseignant() != null) {
                            content += seance.getEnseignant().getPrenom() + " " + seance.getEnseignant().getNom() + "\n";
                        }
                        if (seance.getSalle() != null) {
                            content += seance.getSalle().getNomSalle();
                        }
                        cell.setCellValue(content);

                        // Style selon le type
                        if (seance.getCours().getTypeCours() != null) {
                            switch (seance.getCours().getTypeCours()) {
                                case CM -> cell.setCellStyle(cmStyle);
                                case TD -> cell.setCellStyle(tdStyle);
                                case TP -> cell.setCellStyle(tpStyle);
                            }
                        }
                    }
                }
            }

            // Ajuster les largeurs
            sheet.setColumnWidth(0, 3000);
            for (int i = 1; i <= 6; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSeanceStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        return style;
    }
}
