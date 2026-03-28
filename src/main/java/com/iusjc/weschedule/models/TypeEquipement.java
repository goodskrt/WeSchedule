package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.CategorieEquipement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "types_equipement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeEquipement {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieEquipement categorie;

    @Column(columnDefinition = "TEXT")
    private String description;
}
