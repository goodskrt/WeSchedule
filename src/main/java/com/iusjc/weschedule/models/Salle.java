package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.StatutSalle;
import com.iusjc.weschedule.enums.TypeSalle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "salles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "equipements")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSalle statut = StatutSalle.DISPONIBLE;

    @OneToMany(mappedBy = "salle", fetch = FetchType.LAZY)
    private List<Equipment> equipements = new ArrayList<>();
}
