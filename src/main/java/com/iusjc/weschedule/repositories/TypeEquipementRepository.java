package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.enums.CategorieEquipement;
import com.iusjc.weschedule.models.TypeEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeEquipementRepository extends JpaRepository<TypeEquipement, UUID> {
    List<TypeEquipement> findByCategorie(CategorieEquipement categorie);
    boolean existsByNom(String nom);
    Optional<TypeEquipement> findByNomIgnoreCase(String nom);
}
