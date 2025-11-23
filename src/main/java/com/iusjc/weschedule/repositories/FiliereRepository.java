package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FiliereRepository extends JpaRepository<Filiere, UUID> {

}
