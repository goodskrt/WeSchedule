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

    private String langue;

    private String description;

    private List<UUID> ueIds;
}
