package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.CategorieEquipement;
import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.TypeEquipement;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.TypeEquipementRepository;
import com.iusjc.weschedule.service.ExcelEquipementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/equipements")
@Slf4j
public class EquipmentController {

    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private TypeEquipementRepository typeEquipementRepository;
    @Autowired private SalleRepository salleRepository;
    @Autowired private ExcelEquipementService excelEquipementService;

    // ── Liste ──────────────────────────────────────────────────────────────

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("categories", CategorieEquipement.values());
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
            m.put("statut", eq.getStatut().name());
            if (eq.getTypeEquipement() != null) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", eq.getTypeEquipement().getId());
                t.put("nom", eq.getTypeEquipement().getNom());
                t.put("categorie", eq.getTypeEquipement().getCategorie().name());
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
    public String details(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        return equipmentRepository.findById(id).map(eq -> {
            model.addAttribute("equipement", eq);
            return "admin/equipement-details";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Équipement introuvable");
            return "redirect:/admin/equipements";
        });
    }

    // ── Formulaire création ────────────────────────────────────────────────

    @GetMapping("/nouveau")
    public String nouveauForm(Model model) {
        model.addAttribute("equipement", new Equipment());
        model.addAttribute("types", typeEquipementRepository.findAll());
        model.addAttribute("salles", salleRepository.findAll());
        model.addAttribute("statuts", StatutEquipement.values());
        model.addAttribute("mode", "creation");
        return "admin/equipement-form";
    }

    @PostMapping("/creer")
    public String creer(
            @RequestParam String nom,
            @RequestParam(required = false) String typeEquipementId,
            @RequestParam(required = false) String numeroSerie,
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
            Equipment eq = new Equipment();
            eq.setNom(nom.trim());
            eq.setStatut(StatutEquipement.valueOf(statut));
            if (numeroSerie != null && !numeroSerie.isBlank()) eq.setNumeroSerie(numeroSerie.trim());
            if (description != null && !description.isBlank()) eq.setDescription(description.trim());
            if (photo != null && !photo.isBlank()) eq.setPhoto(photo.trim());
            if (typeEquipementId != null && !typeEquipementId.isBlank())
                typeEquipementRepository.findById(UUID.fromString(typeEquipementId)).ifPresent(eq::setTypeEquipement);
            if (salleId != null && !salleId.isBlank())
                salleRepository.findById(UUID.fromString(salleId)).ifPresent(eq::setSalle);
            equipmentRepository.save(eq);
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
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String photo,
            @RequestParam(defaultValue = "DISPONIBLE") String statut,
            @RequestParam(required = false) String salleId,
            RedirectAttributes ra) {
        try {
            Equipment eq = equipmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Équipement introuvable"));
            eq.setNom(nom.trim());
            eq.setStatut(StatutEquipement.valueOf(statut));
            eq.setNumeroSerie(numeroSerie != null && !numeroSerie.isBlank() ? numeroSerie.trim() : null);
            eq.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            eq.setPhoto(photo != null && !photo.isBlank() ? photo.trim() : null);
            eq.setTypeEquipement(null);
            eq.setSalle(null);
            if (typeEquipementId != null && !typeEquipementId.isBlank())
                typeEquipementRepository.findById(UUID.fromString(typeEquipementId)).ifPresent(eq::setTypeEquipement);
            if (salleId != null && !salleId.isBlank())
                salleRepository.findById(UUID.fromString(salleId)).ifPresent(eq::setSalle);
            equipmentRepository.save(eq);
            ra.addFlashAttribute("success", "Équipement modifié avec succès");
            return "redirect:/admin/equipements/details/" + id;
        } catch (Exception e) {
            log.error("Erreur modification équipement", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/equipements/modifier/" + id;
        }
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
    public String listeTypes(Model model) {
        model.addAttribute("types", typeEquipementRepository.findAll());
        model.addAttribute("categories", CategorieEquipement.values());
        return "admin/types-equipement";
    }

    @PostMapping("/types/creer")
    public String creerType(
            @RequestParam String nom,
            @RequestParam String categorie,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            if (typeEquipementRepository.existsByNom(nom.trim())) {
                ra.addFlashAttribute("error", "Un type avec ce nom existe déjà");
                return "redirect:/admin/equipements/types";
            }
            TypeEquipement t = new TypeEquipement();
            t.setNom(nom.trim());
            t.setCategorie(CategorieEquipement.valueOf(categorie));
            if (description != null && !description.isBlank()) t.setDescription(description.trim());
            typeEquipementRepository.save(t);
            ra.addFlashAttribute("success", "Type créé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }

    @PostMapping("/types/supprimer/{id}")
    public String supprimerType(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            typeEquipementRepository.deleteById(id);
            ra.addFlashAttribute("success", "Type supprimé");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/equipements/types";
    }
}
