package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.CreneauDisponibilite;
import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.PlageHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlageHoraireRepository extends JpaRepository<PlageHoraire, UUID> {
    
    List<PlageHoraire> findByCreneauDisponibilite(CreneauDisponibilite creneauDisponibilite);
    
    @Modifying
    @Query("DELETE FROM PlageHoraire p WHERE p.creneauDisponibilite.id = :creneauId")
    void deleteByCreneauDisponibiliteId(@Param("creneauId") UUID creneauId);

    // Vérifie si un enseignant est disponible sur un créneau précis
    @Query("SELECT COUNT(ph) > 0 FROM PlageHoraire ph " +
           "JOIN ph.creneauDisponibilite cd " +
           "JOIN cd.disponibilite d " +
           "WHERE d.enseignant = :enseignant " +
           "AND cd.date = :date " +
           "AND ph.heureDebut <= :heureDebut " +
           "AND ph.heureFin >= :heureFin")
    boolean isEnseignantDisponible(@Param("enseignant") Enseignant enseignant,
                                   @Param("date") LocalDate date,
                                   @Param("heureDebut") LocalTime heureDebut,
                                   @Param("heureFin") LocalTime heureFin);
}