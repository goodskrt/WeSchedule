package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Creneau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreneauRepository extends JpaRepository<Creneau, UUID> {
}