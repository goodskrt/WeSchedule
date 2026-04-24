package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.CategorieEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategorieEquipementRepository extends JpaRepository<CategorieEquipement, UUID> {

    Optional<CategorieEquipement> findByCodeIgnoreCase(String code);

    Optional<CategorieEquipement> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, UUID id);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    List<CategorieEquipement> findAllByOrderByOrdreAscNomAsc();
}
