package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DisponibiliteService {

    @Autowired
    private DisponibiliteEnseignantRepository disponibiliteRepo;
    
    @Autowired
    private CreneauDisponibiliteRepository creneauDispoRepo;
    
    @Autowired
    private PlageHoraireRepository plageHoraireRepo;

    /**
     * Récupérer toutes les disponibilités d'un enseignant
     */
    public List<DisponibiliteEnseignant> getDisponibilitesEnseignant(Enseignant enseignant) {
        log.info("Recherche des disponibilités pour l'enseignant ID: {}", enseignant.getIdUser());
        List<DisponibiliteEnseignant> disponibilites = disponibiliteRepo.findByEnseignant(enseignant);
        log.info("Nombre de disponibilités trouvées: {}", disponibilites.size());
        return disponibilites;
    }

    /**
     * Récupérer une disponibilité par ID
     */
    public Optional<DisponibiliteEnseignant> getDisponibiliteById(UUID id) {
        return disponibiliteRepo.findById(id);
    }

    /**
     * Créer une nouvelle disponibilité pour une semaine
     */
    public DisponibiliteEnseignant creerDisponibiliteSemaine(Enseignant enseignant, LocalDate dateDebut) {
        // Calculer le début et la fin de la semaine (lundi à dimanche)
        LocalDate debutSemaine = dateDebut.with(DayOfWeek.MONDAY);
        LocalDate finSemaine = debutSemaine.plusDays(6);
        
        log.info("Création disponibilité pour enseignant {} - Semaine du {} au {}", 
            enseignant.getIdUser(), debutSemaine, finSemaine);
        
        // Vérifier si une disponibilité existe déjà pour cette semaine
        List<DisponibiliteEnseignant> existantes = disponibiliteRepo
            .findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                enseignant, finSemaine, debutSemaine);
        
        if (!existantes.isEmpty()) {
            log.warn("Disponibilité existe déjà pour cette semaine");
            throw new IllegalArgumentException("Une disponibilité existe déjà pour cette semaine");
        }

        DisponibiliteEnseignant disponibilite = new DisponibiliteEnseignant();
        disponibilite.setEnseignant(enseignant);
        disponibilite.setDateDebut(debutSemaine);
        disponibilite.setDateFin(finSemaine);
        
        DisponibiliteEnseignant saved = disponibiliteRepo.save(disponibilite);
        log.info("Disponibilité créée avec ID: {}", saved.getId());
        return saved;
    }

    /**
     * Ajouter un créneau de disponibilité
     */
    public PlageHoraire ajouterCreneau(UUID disponibiliteId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        log.info("Ajout créneau - Dispo: {}, Date: {}, {}h-{}h", disponibiliteId, date, heureDebut, heureFin);
        
        DisponibiliteEnseignant disponibilite = disponibiliteRepo.findById(disponibiliteId)
            .orElseThrow(() -> new IllegalArgumentException("Disponibilité non trouvée"));

        // Vérifier que la date est dans la semaine de la disponibilité
        if (date.isBefore(disponibilite.getDateDebut()) || date.isAfter(disponibilite.getDateFin())) {
            log.error("Date {} hors de la semaine {} - {}", date, disponibilite.getDateDebut(), disponibilite.getDateFin());
            throw new IllegalArgumentException("La date doit être dans la semaine de disponibilité");
        }

        // Trouver ou créer le créneau pour ce jour
        CreneauDisponibilite creneauJour = creneauDispoRepo
            .findByDisponibiliteAndDate(disponibilite, date)
            .stream()
            .findFirst()
            .orElseGet(() -> {
                log.info("Création nouveau créneau pour la date {}", date);
                CreneauDisponibilite nouveau = new CreneauDisponibilite();
                nouveau.setDisponibilite(disponibilite);
                nouveau.setDate(date);
                return creneauDispoRepo.save(nouveau);
            });

        // Créer la plage horaire
        PlageHoraire plageHoraire = new PlageHoraire();
        plageHoraire.setCreneauDisponibilite(creneauJour);
        plageHoraire.setHeureDebut(heureDebut);
        plageHoraire.setHeureFin(heureFin);
        
        PlageHoraire saved = plageHoraireRepo.save(plageHoraire);
        log.info("Plage horaire créée avec ID: {}", saved.getId());
        return saved;
    }

    /**
     * Supprimer une plage horaire
     */
    public void supprimerPlageHoraire(UUID plageHoraireId) {
        PlageHoraire plageHoraire = plageHoraireRepo.findById(plageHoraireId)
            .orElseThrow(() -> new IllegalArgumentException("Plage horaire non trouvée"));
        
        plageHoraireRepo.delete(plageHoraire);
        
        // Si c'était la dernière plage horaire du jour, supprimer le créneau jour
        CreneauDisponibilite creneauJour = plageHoraire.getCreneauDisponibilite();
        List<PlageHoraire> plagesRestantes = plageHoraireRepo.findByCreneauDisponibilite(creneauJour);
        
        if (plagesRestantes.isEmpty()) {
            creneauDispoRepo.delete(creneauJour);
        }
    }

    /**
     * Supprimer une disponibilité complète (avec tous ses créneaux)
     */
    @Transactional
    public void supprimerDisponibilite(UUID disponibiliteId) {
        log.info("Début suppression disponibilité {}", disponibiliteId);
        
        // Supprimer directement via requêtes SQL bulk sans charger les entités
        // 1. Supprimer toutes les plages horaires
        plageHoraireRepo.deleteByDisponibiliteIdViaCreneaux(disponibiliteId);
        log.info("Plages horaires supprimées");
        
        // 2. Supprimer tous les créneaux
        creneauDispoRepo.deleteByDisponibiliteId(disponibiliteId);
        log.info("Créneaux supprimés");
        
        // 3. Supprimer la disponibilité
        disponibiliteRepo.deleteById(disponibiliteId);
        log.info("Disponibilité {} supprimée avec succès", disponibiliteId);
    }

    /**
     * Supprimer tous les créneaux d'une disponibilité
     */
    @Transactional
    public void supprimerTousLesCreneaux(UUID disponibiliteId) {
        log.info("Début suppression créneaux pour disponibilité {}", disponibiliteId);
        
        // Supprimer d'abord toutes les plages horaires via requête bulk
        plageHoraireRepo.deleteByDisponibiliteIdViaCreneaux(disponibiliteId);
        log.info("Plages horaires supprimées");
        
        // Ensuite supprimer tous les créneaux via requête bulk
        creneauDispoRepo.deleteByDisponibiliteId(disponibiliteId);
        log.info("Créneaux supprimés");
        
        log.info("Tous les créneaux supprimés pour disponibilité {}", disponibiliteId);
    }

    /**
     * Obtenir l'emploi du temps d'une semaine de disponibilité
     */
    public Map<LocalDate, List<PlageHoraire>> getEmploiDuTempsSemaine(UUID disponibiliteId) {
        DisponibiliteEnseignant disponibilite = disponibiliteRepo.findById(disponibiliteId)
            .orElseThrow(() -> new IllegalArgumentException("Disponibilité non trouvée"));

        List<CreneauDisponibilite> creneaux = creneauDispoRepo.findByDisponibilite(disponibilite);
        
        Map<LocalDate, List<PlageHoraire>> emploiDuTemps = new HashMap<>();
        
        // Initialiser tous les jours de la semaine
        for (int i = 0; i < 7; i++) {
            LocalDate jour = disponibilite.getDateDebut().plusDays(i);
            emploiDuTemps.put(jour, new ArrayList<>());
        }
        
        // Remplir avec les plages horaires existantes
        for (CreneauDisponibilite creneau : creneaux) {
            List<PlageHoraire> plages = plageHoraireRepo.findByCreneauDisponibilite(creneau);
            // Trier par heure de début
            plages.sort(Comparator.comparing(PlageHoraire::getHeureDebut));
            emploiDuTemps.put(creneau.getDate(), plages);
        }
        
        return emploiDuTemps;
    }
}