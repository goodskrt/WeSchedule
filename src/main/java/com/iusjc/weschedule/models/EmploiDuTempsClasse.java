package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "emplois_du_temps_classe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTempsClasse {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;
    
    @Column(nullable = false)
    private LocalDate dateDebut;
    
    @Column(nullable = false)
    private LocalDate dateFin;
    
    @Column(nullable = false)
    private Integer semaine; // Numéro de la semaine dans l'année
    
    @Column(nullable = false)
    private Integer annee; // Année
    
    @OneToMany(mappedBy = "emploiDuTemps", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SeanceClasse> seances;
}
