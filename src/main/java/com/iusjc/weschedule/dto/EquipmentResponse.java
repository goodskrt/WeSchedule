package com.iusjc.weschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentResponse {

    private UUID id;
    private String name;
    private String category;
    private String categoryName;
    private String icon;
    private String description;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer assignedQuantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AssignmentSimpleResponse> activeAssignments;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentSimpleResponse {
        private UUID id;
        private String assignmentType;
        private String targetName;
        private Integer quantity;
        private String duration;
    }
}
