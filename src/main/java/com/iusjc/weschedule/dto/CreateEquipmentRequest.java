package com.iusjc.weschedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEquipmentRequest {

    @NotBlank(message = "Le nom de l'équipement est requis")
    private String name;

    @NotBlank(message = "La catégorie est requise")
    private String category;

    private String icon;

    private String description;

    @NotNull(message = "La quantité totale est requise")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer totalQuantity;

    private String status; // active, maintenance, retired
}
