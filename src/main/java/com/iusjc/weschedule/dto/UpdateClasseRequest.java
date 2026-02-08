package com.iusjc.weschedule.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClasseRequest {

    private String nom;

    private UUID ecoleId;

    private UUID filiereId;

    private String niveau;

    @Min(value = 0, message = "L'effectif ne peut pas être négatif")
    private Integer effectif;

    private Integer semestre;

    @Min(value = 1, message = "L'effectif maximum doit être supérieur à 0")
    private Integer effectifMax;

    private String responsable;

    private String description;

    private String specialite;

    private List<UUID> ueIds;
}
