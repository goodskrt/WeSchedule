package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.service.DisponibiliteService;
import com.iusjc.weschedule.service.EnseignantService;
import com.iusjc.weschedule.service.ExcelEnseignantService;
import com.iusjc.weschedule.service.ExcelSalleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private ExcelEnseignantService excelEnseignantService;

    @Autowired
    private ExcelSalleService excelSalleService;

    @Autowired
    private SalleRepository salleRepository;

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
                
                // Charger les UEs via le service pour éviter les problèmes de lazy loading
                List<UE> ues = enseignantService.getUEsEnseignant(ens.getIdUser());
                data.put("nbUes", ues.size());
                
                List<Map<String, Object>> uesData = ues.stream().map(ue -> {
                    Map<String, Object> ueMap = new HashMap<>();
                    ueMap.put("idUE", ue.getIdUE().toString());
                    ueMap.put("code", ue.getCode());
                    ueMap.put("intitule", ue.getIntitule());
                    ueMap.put("duree", ue.getDuree());
                    return ueMap;
                }).toList();
                data.put("ues", uesData);
                
                int nbDispos = enseignantService.countDisponibilites(ens.getIdUser());
                data.put("nbDisponibilites", nbDispos);
                
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
            
            // Charger les UEs via le service
            List<UE> ues = enseignantService.getUEsEnseignant(id);
            List<Map<String, Object>> uesData = ues.stream().map(ue -> {
                Map<String, Object> ueMap = new HashMap<>();
                ueMap.put("idUE", ue.getIdUE().toString());
                ueMap.put("code", ue.getCode());
                ueMap.put("intitule", ue.getIntitule());
                ueMap.put("duree", ue.getDuree());
                return ueMap;
            }).toList();
            data.put("ues", uesData);
            
            List<String> ueIds = ues.stream().map(ue -> ue.getIdUE().toString()).toList();
            data.put("ueIds", ueIds);
            
            int nbDispos = enseignantService.countDisponibilites(id);
            data.put("nbDisponibilites", nbDispos);
            
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

    // ==================== EXCEL IMPORT / EXPORT ====================

    @GetMapping("/enseignants/export")
    public ResponseEntity<byte[]> exportEnseignants() {
        try {
            byte[] data = excelEnseignantService.exporterEnseignants();
            String filename = "enseignants_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export Excel enseignants", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/enseignants/import")
    public ResponseEntity<Map<String, Object>> importEnseignants(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            ExcelEnseignantService.ImportResult result = excelEnseignantService.importerEnseignants(file);
            response.put("success", true);
            response.put("succes", result.getSucces());
            response.put("erreurs", result.getErreurs());
            response.put("avertissements", result.getAvertissements());
            response.put("nbErreurs", result.getNbErreurs());
            response.put("message", result.getSucces() + " enseignant(s) importé(s)" +
                    (result.hasErreurs() ? ", " + result.getNbErreurs() + " erreur(s)" : " avec succès"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur import Excel enseignants", e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'import : " + e.getMessage());
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
                
                List<Map<String, Object>> classesData = new ArrayList<>();
                if (ue.getClasses() != null && !ue.getClasses().isEmpty()) {
                    for (Classe classe : ue.getClasses()) {
                        Map<String, Object> classeMap = new HashMap<>();
                        classeMap.put("idClasse", classe.getIdClasse());
                        classeMap.put("nom", classe.getNom());
                        classeMap.put("niveau", classe.getNiveau());
                        classeMap.put("effectif", classe.getEffectif());
                        if (classe.getFiliere() != null) {
                            classeMap.put("filiere", classe.getFiliere().getNomFiliere());
                        }
                        if (classe.getEcole() != null) {
                            classeMap.put("ecole", classe.getEcole().getNomEcole());
                        }
                        classesData.add(classeMap);
                    }
                }
                data.put("classes", classesData);
                
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

    // ==================== SALLES EXCEL ====================

    @GetMapping("/salles/export")
    public ResponseEntity<byte[]> exportSalles() {
        try {
            byte[] data = excelSalleService.exporterSalles();
            String filename = "salles_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export Excel salles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/salles/import")
    public ResponseEntity<Map<String, Object>> importSalles(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            ExcelSalleService.ImportResult result = excelSalleService.importerSalles(file);
            response.put("success", true);
            response.put("succes", result.getSucces());
            response.put("erreurs", result.getErreurs());
            response.put("nbErreurs", result.getNbErreurs());
            response.put("message", result.getSucces() + " salle(s) importée(s)" +
                    (result.hasErreurs() ? ", " + result.getNbErreurs() + " erreur(s)" : " avec succès"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur import Excel salles", e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'import : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
