package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.TypeCours;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AutoGenerationEmploiDuTempsService {

    @Autowired
    private CoursRepository coursRepository;
    
    @Autowired
    private DisponibiliteEnseignantRepository disponibiliteRepository;
    
    @Autowired
    private PlageHoraireRepository plageHoraireRepository;
    
    @Autowired
    private CreneauDisponibiliteRepository creneauDisponibiliteRepository;
    
    @Autowired
    private EmploiDuTempsService emploiDuTempsService;
    
    @Autowired
    private SeanceClasseRepository seanceRepository;

    // Créneaux horaires disponibles (Lundi-Samedi, 8h-12h et 13h-17h)
    private static final LocalTime[] HEURES_DEBUT = {
        LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
        LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
    };
    
    private static final LocalTime[] HEURES_FIN = {
        LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
        LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
    };

    @Transactional
    public Map<String, Object> genererEmploiDuTemps(UUID emploiDuTempsId) {
        try {
            log.info("Début génération automatique pour emploi du temps {}", emploiDuTempsId);
            
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.getEmploiDuTempsById(emploiDuTempsId);
            Classe classe = emploiDuTemps.getClasse();
            
            // Récupérer tous les cours de la classe
            List<Cours> cours = coursRepository.findByClasse(classe);
            
            // Filtrer les cours qui ont encore des heures restantes
            List<Cours> coursAplanifier = cours.stream()
                .filter(c -> c.getDureeRestante() != null && c.getDureeRestante() > 0)
                .filter(c -> c.getEnseignant() != null)
                .collect(Collectors.toList());
            
            if (coursAplanifier.isEmpty()) {
                return Map.of("success", false, "message", "Aucun cours à planifier");
            }
            
            log.info("Nombre de cours à planifier: {}", coursAplanifier.size());
            
            int seancesAjoutees = 0;
            int seancesEchouees = 0;
            
            // Calculer le nombre total de créneaux disponibles par enseignant pour la semaine
            Map<UUID, Integer> creneauxDisponiblesParEnseignant = new HashMap<>();
            Map<UUID, List<CreneauDisponible>> creneauxParEnseignant = new HashMap<>();
            
            for (Cours c : coursAplanifier) {
                Enseignant enseignant = c.getEnseignant();
                if (!creneauxParEnseignant.containsKey(enseignant.getIdUser())) {
                    List<DisponibiliteEnseignant> disponibilites = disponibiliteRepository
                        .findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                            enseignant, emploiDuTemps.getDateFin(), emploiDuTemps.getDateDebut());
                    
                    List<CreneauDisponible> creneaux = getCreneauxDisponibles(
                        disponibilites, emploiDuTemps.getDateDebut(), emploiDuTemps.getDateFin());
                    
                    creneauxParEnseignant.put(enseignant.getIdUser(), creneaux);
                    creneauxDisponiblesParEnseignant.put(enseignant.getIdUser(), creneaux.size());
                    
                    log.info("Enseignant {} : {} créneaux disponibles", enseignant.getNom(), creneaux.size());
                }
            }
            
            // Continuer tant qu'il y a des disponibilités et des cours à planifier
            boolean continuer = true;
            int iteration = 0;
            int echecsConsecutifs = 0;
            
            while (continuer && iteration < 100) { // Limite de sécurité
                iteration++;
                boolean progresThisIteration = false;
                
                log.info("=== Itération {} ===", iteration);
                
                // Traiter jour par jour (Lundi à Samedi)
                LocalDate dateDebut = emploiDuTemps.getDateDebut();
                for (int jourOffset = 0; jourOffset < 7; jourOffset++) {
                    LocalDate dateJour = dateDebut.plusDays(jourOffset);
                    
                    // Ignorer le dimanche
                    if (dateJour.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        continue;
                    }
                    
                    // Mettre à jour la liste des cours à planifier
                    coursAplanifier = cours.stream()
                        .filter(c -> c.getDureeRestante() != null && c.getDureeRestante() > 0)
                        .filter(c -> c.getEnseignant() != null)
                        .collect(Collectors.toList());
                    
                    if (coursAplanifier.isEmpty()) {
                        log.info("Tous les cours sont planifiés");
                        continuer = false;
                        break;
                    }
                    
                    // Trouver les enseignants disponibles ce jour avec leurs cours
                    List<CoursEnseignantInfo> coursDisponiblesCeJour = new ArrayList<>();
                    
                    for (Cours c : coursAplanifier) {
                        Enseignant enseignant = c.getEnseignant();
                        List<CreneauDisponible> creneaux = creneauxParEnseignant.get(enseignant.getIdUser());
                        
                        // Vérifier si l'enseignant a des créneaux ce jour
                        boolean disponibleCeJour = creneaux.stream()
                            .anyMatch(cr -> cr.date.equals(dateJour));
                        
                        if (disponibleCeJour) {
                            CoursEnseignantInfo info = new CoursEnseignantInfo();
                            info.cours = c;
                            info.enseignant = enseignant;
                            info.nbCreneauxSemaine = creneauxParEnseignant.get(enseignant.getIdUser()).size();
                            info.dureeRestante = c.getDureeRestante();
                            coursDisponiblesCeJour.add(info);
                        }
                    }
                    
                    if (coursDisponiblesCeJour.isEmpty()) {
                        log.debug("{} - Aucun enseignant disponible", dateJour);
                        continue;
                    }
                    
                    // Trier par : 1) Moins de créneaux disponibles, 2) Plus de durée restante
                    coursDisponiblesCeJour.sort((a, b) -> {
                        int cmp = Integer.compare(a.nbCreneauxSemaine, b.nbCreneauxSemaine);
                        if (cmp != 0) return cmp;
                        return Integer.compare(b.dureeRestante, a.dureeRestante);
                    });
                    
                    // Prendre le premier (prioritaire)
                    CoursEnseignantInfo prioritaire = coursDisponiblesCeJour.get(0);
                    
                    log.info("{} - Enseignant prioritaire: {} ({} créneaux, {} heures restantes pour {})",
                        dateJour,
                        prioritaire.enseignant.getNom(),
                        prioritaire.nbCreneauxSemaine,
                        prioritaire.dureeRestante,
                        prioritaire.cours.getUe().getIntitule());
                    
                    // Assigner des séances pour cet enseignant ce jour
                    int heuresAssignees = assignerSeancesPourJour(
                        emploiDuTempsId,
                        prioritaire,
                        dateJour,
                        creneauxParEnseignant,
                        cours
                    );
                    
                    if (heuresAssignees > 0) {
                        seancesAjoutees += heuresAssignees;
                        progresThisIteration = true;
                        echecsConsecutifs = 0;
                    } else {
                        log.debug("{} - Aucune séance assignée pour {}", dateJour, prioritaire.enseignant.getNom());
                    }
                }
                
                // Si aucun progrès cette itération, incrémenter les échecs
                if (!progresThisIteration) {
                    echecsConsecutifs++;
                    seancesEchouees++;
                    log.warn("Itération {} sans progrès (échec {})", iteration, echecsConsecutifs);
                    
                    // Arrêter après 3 échecs consécutifs
                    if (echecsConsecutifs >= 3) {
                        log.info("Arrêt après {} échecs consécutifs", echecsConsecutifs);
                        continuer = false;
                    }
                } else {
                    continuer = true; // Il y a eu du progrès, continuer
                }
            }
            
            log.info("Génération terminée après {} itérations: {} séances ajoutées, {} échecs", 
                iteration, seancesAjoutees, seancesEchouees);
            
            return Map.of(
                "success", true,
                "message", seancesAjoutees + " séances générées avec succès",
                "seancesAjoutees", seancesAjoutees,
                "seancesEchouees", seancesEchouees
            );
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération automatique", e);
            return Map.of("success", false, "message", "Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Assigner des séances pour un enseignant sur un jour donné
     * Stratégie : 2h CM + 2h TD, ou 4h CM, ou 4h TP, ou ce qui est disponible
     */
    private int assignerSeancesPourJour(
            UUID emploiDuTempsId,
            CoursEnseignantInfo prioritaire,
            LocalDate dateJour,
            Map<UUID, List<CreneauDisponible>> creneauxParEnseignant,
            List<Cours> tousLesCours) {
        
        int heuresAssignees = 0;
        Enseignant enseignant = prioritaire.enseignant;
        
        // Récupérer tous les cours de cet enseignant pour cette classe
        List<Cours> coursEnseignant = tousLesCours.stream()
            .filter(c -> c.getEnseignant() != null)
            .filter(c -> c.getEnseignant().getIdUser().equals(enseignant.getIdUser()))
            .filter(c -> c.getDureeRestante() > 0)
            .collect(Collectors.toList());
        
        if (coursEnseignant.isEmpty()) {
            return 0;
        }
        
        // Trier par durée restante décroissante
        coursEnseignant.sort((a, b) -> Integer.compare(b.getDureeRestante(), a.getDureeRestante()));
        
        // Stratégie : essayer 2h CM + 2h TD, sinon 4h du même type
        Cours coursCM = coursEnseignant.stream()
            .filter(c -> c.getTypeCours() == TypeCours.CM)
            .findFirst().orElse(null);
        
        Cours coursTD = coursEnseignant.stream()
            .filter(c -> c.getTypeCours() == TypeCours.TD)
            .findFirst().orElse(null);
        
        Cours coursTP = coursEnseignant.stream()
            .filter(c -> c.getTypeCours() == TypeCours.TP)
            .findFirst().orElse(null);
        
        List<CreneauDisponible> creneauxJour = creneauxParEnseignant.get(enseignant.getIdUser()).stream()
            .filter(cr -> cr.date.equals(dateJour))
            .collect(Collectors.toList());
        
        if (creneauxJour.isEmpty()) {
            log.debug("Aucun créneau disponible ce jour pour {}", enseignant.getNom());
            return 0;
        }
        
        int creneauxDisponibles = creneauxJour.size();
        log.debug("{} créneaux disponibles pour {}", creneauxDisponibles, enseignant.getNom());
        
        // Stratégie 1 : 2h CM + 2h TD (si au moins 4 créneaux)
        if (creneauxDisponibles >= 4 && 
            coursCM != null && coursCM.getDureeRestante() >= 2 &&
            coursTD != null && coursTD.getDureeRestante() >= 2) {
            
            log.info("Stratégie: 2h CM + 2h TD");
            heuresAssignees += assignerBlocSeances(emploiDuTempsId, coursCM, enseignant, creneauxJour, 2);
            heuresAssignees += assignerBlocSeances(emploiDuTempsId, coursTD, enseignant, creneauxJour, 2);
        }
        // Stratégie 2 : 4h CM (si au moins 4 créneaux)
        else if (creneauxDisponibles >= 4 && coursCM != null && coursCM.getDureeRestante() >= 4) {
            log.info("Stratégie: 4h CM");
            heuresAssignees += assignerBlocSeances(emploiDuTempsId, coursCM, enseignant, creneauxJour, 4);
        }
        // Stratégie 3 : 4h TP (si au moins 4 créneaux)
        else if (creneauxDisponibles >= 4 && coursTP != null && coursTP.getDureeRestante() >= 4) {
            log.info("Stratégie: 4h TP");
            heuresAssignees += assignerBlocSeances(emploiDuTempsId, coursTP, enseignant, creneauxJour, 4);
        }
        // Stratégie 4 : 4h TD (si au moins 4 créneaux)
        else if (creneauxDisponibles >= 4 && coursTD != null && coursTD.getDureeRestante() >= 4) {
            log.info("Stratégie: 4h TD");
            heuresAssignees += assignerBlocSeances(emploiDuTempsId, coursTD, enseignant, creneauxJour, 4);
        }
        // Stratégie 5 : Assigner ce qui est possible avec les créneaux disponibles
        else {
            // Prendre le cours avec le plus de durée restante
            Cours coursRestant = coursEnseignant.get(0);
            int heuresAPlanifier = Math.min(creneauxDisponibles, coursRestant.getDureeRestante());
            
            if (heuresAPlanifier > 0) {
                log.info("Stratégie: {}h {} (créneaux disponibles: {})", 
                    heuresAPlanifier, coursRestant.getTypeCours(), creneauxDisponibles);
                heuresAssignees += assignerBlocSeances(emploiDuTempsId, coursRestant, enseignant, creneauxJour, heuresAPlanifier);
            }
        }
        
        return heuresAssignees;
    }
    
    /**
     * Assigner un bloc de séances consécutives
     */
    private int assignerBlocSeances(
            UUID emploiDuTempsId,
            Cours cours,
            Enseignant enseignant,
            List<CreneauDisponible> creneauxJour,
            int nbHeures) {
        
        int heuresAssignees = 0;
        EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.getEmploiDuTempsById(emploiDuTempsId);
        
        for (int i = 0; i < nbHeures && !creneauxJour.isEmpty(); i++) {
            CreneauDisponible creneau = creneauxJour.remove(0);
            
            // Vérifier les conflits
            if (hasConflict(emploiDuTemps, enseignant, creneau.date, creneau.heureDebut, creneau.heureFin)) {
                log.warn("Conflit détecté pour {} à {}", creneau.date, creneau.heureDebut);
                continue;
            }
            
            try {
                emploiDuTempsService.ajouterSeance(
                    emploiDuTempsId,
                    cours.getIdCours(),
                    enseignant.getIdUser(),
                    null, // Pas de salle
                    creneau.date,
                    creneau.heureDebut,
                    creneau.heureFin,
                    "Généré automatiquement"
                );
                
                heuresAssignees++;
                log.info("  → Séance ajoutée: {} {} à {}", cours.getTypeCours(), cours.getUe().getIntitule(), creneau.heureDebut);
                
            } catch (Exception e) {
                log.error("Erreur lors de l'ajout de séance: {}", e.getMessage());
            }
        }
        
        return heuresAssignees;
    }
    
    // Classe interne pour stocker les infos de cours/enseignant
    private static class CoursEnseignantInfo {
        Cours cours;
        Enseignant enseignant;
        int nbCreneauxSemaine;
        int dureeRestante;
    }
    
    private List<CreneauDisponible> getCreneauxDisponibles(
            List<DisponibiliteEnseignant> disponibilites, LocalDate dateDebut, LocalDate dateFin) {
        
        List<CreneauDisponible> creneaux = new ArrayList<>();
        
        for (DisponibiliteEnseignant dispo : disponibilites) {
            List<CreneauDisponibilite> creneauxJour = creneauDisponibiliteRepository.findByDisponibilite(dispo);
            
            for (CreneauDisponibilite creneauJour : creneauxJour) {
                LocalDate date = creneauJour.getDate();
                
                // Vérifier que la date est dans la semaine de l'emploi du temps
                if (date.isBefore(dateDebut) || date.isAfter(dateFin)) {
                    continue;
                }
                
                // Ignorer le dimanche
                if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    continue;
                }
                
                List<PlageHoraire> plages = plageHoraireRepository.findByCreneauDisponibilite(creneauJour);
                
                for (PlageHoraire plage : plages) {
                    // Découper les plages en créneaux d'1 heure
                    LocalTime debut = plage.getHeureDebut();
                    LocalTime fin = plage.getHeureFin();
                    
                    while (debut.isBefore(fin)) {
                        LocalTime finCreneau = debut.plusHours(1);
                        if (finCreneau.isAfter(fin)) {
                            finCreneau = fin;
                        }
                        
                        // Éviter la pause déjeuner (12h-13h)
                        if (!(debut.equals(LocalTime.of(12, 0)) || 
                              (debut.isBefore(LocalTime.of(13, 0)) && finCreneau.isAfter(LocalTime.of(12, 0))))) {
                            creneaux.add(new CreneauDisponible(date, debut, finCreneau));
                        }
                        
                        debut = finCreneau;
                    }
                }
            }
        }
        
        // Trier les créneaux par date et heure
        creneaux.sort(Comparator.comparing((CreneauDisponible c) -> c.date)
            .thenComparing(c -> c.heureDebut));
        
        return creneaux;
    }
    
    private boolean hasConflict(EmploiDuTempsClasse emploiDuTemps, Enseignant enseignant,
                                LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        
        // Vérifier conflit classe
        List<SeanceClasse> seancesClasse = seanceRepository
            .findByEmploiDuTempsAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                emploiDuTemps, date, heureFin, heureDebut);
        
        if (!seancesClasse.isEmpty()) {
            return true;
        }
        
        // Vérifier conflit enseignant
        List<SeanceClasse> seancesEnseignant = seanceRepository
            .findByEnseignantAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                enseignant, date, heureFin, heureDebut);
        
        return !seancesEnseignant.isEmpty();
    }
    
    // Classe interne pour représenter un créneau disponible
    private static class CreneauDisponible {
        LocalDate date;
        LocalTime heureDebut;
        LocalTime heureFin;
        
        CreneauDisponible(LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
            this.date = date;
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
        }
    }
}
