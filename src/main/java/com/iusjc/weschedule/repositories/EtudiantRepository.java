package com.iusjc.weschedule.repositories;
import com.iusjc.weschedule.models.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EtudiantRepository extends JpaRepository<Etudiant, UUID> {

    @Modifying
    @Query("UPDATE Etudiant e SET e.classe = null WHERE e.classe.idClasse = :classeId")
    void detachFromClasse(@Param("classeId") UUID classeId);
}