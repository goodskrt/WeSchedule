package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.TypeCours;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cours {

    @Id
    @GeneratedValue
    private UUID idCours;

    private String intitule; //A supprimer car la concatenation du type de cours et de l'ue d'enseignant est suffisant

    @Enumerated(EnumType.STRING)
    private TypeCours typeCours;

    private Integer duree; // Nombre d'heures RESTANTES pour cette UE (ex: 45h restantes sur 60h total)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ue_id")
    private UE ue;

}