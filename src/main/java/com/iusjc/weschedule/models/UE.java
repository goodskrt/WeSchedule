package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.StatutUE;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"classes"})
@ToString(exclude = {"classes"})
public class UE {
    @Id
    @GeneratedValue
    private UUID idUE;

    private String intitule;
    private String code;
    private Integer duree; // Nombre d'heures TOTAL

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private Integer semestre; // 1 ou 2

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutUE statut; // ACTIF ou INACTIF

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ue_classe",
        joinColumns = @JoinColumn(name = "ue_id"),
        inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private Set<Classe> classes;
}