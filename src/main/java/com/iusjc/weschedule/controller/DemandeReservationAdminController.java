package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.Utilisateur;
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

import java.util.UUID;

@Controller
@RequestMapping("/admin/reservations")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@RequiredArgsConstructor
@Slf4j
public class DemandeReservationAdminController {

    private final DemandeReservationService demandeReservationService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String liste(Model model) {
        model.addAttribute("demandes", demandeReservationService.listerToutes());
        return "admin/reservations-demandes";
    }

    @PostMapping("/{id}/accepter")
    @Transactional
    public String accepter(@PathVariable @NonNull UUID id, RedirectAttributes ra) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Utilisateur admin = utilisateurRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("Admin introuvable"));
            demandeReservationService.accepterDemande(id, admin, auth.getName());
            ra.addFlashAttribute("success", "Réservation acceptée");
        } catch (Exception e) {
            log.warn("Accepter réservation: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/refuser")
    @Transactional
    public String refuser(
            @PathVariable @NonNull UUID id,
            @RequestParam String commentaire,
            RedirectAttributes ra) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Utilisateur admin = utilisateurRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("Admin introuvable"));
            demandeReservationService.refuserDemande(id, admin, auth.getName(), commentaire);
            ra.addFlashAttribute("success", "Demande refusée");
        } catch (Exception e) {
            log.warn("Refuser réservation: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservations";
    }
}
