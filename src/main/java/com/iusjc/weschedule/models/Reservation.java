package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.StatutReservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue
    private UUID idResa;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String motif;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reservation_equipment",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private java.util.Set<Equipment> equipements = new java.util.HashSet<>();

    // Relations
    @ManyToOne
    private Salle salle;

    @ManyToOne
    private PlageHoraire plageHoraire;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @ManyToOne
    private Cours cours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserve_par_id", nullable = false)
    private Utilisateur reservePar;
}