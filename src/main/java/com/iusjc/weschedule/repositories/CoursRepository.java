package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Cours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoursRepository extends JpaRepository<Cours, UUID> {
}