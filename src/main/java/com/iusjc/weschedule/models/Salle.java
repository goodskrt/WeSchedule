package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.TypeSalle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "salles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salle {

    @Id
    @GeneratedValue
    private UUID idSalle;

    private String nomSalle;

    @Enumerated(EnumType.STRING)
    private TypeSalle typeSalle;

    private Integer capacite;

    /** RDC, 1er étage, 2ème étage, 3ème étage */
    private String etage;

    /** Nouveau bâtiment, Ancien bâtiment */
    private String batiment;
}