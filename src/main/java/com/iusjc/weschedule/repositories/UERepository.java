package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.UE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UERepository extends JpaRepository<UE, UUID> {}
