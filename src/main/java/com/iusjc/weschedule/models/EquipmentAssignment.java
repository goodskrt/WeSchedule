package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipment_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private String assignmentType; // room, class

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private Integer quantity;

    /** Début de la période d'affectation (date et heure). */
    @Column(nullable = false)
    private LocalDateTime startAt;

    /**
     * Fin de la période (date et heure), null si affectation permanente.
     */
    private LocalDateTime endAt;

    @Column(nullable = false)
    private String duration; // permanent, temporary

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /** active, expired, cancelled */
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String assignedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime assignedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Utilisateur responsable du matériel pendant la période d'affectation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Utilisateur responsable;
}
