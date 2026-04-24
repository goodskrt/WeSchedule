package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "emploidetemps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDeTemps {

    @Id
    @GeneratedValue
    private UUID idEDT;

    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    @ManyToOne
    private Ecole ecole;
}
