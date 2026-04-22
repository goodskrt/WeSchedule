package com.iusjc.weschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentAssignmentResponse {

    private UUID id;
    private UUID equipmentId;
    private String equipmentName;
    private String assignmentType;
    private UUID targetId;
    private String targetName;
    private Integer quantity;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private UUID responsableId;
    private String responsableNomComplet;
    private String duration;
    private String reason;
    private String status;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private String notes;
}
