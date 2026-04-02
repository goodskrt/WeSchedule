package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import com.iusjc.weschedule.service.AutoGenerationEmploiDuTempsService;
import com.iusjc.weschedule.service.EmploiDuTempsService;
import com.iusjc.weschedule.service.ExcelEmploiDuTempsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Controller
@RequestMapping("/admin/emplois-du-temps")
public class AdminEmploiDuTempsController {

    @Autowired
    private EmploiDuTempsService emploiDuTempsService;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;

    @Autowired
    private SeanceClasseRepository seanceRepository;
    
    @Autowired
    private ExcelEmploiDuTempsService excelEmploiDuTempsService;
    
    @Autowired
    private AutoGenerationEmploiDuTempsService autoGenerationService;

    /**
     * Page principale de gestion des emplois du temps
     */
    @GetMapping
    public String index(Model model) {
        List<Classe> classes = classeRepository.findAll();
        model.addAttribute("classes", classes);
        return "admin/emplois-du-temps";
    }

    /**
     * Afficher l'emploi du temps d'une classe pour une semaine
     */
    @GetMapping("/classe/{classeId}")
    @Transactional(readOnly = true)
    public String afficherEmploiDuTemps(
            @PathVariable @NonNull UUID classeId,
            @RequestParam(required = false) Integer semaine,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false, defaultValue = "1") Integer semestre,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Classe classe = classeRepository.findById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

            // Si semaine/année non spécifiées, utiliser la semaine actuelle
            LocalDate now = LocalDate.now();
            WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
            
            if (semaine == null) {
                semaine = now.get(weekFields.weekOfWeekBasedYear());
            }
            if (annee == null) {
                annee = now.get(weekFields.weekBasedYear());
            }

            // Récupérer l'emploi du temps
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.getEmploiDuTempsParSemaine(classeId, semaine, annee);

            // Calculer les dates de la semaine
            LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
            LocalDate lundi = premierJanvier.with(weekFields.weekOfWeekBasedYear(), semaine).with(DayOfWeek.MONDAY);
            LocalDate dimanche = lundi.plusDays(6);

            model.addAttribute("classe", classe);
            model.addAttribute("semaine", semaine);
            model.addAttribute("annee", annee);
            model.addAttribute("semestre", semestre);
            model.addAttribute("lundi", lundi);
            model.addAttribute("dimanche", dimanche);
            model.addAttribute("emploiDuTemps", emploiDuTemps);

            if (emploiDuTemps != null) {
                // Récupérer les séances groupées par jour
                Map<LocalDate, List<SeanceClasse>> seancesParJour = emploiDuTempsService.getSeancesParJour(emploiDuTemps.getId());
                
                // Initialiser les collections lazy
                seancesParJour.values().forEach(seances -> {
                    seances.forEach(seance -> {
                        if (seance.getCours() != null && seance.getCours().getUe() != null) {
                            seance.getCours().getUe().getIntitule();
                        }
                        if (seance.getEnseignant() != null) {
                            seance.getEnseignant().getNom();
                        }
                        if (seance.getSalle() != null) {
                            seance.getSalle().getNomSalle();
                        }
                    });
                });
                
                model.addAttribute("seancesParJour", seancesParJour);
            }

            // Filtrer les cours selon le semestre sélectionné
            List<Cours> cours = coursRepository.findAll().stream()
                    .filter(c -> c.getClasse() != null && c.getClasse().getIdClasse().equals(classeId))
                    .filter(c -> c.getUe() != null && c.getUe().getSemestre() != null && c.getUe().getSemestre().equals(semestre))
                    .toList();
            
            List<Enseignant> enseignants = enseignantRepository.findAll();
            List<Salle> salles = salleRepository.findAll();

            model.addAttribute("cours", cours);
            model.addAttribute("enseignants", enseignants);
            model.addAttribute("salles", salles);

            return "admin/emploi-du-temps-classe";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/emplois-du-temps";
        }
    }

    /**
     * Afficher la page de création manuelle d'emploi du temps
     */
    @GetMapping("/classe/{classeId}/manuel")
    @Transactional(readOnly = true)
    public String afficherModeManuel(
            @PathVariable @NonNull UUID classeId,
            @RequestParam Integer semaine,
            @RequestParam Integer annee,
            @RequestParam(required = false, defaultValue = "1") Integer semestre,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Classe classe = classeRepository.findById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

            // Calculer les dates de la semaine
            WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
            LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
            LocalDate lundi = premierJanvier.with(weekFields.weekOfWeekBasedYear(), semaine).with(DayOfWeek.MONDAY);
            LocalDate dimanche = lundi.plusDays(6);

            model.addAttribute("classe", classe);
            model.addAttribute("semaine", semaine);
            model.addAttribute("annee", annee);
            model.addAttribute("semestre", semestre);
            model.addAttribute("lundi", lundi);
            model.addAttribute("dimanche", dimanche);

            // Filtrer les cours selon le semestre sélectionné
            List<Cours> cours = coursRepository.findAll().stream()
                    .filter(c -> c.getClasse() != null && c.getClasse().getIdClasse().equals(classeId))
                    .filter(c -> c.getUe() != null && c.getUe().getSemestre() != null && c.getUe().getSemestre().equals(semestre))
                    .toList();
            
            List<Enseignant> enseignants = enseignantRepository.findAll();
            List<Salle> salles = salleRepository.findAll();

            model.addAttribute("cours", cours);
            model.addAttribute("enseignants", enseignants);
            model.addAttribute("salles", salles);

            return "admin/emploi-du-temps-manuel";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/emplois-du-temps";
        }
    }

    /**
     * Créer un emploi du temps pour une classe
     */
    @PostMapping("/creer")
    public String creerEmploiDuTemps(
            @RequestParam UUID classeId,
            @RequestParam Integer semaine,
            @RequestParam Integer annee,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Calculer la date de début de la semaine
            WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
            LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
            LocalDate lundi = premierJanvier.with(weekFields.weekOfWeekBasedYear(), semaine).with(DayOfWeek.MONDAY);

            emploiDuTempsService.creerEmploiDuTemps(classeId, lundi);
            
            redirectAttributes.addFlashAttribute("success", "Emploi du temps créé avec succès");
            return "redirect:/admin/emplois-du-temps/classe/" + classeId + "?semaine=" + semaine + "&annee=" + annee;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/emplois-du-temps";
        }
    }

    /**
     * Ajouter une séance manuellement
     */
    @PostMapping("/seance/ajouter")
    public String ajouterSeance(
            @RequestParam UUID emploiDuTempsId,
            @RequestParam UUID coursId,
            @RequestParam(required = false) UUID enseignantId,
            @RequestParam(required = false) UUID salleId,
            @RequestParam String date,
            @RequestParam String heureDebut,
            @RequestParam String heureFin,
            @RequestParam(required = false) String remarques,
            RedirectAttributes redirectAttributes) {
        
        try {
            LocalDate dateSeance = LocalDate.parse(date);
            LocalTime heureDebutSeance = LocalTime.parse(heureDebut);
            LocalTime heureFinSeance = LocalTime.parse(heureFin);

            emploiDuTempsService.ajouterSeance(
                    emploiDuTempsId, coursId, enseignantId, salleId,
                    dateSeance, heureDebutSeance, heureFinSeance, remarques);

            redirectAttributes.addFlashAttribute("success", "Séance ajoutée avec succès");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        // Rediriger vers la page de l'emploi du temps
        EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));
        
        return "redirect:/admin/emplois-du-temps/classe/" + emploiDuTemps.getClasse().getIdClasse() +
                "?semaine=" + emploiDuTemps.getSemaine() + "&annee=" + emploiDuTemps.getAnnee();
    }

    /**
     * Ajouter une séance depuis le mode manuel (trouve l'emploi du temps automatiquement)
     */
    @PostMapping("/seance/ajouter-manuel")
    @ResponseBody
    public Map<String, Object> ajouterSeanceManuel(
            @RequestParam UUID classeId,
            @RequestParam Integer semaine,
            @RequestParam Integer annee,
            @RequestParam UUID coursId,
            @RequestParam(required = false) UUID enseignantId,
            @RequestParam(required = false) UUID salleId,
            @RequestParam String date,
            @RequestParam String heureDebut,
            @RequestParam String heureFin,
            @RequestParam(required = false) String remarques) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Trouver l'emploi du temps
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.getEmploiDuTempsParSemaine(classeId, semaine, annee);
            
            if (emploiDuTemps == null) {
                response.put("success", false);
                response.put("message", "Emploi du temps non trouvé");
                return response;
            }

            LocalDate dateSeance = LocalDate.parse(date);
            LocalTime heureDebutSeance = LocalTime.parse(heureDebut);
            LocalTime heureFinSeance = LocalTime.parse(heureFin);

            emploiDuTempsService.ajouterSeance(
                    emploiDuTemps.getId(), coursId, enseignantId, salleId,
                    dateSeance, heureDebutSeance, heureFinSeance, remarques);

            response.put("success", true);
            response.put("message", "Séance ajoutée avec succès");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Supprimer une séance
     */
    @PostMapping("/seance/supprimer/{seanceId}")
    public String supprimerSeance(
            @PathVariable @NonNull UUID seanceId,
            RedirectAttributes redirectAttributes) {
        
        try {
            SeanceClasse seance = seanceRepository.findById(seanceId)
                    .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
            
            EmploiDuTempsClasse emploiDuTemps = seance.getEmploiDuTemps();
            
            emploiDuTempsService.supprimerSeance(seanceId);
            
            redirectAttributes.addFlashAttribute("success", "Séance supprimée avec succès");
            
            return "redirect:/admin/emplois-du-temps/classe/" + emploiDuTemps.getClasse().getIdClasse() +
                    "?semaine=" + emploiDuTemps.getSemaine() + "&annee=" + emploiDuTemps.getAnnee();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/emplois-du-temps";
        }
    }

    /**
     * Supprimer un emploi du temps complet
     */
    @PostMapping("/supprimer/{emploiDuTempsId}")
    public String supprimerEmploiDuTemps(
            @PathVariable @NonNull UUID emploiDuTempsId,
            RedirectAttributes redirectAttributes) {
        
        try {
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                    .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));
            
            UUID classeId = emploiDuTemps.getClasse().getIdClasse();
            
            emploiDuTempsService.supprimerEmploiDuTemps(emploiDuTempsId);
            
            redirectAttributes.addFlashAttribute("success", "Emploi du temps supprimé avec succès");
            
            return "redirect:/admin/emplois-du-temps/classe/" + classeId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/emplois-du-temps";
        }
    }

    /**
     * Dupliquer un emploi du temps
     */
    @PostMapping("/dupliquer/{emploiDuTempsId}")
    public String dupliquerEmploiDuTemps(
            @PathVariable @NonNull UUID emploiDuTempsId,
            @RequestParam Integer nouvelleSemaine,
            @RequestParam Integer nouvelleAnnee,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Calculer la nouvelle date de début
            WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
            LocalDate premierJanvier = LocalDate.of(nouvelleAnnee, 1, 1);
            LocalDate nouvelleDateDebut = premierJanvier.with(weekFields.weekOfWeekBasedYear(), nouvelleSemaine).with(DayOfWeek.MONDAY);

            EmploiDuTempsClasse nouveau = emploiDuTempsService.dupliquerEmploiDuTemps(emploiDuTempsId, nouvelleDateDebut);
            
            redirectAttributes.addFlashAttribute("success", "Emploi du temps dupliqué avec succès");
            
            return "redirect:/admin/emplois-du-temps/classe/" + nouveau.getClasse().getIdClasse() +
                    "?semaine=" + nouvelleSemaine + "&annee=" + nouvelleAnnee;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/emplois-du-temps";
        }
    }
    
    /**
     * Exporter l'emploi du temps en Excel
     */
    @GetMapping("/{emploiDuTempsId}/export")
    public ResponseEntity<byte[]> exporterEmploiDuTemps(@PathVariable @NonNull UUID emploiDuTempsId) {
        try {
            byte[] excelData = excelEmploiDuTempsService.exporterEmploiDuTemps(emploiDuTempsId);
            
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId)
                    .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));
            
            String filename = "EmploiDuTemps_" + emploiDuTemps.getClasse().getNom().replaceAll(" ", "_") +
                    "_S" + emploiDuTemps.getSemaine() + "_" + emploiDuTemps.getAnnee() + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage().getBytes());
        }
    }
    
    /**
     * Importer un emploi du temps depuis Excel
     */
    @PostMapping("/import")
    public ResponseEntity<String> importerEmploiDuTemps(
            @RequestParam("file") MultipartFile file,
            @RequestParam UUID classeId,
            @RequestParam Integer semaine,
            @RequestParam Integer annee) {
        try {
            // TODO: Implémenter l'import Excel
            // Pour l'instant, retourner une erreur
            return ResponseEntity.status(501).body("Import non implémenté");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Auto-générer l'emploi du temps pour une classe
     * Basé sur: CSP (Constraint Satisfaction) + Glouton + Backtracking
     */
    @PostMapping("/auto-generer")
    @ResponseBody
    public Map<String, Object> autoGenererEmploiDuTemps(
            @RequestParam UUID classeId,
            @RequestParam Integer semaine,
            @RequestParam Integer annee,
            @RequestParam(required = false, defaultValue = "1") Integer semestre) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Trouver ou créer l'emploi du temps
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.getEmploiDuTempsParSemaine(classeId, semaine, annee);
            
            if (emploiDuTemps == null) {
                // Créer un nouvel emploi du temps
                WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
                LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
                LocalDate lundi = premierJanvier.with(weekFields.weekOfWeekBasedYear(), semaine).with(DayOfWeek.MONDAY);
                
                emploiDuTemps = emploiDuTempsService.creerEmploiDuTemps(classeId, lundi);
            }
            
            // Lancer la génération automatique avec filtre sur le semestre
            Map<String, Object> resultat = autoGenerationService.genererEmploiDuTemps(emploiDuTemps.getId(), semestre);
            
            return resultat;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return response;
        }
    }
    

}
