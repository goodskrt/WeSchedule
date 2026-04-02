package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class EnseignantService {

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private UERepository ueRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DisponibiliteEnseignantRepository disponibiliteRepository;
    
    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final SecureRandom random = new SecureRandom();

    public List<Enseignant> getAllEnseignants() {
        log.info("Récupération de tous les enseignants");
        List<Enseignant> enseignants = enseignantRepository.findAllWithUEs();
        log.info("Nombre d'enseignants trouvés: {}", enseignants.size());
        return enseignants;
    }
    
    public long countEnseignants() {
        return enseignantRepository.count();
    }

    public Optional<Enseignant> getEnseignantById(UUID id) {
        return enseignantRepository.findById(id);
    }

    public Enseignant getEnseignantWithDetails(UUID id) {
        return enseignantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé"));
    }

    public List<UE> getAllUEs() {
        return ueRepository.findAll();
    }

    public List<UE> getUEsEnseignant(UUID enseignantId) {
        Optional<Enseignant> enseignantOpt = enseignantRepository.findByIdWithUEs(enseignantId);
        if (enseignantOpt.isEmpty() || enseignantOpt.get().getUesEnseignees() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(enseignantOpt.get().getUesEnseignees());
    }

    public int countDisponibilites(UUID enseignantId) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId).orElse(null);
        if (enseignant == null) return 0;
        return disponibiliteRepository.findByEnseignant(enseignant).size();
    }

    /**
     * Créer un nouvel enseignant avec envoi d'email
     */
    public Enseignant creerEnseignant(String nom, String prenom, String email, String phone, String grade, List<UUID> ueIds) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(email.toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Générer un mot de passe
        String generatedPassword = generateRandomPassword(12);

        // Créer l'enseignant
        Enseignant enseignant = new Enseignant();
        enseignant.setNom(nom.trim());
        enseignant.setPrenom(prenom != null ? prenom.trim() : "");
        enseignant.setEmail(email.trim().toLowerCase());
        enseignant.setPhone(phone != null ? phone.trim() : null);
        enseignant.setGrade(grade != null ? grade.trim() : null);
        enseignant.setMotDePasse(passwordEncoder.encode(generatedPassword));
        enseignant.setRole(Role.ENSEIGNANT);

        // Assigner les UEs
        if (ueIds != null && !ueIds.isEmpty()) {
            Set<UE> ues = new HashSet<>(ueRepository.findAllById(ueIds));
            enseignant.setUesEnseignees(ues);
        }

        Enseignant saved = enseignantRepository.save(enseignant);

        // Envoyer l'email
        try {
            sendWelcomeEmail(saved, generatedPassword);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de bienvenue", e);
        }

        return saved;
    }

    /**
     * Mettre à jour un enseignant
     */
    public Enseignant updateEnseignant(UUID id, String nom, String prenom, String email, String phone, String grade, List<UUID> ueIds) {
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé"));

        // Vérifier si le nouvel email n'est pas déjà utilisé par un autre utilisateur
        if (!enseignant.getEmail().equalsIgnoreCase(email)) {
            Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(email.toLowerCase());
            if (existingUser.isPresent() && !existingUser.get().getIdUser().equals(id)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur");
            }
        }

        enseignant.setNom(nom.trim());
        enseignant.setPrenom(prenom != null ? prenom.trim() : "");
        enseignant.setEmail(email.trim().toLowerCase());
        enseignant.setPhone(phone != null ? phone.trim() : null);
        enseignant.setGrade(grade != null ? grade.trim() : null);

        // Mettre à jour les UEs
        if (ueIds != null) {
            Set<UE> ues = new HashSet<>(ueRepository.findAllById(ueIds));
            enseignant.setUesEnseignees(ues);
        } else {
            enseignant.setUesEnseignees(new HashSet<>());
        }

        return enseignantRepository.save(enseignant);
    }

    /**
     * Mettre à jour le profil de l'enseignant (sans les UEs)
     */
    public Enseignant updateProfilEnseignant(UUID id, String nom, String prenom, String email, String phone, String grade) {
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé"));

        // Vérifier si le nouvel email n'est pas déjà utilisé par un autre utilisateur
        if (!enseignant.getEmail().equalsIgnoreCase(email)) {
            Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(email.toLowerCase());
            if (existingUser.isPresent() && !existingUser.get().getIdUser().equals(id)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur");
            }
        }

        enseignant.setNom(nom.trim());
        enseignant.setPrenom(prenom != null ? prenom.trim() : "");
        enseignant.setEmail(email.trim().toLowerCase());
        enseignant.setPhone(phone != null ? phone.trim() : null);
        enseignant.setGrade(grade != null ? grade.trim() : null);

        return enseignantRepository.save(enseignant);
    }

    /**
     * Changer le mot de passe de l'enseignant
     */
    public void changerMotDePasse(UUID id, String ancienMotDePasse, String nouveauMotDePasse) {
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(ancienMotDePasse, enseignant.getMotDePasse())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }

        // Mettre à jour le mot de passe
        enseignant.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        enseignantRepository.save(enseignant);
        log.info("Mot de passe changé pour l'enseignant {}", enseignant.getEmail());
    }

    /**
     * Supprimer un enseignant
     */
    public void supprimerEnseignant(UUID id) {
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé"));
        
        // Supprimer les relations avec les UEs (nettoyer les associations ManyToMany)
        enseignant.setUesEnseignees(new HashSet<>());
        enseignant.setEcoles(new HashSet<>());
        enseignant.setSpecialites(new HashSet<>());
        
        // Dissocier les cours sans les supprimer
        coursRepository.detachFromEnseignant(id);
        
        // Supprimer les créneaux de disponibilité en cascades
        if (enseignant.getDisponibilites() != null && !enseignant.getDisponibilites().isEmpty()) {
            // Supprimer explicitement tous les créneaux avant les disponibilités
            for (DisponibiliteEnseignant disponibilite : new HashSet<>(enseignant.getDisponibilites())) {
                if (disponibilite.getCreneauxParJour() != null) {
                    disponibilite.getCreneauxParJour().clear();
                }
            }
            // Vider les disponibilités (orphanRemoval supprimera les orphans)
            enseignant.getDisponibilites().clear();
        }
        
        // Supprimer l'enseignant
        enseignantRepository.delete(enseignant);
        log.info("Enseignant {} supprimé avec succès", id);
    }

    /**
     * Réinitialiser le mot de passe et envoyer par email
     */
    public void resetPassword(UUID id) {
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé"));

        String newPassword = generateRandomPassword(12);
        enseignant.setMotDePasse(passwordEncoder.encode(newPassword));
        enseignantRepository.save(enseignant);

        // Envoyer l'email avec le nouveau mot de passe
        sendPasswordResetEmail(enseignant, newPassword);
        log.info("Mot de passe réinitialisé pour l'enseignant {}", enseignant.getEmail());
    }

    private String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private void sendWelcomeEmail(Enseignant enseignant, String password) {
        String subject = "🎓 Bienvenue sur WeSchedule - Vos identifiants de connexion";
        String body = String.format(
            "Bonjour %s %s,\n\n" +
            "═══════════════════════════════════════════════════════════\n" +
            "   BIENVENUE SUR WESCHEDULE\n" +
            "═══════════════════════════════════════════════════════════\n\n" +
            "Votre compte enseignant a été créé avec succès ! 🎉\n\n" +
            "───────────────────────────────────────────────────────────\n" +
            "📧 VOS IDENTIFIANTS DE CONNEXION\n" +
            "───────────────────────────────────────────────────────────\n\n" +
            "   Email       : %s\n" +
            "   Mot de passe: %s\n\n" +
            "───────────────────────────────────────────────────────────\n" +
            "🔗 ACCÉDER À LA PLATEFORME\n" +
            "───────────────────────────────────────────────────────────\n\n" +
            "   👉 http://localhost:8080/login\n\n" +
            "🔒 Pour des raisons de sécurité, nous vous recommandons de\n" +
            "   changer votre mot de passe dès votre première connexion.\n\n" +
            "═══════════════════════════════════════════════════════════\n\n" +
            "Cordialement,\n" +
            "L'équipe WeSchedule\n",
            enseignant.getPrenom(),
            enseignant.getNom(),
            enseignant.getEmail(),
            password
        );

        emailService.sendEmail(enseignant.getEmail(), subject, body);
    }

    private void sendPasswordResetEmail(Enseignant enseignant, String password) {
        String subject = "🔐 WeSchedule - Réinitialisation de votre mot de passe";
        String body = String.format(
            "Bonjour %s %s,\n\n" +
            "Votre mot de passe a été réinitialisé par l'administrateur.\n\n" +
            "───────────────────────────────────────────────────────────\n" +
            "📧 VOS NOUVEAUX IDENTIFIANTS\n" +
            "───────────────────────────────────────────────────────────\n\n" +
            "   Email       : %s\n" +
            "   Mot de passe: %s\n\n" +
            "🔒 Nous vous recommandons de changer ce mot de passe\n" +
            "   dès votre prochaine connexion.\n\n" +
            "Cordialement,\n" +
            "L'équipe WeSchedule\n",
            enseignant.getPrenom(),
            enseignant.getNom(),
            enseignant.getEmail(),
            password
        );

        emailService.sendEmail(enseignant.getEmail(), subject, body);
    }
}
