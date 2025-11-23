package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}