package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.TypeCours;
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

    boolean existsByUeAndClasseAndTypeCours(UE ue, Classe classe, TypeCours typeCours);

    @Modifying
    @Query("UPDATE Cours c SET c.classe = null WHERE c.classe.idClasse = :classeId")
    void detachFromClasse(@Param("classeId") UUID classeId);
    
    @Modifying
    @Query("UPDATE Cours c SET c.enseignant = null WHERE c.enseignant.idUser = :enseignantId")
    void detachFromEnseignant(@Param("enseignantId") UUID enseignantId);
}
