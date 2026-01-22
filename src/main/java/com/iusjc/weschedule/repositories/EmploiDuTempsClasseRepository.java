package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.EmploiDuTempsClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploiDuTempsClasseRepository extends JpaRepository<EmploiDuTempsClasse, UUID> {
    
    List<EmploiDuTempsClasse> findByClasse(Classe classe);
    
    List<EmploiDuTempsClasse> findByClasseOrderByDateDebutDesc(Classe classe);
    
    Optional<EmploiDuTempsClasse> findByClasseAndSemaineAndAnnee(Classe classe, Integer semaine, Integer annee);
    
    List<EmploiDuTempsClasse> findByDateDebutBetween(LocalDate start, LocalDate end);
    
    boolean existsByClasseAndSemaineAndAnnee(Classe classe, Integer semaine, Integer annee);
}
