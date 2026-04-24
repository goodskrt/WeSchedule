package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.service.PlanificationSeanceService;
import com.iusjc.weschedule.service.PlanificationSeanceService.CoursPlanifiable;
import com.iusjc.weschedule.service.PlanificationSeanceService.CreneauDisponible;
import com.iusjc.weschedule.service.ReservationSalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Controller pour la planification des séances de classe
 */
@RestController
@RequestMapping("/api/planification")
public class PlanificationSeanceController {

    @Autowired
    private PlanificationSeanceService planificationService;

    @Autowired
    private ReservationSalleService reservationSalleService;

    /**
     * Obtenir tous les cours planifiables pour une classe
     * (UE actives avec heures restantes > 0)
     */
    @GetMapping("/classe/{classeId}/cours-planifiables")
    public ResponseEntity<Map<String, Object>> getCoursPlanifiables(@PathVariable UUID classeId) {
        try {
            List<CoursPlanifiable> cours = planificationService.getCoursPlanifiables(classeId);
            
            List<Map<String, Object>> coursData = new ArrayList<>();
            for (CoursPlanifiable cp : cours) {
                Map<String, Object> data = new HashMap<>();
                data.put("coursId", cp.getCours().getIdCours());
                data.put("coursIntitule", cp.getCours().getIntitule());
                data.put("ueCode", cp.getUe().getCode());
                data.put("ueIntitule", cp.getUe().getIntitule());
                data.put("ueDureeTotal", cp.getUe().getDuree());
                data.put("heuresRestantes", cp.getHeuresRestantes());
                data.put("pourcentageAvancement", cp.getPourcentageAvancement());
                
                if (cp.getEnseignant() != null) {
                    Map<String, Object> enseignantData = new HashMap<>();
                    enseignantData.put("id", cp.getEnseignant().getIdUser());
                    enseignantData.put("nom", cp.getEnseignant().getNom());
                    enseignantData.put("prenom", cp.getEnseignant().getPrenom());
                    data.put("enseignant", enseignantData);
                }
                
                coursData.add(data);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cours", coursData);
            response.put("total", coursData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtenir les créneaux disponibles pour planifier des séances
     */
    @GetMapping("/classe/{classeId}/creneaux-disponibles")
    public ResponseEntity<Map<String, Object>> getCreneauxDisponibles(
            @PathVariable UUID classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        try {
            List<CreneauDisponible> creneaux = planificationService.getCreneauxDisponibles(
                    classeId, dateDebut, dateFin);
            
            List<Map<String, Object>> creneauxData = new ArrayList<>();
            for (CreneauDisponible creneau : creneaux) {
                Map<String, Object> data = new HashMap<>();
                data.put("date", creneau.getDate());
                data.put("heureDebut", creneau.getHeureDebut());
                data.put("heureFin", creneau.getHeureFin());
                
                Map<String, Object> enseignantData = new HashMap<>();
                enseignantData.put("id", creneau.getEnseignant().getIdUser());
                enseignantData.put("nom", creneau.getEnseignant().getNom());
                enseignantData.put("prenom", creneau.getEnseignant().getPrenom());
                data.put("enseignant", enseignantData);
                
                Map<String, Object> coursData = new HashMap<>();
                coursData.put("id", creneau.getCours().getIdCours());
                coursData.put("intitule", creneau.getCours().getIntitule());
                coursData.put("ueCode", creneau.getCours().getUe().getCode());
                data.put("cours", coursData);
                
                creneauxData.add(data);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("creneaux", creneauxData);
            response.put("total", creneauxData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Créer une séance à partir d'un créneau disponible
     */
    @PostMapping("/seance/creer")
    public ResponseEntity<Map<String, Object>> creerSeance(
            @RequestParam UUID emploiDuTempsId,
            @RequestParam UUID coursId,
            @RequestParam UUID enseignantId,
            @RequestParam(required = false) UUID salleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin,
            @RequestParam(required = false) String remarques) {
        try {
            SeanceClasse seance = planificationService.creerSeanceDepuisCreneau(
                    emploiDuTempsId, coursId, enseignantId, salleId,
                    date, heureDebut, heureFin, remarques);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance créée avec succès");
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
     * Obtenir un résumé de planification pour une classe
     */
    @GetMapping("/classe/{classeId}/resume")
    public ResponseEntity<Map<String, Object>> getResumePlanification(@PathVariable UUID classeId) {
        try {
            Map<String, Object> resume = planificationService.getResumePlanification(classeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resume", resume);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtenir les salles disponibles avec capacité suffisante
     */
    @GetMapping("/classe/{classeId}/salles-disponibles")
    public ResponseEntity<Map<String, Object>> getSallesDisponibles(
            @PathVariable UUID classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin) {
        try {
            List<ReservationSalleService.SalleDisponible> salles = 
                    planificationService.getSallesDisponiblesPourSeance(
                            classeId, date, heureDebut, heureFin);
            
            List<Map<String, Object>> sallesData = new ArrayList<>();
            for (ReservationSalleService.SalleDisponible salle : salles) {
                Map<String, Object> data = new HashMap<>();
                data.put("salleId", salle.getSalle().getIdSalle());
                data.put("nomSalle", salle.getSalle().getNomSalle());
                data.put("typeSalle", salle.getSalle().getTypeSalle());
                data.put("capacite", salle.getCapacite());
                data.put("effectifClasse", salle.getEffectifClasse());
                data.put("capaciteSuffisante", salle.isCapaciteSuffisante());
                data.put("placesRestantes", salle.getPlacesRestantes());
                sallesData.add(data);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("salles", sallesData);
            response.put("total", sallesData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifier la capacité d'une salle pour une classe
     */
    @GetMapping("/salle/{salleId}/verifier-capacite/{classeId}")
    public ResponseEntity<Map<String, Object>> verifierCapacite(
            @PathVariable UUID salleId,
            @PathVariable UUID classeId) {
        try {
            Map<String, Object> verification = reservationSalleService.verifierCapacite(salleId, classeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("verification", verification);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Réserver automatiquement la meilleure salle
     */
    @PostMapping("/classe/{classeId}/reserver-meilleure-salle")
    public ResponseEntity<Map<String, Object>> reserverMeilleureSalle(
            @PathVariable UUID classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin) {
        try {
            Salle salle = planificationService.reserverMeilleureSallePourSeance(
                    classeId, date, heureDebut, heureFin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Meilleure salle réservée avec succès");
            response.put("salleId", salle.getIdSalle());
            response.put("nomSalle", salle.getNomSalle());
            response.put("capacite", salle.getCapacite());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtenir les statistiques des salles disponibles
     */
    @GetMapping("/classe/{classeId}/statistiques-salles")
    public ResponseEntity<Map<String, Object>> getStatistiquesSalles(
            @PathVariable UUID classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin) {
        try {
            Map<String, Object> stats = reservationSalleService.getStatistiquesSalles(
                    classeId, date, heureDebut, heureFin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistiques", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
