package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.TypeCours;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.CoursRepository;
import com.iusjc.weschedule.repositories.EnseignantRepository;
import com.iusjc.weschedule.repositories.UERepository;
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
public class ExcelCoursService {

    @Autowired private CoursRepository      coursRepository;
    @Autowired private ClasseRepository     classeRepository;
    @Autowired private UERepository         ueRepository;
    @Autowired private EnseignantRepository enseignantRepository;

    private static final String[] HEADERS = {
        "Intitulé", "Type (CM/TD/TP)", "Classe", "Code UE", "Enseignant (Nom Prénom)", "Durée totale (h)", "Durée restante (h)", "Description"
    };

    // ─── EXPORT ──────────────────────────────────────────────────────────────

    public byte[] exporterCours() throws IOException {
        List<Cours> cours = coursRepository.findAll();

        try (Workbook wb = new XSSFWorkbook()) {
            // Styles
            CellStyle headerStyle = wb.createCellStyle();
            Font hf = wb.createFont(); hf.setBold(true); hf.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hf);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(headerStyle);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(dataStyle);

            CellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            altStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(altStyle);

            // Onglet données
            Sheet sheet = wb.createSheet("Cours");
            Row headerRow = sheet.createRow(0);
            int[] widths = {8000, 4000, 5000, 4000, 6000, 4000, 4000, 8000};
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths[i]);
            }

            int rowNum = 1;
            for (Cours c : cours) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? altStyle : dataStyle;
                cell(row, 0, c.getIntitule(), style);
                cell(row, 1, c.getTypeCours() != null ? c.getTypeCours().name() : "", style);
                cell(row, 2, c.getClasse() != null ? c.getClasse().getNom() : "", style);
                cell(row, 3, c.getUe() != null ? c.getUe().getCode() : "", style);
                String ens = "";
                if (c.getEnseignant() != null)
                    ens = c.getEnseignant().getNom() + " " + c.getEnseignant().getPrenom();
                cell(row, 4, ens, style);
                cell(row, 5, c.getDureeTotal() != null ? String.valueOf(c.getDureeTotal()) : "", style);
                cell(row, 6, c.getDureeRestante() != null ? String.valueOf(c.getDureeRestante()) : "", style);
                cell(row, 7, c.getDescription() != null ? c.getDescription() : "", style);
                rowNum++;
            }

            // Onglet modèle
            Sheet modele = wb.createSheet("Modèle Import");
            Row mh = modele.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = mh.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                modele.setColumnWidth(i, widths[i]);
            }
            Row ex = modele.createRow(1);
            cell(ex, 0, "CM - Algorithmique", dataStyle);
            cell(ex, 1, "CM", dataStyle);
            cell(ex, 2, "Informatique L1A", dataStyle);
            cell(ex, 3, "INF101", dataStyle);
            cell(ex, 4, "Dupont Martin", dataStyle);
            cell(ex, 5, "60", dataStyle);
            cell(ex, 6, "60", dataStyle);
            cell(ex, 7, "Description optionnelle", dataStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── IMPORT ──────────────────────────────────────────────────────────────

    public ImportResult importerCours(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Le fichier est vide");
        String fn = file.getOriginalFilename();
        if (fn == null || (!fn.endsWith(".xlsx") && !fn.endsWith(".xls")))
            throw new IllegalArgumentException("Format invalide. Utilisez .xlsx ou .xls");

        ImportResult result = new ImportResult();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) throw new IllegalArgumentException("Feuille introuvable");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    final int lineNum = i + 1;
                    String intitule    = str(row, 0);
                    String typeStr     = str(row, 1).toUpperCase();
                    String classeNom   = str(row, 2);
                    String ueCode      = str(row, 3).toUpperCase();
                    String enseignant  = str(row, 4);
                    String dureeTotStr = str(row, 5);
                    String dureeRestStr= str(row, 6);
                    String description = str(row, 7);

                    if (intitule.isBlank()) {
                        result.addErreur(lineNum, "Intitulé obligatoire");
                        continue;
                    }
                    if (classeNom.isBlank()) {
                        result.addErreur(lineNum, "Classe obligatoire");
                        continue;
                    }
                    if (ueCode.isBlank()) {
                        result.addErreur(lineNum, "Code UE obligatoire");
                        continue;
                    }

                    // Type
                    TypeCours type;
                    try { type = TypeCours.valueOf(typeStr); }
                    catch (Exception e) {
                        result.addErreur(lineNum, "Type invalide « " + typeStr + " » (CM/TD/TP attendu)");
                        continue;
                    }

                    // Classe
                    List<Classe> classes = classeRepository.findAll();
                    Optional<Classe> classeOpt = classes.stream()
                            .filter(c -> c.getNom().equalsIgnoreCase(classeNom.trim()))
                            .findFirst();
                    if (classeOpt.isEmpty()) {
                        result.addErreur(lineNum, "Classe introuvable : « " + classeNom + " »");
                        continue;
                    }

                    // UE
                    Optional<UE> ueOpt = ueRepository.findByCode(ueCode.trim());
                    if (ueOpt.isEmpty()) {
                        result.addErreur(lineNum, "UE introuvable : « " + ueCode + " »");
                        continue;
                    }

                    // Durée
                    int dureeTotal;
                    try { dureeTotal = Integer.parseInt(dureeTotStr.trim()); }
                    catch (Exception e) {
                        result.addErreur(lineNum, "Durée totale invalide");
                        continue;
                    }
                    int dureeRestante = dureeTotal;
                    if (!dureeRestStr.isBlank()) {
                        try { dureeRestante = Math.min(Integer.parseInt(dureeRestStr.trim()), dureeTotal); }
                        catch (Exception ignored) {
                            result.addAvertissement(lineNum, "Durée restante invalide → durée totale utilisée");
                        }
                    }

                    Cours cours = new Cours();
                    cours.setIntitule(intitule.trim());
                    cours.setTypeCours(type);
                    cours.setClasse(classeOpt.get());
                    cours.setUe(ueOpt.get());
                    cours.setDureeTotal(dureeTotal);
                    cours.setDureeRestante(dureeRestante);
                    if (!description.isBlank()) cours.setDescription(description.trim());

                    // Enseignant (optionnel)
                    if (!enseignant.isBlank()) {
                        String[] parts = enseignant.trim().split("\\s+", 2);
                        final String nomF = parts[0];
                        final String prenomF = parts.length > 1 ? parts[1] : "";
                        enseignantRepository.findAll().stream()
                                .filter(e -> e.getNom().equalsIgnoreCase(nomF) && e.getPrenom().equalsIgnoreCase(prenomF))
                                .findFirst()
                                .ifPresentOrElse(cours::setEnseignant,
                                        () -> result.addAvertissement(lineNum, "Enseignant « " + enseignant + " » introuvable → ignoré"));
                    }

                    coursRepository.save(cours);
                    result.incrementSucces();

                } catch (Exception e) {
                    log.error("Erreur ligne {}: {}", i + 1, e.getMessage());
                    result.addErreur(i + 1, "Erreur : " + e.getMessage());
                }
            }
        }
        return result;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void setBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN); s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
    }

    private void cell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private String str(Row row, int col) {
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
            if (cell != null && cell.getCellType() != CellType.BLANK && !str(row, c).isEmpty()) return false;
        }
        return true;
    }

    // ─── DTO ─────────────────────────────────────────────────────────────────

    public static class ImportResult {
        private int succes = 0;
        private final List<String> erreurs = new ArrayList<>();
        private final List<String> avertissements = new ArrayList<>();

        public void incrementSucces()                    { succes++; }
        public void addErreur(int l, String m)           { erreurs.add("Ligne " + l + " : " + m); }
        public void addAvertissement(int l, String m)    { avertissements.add("Ligne " + l + " : " + m); }
        public int getSucces()                           { return succes; }
        public List<String> getErreurs()                 { return erreurs; }
        public List<String> getAvertissements()          { return avertissements; }
        public int getNbErreurs()                        { return erreurs.size(); }
        public boolean hasErreurs()                      { return !erreurs.isEmpty(); }
        public boolean hasAvertissements()               { return !avertissements.isEmpty(); }
    }
}
