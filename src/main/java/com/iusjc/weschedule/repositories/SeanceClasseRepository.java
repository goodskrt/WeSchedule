package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.EmploiDuTempsClasse;
import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.SeanceClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeanceClasseRepository extends JpaRepository<SeanceClasse, UUID> {
    
    List<SeanceClasse> findByEmploiDuTemps(EmploiDuTempsClasse emploiDuTemps);
    
    List<SeanceClasse> findByEmploiDuTempsOrderByDateAscHeureDebutAsc(EmploiDuTempsClasse emploiDuTemps);
    
    List<SeanceClasse> findByEnseignant(Enseignant enseignant);
    
    List<SeanceClasse> findByEnseignantAndDateBetween(Enseignant enseignant, LocalDate start, LocalDate end);
    
    List<SeanceClasse> findByDateBetween(LocalDate start, LocalDate end);
    
    List<SeanceClasse> findBySalle(Salle salle);
    
    List<SeanceClasse> findBySalleAndDate(Salle salle, LocalDate date);
    
    List<SeanceClasse> findByCours(Cours cours);
    
    // Trouver la première séance d'un cours triée par date
    SeanceClasse findFirstByCoursOrderByDateAsc(Cours cours);
    
    // Trouver toutes les séances d'un cours triées par date
    List<SeanceClasse> findByCoursOrderByDateAsc(Cours cours);
    
    // Vérifier les conflits de salle
    List<SeanceClasse> findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
        Salle salle, LocalDate date, LocalTime heureFin, LocalTime heureDebut
    );

    boolean existsBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
        Salle salle, LocalDate date, LocalTime heureFin, LocalTime heureDebut
    );
    
    // Vérifier les conflits d'enseignant
    List<SeanceClasse> findByEnseignantAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
        Enseignant enseignant, LocalDate date, LocalTime heureFin, LocalTime heureDebut
    );
    
    // Vérifier les conflits de classe (emploi du temps)
    List<SeanceClasse> findByEmploiDuTempsAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
        EmploiDuTempsClasse emploiDuTemps, LocalDate date, LocalTime heureFin, LocalTime heureDebut
    );
    
    // Trouver les séances d'un cours dans un emploi du temps spécifique
    List<SeanceClasse> findByEmploiDuTempsAndCours(EmploiDuTempsClasse emploiDuTemps, Cours cours);
}
