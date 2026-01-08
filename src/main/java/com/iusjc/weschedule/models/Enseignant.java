package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "enseignants")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"uesEnseignees", "disponibilites"})
@ToString(exclude = {"uesEnseignees", "disponibilites"})
public class Enseignant extends Utilisateur {
    private String grade;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enseignant_ue",
        joinColumns = @JoinColumn(name = "enseignant_id"),
        inverseJoinColumns = @JoinColumn(name = "ue_id")
    )
    private Set<UE> uesEnseignees;
    
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DisponibiliteEnseignant> disponibilites;
}