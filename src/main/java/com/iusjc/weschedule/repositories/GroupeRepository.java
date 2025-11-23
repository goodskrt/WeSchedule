package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupeRepository extends JpaRepository<Groupe, UUID> {
}