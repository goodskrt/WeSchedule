package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.DisponibiliteEnseignant;
import com.iusjc.weschedule.models.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DisponibiliteEnseignantRepository extends JpaRepository<DisponibiliteEnseignant, UUID> {
    
    List<DisponibiliteEnseignant> findByEnseignant(Enseignant enseignant);
    
    List<DisponibiliteEnseignant> findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
        Enseignant enseignant, LocalDate dateFin, LocalDate dateDebut);
}