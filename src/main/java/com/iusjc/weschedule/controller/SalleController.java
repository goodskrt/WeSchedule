package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.TypeSalle;
import com.iusjc.weschedule.enums.StatutReservation;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.Reservation;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/salles")
public class SalleController {

    @Autowired
    private SalleRepository salleRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Liste de toutes les salles
     */
    @GetMapping
    public String listeSalles(Model model) {
        List<Salle> salles = salleRepository.findAll();
        
        model.addAttribute("salles", salles);
        model.addAttribute("typesSalle", TypeSalle.values());
        
        return "admin/salles-liste";
    }

    /**
     * Formulaire de création d'une salle
     */
    @GetMapping("/nouvelle")
    public String nouvelleSalleForm(Model model) {
        model.addAttribute("salle", new Salle());
        model.addAttribute("typesSalle", TypeSalle.values());
        model.addAttribute("mode", "creation");
        
        return "admin/salle-form";
    }

    /**
     * Créer une nouvelle salle
     */
    @PostMapping("/creer")
    public String creerSalle(
            @RequestParam String nomSalle,
            @RequestParam String typeSalle,
            @RequestParam Integer capacite,
            @RequestParam(required = false) String etage,
            @RequestParam(required = false) String batiment,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Salle> salleExistante = salleRepository.findByNomSalle(nomSalle);
            if (salleExistante.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Une salle avec ce nom existe déjà");
                return "redirect:/admin/salles/nouvelle";
            }
            if (capacite <= 0) {
                redirectAttributes.addFlashAttribute("error", "La capacité doit être supérieure à 0");
                return "redirect:/admin/salles/nouvelle";
            }
            
            Salle salle = new Salle();
            salle.setNomSalle(nomSalle);
            salle.setTypeSalle(TypeSalle.valueOf(typeSalle));
            salle.setCapacite(capacite);
            salle.setEtage(etage != null && !etage.isBlank() ? etage : null);
            salle.setBatiment(batiment != null && !batiment.isBlank() ? batiment : null);
            salleRepository.save(salle);
            
            redirectAttributes.addFlashAttribute("success", "Salle créée avec succès");
            return "redirect:/admin/salles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/admin/salles/nouvelle";
        }
    }

    /**
     * Formulaire de modification d'une salle
     */
    @GetMapping("/modifier/{id}")
    public String modifierSalleForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            
            model.addAttribute("salle", salle);
            model.addAttribute("typesSalle", TypeSalle.values());
            model.addAttribute("mode", "modification");
            
            return "admin/salle-form";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur: " + e.getMessage());
            return "redirect:/admin/salles";
        }
    }

    /**
     * Mettre à jour une salle
     */
    @PostMapping("/modifier/{id}")
    public String modifierSalle(
            @PathVariable UUID id,
            @RequestParam String nomSalle,
            @RequestParam String typeSalle,
            @RequestParam Integer capacite,
            @RequestParam(required = false) String etage,
            @RequestParam(required = false) String batiment,
            RedirectAttributes redirectAttributes) {
        
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            
            Optional<Salle> salleExistante = salleRepository.findByNomSalle(nomSalle);
            if (salleExistante.isPresent() && !salleExistante.get().getIdSalle().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Une autre salle avec ce nom existe déjà");
                return "redirect:/admin/salles/modifier/" + id;
            }
            if (capacite <= 0) {
                redirectAttributes.addFlashAttribute("error", "La capacité doit être supérieure à 0");
                return "redirect:/admin/salles/modifier/" + id;
            }
            
            salle.setNomSalle(nomSalle);
            salle.setTypeSalle(TypeSalle.valueOf(typeSalle));
            salle.setCapacite(capacite);
            salle.setEtage(etage != null && !etage.isBlank() ? etage : null);
            salle.setBatiment(batiment != null && !batiment.isBlank() ? batiment : null);
            salleRepository.save(salle);
            
            redirectAttributes.addFlashAttribute("success", "Salle modifiée avec succès");
            return "redirect:/admin/salles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/admin/salles/modifier/" + id;
        }
    }

    /**
     * Supprimer une salle
     */
    @PostMapping("/supprimer/{id}")
    public String supprimerSalle(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            
            // TODO: Vérifier s'il y a des séances ou réservations liées
            
            salleRepository.delete(salle);
            
            redirectAttributes.addFlashAttribute("success", 
                "Salle supprimée avec succès");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
        
        return "redirect:/admin/salles";
    }

    /**
     * Détails d'une salle
     */
    @GetMapping("/details/{id}")
    public String detailsSalle(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            
            model.addAttribute("salle", salle);
            
            // TODO: Ajouter les statistiques d'utilisation
            // - Nombre de séances planifiées
            // - Taux d'occupation
            // - Prochaines réservations
            
            return "admin/salle-details";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur: " + e.getMessage());
            return "redirect:/admin/salles";
        }
    }
}
