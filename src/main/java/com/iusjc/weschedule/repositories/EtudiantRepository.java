package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EtudiantRepository extends JpaRepository<Etudiant, UUID> {

    List<Etudiant> findAllByOrderByNomAscPrenomAsc();

    List<Etudiant> findByClasse_IdClasseOrderByNomAscPrenomAsc(UUID idClasse);

    List<Etudiant> findByClasse_Ecole_IdEcoleOrderByNomAscPrenomAsc(UUID idEcole);

    List<Etudiant> findByClasse_IdClasseAndClasse_Ecole_IdEcoleOrderByNomAscPrenomAsc(UUID idClasse, UUID idEcole);

    long countByClasse(Classe classe);

    long countByClasseIsNotNull();

    boolean existsByNumeroEtudiantIgnoreCase(String numeroEtudiant);

    boolean existsByEmailIgnoreCaseAndIdEtudiantNot(String email, UUID idEtudiant);

    boolean existsByNumeroEtudiantIgnoreCaseAndIdEtudiantNot(String numeroEtudiant, UUID idEtudiant);

    boolean existsByEmailIgnoreCase(String email);

    @Modifying
    @Query("UPDATE Etudiant e SET e.classe = null WHERE e.classe.idClasse = :classeId")
    void detachFromClasse(@Param("classeId") UUID classeId);
}
