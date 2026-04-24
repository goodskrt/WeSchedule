package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.models.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UERepository extends JpaRepository<UE, UUID> {

    List<UE> findByClassesIdClasse(UUID classeId);

    Optional<UE> findByCode(String code);
    List<UE> findBySemestre(Integer semestre);
    List<UE> findByStatut(StatutUE statut);
    List<UE> findBySemestreAndStatut(Integer semestre, StatutUE statut);
    List<UE> findByStatutOrderByCodeAsc(StatutUE statut);
    List<UE> findBySemestreOrderByCodeAsc(Integer semestre);
    boolean existsByCode(String code);

    @Modifying
    @Query(value = "DELETE FROM ue_classe WHERE classe_id = :classeId", nativeQuery = true)
    void deleteUeClasseByClasseId(@Param("classeId") UUID classeId);
}
