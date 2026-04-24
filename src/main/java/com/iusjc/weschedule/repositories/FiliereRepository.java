package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FiliereRepository extends JpaRepository<Filiere, UUID> {

    List<Filiere> findByEcoleIdEcole(UUID ecoleId);

    @Query("SELECT f FROM Filiere f WHERE f.ecole.idEcole = :ecoleId AND :niveau MEMBER OF f.niveaux")
    List<Filiere> findByEcoleAndNiveau(@Param("ecoleId") UUID ecoleId, @Param("niveau") String niveau);
}
