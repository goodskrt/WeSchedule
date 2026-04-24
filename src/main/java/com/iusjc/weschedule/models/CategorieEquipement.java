package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Catégorie de matériel (ex. Audiovisuel, Informatique). Référencée par les types d'équipement.
 */
@Entity
@Table(name = "categories_equipement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieEquipement {

    @Id
    @GeneratedValue
    private UUID id;

    /** Libellé affiché (ex. « Audiovisuel »). */
    @Column(nullable = false, unique = true, length = 120)
    private String nom;

    /**
     * Code stable pour import Excel / API (ex. AUDIOVISUEL). Unique.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Ordre d'affichage dans les listes déroulantes. */
    private Integer ordre;
}
