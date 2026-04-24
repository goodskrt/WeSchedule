package com.iusjc.weschedule.service;

import com.iusjc.weschedule.dto.RegisterRequest;
import com.iusjc.weschedule.dto.AuthResponse;
import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Enregistrement d'un nouvel utilisateur (Backend only)
     */
    public AuthResponse register(RegisterRequest request, Role role) {
        try {
            // Vérifier si l'email existe déjà
            if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Cet email est déjà utilisé")
                        .build();
            }

            // Créer l'utilisateur approprié selon le rôle
            // Seuls ADMINISTRATEUR et ENSEIGNANT sont autorisés
            Utilisateur utilisateur;

            if (role == Role.ADMINISTRATEUR) {
                utilisateur = new Administrateur();
            } else if (role == Role.ENSEIGNANT) {
                utilisateur = new Enseignant();
            } else {
                return AuthResponse.builder()
                        .success(false)
                        .message("Rôle invalide. Seuls administrateurs et enseignants sont autorisés")
                        .build();
            }

            // Remplir les données communes
            utilisateur.setNom(request.getNom());
            utilisateur.setPrenom(request.getPrenom());
            utilisateur.setEmail(request.getEmail());
            utilisateur.setPhone(request.getPhone());
            // Hasher le mot de passe
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
            utilisateur.setRole(role);

            // Sauvegarder en fonction du rôle
            switch (role) {
                case ADMINISTRATEUR:
                    administrateurRepository.save((Administrateur) utilisateur);
                    break;
                case ENSEIGNANT:
                    enseignantRepository.save((Enseignant) utilisateur);
                    break;
            }

            log.info("Nouvel utilisateur enregistré : {} {}", request.getNom(), request.getPrenom());

            return AuthResponse.builder()
                    .idUser(utilisateur.getIdUser().toString()) // Conversion UUID vers String
                    .email(utilisateur.getEmail())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .role(utilisateur.getRole())
                    .success(true)
                    .message("Inscription réussie")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors de l'inscription : " + e.getMessage())
                    .build();
        }
    }

    /**
     * Authentification d'un utilisateur
     */
    public AuthResponse authenticate(String email, String motDePasse) {
        try {
            log.info("=== DÉBUT AUTHENTIFICATION ===");
            log.info("Email: {}", email);
            log.info("Mot de passe reçu (longueur): {}", motDePasse != null ? motDePasse.length() : "null");
            
            Optional<Utilisateur> optionalUser = utilisateurRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé pour email: {}", email);
                return AuthResponse.builder()
                        .success(false)
                        .message("Email ou mot de passe incorrect")
                        .build();
            }

            Utilisateur utilisateur = optionalUser.get();
            log.info("Utilisateur trouvé pour email: {}", email);

            // Vérifier le mot de passe
            boolean passwordMatches = passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse());
            log.info("Vérification mot de passe: {}", passwordMatches);
            
            if (!passwordMatches) {
                log.warn("Mot de passe incorrect pour: {}", email);
                return AuthResponse.builder()
                        .success(false)
                        .message("Email ou mot de passe incorrect")
                        .build();
            }

            log.info("Authentification réussie pour: {}", email);

            return AuthResponse.builder()
                    .idUser(utilisateur.getIdUser().toString()) // Conversion UUID vers String
                    .email(utilisateur.getEmail())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .role(utilisateur.getRole())
                    .success(true)
                    .message("Authentification réussie")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de l'authentification pour {}: {}", email, e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Erreur lors de l'authentification")
                    .build();
        }
    }

    /**
     * Obtenir un utilisateur par email
     */
    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }
}
