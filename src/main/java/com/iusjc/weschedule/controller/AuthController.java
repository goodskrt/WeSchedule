package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.dto.RegisterRequest;
import com.iusjc.weschedule.dto.AuthResponse;
import com.iusjc.weschedule.dto.ForgotPasswordRequest;
import com.iusjc.weschedule.dto.LoginRequest;
import com.iusjc.weschedule.dto.ResetPasswordWithTokenRequest;
import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.service.AuthService;
import com.iusjc.weschedule.service.PasswordResetService;
import com.iusjc.weschedule.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Page de login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, 
                           @RequestParam(required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("logout", "Vous avez été déconnecté avec succès");
        }
        return "login";
    }

    /**
     * Page d'inscription
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    /**
     * Traitement de l'inscription (Backend only - POST REST API)
     * Les étudiants ne sont pas autorisés à créer un compte
     */
    @PostMapping("/api/register")
    @ResponseBody
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Données d'inscription invalides")
                    .build();
        }

        return AuthResponse.builder()
                .success(false)
                .message("L'accès est réservé aux administrateurs et enseignants uniquement")
                .build();
    }

    /**
     * Inscription admin (uniquement par formulaire HTML)
     */
    @PostMapping("/admin/signup")
    public String adminSignup(@Valid RegisterRequest request, 
                             BindingResult bindingResult, 
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("registerRequest", request);
            return "admin/register";
        }

        AuthResponse response = authService.register(request, Role.ADMINISTRATEUR);
        
        if (response.isSuccess()) {
            model.addAttribute("message", "Administrateur enregistré avec succès");
            return "redirect:/login";
        } else {
            model.addAttribute("error", response.getMessage());
            model.addAttribute("registerRequest", request);
            return "admin/register";
        }
    }

    /**
     * Dashboard - Redirection selon le rôle
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            
            String role = userPrincipal.getUtilisateur().getRole().toString();
            
            switch (role) {
                case "ADMINISTRATEUR":
                    return "redirect:/dashboard/admin";
                case "ENSEIGNANT":
                    return "redirect:/dashboard/enseignant";
                default:
                    return "redirect:/login";
            }
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Vue d'ensemble
     */
    @GetMapping("/dashboard/admin")
    public String dashboardAdmin(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/dashboard-admin";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Enseignants
     */
    @GetMapping("/dashboard/admin/enseignants")
    public String dashboardAdminEnseignants(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/enseignants";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Nouvel Enseignant
     */
    @GetMapping("/dashboard/admin/enseignants/nouveau")
    public String dashboardAdminNouvelEnseignant(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("isEdit", false);
            return "admin/enseignant-form";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Modifier Enseignant
     */
    @GetMapping("/dashboard/admin/enseignants/{id}/modifier")
    public String dashboardAdminModifierEnseignant(@PathVariable String id, Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("enseignantId", id);
            model.addAttribute("isEdit", true);
            return "admin/enseignant-form";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Détail Enseignant
     */
    @GetMapping("/dashboard/admin/enseignants/{id}")
    public String dashboardAdminDetailEnseignant(@PathVariable String id, Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("enseignantId", id);
            return "admin/enseignant-detail";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Disponibilités d'un Enseignant
     */
    @GetMapping("/dashboard/admin/enseignants/{id}/disponibilites")
    public String dashboardAdminEnseignantDisponibilites(@PathVariable String id, Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("enseignantId", id);
            return "admin/enseignant-disponibilites";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Salles
     */
    @GetMapping("/dashboard/admin/salles")
    public String dashboardAdminSalles(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/salles";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Cours
     */
    @GetMapping("/dashboard/admin/cours")
    public String dashboardAdminCours(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/cours";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Emplois du temps
     */
    @GetMapping("/dashboard/admin/emplois")
    public String dashboardAdminEmplois(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/emplois";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Rapports
     */
    @GetMapping("/dashboard/admin/rapports")
    public String dashboardAdminRapports(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/rapports";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Admin - Paramètres
     */
    @GetMapping("/dashboard/admin/parametres")
    public String dashboardAdminParametres(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "admin/parametres";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Enseignant - Vue d'ensemble
     */
    @GetMapping("/dashboard/enseignant")
    public String dashboardEnseignant(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "dashboard/dashboard-enseignant";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Enseignant - Mes Cours
     */
    @GetMapping("/dashboard/enseignant/cours")
    public String dashboardEnseignantCours(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "dashboard/mes-cours";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Enseignant - Mon Emploi du temps
     */
    @GetMapping("/dashboard/enseignant/emploi-temps")
    public String dashboardEnseignantEmploi(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "dashboard/emploi-temps";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard Enseignant - Mon Profil
     */
    @GetMapping("/dashboard/enseignant/profil")
    public String dashboardEnseignantProfil(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("email", userPrincipal.getUtilisateur().getEmail());
            return "dashboard/profil";
        }
        return "redirect:/login";
    }

    /**
     * Page d'accueil - redirige vers login
     */
    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Page de navigation complète vers tous les sections
     */
    @GetMapping("/navigation")
    public String navigation(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            model.addAttribute("user", userPrincipal.getUtilisateur());
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
        }
        return "navigation";
    }

    /**
     * Endpoint pour initier la réinitialisation de mot de passe
     * POST /api/forgot-password
     */
    @PostMapping("/api/forgot-password")
    @ResponseBody
    public AuthResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());
            return AuthResponse.builder()
                    .success(true)
                    .message("Un lien de réinitialisation a été envoyé à votre email si le compte existe")
                    .build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors de l'envoi du lien de réinitialisation")
                    .build();
        }
    }

    /**
     * Page de demande de réinitialisation de mot de passe
     * GET /reset-password-request
     */
    @GetMapping("/reset-password-request")
    public String resetPasswordRequestPage() {
        return "reset-password-request";
    }

    /**
     * Page de réinitialisation de mot de passe (accessible via lien email)
     * GET /reset-password?token=xxx
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        log.info("Accès à la page de réinitialisation avec token: {}", token);
        
        if (token != null && !token.isEmpty()) {
            boolean isValid = passwordResetService.validateResetToken(token);
            log.info("Validation du token {}: {}", token, isValid);
            
            if (isValid) {
                // Récupérer les données utilisateur pour validation côté frontend
                String[] userData = passwordResetService.getEmailAndPasswordFromToken(token);
                if (userData != null) {
                    model.addAttribute("token", token);
                    model.addAttribute("userEmail", userData[0]);
                    model.addAttribute("currentPasswordHash", userData[1]);
                    return "reset-password";
                }
            }
            
            log.warn("Token invalide ou expiré: {}", token);
            return "reset-password-error";
        }
        
        log.warn("Accès à la page de réinitialisation sans token");
        return "reset-password-error";
    }

    /**
     * Endpoint pour réinitialiser le mot de passe via token
     * POST /api/reset-password-with-token
     */
    @PostMapping("/api/reset-password-with-token")
    @ResponseBody
    public AuthResponse resetPasswordWithToken(@RequestBody ResetPasswordWithTokenRequest request,
                                             HttpServletRequest httpRequest) {
        try {
            log.info("Tentative de réinitialisation avec token: {}", request.getToken());
            
            // Valider le token
            if (!passwordResetService.validateResetToken(request.getToken())) {
                log.warn("Token invalide ou expiré: {}", request.getToken());
                return AuthResponse.builder()
                        .success(false)
                        .message("Lien de réinitialisation invalide ou expiré")
                        .build();
            }

            // Vérifier que les mots de passe correspondent
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                log.warn("Les mots de passe ne correspondent pas");
                return AuthResponse.builder()
                        .success(false)
                        .message("Les mots de passe ne correspondent pas")
                        .build();
            }

            // Vérifier la longueur du mot de passe
            if (request.getNewPassword().length() < 8) {
                log.warn("Mot de passe trop court: {} caractères", request.getNewPassword().length());
                return AuthResponse.builder()
                        .success(false)
                        .message("Le mot de passe doit contenir au moins 8 caractères")
                        .build();
            }

            // Réinitialiser le mot de passe et récupérer l'email
            String userEmail = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            log.info("Mot de passe réinitialisé avec succès pour: {}", userEmail);
            
            // CONNEXION AUTOMATIQUE IMMÉDIATE
            Optional<Utilisateur> userOptional = authService.findByEmail(userEmail);
            if (userOptional.isPresent()) {
                Utilisateur utilisateur = userOptional.get();
                
                // Créer le UserPrincipal et l'authentification
                UserPrincipal userPrincipal = new UserPrincipal(utilisateur);
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
                
                // Définir l'authentification dans le contexte
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Sauvegarder la session
                HttpSession session = httpRequest.getSession(true);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                                   SecurityContextHolder.getContext());
                
                log.info("Connexion automatique réussie pour: {}", userEmail);
                
                return AuthResponse.builder()
                        .success(true)
                        .message("RESET_AND_LOGIN_SUCCESS")
                        .email(userEmail)
                        .role(utilisateur.getRole())
                        .build();
            }
            
            return AuthResponse.builder()
                    .success(true)
                    .message("RESET_SUCCESS")
                    .email(userEmail)
                    .build();
            
        } catch (RuntimeException e) {
            log.error("Erreur lors de la réinitialisation: {}", e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation: {}", e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors de la réinitialisation du mot de passe")
                    .build();
        }
    }

    /**
     * Page de succès après réinitialisation
     * GET /reset-password-success?email=xxx&role=xxx&autoLogin=true
     */
    @GetMapping("/reset-password-success")
    public String resetPasswordSuccess(@RequestParam(required = false) String email,
                                     @RequestParam(required = false) String role,
                                     @RequestParam(required = false) String autoLogin,
                                     Model model) {
        log.info("Accès à la page de succès - Email: {}, Rôle: {}, AutoLogin: {}", 
                email, role, autoLogin);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("autoLogin", "true".equals(autoLogin));
        return "reset-password-success";
    }

    /**
     * Endpoint pour valider si le nouveau mot de passe est différent de l'ancien
     * POST /api/validate-new-password
     */
    @PostMapping("/api/validate-new-password")
    @ResponseBody
    public AuthResponse validateNewPassword(@RequestBody Map<String, String> request) {
        try {
            String currentHash = request.get("currentHash");
            String newPassword = request.get("newPassword");
            
            if (currentHash == null || newPassword == null) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Données manquantes")
                        .build();
            }
            
            // Vérifier si le nouveau mot de passe est identique à l'ancien
            boolean isSamePassword = passwordEncoder.matches(newPassword, currentHash);
            
            return AuthResponse.builder()
                    .success(!isSamePassword)
                    .message(isSamePassword ? "Le nouveau mot de passe doit être différent de l'ancien" : "Mot de passe valide")
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de la validation du mot de passe: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(true) // En cas d'erreur, on laisse passer
                    .message("Validation impossible")
                    .build();
        }
    }

    /**
     * Endpoint pour connexion automatique après réinitialisation
     * POST /api/auto-login
     */
    @PostMapping("/api/auto-login")
    @ResponseBody
    public AuthResponse autoLogin(@RequestBody LoginRequest loginRequest, 
                                HttpServletRequest request, 
                                HttpServletResponse response) {
        try {
            log.info("=== DÉBUT CONNEXION AUTOMATIQUE ===");
            log.info("Email reçu: {}", loginRequest.getEmail());
            log.info("Mot de passe reçu (longueur): {}", loginRequest.getMotDePasse() != null ? loginRequest.getMotDePasse().length() : "null");
            
            // Vérifier que l'utilisateur existe
            Optional<Utilisateur> userOptional = authService.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                log.error("Utilisateur non trouvé: {}", loginRequest.getEmail());
                return AuthResponse.builder()
                        .success(false)
                        .message("Utilisateur non trouvé")
                        .build();
            }
            
            Utilisateur utilisateur = userOptional.get();
            log.info("Utilisateur trouvé - Email: {}, Hash en base: {}", 
                    utilisateur.getEmail(), utilisateur.getMotDePasse());
            
            // Utiliser le service d'authentification
            AuthResponse authResponse = authService.authenticate(loginRequest.getEmail(), loginRequest.getMotDePasse());
            log.info("Résultat authentification: {}", authResponse.isSuccess());
            
            if (authResponse.isSuccess()) {
                // Créer le UserPrincipal
                UserPrincipal userPrincipal = new UserPrincipal(utilisateur);
                
                // Créer l'authentification
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal, 
                        null, 
                        userPrincipal.getAuthorities()
                    );
                
                // Définir l'authentification dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Sauvegarder la session
                HttpSession session = request.getSession(true);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                                   SecurityContextHolder.getContext());
                
                log.info("Session créée avec succès pour: {}, Session ID: {}", 
                        loginRequest.getEmail(), session.getId());
                
                return AuthResponse.builder()
                        .success(true)
                        .message("Connexion réussie")
                        .email(authResponse.getEmail())
                        .role(authResponse.getRole())
                        .build();
            } else {
                log.warn("Authentification échouée pour: {}, Message: {}", 
                        loginRequest.getEmail(), authResponse.getMessage());
                return AuthResponse.builder()
                        .success(false)
                        .message("Email ou mot de passe incorrect")
                        .build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la connexion automatique: {}", e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors de la connexion automatique: " + e.getMessage())
                    .build();
        }
    }
}
