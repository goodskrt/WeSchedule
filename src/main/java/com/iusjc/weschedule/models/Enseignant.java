package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "enseignants")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"uesEnseignees", "disponibilites", "ecoles", "cours"})
@ToString(exclude = {"uesEnseignees", "disponibilites", "ecoles", "cours"})
public class Enseignant extends Utilisateur {
    private String grade;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "enseignant_specialites",
        joinColumns = @JoinColumn(name = "enseignant_id")
    )
    @Column(name = "specialite")
    private Set<String> specialites;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enseignant_ue",
        joinColumns = @JoinColumn(name = "enseignant_id"),
        inverseJoinColumns = @JoinColumn(name = "ue_id")
    )
    private Set<UE> uesEnseignees;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enseignant_ecole",
        joinColumns = @JoinColumn(name = "enseignant_id"),
        inverseJoinColumns = @JoinColumn(name = "ecole_id")
    )
    private Set<Ecole> ecoles;
    
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DisponibiliteEnseignant> disponibilites;
    
    @OneToMany(mappedBy = "enseignant", fetch = FetchType.LAZY)
    private Set<Cours> cours;
}