package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EnseignantRepository extends JpaRepository<Enseignant, UUID> {
    
    @Modifying
    @Query(value = "DELETE FROM enseignant_ue WHERE enseignant_id = :id", nativeQuery = true)
    void deleteUERelations(@Param("id") UUID id);
}