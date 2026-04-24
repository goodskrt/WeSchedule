package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injecte automatiquement nomComplet et email dans tous les modèles Thymeleaf.
 * Évite de répéter ces attributs dans chaque contrôleur.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("nomComplet")
    public String nomComplet() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getNomComplet();
        }
        return "";
    }

    @ModelAttribute("email")
    public String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUtilisateur().getEmail();
        }
        return "";
    }
}
