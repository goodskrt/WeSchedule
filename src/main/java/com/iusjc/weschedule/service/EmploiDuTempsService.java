package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmploiDuTempsService {

    @Autowired
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private SalleRepository salleRepository;

    /**
     * Créer un emploi du temps pour une classe pour une semaine donnée
     */
    @Transactional
    public EmploiDuTempsClasse creerEmploiDuTemps(UUID classeId, LocalDate dateDebut) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        // Calculer la date de début (lundi) et de fin (dimanche) de la semaine
        LocalDate lundi = dateDebut.with(DayOfWeek.MONDAY);
        LocalDate dimanche = lundi.plusDays(6);

        // Obtenir le numéro de semaine et l'année
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        int semaine = lundi.get(weekFields.weekOfWeekBasedYear());
        int annee = lundi.get(weekFields.weekBasedYear());

        // Vérifier si un emploi du temps existe déjà pour cette semaine
        if (emploiDuTempsRepository.existsByClasseAndSemaineAndAnnee(classe, semaine, annee)) {
            throw new RuntimeException("Un emploi du temps existe déjà pour cette semaine");
        }

        // Créer l'emploi du temps
        EmploiDuTempsClasse emploiDuTemps = new EmploiDuTempsClasse();
        emploiDuTemps.setClasse(classe);
        emploiDuTemps.setDateDebut(lundi);
        emploiDuTemps.setDateFin(dimanche);
        emploiDuTemps.setSemaine(semaine);
        emploiDuTemps.setAnnee(annee);
        emploiDuTemps.setSeances(new HashSet<>());

        return emploiDuTempsRepository.save(emploiDuTemps);
    }

    /**
     * Ajouter une séance à un emploi du temps
     */
    @Transactional
    public SeanceClasse ajouterSeance(UUID emploiDuTempsId, UUID coursId, UUID enseignantId, 
                                      UUID salleId, LocalDate date, LocalTime heureDebut, 
                                      LocalTime heureFin, String remarques) {
        EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Vérifier que la date est dans la semaine de l'emploi du temps
        if (date.isBefore(emploiDuTemps.getDateDebut()) || date.isAfter(emploiDuTemps.getDateFin())) {
            throw new RuntimeException("La date doit être dans la semaine de l'emploi du temps");
        }

        // Vérifier les conflits
        Enseignant enseignant = null;
        if (enseignantId != null) {
            enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
            
            // Vérifier conflit enseignant
            List<SeanceClasse> conflitsEnseignant = seanceRepository
                    .findByEnseignantAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                            enseignant, date, heureFin, heureDebut);
            if (!conflitsEnseignant.isEmpty()) {
                throw new RuntimeException("L'enseignant a déjà une séance à cet horaire");
            }
        }

        Salle salle = null;
        if (salleId != null) {
            salle = salleRepository.findById(salleId)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            
            // Vérifier conflit salle
            List<SeanceClasse> conflitsSalle = seanceRepository
                    .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                            salle, date, heureFin, heureDebut);
            if (!conflitsSalle.isEmpty()) {
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
        seance.setJourSemaine(date.getDayOfWeek().getValue() - 1); // 0=Lundi
        seance.setHeureDebut(heureDebut);
        seance.setHeureFin(heureFin);
        seance.setRemarques(remarques);

        return seanceRepository.save(seance);
    }

    /**
     * Modifier une séance
     */
    @Transactional
    public SeanceClasse modifierSeance(UUID seanceId, UUID coursId, UUID enseignantId, 
                                       UUID salleId, LocalDate date, LocalTime heureDebut, 
                                       LocalTime heureFin, String remarques) {
        SeanceClasse seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        if (coursId != null) {
            Cours cours = coursRepository.findById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            seance.setCours(cours);
        }

        if (enseignantId != null) {
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
            
            // Vérifier conflit enseignant (exclure la séance actuelle)
            List<SeanceClasse> conflitsEnseignant = seanceRepository
                    .findByEnseignantAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                            enseignant, date, heureFin, heureDebut);
            conflitsEnseignant.removeIf(s -> s.getId().equals(seanceId));
            if (!conflitsEnseignant.isEmpty()) {
                throw new RuntimeException("L'enseignant a déjà une séance à cet horaire");
            }
            seance.setEnseignant(enseignant);
        }

        if (salleId != null) {
            Salle salle = salleRepository.findById(salleId)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            
            // Vérifier conflit salle (exclure la séance actuelle)
            List<SeanceClasse> conflitsSalle = seanceRepository
                    .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                            salle, date, heureFin, heureDebut);
            conflitsSalle.removeIf(s -> s.getId().equals(seanceId));
            if (!conflitsSalle.isEmpty()) {
                throw new RuntimeException("La salle est déjà occupée à cet horaire");
            }
            seance.setSalle(salle);
        }

        if (date != null) {
            seance.setDate(date);
            seance.setJourSemaine(date.getDayOfWeek().getValue() - 1);
        }
        if (heureDebut != null) seance.setHeureDebut(heureDebut);
        if (heureFin != null) seance.setHeureFin(heureFin);
        if (remarques != null) seance.setRemarques(remarques);

        return seanceRepository.save(seance);
    }

    /**
     * Supprimer une séance
     */
    @Transactional
    public void supprimerSeance(UUID seanceId) {
        seanceRepository.deleteById(seanceId);
    }

    /**
     * Obtenir l'emploi du temps d'une classe pour une semaine
     */
    public EmploiDuTempsClasse getEmploiDuTempsParSemaine(UUID classeId, int semaine, int annee) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        
        return emploiDuTempsRepository.findByClasseAndSemaineAndAnnee(classe, semaine, annee)
                .orElse(null);
    }

    /**
     * Obtenir toutes les séances d'un emploi du temps
     */
    public List<SeanceClasse> getSeances(UUID emploiDuTempsId) {
        EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));
        
        return seanceRepository.findByEmploiDuTempsOrderByDateAscHeureDebutAsc(emploiDuTemps);
    }

    /**
     * Obtenir les séances groupées par jour
     */
    public Map<LocalDate, List<SeanceClasse>> getSeancesParJour(UUID emploiDuTempsId) {
        List<SeanceClasse> seances = getSeances(emploiDuTempsId);
        
        return seances.stream()
                .collect(Collectors.groupingBy(
                        SeanceClasse::getDate,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Obtenir tous les emplois du temps d'une classe
     */
    public List<EmploiDuTempsClasse> getEmploisDuTempsClasse(UUID classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        
        return emploiDuTempsRepository.findByClasseOrderByDateDebutDesc(classe);
    }

    /**
     * Supprimer un emploi du temps complet
     */
    @Transactional
    public void supprimerEmploiDuTemps(UUID emploiDuTempsId) {
        emploiDuTempsRepository.deleteById(emploiDuTempsId);
    }

    /**
     * Dupliquer un emploi du temps pour une nouvelle semaine
     */
    @Transactional
    public EmploiDuTempsClasse dupliquerEmploiDuTemps(UUID emploiDuTempsId, LocalDate nouvelleDateDebut) {
        EmploiDuTempsClasse ancien = emploiDuTempsRepository.findById(emploiDuTempsId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));

        // Créer le nouvel emploi du temps
        EmploiDuTempsClasse nouveau = creerEmploiDuTemps(ancien.getClasse().getIdClasse(), nouvelleDateDebut);

        // Copier les séances
        List<SeanceClasse> anciennesSeances = seanceRepository.findByEmploiDuTemps(ancien);
        LocalDate ancienLundi = ancien.getDateDebut();
        LocalDate nouveauLundi = nouveau.getDateDebut();

        for (SeanceClasse ancienneSeance : anciennesSeances) {
            // Calculer la nouvelle date (même jour de la semaine)
            long joursDepuisLundi = ancienneSeance.getDate().toEpochDay() - ancienLundi.toEpochDay();
            LocalDate nouvelleDate = nouveauLundi.plusDays(joursDepuisLundi);

            SeanceClasse nouvelleSeance = new SeanceClasse();
            nouvelleSeance.setEmploiDuTemps(nouveau);
            nouvelleSeance.setCours(ancienneSeance.getCours());
            nouvelleSeance.setEnseignant(ancienneSeance.getEnseignant());
            nouvelleSeance.setSalle(ancienneSeance.getSalle());
            nouvelleSeance.setDate(nouvelleDate);
            nouvelleSeance.setJourSemaine(ancienneSeance.getJourSemaine());
            nouvelleSeance.setHeureDebut(ancienneSeance.getHeureDebut());
            nouvelleSeance.setHeureFin(ancienneSeance.getHeureFin());
            nouvelleSeance.setRemarques(ancienneSeance.getRemarques());

            seanceRepository.save(nouvelleSeance);
        }

        return nouveau;
    }
}
