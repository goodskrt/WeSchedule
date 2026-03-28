package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.dto.AuthResponse;
import com.iusjc.weschedule.dto.LoginRequest;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.security.JwtService;
import com.iusjc.weschedule.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@Tag(name = "Authentication", description = "API pour l'authentification des utilisateurs")
@Slf4j
public class AuthRestController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    /**
     * Endpoint de connexion REST avec JWT
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", 
               description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("=== TENTATIVE DE CONNEXION REST ===");
            log.info("Email: {}", loginRequest.getEmail());
            
            // Authentifier l'utilisateur via AuthService
            AuthResponse authResponse = authService.authenticate(loginRequest.getEmail(), loginRequest.getMotDePasse());
            
            if (!authResponse.isSuccess()) {
                log.warn("Authentification échouée pour: {}", loginRequest.getEmail());
                return ResponseEntity.badRequest().body(authResponse);
            }

            log.info("Authentification réussie pour: {}", loginRequest.getEmail());

            // Récupérer l'utilisateur depuis la base de données
            Optional<Utilisateur> userOptional = authService.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                log.error("Utilisateur non trouvé après authentification: {}", loginRequest.getEmail());
                return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Utilisateur non trouvé")
                        .build()
                );
            }

            Utilisateur utilisateur = userOptional.get();
            log.info("Utilisateur récupéré: {} {} ({})", 
                    utilisateur.getPrenom(), utilisateur.getNom(), utilisateur.getRole());

            // Générer le token JWT
            String token = jwtService.generateToken(utilisateur);
            log.info("Token JWT généré pour: {}", loginRequest.getEmail());

            // Construire la réponse
            AuthResponse response = AuthResponse.builder()
                .success(true)
                .message("Connexion réussie")
                .token(token)
                .idUser(utilisateur.getIdUser().toString()) // Conversion UUID vers String
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .build();

            // Ajouter des informations spécifiques selon le rôle
            if (utilisateur instanceof com.iusjc.weschedule.models.Enseignant enseignant) {
                response.setPhone(enseignant.getPhone());
                response.setGrade(enseignant.getGrade());
            }

            log.info("Connexion REST réussie pour: {} ({})", 
                    utilisateur.getEmail(), utilisateur.getRole());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la connexion REST pour {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                AuthResponse.builder()
                    .success(false)
                    .message("Erreur interne du serveur")
                    .build()
            );
        }
    }

    /**
     * Endpoint pour vérifier la validité d'un token
     * GET /api/auth/verify
     */
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Token manquant ou invalide")
                        .build()
                );
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email != null && jwtService.isTokenValid(token, email)) {
                Optional<Utilisateur> userOptional = authService.findByEmail(email);
                if (userOptional.isPresent()) {
                    Utilisateur utilisateur = userOptional.get();
                    
                    AuthResponse response = AuthResponse.builder()
                        .success(true)
                        .message("Token valide")
                        .idUser(utilisateur.getIdUser().toString())
                        .email(utilisateur.getEmail())
                        .nom(utilisateur.getNom())
                        .prenom(utilisateur.getPrenom())
                        .role(utilisateur.getRole())
                        .build();

                    if (utilisateur instanceof com.iusjc.weschedule.models.Enseignant enseignant) {
                        response.setPhone(enseignant.getPhone());
                        response.setGrade(enseignant.getGrade());
                    }

                    return ResponseEntity.ok(response);
                }
            }

            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .success(false)
                    .message("Token invalide ou expiré")
                    .build()
            );

        } catch (Exception e) {
            log.error("Erreur lors de la vérification du token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors de la vérification du token")
                    .build()
            );
        }
    }

    /**
     * Endpoint pour rafraîchir un token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Token manquant")
                        .build()
                );
            }

            String oldToken = authHeader.substring(7);
            String email = jwtService.extractUsername(oldToken);

            if (email != null) {
                Optional<Utilisateur> userOptional = authService.findByEmail(email);
                if (userOptional.isPresent()) {
                    Utilisateur utilisateur = userOptional.get();
                    String newToken = jwtService.generateToken(utilisateur);

                    return ResponseEntity.ok(
                        AuthResponse.builder()
                            .success(true)
                            .message("Token rafraîchi")
                            .token(newToken)
                            .idUser(utilisateur.getIdUser().toString())
                            .email(utilisateur.getEmail())
                            .nom(utilisateur.getNom())
                            .prenom(utilisateur.getPrenom())
                            .role(utilisateur.getRole())
                            .build()
                    );
                }
            }

            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .success(false)
                    .message("Impossible de rafraîchir le token")
                    .build()
            );

        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors du rafraîchissement")
                    .build()
            );
        }
    }
}