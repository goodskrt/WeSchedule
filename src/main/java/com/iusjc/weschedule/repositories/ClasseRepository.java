package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Classe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClasseRepository extends JpaRepository<Classe, UUID> {

    List<Classe> findByEcole_IdEcoleOrderByNomAsc(UUID idEcole);
}
