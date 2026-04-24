package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutReservation;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import com.iusjc.weschedule.service.ReservationSalleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/enseignant/reservations")
@PreAuthorize("hasRole('ENSEIGNANT')")
@RequiredArgsConstructor
@Slf4j
public class DemandeReservationEnseignantController {

    private final ReservationSalleService reservationSalleService;
    private final ReservationRepository reservationRepository;
    private final SalleRepository salleRepository;
    private final EquipmentRepository equipmentRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String page(Model model) {
        Utilisateur ens = utilisateurCourant();
        model.addAttribute("demandes", reservationRepository.findByReserveParOrderByDateCreationDesc(ens));
        model.addAttribute("salles", salleRepository.findAll());
        model.addAttribute("equipementsDisponibles", equipmentRepository.findBySalleIsNull());
        return "dashboard/enseignant-reservations";
    }

    @PostMapping("/nouvelle")
    @Transactional
    public String nouvelle(
            @RequestParam @NonNull UUID salleId,
            @RequestParam String startAt,
            @RequestParam String endAt,
            @RequestParam String motif,
            @RequestParam(required = false) List<UUID> equipmentIds,
            RedirectAttributes ra) {
        try {
            Utilisateur ens = utilisateurCourant();
            LocalDateTime debut = parseDateTimeInput(startAt);
            LocalDateTime fin = parseDateTimeInput(endAt);
            Salle salle = salleRepository.findById(salleId).orElseThrow();
            
            Set<Equipment> equips = new HashSet<>();
            if (equipmentIds != null) {
                equipmentIds.forEach(id -> equipmentRepository.findById(id).ifPresent(equips::add));
            }

            Reservation d = reservationSalleService.creerReservation(
                    salle, debut, fin, ens, motif, equips, StatutReservation.EN_ATTENTE);
            
            ra.addFlashAttribute("success", "Demande enregistrée (n° " + d.getIdResa().toString().substring(0, 8) + "…), en attente de validation.");
        } catch (Exception e) {
            log.warn("Création demande réservation: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/enseignant/reservations";
    }

    @PostMapping("/{id}/annuler")
    @Transactional
    public String annuler(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            reservationSalleService.annulerReservation(id);
            ra.addFlashAttribute("success", "Demande annulée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/enseignant/reservations";
    }

    private Utilisateur utilisateurCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Non authentifié");
        }
        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
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
}
