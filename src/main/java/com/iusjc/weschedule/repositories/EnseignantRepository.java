package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnseignantRepository extends JpaRepository<Enseignant, UUID> {

    @Query("SELECT DISTINCT e FROM Enseignant e LEFT JOIN FETCH e.uesEnseignees")
    List<Enseignant> findAllWithUEs();

    @Query("SELECT e FROM Enseignant e LEFT JOIN FETCH e.uesEnseignees WHERE e.idUser = :id")
    Optional<Enseignant> findByIdWithUEs(@Param("id") UUID id);
    
    Optional<Enseignant> findByIdUser(UUID idUser);
}