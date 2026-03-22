package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.UE;
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
public class ExcelEnseignantService {

    @Autowired
    private EnseignantService enseignantService;

    @Autowired
    private UERepository ueRepository;

    private static final String[] HEADERS = {"Nom", "Prénom", "Email", "Téléphone", "Grade", "Codes UEs (séparés par virgule)"};

    // ─── EXPORT ──────────────────────────────────────────────────────────────

    public byte[] exporterEnseignants() throws IOException {
        List<Enseignant> enseignants = enseignantService.getAllEnseignants();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Enseignants");

            // Styles
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(headerStyle);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(dataStyle);

            CellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            altStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(altStyle);

            // En-têtes
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i == 5 ? 9000 : 6000);
            }

            // Données
            int rowNum = 1;
            for (Enseignant e : enseignants) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? altStyle : dataStyle;

                createCell(row, 0, e.getNom(), style);
                createCell(row, 1, e.getPrenom(), style);
                createCell(row, 2, e.getEmail(), style);
                createCell(row, 3, e.getPhone() != null ? e.getPhone() : "", style);
                createCell(row, 4, e.getGrade() != null ? e.getGrade() : "", style);

                // Codes UEs séparés par virgule
                String codeUes = "";
                if (e.getUesEnseignees() != null && !e.getUesEnseignees().isEmpty()) {
                    codeUes = e.getUesEnseignees().stream()
                            .map(UE::getCode)
                            .filter(Objects::nonNull)
                            .sorted()
                            .reduce((a, b) -> a + "," + b)
                            .orElse("");
                }
                createCell(row, 5, codeUes, style);
                rowNum++;
            }

            // Feuille modèle (onglet 2)
            Sheet modele = wb.createSheet("Modèle Import");
            Row modeleHeader = modele.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = modeleHeader.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                modele.setColumnWidth(i, i == 5 ? 9000 : 6000);
            }
            // Ligne exemple
            Row exemple = modele.createRow(1);
            createCell(exemple, 0, "Dupont", dataStyle);
            createCell(exemple, 1, "Jean", dataStyle);
            createCell(exemple, 2, "jean.dupont@exemple.com", dataStyle);
            createCell(exemple, 3, "+237 6XX XXX XXX", dataStyle);
            createCell(exemple, 4, "Maître de conférences", dataStyle);
            createCell(exemple, 5, "INF101,MAT101", dataStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── IMPORT ──────────────────────────────────────────────────────────────

    public ImportResult importerEnseignants(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Le fichier est vide");

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Format invalide. Utilisez un fichier .xlsx ou .xls");
        }

        ImportResult result = new ImportResult();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) throw new IllegalArgumentException("Feuille introuvable dans le fichier");

            // Vérifier les en-têtes (ligne 0)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("Le fichier ne contient pas d'en-têtes");

            // Parcourir les lignes de données (à partir de la ligne 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    String nom    = getCellString(row, 0);
                    String prenom = getCellString(row, 1);
                    String email  = getCellString(row, 2);
                    String phone  = getCellString(row, 3);
                    String grade  = getCellString(row, 4);
                    String codesUe = getCellString(row, 5);

                    if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                        result.addErreur(i + 1, "Nom, prénom et email sont obligatoires");
                        continue;
                    }

                    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                        result.addErreur(i + 1, "Email invalide : " + email);
                        continue;
                    }

                    // Résoudre les codes UE en UUIDs
                    List<UUID> ueIds = new ArrayList<>();
                    List<String> codesIntrouvables = new ArrayList<>();
                    if (!codesUe.isEmpty()) {
                        for (String code : codesUe.split(",")) {
                            String trimmed = code.trim().toUpperCase();
                            if (!trimmed.isEmpty()) {
                                Optional<UE> ue = ueRepository.findByCode(trimmed);
                                if (ue.isPresent()) {
                                    ueIds.add(ue.get().getIdUE());
                                } else {
                                    codesIntrouvables.add(trimmed);
                                }
                            }
                        }
                    }

                    enseignantService.creerEnseignant(nom, prenom, email, phone, grade,
                            ueIds.isEmpty() ? null : ueIds);
                    result.incrementSucces();

                    if (!codesIntrouvables.isEmpty()) {
                        result.addAvertissement(i + 1, "UEs introuvables ignorées : " + String.join(", ", codesIntrouvables));
                    }

                } catch (IllegalArgumentException e) {
                    result.addErreur(i + 1, e.getMessage());
                } catch (Exception e) {
                    log.error("Erreur ligne {}: {}", i + 1, e.getMessage());
                    result.addErreur(i + 1, "Erreur inattendue : " + e.getMessage());
                }
            }
        }

        return result;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
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

    // ─── DTO RÉSULTAT ─────────────────────────────────────────────────────────

    public static class ImportResult {
        private int succes = 0;
        private final List<String> erreurs = new ArrayList<>();
        private final List<String> avertissements = new ArrayList<>();

        public void incrementSucces() { succes++; }
        public void addErreur(int ligne, String msg) { erreurs.add("Ligne " + ligne + " : " + msg); }
        public void addAvertissement(int ligne, String msg) { avertissements.add("Ligne " + ligne + " : " + msg); }

        public int getSucces()                      { return succes; }
        public List<String> getErreurs()            { return erreurs; }
        public List<String> getAvertissements()     { return avertissements; }
        public int getNbErreurs()                   { return erreurs.size(); }
        public boolean hasErreurs()                 { return !erreurs.isEmpty(); }
        public boolean hasAvertissements()          { return !avertissements.isEmpty(); }
    }
}
