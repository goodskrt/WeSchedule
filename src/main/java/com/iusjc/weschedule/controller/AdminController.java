package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.SalleRepository;
import com.iusjc.weschedule.repositories.CoursRepository;
import com.iusjc.weschedule.repositories.UERepository;
import com.iusjc.weschedule.service.DisponibiliteService;
import com.iusjc.weschedule.service.ExcelEquipementService;
import com.iusjc.weschedule.service.EnseignantService;
import com.iusjc.weschedule.service.ExcelEnseignantService;
import com.iusjc.weschedule.service.ExcelSalleService;
import com.iusjc.weschedule.service.ExcelClasseService;
import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.FiliereRepository;
import com.iusjc.weschedule.repositories.TypeEquipementRepository;
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
    private ExcelEquipementService excelEquipementService;

    @Autowired
    private EnseignantService enseignantService;

    @Autowired
    private DisponibiliteService disponibiliteService;

    @Autowired
    private ExcelEnseignantService excelEnseignantService;

    @Autowired
    private ExcelSalleService excelSalleService;

    @Autowired
    private ExcelClasseService excelClasseService;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UERepository ueRepository;

    @Autowired
    private com.iusjc.weschedule.repositories.ClasseRepository classeRepository;

    @Autowired
    private FiliereRepository filiereRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TypeEquipementRepository typeEquipementRepository;

    @Autowired
    private com.iusjc.weschedule.repositories.EtudiantRepository etudiantRepository;

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
                
                // UEs déjà chargées via JOIN FETCH dans findAllWithUEs()
                List<UE> ues = ens.getUesEnseignees() != null ? new ArrayList<>(ens.getUesEnseignees()) : new ArrayList<>();
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
                email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nom et email sont obligatoires");
                return ResponseEntity.badRequest().body(response);
            }

            if (prenom == null) {
                prenom = "";
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
            
            // Utiliser le service qui charge tout en transaction (évite LazyInitializationException)
            Map<java.time.LocalDate, List<PlageHoraire>> emploiDuTemps = disponibiliteService.getEmploiDuTempsSemaine(id);
            
            // Grouper les créneaux par nom de jour
            Map<String, List<Map<String, String>>> creneauxParJour = new LinkedHashMap<>();
            String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            for (String jour : jours) {
                creneauxParJour.put(jour, new ArrayList<>());
            }
            
            for (Map.Entry<java.time.LocalDate, List<PlageHoraire>> entry : emploiDuTemps.entrySet()) {
                String jour = entry.getKey().getDayOfWeek().getDisplayName(
                    java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
                jour = jour.substring(0, 1).toUpperCase() + jour.substring(1);
                
                List<Map<String, String>> plagesData = entry.getValue().stream().map(plage -> {
                    Map<String, String> p = new HashMap<>();
                    p.put("heureDebut", plage.getHeureDebut().toString());
                    p.put("heureFin", plage.getHeureFin().toString());
                    return p;
                }).toList();
                
                if (creneauxParJour.containsKey(jour)) {
                    creneauxParJour.get(jour).addAll(plagesData);
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

    // ==================== FILIERES ====================

    @GetMapping("/filieres")
    public ResponseEntity<List<Map<String, Object>>> getFilieres(
            @RequestParam(required = false) UUID ecoleId,
            @RequestParam(required = false) String niveau) {
        try {
            List<com.iusjc.weschedule.models.Filiere> filieres;
            if (ecoleId != null && niveau != null && !niveau.isBlank()) {
                filieres = filiereRepository.findByEcoleAndNiveau(ecoleId, niveau);
            } else if (ecoleId != null) {
                filieres = filiereRepository.findByEcoleIdEcole(ecoleId);
            } else {
                filieres = filiereRepository.findAll();
            }
            List<Map<String, Object>> result = filieres.stream().map(f -> {
                Map<String, Object> m = new HashMap<>();
                m.put("idFiliere", f.getIdFiliere().toString());
                m.put("nomFiliere", f.getNomFiliere());
                m.put("niveaux", f.getNiveaux());
                return m;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur récupération filières", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== CLASSES EXCEL ====================

    @GetMapping("/classes/export")
    public ResponseEntity<byte[]> exportClasses() {
        try {
            byte[] data = excelClasseService.exporterClasses();
            String filename = "classes_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export Excel classes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/classes/import")
    public ResponseEntity<Map<String, Object>> importClasses(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            ExcelClasseService.ImportResult result = excelClasseService.importerClasses(file);
            response.put("success", true);
            response.put("succes", result.getSucces());
            response.put("erreurs", result.getErreurs());
            response.put("nbErreurs", result.getNbErreurs());
            response.put("message", result.getSucces() + " classe(s) importée(s)" +
                    (result.hasErreurs() ? ", " + result.getNbErreurs() + " erreur(s)" : " avec succès"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur import Excel classes", e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'import : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== SALLES IMPORT ====================

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

    // ==================== EQUIPEMENTS EXCEL ====================

    @GetMapping("/equipements/export")
    public ResponseEntity<byte[]> exportEquipements() {
        try {
            byte[] data = excelEquipementService.exporterEquipements();
            String filename = "equipements_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export Excel équipements", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/equipements/import")
    public ResponseEntity<Map<String, Object>> importEquipements(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            ExcelEquipementService.ImportResult result = excelEquipementService.importerEquipements(file);
            response.put("success", true);
            response.put("succes", result.getSucces());
            response.put("erreurs", result.getErreurs());
            response.put("avertissements", result.getAvertissements());
            response.put("nbErreurs", result.getNbErreurs());
            response.put("message", result.getSucces() + " équipement(s) importé(s)" +
                    (result.hasErreurs() ? ", " + result.getNbErreurs() + " erreur(s)" : " avec succès"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Erreur import Excel équipements", e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'import : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== UEs EXCEL ====================

    @Autowired
    private com.iusjc.weschedule.service.ExcelUEService excelUEService;

    @GetMapping("/ues/export")
    public ResponseEntity<byte[]> exportUEs() {
        try {
            byte[] data = excelUEService.exporterUEs();
            String filename = "ues_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Erreur export Excel UEs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ues/import")
    public ResponseEntity<Map<String, Object>> importUEs(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            com.iusjc.weschedule.service.ExcelUEService.ImportResult result = excelUEService.importerUEs(file);
            response.put("success", true);
            response.put("succes", result.getSucces());
            response.put("erreurs", result.getErreurs());
            response.put("avertissements", result.getAvertissements());
            response.put("message", result.getSucces() + " UE(s) importée(s)" +
                    (result.hasErreurs() ? ", " + result.getNbErreurs() + " erreur(s)" : " avec succès"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur import Excel UEs", e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'import : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== CLASSES API ====================

    @GetMapping("/classes")
    public ResponseEntity<List<Map<String, Object>>> getAllClasses() {
        try {
            List<com.iusjc.weschedule.models.Classe> classes = classeRepository.findAll();
            List<Map<String, Object>> result = classes.stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("idClasse", c.getIdClasse().toString());
                m.put("nom", c.getNom());
                m.put("niveau", c.getNiveau());
                m.put("langue", c.getLangue());
                m.put("effectif", c.getEffectif());
                if (c.getEcole() != null) m.put("ecole", c.getEcole().getNomEcole());
                if (c.getFiliere() != null) m.put("filiere", c.getFiliere().getNomFiliere());
                return m;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur récupération classes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== SALLES API ====================

    @GetMapping("/salles")
    public ResponseEntity<List<Map<String, Object>>> getAllSalles() {
        try {
            List<com.iusjc.weschedule.models.Salle> salles = salleRepository.findAll();
            List<Map<String, Object>> result = salles.stream().map(s -> {
                Map<String, Object> m = new HashMap<>();
                m.put("idSalle", s.getIdSalle().toString());
                m.put("nomSalle", s.getNomSalle());
                m.put("capacite", s.getCapacite());
                m.put("typeSalle", s.getTypeSalle() != null ? s.getTypeSalle().name() : null);
                m.put("statut", s.getStatut() != null ? s.getStatut().name() : null);
                m.put("batiment", s.getBatiment());
                m.put("etage", s.getEtage());
                return m;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur récupération salles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== COURS PAR CLASSE ====================

    @GetMapping("/cours/classe/{classeId}")
    public ResponseEntity<List<Map<String, Object>>> getCoursByClasse(@PathVariable UUID classeId) {
        try {
            List<com.iusjc.weschedule.models.Cours> cours = coursRepository.findAll().stream()
                    .filter(c -> c.getClasse() != null && c.getClasse().getIdClasse().equals(classeId))
                    .toList();
            List<Map<String, Object>> result = cours.stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("idCours", c.getIdCours().toString());
                m.put("intitule", c.getIntitule());
                m.put("typeCours", c.getTypeCours() != null ? c.getTypeCours().name() : null);
                m.put("dureeTotal", c.getDureeTotal());
                m.put("dureeRestante", c.getDureeRestante());
                if (c.getUe() != null) {
                    Map<String, Object> ue = new HashMap<>();
                    ue.put("idUE", c.getUe().getIdUE().toString());
                    ue.put("code", c.getUe().getCode());
                    ue.put("intitule", c.getUe().getIntitule());
                    ue.put("semestre", c.getUe().getSemestre());
                    m.put("ue", ue);
                }
                if (c.getEnseignant() != null) {
                    Map<String, Object> ens = new HashMap<>();
                    ens.put("idUser", c.getEnseignant().getIdUser().toString());
                    ens.put("nom", c.getEnseignant().getNom());
                    ens.put("prenom", c.getEnseignant().getPrenom());
                    m.put("enseignant", ens);
                }
                return m;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur récupération cours par classe", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== STATS DASHBOARD ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            // Cours par type
            List<com.iusjc.weschedule.models.Cours> tousLesCours = coursRepository.findAll();
            long nbCM = tousLesCours.stream().filter(c -> c.getTypeCours() != null && c.getTypeCours().name().equals("CM")).count();
            long nbTD = tousLesCours.stream().filter(c -> c.getTypeCours() != null && c.getTypeCours().name().equals("TD")).count();
            long nbTP = tousLesCours.stream().filter(c -> c.getTypeCours() != null && c.getTypeCours().name().equals("TP")).count();
            long totalCours = tousLesCours.size();
            stats.put("cours_cm", nbCM);
            stats.put("cours_td", nbTD);
            stats.put("cours_tp", nbTP);
            stats.put("cours_total", totalCours);

            // Cours avec enseignant
            long coursAvecEns = tousLesCours.stream().filter(c -> c.getEnseignant() != null).count();
            stats.put("cours_avec_enseignant", coursAvecEns);

            // Cours terminés (dureeRestante == 0)
            long coursTermines = tousLesCours.stream().filter(c -> c.getDureeRestante() != null && c.getDureeRestante() == 0).count();
            stats.put("cours_termines", coursTermines);

            // Salles par type
            List<com.iusjc.weschedule.models.Salle> toutesSalles = salleRepository.findAll();
            long totalSalles = toutesSalles.size();
            Map<String, Long> sallesParType = new LinkedHashMap<>();
            for (com.iusjc.weschedule.enums.TypeSalle t : com.iusjc.weschedule.enums.TypeSalle.values()) {
                sallesParType.put(t.name(), toutesSalles.stream().filter(s -> t.equals(s.getTypeSalle())).count());
            }
            stats.put("salles_par_type", sallesParType);
            stats.put("salles_total", totalSalles);

            // Salles disponibles
            long sallesDispo = toutesSalles.stream()
                    .filter(s -> s.getStatut() != null && s.getStatut().name().equals("DISPONIBLE")).count();
            stats.put("salles_disponibles", sallesDispo);

            // Équipements
            long totalEquipements = equipmentRepository.count();
            stats.put("equipements_total", totalEquipements);
            stats.put("equipements_types_total", typeEquipementRepository.count());
            Map<String, Long> equipementsParStatut = new LinkedHashMap<>();
            for (StatutEquipement st : StatutEquipement.values()) {
                equipementsParStatut.put(st.name(), equipmentRepository.countByStatut(st));
            }
            stats.put("equipements_par_statut", equipementsParStatut);
            stats.put("equipements_stock", equipmentRepository.countBySalleIsNull());

            stats.put("etudiants_total", etudiantRepository.count());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur stats dashboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

