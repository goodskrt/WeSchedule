package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.UERepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelUEService {

    @Autowired private UERepository      ueRepository;
    @Autowired private ClasseRepository  classeRepository;

    // ── Export ─────────────────────────────────────────────────────────────

    public byte[] exporterUEs() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle title  = titleStyle(wb);
            CellStyle header = headerStyle(wb);
            CellStyle data   = dataStyle(wb);

            // ── Onglet 1 : Données ──────────────────────────────────────────
            Sheet sheet = wb.createSheet("UEs");
            int[] widths = {3500, 8000, 3000, 3000, 3000, 4000, 8000, 10000};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

            Row t = sheet.createRow(0);
            cell(t, 0, "LISTE DES UNITÉS D'ENSEIGNEMENT", title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row h = sheet.createRow(1);
            String[] cols = {"Code", "Intitulé", "Semestre", "Crédits", "Durée (h)", "Statut", "Classes", "Description"};
            for (int i = 0; i < cols.length; i++) cell(h, i, cols[i], header);

            int row = 2;
            for (UE ue : ueRepository.findAll()) {
                Row r = sheet.createRow(row++);
                cell(r, 0, ue.getCode() != null ? ue.getCode() : "", data);
                cell(r, 1, ue.getIntitule() != null ? ue.getIntitule() : "", data);
                cell(r, 2, ue.getSemestre() != null ? String.valueOf(ue.getSemestre()) : "", data);
                cell(r, 3, ue.getCredits() != null ? String.valueOf(ue.getCredits()) : "", data);
                cell(r, 4, ue.getDuree() != null ? String.valueOf(ue.getDuree()) : "", data);
                cell(r, 5, ue.getStatut() != null ? ue.getStatut().name() : "ACTIF", data);
                String classesStr = ue.getClasses() != null
                        ? ue.getClasses().stream().map(Classe::getNom).collect(Collectors.joining(", "))
                        : "";
                cell(r, 6, classesStr, data);
                cell(r, 7, ue.getDescription() != null ? ue.getDescription() : "", data);
            }

            // ── Onglet 2 : Modèle import ────────────────────────────────────
            Sheet tpl = wb.createSheet("Modèle import");
            for (int i = 0; i < widths.length; i++) tpl.setColumnWidth(i, widths[i]);

            Row tt = tpl.createRow(0);
            cell(tt, 0, "MODÈLE D'IMPORT — UEs", title);
            tpl.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row th = tpl.createRow(1);
            for (int i = 0; i < cols.length; i++) cell(th, i, cols[i], header);

            Row ex = tpl.createRow(2);
            cell(ex, 0, "INF101", data);
            cell(ex, 1, "Introduction à la Programmation", data);
            cell(ex, 2, "1", data);
            cell(ex, 3, "6", data);
            cell(ex, 4, "60", data);
            cell(ex, 5, "ACTIF", data);
            cell(ex, 6, "Informatique L1A, Informatique L1B", data);
            cell(ex, 7, "Description optionnelle", data);

            CellStyle note = noteStyle(wb);
            Row n1 = tpl.createRow(4);
            cell(n1, 0, "Statuts valides : ACTIF, INACTIF", note);
            tpl.addMergedRegion(new CellRangeAddress(4, 4, 0, 7));
            Row n2 = tpl.createRow(5);
            cell(n2, 0, "Classes : noms exacts séparés par des virgules (optionnel)", note);
            tpl.addMergedRegion(new CellRangeAddress(5, 5, 0, 7));

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Import ─────────────────────────────────────────────────────────────

    public ImportResult importerUEs(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Fichier vide");
        ImportResult result = new ImportResult();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) { result.addErreur("Fichier vide ou invalide"); return result; }

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                String code        = str(row, 0);
                String intitule    = str(row, 1);
                String semestreStr = str(row, 2);
                String creditsStr  = str(row, 3);
                String dureeStr    = str(row, 4);
                String statutStr   = str(row, 5);
                String classesStr  = str(row, 6);
                String description = str(row, 7);

                if (code.isBlank()) { result.addErreur("Ligne " + (i+1) + " : Code obligatoire"); continue; }
                if (intitule.isBlank()) { result.addErreur("Ligne " + (i+1) + " : Intitulé obligatoire"); continue; }

                String codeUp = code.trim().toUpperCase();
                UE ue = ueRepository.findByCode(codeUp).orElse(new UE());
                ue.setCode(codeUp);
                ue.setIntitule(intitule.trim());

                try { ue.setSemestre(Integer.parseInt(semestreStr.trim())); }
                catch (Exception e) { result.addErreur("Ligne " + (i+1) + " : Semestre invalide"); continue; }

                try { ue.setCredits(Integer.parseInt(creditsStr.trim())); }
                catch (Exception e) { result.addErreur("Ligne " + (i+1) + " : Crédits invalides"); continue; }

                if (!dureeStr.isBlank()) {
                    try { ue.setDuree(Integer.parseInt(dureeStr.trim())); }
                    catch (Exception e) { result.addAvertissement("Ligne " + (i+1) + " : Durée ignorée (invalide)"); }
                }

                try {
                    ue.setStatut(statutStr.isBlank() ? StatutUE.ACTIF : StatutUE.valueOf(statutStr.trim().toUpperCase()));
                } catch (Exception e) {
                    result.addAvertissement("Ligne " + (i+1) + " : Statut « " + statutStr + " » inconnu → ACTIF utilisé");
                    ue.setStatut(StatutUE.ACTIF);
                }

                ue.setDescription(description.isBlank() ? null : description.trim());

                // Classes
                if (!classesStr.isBlank()) {
                    Set<Classe> classes = new HashSet<>();
                    final int lineNum = i + 1;
                    for (String nomClasse : classesStr.split(",")) {
                        String nom = nomClasse.trim();
                        if (!nom.isEmpty()) {
                            classeRepository.findAll().stream()
                                    .filter(c -> c.getNom().equalsIgnoreCase(nom))
                                    .findFirst()
                                    .ifPresentOrElse(classes::add,
                                            () -> result.addAvertissement("Ligne " + lineNum + " : Classe « " + nom + " » introuvable"));
                        }
                    }
                    ue.setClasses(classes);
                }

                ueRepository.save(ue);
                result.incrementSucces();
            }
        }
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex()); f.setFontHeightInPoints((short)13);
        s.setFont(f); s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex()); f.setFontHeightInPoints((short)11);
        s.setFont(f); s.setAlignment(HorizontalAlignment.CENTER);
        borders(s); return s;
    }

    private CellStyle dataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle(); s.setWrapText(true); borders(s); return s;
    }

    private CellStyle noteStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont(); f.setItalic(true); f.setFontHeightInPoints((short)9);
        s.setFont(f); return s;
    }

    private void borders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN); s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
    }

    private void cell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col); c.setCellValue(value != null ? value : "");
        if (style != null) c.setCellStyle(style);
    }

    private String str(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING  -> c.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) c.getNumericCellValue());
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            default      -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 8; i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellType() != CellType.BLANK && !c.toString().trim().isEmpty()) return false;
        }
        return true;
    }

    // ── ImportResult ───────────────────────────────────────────────────────

    public static class ImportResult {
        private int succes = 0;
        private final List<String> erreurs        = new ArrayList<>();
        private final List<String> avertissements = new ArrayList<>();

        public void incrementSucces()           { succes++; }
        public void addErreur(String msg)       { erreurs.add(msg); }
        public void addAvertissement(String msg){ avertissements.add(msg); }
        public int getSucces()                  { return succes; }
        public List<String> getErreurs()        { return erreurs; }
        public List<String> getAvertissements() { return avertissements; }
        public boolean hasErreurs()             { return !erreurs.isEmpty(); }
        public int getNbErreurs()               { return erreurs.size(); }
    }
}
