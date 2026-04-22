package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.DemandeReservationSalle;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import com.iusjc.weschedule.service.DemandeReservationService;
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
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/enseignant/reservations")
@PreAuthorize("hasRole('ENSEIGNANT')")
@RequiredArgsConstructor
@Slf4j
public class DemandeReservationEnseignantController {

    private final DemandeReservationService demandeReservationService;
    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String page(Model model) {
        Utilisateur ens = utilisateurCourant();
        model.addAttribute("demandes", demandeReservationService.listerPourEnseignant(ens));
        model.addAttribute("salles", salleRepository.findAll());
        model.addAttribute("equipementsDisponibles", demandeReservationService.equipementsDisponibles());
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
            DemandeReservationSalle d = demandeReservationService.creerDemande(
                    ens, salleId, debut, fin, motif, equipmentIds != null ? equipmentIds : List.of());
            ra.addFlashAttribute("success", "Demande enregistrée (n° " + d.getId().toString().substring(0, 8) + "…), en attente de validation par l'administration.");
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
            demandeReservationService.annulerParEnseignant(id, utilisateurCourant());
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
