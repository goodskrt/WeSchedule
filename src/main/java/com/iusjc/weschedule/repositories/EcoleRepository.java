package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Ecole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EcoleRepository extends JpaRepository<Ecole, UUID> {
}