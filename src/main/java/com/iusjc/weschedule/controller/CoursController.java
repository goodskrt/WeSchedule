package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.CoursRepository;
import com.iusjc.weschedule.repositories.UERepository;
import com.iusjc.weschedule.service.DureeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/cours")
public class CoursController {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UERepository ueRepository;

    @Autowired
    private DureeService dureeService;
    
    @Autowired
    private com.iusjc.weschedule.repositories.SeanceClasseRepository seanceClasseRepository;

    /**
     * Liste de tous les cours
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String listeCours(Model model) {
        List<Cours> cours = coursRepository.findAll();
        
        // Enrichir avec les informations d'avancement
        List<Map<String, Object>> coursAvecInfos = new ArrayList<>();
        for (Cours c : cours) {
            Map<String, Object> info = new HashMap<>();
            info.put("cours", c);
            
            // Charger explicitement les collections pour éviter LazyInitializationException
            if (c.getUe() != null) {
                int heuresEffectuees = dureeService.calculerHeuresEffectuees(c);
                double pourcentage = dureeService.calculerPourcentageAvancement(c);
                boolean termine = dureeService.estTermine(c);
                
                info.put("heuresEffectuees", heuresEffectuees);
                info.put("pourcentage", pourcentage);
                info.put("termine", termine);
                
                // Initialiser les classes et écoles
                if (c.getUe().getClasses() != null) {
                    c.getUe().getClasses().size(); // Force l'initialisation
                    c.getUe().getClasses().forEach(classe -> {
                        if (classe.getEcole() != null) {
                            classe.getEcole().getNomEcole(); // Force l'initialisation de l'école
                        }
                    });
                }
            }
            
            // Récupérer la première séance pour le tri
            com.iusjc.weschedule.models.SeanceClasse premiereSeance = seanceClasseRepository.findFirstByCoursOrderByDateAsc(c);
            info.put("premiereSeance", premiereSeance);
            
            coursAvecInfos.add(info);
        }
        
        // Trier par date de réservation (date de la première séance)
        coursAvecInfos.sort((info1, info2) -> {
            com.iusjc.weschedule.models.SeanceClasse seance1 = (com.iusjc.weschedule.models.SeanceClasse) info1.get("premiereSeance");
            com.iusjc.weschedule.models.SeanceClasse seance2 = (com.iusjc.weschedule.models.SeanceClasse) info2.get("premiereSeance");
            
            // Les cours sans séance vont à la fin
            if (seance1 == null && seance2 == null) return 0;
            if (seance1 == null) return 1;
            if (seance2 == null) return -1;
            
            return seance1.getDate().compareTo(seance2.getDate());
        });
        
        model.addAttribute("coursListe", coursAvecInfos);
        model.addAttribute("totalCours", cours.size());
        
        return "admin/cours-liste";
    }

    /**
     * Formulaire de création d'un cours
     */
    @GetMapping("/nouveau")
    public String nouveauCoursForm(Model model) {
        // Récupérer uniquement les UE actives
        List<UE> uesActives = ueRepository.findByStatut(StatutUE.ACTIF);
        
        model.addAttribute("cours", new Cours());
        model.addAttribute("ues", uesActives);
        model.addAttribute("mode", "creation");
        
        return "admin/cours-form";
    }

    /**
     * Créer un nouveau cours
     */
    @PostMapping("/creer")
    public String creerCours(
            @RequestParam String intitule,
            @RequestParam String typeCours,
            @RequestParam UUID ueId,
            @RequestParam(required = false) Integer duree,
            RedirectAttributes redirectAttributes) {
        
        try {
            UE ue = ueRepository.findById(ueId)
                    .orElseThrow(() -> new RuntimeException("UE non trouvée"));
            
            // Vérifier que l'UE est active
            if (ue.getStatut() != StatutUE.ACTIF) {
                redirectAttributes.addFlashAttribute("error", 
                    "Impossible de créer un cours pour une UE inactive");
                return "redirect:/admin/cours/nouveau";
            }
            
            Cours cours = new Cours();
            cours.setIntitule(intitule);
            cours.setTypeCours(com.iusjc.weschedule.enums.TypeCours.valueOf(typeCours));
            cours.setUe(ue);
            
            // Si durée non spécifiée, utiliser la durée totale de l'UE
            if (duree == null || duree == 0) {
                cours.setDuree(ue.getDuree());
            } else {
                cours.setDuree(duree);
            }
            
            // Valider la durée
            dureeService.validerDureeCours(cours);
            
            coursRepository.save(cours);
            
            redirectAttributes.addFlashAttribute("success", 
                "Cours créé avec succès");
            return "redirect:/admin/cours";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la création du cours: " + e.getMessage());
            return "redirect:/admin/cours/nouveau";
        }
    }

    /**
     * Formulaire de modification d'un cours
     */
    @GetMapping("/modifier/{id}")
    public String modifierCoursForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            
            List<UE> uesActives = ueRepository.findByStatut(StatutUE.ACTIF);
            
            // Ajouter l'UE actuelle si elle n'est pas active (pour permettre la modification)
            if (cours.getUe() != null && cours.getUe().getStatut() != StatutUE.ACTIF) {
                if (!uesActives.contains(cours.getUe())) {
                    uesActives.add(cours.getUe());
                }
            }
            
            model.addAttribute("cours", cours);
            model.addAttribute("ues", uesActives);
            model.addAttribute("mode", "modification");
            
            // Informations d'avancement
            if (cours.getUe() != null) {
                model.addAttribute("heuresEffectuees", dureeService.calculerHeuresEffectuees(cours));
                model.addAttribute("pourcentage", dureeService.calculerPourcentageAvancement(cours));
            }
            
            return "admin/cours-form";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur: " + e.getMessage());
            return "redirect:/admin/cours";
        }
    }

    /**
     * Mettre à jour un cours
     */
    @PostMapping("/modifier/{id}")
    public String modifierCours(
            @PathVariable UUID id,
            @RequestParam String intitule,
            @RequestParam String typeCours,
            @RequestParam UUID ueId,
            @RequestParam Integer duree,
            RedirectAttributes redirectAttributes) {
        
        try {
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            
            UE ue = ueRepository.findById(ueId)
                    .orElseThrow(() -> new RuntimeException("UE non trouvée"));
            
            cours.setIntitule(intitule);
            cours.setTypeCours(com.iusjc.weschedule.enums.TypeCours.valueOf(typeCours));
            cours.setUe(ue);
            cours.setDuree(duree);
            
            // Valider la durée
            dureeService.validerDureeCours(cours);
            
            coursRepository.save(cours);
            
            redirectAttributes.addFlashAttribute("success", 
                "Cours modifié avec succès");
            return "redirect:/admin/cours";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/admin/cours/modifier/" + id;
        }
    }

    /**
     * Supprimer un cours
     */
    @PostMapping("/supprimer/{id}")
    public String supprimerCours(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            
            coursRepository.delete(cours);
            
            redirectAttributes.addFlashAttribute("success", 
                "Cours supprimé avec succès");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
        
        return "redirect:/admin/cours";
    }

    /**
     * Réinitialiser les heures d'un cours
     */
    @PostMapping("/reinitialiser/{id}")
    public String reinitialiserHeures(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            
            dureeService.reinitialiserHeures(cours);
            
            redirectAttributes.addFlashAttribute("success", 
                "Heures réinitialisées avec succès");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la réinitialisation: " + e.getMessage());
        }
        
        return "redirect:/admin/cours";
    }

    /**
     * Détails d'un cours
     */
    @GetMapping("/details/{id}")
    @Transactional(readOnly = true)
    public String detailsCours(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            
            model.addAttribute("cours", cours);
            
            if (cours.getUe() != null) {
                model.addAttribute("heuresEffectuees", dureeService.calculerHeuresEffectuees(cours));
                model.addAttribute("pourcentage", dureeService.calculerPourcentageAvancement(cours));
                model.addAttribute("termine", dureeService.estTermine(cours));
                model.addAttribute("resume", dureeService.obtenirResume(cours));
                
                // Initialiser les classes et écoles pour éviter LazyInitializationException
                if (cours.getUe().getClasses() != null) {
                    cours.getUe().getClasses().size(); // Force l'initialisation
                    cours.getUe().getClasses().forEach(classe -> {
                        if (classe.getEcole() != null) {
                            classe.getEcole().getNomEcole(); // Force l'initialisation de l'école
                        }
                    });
                }
            }
            
            return "admin/cours-details";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur: " + e.getMessage());
            return "redirect:/admin/cours";
        }
    }
}
