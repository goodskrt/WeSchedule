package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.TypeCours;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "creneaux_emploi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreneauEmploi {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_de_temps_id", nullable = false)
    private EmploiDeTemps emploiDeTemps;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ue_id")
    private UE ue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @Enumerated(EnumType.STRING)
    private TypeCours typeCours;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;
}
