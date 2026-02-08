package com.iusjc.weschedule.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquipmentRequest {

    private String name;

    private String category;

    private String icon;

    private String description;

    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer totalQuantity;

    private String status; // active, maintenance, retired
}
