package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.StatutCours;
import com.iusjc.weschedule.enums.TypeCours;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"classes"})
@ToString(exclude = {"classes"})
public class Cours {

    @Id
    @GeneratedValue
    private UUID idCours;

    private String intitule; //A supprimer car la concatenation du type de cours et de l'ue d'enseignant est suffisant

    @Enumerated(EnumType.STRING)
    private TypeCours typeCours;

    private Integer duree; // Nombre d'heures RESTANTES pour cette UE (ex: 45h restantes sur 60h total)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ue_id")
    private UE ue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "cours_classe",
        joinColumns = @JoinColumn(name = "cours_id"),
        inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private Set<Classe> classes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCours statut = StatutCours.ACTIF;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}