package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

}
