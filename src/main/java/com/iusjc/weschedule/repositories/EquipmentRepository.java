package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    
    List<Equipment> findByCategory(String category);
    
    List<Equipment> findByStatus(String status);
    
    List<Equipment> findByCategoryAndStatus(String category, String status);
}
