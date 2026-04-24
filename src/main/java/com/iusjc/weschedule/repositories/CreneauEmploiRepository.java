package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreneauEmploiRepository extends JpaRepository<CreneauEmploi, UUID> {

    List<CreneauEmploi> findByEmploiDeTempsIdEDTOrderByDateAscHeureDebutAsc(UUID edtId);

    void deleteByEmploiDeTemps(EmploiDeTemps edt);

    // Conflit enseignant : un enseignant ne peut pas avoir deux cours au même créneau
    @Query("SELECT COUNT(c) > 0 FROM CreneauEmploi c " +
           "WHERE c.enseignant = :enseignant AND c.date = :date " +
           "AND c.heureDebut < :heureFin AND c.heureFin > :heureDebut")
    boolean existsConflitEnseignant(@Param("enseignant") Enseignant enseignant,
                                    @Param("date") LocalDate date,
                                    @Param("heureDebut") LocalTime heureDebut,
                                    @Param("heureFin") LocalTime heureFin);

    // Conflit salle : une salle ne peut pas être utilisée deux fois au même créneau
    @Query("SELECT COUNT(c) > 0 FROM CreneauEmploi c " +
           "WHERE c.salle = :salle AND c.date = :date " +
           "AND c.heureDebut < :heureFin AND c.heureFin > :heureDebut")
    boolean existsConflitSalle(@Param("salle") Salle salle,
                               @Param("date") LocalDate date,
                               @Param("heureDebut") LocalTime heureDebut,
                               @Param("heureFin") LocalTime heureFin);

    // Conflit classe : une classe ne peut pas avoir deux cours au même créneau
    @Query("SELECT COUNT(c) > 0 FROM CreneauEmploi c " +
           "WHERE c.classe = :classe AND c.date = :date " +
           "AND c.heureDebut < :heureFin AND c.heureFin > :heureDebut")
    boolean existsConflitClasse(@Param("classe") Classe classe,
                                @Param("date") LocalDate date,
                                @Param("heureDebut") LocalTime heureDebut,
                                @Param("heureFin") LocalTime heureFin);

    // Compter les sessions déjà planifiées pour une UE + classe dans un emploi
    long countByEmploiDeTempsIdEDTAndUeAndClasse(UUID edtId, UE ue, Classe classe);
}
