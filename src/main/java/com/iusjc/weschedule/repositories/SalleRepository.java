package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Salle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalleRepository extends JpaRepository<Salle, UUID> {
}