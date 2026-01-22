package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour planifier des séances de classe en tenant compte:
 * - Des disponibilités des enseignants
 * - Des cours avec UE actives
 * - De la durée restante des cours (cours.duree < ue.duree)
 * - De la classe concernée
 */
@Service
public class PlanificationSeanceService {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private DisponibiliteEnseignantRepository disponibiliteRepository;

    @Autowired
    private CreneauDisponibiliteRepository creneauRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;

    @Autowired
    private DureeService dureeService;

    @Autowired
    private ReservationSalleService reservationSalleService;

    /**
     * DTO pour représenter un cours planifiable
     */
    public static class CoursPlanifiable {
        private Cours cours;
        private UE ue;
        private Enseignant enseignant;
        private int heuresRestantes;
        private double pourcentageAvancement;

        public CoursPlanifiable(Cours cours, UE ue, Enseignant enseignant, 
                               int heuresRestantes, double pourcentageAvancement) {
            this.cours = cours;
            this.ue = ue;
            this.enseignant = enseignant;
            this.heuresRestantes = heuresRestantes;
            this.pourcentageAvancement = pourcentageAvancement;
        }

        // Getters
        public Cours getCours() { return cours; }
        public UE getUe() { return ue; }
        public Enseignant getEnseignant() { return enseignant; }
        public int getHeuresRestantes() { return heuresRestantes; }
        public double getPourcentageAvancement() { return pourcentageAvancement; }
    }

    /**
     * DTO pour représenter un créneau disponible
     */
    public static class CreneauDisponible {
        private Enseignant enseignant;
        private Cours cours;
        private LocalDate date;
        private LocalTime heureDebut;
        private LocalTime heureFin;
        private PlageHoraire plageHoraire;

        public CreneauDisponible(Enseignant enseignant, Cours cours, LocalDate date,
                                LocalTime heureDebut, LocalTime heureFin, PlageHoraire plageHoraire) {
            this.enseignant = enseignant;
            this.cours = cours;
            this.date = date;
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.plageHoraire = plageHoraire;
        }

        // Getters
        public Enseignant getEnseignant() { return enseignant; }
        public Cours getCours() { return cours; }
        public LocalDate getDate() { return date; }
        public LocalTime getHeureDebut() { return heureDebut; }
        public LocalTime getHeureFin() { return heureFin; }
        public PlageHoraire getPlageHoraire() { return plageHoraire; }
    }

    /**
     * Obtenir tous les cours planifiables pour une classe
     * (UE actives avec heures restantes > 0)
     */
    public List<CoursPlanifiable> getCoursPlanifiables(UUID classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        List<CoursPlanifiable> coursPlanifiables = new ArrayList<>();

        // Parcourir toutes les UE de la classe
        for (UE ue : classe.getUes()) {
            // Vérifier que l'UE est active
            if (ue.getStatut() != StatutUE.ACTIF) {
                continue;
            }

            // Trouver tous les cours pour cette UE
            List<Cours> cours = coursRepository.findByUe(ue);

            for (Cours c : cours) {
                // Vérifier qu'il reste des heures à planifier
                if (dureeService.aDesHeuresRestantes(c)) {
                    int heuresRestantes = c.getDuree();
                    double pourcentage = dureeService.calculerPourcentageAvancement(c);

                    // Trouver l'enseignant du cours (si assigné)
                    Enseignant enseignant = trouverEnseignantPourCours(c);

                    coursPlanifiables.add(new CoursPlanifiable(
                            c, ue, enseignant, heuresRestantes, pourcentage
                    ));
                }
            }
        }

        return coursPlanifiables;
    }

    /**
     * Obtenir les créneaux disponibles pour planifier des séances
     * pour une classe sur une période donnée
     */
    public List<CreneauDisponible> getCreneauxDisponibles(
            UUID classeId, LocalDate dateDebut, LocalDate dateFin) {

        List<CoursPlanifiable> coursPlanifiables = getCoursPlanifiables(classeId);
        List<CreneauDisponible> creneauxDisponibles = new ArrayList<>();

        for (CoursPlanifiable cp : coursPlanifiables) {
            if (cp.getEnseignant() == null) {
                continue; // Pas d'enseignant assigné
            }

            // Récupérer les disponibilités de l'enseignant
            List<DisponibiliteEnseignant> disponibilites = 
                    disponibiliteRepository.findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                            cp.getEnseignant(), dateFin, dateDebut
                    );

            for (DisponibiliteEnseignant dispo : disponibilites) {
                // Parcourir les créneaux de disponibilité
                for (CreneauDisponibilite creneau : dispo.getCreneauxParJour()) {
                    LocalDate date = creneau.getDate();

                    // Vérifier que la date est dans la période
                    if (date.isBefore(dateDebut) || date.isAfter(dateFin)) {
                        continue;
                    }

                    // Parcourir les plages horaires
                    for (PlageHoraire plage : creneau.getPlagesHoraires()) {
                        // Vérifier qu'il n'y a pas de conflit avec une séance existante
                        if (!aConflitSeance(cp.getEnseignant(), date, 
                                plage.getHeureDebut(), plage.getHeureFin())) {
                            
                            creneauxDisponibles.add(new CreneauDisponible(
                                    cp.getEnseignant(),
                                    cp.getCours(),
                                    date,
                                    plage.getHeureDebut(),
                                    plage.getHeureFin(),
                                    plage
                            ));
                        }
                    }
                }
            }
        }

        return creneauxDisponibles;
    }

    /**
     * Créer une séance à partir d'un créneau disponible
     */
    @Transactional
    public SeanceClasse creerSeanceDepuisCreneau(
            UUID emploiDuTempsId,
            UUID coursId,
            UUID enseignantId,
            UUID salleId,
            LocalDate date,
            LocalTime heureDebut,
            LocalTime heureFin,
            String remarques) {

        // Vérifications
        EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Vérifier que l'UE est active
        if (cours.getUe().getStatut() != StatutUE.ACTIF) {
            throw new RuntimeException("L'UE n'est pas active");
        }

        // Vérifier qu'il reste des heures
        if (!dureeService.aDesHeuresRestantes(cours)) {
            throw new RuntimeException("Toutes les heures ont été planifiées pour ce cours");
        }

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

        // Vérifier la disponibilité de l'enseignant
        if (!estDisponible(enseignant, date, heureDebut, heureFin)) {
            throw new RuntimeException("L'enseignant n'est pas disponible à cet horaire");
        }

        // Vérifier les conflits
        if (aConflitSeance(enseignant, date, heureDebut, heureFin)) {
            throw new RuntimeException("L'enseignant a déjà une séance à cet horaire");
        }

        Salle salle = null;
        if (salleId != null) {
            salle = salleRepository.findById(salleId)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

            // VÉRIFICATION CRITIQUE: Capacité suffisante
            if (!reservationSalleService.aCapaciteSuffisante(salleId, emploiDuTemps.getClasse().getIdClasse())) {
                Map<String, Object> verif = reservationSalleService.verifierCapacite(
                    salleId, emploiDuTemps.getClasse().getIdClasse()
                );
                throw new RuntimeException((String) verif.get("message"));
            }

            // Vérifier disponibilité salle
            if (!reservationSalleService.estSalleDisponible(salle, date, heureDebut, heureFin)) {
                throw new RuntimeException("La salle est déjà occupée à cet horaire");
            }
        }

        // Créer la séance
        SeanceClasse seance = new SeanceClasse();
        seance.setEmploiDuTemps(emploiDuTemps);
        seance.setCours(cours);
        seance.setEnseignant(enseignant);
        seance.setSalle(salle);
        seance.setDate(date);
        seance.setJourSemaine(date.getDayOfWeek().getValue() - 1);
        seance.setHeureDebut(heureDebut);
        seance.setHeureFin(heureFin);
        seance.setRemarques(remarques);

        SeanceClasse seanceSaved = seanceRepository.save(seance);

        // Créer une réservation de salle si une salle est fournie
        if (salle != null) {
            try {
                reservationSalleService.creerReservation(
                    salleId,
                    emploiDuTemps.getClasse().getIdClasse(),
                    coursId,
                    enseignantId,
                    date,
                    heureDebut,
                    heureFin
                );
            } catch (Exception e) {
                // Si la réservation échoue, on continue quand même
                // (la séance est créée mais sans réservation formelle)
                System.err.println("Avertissement: Impossible de créer la réservation - " + e.getMessage());
            }
        }

        // Décrémenter les heures restantes
        dureeService.decrementerApresSeance(seanceSaved);

        return seanceSaved;
    }

    /**
     * Vérifier si un enseignant est disponible à un horaire donné
     */
    private boolean estDisponible(Enseignant enseignant, LocalDate date, 
                                  LocalTime heureDebut, LocalTime heureFin) {
        
        List<DisponibiliteEnseignant> disponibilites = 
                disponibiliteRepository.findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                        enseignant, date, date
                );

        for (DisponibiliteEnseignant dispo : disponibilites) {
            for (CreneauDisponibilite creneau : dispo.getCreneauxParJour()) {
                if (creneau.getDate().equals(date)) {
                    for (PlageHoraire plage : creneau.getPlagesHoraires()) {
                        // Vérifier si la plage horaire couvre l'horaire demandé
                        if (!plage.getHeureDebut().isAfter(heureDebut) && 
                            !plage.getHeureFin().isBefore(heureFin)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Vérifier s'il y a un conflit avec une séance existante
     */
    private boolean aConflitSeance(Enseignant enseignant, LocalDate date,
                                   LocalTime heureDebut, LocalTime heureFin) {
        List<SeanceClasse> seances = seanceRepository
                .findByEnseignantAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                        enseignant, date, heureFin, heureDebut
                );
        return !seances.isEmpty();
    }

    /**
     * Trouver l'enseignant assigné à un cours
     * (À adapter selon votre logique métier)
     */
    private Enseignant trouverEnseignantPourCours(Cours cours) {
        // Option 1: Si le cours a un champ enseignant
        // return cours.getEnseignant();

        // Option 2: Chercher dans les séances existantes
        List<SeanceClasse> seances = seanceRepository.findByCours(cours);
        if (!seances.isEmpty()) {
            return seances.get(0).getEnseignant();
        }

        // Option 3: Chercher via l'UE et les enseignants
        // À implémenter selon votre logique

        return null;
    }

    /**
     * Obtenir un résumé de planification pour une classe
     */
    public Map<String, Object> getResumePlanification(UUID classeId) {
        List<CoursPlanifiable> coursPlanifiables = getCoursPlanifiables(classeId);

        int totalCours = coursPlanifiables.size();
        int totalHeuresRestantes = coursPlanifiables.stream()
                .mapToInt(CoursPlanifiable::getHeuresRestantes)
                .sum();

        Map<String, Object> resume = new HashMap<>();
        resume.put("totalCours", totalCours);
        resume.put("totalHeuresRestantes", totalHeuresRestantes);
        resume.put("cours", coursPlanifiables);

        return resume;
    }

    /**
     * Obtenir les salles disponibles avec capacité suffisante pour une séance
     */
    public List<ReservationSalleService.SalleDisponible> getSallesDisponiblesPourSeance(
            UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        
        return reservationSalleService.getSallesAvecCapaciteSuffisante(
            classeId, date, heureDebut, heureFin
        );
    }

    /**
     * Réserver automatiquement la meilleure salle pour une séance
     */
    public Salle reserverMeilleureSallePourSeance(
            UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        
        return reservationSalleService.reserverMeilleureSalle(
            classeId, date, heureDebut, heureFin
        );
    }
}
