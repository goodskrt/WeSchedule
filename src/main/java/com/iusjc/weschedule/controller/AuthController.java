package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.security.JwtService;
import com.iusjc.weschedule.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    // Page de login
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // → src/main/resources/templates/login.html
    }

    // Traitement du login (formulaire)
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String motDePasse,
            Model model
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, motDePasse)
            );

            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(user);

            model.addAttribute("token", token);
            model.addAttribute("user", user.getUtilisateur());
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
            return "login";
        }
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal user) {
            model.addAttribute("user", user.getUtilisateur());
        }
        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}