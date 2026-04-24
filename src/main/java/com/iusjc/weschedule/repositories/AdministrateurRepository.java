package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdministrateurRepository extends JpaRepository<Administrateur, UUID> {
}