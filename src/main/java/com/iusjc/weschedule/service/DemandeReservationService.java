package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.enums.StatutDemandeReservation;
import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.DemandeReservationSalle;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.DemandeReservationSalleRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandeReservationService {

    private static final List<StatutDemandeReservation> STATUTS_BLOQUANTS =
            List.of(StatutDemandeReservation.EN_ATTENTE, StatutDemandeReservation.ACCEPTEE);

    private final DemandeReservationSalleRepository demandeRepository;
    private final SalleRepository salleRepository;
    private final EquipmentRepository equipmentRepository;

    public List<Equipment> listerEquipementsDisponibles() {
        return equipmentRepository.findByStatut(StatutEquipement.DISPONIBLE).stream()
                .sorted(java.util.Comparator.comparing(Equipment::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public DemandeReservationSalle creerDemande(
            Utilisateur enseignant,
            UUID salleId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String motif,
            List<UUID> equipmentIds) {
        if (enseignant == null || enseignant.getRole() != Role.ENSEIGNANT) {
            throw new IllegalArgumentException("Seuls les enseignants peuvent créer une demande de réservation");
        }
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("Le motif est obligatoire");
        }
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("Les dates sont invalides : la fin doit être après le début");
        }
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        Set<Equipment> equipements = new HashSet<>();
        if (equipmentIds != null) {
            for (UUID eid : new HashSet<>(equipmentIds)) {
                Equipment e = equipmentRepository.findById(eid)
                        .orElseThrow(() -> new IllegalArgumentException("Équipement introuvable : " + eid));
                if (e.getStatut() != StatutEquipement.DISPONIBLE) {
                    throw new IllegalArgumentException(
                            "L'équipement « " + e.getNom() + " » n'est pas disponible pour réservation");
                }
                equipements.add(e);
            }
        }

        assertCreneauDisponible(salle.getIdSalle(), startAt, endAt, equipements, null);

        DemandeReservationSalle d = new DemandeReservationSalle();
        d.setEnseignant(enseignant);
        d.setSalle(salle);
        d.setStartAt(startAt);
        d.setEndAt(endAt);
        d.setMotif(motif.trim());
        d.setStatut(StatutDemandeReservation.EN_ATTENTE);
        d.setEquipements(equipements);
        return demandeRepository.save(d);
    }

    public void assertCreneauDisponible(
            UUID salleId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Set<Equipment> equipements,
            UUID excludeDemandeId) {
        if (demandeRepository.existsSalleCreneauConflict(salleId, startAt, endAt, STATUTS_BLOQUANTS, excludeDemandeId)) {
            throw new IllegalArgumentException("La salle est déjà réservée ou en demande sur ce créneau");
        }
        for (Equipment e : equipements) {
            if (demandeRepository.existsEquipementCreneauConflict(
                    e.getId(), startAt, endAt, STATUTS_BLOQUANTS, excludeDemandeId)) {
                throw new IllegalArgumentException(
                        "L'équipement « " + e.getNom() + " » est déjà demandé ou réservé sur ce créneau");
            }
        }
    }

    @Transactional
    public void accepterDemande(UUID demandeId, Utilisateur admin, String adminEmail) {
        if (admin == null || admin.getRole() != Role.ADMINISTRATEUR) {
            throw new IllegalArgumentException("Action réservée aux administrateurs");
        }
        DemandeReservationSalle d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        if (d.getStatut() != StatutDemandeReservation.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les demandes en attente peuvent être acceptées");
        }
        assertCreneauDisponible(
                d.getSalle().getIdSalle(), d.getStartAt(), d.getEndAt(), d.getEquipements(), d.getId());
        d.setStatut(StatutDemandeReservation.ACCEPTEE);
        d.setTraiteLe(LocalDateTime.now());
        d.setTraiteParEmail(adminEmail);
        d.setCommentaireAdmin(null);
        demandeRepository.save(d);
    }

    @Transactional
    public void refuserDemande(UUID demandeId, Utilisateur admin, String adminEmail, String commentaire) {
        if (admin == null || admin.getRole() != Role.ADMINISTRATEUR) {
            throw new IllegalArgumentException("Action réservée aux administrateurs");
        }
        DemandeReservationSalle d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        if (d.getStatut() != StatutDemandeReservation.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les demandes en attente peuvent être refusées");
        }
        String c = commentaire != null ? commentaire.trim() : "";
        if (c.isEmpty()) {
            throw new IllegalArgumentException("Un motif de refus est obligatoire");
        }
        d.setStatut(StatutDemandeReservation.REFUSEE);
        d.setCommentaireAdmin(c);
        d.setTraiteLe(LocalDateTime.now());
        d.setTraiteParEmail(adminEmail);
        demandeRepository.save(d);
    }

    @Transactional
    public void annulerParEnseignant(UUID demandeId, Utilisateur enseignant) {
        if (enseignant == null || enseignant.getRole() != Role.ENSEIGNANT) {
            throw new IllegalArgumentException("Action non autorisée");
        }
        DemandeReservationSalle d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        if (!d.getEnseignant().getIdUser().equals(enseignant.getIdUser())) {
            throw new IllegalArgumentException("Cette demande ne vous appartient pas");
        }
        if (d.getStatut() != StatutDemandeReservation.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les demandes en attente peuvent être annulées");
        }
        d.setStatut(StatutDemandeReservation.ANNULEE);
        demandeRepository.save(d);
    }

    public List<DemandeReservationSalle> listerPourEnseignant(Utilisateur enseignant) {
        return demandeRepository.findByEnseignantOrderByCreatedAtDesc(enseignant);
    }

    public List<DemandeReservationSalle> listerToutes() {
        return demandeRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Pour formulaires : liste globale d'équipements disponibles. */
    public List<Equipment> equipementsDisponibles() {
        return listerEquipementsDisponibles();
    }
}
