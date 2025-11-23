package com.iusjc.weschedule.repositories;
import com.iusjc.weschedule.models.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnseignantRepository extends JpaRepository<Enseignant, UUID> {
}