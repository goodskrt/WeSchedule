package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.TableauDeBord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TableauDeBordRepository extends JpaRepository<TableauDeBord, UUID> {

}
