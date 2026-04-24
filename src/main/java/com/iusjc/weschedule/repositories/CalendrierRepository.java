package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Calendrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CalendrierRepository extends JpaRepository<Calendrier, UUID> {

}
