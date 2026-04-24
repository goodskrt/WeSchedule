package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.CreneauDisponibilite;
import com.iusjc.weschedule.models.DisponibiliteEnseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreneauDisponibiliteRepository extends JpaRepository<CreneauDisponibilite, UUID> {
    
    List<CreneauDisponibilite> findByDisponibilite(DisponibiliteEnseignant disponibilite);
    
    List<CreneauDisponibilite> findByDisponibiliteAndDate(DisponibiliteEnseignant disponibilite, LocalDate date);
    
    @Modifying
    @Query("DELETE FROM CreneauDisponibilite c WHERE c.disponibilite.id = :disponibiliteId")
    void deleteByDisponibiliteId(@Param("disponibiliteId") UUID disponibiliteId);
}