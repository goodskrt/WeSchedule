package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.EmploiDuTempsClasse;
import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.service.EmploiDuTempsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/emplois-du-temps")
public class EmploiDuTempsController {

    @Autowired
    private EmploiDuTempsService emploiDuTempsService;

    /**
     * Créer un emploi du temps pour une classe
     */
    @PostMapping("/creer")
    public ResponseEntity<Map<String, Object>> creerEmploiDuTemps(
            @RequestParam UUID classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut) {
        try {
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.creerEmploiDuTemps(classeId, dateDebut);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Emploi du temps créé avec succès");
            response.put("emploiDuTempsId", emploiDuTemps.getId());
            response.put("dateDebut", emploiDuTemps.getDateDebut());
            response.put("dateFin", emploiDuTemps.getDateFin());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Ajouter une séance
     */
    @PostMapping("/{emploiDuTempsId}/seances")
    public ResponseEntity<Map<String, Object>> ajouterSeance(
            @PathVariable @NonNull UUID emploiDuTempsId,
            @RequestParam UUID coursId,
            @RequestParam(required = false) UUID enseignantId,
            @RequestParam(required = false) UUID salleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin,
            @RequestParam(required = false) String remarques) {
        try {
            SeanceClasse seance = emploiDuTempsService.ajouterSeance(
                    emploiDuTempsId, coursId, enseignantId, salleId, 
                    date, heureDebut, heureFin, remarques);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance ajoutée avec succès");
            response.put("seanceId", seance.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Modifier une séance
     */
    @PutMapping("/seances/{seanceId}")
    public ResponseEntity<Map<String, Object>> modifierSeance(
            @PathVariable @NonNull UUID seanceId,
            @RequestParam(required = false) UUID coursId,
            @RequestParam(required = false) UUID enseignantId,
            @RequestParam(required = false) UUID salleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin,
            @RequestParam(required = false) String remarques) {
        try {
            SeanceClasse seance = emploiDuTempsService.modifierSeance(
                    seanceId, coursId, enseignantId, salleId, 
                    date, heureDebut, heureFin, remarques);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance modifiée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Supprimer une séance
     */
    @DeleteMapping("/seances/{seanceId}")
    public ResponseEntity<Map<String, Object>> supprimerSeance(@PathVariable @NonNull UUID seanceId) {
        try {
            emploiDuTempsService.supprimerSeance(seanceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance supprimée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtenir l'emploi du temps d'une classe pour une semaine
     */
    @GetMapping("/classe/{classeId}/semaine")
    public ResponseEntity<Map<String, Object>> getEmploiDuTempsSemaine(
            @PathVariable UUID classeId,
            @RequestParam int semaine,
            @RequestParam int annee) {
        try {
            EmploiDuTempsClasse emploiDuTemps = emploiDuTempsService.getEmploiDuTempsParSemaine(classeId, semaine, annee);
            
            if (emploiDuTemps == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Aucun emploi du temps trouvé pour cette semaine");
                return ResponseEntity.ok(response);
            }
            
            Map<LocalDate, List<SeanceClasse>> seancesParJour = emploiDuTempsService.getSeancesParJour(emploiDuTemps.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("emploiDuTemps", convertEmploiDuTempsToMap(emploiDuTemps));
            response.put("seancesParJour", convertSeancesParJourToMap(seancesParJour));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtenir tous les emplois du temps d'une classe
     */
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<Map<String, Object>> getEmploisDuTempsClasse(@PathVariable @NonNull UUID classeId) {
        try {
            List<EmploiDuTempsClasse> emplois = emploiDuTempsService.getEmploisDuTempsClasse(classeId);
            
            List<Map<String, Object>> emploisData = new ArrayList<>();
            for (EmploiDuTempsClasse emploi : emplois) {
                emploisData.add(convertEmploiDuTempsToMap(emploi));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("emplois", emploisData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Supprimer un emploi du temps
     */
    @DeleteMapping("/{emploiDuTempsId}")
    public ResponseEntity<Map<String, Object>> supprimerEmploiDuTemps(@PathVariable @NonNull UUID emploiDuTempsId) {
        try {
            emploiDuTempsService.supprimerEmploiDuTemps(emploiDuTempsId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Emploi du temps supprimé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Dupliquer un emploi du temps
     */
    @PostMapping("/{emploiDuTempsId}/dupliquer")
    public ResponseEntity<Map<String, Object>> dupliquerEmploiDuTemps(
            @PathVariable @NonNull UUID emploiDuTempsId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nouvelleDateDebut) {
        try {
            EmploiDuTempsClasse nouveau = emploiDuTempsService.dupliquerEmploiDuTemps(emploiDuTempsId, nouvelleDateDebut);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Emploi du temps dupliqué avec succès");
            response.put("emploiDuTempsId", nouveau.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Méthodes utilitaires pour convertir les entités en Map
    
    private Map<String, Object> convertEmploiDuTempsToMap(EmploiDuTempsClasse emploi) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", emploi.getId());
        map.put("dateDebut", emploi.getDateDebut());
        map.put("dateFin", emploi.getDateFin());
        map.put("semaine", emploi.getSemaine());
        map.put("annee", emploi.getAnnee());
        
        if (emploi.getClasse() != null) {
            Map<String, Object> classeMap = new HashMap<>();
            classeMap.put("id", emploi.getClasse().getIdClasse());
            classeMap.put("nom", emploi.getClasse().getNom());
            map.put("classe", classeMap);
        }
        
        return map;
    }
    
    private Map<String, List<Map<String, Object>>> convertSeancesParJourToMap(Map<LocalDate, List<SeanceClasse>> seancesParJour) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        
        for (Map.Entry<LocalDate, List<SeanceClasse>> entry : seancesParJour.entrySet()) {
            List<Map<String, Object>> seancesData = new ArrayList<>();
            
            for (SeanceClasse seance : entry.getValue()) {
                Map<String, Object> seanceMap = new HashMap<>();
                seanceMap.put("id", seance.getId());
                seanceMap.put("date", seance.getDate());
                seanceMap.put("jourSemaine", seance.getJourSemaine());
                seanceMap.put("heureDebut", seance.getHeureDebut());
                seanceMap.put("heureFin", seance.getHeureFin());
                seanceMap.put("remarques", seance.getRemarques());
                
                if (seance.getCours() != null) {
                    Map<String, Object> coursMap = new HashMap<>();
                    coursMap.put("id", seance.getCours().getIdCours());
                    coursMap.put("intitule", seance.getCours().getIntitule());
                    coursMap.put("typeCours", seance.getCours().getTypeCours());
                    coursMap.put("duree", seance.getCours().getDureeRestante());
                    if (seance.getCours().getUe() != null) {
                        Map<String, Object> ueMap = new HashMap<>();
                        ueMap.put("id", seance.getCours().getUe().getIdUE());
                        ueMap.put("code", seance.getCours().getUe().getCode());
                        ueMap.put("intitule", seance.getCours().getUe().getIntitule());
                        coursMap.put("ue", ueMap);
                    }
                    seanceMap.put("cours", coursMap);
                }
                
                if (seance.getEnseignant() != null) {
                    Map<String, Object> enseignantMap = new HashMap<>();
                    enseignantMap.put("id", seance.getEnseignant().getIdUser());
                    enseignantMap.put("nom", seance.getEnseignant().getNom());
                    enseignantMap.put("prenom", seance.getEnseignant().getPrenom());
                    seanceMap.put("enseignant", enseignantMap);
                }
                
                if (seance.getSalle() != null) {
                    Map<String, Object> salleMap = new HashMap<>();
                    salleMap.put("id", seance.getSalle().getIdSalle());
                    salleMap.put("nom", seance.getSalle().getNomSalle());
                    salleMap.put("capacite", seance.getSalle().getCapacite());
                    seanceMap.put("salle", salleMap);
                }
                
                seancesData.add(seanceMap);
            }
            
            result.put(entry.getKey().toString(), seancesData);
        }
        
        return result;
    }
}
