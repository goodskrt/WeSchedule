package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.models.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UERepository extends JpaRepository<UE, UUID> {
    
    // Recherche par code
    Optional<UE> findByCode(String code);
    
    // Recherche par semestre
    List<UE> findBySemestre(Integer semestre);
    
    // Recherche par statut
    List<UE> findByStatut(StatutUE statut);
    
    // Recherche par semestre et statut
    List<UE> findBySemestreAndStatut(Integer semestre, StatutUE statut);
    
    // Recherche des UE actives
    List<UE> findByStatutOrderByCodeAsc(StatutUE statut);
    
    // Recherche par semestre (triées par code)
    List<UE> findBySemestreOrderByCodeAsc(Integer semestre);
    
    // Vérifier si un code existe déjà
    boolean existsByCode(String code);
}
