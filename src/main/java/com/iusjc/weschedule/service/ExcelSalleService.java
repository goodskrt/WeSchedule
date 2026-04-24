package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutSalle;
import com.iusjc.weschedule.enums.TypeSalle;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.repositories.SalleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ExcelSalleService {

    @Autowired
    private SalleRepository salleRepository;

    private static final String[] HEADERS = {
        "Nom de la salle", "Type", "Capacité", "Étage", "Bâtiment", "Statut"
    };
    private static final String[] TYPES_VALIDES = {
        "SALLE_DE_COURS", "SALLE_DE_TD", "SALLE_INFORMATIQUE", "LABORATOIRE"
    };
    private static final String[] ETAGES_VALIDES = {
        "RDC", "1er étage", "2ème étage", "3ème étage"
    };
    private static final String[] BATIMENTS_VALIDES = {
        "Nouveau bâtiment", "Ancien bâtiment"
    };
    private static final String[] STATUTS_VALIDES = {
        "DISPONIBLE", "OCCUPE", "EN_MAINTENANCE"
    };

    // ─── EXPORT ──────────────────────────────────────────────────────────────

    public byte[] exporterSalles() throws IOException {
        List<Salle> salles = salleRepository.findAll();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Salles");

            CellStyle headerStyle = buildHeaderStyle(wb);
            CellStyle dataStyle   = buildDataStyle(wb, false);
            CellStyle altStyle    = buildDataStyle(wb, true);

            // En-têtes
            Row headerRow = sheet.createRow(0);
            int[] widths = {8000, 6000, 4000, 5000, 6000, 5000};
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths[i]);
            }

            // Données
            int rowNum = 1;
            for (Salle s : salles) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? altStyle : dataStyle;
                createCell(row, 0, s.getNomSalle(), style);
                createCell(row, 1, s.getTypeSalle() != null ? s.getTypeSalle().name() : "", style);
                createCell(row, 2, s.getCapacite() != null ? String.valueOf(s.getCapacite()) : "", style);
                createCell(row, 3, s.getEtage() != null ? s.getEtage() : "", style);
                createCell(row, 4, s.getBatiment() != null ? s.getBatiment() : "", style);
                createCell(row, 5, s.getStatut() != null ? s.getStatut().name() : "DISPONIBLE", style);
                rowNum++;
            }

            // Onglet modèle
            Sheet modele = wb.createSheet("Modèle Import");
            Row mHeader = modele.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = mHeader.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                modele.setColumnWidth(i, widths[i]);
            }
            Row exemple = modele.createRow(1);
            createCell(exemple, 0, "Salle A101", dataStyle);
            createCell(exemple, 1, "SALLE_DE_COURS", dataStyle);
            createCell(exemple, 2, "40", dataStyle);
            createCell(exemple, 3, "1er étage", dataStyle);
            createCell(exemple, 4, "Nouveau bâtiment", dataStyle);
            createCell(exemple, 5, "DISPONIBLE", dataStyle);

            // Onglet référence
            Sheet ref = wb.createSheet("Valeurs acceptées");
            addRefColumn(ref, wb, headerStyle, dataStyle, 0, "Types valides", TYPES_VALIDES);
            addRefColumn(ref, wb, headerStyle, dataStyle, 1, "Étages valides", ETAGES_VALIDES);
            addRefColumn(ref, wb, headerStyle, dataStyle, 2, "Bâtiments valides", BATIMENTS_VALIDES);
            addRefColumn(ref, wb, headerStyle, dataStyle, 3, "Statuts valides", STATUTS_VALIDES);
            ref.setColumnWidth(0, 7000);
            ref.setColumnWidth(1, 5000);
            ref.setColumnWidth(2, 6000);
            ref.setColumnWidth(3, 5000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── IMPORT ──────────────────────────────────────────────────────────────

    public ImportResult importerSalles(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Le fichier est vide");
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Format invalide. Utilisez .xlsx ou .xls");
        }

        ImportResult result = new ImportResult();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) throw new IllegalArgumentException("Feuille introuvable");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    String nom      = getCellString(row, 0);
                    String type     = getCellString(row, 1).toUpperCase().replace(" ", "_");
                    String capStr   = getCellString(row, 2);
                    String etage    = getCellString(row, 3);
                    String batiment = getCellString(row, 4);
                    String statutStr = getCellString(row, 5).toUpperCase().replace(" ", "_");

                    if (nom.isEmpty()) {
                        result.addErreur(i + 1, "Nom de la salle obligatoire");
                        continue;
                    }
                    if (type.isEmpty()) {
                        result.addErreur(i + 1, "Type obligatoire");
                        continue;
                    }

                    TypeSalle typeSalle;
                    try {
                        typeSalle = TypeSalle.valueOf(type);
                    } catch (IllegalArgumentException e) {
                        result.addErreur(i + 1, "Type invalide : " + type + ". Valeurs acceptées : " + String.join(", ", TYPES_VALIDES));
                        continue;
                    }

                    int capacite = 0;
                    try {
                        capacite = Integer.parseInt(capStr.trim());
                        if (capacite <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        result.addErreur(i + 1, "Capacité invalide : doit être un entier > 0");
                        continue;
                    }

                    // Vérifier doublon
                    if (salleRepository.findByNomSalle(nom).isPresent()) {
                        result.addErreur(i + 1, "Une salle avec le nom \"" + nom + "\" existe déjà");
                        continue;
                    }

                    Salle salle = new Salle();
                    salle.setNomSalle(nom);
                    salle.setTypeSalle(typeSalle);
                    salle.setCapacite(capacite);
                    salle.setEtage(etage.isEmpty() ? null : etage);
                    salle.setBatiment(batiment.isEmpty() ? null : batiment);
                    // Statut
                    if (!statutStr.isEmpty()) {
                        try {
                            salle.setStatut(StatutSalle.valueOf(statutStr));
                        } catch (IllegalArgumentException e) {
                            salle.setStatut(StatutSalle.DISPONIBLE);
                        }
                    }
                    salleRepository.save(salle);
                    result.incrementSucces();

                } catch (Exception e) {
                    log.error("Erreur ligne {}: {}", i + 1, e.getMessage());
                    result.addErreur(i + 1, "Erreur inattendue : " + e.getMessage());
                }
            }
        }
        return result;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private CellStyle buildHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private CellStyle buildDataStyle(Workbook wb, boolean alt) {
        CellStyle s = wb.createCellStyle();
        if (alt) {
            s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setAlignment(HorizontalAlignment.LEFT);
        setBorders(s);
        return s;
    }

    private void addRefColumn(Sheet sheet, Workbook wb, CellStyle hStyle, CellStyle dStyle,
                               int col, String title, String[] values) {
        Row h = sheet.getRow(0);
        if (h == null) h = sheet.createRow(0);
        Cell hCell = h.createCell(col);
        hCell.setCellValue(title);
        hCell.setCellStyle(hStyle);
        for (int i = 0; i < values.length; i++) {
            Row r = sheet.getRow(i + 1);
            if (r == null) r = sheet.createRow(i + 1);
            createCell(r, col, values[i], dStyle);
        }
    }

    private void setBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue()).trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellString(row, c).isEmpty()) return false;
        }
        return true;
    }

    // ─── DTO ─────────────────────────────────────────────────────────────────

    public static class ImportResult {
        private int succes = 0;
        private final List<String> erreurs = new ArrayList<>();

        public void incrementSucces()              { succes++; }
        public void addErreur(int l, String msg)   { erreurs.add("Ligne " + l + " : " + msg); }

        public int getSucces()           { return succes; }
        public List<String> getErreurs() { return erreurs; }
        public int getNbErreurs()        { return erreurs.size(); }
        public boolean hasErreurs()      { return !erreurs.isEmpty(); }
    }
}
