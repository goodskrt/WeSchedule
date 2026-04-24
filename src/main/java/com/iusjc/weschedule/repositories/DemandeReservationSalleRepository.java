package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.StatutDemandeReservation;
import com.iusjc.weschedule.models.DemandeReservationSalle;
import com.iusjc.weschedule.models.Utilisateur;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DemandeReservationSalleRepository extends JpaRepository<DemandeReservationSalle, UUID> {

    @EntityGraph(attributePaths = {"equipements", "salle", "enseignant"})
    List<DemandeReservationSalle> findByEnseignantOrderByCreatedAtDesc(Utilisateur enseignant);

    @EntityGraph(attributePaths = {"equipements", "salle", "enseignant"})
    List<DemandeReservationSalle> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM DemandeReservationSalle d
            WHERE d.salle.idSalle = :salleId
              AND d.statut IN :statuts
              AND (:excludeId IS NULL OR d.id <> :excludeId)
              AND d.startAt < :endAt
              AND d.endAt > :startAt
            """)
    boolean existsSalleCreneauConflict(
            @Param("salleId") UUID salleId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("statuts") List<StatutDemandeReservation> statuts,
            @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM DemandeReservationSalle d
            JOIN d.equipements e
            WHERE e.id = :equipmentId
              AND d.statut IN :statuts
              AND (:excludeId IS NULL OR d.id <> :excludeId)
              AND d.startAt < :endAt
              AND d.endAt > :startAt
            """)
    boolean existsEquipementCreneauConflict(
            @Param("equipmentId") UUID equipmentId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("statuts") List<StatutDemandeReservation> statuts,
            @Param("excludeId") UUID excludeId);
}
