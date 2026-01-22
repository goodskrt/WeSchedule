package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.TypeSalle;
import com.iusjc.weschedule.models.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalleRepository extends JpaRepository<Salle, UUID> {
    
    // Trouver une salle par nom
    Optional<Salle> findByNomSalle(String nomSalle);
    
    // Trouver les salles par type
    List<Salle> findByTypeSalle(TypeSalle typeSalle);
    
    // Trouver les salles avec capacité minimale
    List<Salle> findByCapaciteGreaterThanEqual(Integer capacite);
    
    // Trouver les salles par type et capacité minimale
    List<Salle> findByTypeSalleAndCapaciteGreaterThanEqual(TypeSalle typeSalle, Integer capacite);
}
