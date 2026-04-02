package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.TypeCours;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.CoursRepository;
import com.iusjc.weschedule.repositories.EcoleRepository;
import com.iusjc.weschedule.repositories.EnseignantRepository;
import com.iusjc.weschedule.repositories.UERepository;
import com.iusjc.weschedule.service.ExcelCoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/cours")
@Slf4j
public class CoursController {

    @Autowired private CoursRepository      coursRepository;
    @Autowired private UERepository         ueRepository;
    @Autowired private ClasseRepository     classeRepository;
    @Autowired private EcoleRepository      ecoleRepository;
    @Autowired private EnseignantRepository enseignantRepository;
    @Autowired private ExcelCoursService    excelCoursService;

    // ── API : Classes d'une école (pour le filtre dynamique) ──────────────

    @GetMapping("/api/classes-par-ecole/{ecoleId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> classesParEcole(@PathVariable UUID ecoleId) {
        List<Classe> classes = classeRepository.findAll().stream()
                .filter(c -> c.getEcole() != null && c.getEcole().getIdEcole().equals(ecoleId))
                .toList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Classe c : classes) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",  c.getIdClasse());
            m.put("nom", c.getNom());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // ── API : UEs d'une classe (pour le filtre dynamique) ─────────────────

    @GetMapping("/api/ues-par-classe/{classeId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> uesParClasse(@PathVariable UUID classeId) {
        List<UE> ues = ueRepository.findByClassesIdClasse(classeId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UE ue : ues) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",       ue.getIdUE());
            m.put("code",     ue.getCode());
            m.put("intitule", ue.getIntitule());
            m.put("semestre", ue.getSemestre());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/api/enseignants-par-ue/{ueId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> enseignantsParUE(@PathVariable UUID ueId) {
        // Récupérer tous les enseignants qui ont cette UE dans leur liste uesEnseignees
        List<Enseignant> enseignants = enseignantRepository.findAll().stream()
                .filter(e -> e.getUesEnseignees() != null && 
                            e.getUesEnseignees().stream()
                                .anyMatch(ue -> ue.getIdUE().equals(ueId)))
                .toList();
        
        // Construire la liste de résultats
        List<Map<String, Object>> result = new ArrayList<>();
        for (Enseignant enseignant : enseignants) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", enseignant.getIdUser().toString());
            m.put("nom", enseignant.getNom());
            m.put("prenom", enseignant.getPrenom());
            result.add(m);
        }
        
        // Trier par nom
        result.sort((a, b) -> {
            String nomA = (String) a.get("nom");
            String nomB = (String) b.get("nom");
            return nomA.compareTo(nomB);
        });
        
        return ResponseEntity.ok(result);
    }

    // ── Liste ──────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public String liste(Model model) {
        List<Cours> cours = coursRepository.findAll();
        cours.forEach(c -> {
            if (c.getClasse() != null) {
                c.getClasse().getNom();
                if (c.getClasse().getEcole() != null) c.getClasse().getEcole().getNomEcole();
            }
            if (c.getUe() != null) c.getUe().getCode();
            if (c.getEnseignant() != null) c.getEnseignant().getNom();
        });
        model.addAttribute("cours",   cours);
        model.addAttribute("classes", classeRepository.findAll());
        model.addAttribute("ecoles",  ecoleRepository.findAll());
        model.addAttribute("ues",     ueRepository.findAll());
        return "admin/cours-liste";
    }

    // ── Détails ────────────────────────────────────────────────────────────

    @GetMapping("/details/{id}")
    @Transactional(readOnly = true)
    public String details(@PathVariable UUID id, Model model, RedirectAttributes ra) {
        Optional<Cours> opt = coursRepository.findById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("error", "Cours introuvable"); return "redirect:/admin/cours"; }
        Cours c = opt.get();
        if (c.getClasse() != null && c.getClasse().getEcole() != null) c.getClasse().getEcole().getNomEcole();
        if (c.getUe() != null) c.getUe().getCode();
        if (c.getEnseignant() != null) c.getEnseignant().getNom();
        model.addAttribute("cours", c);
        return "admin/cours-details";
    }

    // ── Formulaire création ────────────────────────────────────────────────

    @GetMapping("/nouveau")
    public String nouveauForm(Model model) {
        model.addAttribute("cours",       new Cours());
        model.addAttribute("classes",     classeRepository.findAll());
        model.addAttribute("ecoles",      ecoleRepository.findAll());
        model.addAttribute("enseignants", enseignantRepository.findAll());
        model.addAttribute("types",       TypeCours.values());
        model.addAttribute("mode",        "creation");
        return "admin/cours-form";
    }

    @PostMapping("/creer")
    public String creer(
            @RequestParam String intitule,
            @RequestParam String typeCours,
            @RequestParam UUID classeId,
            @RequestParam UUID ueId,
            @RequestParam(required = false) UUID enseignantId,
            @RequestParam Integer dureeTotal,
            @RequestParam(required = false) Integer dureeSeanceParJour,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            Cours cours = new Cours();
            cours.setIntitule(intitule.trim());
            cours.setTypeCours(TypeCours.valueOf(typeCours));
            cours.setDureeTotal(dureeTotal);
            cours.setDureeRestante(dureeTotal);
            cours.setDureeSeanceParJour(dureeSeanceParJour);
            cours.setDescription(description != null && !description.isBlank() ? description.trim() : null);

            classeRepository.findById(classeId).ifPresent(cours::setClasse);
            ueRepository.findById(ueId).ifPresent(cours::setUe);
            if (enseignantId != null) enseignantRepository.findById(enseignantId).ifPresent(cours::setEnseignant);

            coursRepository.save(cours);
            ra.addFlashAttribute("success", "Cours « " + cours.getIntitule() + " » créé avec succès");
            return "redirect:/admin/cours";
        } catch (Exception e) {
            log.error("Erreur création cours", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/cours/nouveau";
        }
    }

    // ── Formulaire modification ────────────────────────────────────────────

    @GetMapping("/modifier/{id}")
    @Transactional(readOnly = true)
    public String modifierForm(@PathVariable UUID id, Model model, RedirectAttributes ra) {
        Optional<Cours> opt = coursRepository.findById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("error", "Cours introuvable"); return "redirect:/admin/cours"; }
        Cours c = opt.get();
        if (c.getClasse() != null && c.getClasse().getEcole() != null) c.getClasse().getEcole().getNomEcole();
        if (c.getUe() != null) c.getUe().getCode();
        if (c.getEnseignant() != null) c.getEnseignant().getNom();

        // UEs de la classe sélectionnée (pour pré-remplir le select)
        List<UE> uesClasse = c.getClasse() != null
                ? ueRepository.findByClassesIdClasse(c.getClasse().getIdClasse())
                : List.of();

        model.addAttribute("cours",       c);
        model.addAttribute("classes",     classeRepository.findAll());
        model.addAttribute("ecoles",      ecoleRepository.findAll());
        model.addAttribute("uesClasse",   uesClasse);
        model.addAttribute("enseignants", enseignantRepository.findAll());
        model.addAttribute("types",       TypeCours.values());
        model.addAttribute("mode",        "modification");
        return "admin/cours-form";
    }

    @PostMapping("/modifier/{id}")
    public String modifier(
            @PathVariable UUID id,
            @RequestParam String intitule,
            @RequestParam String typeCours,
            @RequestParam UUID classeId,
            @RequestParam UUID ueId,
            @RequestParam(required = false) UUID enseignantId,
            @RequestParam Integer dureeTotal,
            @RequestParam Integer dureeRestante,
            @RequestParam(required = false) Integer dureeSeanceParJour,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
            cours.setIntitule(intitule.trim());
            cours.setTypeCours(TypeCours.valueOf(typeCours));
            cours.setDureeTotal(dureeTotal);
            cours.setDureeRestante(Math.min(dureeRestante, dureeTotal));
            cours.setDureeSeanceParJour(dureeSeanceParJour);
            cours.setDescription(description != null && !description.isBlank() ? description.trim() : null);

            classeRepository.findById(classeId).ifPresent(cours::setClasse);
            ueRepository.findById(ueId).ifPresent(cours::setUe);
            cours.setEnseignant(enseignantId != null
                    ? enseignantRepository.findById(enseignantId).orElse(null) : null);

            coursRepository.save(cours);
            ra.addFlashAttribute("success", "Cours modifié avec succès");
            return "redirect:/admin/cours/details/" + id;
        } catch (Exception e) {
            log.error("Erreur modification cours", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/cours/modifier/" + id;
        }
    }

    // ── Suppression ────────────────────────────────────────────────────────

    @PostMapping("/supprimer/{id}")
    public String supprimer(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            coursRepository.deleteById(id);
            ra.addFlashAttribute("success", "Cours supprimé");
        } catch (Exception e) {
            log.error("Erreur suppression cours", e);
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/cours";
    }

    // ── Export Excel ───────────────────────────────────────────────────────

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] data = excelCoursService.exporterCours();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cours.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export cours", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Import Excel ───────────────────────────────────────────────────────

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        try {
            ExcelCoursService.ImportResult result = excelCoursService.importerCours(file);
            if (result.getSucces() > 0)
                ra.addFlashAttribute("success", result.getSucces() + " cours importé(s) avec succès");
            if (result.hasErreurs())
                ra.addFlashAttribute("importErreurs", result.getErreurs());
            if (result.hasAvertissements())
                ra.addFlashAttribute("importAvertissements", result.getAvertissements());
            if (result.getSucces() == 0 && !result.hasErreurs())
                ra.addFlashAttribute("error", "Aucun cours importé");
        } catch (Exception e) {
            log.error("Erreur import cours", e);
            ra.addFlashAttribute("error", "Erreur import : " + e.getMessage());
        }
        return "redirect:/admin/cours";
    }
}
