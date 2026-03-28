package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.repositories.CoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Service pour gérer la logique de durée des cours.
 * Cours.dureeTotal  = heures totales prévues
 * Cours.dureeRestante = heures restantes à planifier (décrémentées au fil des séances)
 */
@Service
public class DureeService {

    @Autowired
    private CoursRepository coursRepository;

    public int calculerHeuresEffectuees(Cours cours) {
        return cours.getHeuresEffectuees();
    }

    public double calculerPourcentageAvancement(Cours cours) {
        return cours.getPourcentageAvancement();
    }

    public boolean estTermine(Cours cours) {
        return cours.isTermine();
    }

    public boolean aDesHeuresRestantes(Cours cours) {
        return cours.getDureeRestante() != null && cours.getDureeRestante() > 0;
    }

    @Transactional
    public void decrementerHeures(Cours cours, int heures) {
        int restantes = cours.getDureeRestante() != null ? cours.getDureeRestante() : 0;
        int nouvelles = restantes - heures;
        if (nouvelles < 0) {
            throw new RuntimeException(
                "Impossible de décrémenter : il reste " + restantes +
                "h mais vous essayez de décrémenter de " + heures + "h");
        }
        cours.setDureeRestante(nouvelles);
        coursRepository.save(cours);
    }

    public long calculerDureeSeance(LocalTime debut, LocalTime fin) {
        return ChronoUnit.HOURS.between(debut, fin);
    }

    public long calculerDureeSeanceMinutes(LocalTime debut, LocalTime fin) {
        return ChronoUnit.MINUTES.between(debut, fin);
    }

    @Transactional
    public void decrementerApresSeance(com.iusjc.weschedule.models.SeanceClasse seance) {
        long dureeSeance = calculerDureeSeance(seance.getHeureDebut(), seance.getHeureFin());
        decrementerHeures(seance.getCours(), (int) dureeSeance);
    }

    public void validerDureeCours(Cours cours) {
        if (cours.getDureeRestante() != null && cours.getDureeRestante() < 0) {
            throw new RuntimeException("La durée restante ne peut pas être négative");
        }
        if (cours.getDureeTotal() != null && cours.getDureeRestante() != null
                && cours.getDureeRestante() > cours.getDureeTotal()) {
            throw new RuntimeException(
                "Les heures restantes (" + cours.getDureeRestante() +
                "h) ne peuvent pas dépasser le total (" + cours.getDureeTotal() + "h)");
        }
    }

    @Transactional
    public void reinitialiserHeures(Cours cours) {
        cours.setDureeRestante(cours.getDureeTotal());
        coursRepository.save(cours);
    }

    public String obtenirResume(Cours cours) {
        int total     = cours.getDureeTotal()    != null ? cours.getDureeTotal()    : 0;
        int restantes = cours.getDureeRestante() != null ? cours.getDureeRestante() : 0;
        int effectuees = cours.getHeuresEffectuees();
        double pct    = cours.getPourcentageAvancement();
        String ueCode = cours.getUe() != null ? cours.getUe().getCode() : "—";
        return String.format("%s : %dh effectuées / %dh total (%.1f%%) — %dh restantes",
                ueCode, effectuees, total, pct, restantes);
    }
}
