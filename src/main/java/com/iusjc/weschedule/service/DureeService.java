package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.repositories.CoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Service pour gérer la logique de durée entre UE et Cours
 * 
 * Logique métier:
 * - UE.duree = Nombre d'heures TOTAL pour l'UE (ex: 60h)
 * - Cours.duree = Nombre d'heures RESTANTES pour l'UE (ex: 45h restantes)
 * - Heures effectuées = UE.duree - Cours.duree
 */
@Service
public class DureeService {
    
    @Autowired
    private CoursRepository coursRepository;
    
    /**
     * Calculer les heures effectuées pour un cours
     * @param cours Le cours
     * @return Nombre d'heures déjà effectuées
     */
    public int calculerHeuresEffectuees(Cours cours) {
        if (cours.getUe() == null) {
            throw new RuntimeException("Le cours doit être lié à une UE");
        }
        return cours.getUe().getDuree() - cours.getDuree();
    }
    
    /**
     * Calculer le pourcentage d'avancement d'un cours
     * @param cours Le cours
     * @return Pourcentage d'avancement (0-100)
     */
    public double calculerPourcentageAvancement(Cours cours) {
        if (cours.getUe() == null) {
            throw new RuntimeException("Le cours doit être lié à une UE");
        }
        
        int heuresTotal = cours.getUe().getDuree();
        if (heuresTotal == 0) {
            return 0.0;
        }
        
        int heuresEffectuees = calculerHeuresEffectuees(cours);
        return (heuresEffectuees * 100.0) / heuresTotal;
    }
    
    /**
     * Vérifier si un cours est terminé (toutes les heures effectuées)
     * @param cours Le cours
     * @return true si terminé, false sinon
     */
    public boolean estTermine(Cours cours) {
        return cours.getDuree() == 0;
    }
    
    /**
     * Vérifier s'il reste des heures à planifier
     * @param cours Le cours
     * @return true s'il reste des heures, false sinon
     */
    public boolean aDesHeuresRestantes(Cours cours) {
        return cours.getDuree() > 0;
    }
    
    /**
     * Décrémenter les heures restantes après une séance
     * @param cours Le cours
     * @param heures Nombre d'heures à décrémenter
     * @throws RuntimeException si les heures restantes deviennent négatives
     */
    @Transactional
    public void decrementerHeures(Cours cours, int heures) {
        int nouvellesDuree = cours.getDuree() - heures;
        
        if (nouvellesDuree < 0) {
            throw new RuntimeException(
                "Impossible de décrémenter: il reste " + cours.getDuree() + 
                "h mais vous essayez de décrémenter de " + heures + "h"
            );
        }
        
        cours.setDuree(nouvellesDuree);
        coursRepository.save(cours);
    }
    
    /**
     * Calculer la durée d'une séance en heures
     * @param debut Heure de début
     * @param fin Heure de fin
     * @return Durée en heures
     */
    public long calculerDureeSeance(LocalTime debut, LocalTime fin) {
        return ChronoUnit.HOURS.between(debut, fin);
    }
    
    /**
     * Calculer la durée d'une séance en minutes
     * @param debut Heure de début
     * @param fin Heure de fin
     * @return Durée en minutes
     */
    public long calculerDureeSeanceMinutes(LocalTime debut, LocalTime fin) {
        return ChronoUnit.MINUTES.between(debut, fin);
    }
    
    /**
     * Décrémenter automatiquement les heures après la création d'une séance
     * @param seance La séance créée
     */
    @Transactional
    public void decrementerApresSeance(SeanceClasse seance) {
        long dureeSeance = calculerDureeSeance(
            seance.getHeureDebut(), 
            seance.getHeureFin()
        );
        
        decrementerHeures(seance.getCours(), (int) dureeSeance);
    }
    
    /**
     * Valider qu'un cours peut être créé avec une durée donnée
     * @param cours Le cours
     * @throws RuntimeException si la durée est invalide
     */
    public void validerDureeCours(Cours cours) {
        if (cours.getUe() == null) {
            throw new RuntimeException("Le cours doit être lié à une UE");
        }
        
        if (cours.getDuree() < 0) {
            throw new RuntimeException("La durée ne peut pas être négative");
        }
        
        if (cours.getDuree() > cours.getUe().getDuree()) {
            throw new RuntimeException(
                "Les heures restantes (" + cours.getDuree() + 
                "h) ne peuvent pas dépasser le total de l'UE (" + 
                cours.getUe().getDuree() + "h)"
            );
        }
    }
    
    /**
     * Réinitialiser les heures restantes à la durée totale de l'UE
     * @param cours Le cours
     */
    @Transactional
    public void reinitialiserHeures(Cours cours) {
        if (cours.getUe() == null) {
            throw new RuntimeException("Le cours doit être lié à une UE");
        }
        
        cours.setDuree(cours.getUe().getDuree());
        coursRepository.save(cours);
    }
    
    /**
     * Obtenir un résumé de l'avancement d'un cours
     * @param cours Le cours
     * @return Résumé sous forme de texte
     */
    public String obtenirResume(Cours cours) {
        int heuresTotal = cours.getUe().getDuree();
        int heuresRestantes = cours.getDuree();
        int heuresEffectuees = calculerHeuresEffectuees(cours);
        double pourcentage = calculerPourcentageAvancement(cours);
        
        return String.format(
            "%s: %dh effectuées / %dh total (%.1f%%) - %dh restantes",
            cours.getUe().getCode(),
            heuresEffectuees,
            heuresTotal,
            pourcentage,
            heuresRestantes
        );
    }
}
