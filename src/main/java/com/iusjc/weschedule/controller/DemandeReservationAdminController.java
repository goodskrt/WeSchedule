package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutReservation;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import com.iusjc.weschedule.service.ReservationSalleService;

import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/admin/reservations")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@RequiredArgsConstructor
@Slf4j
public class DemandeReservationAdminController {

    private final ReservationRepository reservationRepository;
    private final ReservationSalleService reservationSalleService;
    private final SalleRepository salleRepository;
    private final CoursRepository coursRepository;
    private final PlageHoraireRepository plageHoraireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EquipmentRepository equipmentRepository;

    private Utilisateur utilisateurCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Non authentifié");
        }
        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    private static java.time.LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Date/heure obligatoire");
        }
        String t = raw.trim();
        if (t.length() == 16) {
            t = t + ":00";
        }
        return java.time.LocalDateTime.parse(t);
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("reservations", reservationRepository.findAllByOrderByDateCreationDesc());
        return "admin/reservations-demandes";
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            reservationSalleService.validerReservation(id);
            ra.addFlashAttribute("success", "Réservation validée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/terminer")
    public String marquerTerminee(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            reservationSalleService.marquerTerminee(id);
            ra.addFlashAttribute("success", "Réservation marquée comme terminée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            reservationSalleService.annulerReservation(id);
            ra.addFlashAttribute("success", "Réservation annulée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }



    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            reservationSalleService.supprimerReservation(id);
            ra.addFlashAttribute("success", "Réservation supprimée définitivement");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("salles", salleRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll()); // Tous les utilisateurs (Admin +
                                                                             // Enseignants)
        model.addAttribute("currentUser", utilisateurCourant());
        model.addAttribute("cours", coursRepository.findAll());
        model.addAttribute("plages", plageHoraireRepository.findAll());
        model.addAttribute("equipementsMobiles", equipmentRepository.findBySalleIsNull());
        return "admin/reservation-form";
    }

    @PostMapping("/creer")
    public String creer(
            @RequestParam @NonNull UUID salleId,
            @RequestParam @NonNull UUID reserveParId,
            @RequestParam String startAt,
            @RequestParam String endAt,
            @RequestParam String motif,
            @RequestParam(required = false) List<UUID> equipementIds,
            @RequestParam(required = false) UUID coursId,
            RedirectAttributes ra) {
        try {
            Salle salle = salleRepository.findById(salleId).orElseThrow();
            Utilisateur demandeur = utilisateurRepository.findById(reserveParId).orElseThrow();
            LocalDateTime debut = parseDateTime(startAt);
            LocalDateTime fin = parseDateTime(endAt);

            Set<Equipment> equips = new java.util.HashSet<>();
            if (equipementIds != null) {
                equipementIds.forEach(id -> equipmentRepository.findById(id).ifPresent(equips::add));
            }

            Reservation r = reservationSalleService.creerReservation(
                    salle, debut, fin, demandeur, motif, equips, StatutReservation.CONFIRMEE);

            if (coursId != null) {
                coursRepository.findById(coursId).ifPresent(r::setCours);
                reservationRepository.save(r);
            }

            ra.addFlashAttribute("success", "Réservation administrative créée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @GetMapping("/editer/{id}")
    public String formulaireEdition(@PathVariable UUID id, Model model) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation invalide"));
        model.addAttribute("reservation", reservation);
        model.addAttribute("salles", salleRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("currentUser", utilisateurCourant());
        model.addAttribute("cours", coursRepository.findAll());
        model.addAttribute("plages", plageHoraireRepository.findAll());
        model.addAttribute("equipementsMobiles", equipmentRepository.findBySalleIsNull());
        return "admin/reservation-form";
    }

    @PostMapping("/modifier/{id}")
    public String modifier(
            @PathVariable @NonNull UUID id,
            @RequestParam @NonNull UUID salleId,
            @RequestParam @NonNull UUID reserveParId,
            @RequestParam String startAt,
            @RequestParam String endAt,
            @RequestParam String motif,
            @RequestParam(required = false) List<UUID> equipementIds,
            @RequestParam(required = false) UUID coursId,
            @RequestParam StatutReservation statut,
            RedirectAttributes ra) {
        try {
            Reservation r = reservationRepository.findById(id).orElseThrow();
            r.setSalle(salleRepository.findById(salleId).orElseThrow());
            r.setReservePar(utilisateurRepository.findById(reserveParId).orElseThrow());
            r.setStartAt(parseDateTime(startAt));
            r.setEndAt(parseDateTime(endAt));
            r.setMotif(motif);
            r.setStatut(statut);

            Set<Equipment> equips = new java.util.HashSet<>();
            if (equipementIds != null) {
                equipementIds.forEach(eid -> equipmentRepository.findById(eid).ifPresent(equips::add));
            }
            r.setEquipements(equips);

            if (coursId != null) {
                coursRepository.findById(coursId).ifPresent(r::setCours);
            } else {
                r.setCours(null);
            }

            reservationRepository.save(r);
            ra.addFlashAttribute("success", "Réservation modifiée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }
}
