package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Classe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClasseRepository extends JpaRepository<Classe, UUID> {}
