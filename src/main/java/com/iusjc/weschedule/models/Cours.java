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

    private String intitule;

    @Enumerated(EnumType.STRING)
    private TypeCours typeCours;

    private Integer duree; // en minutes

}