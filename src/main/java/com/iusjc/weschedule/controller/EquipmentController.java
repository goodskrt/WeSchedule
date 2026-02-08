package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.dto.*;
import com.iusjc.weschedule.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/equipments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ========== EQUIPMENT CRUD OPERATIONS ==========

    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentResponse>> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {
        EquipmentResponse response = equipmentService.createEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Équipement créé avec succès"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getAllEquipments() {
        List<EquipmentResponse> equipments = equipmentService.getAllEquipments();
        return ResponseEntity.ok(
                ApiResponse.success(equipments, "Équipements récupérés avec succès"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> getEquipmentById(@PathVariable UUID id) {
        EquipmentResponse response = equipmentService.getEquipmentById(id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Équipement récupéré avec succès"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getEquipmentsByCategory(
            @PathVariable String category) {
        List<EquipmentResponse> equipments = equipmentService.getEquipmentsByCategory(category);
        return ResponseEntity.ok(
                ApiResponse.success(equipments, "Équipements de la catégorie récupérés avec succès"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getEquipmentsByStatus(
            @PathVariable String status) {
        List<EquipmentResponse> equipments = equipmentService.getEquipmentsByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success(equipments, "Équipements avec le statut récupérés avec succès"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEquipmentRequest request) {
        EquipmentResponse response = equipmentService.updateEquipment(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Équipement mis à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable UUID id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Équipement supprimé avec succès"));
    }

    // ========== ASSIGNMENT OPERATIONS ==========

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<EquipmentAssignmentResponse>> createAssignment(
            @Valid @RequestBody CreateEquipmentAssignmentRequest request,
            Authentication authentication) {
        String assignedBy = authentication.getName();
        EquipmentAssignmentResponse response = equipmentService.createAssignment(request, assignedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Affectation créée avec succès"));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<EquipmentAssignmentResponse>>> getAllAssignments() {
        List<EquipmentAssignmentResponse> assignments = equipmentService.getAllAssignments();
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Affectations récupérées avec succès"));
    }

    @GetMapping("/{equipmentId}/assignments")
    public ResponseEntity<ApiResponse<List<EquipmentAssignmentResponse>>> getAssignmentsByEquipment(
            @PathVariable UUID equipmentId) {
        List<EquipmentAssignmentResponse> assignments = 
                equipmentService.getAssignmentsByEquipment(equipmentId);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Affectations de l'équipement récupérées avec succès"));
    }

    @GetMapping("/assignments/target/{targetId}")
    public ResponseEntity<ApiResponse<List<EquipmentAssignmentResponse>>> getAssignmentsByTarget(
            @PathVariable UUID targetId,
            @RequestParam String assignmentType) {
        List<EquipmentAssignmentResponse> assignments = 
                equipmentService.getAssignmentsByTarget(targetId, assignmentType);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Affectations de la cible récupérées avec succès"));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> cancelAssignment(@PathVariable UUID assignmentId) {
        equipmentService.cancelAssignment(assignmentId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Affectation annulée avec succès"));
    }
}
