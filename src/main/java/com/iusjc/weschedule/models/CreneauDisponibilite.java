package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "creneaux_disponibilite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreneauDisponibilite {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disponibilite_id", nullable = false)
    private DisponibiliteEnseignant disponibilite;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @OneToMany(mappedBy = "creneauDisponibilite", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PlageHoraire> plagesHoraires;
}