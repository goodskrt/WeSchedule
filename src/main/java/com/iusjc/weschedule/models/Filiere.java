package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "filieres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filiere {

    @Id
    @GeneratedValue
    private UUID idFiliere;

    private String nomFiliere;
    private String description;

    /** École à laquelle cette filière appartient */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecole_id")
    private Ecole ecole;

    /**
     * Niveaux disponibles dans cette filière.
     * Stockés comme liste de strings séparées par virgule en base.
     * Ex: ["Niveau 1", "Niveau 2", "Niveau 3"]
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "filiere_niveaux", joinColumns = @JoinColumn(name = "filiere_id"))
    @Column(name = "niveau")
    private List<String> niveaux;
}
