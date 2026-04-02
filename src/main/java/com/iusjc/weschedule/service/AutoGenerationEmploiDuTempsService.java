package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'auto-génération d'emploi du temps basé sur :
 * - CSP (Constraint Satisfaction Problem)
 * - Approche gloutonne (greedy algorithm)
 * - Backtracking pour résoudre les blocages
 */
@Service
@Slf4j
@Transactional
public class AutoGenerationEmploiDuTempsService {

    @Autowired
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private DisponibiliteEnseignantRepository disponibiliteRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private EmploiDuTempsService emploiDuTempsService;

    /**
     * Génère automatiquement l'emploi du temps pour une classe
     * VERSION OPTIMISÉE pour réduire le temps de génération
     */
    public Map<String, Object> genererEmploiDuTemps(UUID emploiDuTempsId, Integer semestre) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                    .orElseThrow(() -> new IllegalArgumentException("Emploi du temps non trouvé"));

            int seancesAjoutees = 0;
            int seancesEchouees = 0;

            // Générer tous les candidats une seule fois
            List<SeanceCandidate> candidates = genererCandidats(emploiDuTemps, semestre);
            
            if (candidates.isEmpty()) {
                log.info("Aucun cours à programmer pour ce semestre");
            } else {
                // Trier par ordre de priorité (une seule fois)
                candidates.sort(Comparator
                        .comparingDouble(SeanceCandidate::getPriorityScore).reversed()
                        .thenComparingInt((a) -> -a.getCours().getDureeRestante()));

                log.info("Tentative de placement de {} cours", candidates.size());

                // Essayer de placer chaque cours une seule fois
                for (SeanceCandidate candidate : candidates) {
                    if (essayerAffecterSeance(emploiDuTemps, candidate)) {
                        seancesAjoutees++;
                        log.info("✓ Cours {} placé avec succès", candidate.getCours().getIntitule());
                    } else {
                        seancesEchouees++;
                        log.warn("✗ Impossible de placer le cours {}", candidate.getCours().getIntitule());
                    }
                }
            }
            
            // Remplir les trous avec TPE
            log.info("=== REMPLISSAGE DES TROUS AVEC TPE ===");
            int seancesTPE = remplirTrousAvecTPE(emploiDuTemps);
            log.info("Séances TPE ajoutées : {}", seancesTPE);

            result.put("success", true);
            result.put("message", "Génération terminée avec succès");
            result.put("seancesAjoutees", seancesAjoutees);
            result.put("seancesEchouees", seancesEchouees);
            result.put("seancesTPE", seancesTPE);

        } catch (Exception e) {
            log.error("Erreur lors de la génération", e);
            result.put("success", false);
            result.put("message", "Erreur: " + e.getMessage());
        }

        return result;
    }
    
    /**
     * Remplit les trous dans l'emploi du temps avec des séances TPE
     * Crée un cours TPE générique si nécessaire
     * HORAIRES: 8h-17h (pas de séances 17h-18h)
     * TPE: SANS salle assignée
     * JOURS: Lundi à Samedi (6 jours)
     */
    private int remplirTrousAvecTPE(EmploiDuTempsClasse emploiDuTemps) {
        int seancesTPEAjoutees = 0;
        LocalDate lundi = emploiDuTemps.getDateDebut();
        
        // Créer ou récupérer un cours TPE générique pour cette classe
        Cours coursTPE = getOrCreateCoursTPE(emploiDuTemps.getClasse());
        
        // Parcourir tous les jours de la semaine (Lundi à Samedi = 6 jours)
        for (int jour = 0; jour < 6; jour++) {
            LocalDate date = lundi.plusDays(jour);
            
            // Parcourir toutes les heures de 8h à 17h (pas 17h-18h)
            LocalTime currentHeure = LocalTime.of(8, 0);
            
            while (currentHeure.isBefore(LocalTime.of(17, 0))) {
                // Trouver le prochain créneau libre
                LocalTime debutTrou = currentHeure;
                LocalTime finTrou = debutTrou.plusHours(1);
                
                // Vérifier si c'est la pause déjeuner (sauf le samedi)
                if (jour < 5 && isOverlappingLunchBreak(debutTrou, finTrou)) {
                    currentHeure = LocalTime.of(13, 0);
                    continue;
                }
                
                // Vérifier si le créneau est libre
                List<SeanceClasse> seancesExistantes = seanceRepository
                        .findByEmploiDuTempsAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                                emploiDuTemps, date, finTrou, debutTrou);
                
                if (seancesExistantes.isEmpty()) {
                    // Créneau libre, créer une séance TPE
                    SeanceClasse seanceTPE = new SeanceClasse();
                    seanceTPE.setEmploiDuTemps(emploiDuTemps);
                    seanceTPE.setDate(date);
                    seanceTPE.setHeureDebut(debutTrou);
                    seanceTPE.setHeureFin(finTrou);
                    seanceTPE.setCours(coursTPE); // Utiliser le cours TPE générique
                    seanceTPE.setEnseignant(null);
                    seanceTPE.setJourSemaine(jour);
                    seanceTPE.setRemarques("TPE - Travail Personnel de l'Étudiant");
                    seanceTPE.setSalle(null); // TPE SANS salle
                    
                    seanceRepository.save(seanceTPE);
                    seancesTPEAjoutees++;
                    log.debug("TPE ajouté : {} de {} à {} (sans salle)", date, debutTrou, finTrou);
                }
                
                currentHeure = currentHeure.plusHours(1);
            }
        }
        
        return seancesTPEAjoutees;
    }
    
    /**
     * Récupère ou crée un cours TPE générique pour une classe
     */
    private Cours getOrCreateCoursTPE(Classe classe) {
        // Chercher un cours TPE existant pour cette classe
        List<Cours> coursDeLaClasse = coursRepository.findByClasse(classe);
        for (Cours cours : coursDeLaClasse) {
            if (cours.getTypeCours() == com.iusjc.weschedule.enums.TypeCours.TPE) {
                return cours;
            }
        }
        
        // Créer un nouveau cours TPE générique
        Cours coursTPE = new Cours();
        coursTPE.setIntitule("TPE - Travail Personnel de l'Étudiant");
        coursTPE.setTypeCours(com.iusjc.weschedule.enums.TypeCours.TPE);
        coursTPE.setDureeTotal(0); // Pas de durée totale pour TPE
        coursTPE.setDureeRestante(0);
        coursTPE.setDureeSeanceParJour(1);
        coursTPE.setClasse(classe);
        coursTPE.setUe(null); // TPE n'est pas lié à une UE
        coursTPE.setEnseignant(null);
        coursTPE.setDescription("Cours générique pour les séances de Travail Personnel de l'Étudiant");
        
        coursRepository.save(coursTPE);
        log.info("Cours TPE générique créé pour la classe {}", classe.getNom());
        
        return coursTPE;
    }

    /**
     * Génère la liste des candidats (matière/type) possibles
     * CSP: Filtre basé sur le volume restant et faisabilité
     */
    private List<SeanceCandidate> genererCandidats(EmploiDuTempsClasse emploiDuTemps, Integer semestre) {
        List<SeanceCandidate> candidates = new ArrayList<>();

        log.info("=== DEBUT GENERATION CANDIDATS ===");
        log.info("Classe : {}", emploiDuTemps.getClasse().getNom());
        log.info("Semestre : {}", semestre);

        // Récupérer tous les cours de la classe filtrés par semestre
        List<Cours> coursDeLaClasse = coursRepository.findByClasse(emploiDuTemps.getClasse());
        log.info("Cours trouvés pour la classe : {}", coursDeLaClasse.size());
        
        List<Cours> coursSemestre = coursDeLaClasse.stream()
                .filter(c -> c.getUe() != null && c.getUe().getSemestre() != null && c.getUe().getSemestre().equals(semestre))
                .toList();
        log.info("Cours du semestre {} : {}", semestre, coursSemestre.size());

        for (Cours cours : coursSemestre) {
            log.info("Analyse cours : {} | Enseignant : {} | DureeRestante : {} | DureeSeance : {}", 
                cours.getIntitule(), 
                cours.getEnseignant() != null ? cours.getEnseignant().getNom() : "NULL",
                cours.getDureeRestante(),
                cours.getDureeSeanceParJour());
            
            // CSP: Vérifier le volume restant
            if (cours.getDureeRestante() == null || cours.getDureeRestante() <= 0) {
                log.info("  -> REJETÉ : Volume restant nul ou négatif");
                continue;
            }

            // CSP: Vérifier la durée de séance est définie
            if (cours.getDureeSeanceParJour() == null || cours.getDureeSeanceParJour() <= 0) {
                log.warn("  -> REJETÉ : Cours {} n'a pas de dureeSeanceParJour définie", cours.getIntitule());
                continue;
            }

            // CSP: Vérifier que le volume restant ≥ durée de séance
            if (cours.getDureeRestante() < cours.getDureeSeanceParJour()) {
                log.info("  -> REJETÉ : Volume insuffisant pour {}: {} < {}", 
                    cours.getIntitule(), cours.getDureeRestante(), cours.getDureeSeanceParJour());
                continue;
            }
            
            // CSP: Vérifier que l'enseignant est assigné
            if (cours.getEnseignant() == null) {
                log.warn("  -> REJETÉ : Pas d'enseignant assigné");
                continue;
            }

            // CSP: Vérifier les disponibilités de l'enseignant
            if (!hasTeacherAvailability(cours, emploiDuTemps)) {
                log.warn("  -> REJETÉ : Enseignant {} n'a pas de disponibilité", 
                    cours.getEnseignant().getNom());
                continue;
            }

            // CSP: Vérifier qu'il existe au moins une salle disponible
            if (!hasRoomAvailable(emploiDuTemps)) {
                log.warn("  -> REJETÉ : Aucune salle disponible dans le système");
                continue;
            }

            // Calcul du score de priorité (glouton)
            double priorityScore = calculatePriorityScore(cours, emploiDuTemps);
            candidates.add(new SeanceCandidate(cours, priorityScore));
            log.info("  -> ACCEPTÉ comme candidat (score: {})", priorityScore);
        }

        log.info("=== FIN GENERATION CANDIDATS : {} candidats ===", candidates.size());
        return candidates;
    }

    /**
     * Calcule le score de priorité pour la sélection gloutonne (VERSION SIMPLIFIÉE)
     */
    private double calculatePriorityScore(Cours cours, EmploiDuTempsClasse emploiDuTemps) {
        double score = 0;

        // Urgence: volume restant élevé
        if (cours.getDureeTotal() != null && cours.getDureeTotal() > 0) {
            score += (double) cours.getDureeRestante() / cours.getDureeTotal() * 100;
        }

        // Bonus pour les cours avec moins d'heures (plus faciles à placer)
        score += (20 - cours.getDureeSeanceParJour()) * 5;

        return score;
    }

    /**
     * Vérifie si un créneau est disponible (CSP)
     */
    private boolean isSlotAvailable(Cours cours, LocalDate date, LocalTime debut, LocalTime fin, 
                                    EmploiDuTempsClasse emploiDuTemps) {
        // CSP: Vérifier que la classe n'a rien à cette heure
        List<SeanceClasse> seancesClasse = seanceRepository
                .findByEmploiDuTempsAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                        emploiDuTemps, date, fin, debut);
        if (!seancesClasse.isEmpty()) {
            return false;
        }

        // CSP: Vérifier que l'enseignant est disponible à cette date
        List<DisponibiliteEnseignant> disponibilites = disponibiliteRepository
                .findByEnseignant(cours.getEnseignant());
        
        boolean hasAvailability = disponibilites.stream()
                .anyMatch(d -> !date.isBefore(d.getDateDebut()) && !date.isAfter(d.getDateFin()));
        
        if (!hasAvailability) {
            return false;
        }

        // CSP: Vérifier qu'une salle est disponible
        List<Salle> salles = salleRepository.findAll();
        for (Salle salle : salles) {
            List<SeanceClasse> seancesSalle = seanceRepository
                    .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                            salle, date, fin, debut);
            if (seancesSalle.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Essaye d'affecter une séance (VERSION OPTIMISÉE)
     * CONTRAINTE 1: Un cours ne peut être programmé que sur UN SEUL jour par semaine
     * CONTRAINTE 2: Un CM et son TP ne peuvent pas être programmés la même semaine
     * HORAIRES: 8h-17h
     */
    private boolean essayerAffecterSeance(EmploiDuTempsClasse emploiDuTemps, SeanceCandidate candidate) {
        Cours cours = candidate.getCours();
        int dureeSeanceParJour = cours.getDureeSeanceParJour();
        LocalDate lundi = emploiDuTemps.getDateDebut();

        // CONTRAINTE 1: Vérifier si le cours est déjà programmé
        List<SeanceClasse> seancesExistantes = seanceRepository.findByEmploiDuTempsAndCours(emploiDuTemps, cours);
        if (!seancesExistantes.isEmpty()) {
            return false;
        }
        
        // CONTRAINTE 2: Vérifier qu'un cours de la même UE n'est pas déjà programmé
        if (cours.getUe() != null) {
            List<Cours> coursMemeUE = coursRepository.findByClasse(emploiDuTemps.getClasse()).stream()
                .filter(c -> c.getUe() != null && c.getUe().getIdUE().equals(cours.getUe().getIdUE()))
                .filter(c -> !c.getIdCours().equals(cours.getIdCours()))
                .toList();
            
            for (Cours autreCoursUE : coursMemeUE) {
                List<SeanceClasse> seancesAutreCours = seanceRepository.findByEmploiDuTempsAndCours(emploiDuTemps, autreCoursUE);
                if (!seancesAutreCours.isEmpty()) {
                    return false;
                }
            }
        }

        // OPTIMISATION: Essayer seulement les créneaux du matin (8h-12h) et après-midi (13h-17h)
        // au lieu de toutes les heures
        List<LocalTime> heuresDebut = List.of(
            LocalTime.of(8, 0),   // Matin
            LocalTime.of(13, 0)   // Après-midi
        );

        // Essayer chaque jour
        for (int jour = 0; jour < 5; jour++) {
            LocalDate date = lundi.plusDays(jour);
            
            // Essayer les créneaux optimisés
            for (LocalTime debut : heuresDebut) {
                if (tryPlacerSeancesSuccessives(emploiDuTemps, cours, date, debut, dureeSeanceParJour)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Tente de placer des séances successives pour un cours à partir d'un créneau donné
     * Les séances sont de 1h chacune, placées successivement (sans trou) 
     * jusqu'à atteindre la dureeSeanceParJour (durée totale par jour)
     * CONTRAINTE: Toutes les séances d'un cours doivent être sur le MÊME JOUR
     * HORAIRES: 8h-17h (pas de séances 17h-18h)
     */
    private boolean tryPlacerSeancesSuccessives(EmploiDuTempsClasse emploiDuTemps, Cours cours, 
                                                 LocalDate dateDebut, LocalTime heureDebut, 
                                                 int dureeSeanceParJour) {
        int volumeRestant = cours.getDureeRestante();
        List<SeanceClasse> seancesACreer = new ArrayList<>();
        LocalDate lundi = emploiDuTemps.getDateDebut();
        LocalDate currentDate = dateDebut;
        LocalTime currentHeure = heureDebut;
        int volumeTraite = 0;
        int jourIndex = (int) (currentDate.toEpochDay() - lundi.toEpochDay());
        
        // CONTRAINTE: Toutes les séances doivent être sur le même jour
        // Chaque séance dure 1h, on en crée jusqu'à atteindre dureeSeanceParJour
        while (volumeTraite < volumeRestant && volumeTraite < dureeSeanceParJour) {
            LocalTime heureFin = currentHeure.plusHours(1); // Séances de 1h
            
            // Si la séance dépasse 17h, on ne peut plus placer de séances ce jour
            if (heureFin.isAfter(LocalTime.of(17, 0))) {
                break; // Arrêter, on ne peut pas continuer
            }
            
            // Si la séance chevauche la pause déjeuner, passer à 13h
            if (isOverlappingLunchBreak(currentHeure, heureFin)) {
                currentHeure = LocalTime.of(13, 0);
                continue;
            }
            
            // Vérifier que le créneau est disponible
            if (!isSlotAvailable(cours, currentDate, currentHeure, heureFin, emploiDuTemps)) {
                // Créneau occupé, on ne peut pas continuer (séances doivent être successives)
                break;
            }
            
            // Créer la séance de 1h (sans la sauvegarder encore)
            SeanceClasse seance = new SeanceClasse();
            seance.setEmploiDuTemps(emploiDuTemps);
            seance.setDate(currentDate);
            seance.setHeureDebut(currentHeure);
            seance.setHeureFin(heureFin);
            seance.setCours(cours);
            seance.setEnseignant(cours.getEnseignant());
            seance.setJourSemaine(jourIndex);
            
            seancesACreer.add(seance);
            volumeTraite += 1; // Chaque séance = 1h
            
            // Passer à la séance suivante (immédiatement après, sans trou)
            currentHeure = heureFin;
        }
        
        // Si on n'a pas pu placer au moins une séance, échec
        if (seancesACreer.isEmpty()) {
            return false;
        }
        
        // Vérifier qu'on a bien atteint la durée minimale requise par jour
        // OU qu'on a épuisé le volume restant
        if (volumeTraite < dureeSeanceParJour && volumeTraite < volumeRestant) {
            // On n'a pas pu placer toutes les séances nécessaires pour ce jour
            // (pas assez de place dans la journée)
            log.debug("Impossible de placer {} séances successives pour {} à partir de {} (seulement {} placées)", 
                dureeSeanceParJour, cours.getIntitule(), heureDebut, volumeTraite);
            return false;
        }
        
        // Sauvegarder l'état actuel pour backtracking
        Integer volumeAncien = cours.getDureeRestante();
        
        try {
            // Trouver une salle disponible pour toutes les séances
            List<Salle> salles = salleRepository.findAll();
            Salle salleTrouvee = null;
            
            for (Salle salle : salles) {
                boolean salleDisponiblePourTout = true;
                
                // Vérifier que la salle est disponible pour toutes les séances
                for (SeanceClasse seance : seancesACreer) {
                    List<SeanceClasse> seancesSalle = seanceRepository
                            .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                                    salle, seance.getDate(), seance.getHeureFin(), seance.getHeureDebut());
                    if (!seancesSalle.isEmpty()) {
                        salleDisponiblePourTout = false;
                        break;
                    }
                }
                
                if (salleDisponiblePourTout) {
                    salleTrouvee = salle;
                    break;
                }
            }
            
            if (salleTrouvee == null) {
                return false; // Aucune salle disponible
            }
            
            // Sauvegarder toutes les séances
            for (SeanceClasse seance : seancesACreer) {
                seance.setSalle(salleTrouvee);
                seanceRepository.save(seance);
                log.debug("✓ Séance ajoutée: {} le {} de {} à {}", 
                    cours.getIntitule(), seance.getDate(), seance.getHeureDebut(), seance.getHeureFin());
            }
            
            // Mettre à jour le volume restant
            cours.setDureeRestante(cours.getDureeRestante() - volumeTraite);
            coursRepository.save(cours);
            
            log.info("✓ {} séances de 1h successives créées pour {} (volume traité: {}h sur le jour {})", 
                seancesACreer.size(), cours.getIntitule(), volumeTraite, currentDate);
            
            return true;
            
        } catch (Exception e) {
            // Backtracking: Restaurer l'état
            cours.setDureeRestante(volumeAncien);
            coursRepository.save(cours);
            log.warn("Backtracking: Annulation de {} séances pour {}", seancesACreer.size(), cours.getIntitule());
            return false;
        }
    }
    
    /**
     * Vérifie si un créneau chevauche la pause déjeuner (12h-13h)
     */
    private boolean isOverlappingLunchBreak(LocalTime debut, LocalTime fin) {
        LocalTime lunchStart = LocalTime.of(12, 0);
        LocalTime lunchEnd = LocalTime.of(13, 0);
        
        // Chevauchement si : debut < 13h ET fin > 12h
        return debut.isBefore(lunchEnd) && fin.isAfter(lunchStart);
    }

    /**
     * CSP: Vérifie si l'enseignant a des disponibilités
     */
    private boolean hasTeacherAvailability(Cours cours, EmploiDuTempsClasse emploiDuTemps) {
        List<DisponibiliteEnseignant> dispo = disponibiliteRepository
                .findByEnseignant(cours.getEnseignant());
        return !dispo.isEmpty();
    }

    /**
     * CSP: Vérifie s'il y a au moins une salle disponible
     */
    private boolean hasRoomAvailable(EmploiDuTempsClasse emploiDuTemps) {
        List<Salle> salles = salleRepository.findAll();
        return !salles.isEmpty();
    }

    /**
     * Classe interne pour représenter un candidat de séance
     */
    private static class SeanceCandidate {
        private final Cours cours;
        private final double priorityScore;

        public SeanceCandidate(Cours cours, double priorityScore) {
            this.cours = cours;
            this.priorityScore = priorityScore;
        }

        public Cours getCours() {
            return cours;
        }

        public double getPriorityScore() {
            return priorityScore;
        }
    }
}
