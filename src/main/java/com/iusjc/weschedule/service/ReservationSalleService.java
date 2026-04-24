package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutReservation;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationSalleService {

    private final SalleRepository salleRepository;
    private final ReservationRepository reservationRepository;
    private final SeanceClasseRepository seanceRepository;
    private final EquipmentRepository equipmentRepository;
    private final ClasseRepository classeRepository;
    private final EmploiDuTempsClasseRepository emploiDuTempsRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CoursRepository coursRepository;

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

        public Salle getSalle() { return salle; }
        public int getCapacite() { return capacite; }
        public int getEffectifClasse() { return effectifClasse; }
        public boolean isCapaciteSuffisante() { return capaciteSuffisante; }
        public int getPlacesRestantes() { return placesRestantes; }
    }

    @Transactional
    public void validerReservation(UUID reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));
        
        LocalDateTime start = res.getPlageHoraire() != null 
            ? LocalDateTime.of(res.getPlageHoraire().getCreneauDisponibilite().getDate(), res.getPlageHoraire().getHeureDebut())
            : res.getStartAt();
        LocalDateTime end = res.getPlageHoraire() != null 
            ? LocalDateTime.of(res.getPlageHoraire().getCreneauDisponibilite().getDate(), res.getPlageHoraire().getHeureFin())
            : res.getEndAt();

        if (!estSalleDisponible(res.getSalle(), start, end, res.getIdResa())) {
            throw new IllegalStateException("Conflit détecté : la salle n'est plus disponible.");
        }
        
        res.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(res);
    }

    @Transactional
    public void annulerReservation(UUID reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));
        res.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(res);
    }

    @Transactional
    public void marquerTerminee(UUID reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));
        res.setStatut(StatutReservation.TERMINEE);
        reservationRepository.save(res);
    }

    @Transactional
    public void supprimerReservation(UUID reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public boolean estSalleDisponible(Salle salle, LocalDate date, LocalTime start, LocalTime end, UUID excludeId) {
        return estSalleDisponible(salle, LocalDateTime.of(date, start), LocalDateTime.of(date, end), excludeId);
    }

    public boolean estSalleDisponible(Salle salle, LocalDate date, LocalTime start, LocalTime end) {
        return estSalleDisponible(salle, date, start, end, null);
    }

    public boolean estSalleDisponible(Salle salle, LocalDateTime start, LocalDateTime end, UUID excludeId) {
        LocalDate date = start.toLocalDate();
        LocalTime t1 = start.toLocalTime();
        LocalTime t2 = end.toLocalTime();

        if (seanceRepository.existsBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(salle, date, t2, t1)) {
            return false;
        }

        List<Reservation> conflits = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .filter(r -> r.getSalle().getIdSalle().equals(salle.getIdSalle()))
                .filter(r -> !r.getIdResa().equals(excludeId))
                .filter(r -> {
                    LocalDateTime rStart, rEnd;
                    if (r.getPlageHoraire() != null) {
                        rStart = LocalDateTime.of(r.getPlageHoraire().getCreneauDisponibilite().getDate(), r.getPlageHoraire().getHeureDebut());
                        rEnd = LocalDateTime.of(r.getPlageHoraire().getCreneauDisponibilite().getDate(), r.getPlageHoraire().getHeureFin());
                    } else {
                        rStart = r.getStartAt();
                        rEnd = r.getEndAt();
                    }
                    if (rStart == null || rEnd == null) return false;
                    return rStart.isBefore(end) && rEnd.isAfter(start);
                }).toList();

        return conflits.isEmpty();
    }

    @Transactional
    public Reservation creerReservation(Salle salle, PlageHoraire plageAt, Utilisateur demandeur, String motif, Set<Equipment> equipements, StatutReservation statutInitial) {
        LocalDateTime s = LocalDateTime.of(plageAt.getCreneauDisponibilite().getDate(), plageAt.getHeureDebut());
        LocalDateTime e = LocalDateTime.of(plageAt.getCreneauDisponibilite().getDate(), plageAt.getHeureFin());
        if (!estSalleDisponible(salle, s, e, null)) {
            throw new IllegalStateException("La salle est déjà occupée.");
        }
        Reservation res = new Reservation();
        res.setSalle(salle);
        res.setPlageHoraire(plageAt);
        res.setReservePar(demandeur);
        res.setMotif(motif);
        res.setEquipements(equipements);
        res.setStatut(statutInitial);
        return reservationRepository.save(res);
    }

    @Transactional
    public Reservation creerReservation(Salle salle, LocalDateTime start, LocalDateTime end, Utilisateur demandeur, String motif, Set<Equipment> equipements, StatutReservation statutInitial) {
        if (!estSalleDisponible(salle, start, end, null)) {
            throw new IllegalStateException("La salle est déjà occupée.");
        }
        Reservation res = new Reservation();
        res.setSalle(salle);
        res.setStartAt(start);
        res.setEndAt(end);
        res.setReservePar(demandeur);
        res.setMotif(motif);
        res.setEquipements(equipements);
        res.setStatut(statutInitial);
        return reservationRepository.save(res);
    }

    /** Legacy signature for compatibility */
    @Transactional
    public Reservation creerReservation(UUID salleId, UUID classeId, UUID coursId, UUID enseignantId, LocalDate date, LocalTime start, LocalTime end) {
        Salle salle = salleRepository.findById(salleId).orElseThrow();
        Utilisateur reservePar = utilisateurRepository.findById(enseignantId).orElseThrow();
        Cours cours = coursId != null ? coursRepository.findById(coursId).orElse(null) : null;

        Reservation res = new Reservation();
        res.setSalle(salle);
        res.setReservePar(reservePar);
        res.setCours(cours);
        res.setMotif("Séance de cours planifiée");
        res.setStatut(StatutReservation.CONFIRMEE);
        res.setStartAt(LocalDateTime.of(date, start));
        res.setEndAt(LocalDateTime.of(date, end));
        
        return reservationRepository.save(res);
    }

    public List<SalleDisponible> getSallesDisponiblesAvecCapacite(UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        Classe classe = classeRepository.findById(classeId).orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        int effectif = classe.getEffectif();
        return salleRepository.findAll().stream()
                .filter(s -> s.getStatut() == com.iusjc.weschedule.enums.StatutSalle.DISPONIBLE && estSalleDisponible(s, date, heureDebut, heureFin))
                .map(s -> new SalleDisponible(s, effectif))
                .sorted(Comparator.comparing(SalleDisponible::isCapaciteSuffisante).reversed()
                        .thenComparing(s -> Math.abs(s.getPlacesRestantes())))
                .collect(Collectors.toList());
    }

    public List<SalleDisponible> getSallesAvecCapaciteSuffisante(UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        return getSallesDisponiblesAvecCapacite(classeId, date, heureDebut, heureFin).stream()
                .filter(SalleDisponible::isCapaciteSuffisante).collect(Collectors.toList());
    }

    public boolean aCapaciteSuffisante(UUID salleId, UUID classeId) {
        Salle salle = salleRepository.findById(salleId).orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        Classe classe = classeRepository.findById(classeId).orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        return salle.getCapacite() >= classe.getEffectif();
    }

    public Map<String, Object> verifierCapacite(UUID salleId, UUID classeId) {
        Salle salle = salleRepository.findById(salleId).orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        Classe classe = classeRepository.findById(classeId).orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        boolean ok = salle.getCapacite() >= classe.getEffectif();
        int diff = salle.getCapacite() - classe.getEffectif();
        Map<String, Object> res = new HashMap<>();
        res.put("salle", salle.getNomSalle());
        res.put("capaciteSalle", salle.getCapacite());
        res.put("effectifClasse", classe.getEffectif());
        res.put("capaciteSuffisante", ok);
        res.put("placesRestantes", diff);
        res.put("message", ok ? "Capacité suffisante" : "Capacité insuffisante");
        return res;
    }

    @Transactional
    public Salle reserverMeilleureSalle(UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        List<SalleDisponible> dispo = getSallesAvecCapaciteSuffisante(classeId, date, heureDebut, heureFin);
        if (dispo.isEmpty()) throw new RuntimeException("Aucune salle disponible");
        return dispo.get(0).getSalle();
    }

    public Map<String, Object> getStatistiquesSalles(UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        List<SalleDisponible> dispo = getSallesDisponiblesAvecCapacite(classeId, date, heureDebut, heureFin);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSallesDisponibles", dispo.size());
        stats.put("sallesCapaciteSuffisante", dispo.stream().filter(SalleDisponible::isCapaciteSuffisante).count());
        stats.put("salles", dispo);
        return stats;
    }

    public Map<String, List<Equipment>> getEquipementsPourReservation(UUID salleId) {
        Salle salle = (salleId != null) ? salleRepository.findById(salleId).orElse(null) : null;
        List<Equipment> fixes = (salle != null) ? equipmentRepository.findBySalle(salle) : Collections.emptyList();
        List<Equipment> mobiles = equipmentRepository.findBySalleIsNull();
        Map<String, List<Equipment>> res = new HashMap<>();
        res.put("fixes", fixes);
        res.put("mobiles", mobiles);
        return res;
    }
}
