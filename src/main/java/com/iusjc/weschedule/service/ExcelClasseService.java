package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.EcoleRepository;
import com.iusjc.weschedule.repositories.FiliereRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelClasseService {

    private final ClasseRepository classeRepository;
    private final EcoleRepository ecoleRepository;
    private final FiliereRepository filiereRepository;

    private static final String[] HEADERS = {"Nom *", "Niveau *", "Langue *", "Effectif", "École", "Filière", "Description"};
    private static final String[] NIVEAUX = {"Niveau 1", "Niveau 2", "Niveau 3", "Niveau 4", "Niveau 5"};
    private static final String[] LANGUES = {"Francophone", "Anglophone"};

    // ── Export ─────────────────────────────────────────────────────────────

    public byte[] exporterClasses() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Styles ──
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            Font hFont = wb.createFont();
            hFont.setColor(IndexedColors.WHITE.getIndex());
            hFont.setBold(true);
            headerStyle.setFont(hFont);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // ── Onglet 1 : Données ──
            XSSFSheet sheet = wb.createSheet("Classes");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            List<Classe> classes = classeRepository.findAll();
            int rowNum = 1;
            for (Classe c : classes) {
                Row row = sheet.createRow(rowNum++);
                setCell(row, 0, c.getNom(), dataStyle);
                setCell(row, 1, c.getNiveau() != null ? c.getNiveau() : "", dataStyle);
                setCell(row, 2, c.getLangue() != null ? c.getLangue() : "", dataStyle);
                setCell(row, 3, c.getEffectif() != null ? String.valueOf(c.getEffectif()) : "", dataStyle);
                setCell(row, 4, c.getEcole() != null ? c.getEcole().getNomEcole() : "", dataStyle);
                setCell(row, 5, c.getFiliere() != null ? c.getFiliere().getNomFiliere() : "", dataStyle);
                setCell(row, 6, c.getDescription() != null ? c.getDescription() : "", dataStyle);
            }

            // ── Onglet 2 : Template vide ──
            XSSFSheet tpl = wb.createSheet("Template Import");
            Row tplHeader = tpl.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = tplHeader.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                tpl.setColumnWidth(i, 5000);
            }
            // Validation dropdown Niveau
            DataValidationHelper dvHelper = tpl.getDataValidationHelper();
            DataValidationConstraint niveauConstraint = dvHelper.createExplicitListConstraint(NIVEAUX);
            CellRangeAddressList niveauRange = new CellRangeAddressList(1, 500, 1, 1);
            DataValidation niveauValidation = dvHelper.createValidation(niveauConstraint, niveauRange);
            niveauValidation.setShowErrorBox(true);
            tpl.addValidationData(niveauValidation);
            // Validation dropdown Langue
            DataValidationConstraint langueConstraint = dvHelper.createExplicitListConstraint(LANGUES);
            CellRangeAddressList langueRange = new CellRangeAddressList(1, 500, 2, 2);
            DataValidation langueValidation = dvHelper.createValidation(langueConstraint, langueRange);
            langueValidation.setShowErrorBox(true);
            tpl.addValidationData(langueValidation);

            // ── Onglet 3 : Valeurs acceptées ──
            XSSFSheet info = wb.createSheet("Valeurs acceptées");
            CellStyle infoHeader = wb.createCellStyle();
            infoHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            infoHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font infoFont = wb.createFont();
            infoFont.setBold(true);
            infoHeader.setFont(infoFont);

            Row r0 = info.createRow(0);
            r0.createCell(0).setCellValue("Niveaux acceptés");
            r0.getCell(0).setCellStyle(infoHeader);
            r0.createCell(2).setCellValue("Langues acceptées");
            r0.getCell(2).setCellStyle(infoHeader);
            int maxRows = Math.max(NIVEAUX.length, LANGUES.length);
            for (int i = 0; i < maxRows; i++) {
                Row r = info.createRow(i + 1);
                if (i < NIVEAUX.length) r.createCell(0).setCellValue(NIVEAUX[i]);
                if (i < LANGUES.length) r.createCell(2).setCellValue(LANGUES[i]);
            }
            info.setColumnWidth(0, 4000);
            info.setColumnWidth(2, 4000);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Import ─────────────────────────────────────────────────────────────

    public ImportResult importerClasses(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Fichier vide");
        String fname = file.getOriginalFilename();
        if (fname == null || (!fname.endsWith(".xlsx") && !fname.endsWith(".xls")))
            throw new IllegalArgumentException("Format invalide — utilisez un fichier .xlsx");

        ImportResult result = new ImportResult();

        try (XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                String nom     = getCellString(row, 0);
                String niveau  = getCellString(row, 1);
                String langue  = getCellString(row, 2);
                String effectifStr = getCellString(row, 3);
                String ecoleName   = getCellString(row, 4);
                String filiereName = getCellString(row, 5);
                String description = getCellString(row, 6);

                int ligneNum = i + 1;

                if (nom.isBlank()) {
                    result.addErreur("Ligne " + ligneNum + " : nom manquant");
                    continue;
                }
                if (!niveau.isBlank() && !isNiveauValide(niveau)) {
                    result.addErreur("Ligne " + ligneNum + " : niveau invalide « " + niveau + " »");
                    continue;
                }
                if (!langue.isBlank() && !isLangueValide(langue)) {
                    result.addErreur("Ligne " + ligneNum + " : langue invalide « " + langue + " »");
                    continue;
                }

                try {
                    Classe c = new Classe();
                    c.setNom(nom.trim());
                    c.setNiveau(niveau.isBlank() ? null : niveau.trim());
                    c.setLangue(langue.isBlank() ? null : langue.trim());
                    c.setDescription(description.isBlank() ? null : description.trim());

                    if (!effectifStr.isBlank()) {
                        try { c.setEffectif(Integer.parseInt(effectifStr.trim())); }
                        catch (NumberFormatException e) { /* ignore */ }
                    }
                    if (!ecoleName.isBlank()) {
                        ecoleRepository.findAll().stream()
                                .filter(e -> e.getNomEcole().equalsIgnoreCase(ecoleName.trim()))
                                .findFirst().ifPresent(c::setEcole);
                    }
                    if (!filiereName.isBlank()) {
                        filiereRepository.findAll().stream()
                                .filter(f -> f.getNomFiliere().equalsIgnoreCase(filiereName.trim()))
                                .findFirst().ifPresent(c::setFiliere);
                    }

                    classeRepository.save(c);
                    result.incrementSucces();
                } catch (Exception e) {
                    result.addErreur("Ligne " + ligneNum + " : " + e.getMessage());
                }
            }
        }
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            if (!getCellString(row, i).isBlank()) return false;
        }
        return true;
    }

    private boolean isNiveauValide(String niveau) {
        for (String n : NIVEAUX) if (n.equalsIgnoreCase(niveau)) return true;
        return false;
    }

    private boolean isLangueValide(String langue) {
        for (String l : LANGUES) if (l.equalsIgnoreCase(langue)) return true;
        return false;
    }

    // ── Result DTO ─────────────────────────────────────────────────────────

    @Data
    public static class ImportResult {
        private int succes = 0;
        private final List<String> erreurs = new ArrayList<>();

        public void incrementSucces() { succes++; }
        public void addErreur(String msg) { erreurs.add(msg); }
        public int getNbErreurs() { return erreurs.size(); }
        public boolean hasErreurs() { return !erreurs.isEmpty(); }
    }
}
