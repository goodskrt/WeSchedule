package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classe {

    @Id
    @GeneratedValue
    private UUID idClasse;

    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecole_id")
    private Ecole ecole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    private String niveau;

    private Integer effectif;

    /** Langue d'enseignement (ex: Français, Anglais, Bilingue) */
    private String langue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "classes", fetch = FetchType.LAZY)
    private Set<UE> ues;
}
