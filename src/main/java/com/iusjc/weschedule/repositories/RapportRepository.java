package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RapportRepository extends JpaRepository<Rapport, UUID> {

}
