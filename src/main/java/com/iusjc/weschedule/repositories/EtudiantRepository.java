package com.iusjc.weschedule.repositories;
import com.iusjc.weschedule.models.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EtudiantRepository extends JpaRepository<Etudiant, UUID> {
}