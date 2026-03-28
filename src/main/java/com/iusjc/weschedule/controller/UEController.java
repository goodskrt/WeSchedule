package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.CoursRepository;
import com.iusjc.weschedule.repositories.EcoleRepository;
import com.iusjc.weschedule.repositories.UERepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/ues")
@Slf4j
public class UEController {

    @Autowired private UERepository      ueRepository;
    @Autowired private ClasseRepository  classeRepository;
    @Autowired private CoursRepository   coursRepository;
    @Autowired private EcoleRepository   ecoleRepository;

    private static final List<Integer> SEMESTRES = List.of(1, 2);

    // ── Liste ──────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional
    public String liste(Model model) {
        List<UE> ues = ueRepository.findAll();
        // Forcer le chargement lazy + construire les maps pour Thymeleaf
        Map<UUID, String> ueClassNames = new HashMap<>();
        Map<UUID, String> ueEcoleIds   = new HashMap<>();
        ues.forEach(ue -> {
            if (ue.getClasses() != null && !ue.getClasses().isEmpty()) {
                ue.getClasses().size(); // init lazy
                ueClassNames.put(ue.getIdUE(),
                    ue.getClasses().stream()
                        .map(c -> c.getNom() != null ? c.getNom() : "")
                        .collect(java.util.stream.Collectors.joining("|")));
                ueEcoleIds.put(ue.getIdUE(),
                    ue.getClasses().stream()
                        .filter(c -> c.getEcole() != null)
                        .map(c -> c.getEcole().getIdEcole().toString())
                        .distinct()
                        .collect(java.util.stream.Collectors.joining("|")));
            } else {
                ueClassNames.put(ue.getIdUE(), "");
                ueEcoleIds.put(ue.getIdUE(), "");
            }
        });
        model.addAttribute("ues",          ues);
        model.addAttribute("ueClassNames", ueClassNames);
        model.addAttribute("ueEcoleIds",   ueEcoleIds);
        model.addAttribute("classes",      classeRepository.findAll());
        model.addAttribute("ecoles",       ecoleRepository.findAll());
        return "admin/ues";
    }

    // ── Détails ────────────────────────────────────────────────────────────

    @GetMapping("/details/{id}")
    public String details(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        Optional<UE> opt = ueRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "UE introuvable");
            return "redirect:/admin/ues";
        }
        UE ue = opt.get();
        // Forcer l'initialisation du Set lazy pour Thymeleaf
        if (ue.getClasses() != null) ue.getClasses().size();
        model.addAttribute("ue", ue);
        return "admin/ue-details";
    }

    // ── Formulaire création ────────────────────────────────────────────────

    @GetMapping("/nouvelle")
    public String nouvelleForm(Model model) {
        model.addAttribute("ue",        new UE());
        model.addAttribute("classes",   classeRepository.findAll());
        model.addAttribute("ecoles",    ecoleRepository.findAll());
        model.addAttribute("semestres", SEMESTRES);
        model.addAttribute("statuts",   StatutUE.values());
        model.addAttribute("mode",      "creation");
        return "admin/ue-form";
    }

    @PostMapping("/creer")
    public String creer(
            @RequestParam String code,
            @RequestParam String intitule,
            @RequestParam Integer semestre,
            @RequestParam Integer credits,
            @RequestParam(required = false) Integer duree,
            @RequestParam(defaultValue = "ACTIF") String statut,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<String> classeIds,
            RedirectAttributes ra) {
        try {
            if (code == null || code.isBlank()) {
                ra.addFlashAttribute("error", "Le code est obligatoire");
                return "redirect:/admin/ues/nouvelle";
            }
            if (ueRepository.existsByCode(code.trim().toUpperCase())) {
                ra.addFlashAttribute("error", "Une UE avec ce code existe déjà");
                return "redirect:/admin/ues/nouvelle";
            }
            UE ue = new UE();
            ue.setCode(code.trim().toUpperCase());
            ue.setIntitule(intitule != null ? intitule.trim() : null);
            ue.setSemestre(semestre);
            ue.setCredits(credits);
            ue.setDuree(duree);
            ue.setStatut(StatutUE.valueOf(statut));
            ue.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            if (classeIds != null && !classeIds.isEmpty()) {
                Set<Classe> classes = new HashSet<>();
                classeIds.forEach(cid -> {
                    try { classeRepository.findById(UUID.fromString(cid)).ifPresent(classes::add); }
                    catch (Exception ignored) {}
                });
                ue.setClasses(classes);
            }
            ueRepository.save(ue);
            ra.addFlashAttribute("success", "UE « " + ue.getCode() + " » créée avec succès");
            return "redirect:/admin/ues";
        } catch (Exception e) {
            log.error("Erreur création UE", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/ues/nouvelle";
        }
    }

    // ── Formulaire modification ────────────────────────────────────────────

    @GetMapping("/modifier/{id}")
    public String modifierForm(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        Optional<UE> opt = ueRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "UE introuvable");
            return "redirect:/admin/ues";
        }
        UE ue = opt.get();
        // Forcer l'initialisation du Set lazy pour Thymeleaf
        if (ue.getClasses() != null) ue.getClasses().size();
        model.addAttribute("ue",        ue);
        model.addAttribute("classes",   classeRepository.findAll());
        model.addAttribute("ecoles",    ecoleRepository.findAll());
        model.addAttribute("semestres", SEMESTRES);
        model.addAttribute("statuts",   StatutUE.values());
        model.addAttribute("mode",      "modification");
        return "admin/ue-form";
    }

    @PostMapping("/modifier/{id}")
    public String modifier(
            @PathVariable @NonNull UUID id,
            @RequestParam String code,
            @RequestParam String intitule,
            @RequestParam Integer semestre,
            @RequestParam Integer credits,
            @RequestParam(required = false) Integer duree,
            @RequestParam(defaultValue = "ACTIF") String statut,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<String> classeIds,
            RedirectAttributes ra) {
        try {
            UE ue = ueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("UE introuvable"));
            // Vérifier unicité code si changé
            String newCode = code.trim().toUpperCase();
            if (!newCode.equals(ue.getCode()) && ueRepository.existsByCode(newCode)) {
                ra.addFlashAttribute("error", "Une UE avec ce code existe déjà");
                return "redirect:/admin/ues/modifier/" + id;
            }
            ue.setCode(newCode);
            ue.setIntitule(intitule != null ? intitule.trim() : null);
            ue.setSemestre(semestre);
            ue.setCredits(credits);
            ue.setDuree(duree);
            ue.setStatut(StatutUE.valueOf(statut));
            ue.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            Set<Classe> classes = new HashSet<>();
            if (classeIds != null) {
                classeIds.forEach(cid -> {
                    try { classeRepository.findById(UUID.fromString(cid)).ifPresent(classes::add); }
                    catch (Exception ignored) {}
                });
            }
            ue.setClasses(classes);
            ueRepository.save(ue);
            ra.addFlashAttribute("success", "UE modifiée avec succès");
            return "redirect:/admin/ues/details/" + id;
        } catch (Exception e) {
            log.error("Erreur modification UE", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/ues/modifier/" + id;
        }
    }

    // ── Suppression ────────────────────────────────────────────────────────

    @PostMapping("/supprimer/{id}")
    @Transactional
    public String supprimer(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            UE ue = ueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("UE introuvable"));
            // Détacher les cours liés
            coursRepository.findByUe(ue).forEach(c -> {
                c.setUe(null);
                coursRepository.save(c);
            });
            // Vider les classes associées
            if (ue.getClasses() != null) ue.getClasses().clear();
            ueRepository.save(ue);
            ueRepository.delete(ue);
            ra.addFlashAttribute("success", "UE supprimée");
        } catch (Exception e) {
            log.error("Erreur suppression UE", e);
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/ues";
    }
}
