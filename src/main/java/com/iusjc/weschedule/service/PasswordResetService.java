package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.PasswordResetToken;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.PasswordResetTokenRepository;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final long RESET_TOKEN_EXPIRY_MINUTES = 15;

    /**
     * Initie une réinitialisation de mot de passe avec token UUID
     * Génère un token unique et l'envoie par email
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Demande de réinitialisation pour email: {}", email);
        
        // Valider le format de l'email
        if (!isValidEmail(email)) {
            log.warn("Format d'email invalide: {}", email);
            // On ne révèle pas l'erreur pour des raisons de sécurité
            return;
        }
        
        Optional<Utilisateur> userOptional = utilisateurRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            log.warn("Email non trouvé en base: {}", email);
            // On ne révèle pas si l'email existe ou non pour des raisons de sécurité
            return;
        }

        Utilisateur utilisateur = userOptional.get();
        log.info("Utilisateur trouvé: {} {}", utilisateur.getPrenom(), utilisateur.getNom());
        
        // Supprimer tous les anciens tokens pour cet utilisateur
        resetTokenRepository.deleteByUtilisateur(utilisateur);
        log.info("Anciens tokens supprimés pour: {}", email);
        
        // Générer un nouveau token UUID
        String resetToken = generateUniqueToken();
        log.info("Nouveau token généré: {}", resetToken);
        
        // Créer et sauvegarder le token
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(resetToken);
        token.setUtilisateur(utilisateur);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        
        PasswordResetToken savedToken = resetTokenRepository.save(token);
        log.info("Token sauvegardé avec ID: {}, expire le: {}", savedToken.getId(), savedToken.getExpiryDate());
        
        // Envoyer l'email avec le lien
        try {
            String fullName = utilisateur.getPrenom() + " " + utilisateur.getNom();
            emailService.sendPasswordResetEmail(utilisateur.getEmail(), fullName, resetToken);
            log.info("Email de réinitialisation envoyé à: {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }

    /**
     * Valide un token de réinitialisation
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String resetToken) {
        log.info("Validation du token: {}", resetToken);
        
        if (resetToken == null || resetToken.trim().isEmpty()) {
            log.warn("Token vide ou null");
            return false;
        }
        
        Optional<PasswordResetToken> tokenOptional = resetTokenRepository.findByToken(resetToken);
        
        if (tokenOptional.isEmpty()) {
            log.warn("Token non trouvé en base: {}", resetToken);
            return false;
        }

        PasswordResetToken token = tokenOptional.get();
        boolean isExpired = token.isExpired();
        boolean isUsed = token.isUsed();
        
        log.info("Token trouvé - Expiré: {}, Utilisé: {}, Utilisateur: {}", 
                isExpired, isUsed, token.getUtilisateur().getEmail());
        
        return !isExpired && !isUsed;
    }
    
    /**
     * Récupère l'email et le hash du mot de passe actuel associés au token
     */
    @Transactional(readOnly = true)
    public String[] getEmailAndPasswordFromToken(String resetToken) {
        log.info("Récupération des données pour le token: {}", resetToken);
        
        Optional<PasswordResetToken> tokenOptional = resetTokenRepository.findByToken(resetToken);
        
        if (tokenOptional.isEmpty()) {
            log.warn("Token non trouvé: {}", resetToken);
            return null;
        }
        
        PasswordResetToken token = tokenOptional.get();
        
        if (token.isExpired() || token.isUsed()) {
            log.warn("Token invalide - Expiré: {}, Utilisé: {}", token.isExpired(), token.isUsed());
            return null;
        }
        
        String email = token.getUtilisateur().getEmail();
        String currentPasswordHash = token.getUtilisateur().getMotDePasse();
        log.info("Données trouvées pour le token - Email: {}", email);
        return new String[]{email, currentPasswordHash};
    }

    /**
     * Réinitialise le mot de passe via le token UUID
     * Retourne l'email pour la connexion automatique
     */
    @Transactional
    public String resetPassword(String resetToken, String newPassword) {
        log.info("Réinitialisation de mot de passe demandée");
        
        // Validation des paramètres
        if (resetToken == null || resetToken.trim().isEmpty()) {
            throw new RuntimeException("Token de réinitialisation invalide");
        }
        
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }
        
        // Rechercher le token
        Optional<PasswordResetToken> tokenOptional = resetTokenRepository.findByToken(resetToken);
        
        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("Lien de réinitialisation invalide");
        }

        PasswordResetToken token = tokenOptional.get();
        
        // Vérifications du token
        if (token.isExpired()) {
            throw new RuntimeException("Le lien de réinitialisation a expiré");
        }
        
        if (token.isUsed()) {
            throw new RuntimeException("Ce lien a déjà été utilisé");
        }
        
        // Récupérer l'utilisateur
        Utilisateur utilisateur = token.getUtilisateur();
        
        // Encoder et mettre à jour le mot de passe
        String encodedPassword = passwordEncoder.encode(newPassword);
        utilisateur.setMotDePasse(encodedPassword);
        utilisateurRepository.save(utilisateur);
        
        // Marquer le token comme utilisé
        token.setUsed(true);
        resetTokenRepository.save(token);
        
        log.info("Mot de passe réinitialisé avec succès pour: {}", utilisateur.getEmail());
        
        // Retourner l'email pour la connexion automatique
        return utilisateur.getEmail();
    }

    /**
     * Génère un token UUID unique
     */
    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (resetTokenRepository.findByToken(token).isPresent());
        
        return token;
    }
    
    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
