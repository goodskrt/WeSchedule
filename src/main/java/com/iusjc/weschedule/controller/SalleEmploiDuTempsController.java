package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.SeanceClasseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/salles")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@RequiredArgsConstructor
@Slf4j
public class SalleEmploiDuTempsController {

    private final SalleRepository salleRepository;
    private final SeanceClasseRepository seanceRepository;

    @GetMapping("/{id}/emploi-temps")
    @Transactional(readOnly = true)
    public String page(@PathVariable @NonNull UUID id, Model model) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));
        model.addAttribute("salle", salle);
        return "admin/salle-emploi-temps";
    }

    @GetMapping("/{id}/emploi-du-temps/api")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<Map<String, Object>> api(
            @PathVariable @NonNull UUID id,
            @RequestParam Integer semaine,
            @RequestParam Integer annee) {

        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
        LocalDate lundi = premierJanvier.with(weekFields.weekOfWeekBasedYear(), semaine).with(DayOfWeek.MONDAY);
        LocalDate dimanche = lundi.plusDays(6);

        List<SeanceClasse> toutesSeances = seanceRepository.findByDateBetween(lundi, dimanche);
        List<SeanceClasse> seancesSalle = toutesSeances.stream()
                .filter(s -> s.getSalle() != null && salle.getIdSalle().equals(s.getSalle().getIdSalle()))
                .collect(Collectors.toList());

        return seancesSalle.stream().map(this::convertSeanceToMap).collect(Collectors.toList());
    }

    private Map<String, Object> convertSeanceToMap(SeanceClasse seance) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", seance.getId());
        map.put("date", seance.getDate() != null ? seance.getDate().toString() : null);
        map.put("heureDebut", seance.getHeureDebut() != null ? seance.getHeureDebut().toString() : null);
        map.put("heureFin", seance.getHeureFin() != null ? seance.getHeureFin().toString() : null);
        map.put("jourSemaine", seance.getJourSemaine());
        map.put("remarques", seance.getRemarques());

        if (seance.getCours() != null) {
            Map<String, Object> cours = new HashMap<>();
            cours.put("idCours", seance.getCours().getIdCours());
            cours.put("intitule", seance.getCours().getIntitule());
            cours.put("typeCours", seance.getCours().getTypeCours() != null ? seance.getCours().getTypeCours().name() : null);
            map.put("cours", cours);
        }

        if (seance.getEmploiDuTemps() != null && seance.getEmploiDuTemps().getClasse() != null) {
            Map<String, Object> classe = new HashMap<>();
            classe.put("idClasse", seance.getEmploiDuTemps().getClasse().getIdClasse());
            classe.put("nom", seance.getEmploiDuTemps().getClasse().getNom());
            classe.put("effectif", seance.getEmploiDuTemps().getClasse().getEffectif());
            map.put("classe", classe);
        }

        if (seance.getSalle() != null) {
            Map<String, Object> salle = new HashMap<>();
            salle.put("idSalle", seance.getSalle().getIdSalle());
            salle.put("nomSalle", seance.getSalle().getNomSalle());
            map.put("salle", salle);
        }

        return map;
    }
}

