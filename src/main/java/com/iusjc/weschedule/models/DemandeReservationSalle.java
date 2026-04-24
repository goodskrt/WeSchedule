package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.StatutDemandeReservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "demandes_reservation_salle")
@Data
@EqualsAndHashCode(exclude = {"equipements", "enseignant", "salle"})
@ToString(exclude = {"equipements", "enseignant", "salle"})
@NoArgsConstructor
@AllArgsConstructor
public class DemandeReservationSalle {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Utilisateur enseignant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemandeReservation statut = StatutDemandeReservation.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String commentaireAdmin;

    private LocalDateTime traiteLe;

    /** Email de l'administrateur ayant traité la demande. */
    private String traiteParEmail;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "demande_reservation_equipement",
            joinColumns = @JoinColumn(name = "demande_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<Equipment> equipements = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
