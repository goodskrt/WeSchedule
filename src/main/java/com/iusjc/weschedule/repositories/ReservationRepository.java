package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.StatutReservation;
import com.iusjc.weschedule.models.Reservation;
import com.iusjc.weschedule.models.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    // Trouver les réservations d'une salle pour une date
    List<Reservation> findBySalleAndPlageHoraireCreneauDisponibiliteDate(Salle salle, LocalDate date);
    
    // Trouver les réservations confirmées d'une salle avec conflit horaire
    List<Reservation> findBySalleAndStatutAndPlageHoraireCreneauDisponibiliteDateAndPlageHoraireHeureDebutLessThanAndPlageHoraireHeureFinGreaterThan(
        Salle salle, StatutReservation statut, LocalDate date, LocalTime heureFin, LocalTime heureDebut
    );
    
    // Trouver toutes les réservations d'une salle
    List<Reservation> findBySalle(Salle salle);
    
    // Trouver les réservations par statut
    List<Reservation> findByStatut(StatutReservation statut);
}
