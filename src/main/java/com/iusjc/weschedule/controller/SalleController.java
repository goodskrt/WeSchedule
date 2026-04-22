package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.enums.StatutSalle;
import com.iusjc.weschedule.enums.TypeSalle;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.util.AdminStatsFactory;
import com.iusjc.weschedule.util.EquipmentStatutRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/salles")
public class SalleController {

    @Autowired private SalleRepository salleRepository;
    @Autowired private EquipmentRepository equipmentRepository;

    // ── Helper : évite les null renvoyés par le repository ──────────────────
    private <T> List<T> safe(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    @GetMapping
    public String listeSalles(Model model) {
        model.addAttribute("salles", safe(salleRepository.findAll()));
        model.addAttribute("typesSalle", TypeSalle.values());
        model.addAttribute("statuts", StatutSalle.values());
        model.addAttribute("pageStats", AdminStatsFactory.salles(salleRepository));
        return "admin/salles-liste";
    }

    @GetMapping("/nouvelle")
    public String nouvelleSalleForm(Model model) {
        model.addAttribute("salle", new Salle());
        model.addAttribute("typesSalle", TypeSalle.values());
        model.addAttribute("statuts", StatutSalle.values());
        // CORRECTION : safe() garantit une liste non-null → plus de "Iteration variable cannot be null"
        model.addAttribute("equipementsDisponibles", safe(equipmentRepository.findBySalleIsNull()));
        model.addAttribute("equipementsActuels", Collections.emptyList());
        model.addAttribute("mode", "creation");
        return "admin/salle-form";
    }

    @PostMapping("/creer")
    public String creerSalle(
            @RequestParam String nomSalle,
            @RequestParam String typeSalle,
            @RequestParam Integer capacite,
            @RequestParam(required = false) String etage,
            @RequestParam(required = false) String batiment,
            @RequestParam(defaultValue = "DISPONIBLE") String statut,
            @RequestParam(required = false) List<String> equipementIds,
            RedirectAttributes ra) {
        try {
            Optional<Salle> existante = salleRepository.findByNomSalle(nomSalle);
            if (existante.isPresent()) {
                ra.addFlashAttribute("error", "Une salle avec ce nom existe déjà");
                return "redirect:/admin/salles/nouvelle";
            }
            if (capacite <= 0) {
                ra.addFlashAttribute("error", "La capacité doit être supérieure à 0");
                return "redirect:/admin/salles/nouvelle";
            }
            Salle salle = new Salle();
            salle.setNomSalle(nomSalle);
            salle.setTypeSalle(TypeSalle.valueOf(typeSalle));
            salle.setCapacite(capacite);
            salle.setEtage(etage != null && !etage.isBlank() ? etage : null);
            salle.setBatiment(batiment != null && !batiment.isBlank() ? batiment : null);
            salle.setStatut(StatutSalle.valueOf(statut));
            Salle saved = salleRepository.save(salle);

            if (equipementIds != null) {
                equipementIds.forEach(idStr -> {
                    try {
                        equipmentRepository.findById(UUID.fromString(idStr)).ifPresent(eq -> {
                            eq.setSalle(saved);
                            EquipmentStatutRules.syncStatutAvecSalle(eq, StatutEquipement.DISPONIBLE);
                            equipmentRepository.save(eq);
                        });
                    } catch (Exception ignored) {}
                });
            }
            ra.addFlashAttribute("success", "Salle créée avec succès");
            return "redirect:/admin/salles";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/salles/nouvelle";
        }
    }

    @GetMapping("/modifier/{id}")
    public String modifierSalleForm(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            model.addAttribute("salle", salle);
            model.addAttribute("typesSalle", TypeSalle.values());
            model.addAttribute("statuts", StatutSalle.values());

            List<Equipment> equipsActuels = safe(equipmentRepository.findBySalle(salle));
            List<Equipment> equipsLibres  = safe(equipmentRepository.findBySalleIsNull());

            List<Equipment> tousEquips = new ArrayList<>(equipsActuels);
            tousEquips.addAll(equipsLibres);

            model.addAttribute("equipementsDisponibles", tousEquips);
            model.addAttribute("equipementsActuels",
                    equipsActuels.stream().map(e -> e.getId().toString()).toList());
            model.addAttribute("mode", "modification");
            return "admin/salle-form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/salles";
        }
    }

    @PostMapping("/modifier/{id}")
    public String modifierSalle(
            @PathVariable @NonNull UUID id,
            @RequestParam String nomSalle,
            @RequestParam String typeSalle,
            @RequestParam Integer capacite,
            @RequestParam(required = false) String etage,
            @RequestParam(required = false) String batiment,
            @RequestParam(defaultValue = "DISPONIBLE") String statut,
            @RequestParam(required = false) List<String> equipementIds,
            RedirectAttributes ra) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            Optional<Salle> existante = salleRepository.findByNomSalle(nomSalle);
            if (existante.isPresent() && !existante.get().getIdSalle().equals(id)) {
                ra.addFlashAttribute("error", "Une autre salle avec ce nom existe déjà");
                return "redirect:/admin/salles/modifier/" + id;
            }
            if (capacite <= 0) {
                ra.addFlashAttribute("error", "La capacité doit être supérieure à 0");
                return "redirect:/admin/salles/modifier/" + id;
            }
            salle.setNomSalle(nomSalle);
            salle.setTypeSalle(TypeSalle.valueOf(typeSalle));
            salle.setCapacite(capacite);
            salle.setEtage(etage != null && !etage.isBlank() ? etage : null);
            salle.setBatiment(batiment != null && !batiment.isBlank() ? batiment : null);
            salle.setStatut(StatutSalle.valueOf(statut));
            salleRepository.save(salle);

            // CORRECTION : safe() avant forEach
            safe(equipmentRepository.findBySalle(salle)).forEach(eq -> {
                eq.setSalle(null);
                EquipmentStatutRules.syncStatutAvecSalle(eq, StatutEquipement.DISPONIBLE);
                equipmentRepository.save(eq);
            });

            if (equipementIds != null) {
                equipementIds.forEach(idStr -> {
                    try {
                        equipmentRepository.findById(UUID.fromString(idStr)).ifPresent(eq -> {
                            eq.setSalle(salle);
                            EquipmentStatutRules.syncStatutAvecSalle(eq, StatutEquipement.DISPONIBLE);
                            equipmentRepository.save(eq);
                        });
                    } catch (Exception ignored) {}
                });
            }
            ra.addFlashAttribute("success", "Salle modifiée avec succès");
            return "redirect:/admin/salles";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/salles/modifier/" + id;
        }
    }

    @PostMapping("/supprimer/{id}")
    public String supprimerSalle(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            // CORRECTION : safe() avant forEach
            safe(equipmentRepository.findBySalle(salle)).forEach(eq -> {
                eq.setSalle(null);
                EquipmentStatutRules.syncStatutAvecSalle(eq, StatutEquipement.DISPONIBLE);
                equipmentRepository.save(eq);
            });
            salleRepository.delete(salle);
            ra.addFlashAttribute("success", "Salle supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/salles";
    }

    @GetMapping("/details/{id}")
    public String detailsSalle(@PathVariable @NonNull UUID id, Model model, RedirectAttributes ra) {
        try {
            Salle salle = salleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            model.addAttribute("salle", salle);
            // CORRECTION : safe() garantit une liste non-null → plus de "Iteration variable cannot be null"
            model.addAttribute("equipements", safe(equipmentRepository.findBySalle(salle)));
            return "admin/salle-details";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/salles";
        }
    }
}