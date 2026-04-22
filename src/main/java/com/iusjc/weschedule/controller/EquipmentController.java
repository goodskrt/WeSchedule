package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.CategorieEquipement;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.EquipmentAssignment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.TypeEquipement;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.CategorieEquipementRepository;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.EquipmentAssignmentRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.TypeEquipementRepository;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import com.iusjc.weschedule.service.EquipmentAssignmentService;
import com.iusjc.weschedule.service.ExcelEquipementService;
import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.util.AdminStatsFactory;
import com.iusjc.weschedule.util.EquipmentStatutRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/equipements")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Slf4j
public class EquipmentController {

    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private EquipmentAssignmentRepository equipmentAssignmentRepository;
    @Autowired private TypeEquipementRepository typeEquipementRepository;
    @Autowired private CategorieEquipementRepository categorieEquipementRepository;
    @Autowired private SalleRepository salleRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private EquipmentAssignmentService equipmentAssignmentService;
    @Autowired private ExcelEquipementService excelEquipementService;

    public static final String ASSIGNMENT_TYPE_ROOM = "room";
    public static final String ASSIGNMENT_TYPE_CLASS = "class";

    // ── Liste ──────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public String liste(Model model) {
        List<Equipment> equipements = equipmentRepository.findAll().stream()
                .sorted(Comparator.comparing((Equipment e) -> e.getNom() != null ? e.getNom() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();
        model.addAttribute("equipements", equipements);
        model.addAttribute("categories", categorieEquipementRepository.findAllByOrderByOrdreAscNomAsc());
        model.addAttribute("statuts", StatutEquipement.values());
        model.addAttribute("pageStats", AdminStatsFactory.equipements(equipmentRepository, typeEquipementRepository, categorieEquipementRepository));
        return "admin/equipements";
    }

    // ── API JSON liste ─────────────────────────────────────────────────────

    @GetMapping("/api")
    @ResponseBody
    public List<Map<String, Object>> listeApi() {
        return equipmentRepository.findAll().stream().map(eq -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", eq.getId());
            m.put("nom", eq.getNom());
            m.put("numeroSerie", eq.getNumeroSerie());
            m.put("description", eq.getDescription());
            m.put("statut", eq.getStatut() != null ? eq.getStatut().name() : null);
            if (eq.getTypeEquipement() != null) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", eq.getTypeEquipement().getId());
                t.put("nom", eq.getTypeEquipement().getNom());
                if (eq.getTypeEquipement().getCategorie() != null) {
                    Map<String, Object> cat = new HashMap<>();
                    cat.put("id", eq.getTypeEquipement().getCategorie().getId());
                    cat.put("nom", eq.getTypeEquipement().getCategorie().getNom());
                    cat.put("code", eq.getTypeEquipement().getCategorie().getCode());
                    t.put("categorie", cat);
                } else {
                    t.put("categorie", null);
                }
                m.put("typeEquipement", t);
            } else {
                m.put("typeEquipement", null);
            }
            if (eq.getSalle() != null) {
                Map<String, Object> s = new HashMap<>();
                s.put("idSalle", eq.getSalle().getIdSalle());
                s.put("nomSalle", eq.getSalle().getNomSalle());
                m.put("salle", s);
            } else {
                m.put("salle", null);
            }
            return m;
        }).toList();
    }

    // ── Détails ────────────────────────────────────────────────────────────

    @GetMapping("/details/{id}")
    @Transactional(readOnly = true)
    public String details(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        return equipmentRepository.findById(id).map(eq -> {
            model.addAttribute("equipement", eq);
            List<EquipmentAssignment> raw = equipmentAssignmentRepository.findByEquipmentOrderByStartAtDesc(eq);
            model.addAttribute("nbAffectations", raw.size());
            List<Map<String, Object>> affectationRows = new ArrayList<>();
            for (EquipmentAssignment a : raw) {
                Map<String, Object> row = new HashMap<>();
                row.put("a", a);
                row.put("cible", libelleCibleAffectation(a));
                affectationRows.add(row);
            }
            model.addAttribute("affectationRows", affectationRows);
            model.addAttribute("salles", salleRepository.findAll());
            model.addAttribute("classes", classeRepository.findAll());
            model.addAttribute("responsables", utilisateurRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMINISTRATEUR || u.getRole() == Role.ENSEIGNANT)
                    .sorted(Comparator.comparing(Utilisateur::getNom, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(Utilisateur::getPrenom, String.CASE_INSENSITIVE_ORDER))
                    .toList());
            return "admin/equipement-details";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Équipement introuvable");
            return "redirect:/admin/equipements";
        });
    }

    // ── Formulaire création ────────────────────────────────────────────────

    @GetMapping("/nouveau")
    @Transactional(readOnly = true)
    public String nouveauForm(Model model) {
        model.addAttribute("equipement", new Equipment());
        model.addAttribute("types", typeEquipementRepository.findAll());
        model.addAttribute("salles", salleRepository.findAll());
        model.addAttribute("statuts", StatutEquipement.values());
        model.addAttribute("mode", "creation");
        return "admin/equipement-form";
    }

    @PostMapping("/creer")
    @Transactional
    public String creer(
            @RequestParam String nom,
            @RequestParam(required = false) String typeEquipementId,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String photo,
            @RequestParam(defaultValue = "DISPONIBLE") String statut,
            @RequestParam(required = false) String salleId,
            RedirectAttributes ra) {
        try {
            if (nom == null || nom.isBlank()) {
                ra.addFlashAttribute("error", "Le nom est obligatoire");
                return "redirect:/admin/equipements/nouveau";
            }
            String num = numeroSerie != null ? numeroSerie.trim() : "";
            if (num.isEmpty()) {
                ra.addFlashAttribute("error", "Le numéro de série est obligatoire");
                return "redirect:/admin/equipements/nouveau";
            }
            if (equipmentRepository.existsByNumeroSerieIgnoreCase(num)) {
                ra.addFlashAttribute("error", "Un équipement avec ce numéro de série existe déjà");
                return "redirect:/admin/equipements/nouveau";
            }
            StatutEquipement statutSouhaite = StatutEquipement.valueOf(statut);
            Equipment eq = new Equipment();
            eq.setNom(nom.trim());
            eq.setNumeroSerie(num);
            if (description != null && !description.isBlank()) eq.setDescription(description.trim());
            if (photo != null && !photo.isBlank()) eq.setPhoto(photo.trim());
            if (typeEquipementId != null && !typeEquipementId.isBlank())
                typeEquipementRepository.findById(UUID.fromString(typeEquipementId)).ifPresent(eq::setTypeEquipement);
            if (salleId != null && !salleId.isBlank())
                salleRepository.findById(UUID.fromString(salleId)).ifPresent(eq::setSalle);
            EquipmentStatutRules.syncStatutAvecSalle(eq, statutSouhaite);
            equipmentRepository.save(eq);
            if (eq.getSalle() != null) {
                Utilisateur resp = resolveResponsableOrFallback();
                if (resp != null) {
                    equipmentAssignmentService.createInitialPermanentRoomAssignment(
                            eq, eq.getSalle(), resp, utilisateurCourant());
                }
            }
            ra.addFlashAttribute("success", "Équipement « " + eq.getNom() + " » créé avec succès");
            return "redirect:/admin/equipements";
        } catch (Exception e) {
            log.error("Erreur création équipement", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/equipements/nouveau";
        }
    }

    // ── Formulaire modification ────────────────────────────────────────────

    @GetMapping("/modifier/{id}")
    @Transactional(readOnly = true)
    public String modifierForm(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        return equipmentRepository.findById(id).map(eq -> {
            model.addAttribute("equipement", eq);
            model.addAttribute("types", typeEquipementRepository.findAll());
            model.addAttribute("salles", salleRepository.findAll());
            model.addAttribute("statuts", StatutEquipement.values());
            model.addAttribute("mode", "modification");
            return "admin/equipement-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Équipement introuvable");
            return "redirect:/admin/equipements";
        });
    }

    @PostMapping("/modifier/{id}")
    public String modifier(
            @PathVariable @NonNull UUID id,
            @RequestParam String nom,
            @RequestParam(required = false) String typeEquipementId,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String photo,
            @RequestParam(defaultValue = "DISPONIBLE") String statut,
            @RequestParam(required = false) String salleId,
            RedirectAttributes ra) {
        try {
            Equipment eq = equipmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Équipement introuvable"));
            String num = numeroSerie != null ? numeroSerie.trim() : "";
            if (num.isEmpty()) {
                ra.addFlashAttribute("error", "Le numéro de série est obligatoire");
                return "redirect:/admin/equipements/modifier/" + id;
            }
            if (equipmentRepository.existsByNumeroSerieIgnoreCaseAndIdNot(num, id)) {
                ra.addFlashAttribute("error", "Un autre équipement utilise déjà ce numéro de série");
                return "redirect:/admin/equipements/modifier/" + id;
            }
            StatutEquipement statutSouhaite = StatutEquipement.valueOf(statut);
            eq.setNom(nom.trim());
            eq.setNumeroSerie(num);
            eq.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            eq.setPhoto(photo != null && !photo.isBlank() ? photo.trim() : null);
            eq.setTypeEquipement(null);
            eq.setSalle(null);
            if (typeEquipementId != null && !typeEquipementId.isBlank())
                typeEquipementRepository.findById(UUID.fromString(typeEquipementId)).ifPresent(eq::setTypeEquipement);
            if (salleId != null && !salleId.isBlank())
                salleRepository.findById(UUID.fromString(salleId)).ifPresent(eq::setSalle);
            EquipmentStatutRules.syncStatutAvecSalle(eq, statutSouhaite);
            equipmentRepository.save(eq);
            ra.addFlashAttribute("success", "Équipement modifié avec succès");
            return "redirect:/admin/equipements/details/" + id;
        } catch (Exception e) {
            log.error("Erreur modification équipement", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/equipements/modifier/" + id;
        }
    }

    @PostMapping("/details/{equipmentId}/affectations/creer")
    @Transactional
    public String creerAffectation(
            @PathVariable @NonNull UUID equipmentId,
            /** Valeur du select : "room:uuid" ou "class:uuid" */
            @RequestParam String cible,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam String startAt,
            @RequestParam(required = false) String endAt,
            @RequestParam String duration,
            @RequestParam String responsableId,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes ra) {
        try {
            Equipment eq = equipmentRepository.findById(equipmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Équipement introuvable"));
            String raw = cible != null ? cible.trim() : "";
            int sep = raw.indexOf(':');
            if (sep < 1 || sep >= raw.length() - 1) {
                ra.addFlashAttribute("error", "Cible d'affectation invalide");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            String type = raw.substring(0, sep).trim().toLowerCase();
            UUID tid = UUID.fromString(raw.substring(sep + 1).trim());
            if (!ASSIGNMENT_TYPE_ROOM.equals(type) && !ASSIGNMENT_TYPE_CLASS.equals(type)) {
                ra.addFlashAttribute("error", "Type d'affectation invalide (salle ou classe)");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            if (ASSIGNMENT_TYPE_ROOM.equals(type) && salleRepository.findById(tid).isEmpty()) {
                ra.addFlashAttribute("error", "Salle introuvable");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            if (ASSIGNMENT_TYPE_CLASS.equals(type) && classeRepository.findById(tid).isEmpty()) {
                ra.addFlashAttribute("error", "Classe introuvable");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            Utilisateur responsable = utilisateurRepository.findById(UUID.fromString(responsableId.trim()))
                    .orElseThrow(() -> new IllegalArgumentException("Responsable introuvable"));

            LocalDateTime debut = parseDateTimeInput(startAt);
            String dur = duration != null ? duration.trim().toLowerCase() : "";
            if (!"permanent".equals(dur) && !"temporary".equals(dur)) {
                ra.addFlashAttribute("error", "Durée invalide (permanent ou temporary)");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            LocalDateTime fin = null;
            if ("temporary".equals(dur)) {
                if (endAt == null || endAt.isBlank()) {
                    ra.addFlashAttribute("error", "Pour une affectation temporaire, la date et heure de fin sont obligatoires");
                    return "redirect:/admin/equipements/details/" + equipmentId;
                }
                fin = parseDateTimeInput(endAt);
                if (!fin.isAfter(debut)) {
                    ra.addFlashAttribute("error", "La fin doit être strictement après le début");
                    return "redirect:/admin/equipements/details/" + equipmentId;
                }
            }
            if (quantity < 1) quantity = 1;
            String raison = reason != null ? reason.trim() : "";
            if (raison.isEmpty()) {
                ra.addFlashAttribute("error", "Le motif de l'affectation est obligatoire");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }

            equipmentAssignmentService.assertNoOverlap(eq, debut, fin, null);

            EquipmentAssignment a = new EquipmentAssignment();
            a.setEquipment(eq);
            a.setAssignmentType(type);
            a.setTargetId(tid);
            a.setQuantity(quantity);
            a.setStartAt(debut);
            a.setEndAt(fin);
            a.setDuration(dur);
            a.setReason(raison);
            a.setStatus(EquipmentAssignmentService.STATUS_ACTIVE);
            a.setAssignedBy(utilisateurCourant());
            a.setNotes(notes != null && !notes.isBlank() ? notes.trim() : null);
            a.setResponsable(responsable);
            equipmentAssignmentRepository.save(a);

            if (ASSIGNMENT_TYPE_ROOM.equals(type)) {
                equipmentAssignmentService.syncEquipmentSalleFromRoomAssignment(eq, tid);
            }

            ra.addFlashAttribute("success", "Affectation enregistrée");
        } catch (Exception e) {
            log.error("Erreur création affectation équipement", e);
            ra.addFlashAttribute("error", "Affectation : " + e.getMessage());
        }
        return "redirect:/admin/equipements/details/" + equipmentId;
    }

    @PostMapping("/details/{equipmentId}/affectations/modifier/{assignmentId}")
    @Transactional
    public String modifierAffectation(
            @PathVariable @NonNull UUID equipmentId,
            @PathVariable @NonNull UUID assignmentId,
            @RequestParam String cible,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam String startAt,
            @RequestParam(required = false) String endAt,
            @RequestParam String duration,
            @RequestParam String responsableId,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes ra) {
        try {
            equipmentRepository.findById(equipmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Équipement introuvable"));
            String raw = cible != null ? cible.trim() : "";
            int sep = raw.indexOf(':');
            if (sep < 1 || sep >= raw.length() - 1) {
                ra.addFlashAttribute("error", "Cible d'affectation invalide");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            String type = raw.substring(0, sep).trim().toLowerCase();
            UUID tid = UUID.fromString(raw.substring(sep + 1).trim());
            if (!ASSIGNMENT_TYPE_ROOM.equals(type) && !ASSIGNMENT_TYPE_CLASS.equals(type)) {
                ra.addFlashAttribute("error", "Type d'affectation invalide (salle ou classe)");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            if (ASSIGNMENT_TYPE_ROOM.equals(type) && salleRepository.findById(tid).isEmpty()) {
                ra.addFlashAttribute("error", "Salle introuvable");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            if (ASSIGNMENT_TYPE_CLASS.equals(type) && classeRepository.findById(tid).isEmpty()) {
                ra.addFlashAttribute("error", "Classe introuvable");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            Utilisateur responsable = utilisateurRepository.findById(UUID.fromString(responsableId.trim()))
                    .orElseThrow(() -> new IllegalArgumentException("Responsable introuvable"));

            LocalDateTime debut = parseDateTimeInput(startAt);
            String dur = duration != null ? duration.trim().toLowerCase() : "";
            if (!"permanent".equals(dur) && !"temporary".equals(dur)) {
                ra.addFlashAttribute("error", "Durée invalide (permanent ou temporary)");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }
            LocalDateTime fin = null;
            if ("temporary".equals(dur)) {
                if (endAt == null || endAt.isBlank()) {
                    ra.addFlashAttribute("error", "Pour une affectation temporaire, la date et heure de fin sont obligatoires");
                    return "redirect:/admin/equipements/details/" + equipmentId;
                }
                fin = parseDateTimeInput(endAt);
                if (!fin.isAfter(debut)) {
                    ra.addFlashAttribute("error", "La fin doit être strictement après le début");
                    return "redirect:/admin/equipements/details/" + equipmentId;
                }
            }
            if (quantity < 1) {
                quantity = 1;
            }
            String raison = reason != null ? reason.trim() : "";
            if (raison.isEmpty()) {
                ra.addFlashAttribute("error", "Le motif de l'affectation est obligatoire");
                return "redirect:/admin/equipements/details/" + equipmentId;
            }

            equipmentAssignmentService.mettreAJourAffectation(
                    assignmentId,
                    equipmentId,
                    type,
                    tid,
                    quantity,
                    debut,
                    fin,
                    dur,
                    raison,
                    notes != null && !notes.isBlank() ? notes.trim() : null,
                    responsable);

            ra.addFlashAttribute("success", "Affectation mise à jour");
        } catch (Exception e) {
            log.error("Erreur modification affectation équipement", e);
            ra.addFlashAttribute("error", "Affectation : " + e.getMessage());
        }
        return "redirect:/admin/equipements/details/" + equipmentId;
    }

    @PostMapping("/affectations/supprimer/{assignmentId}")
    public String supprimerAffectation(
            @PathVariable @NonNull UUID assignmentId,
            @RequestParam @NonNull UUID equipmentId,
            RedirectAttributes ra) {
        try {
            equipmentAssignmentService.supprimerAffectation(assignmentId, equipmentId);
            ra.addFlashAttribute("success", "Affectation supprimée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer l'affectation : " + e.getMessage());
        }
        return "redirect:/admin/equipements/details/" + equipmentId;
    }

    private String libelleCibleAffectation(EquipmentAssignment a) {
        if (a == null || a.getAssignmentType() == null) return "—";
        if (ASSIGNMENT_TYPE_ROOM.equalsIgnoreCase(a.getAssignmentType())) {
            return salleRepository.findById(a.getTargetId()).map(Salle::getNomSalle).orElse("(salle supprimée)");
        }
        if (ASSIGNMENT_TYPE_CLASS.equalsIgnoreCase(a.getAssignmentType())) {
            return classeRepository.findById(a.getTargetId()).map(Classe::getNom).orElse("(classe supprimée)");
        }
        return a.getTargetId().toString();
    }

    private static String utilisateurCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "système";
        }
        return auth.getName();
    }

    private Utilisateur resolveResponsableOrFallback() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && a.getName() != null && !"anonymousUser".equals(a.getPrincipal())) {
            return utilisateurRepository.findByEmail(a.getName()).orElse(null);
        }
        return equipmentAssignmentService.findFallbackAdminResponsable();
    }

    private static LocalDateTime parseDateTimeInput(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Date/heure obligatoire");
        }
        String t = raw.trim();
        if (t.length() == 16) {
            t = t + ":00";
        }
        return LocalDateTime.parse(t);
    }

    // ── Suppression ────────────────────────────────────────────────────────

    @PostMapping("/supprimer/{id}")
    public String supprimer(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            Equipment eq = equipmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Équipement introuvable"));
            equipmentRepository.delete(eq);
            ra.addFlashAttribute("success", "Équipement supprimé");
        } catch (Exception e) {
            log.error("Erreur suppression équipement", e);
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/equipements";
    }

    // ── Export Excel ───────────────────────────────────────────────────────

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        try {
            byte[] data = excelEquipementService.exporterEquipements();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"equipements.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export équipements", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Import Excel ───────────────────────────────────────────────────────

    @PostMapping("/import")
    @ResponseBody
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> resp = new HashMap<>();
        try {
            ExcelEquipementService.ImportResult result = excelEquipementService.importerEquipements(file);
            resp.put("success", !result.hasErreurs() || result.getSucces() > 0);
            resp.put("succes", result.getSucces());
            resp.put("message", result.getSucces() + " équipement(s) importé(s) avec succès");
            resp.put("erreurs", result.getErreurs());
            resp.put("avertissements", result.getAvertissements());
        } catch (Exception e) {
            log.error("Erreur import équipements", e);
            resp.put("success", false);
            resp.put("message", "Erreur lors de l'import : " + e.getMessage());
        }
        return resp;
    }

    // ── Types d'équipement ─────────────────────────────────────────────────

    @GetMapping("/types")
    @Transactional(readOnly = true)
    public String listeTypes(Model model) {
        List<TypeEquipement> types = typeEquipementRepository.findAll().stream()
                .sorted(Comparator.comparing(TypeEquipement::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
        Map<UUID, Long> nbParType = new HashMap<>();
        for (TypeEquipement t : types) {
            nbParType.put(t.getId(), equipmentRepository.countByTypeEquipement(t));
        }
        List<CategorieEquipement> categories = categorieEquipementRepository.findAllByOrderByOrdreAscNomAsc();
        Map<UUID, Long> nbParCategorie = new HashMap<>();
        for (CategorieEquipement cat : categories) {
            nbParCategorie.put(cat.getId(), typeEquipementRepository.countByCategorie(cat));
        }
        model.addAttribute("types", types);
        model.addAttribute("nbParType", nbParType);
        model.addAttribute("nbParCategorie", nbParCategorie);
        model.addAttribute("categories", categories);
        model.addAttribute("totalTypes", types.size());
        model.addAttribute("totalEquipements", equipmentRepository.count());
        model.addAttribute("pageStats", AdminStatsFactory.typesEquipement(equipmentRepository, typeEquipementRepository, categorieEquipementRepository));
        return "admin/types-equipement";
    }

    @PostMapping("/types/creer")
    public String creerType(
            @RequestParam String nom,
            @RequestParam String categorieId,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            if (typeEquipementRepository.existsByNom(nom.trim())) {
                ra.addFlashAttribute("error", "Un type avec ce nom existe déjà");
                return "redirect:/admin/equipements/types";
            }
            CategorieEquipement cat = categorieEquipementRepository.findById(UUID.fromString(categorieId))
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            TypeEquipement t = new TypeEquipement();
            t.setNom(nom.trim());
            t.setCategorie(cat);
            if (description != null && !description.isBlank()) t.setDescription(description.trim());
            typeEquipementRepository.save(t);
            ra.addFlashAttribute("success", "Type créé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }

    @PostMapping("/types/modifier/{id}")
    public String modifierType(
            @PathVariable @NonNull UUID id,
            @RequestParam String nom,
            @RequestParam String categorieId,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            TypeEquipement t = typeEquipementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Type introuvable"));
            String nomTrim = nom.trim();
            if (nomTrim.isEmpty()) {
                ra.addFlashAttribute("error", "Le nom est obligatoire");
                return "redirect:/admin/equipements/types";
            }
            if (!t.getNom().equalsIgnoreCase(nomTrim)
                    && typeEquipementRepository.existsByNom(nomTrim)) {
                ra.addFlashAttribute("error", "Un type avec ce nom existe déjà");
                return "redirect:/admin/equipements/types";
            }
            CategorieEquipement cat = categorieEquipementRepository.findById(UUID.fromString(categorieId))
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            t.setNom(nomTrim);
            t.setCategorie(cat);
            t.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            typeEquipementRepository.save(t);
            ra.addFlashAttribute("success", "Type modifié avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }

    @PostMapping("/types/supprimer/{id}")
    public String supprimerType(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            TypeEquipement t = typeEquipementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Type introuvable"));
            long n = equipmentRepository.countByTypeEquipement(t);
            if (n > 0) {
                ra.addFlashAttribute("error", "Ce type est utilisé par " + n + " équipement(s). Réaffectez-les avant suppression.");
                return "redirect:/admin/equipements/types";
            }
            typeEquipementRepository.deleteById(id);
            ra.addFlashAttribute("success", "Type supprimé");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }

    // ── Catégories d'équipement (entités) ───────────────────────────────────

    @PostMapping("/categories/creer")
    public String creerCategorie(
            @RequestParam String nom,
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer ordre,
            RedirectAttributes ra) {
        try {
            String nomT = nom.trim();
            String codeT = code.trim().toUpperCase();
            if (nomT.isEmpty() || codeT.isEmpty()) {
                ra.addFlashAttribute("error", "Le nom et le code sont obligatoires");
                return "redirect:/admin/equipements/types";
            }
            if (categorieEquipementRepository.existsByNomIgnoreCase(nomT)
                    || categorieEquipementRepository.existsByCodeIgnoreCase(codeT)) {
                ra.addFlashAttribute("error", "Une catégorie avec ce nom ou ce code existe déjà");
                return "redirect:/admin/equipements/types";
            }
            CategorieEquipement c = new CategorieEquipement();
            c.setNom(nomT);
            c.setCode(codeT);
            c.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            c.setOrdre(ordre != null ? ordre : 0);
            categorieEquipementRepository.save(c);
            ra.addFlashAttribute("success", "Catégorie créée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }

    @PostMapping("/categories/modifier/{id}")
    public String modifierCategorie(
            @PathVariable @NonNull UUID id,
            @RequestParam String nom,
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer ordre,
            RedirectAttributes ra) {
        try {
            CategorieEquipement c = categorieEquipementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            String nomT = nom.trim();
            String codeT = code.trim().toUpperCase();
            if (nomT.isEmpty() || codeT.isEmpty()) {
                ra.addFlashAttribute("error", "Le nom et le code sont obligatoires");
                return "redirect:/admin/equipements/types";
            }
            if (categorieEquipementRepository.existsByNomIgnoreCaseAndIdNot(nomT, id)
                    || categorieEquipementRepository.existsByCodeIgnoreCaseAndIdNot(codeT, id)) {
                ra.addFlashAttribute("error", "Nom ou code déjà utilisé par une autre catégorie");
                return "redirect:/admin/equipements/types";
            }
            c.setNom(nomT);
            c.setCode(codeT);
            c.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            c.setOrdre(ordre != null ? ordre : c.getOrdre());
            categorieEquipementRepository.save(c);
            ra.addFlashAttribute("success", "Catégorie modifiée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }

    @PostMapping("/categories/supprimer/{id}")
    public String supprimerCategorie(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            CategorieEquipement c = categorieEquipementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            long n = typeEquipementRepository.countByCategorie(c);
            if (n > 0) {
                ra.addFlashAttribute("error", "Cette catégorie est utilisée par " + n + " type(s) d'équipement.");
                return "redirect:/admin/equipements/types";
            }
            categorieEquipementRepository.deleteById(id);
            ra.addFlashAttribute("success", "Catégorie supprimée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }
}
