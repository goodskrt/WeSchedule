package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.EnseignantRepository;
import com.iusjc.weschedule.repositories.SeanceClasseRepository;
import com.iusjc.weschedule.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
@Slf4j
public class EnseignantController {

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;

    /**
     * Page emploi du temps enseignant
     */
    @GetMapping("/emploi-temps")
    public String emploiTemps(Authentication auth, Model model) {
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        Utilisateur user = userPrincipal.getUtilisateur();
        Optional<Enseignant> enseignantOpt = enseignantRepository.findByIdUser(user.getIdUser());
        
        if (enseignantOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        model.addAttribute("enseignant", enseignantOpt.get());
        return "dashboard/emploi-temps";
    }

    /**
     * API: Récupérer l'emploi du temps de l'enseignant pour une semaine donnée
     */
    @GetMapping("/emploi-du-temps/api")
    @ResponseBody
    public List<Map<String, Object>> getEmploiDuTempsEnseignant(
            Authentication auth,
            @RequestParam Integer semaine,
            @RequestParam Integer annee) {
        
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        Utilisateur user = userPrincipal.getUtilisateur();
        Optional<Enseignant> enseignantOpt = enseignantRepository.findByIdUser(user.getIdUser());
        
        if (enseignantOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        Enseignant enseignant = enseignantOpt.get();
        
        // Calculer le lundi de la semaine
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
        LocalDate lundi = premierJanvier.with(weekFields.weekOfWeekBasedYear(), semaine).with(DayOfWeek.MONDAY);
        LocalDate dimanche = lundi.plusDays(6);
        
        log.info("Chargement emploi du temps enseignant {} pour semaine {} (du {} au {})", 
            enseignant.getNom(), semaine, lundi, dimanche);
        
        // Récupérer TOUTES les séances de cette semaine
        List<SeanceClasse> toutesSeances = seanceRepository.findByDateBetween(lundi, dimanche);
        
        log.info("Total séances trouvées pour la semaine: {}", toutesSeances.size());
        
        // Filtrer les séances où le cours a cet enseignant
        List<SeanceClasse> seancesEnseignant = toutesSeances.stream()
            .filter(seance -> seance.getCours() != null && 
                             seance.getCours().getEnseignant() != null &&
                             seance.getCours().getEnseignant().getIdUser().equals(enseignant.getIdUser()))
            .collect(Collectors.toList());
        
        log.info("Séances de l'enseignant: {}", seancesEnseignant.size());
        
        // Convertir en Map pour le JSON
        return seancesEnseignant.stream()
            .map(this::convertSeanceToMap)
            .collect(Collectors.toList());
    }

    /**
     * Convertit une séance en Map pour le JSON
     */
    private Map<String, Object> convertSeanceToMap(SeanceClasse seance) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", seance.getId());
        map.put("date", seance.getDate().toString());
        map.put("heureDebut", seance.getHeureDebut().toString());
        map.put("heureFin", seance.getHeureFin().toString());
        map.put("jourSemaine", seance.getJourSemaine());
        map.put("remarques", seance.getRemarques());
        
        // Cours
        if (seance.getCours() != null) {
            Map<String, Object> cours = new HashMap<>();
            cours.put("idCours", seance.getCours().getIdCours());
            cours.put("intitule", seance.getCours().getIntitule());
            cours.put("typeCours", seance.getCours().getTypeCours() != null ? 
                seance.getCours().getTypeCours().name() : null);
            map.put("cours", cours);
        }
        
        // Classe
        if (seance.getEmploiDuTemps() != null && seance.getEmploiDuTemps().getClasse() != null) {
            Map<String, Object> classe = new HashMap<>();
            classe.put("idClasse", seance.getEmploiDuTemps().getClasse().getIdClasse());
            classe.put("nom", seance.getEmploiDuTemps().getClasse().getNom());
            classe.put("effectif", seance.getEmploiDuTemps().getClasse().getEffectif());
            map.put("classe", classe);
        }
        
        // Salle
        if (seance.getSalle() != null) {
            Map<String, Object> salle = new HashMap<>();
            salle.put("idSalle", seance.getSalle().getIdSalle());
            salle.put("nomSalle", seance.getSalle().getNomSalle());
            salle.put("capacite", seance.getSalle().getCapacite());
            map.put("salle", salle);
        }
        
        return map;
    }
}
