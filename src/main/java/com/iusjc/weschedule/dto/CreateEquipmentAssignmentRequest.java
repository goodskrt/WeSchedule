package com.iusjc.weschedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEquipmentAssignmentRequest {

    @NotNull(message = "L'équipement est requis")
    private UUID equipmentId;

    @NotBlank(message = "Le type d'affectation est requis")
    private String assignmentType; // room, class

    @NotNull(message = "La cible est requise")
    private UUID targetId; // roomId or classId

    @NotNull(message = "La quantité est requise")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantity;

    @NotNull(message = "La date de début est requise")
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @NotNull(message = "Le responsable est requis")
    private UUID responsableId;

    @NotBlank(message = "La durée est requise")
    private String duration; // permanent, temporary

    @NotBlank(message = "La raison est requise")
    private String reason;

    private String notes;
}
