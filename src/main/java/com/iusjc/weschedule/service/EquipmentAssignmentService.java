package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.EquipmentAssignment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.EquipmentAssignmentRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import com.iusjc.weschedule.util.EquipmentStatutRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentAssignmentService {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String TYPE_ROOM = "room";
    public static final String TYPE_CLASS = "class";

    private final EquipmentAssignmentRepository assignmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Vérifie que la période [startAt, endAt] ne chevauche aucune affectation <strong>active</strong>
     * du même équipement (hors {@code excludeAssignmentId}).
     */
    public void assertNoOverlap(Equipment equipment, LocalDateTime startAt, LocalDateTime endAt, UUID excludeAssignmentId) {
        List<EquipmentAssignment> actives = assignmentRepository.findByEquipmentAndStatus(equipment, STATUS_ACTIVE);
        for (EquipmentAssignment other : actives) {
            if (excludeAssignmentId != null && excludeAssignmentId.equals(other.getId())) {
                continue;
            }
            if (intervalsOverlap(startAt, endAt, other.getStartAt(), other.getEndAt())) {
                throw new IllegalArgumentException(
                        "Chevauchement avec une affectation existante du "
                                + other.getStartAt()
                                + (other.getEndAt() != null ? " au " + other.getEndAt() : " (permanente)")
                                + ". Ajustez les dates ou terminez l'autre affectation.");
            }
        }
    }

    public static boolean intervalsOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        LocalDateTime end1 = e1 != null ? e1 : LocalDateTime.MAX;
        LocalDateTime end2 = e2 != null ? e2 : LocalDateTime.MAX;
        return !s1.isAfter(end2) && !s2.isAfter(end1);
    }

    /**
     * Affectation permanente en salle lors de la création d'un équipement (ou import) avec salle renseignée.
     */
    @Transactional
    public void createInitialPermanentRoomAssignment(Equipment equipment, Salle salle, Utilisateur responsable, String assignedByLabel) {
        if (equipment == null || salle == null || responsable == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        EquipmentAssignment a = new EquipmentAssignment();
        a.setEquipment(equipment);
        a.setAssignmentType(TYPE_ROOM);
        a.setTargetId(salle.getIdSalle());
        a.setQuantity(Integer.valueOf(1));
        a.setStartAt(now);
        a.setEndAt(null);
        a.setDuration("permanent");
        a.setReason("Affectation initiale à la création de l'équipement");
        a.setNotes(null);
        a.setStatus(STATUS_ACTIVE);
        a.setAssignedBy(assignedByLabel);
        a.setResponsable(responsable);
        assertNoOverlap(equipment, now, null, null);
        assignmentRepository.save(a);
    }

    /**
     * Premier utilisateur administrateur (secours pour import hors session).
     */
    public Utilisateur findFallbackAdminResponsable() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMINISTRATEUR)
                .min(Comparator.comparing(Utilisateur::getEmail))
                .orElse(null);
    }

    /**
     * Vérifie si une affectation est actuellement active temporellement.
     */
    public boolean isAssignmentCurrent(EquipmentAssignment a) {
        if (a == null || !STATUS_ACTIVE.equals(a.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = !a.getStartAt().isAfter(now);
        boolean beforeEnd = (a.getEndAt() == null || !a.getEndAt().isBefore(now));
        return afterStart && beforeEnd;
    }

    /**
     * Après création ou activation d'une affectation vers une salle : met à jour la salle courante de l'équipement
     * si l'affectation est actuellement en cours.
     */
    @Transactional
    public void syncEquipmentSalleFromRoomAssignment(Equipment equipment, UUID salleId, EquipmentAssignment assignment) {
        if (!isAssignmentCurrent(assignment)) {
            return;
        }
        salleRepository.findById(salleId).ifPresent(s -> {
            equipment.setSalle(s);
            EquipmentStatutRules.syncStatutAvecSalle(equipment, StatutEquipement.DISPONIBLE);
            equipmentRepository.save(equipment);
        });
    }

    @Transactional
    public void onDeleteActiveRoomAssignment(EquipmentAssignment deleted) {
        if (deleted == null || !STATUS_ACTIVE.equals(deleted.getStatus())) {
            return;
        }
        if (!TYPE_ROOM.equalsIgnoreCase(deleted.getAssignmentType())) {
            return;
        }
        Equipment eq = deleted.getEquipment();
        if (eq == null) {
            return;
        }
        UUID sid = deleted.getTargetId();
        if (eq.getSalle() != null && eq.getSalle().getIdSalle().equals(sid)) {
            eq.setSalle(null);
            EquipmentStatutRules.syncStatutAvecSalle(eq, StatutEquipement.DISPONIBLE);
            equipmentRepository.save(eq);
        }
    }

    @Transactional
    public void supprimerAffectation(UUID assignmentId, UUID equipmentId) {
        EquipmentAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Affectation introuvable"));
        if (a.getEquipment() == null || !a.getEquipment().getId().equals(equipmentId)) {
            throw new IllegalArgumentException("Affectation ne correspond pas à cet équipement");
        }
        onDeleteActiveRoomAssignment(a);
        assignmentRepository.delete(a);
    }

    /**
     * Met à jour une affectation existante (validation d'unicité des créneaux identique à la création).
     */
    @Transactional
    public void mettreAJourAffectation(
            UUID assignmentId,
            UUID equipmentId,
            String assignmentType,
            UUID targetId,
            int quantity,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String duration,
            String reason,
            String notes,
            Utilisateur responsable) {
        EquipmentAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Affectation introuvable"));
        Equipment eq = a.getEquipment();
        if (eq == null || !eq.getId().equals(equipmentId)) {
            throw new IllegalArgumentException("Affectation ne correspond pas à cet équipement");
        }

        String prevType = a.getAssignmentType();
        UUID prevTarget = a.getTargetId();
        String prevStatus = a.getStatus();

        assertNoOverlap(eq, startAt, endAt, assignmentId);

        a.setAssignmentType(assignmentType);
        a.setTargetId(targetId);
        a.setQuantity(Math.max(1, quantity));
        a.setStartAt(startAt);
        a.setEndAt(endAt);
        a.setDuration(duration);
        a.setReason(reason);
        a.setNotes(notes);
        a.setResponsable(responsable);

        assignmentRepository.save(a);
        reconcileEquipmentSalleAfterAssignmentUpdate(eq, prevType, prevTarget, prevStatus, a);
    }

    private void reconcileEquipmentSalleAfterAssignmentUpdate(
            Equipment eq,
            String previousType,
            UUID previousTargetId,
            String previousStatus,
            EquipmentAssignment updated) {
        String newType = updated.getAssignmentType();
        UUID newTarget = updated.getTargetId();
        String newStatus = updated.getStatus();

        boolean prevWasLinkedRoom = TYPE_ROOM.equalsIgnoreCase(previousType)
                && STATUS_ACTIVE.equals(previousStatus)
                && eq.getSalle() != null
                && previousTargetId != null
                && previousTargetId.equals(eq.getSalle().getIdSalle());
        boolean newIsLinkedRoom = TYPE_ROOM.equalsIgnoreCase(newType) 
                && STATUS_ACTIVE.equals(newStatus)
                && isAssignmentCurrent(updated);

        if (prevWasLinkedRoom && !newIsLinkedRoom) {
            eq.setSalle(null);
            EquipmentStatutRules.syncStatutAvecSalle(eq, StatutEquipement.DISPONIBLE);
            equipmentRepository.save(eq);
        } else if (newIsLinkedRoom) {
            syncEquipmentSalleFromRoomAssignment(eq, newTarget, updated);
        }
    }
}
