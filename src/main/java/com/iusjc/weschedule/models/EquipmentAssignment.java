package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
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
    private UUID targetId; // roomId or classId

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // Optional for permanent assignments

    @Column(nullable = false)
    private String duration; // permanent, temporary

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String status = "active"; // active, expired, cancelled

    @Column(nullable = false)
    private String assignedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime assignedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
