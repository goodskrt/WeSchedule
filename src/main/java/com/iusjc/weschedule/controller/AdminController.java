package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.service.DisponibiliteService;
import com.iusjc.weschedule.service.EmploiDeTempsService;
import com.iusjc.weschedule.service.EnseignantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Slf4j
public class AdminController {

    @Autowired
    private EnseignantService enseignantService;

    @Autowired
    private DisponibiliteService disponibiliteService;

    @Autowired
    private EmploiDeTempsService emploiDeTempsService;

    // ==================== ENSEIGNANTS ====================

    @GetMapping("/enseignants")
    public ResponseEntity<List<Map<String, Object>>> getAllEnseignants() {
        try {
            log.info("=== Récupération de tous les enseignants ===");
            List<Enseignant> enseignants = enseignantService.getAllEnseignants();
            log.info("Nombre d'enseignants récupérés: {}", enseignants.size());
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Enseignant ens : enseignants) {
                Map<String, Object> data = new HashMap<>();
                data.put("idUser", ens.getIdUser().toString());
                data.put("nom", ens.getNom());
                data.put("prenom", ens.getPrenom());
                data.put("email", ens.getEmail());
                data.put("phone", ens.getPhone());
                data.put("grade", ens.getGrade());
                
                // Ne pas charger les UEs dans la liste pour éviter les problèmes de lazy loading
                data.put("nbUes", 0);
                data.put("ues", new ArrayList<>());
                data.put("nbDisponibilites", 0);
                
                result.add(data);
            }
            
            log.info("Retour de {} enseignants", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enseignants", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/enseignants/{id}")
    public ResponseEntity<Map<String, Object>> getEnseignantById(@PathVariable UUID id) {
        try {
            log.info("Récupération de l'enseignant: {}", id);
            Optional<Enseignant> ensOpt = enseignantService.getEnseignantById(id);
            
            if (ensOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Enseignant ens = ensOpt.get();
            
            Map<String, Object> data = new HashMap<>();
            data.put("idUser", ens.getIdUser().toString());
            data.put("nom", ens.getNom());
            data.put("prenom", ens.getPrenom());
            data.put("email", ens.getEmail());
            data.put("phone", ens.getPhone());
            data.put("grade", ens.getGrade());
            data.put("ues", new ArrayList<>());
            data.put("ueIds", new ArrayList<>());
            data.put("nbDisponibilites", 0);
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'enseignant", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/enseignants")
    public ResponseEntity<Map<String, Object>> createEnseignant(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String nom = (String) request.get("nom");
            String prenom = (String) request.get("prenom");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String grade = (String) request.get("grade");
            
            @SuppressWarnings("unchecked")
            List<String> ueIdsStr = (List<String>) request.get("ueIds");
            List<UUID> ueIds = ueIdsStr != null ? ueIdsStr.stream().map(UUID::fromString).toList() : null;

            // Validation
            if (nom == null || nom.trim().isEmpty() || 
                prenom == null || prenom.trim().isEmpty() || 
                email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nom, prénom et email sont obligatoires");
                return ResponseEntity.badRequest().body(response);
            }

            Enseignant saved = enseignantService.creerEnseignant(nom, prenom, email, phone, grade, ueIds);

            response.put("success", true);
            response.put("message", "Enseignant créé avec succès. Un email avec les identifiants a été envoyé.");
            response.put("id", saved.getIdUser().toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'enseignant", e);
            response.put("success", false);
            response.put("message", "Erreur lors de la création: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/enseignants/{id}")
    public ResponseEntity<Map<String, Object>> updateEnseignant(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String nom = (String) request.get("nom");
            String prenom = (String) request.get("prenom");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String grade = (String) request.get("grade");
            
            @SuppressWarnings("unchecked")
            List<String> ueIdsStr = (List<String>) request.get("ueIds");
            List<UUID> ueIds = ueIdsStr != null ? ueIdsStr.stream().map(UUID::fromString).toList() : null;

            Enseignant updated = enseignantService.updateEnseignant(id, nom, prenom, email, phone, grade, ueIds);

            response.put("success", true);
            response.put("message", "Enseignant mis à jour avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'enseignant", e);
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/enseignants/{id}")
    public ResponseEntity<Map<String, Object>> deleteEnseignant(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            enseignantService.supprimerEnseignant(id);
            response.put("success", true);
            response.put("message", "Enseignant supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'enseignant", e);
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/enseignants/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            enseignantService.resetPassword(id);
            response.put("success", true);
            response.put("message", "Mot de passe réinitialisé. Un email a été envoyé à l'enseignant.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe", e);
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== UES ====================

    @GetMapping("/ues")
    public ResponseEntity<List<Map<String, Object>>> getAllUEs() {
        try {
            List<UE> ues = enseignantService.getAllUEs();
            List<Map<String, Object>> result = ues.stream().map(ue -> {
                Map<String, Object> data = new HashMap<>();
                data.put("idUE", ue.getIdUE());
                data.put("code", ue.getCode() != null ? ue.getCode() : "");
                data.put("intitule", ue.getIntitule());
                data.put("duree", ue.getDuree());
                return data;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des UEs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DISPONIBILITES ====================

    @GetMapping("/enseignants/{id}/disponibilites")
    public ResponseEntity<List<Map<String, Object>>> getDisponibilitesEnseignant(@PathVariable UUID id) {
        try {
            Optional<Enseignant> ensOpt = enseignantService.getEnseignantById(id);
            if (ensOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<DisponibiliteEnseignant> disponibilites = disponibiliteService.getDisponibilitesEnseignant(ensOpt.get());
            disponibilites.sort((a, b) -> b.getDateDebut().compareTo(a.getDateDebut()));
            
            List<Map<String, Object>> result = disponibilites.stream().map(d -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", d.getId().toString());
                data.put("dateDebut", d.getDateDebut().toString());
                data.put("dateFin", d.getDateFin().toString());
                return data;
            }).toList();
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des disponibilités", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== EMPLOIS DU TEMPS ====================

    @GetMapping("/emplois")
    public ResponseEntity<List<Map<String, Object>>> getAllEmplois() {
        try {
            List<EmploiDeTemps> emplois = emploiDeTempsService.getAllEmplois();
            List<Map<String, Object>> result = emplois.stream().map(e -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", e.getIdEDT().toString());
                data.put("periodeDebut", e.getPeriodeDebut() != null ? e.getPeriodeDebut().toString() : null);
                data.put("periodeFin", e.getPeriodeFin() != null ? e.getPeriodeFin().toString() : null);
                return data;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des emplois du temps", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/emplois/generer")
    public ResponseEntity<Map<String, Object>> genererEmploi(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String debutStr = request.get("periodeDebut");
            String finStr   = request.get("periodeFin");

            if (debutStr == null || finStr == null) {
                response.put("success", false);
                response.put("message", "Les dates de début et de fin sont obligatoires.");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate debut = LocalDate.parse(debutStr);
            LocalDate fin   = LocalDate.parse(finStr);

            if (fin.isBefore(debut)) {
                response.put("success", false);
                response.put("message", "La date de fin doit être après la date de début.");
                return ResponseEntity.badRequest().body(response);
            }

            EmploiDeTempsService.GenerationResult result =
                    emploiDeTempsService.genererEmploiDeTemps(debut, fin);

            response.put("success", true);
            response.put("emploiId", result.emploiDeTemps().getIdEDT().toString());
            response.put("totalSeances", result.totalSeances());
            response.put("seancesPlanifiees", result.seancesPlanifiees());
            response.put("avertissements", result.avertissements());
            response.put("message", String.format(
                    "Emploi du temps généré : %d/%d séances planifiées.",
                    result.seancesPlanifiees(), result.totalSeances()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'emploi du temps", e);
            response.put("success", false);
            response.put("message", "Erreur : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/emplois/{id}/creneaux")
    public ResponseEntity<List<Map<String, Object>>> getCreneauxEmploi(@PathVariable UUID id) {
        try {
            List<CreneauEmploi> creneaux = emploiDeTempsService.getCreneauxByEmploi(id);
            List<Map<String, Object>> result = creneaux.stream().map(c -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", c.getId().toString());
                data.put("date", c.getDate().toString());
                data.put("heureDebut", c.getHeureDebut().toString());
                data.put("heureFin", c.getHeureFin().toString());
                data.put("ue", c.getUe() != null ? c.getUe().getIntitule() : "");
                data.put("ueCode", c.getUe() != null ? c.getUe().getCode() : "");
                data.put("enseignant", c.getEnseignant() != null
                        ? c.getEnseignant().getPrenom() + " " + c.getEnseignant().getNom() : "");
                data.put("classe", c.getClasse() != null ? c.getClasse().getNom() : "");
                data.put("salle", c.getSalle() != null ? c.getSalle().getNomSalle() : "");
                data.put("typeCours", c.getTypeCours() != null ? c.getTypeCours().toString() : "");
                return data;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des créneaux", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/emplois/{id}")
    public ResponseEntity<Map<String, Object>> supprimerEmploi(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            emploiDeTempsService.supprimerEmploi(id);
            response.put("success", true);
            response.put("message", "Emploi du temps supprimé avec succès.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'emploi du temps", e);
            response.put("success", false);
            response.put("message", "Erreur : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/disponibilites/{id}/creneaux")
    public ResponseEntity<Map<String, Object>> getCreneauxDisponibilite(@PathVariable UUID id) {
        try {
            Optional<DisponibiliteEnseignant> dispoOpt = disponibiliteService.getDisponibiliteById(id);
            if (dispoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DisponibiliteEnseignant dispo = dispoOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("id", dispo.getId().toString());
            result.put("dateDebut", dispo.getDateDebut().toString());
            result.put("dateFin", dispo.getDateFin().toString());
            
            // Grouper les créneaux par jour
            Map<String, List<Map<String, String>>> creneauxParJour = new LinkedHashMap<>();
            String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            
            for (String jour : jours) {
                creneauxParJour.put(jour, new ArrayList<>());
            }
            
            if (dispo.getCreneauxParJour() != null) {
                for (CreneauDisponibilite creneau : dispo.getCreneauxParJour()) {
                    if (creneau.getPlagesHoraires() != null && creneau.getDate() != null) {
                        // Déterminer le jour de la semaine à partir de la date
                        String jour = creneau.getDate().getDayOfWeek().getDisplayName(
                            java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
                        // Capitaliser la première lettre
                        jour = jour.substring(0, 1).toUpperCase() + jour.substring(1);
                        
                        for (PlageHoraire plage : creneau.getPlagesHoraires()) {
                            Map<String, String> plageData = new HashMap<>();
                            plageData.put("heureDebut", plage.getHeureDebut().toString());
                            plageData.put("heureFin", plage.getHeureFin().toString());
                            if (creneauxParJour.containsKey(jour)) {
                                creneauxParJour.get(jour).add(plageData);
                            }
                        }
                    }
                }
            }
            
            result.put("creneaux", creneauxParJour);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des créneaux", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
