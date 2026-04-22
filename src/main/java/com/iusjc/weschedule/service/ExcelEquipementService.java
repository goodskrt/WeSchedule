package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.CategorieEquipement;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.TypeEquipement;
import com.iusjc.weschedule.repositories.CategorieEquipementRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.TypeEquipementRepository;
import com.iusjc.weschedule.util.EquipmentStatutRules;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ExcelEquipementService {

    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private TypeEquipementRepository typeEquipementRepository;
    @Autowired private CategorieEquipementRepository categorieEquipementRepository;
    @Autowired private SalleRepository salleRepository;
    @Autowired private EquipmentAssignmentService equipmentAssignmentService;

    // ── EXPORT ────────────────────────────────────────────────────────────────

    public byte[] exporterEquipements() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle dataStyle   = createDataStyle(wb);
            CellStyle titleStyle  = createTitleStyle(wb);

            // ── Onglet 1 : Données ──────────────────────────────────────────
            Sheet sheet = wb.createSheet("Équipements");
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 6000);
            sheet.setColumnWidth(3, 6000);
            sheet.setColumnWidth(4, 7000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 10000);

            Row title = sheet.createRow(0);
            createCell(title, 0, "LISTE DES ÉQUIPEMENTS", titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row header = sheet.createRow(1);
            String[] cols = {"Nom", "Type", "Catégorie", "N° Série", "Salle assignée", "Statut", "Description"};
            for (int i = 0; i < cols.length; i++) createCell(header, i, cols[i], headerStyle);

            List<Equipment> equipements = equipmentRepository.findAll();
            int row = 2;
            for (Equipment eq : equipements) {
                Row r = sheet.createRow(row++);
                createCell(r, 0, eq.getNom(), dataStyle);
                createCell(r, 1, eq.getTypeEquipement() != null ? eq.getTypeEquipement().getNom() : "", dataStyle);
                createCell(r, 2, eq.getTypeEquipement() != null && eq.getTypeEquipement().getCategorie() != null
                        ? eq.getTypeEquipement().getCategorie().getCode() : "", dataStyle);
                createCell(r, 3, eq.getNumeroSerie() != null ? eq.getNumeroSerie() : "", dataStyle);
                createCell(r, 4, eq.getSalle() != null ? eq.getSalle().getNomSalle() : "", dataStyle);
                createCell(r, 5, eq.getStatut().name(), dataStyle);
                createCell(r, 6, eq.getDescription() != null ? eq.getDescription() : "", dataStyle);
            }

            // ── Onglet 2 : Modèle d'import ─────────────────────────────────
            Sheet tpl = wb.createSheet("Modèle import");
            tpl.setColumnWidth(0, 8000);
            tpl.setColumnWidth(1, 7000);
            tpl.setColumnWidth(2, 6000);
            tpl.setColumnWidth(3, 6000);
            tpl.setColumnWidth(4, 7000);
            tpl.setColumnWidth(5, 5000);
            tpl.setColumnWidth(6, 10000);

            Row tplTitle = tpl.createRow(0);
            createCell(tplTitle, 0, "MODÈLE D'IMPORT — ÉQUIPEMENTS", titleStyle);
            tpl.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row tplHeader = tpl.createRow(1);
            for (int i = 0; i < cols.length; i++) createCell(tplHeader, i, cols[i], headerStyle);

            // Ligne exemple
            Row ex = tpl.createRow(2);
            createCell(ex, 0, "Projecteur Epson EB-X51", dataStyle);
            createCell(ex, 1, "Vidéoprojecteur", dataStyle);
            createCell(ex, 2, "AUDIOVISUEL", dataStyle);
            createCell(ex, 3, "SN-2024-001", dataStyle);
            createCell(ex, 4, "Salle de Cours 101", dataStyle);
            createCell(ex, 5, "DISPONIBLE", dataStyle);
            createCell(ex, 6, "Projecteur HD pour présentations", dataStyle);

            // Note
            Row note = tpl.createRow(4);
            CellStyle noteStyle = wb.createCellStyle();
            noteStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            noteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font noteFont = wb.createFont();
            noteFont.setItalic(true);
            noteFont.setFontHeightInPoints((short) 9);
            noteStyle.setFont(noteFont);
            createCell(note, 0, "N° série obligatoire et unique. Statuts : DISPONIBLE, EN_SERVICE, EN_MAINTENANCE, HORS_SERVICE (une salle renseignée force EN_SERVICE).", noteStyle);
            tpl.addMergedRegion(new CellRangeAddress(4, 4, 0, 6));

            Row note2 = tpl.createRow(5);
            createCell(note2, 0, "Type : nom exact d'un type existant. Catégorie : code ou nom (ex. AUDIOVISUEL). Salle : nom exact.", noteStyle);
            tpl.addMergedRegion(new CellRangeAddress(5, 5, 0, 6));

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── IMPORT ────────────────────────────────────────────────────────────────

    public ImportResult importerEquipements(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                result.addErreur(0, "Fichier vide ou invalide");
                return result;
            }

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                String nom         = getCellString(row, 0);
                String typeNom     = getCellString(row, 1);
                String categorieStr= getCellString(row, 2);
                String numeroSerie = getCellString(row, 3);
                String salleNom    = getCellString(row, 4);
                String statutStr   = getCellString(row, 5);
                String description = getCellString(row, 6);

                if (nom.isBlank()) {
                    result.addErreur(i + 1, "Nom obligatoire");
                    continue;
                }
                if (numeroSerie == null || numeroSerie.isBlank()) {
                    result.addErreur(i + 1, "Numéro de série obligatoire");
                    continue;
                }
                String numTrim = numeroSerie.trim();
                if (equipmentRepository.existsByNumeroSerieIgnoreCase(numTrim)) {
                    result.addErreur(i + 1, "Numéro de série déjà utilisé : " + numTrim);
                    continue;
                }

                Equipment eq = new Equipment();
                eq.setNom(nom.trim());
                eq.setNumeroSerie(numTrim);
                eq.setDescription(description.isBlank() ? null : description.trim());

                // Statut demandé (réévalué après affectation salle : salle → EN_SERVICE)
                StatutEquipement statutSouhaite;
                try {
                    statutSouhaite = statutStr.isBlank() ? StatutEquipement.DISPONIBLE
                            : StatutEquipement.valueOf(statutStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    result.addAvertissement(i + 1, "Statut « " + statutStr + " » inconnu → DISPONIBLE utilisé");
                    statutSouhaite = StatutEquipement.DISPONIBLE;
                }
                eq.setStatut(statutSouhaite);

                // Type
                if (!typeNom.isBlank()) {
                    TypeEquipement type = typeEquipementRepository.findByNomIgnoreCase(typeNom.trim()).orElse(null);
                    if (type != null) {
                        eq.setTypeEquipement(type);
                    } else {
                        // Créer le type si catégorie fournie
                        if (!categorieStr.isBlank()) {
                            Optional<CategorieEquipement> catOpt = resolveCategorie(categorieStr.trim());
                            if (catOpt.isPresent()) {
                                TypeEquipement newType = new TypeEquipement();
                                newType.setNom(typeNom.trim());
                                newType.setCategorie(catOpt.get());
                                eq.setTypeEquipement(typeEquipementRepository.save(newType));
                                result.addAvertissement(i + 1, "Type « " + typeNom + " » créé automatiquement");
                            } else {
                                result.addAvertissement(i + 1, "Type « " + typeNom + " » introuvable et catégorie « " + categorieStr + " » inconnue → ignoré");
                            }
                        } else {
                            result.addAvertissement(i + 1, "Type « " + typeNom + " » introuvable → ignoré");
                        }
                    }
                }

                // Salle
                if (!salleNom.isBlank()) {
                    Salle salle = salleRepository.findByNomSalleIgnoreCase(salleNom.trim()).orElse(null);
                    if (salle != null) {
                        eq.setSalle(salle);
                    } else {
                        result.addAvertissement(i + 1, "Salle « " + salleNom + " » introuvable → non assigné");
                    }
                }

                EquipmentStatutRules.syncStatutAvecSalle(eq, statutSouhaite);

                equipmentRepository.save(eq);
                if (eq.getSalle() != null) {
                    Utilisateur resp = equipmentAssignmentService.findFallbackAdminResponsable();
                    if (resp != null) {
                        equipmentAssignmentService.createInitialPermanentRoomAssignment(
                                eq, eq.getSalle(), resp, "import-excel");
                    }
                }
                result.incrementSucces();
            }
        }
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Résout une catégorie par code ou nom (insensible à la casse). */
    private Optional<CategorieEquipement> resolveCategorie(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String s = raw.trim();
        return categorieEquipementRepository.findByCodeIgnoreCase(s)
                .or(() -> categorieEquipementRepository.findByNomIgnoreCase(s));
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        setBorders(s);
        s.setWrapText(true);
        return s;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        f.setFontHeightInPoints((short) 13);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
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
        if (style != null) cell.setCellStyle(style);
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
        for (int i = 0; i < 7; i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellType() != CellType.BLANK
                    && !c.toString().trim().isEmpty()) return false;
        }
        return true;
    }

    // ── ImportResult ──────────────────────────────────────────────────────────

    public static class ImportResult {
        private int succes = 0;
        private final List<String> erreurs        = new ArrayList<>();
        private final List<String> avertissements = new ArrayList<>();

        public void incrementSucces()                          { succes++; }
        public void addErreur(int l, String msg)               { erreurs.add("Ligne " + l + " : " + msg); }
        public void addAvertissement(int l, String msg)        { avertissements.add("Ligne " + l + " : " + msg); }
        public int getSucces()                                 { return succes; }
        public List<String> getErreurs()                       { return erreurs; }
        public List<String> getAvertissements()                { return avertissements; }
        public boolean hasErreurs()                            { return !erreurs.isEmpty(); }
        public boolean hasAvertissements()                     { return !avertissements.isEmpty(); }
        public int getNbErreurs()                              { return erreurs.size(); }
    }
}
