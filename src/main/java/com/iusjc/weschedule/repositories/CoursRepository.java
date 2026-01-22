package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoursRepository extends JpaRepository<Cours, UUID> {
    
    // Trouver tous les cours pour une UE
    List<Cours> findByUe(UE ue);
    
    // Trouver les cours avec heures restantes > 0
    List<Cours> findByDureeGreaterThan(Integer duree);
    
    // Trouver les cours d'une UE avec heures restantes
    List<Cours> findByUeAndDureeGreaterThan(UE ue, Integer duree);
}
