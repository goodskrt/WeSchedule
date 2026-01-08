package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.service.DisponibiliteService;
import com.iusjc.weschedule.service.ExcelDisponibiliteService;
import com.iusjc.weschedule.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@Slf4j
public class DisponibiliteController {

    @Autowired
    private DisponibiliteService disponibiliteService;
    
    @Autowired
    private ExcelDisponibiliteService excelService;

    /**
     * Page principale des disponibilités
     */
    @GetMapping("/dashboard/enseignant/disponibilites")
    public String mesDisponibilites(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            Utilisateur utilisateur = userPrincipal.getUtilisateur();
            
            if (!(utilisateur instanceof Enseignant enseignant)) {
                return "redirect:/login";
            }

            List<DisponibiliteEnseignant> disponibilites = disponibiliteService.getDisponibilitesEnseignant(enseignant);
            disponibilites.sort((a, b) -> b.getDateDebut().compareTo(a.getDateDebut()));

            model.addAttribute("user", utilisateur);
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("disponibilites", disponibilites);
            
            return "dashboard/disponibilites";
        }
        return "redirect:/login";
    }

    /**
     * Page de création d'une nouvelle disponibilité
     */
    @GetMapping("/dashboard/enseignant/disponibilites/nouvelle")
    public String nouvelleDisponibilite(Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            Utilisateur utilisateur = userPrincipal.getUtilisateur();
            
            if (!(utilisateur instanceof Enseignant)) {
                return "redirect:/login";
            }

            model.addAttribute("user", utilisateur);
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("isEdit", false);
            model.addAttribute("creneauxList", new ArrayList<>());
            
            return "dashboard/disponibilite-form";
        }
        return "redirect:/login";
    }

    /**
     * Page de consultation d'une disponibilité (vue simple)
     */
    @GetMapping("/dashboard/enseignant/disponibilites/{id}/voir")
    public String voirDisponibilite(@PathVariable UUID id, Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            Utilisateur utilisateur = userPrincipal.getUtilisateur();
            
            if (!(utilisateur instanceof Enseignant)) {
                return "redirect:/login";
            }

            Optional<DisponibiliteEnseignant> disponibiliteOpt = disponibiliteService.getDisponibiliteById(id);
            if (disponibiliteOpt.isEmpty()) {
                return "redirect:/dashboard/enseignant/disponibilites";
            }

            DisponibiliteEnseignant disponibilite = disponibiliteOpt.get();
            Map<LocalDate, List<PlageHoraire>> emploiDuTemps = disponibiliteService.getEmploiDuTempsSemaine(id);

            model.addAttribute("user", utilisateur);
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("disponibilite", disponibilite);
            model.addAttribute("emploiDuTemps", emploiDuTemps);
            
            return "dashboard/disponibilite-voir";
        }
        return "redirect:/login";
    }

    /**
     * Page de modification d'une disponibilité
     */
    @GetMapping("/dashboard/enseignant/disponibilites/{id}/modifier")
    public String modifierDisponibilite(@PathVariable UUID id, Authentication auth, Model model) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            Utilisateur utilisateur = userPrincipal.getUtilisateur();
            
            if (!(utilisateur instanceof Enseignant)) {
                return "redirect:/login";
            }

            Optional<DisponibiliteEnseignant> disponibiliteOpt = disponibiliteService.getDisponibiliteById(id);
            if (disponibiliteOpt.isEmpty()) {
                return "redirect:/dashboard/enseignant/disponibilites";
            }

            DisponibiliteEnseignant disponibilite = disponibiliteOpt.get();
            Map<LocalDate, List<PlageHoraire>> emploiDuTemps = disponibiliteService.getEmploiDuTempsSemaine(id);
            
            // Convertir en liste pour JavaScript
            List<Map<String, Object>> creneauxList = new ArrayList<>();
            LocalDate debutSemaine = disponibilite.getDateDebut();
            
            log.info("Chargement des créneaux pour disponibilité {}", id);
            
            emploiDuTemps.forEach((date, plages) -> {
                int dayIndex = (int) ChronoUnit.DAYS.between(debutSemaine, date);
                log.info("Date: {}, dayIndex: {}, plages: {}", date, dayIndex, plages.size());
                for (PlageHoraire plage : plages) {
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("dayIndex", dayIndex);
                    slot.put("heureDebut", plage.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")));
                    slot.put("heureFin", plage.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm")));
                    creneauxList.add(slot);
                    log.info("Ajout créneau: jour={}, {}h-{}h", dayIndex, plage.getHeureDebut(), plage.getHeureFin());
                }
            });

            model.addAttribute("user", utilisateur);
            model.addAttribute("nomComplet", userPrincipal.getNomComplet());
            model.addAttribute("isEdit", true);
            model.addAttribute("disponibilite", disponibilite);
            model.addAttribute("creneauxList", creneauxList);
            
            return "dashboard/disponibilite-form";
        }
        return "redirect:/login";
    }

    /**
     * API - Créer une nouvelle disponibilité avec créneaux (JSON)
     */
    @PostMapping("/dashboard/enseignant/api/disponibilites/create")
    @ResponseBody
    public ResponseEntity<?> creerDisponibiliteComplete(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant enseignant)) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Accès non autorisé"));
                }

                String dateDebutStr = (String) payload.get("dateDebut");
                LocalDate dateDebut = LocalDate.parse(dateDebutStr);
                
                // Créer la disponibilité
                DisponibiliteEnseignant disponibilite = disponibiliteService.creerDisponibiliteSemaine(enseignant, dateDebut);
                
                // Ajouter les créneaux
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> creneaux = (List<Map<String, Object>>) payload.get("creneaux");
                
                if (creneaux != null) {
                    for (Map<String, Object> creneau : creneaux) {
                        int dayIndex = ((Number) creneau.get("dayIndex")).intValue();
                        String heureDebut = (String) creneau.get("heureDebut");
                        String heureFin = (String) creneau.get("heureFin");
                        
                        LocalDate date = disponibilite.getDateDebut().plusDays(dayIndex);
                        disponibiliteService.ajouterCreneau(
                            disponibilite.getId(),
                            date,
                            LocalTime.parse(heureDebut),
                            LocalTime.parse(heureFin)
                        );
                    }
                }
                
                return ResponseEntity.ok(Map.of("success", true, "message", "Disponibilité créée", "id", disponibilite.getId()));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Non authentifié"));
        } catch (Exception e) {
            log.error("Erreur création disponibilité", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * API - Mettre à jour une disponibilité (remplacer tous les créneaux)
     */
    @PostMapping("/dashboard/enseignant/api/disponibilites/{id}/update")
    @ResponseBody
    public ResponseEntity<?> updateDisponibilite(@PathVariable UUID id, @RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant)) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Accès non autorisé"));
                }

                Optional<DisponibiliteEnseignant> disponibiliteOpt = disponibiliteService.getDisponibiliteById(id);
                if (disponibiliteOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Disponibilité non trouvée"));
                }

                DisponibiliteEnseignant disponibilite = disponibiliteOpt.get();
                
                // Supprimer tous les créneaux existants
                disponibiliteService.supprimerTousLesCreneaux(id);
                
                // Ajouter les nouveaux créneaux
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> creneaux = (List<Map<String, Object>>) payload.get("creneaux");
                
                if (creneaux != null) {
                    for (Map<String, Object> creneau : creneaux) {
                        int dayIndex = ((Number) creneau.get("dayIndex")).intValue();
                        String heureDebut = (String) creneau.get("heureDebut");
                        String heureFin = (String) creneau.get("heureFin");
                        
                        LocalDate date = disponibilite.getDateDebut().plusDays(dayIndex);
                        disponibiliteService.ajouterCreneau(
                            id,
                            date,
                            LocalTime.parse(heureDebut),
                            LocalTime.parse(heureFin)
                        );
                    }
                }
                
                return ResponseEntity.ok(Map.of("success", true, "message", "Disponibilité mise à jour"));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Non authentifié"));
        } catch (Exception e) {
            log.error("Erreur mise à jour disponibilité", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * API - Créer une nouvelle disponibilité (simple, pour compatibilité)
     */
    @PostMapping("/dashboard/enseignant/api/disponibilites")
    @ResponseBody
    public ResponseEntity<?> creerDisponibilite(@RequestParam String dateDebut, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant enseignant)) {
                    return ResponseEntity.badRequest().body("Accès non autorisé");
                }

                LocalDate date = LocalDate.parse(dateDebut);
                DisponibiliteEnseignant disponibilite = disponibiliteService.creerDisponibiliteSemaine(enseignant, date);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Disponibilité créée avec succès",
                    "id", disponibilite.getId()
                ));
            }
            return ResponseEntity.badRequest().body("Non authentifié");
        } catch (Exception e) {
            log.error("Erreur lors de la création de disponibilité", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * API - Ajouter un créneau
     */
    @PostMapping("/dashboard/enseignant/api/disponibilites/{id}/creneaux")
    @ResponseBody
    public ResponseEntity<?> ajouterCreneau(
            @PathVariable UUID id,
            @RequestParam String date,
            @RequestParam String heureDebut,
            @RequestParam String heureFin,
            Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant)) {
                    return ResponseEntity.badRequest().body("Accès non autorisé");
                }

                LocalDate dateLocal = LocalDate.parse(date);
                LocalTime heureDebutLocal = LocalTime.parse(heureDebut);
                LocalTime heureFinLocal = LocalTime.parse(heureFin);
                
                PlageHoraire plageHoraire = disponibiliteService.ajouterCreneau(id, dateLocal, heureDebutLocal, heureFinLocal);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Créneau ajouté avec succès",
                    "plageHoraire", Map.of(
                        "id", plageHoraire.getId(),
                        "heureDebut", plageHoraire.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")),
                        "heureFin", plageHoraire.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"))
                    )
                ));
            }
            return ResponseEntity.badRequest().body("Non authentifié");
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de créneau", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * API - Supprimer une plage horaire
     */
    @DeleteMapping("/dashboard/enseignant/api/plages-horaires/{id}")
    @ResponseBody
    public ResponseEntity<?> supprimerPlageHoraire(@PathVariable UUID id, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant)) {
                    return ResponseEntity.badRequest().body("Accès non autorisé");
                }

                disponibiliteService.supprimerPlageHoraire(id);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Créneau supprimé avec succès"
                ));
            }
            return ResponseEntity.badRequest().body("Non authentifié");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de plage horaire", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * API - Supprimer une disponibilité complète
     */
    @DeleteMapping("/dashboard/enseignant/api/disponibilites/{id}")
    @ResponseBody
    public ResponseEntity<?> supprimerDisponibilite(@PathVariable UUID id, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant)) {
                    return ResponseEntity.badRequest().body("Accès non autorisé");
                }

                disponibiliteService.supprimerDisponibilite(id);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Disponibilité supprimée avec succès"
                ));
            }
            return ResponseEntity.badRequest().body("Non authentifié");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de disponibilité", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Télécharger le modèle Excel pour une semaine
     */
    @GetMapping("/dashboard/enseignant/api/disponibilites/excel/modele")
    public ResponseEntity<byte[]> telechargerModeleExcel(@RequestParam String dateDebut, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant)) {
                    return ResponseEntity.badRequest().build();
                }

                LocalDate date = LocalDate.parse(dateDebut);
                LocalDate debutSemaine = date.with(DayOfWeek.MONDAY);
                byte[] excelContent = excelService.genererModeleExcel(debutSemaine);
                
                String filename = "modele_disponibilites_" + debutSemaine.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx";
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du modèle Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporter une disponibilité existante vers Excel
     */
    @GetMapping("/dashboard/enseignant/api/disponibilites/{id}/excel/export")
    public ResponseEntity<byte[]> exporterDisponibiliteExcel(@PathVariable UUID id, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant)) {
                    return ResponseEntity.badRequest().build();
                }

                Optional<DisponibiliteEnseignant> dispoOpt = disponibiliteService.getDisponibiliteById(id);
                if (dispoOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }

                DisponibiliteEnseignant dispo = dispoOpt.get();
                byte[] excelContent = excelService.exporterDisponibilite(id);
                
                String filename = "disponibilites_" + dispo.getDateDebut().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx";
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de l'export Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Importer les disponibilités depuis un fichier Excel
     */
    @PostMapping("/dashboard/enseignant/api/disponibilites/excel/import")
    @ResponseBody
    public ResponseEntity<?> importerExcel(@RequestParam("file") MultipartFile file, Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                Utilisateur utilisateur = userPrincipal.getUtilisateur();
                
                if (!(utilisateur instanceof Enseignant enseignant)) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Accès non autorisé"));
                }

                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Fichier vide"));
                }

                String filename = file.getOriginalFilename();
                if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Format de fichier invalide. Utilisez un fichier Excel (.xlsx ou .xls)"));
                }

                DisponibiliteEnseignant disponibilite = excelService.importerExcel(file, enseignant);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Disponibilités importées avec succès",
                    "id", disponibilite.getId().toString(),
                    "dateDebut", disponibilite.getDateDebut().toString(),
                    "dateFin", disponibilite.getDateFin().toString()
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Non authentifié"));
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de l'import Excel", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de l'import Excel", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Erreur lors de l'import: " + e.getMessage()));
        }
    }
}