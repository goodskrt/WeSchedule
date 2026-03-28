package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Ecole;
import com.iusjc.weschedule.models.Filiere;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.CoursRepository;
import com.iusjc.weschedule.repositories.EcoleRepository;
import com.iusjc.weschedule.repositories.EmploiDuTempsClasseRepository;
import com.iusjc.weschedule.repositories.EtudiantRepository;
import com.iusjc.weschedule.repositories.FiliereRepository;
import com.iusjc.weschedule.repositories.UERepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/classes")
@Slf4j
public class ClasseController {

    @Autowired private ClasseRepository  classeRepository;
    @Autowired private EcoleRepository   ecoleRepository;
    @Autowired private FiliereRepository filiereRepository;
    @Autowired private CoursRepository   coursRepository;
    @Autowired private UERepository      ueRepository;
    @Autowired private EtudiantRepository            etudiantRepository;
    @Autowired private EmploiDuTempsClasseRepository emploiDuTempsClasseRepository;

    private static final List<String> NIVEAUX  = List.of("Niveau 1","Niveau 2","Niveau 3","Niveau 4","Niveau 5");
    private static final List<String> LANGUES  = List.of("Francophone","Anglophone");

    // ── Liste ──────────────────────────────────────────────────────────────

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("classes",  classeRepository.findAll());
        model.addAttribute("ecoles",   ecoleRepository.findAll());
        model.addAttribute("filieres", filiereRepository.findAll());
        return "admin/classes";
    }

    // ── Détails ────────────────────────────────────────────────────────────

    @GetMapping("/details/{id}")
    public String details(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        return classeRepository.findById(id).map(c -> {
            model.addAttribute("classe", c);
            return "admin/classe-details";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Classe introuvable");
            return "redirect:/admin/classes";
        });
    }

    // ── Formulaire création ────────────────────────────────────────────────

    @GetMapping("/nouvelle")
    public String nouvelleForm(Model model) {
        model.addAttribute("classe",   new Classe());
        model.addAttribute("ecoles",   ecoleRepository.findAll());
        model.addAttribute("filieres", filiereRepository.findAll());
        model.addAttribute("niveaux",  NIVEAUX);
        model.addAttribute("langues",  LANGUES);
        model.addAttribute("mode",     "creation");
        return "admin/classe-form";
    }

    @PostMapping("/creer")
    public String creer(
            @RequestParam String nom,
            @RequestParam(required = false) String ecoleId,
            @RequestParam(required = false) String filiereId,
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) Integer effectif,
            @RequestParam(required = false) String langue,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            if (nom == null || nom.isBlank()) {
                ra.addFlashAttribute("error", "Le nom est obligatoire");
                return "redirect:/admin/classes/nouvelle";
            }
            Classe c = new Classe();
            c.setNom(nom.trim());
            c.setNiveau(niveau);
            c.setEffectif(effectif);
            c.setLangue(langue != null && !langue.isBlank() ? langue : null);
            c.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            if (ecoleId != null && !ecoleId.isBlank())
                ecoleRepository.findById(UUID.fromString(ecoleId)).ifPresent(c::setEcole);
            if (filiereId != null && !filiereId.isBlank())
                filiereRepository.findById(UUID.fromString(filiereId)).ifPresent(c::setFiliere);
            classeRepository.save(c);
            ra.addFlashAttribute("success", "Classe « " + c.getNom() + " » créée avec succès");
            return "redirect:/admin/classes";
        } catch (Exception e) {
            log.error("Erreur création classe", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/classes/nouvelle";
        }
    }

    // ── Formulaire modification ────────────────────────────────────────────

    @GetMapping("/modifier/{id}")
    public String modifierForm(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        return classeRepository.findById(id).map(c -> {
            model.addAttribute("classe",   c);
            model.addAttribute("ecoles",   ecoleRepository.findAll());
            model.addAttribute("filieres", filiereRepository.findAll());
            model.addAttribute("niveaux",  NIVEAUX);
            model.addAttribute("langues",  LANGUES);
            model.addAttribute("mode",     "modification");
            return "admin/classe-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Classe introuvable");
            return "redirect:/admin/classes";
        });
    }

    @PostMapping("/modifier/{id}")
    public String modifier(
            @PathVariable @NonNull UUID id,
            @RequestParam String nom,
            @RequestParam(required = false) String ecoleId,
            @RequestParam(required = false) String filiereId,
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) Integer effectif,
            @RequestParam(required = false) String langue,
            @RequestParam(required = false) String description,
            RedirectAttributes ra) {
        try {
            Classe c = classeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Classe introuvable"));
            c.setNom(nom.trim());
            c.setNiveau(niveau);
            c.setEffectif(effectif);
            c.setLangue(langue != null && !langue.isBlank() ? langue : null);
            c.setDescription(description != null && !description.isBlank() ? description.trim() : null);
            c.setEcole(null);
            c.setFiliere(null);
            if (ecoleId != null && !ecoleId.isBlank())
                ecoleRepository.findById(UUID.fromString(ecoleId)).ifPresent(c::setEcole);
            if (filiereId != null && !filiereId.isBlank())
                filiereRepository.findById(UUID.fromString(filiereId)).ifPresent(c::setFiliere);
            classeRepository.save(c);
            ra.addFlashAttribute("success", "Classe modifiée avec succès");
            return "redirect:/admin/classes/details/" + id;
        } catch (Exception e) {
            log.error("Erreur modification classe", e);
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/classes/modifier/" + id;
        }
    }

    // ── Suppression ────────────────────────────────────────────────────────

    @PostMapping("/supprimer/{id}")
    @Transactional
    public String supprimer(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            Classe c = classeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Classe introuvable"));
            // 1. Détacher les cours de cette classe (cours.classe_id → null)
            coursRepository.detachFromClasse(id);
            // 2. Supprimer les lignes de jointure ue_classe
            ueRepository.deleteUeClasseByClasseId(id);
            // 3. Détacher les étudiants (etudiants.classe_id → null)
            etudiantRepository.detachFromClasse(id);
            // 4. Supprimer les emplois du temps (cascade supprime les séances)
            emploiDuTempsClasseRepository.deleteAll(emploiDuTempsClasseRepository.findByClasse(c));
            // 5. Supprimer la classe
            classeRepository.delete(c);
            ra.addFlashAttribute("success", "Classe supprimée");
        } catch (Exception e) {
            log.error("Erreur suppression classe", e);
            ra.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/classes";
    }
}
