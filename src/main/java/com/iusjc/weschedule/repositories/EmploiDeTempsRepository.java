package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.EmploiDeTemps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmploiDeTempsRepository extends JpaRepository<    EmploiDeTemps, UUID> {
}