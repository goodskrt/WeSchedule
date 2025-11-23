package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Ressource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RessourceRepository extends JpaRepository<Ressource, UUID> {}
