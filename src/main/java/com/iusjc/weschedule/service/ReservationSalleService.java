package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutReservation;
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
 * Service pour gérer les réservations de salles avec vérification de capacité
 */
@Service
public class ReservationSalleService {

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;

    /**
     * DTO pour représenter une salle disponible avec sa capacité
     */
    public static class SalleDisponible {
        private Salle salle;
        private int capacite;
        private int effectifClasse;
        private boolean capaciteSuffisante;
        private int placesRestantes;

        public SalleDisponible(Salle salle, int effectifClasse) {
            this.salle = salle;
            this.capacite = salle.getCapacite();
            this.effectifClasse = effectifClasse;
            this.capaciteSuffisante = capacite >= effectifClasse;
            this.placesRestantes = capacite - effectifClasse;
        }

        // Getters
        public Salle getSalle() { return salle; }
        public int getCapacite() { return capacite; }
        public int getEffectifClasse() { return effectifClasse; }
        public boolean isCapaciteSuffisante() { return capaciteSuffisante; }
        public int getPlacesRestantes() { return placesRestantes; }
    }

    /**
     * Trouver les salles disponibles avec capacité suffisante
     * pour une classe à une date et heure données
     */
    public List<SalleDisponible> getSallesDisponiblesAvecCapacite(
            UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        int effectifClasse = classe.getEffectif();

        // Récupérer toutes les salles
        List<Salle> toutesLesSalles = salleRepository.findAll();

        List<SalleDisponible> sallesDisponibles = new ArrayList<>();

        for (Salle salle : toutesLesSalles) {
            // Vérifier si la salle est disponible (pas de conflit)
            if (estSalleDisponible(salle, date, heureDebut, heureFin)) {
                SalleDisponible salleDisponible = new SalleDisponible(salle, effectifClasse);
                sallesDisponibles.add(salleDisponible);
            }
        }

        // Trier: d'abord les salles avec capacité suffisante, puis par capacité croissante
        sallesDisponibles.sort((s1, s2) -> {
            // Priorité 1: Capacité suffisante
            if (s1.isCapaciteSuffisante() != s2.isCapaciteSuffisante()) {
                return s1.isCapaciteSuffisante() ? -1 : 1;
            }
            // Priorité 2: Capacité la plus proche de l'effectif (éviter le gaspillage)
            return Integer.compare(
                Math.abs(s1.getPlacesRestantes()),
                Math.abs(s2.getPlacesRestantes())
            );
        });

        return sallesDisponibles;
    }

    /**
     * Trouver uniquement les salles avec capacité suffisante
     */
    public List<SalleDisponible> getSallesAvecCapaciteSuffisante(
            UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {

        return getSallesDisponiblesAvecCapacite(classeId, date, heureDebut, heureFin)
                .stream()
                .filter(SalleDisponible::isCapaciteSuffisante)
                .collect(Collectors.toList());
    }

    /**
     * Vérifier si une salle est disponible à un horaire donné
     */
    public boolean estSalleDisponible(Salle salle, LocalDate date, 
                                      LocalTime heureDebut, LocalTime heureFin) {
        
        // Vérifier les conflits avec les séances existantes
        List<SeanceClasse> conflitsSeances = seanceRepository
                .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                        salle, date, heureFin, heureDebut
                );

        if (!conflitsSeances.isEmpty()) {
            return false;
        }

        // Vérifier les conflits avec les réservations existantes
        List<Reservation> conflitsReservations = reservationRepository
                .findBySalleAndStatutAndPlageHoraireCreneauDisponibiliteDateAndPlageHoraireHeureDebutLessThanAndPlageHoraireHeureFinGreaterThan(
                        salle, StatutReservation.CONFIRMEE, date, heureFin, heureDebut
                );

        return conflitsReservations.isEmpty();
    }

    /**
     * Vérifier si une salle a une capacité suffisante pour une classe
     */
    public boolean aCapaciteSuffisante(UUID salleId, UUID classeId) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        return salle.getCapacite() >= classe.getEffectif();
    }

    /**
     * Créer une réservation de salle avec vérification de capacité
     */
    @Transactional
    public Reservation creerReservation(
            UUID salleId,
            UUID classeId,
            UUID coursId,
            UUID enseignantId,
            LocalDate date,
            LocalTime heureDebut,
            LocalTime heureFin) {

        // 1. Vérifier que la salle existe
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        // 2. Vérifier que la classe existe
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        // 3. VÉRIFICATION CRITIQUE: Capacité suffisante
        if (salle.getCapacite() < classe.getEffectif()) {
            throw new RuntimeException(
                String.format(
                    "Capacité insuffisante: la salle '%s' a une capacité de %d places " +
                    "mais la classe a un effectif de %d étudiants",
                    salle.getNomSalle(),
                    salle.getCapacite(),
                    classe.getEffectif()
                )
            );
        }

        // 4. Vérifier la disponibilité
        if (!estSalleDisponible(salle, date, heureDebut, heureFin)) {
            throw new RuntimeException(
                String.format(
                    "La salle '%s' n'est pas disponible le %s de %s à %s",
                    salle.getNomSalle(),
                    date,
                    heureDebut,
                    heureFin
                )
            );
        }

        // 5. Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setSalle(salle);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        // Ajouter les autres informations si disponibles
        if (coursId != null) {
            // Récupérer le cours (à implémenter selon votre logique)
        }

        if (enseignantId != null) {
            // Récupérer l'enseignant (à implémenter selon votre logique)
        }

        return reservationRepository.save(reservation);
    }

    /**
     * Réserver automatiquement la meilleure salle pour une classe
     * (celle avec capacité suffisante et la plus proche de l'effectif)
     */
    @Transactional
    public Salle reserverMeilleureSalle(
            UUID classeId,
            LocalDate date,
            LocalTime heureDebut,
            LocalTime heureFin) {

        List<SalleDisponible> sallesDisponibles = 
                getSallesAvecCapaciteSuffisante(classeId, date, heureDebut, heureFin);

        if (sallesDisponibles.isEmpty()) {
            throw new RuntimeException(
                "Aucune salle disponible avec capacité suffisante pour cette classe"
            );
        }

        // La première salle est la meilleure (grâce au tri)
        SalleDisponible meilleureSalle = sallesDisponibles.get(0);

        return meilleureSalle.getSalle();
    }

    /**
     * Obtenir des statistiques sur les salles disponibles
     */
    public Map<String, Object> getStatistiquesSalles(
            UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {

        List<SalleDisponible> sallesDisponibles = 
                getSallesDisponiblesAvecCapacite(classeId, date, heureDebut, heureFin);

        long sallesCapaciteSuffisante = sallesDisponibles.stream()
                .filter(SalleDisponible::isCapaciteSuffisante)
                .count();

        long sallesCapaciteInsuffisante = sallesDisponibles.stream()
                .filter(s -> !s.isCapaciteSuffisante())
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSallesDisponibles", sallesDisponibles.size());
        stats.put("sallesCapaciteSuffisante", sallesCapaciteSuffisante);
        stats.put("sallesCapaciteInsuffisante", sallesCapaciteInsuffisante);
        stats.put("salles", sallesDisponibles);

        return stats;
    }

    /**
     * Annuler une réservation
     */
    @Transactional
    public void annulerReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);
    }

    /**
     * Obtenir toutes les réservations d'une salle pour une date
     */
    public List<Reservation> getReservationsSalle(UUID salleId, LocalDate date) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        return reservationRepository.findBySalleAndPlageHoraireCreneauDisponibiliteDate(salle, date);
    }

    /**
     * Vérifier si une salle peut accueillir une classe (capacité uniquement)
     */
    public Map<String, Object> verifierCapacite(UUID salleId, UUID classeId) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        boolean capaciteSuffisante = salle.getCapacite() >= classe.getEffectif();
        int placesRestantes = salle.getCapacite() - classe.getEffectif();

        Map<String, Object> resultat = new HashMap<>();
        resultat.put("salle", salle.getNomSalle());
        resultat.put("capaciteSalle", salle.getCapacite());
        resultat.put("effectifClasse", classe.getEffectif());
        resultat.put("capaciteSuffisante", capaciteSuffisante);
        resultat.put("placesRestantes", placesRestantes);

        if (!capaciteSuffisante) {
            resultat.put("message", String.format(
                "Capacité insuffisante: il manque %d places",
                Math.abs(placesRestantes)
            ));
        } else {
            resultat.put("message", String.format(
                "Capacité suffisante: %d places disponibles",
                placesRestantes
            ));
        }

        return resultat;
    }
}
