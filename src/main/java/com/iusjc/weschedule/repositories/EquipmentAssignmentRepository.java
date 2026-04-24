package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.EquipmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EquipmentAssignmentRepository extends JpaRepository<EquipmentAssignment, UUID> {
    
    List<EquipmentAssignment> findByEquipment(Equipment equipment);

    List<EquipmentAssignment> findByEquipmentOrderByStartAtDesc(Equipment equipment);
    
    List<EquipmentAssignment> findByEquipmentAndStatus(Equipment equipment, String status);
    
    List<EquipmentAssignment> findByTargetIdAndAssignmentType(UUID targetId, String assignmentType);
    
    List<EquipmentAssignment> findByStatus(String status);

    long countByEquipment(Equipment equipment);
}
