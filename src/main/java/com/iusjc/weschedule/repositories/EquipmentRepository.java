package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    List<Equipment> findBySalle(Salle salle);
    List<Equipment> findBySalleIsNull();
    List<Equipment> findByStatut(StatutEquipement statut);
    boolean existsByNumeroSerie(String numeroSerie);
    List<Equipment> findByCategory(String category);
    List<Equipment> findByStatus(String status);
}
