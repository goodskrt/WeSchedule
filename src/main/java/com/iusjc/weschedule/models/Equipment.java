package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.StatutEquipement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "equipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue
    private UUID id;

    // ── Champs système Thymeleaf (EquipmentController) ──────────────────────
    @Column(nullable = true)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_equipement_id")
    private TypeEquipement typeEquipement;

    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    private StatutEquipement statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id")
    private Salle salle;

    private String photo;

    // ── Champs système Angular (EquipmentService) ───────────────────────────
    private String name;

    private String category;

    private String icon;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalQuantity;

    private Integer availableQuantity;

    private String status = "active";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EquipmentAssignment> assignments;
}
