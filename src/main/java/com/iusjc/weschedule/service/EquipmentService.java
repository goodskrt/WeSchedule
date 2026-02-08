package com.iusjc.weschedule.service;

import com.iusjc.weschedule.dto.*;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.EquipmentAssignment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.EquipmentAssignmentRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentAssignmentRepository assignmentRepository;
    private final SalleRepository salleRepository;
    private final ClasseRepository classeRepository;

    private static final Map<String, String> CATEGORY_NAMES = new HashMap<>();
    
    static {
        CATEGORY_NAMES.put("audiovisuel", "Audiovisuel");
        CATEGORY_NAMES.put("informatique", "Informatique");
        CATEGORY_NAMES.put("ecriture", "Écriture");
        CATEGORY_NAMES.put("confort", "Confort");
        CATEGORY_NAMES.put("connectivite", "Connectivité");
        CATEGORY_NAMES.put("mobilier", "Mobilier");
    }

    @Transactional
    public EquipmentResponse createEquipment(CreateEquipmentRequest request) {
        log.info("Creating new equipment: {}", request.getName());

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setCategory(request.getCategory());
        equipment.setIcon(request.getIcon() != null ? request.getIcon() : "📦");
        equipment.setDescription(request.getDescription());
        equipment.setTotalQuantity(request.getTotalQuantity());
        equipment.setAvailableQuantity(request.getTotalQuantity());
        equipment.setStatus(request.getStatus() != null ? request.getStatus() : "active");

        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipment created successfully with ID: {}", savedEquipment.getId());

        return mapToResponse(savedEquipment);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipments() {
        log.info("Fetching all equipments");
        return equipmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(UUID id) {
        log.info("Fetching equipment with ID: {}", id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé"));
        return mapToResponse(equipment);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipmentsByCategory(String category) {
        log.info("Fetching equipments for category: {}", category);
        return equipmentRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipmentsByStatus(String status) {
        log.info("Fetching equipments with status: {}", status);
        return equipmentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipmentResponse updateEquipment(UUID id, UpdateEquipmentRequest request) {
        log.info("Updating equipment with ID: {}", id);

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé"));

        if (request.getName() != null) {
            equipment.setName(request.getName());
        }
        if (request.getCategory() != null) {
            equipment.setCategory(request.getCategory());
        }
        if (request.getIcon() != null) {
            equipment.setIcon(request.getIcon());
        }
        if (request.getDescription() != null) {
            equipment.setDescription(request.getDescription());
        }
        if (request.getTotalQuantity() != null) {
            int difference = request.getTotalQuantity() - equipment.getTotalQuantity();
            equipment.setTotalQuantity(request.getTotalQuantity());
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + difference);
        }
        if (request.getStatus() != null) {
            equipment.setStatus(request.getStatus());
        }

        Equipment updatedEquipment = equipmentRepository.save(equipment);
        log.info("Equipment updated successfully");

        return mapToResponse(updatedEquipment);
    }

    @Transactional
    public void deleteEquipment(UUID id) {
        log.info("Deleting equipment with ID: {}", id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé"));
        
        // Vérifier s'il y a des affectations actives
        List<EquipmentAssignment> activeAssignments = assignmentRepository
                .findByEquipmentAndStatus(equipment, "active");
        
        if (!activeAssignments.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un équipement avec des affectations actives");
        }
        
        equipmentRepository.delete(equipment);
        log.info("Equipment deleted successfully");
    }

    // ========== ASSIGNMENT OPERATIONS ==========

    @Transactional
    public EquipmentAssignmentResponse createAssignment(CreateEquipmentAssignmentRequest request, String assignedBy) {
        log.info("Creating new equipment assignment");

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé"));

        // Vérifier la disponibilité
        if (equipment.getAvailableQuantity() < request.getQuantity()) {
            throw new RuntimeException("Quantité insuffisante disponible");
        }

        // Valider la cible selon le type
        validateTarget(request.getTargetId(), request.getAssignmentType());

        EquipmentAssignment assignment = new EquipmentAssignment();
        assignment.setEquipment(equipment);
        assignment.setAssignmentType(request.getAssignmentType());
        assignment.setTargetId(request.getTargetId());
        assignment.setQuantity(request.getQuantity());
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());
        assignment.setDuration(request.getDuration());
        assignment.setReason(request.getReason());
        assignment.setStatus("active");
        assignment.setAssignedBy(assignedBy);
        assignment.setNotes(request.getNotes());

        EquipmentAssignment savedAssignment = assignmentRepository.save(assignment);

        // Mettre à jour la quantité disponible
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - request.getQuantity());
        equipmentRepository.save(equipment);

        log.info("Equipment assignment created successfully with ID: {}", savedAssignment.getId());

        return mapAssignmentToResponse(savedAssignment);
    }

    @Transactional(readOnly = true)
    public List<EquipmentAssignmentResponse> getAllAssignments() {
        log.info("Fetching all equipment assignments");
        return assignmentRepository.findAll().stream()
                .map(this::mapAssignmentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentAssignmentResponse> getAssignmentsByEquipment(UUID equipmentId) {
        log.info("Fetching assignments for equipment ID: {}", equipmentId);
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé"));
        return assignmentRepository.findByEquipment(equipment).stream()
                .map(this::mapAssignmentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentAssignmentResponse> getAssignmentsByTarget(UUID targetId, String assignmentType) {
        log.info("Fetching assignments for target ID: {} and type: {}", targetId, assignmentType);
        return assignmentRepository.findByTargetIdAndAssignmentType(targetId, assignmentType).stream()
                .map(this::mapAssignmentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelAssignment(UUID assignmentId) {
        log.info("Cancelling assignment with ID: {}", assignmentId);

        EquipmentAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée"));

        if (!"active".equals(assignment.getStatus())) {
            throw new RuntimeException("Seules les affectations actives peuvent être annulées");
        }

        // Marquer comme annulée
        assignment.setStatus("cancelled");
        assignmentRepository.save(assignment);

        // Remettre la quantité dans le stock disponible
        Equipment equipment = assignment.getEquipment();
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + assignment.getQuantity());
        equipmentRepository.save(equipment);

        log.info("Assignment cancelled successfully");
    }

    // ========== HELPER METHODS ==========

    private void validateTarget(UUID targetId, String assignmentType) {
        if ("room".equals(assignmentType)) {
            salleRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        } else if ("class".equals(assignmentType)) {
            classeRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        } else {
            throw new RuntimeException("Type d'affectation invalide");
        }
    }

    private String getTargetName(UUID targetId, String assignmentType) {
        if ("room".equals(assignmentType)) {
            return salleRepository.findById(targetId)
                    .map(Salle::getNomSalle)
                    .orElse("Salle inconnue");
        } else if ("class".equals(assignmentType)) {
            return classeRepository.findById(targetId)
                    .map(Classe::getNom)
                    .orElse("Classe inconnue");
        }
        return "Cible inconnue";
    }

    private EquipmentResponse mapToResponse(Equipment equipment) {
        List<EquipmentResponse.AssignmentSimpleResponse> activeAssignments = null;
        
        if (equipment.getAssignments() != null) {
            activeAssignments = equipment.getAssignments().stream()
                    .filter(a -> "active".equals(a.getStatus()))
                    .map(a -> EquipmentResponse.AssignmentSimpleResponse.builder()
                            .id(a.getId())
                            .assignmentType(a.getAssignmentType())
                            .targetName(getTargetName(a.getTargetId(), a.getAssignmentType()))
                            .quantity(a.getQuantity())
                            .duration(a.getDuration())
                            .build())
                    .collect(Collectors.toList());
        }

        int assignedQuantity = equipment.getTotalQuantity() - equipment.getAvailableQuantity();

        return EquipmentResponse.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .category(equipment.getCategory())
                .categoryName(CATEGORY_NAMES.getOrDefault(equipment.getCategory(), equipment.getCategory()))
                .icon(equipment.getIcon())
                .description(equipment.getDescription())
                .totalQuantity(equipment.getTotalQuantity())
                .availableQuantity(equipment.getAvailableQuantity())
                .assignedQuantity(assignedQuantity)
                .status(equipment.getStatus())
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .activeAssignments(activeAssignments)
                .build();
    }

    private EquipmentAssignmentResponse mapAssignmentToResponse(EquipmentAssignment assignment) {
        return EquipmentAssignmentResponse.builder()
                .id(assignment.getId())
                .equipmentId(assignment.getEquipment().getId())
                .equipmentName(assignment.getEquipment().getName())
                .assignmentType(assignment.getAssignmentType())
                .targetId(assignment.getTargetId())
                .targetName(getTargetName(assignment.getTargetId(), assignment.getAssignmentType()))
                .quantity(assignment.getQuantity())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .duration(assignment.getDuration())
                .reason(assignment.getReason())
                .status(assignment.getStatus())
                .assignedBy(assignment.getAssignedBy())
                .assignedAt(assignment.getAssignedAt())
                .notes(assignment.getNotes())
                .build();
    }
}
