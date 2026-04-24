package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Etudiant;
import com.iusjc.weschedule.models.Groupe;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.EcoleRepository;
import com.iusjc.weschedule.repositories.EtudiantRepository;
import com.iusjc.weschedule.repositories.GroupeRepository;
import com.iusjc.weschedule.util.AdminStatsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/etudiants")
public class EtudiantController {

    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private EcoleRepository ecoleRepository;
    @Autowired private GroupeRepository groupeRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String liste(
            @RequestParam(required = false) UUID ecoleId,
            @RequestParam(required = false) UUID classeId,
            Model model) {
        UUID classeFiltreEffective = classeId;
        if (ecoleId != null && classeFiltreEffective != null) {
            final UUID cf = classeFiltreEffective;
            boolean ok = classeRepository.findByEcole_IdEcoleOrderByNomAsc(ecoleId).stream()
                    .anyMatch(c -> c.getIdClasse().equals(cf));
            if (!ok) {
                classeFiltreEffective = null;
            }
        }

        List<Etudiant> etudiants;
        if (ecoleId != null && classeFiltreEffective != null) {
            etudiants = etudiantRepository.findByClasse_IdClasseAndClasse_Ecole_IdEcoleOrderByNomAscPrenomAsc(classeFiltreEffective, ecoleId);
        } else if (ecoleId != null) {
            etudiants = etudiantRepository.findByClasse_Ecole_IdEcoleOrderByNomAscPrenomAsc(ecoleId);
        } else if (classeFiltreEffective != null) {
            etudiants = etudiantRepository.findByClasse_IdClasseOrderByNomAscPrenomAsc(classeFiltreEffective);
        } else {
            etudiants = etudiantRepository.findAllByOrderByNomAscPrenomAsc();
        }

        List<Classe> classesPourFiltre = (ecoleId != null)
                ? classeRepository.findByEcole_IdEcoleOrderByNomAsc(ecoleId)
                : new ArrayList<>();

        model.addAttribute("etudiants", etudiants);
        model.addAttribute("ecoles", ecoleRepository.findAll());
        model.addAttribute("ecoleFiltre", ecoleId);
        model.addAttribute("classesPourFiltre", classesPourFiltre);
        model.addAttribute("classeFiltre", classeFiltreEffective);
        model.addAttribute("pageStats", AdminStatsFactory.etudiants(etudiantRepository, classeRepository, groupeRepository));
        return "admin/etudiants";
    }

    @GetMapping("/nouveau")
    @Transactional(readOnly = true)
    public String formulaireNouveau(Model model) {
        model.addAttribute("etudiant", new Etudiant());
        model.addAttribute("classes", classeRepository.findAll());
        model.addAttribute("groupes", groupeRepository.findAll());
        model.addAttribute("mode", "creation");
        return "admin/etudiant-form";
    }

    @GetMapping("/modifier/{id}")
    @Transactional(readOnly = true)
    public String formulaireEdition(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        return etudiantRepository.findById(id).map(e -> {
            model.addAttribute("etudiant", e);
            model.addAttribute("classes", classeRepository.findAll());
            model.addAttribute("groupes", groupeRepository.findAll());
            model.addAttribute("mode", "edition");
            return "admin/etudiant-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Étudiant introuvable");
            return "redirect:/admin/etudiants";
        });
    }

    @PostMapping("/creer")
    public String creer(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String numeroEtudiant,
            @RequestParam(required = false) String dateNaissance,
            @RequestParam(required = false) String classeId,
            @RequestParam(required = false) String groupeId,
            RedirectAttributes ra) {
        try {
            String nomT = trimReq(nom);
            String prenomT = trimReq(prenom);
            if (nomT.isEmpty() || prenomT.isEmpty()) {
                ra.addFlashAttribute("error", "Le nom et le prénom sont obligatoires");
                return "redirect:/admin/etudiants/nouveau";
            }
            String emailN = normalizeEmail(email);
            String numN = normalizeOptional(numeroEtudiant);
            if (emailN != null && etudiantRepository.existsByEmailIgnoreCase(emailN)) {
                ra.addFlashAttribute("error", "Un étudiant avec cet email existe déjà");
                return "redirect:/admin/etudiants/nouveau";
            }
            if (numN != null && etudiantRepository.existsByNumeroEtudiantIgnoreCase(numN)) {
                ra.addFlashAttribute("error", "Ce numéro étudiant est déjà utilisé");
                return "redirect:/admin/etudiants/nouveau";
            }

            Etudiant e = new Etudiant();
            e.setNom(nomT);
            e.setPrenom(prenomT);
            e.setEmail(emailN);
            e.setTelephone(normalizeOptional(telephone));
            e.setNumeroEtudiant(numN);
            e.setDateNaissance(parseDate(dateNaissance));
            e.setClasse(resolveClasse(classeId));
            e.setGroupe(resolveGroupe(groupeId));

            etudiantRepository.save(e);
            ra.addFlashAttribute("success", "Étudiant créé avec succès");
            return "redirect:/admin/etudiants";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Erreur : " + ex.getMessage());
            return "redirect:/admin/etudiants/nouveau";
        }
    }

    @PostMapping("/modifier/{id}")
    public String modifier(
            @PathVariable @NonNull UUID id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String numeroEtudiant,
            @RequestParam(required = false) String dateNaissance,
            @RequestParam(required = false) String classeId,
            @RequestParam(required = false) String groupeId,
            RedirectAttributes ra) {
        try {
            Etudiant e = etudiantRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable"));
            String nomT = trimReq(nom);
            String prenomT = trimReq(prenom);
            if (nomT.isEmpty() || prenomT.isEmpty()) {
                ra.addFlashAttribute("error", "Le nom et le prénom sont obligatoires");
                return "redirect:/admin/etudiants/modifier/" + id;
            }
            String emailN = normalizeEmail(email);
            String numN = normalizeOptional(numeroEtudiant);
            if (emailN != null && etudiantRepository.existsByEmailIgnoreCaseAndIdEtudiantNot(emailN, id)) {
                ra.addFlashAttribute("error", "Un autre étudiant utilise cet email");
                return "redirect:/admin/etudiants/modifier/" + id;
            }
            if (numN != null && etudiantRepository.existsByNumeroEtudiantIgnoreCaseAndIdEtudiantNot(numN, id)) {
                ra.addFlashAttribute("error", "Ce numéro étudiant est déjà utilisé");
                return "redirect:/admin/etudiants/modifier/" + id;
            }

            e.setNom(nomT);
            e.setPrenom(prenomT);
            e.setEmail(emailN);
            e.setTelephone(normalizeOptional(telephone));
            e.setNumeroEtudiant(numN);
            e.setDateNaissance(parseDate(dateNaissance));
            e.setClasse(resolveClasse(classeId));
            e.setGroupe(resolveGroupe(groupeId));

            etudiantRepository.save(e);
            ra.addFlashAttribute("success", "Étudiant mis à jour");
            return "redirect:/admin/etudiants";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Erreur : " + ex.getMessage());
            return "redirect:/admin/etudiants/modifier/" + id;
        }
    }

    @PostMapping("/supprimer/{id}")
    public String supprimer(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            if (!etudiantRepository.existsById(id)) {
                ra.addFlashAttribute("error", "Étudiant introuvable");
                return "redirect:/admin/etudiants";
            }
            etudiantRepository.deleteById(id);
            ra.addFlashAttribute("success", "Étudiant supprimé");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Impossible de supprimer : " + ex.getMessage());
        }
        return "redirect:/admin/etudiants";
    }

    private static String trimReq(String s) {
        return s != null ? s.trim() : "";
    }

    private static String normalizeOptional(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private static String normalizeEmail(String email) {
        String n = normalizeOptional(email);
        return n != null ? n.toLowerCase() : null;
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Optional<Classe> parseClasseId(String classeId) {
        if (classeId == null || classeId.isBlank()) return Optional.empty();
        try {
            return classeRepository.findById(UUID.fromString(classeId.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Groupe> parseGroupeId(String groupeId) {
        if (groupeId == null || groupeId.isBlank()) return Optional.empty();
        try {
            return groupeRepository.findById(UUID.fromString(groupeId.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Classe resolveClasse(String classeId) {
        return parseClasseId(classeId).orElse(null);
    }

    private Groupe resolveGroupe(String groupeId) {
        return parseGroupeId(groupeId).orElse(null);
    }
}
