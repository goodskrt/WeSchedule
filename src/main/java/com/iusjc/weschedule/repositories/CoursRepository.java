package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoursRepository extends JpaRepository<Cours, UUID> {

    List<Cours> findByUe(UE ue);
    List<Cours> findByClasse(Classe classe);
    List<Cours> findByClasseIdClasse(UUID classeId);
    List<Cours> findByUeAndClasse(UE ue, Classe classe);

    @Modifying
    @Query("UPDATE Cours c SET c.classe = null WHERE c.classe.idClasse = :classeId")
    void detachFromClasse(@Param("classeId") UUID classeId);
}
