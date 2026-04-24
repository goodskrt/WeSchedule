package com.iusjc.weschedule.config;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.enums.TypeCours;
import com.iusjc.weschedule.enums.TypeSalle;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Configuration
@Slf4j
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initializeDatabase(
            AdministrateurRepository adminRepo,
            EnseignantRepository enseignantRepo,
            UtilisateurRepository utilisateurRepo,
            EcoleRepository ecoleRepo,
            FiliereRepository filiereRepo,
            ClasseRepository classeRepo,
            UERepository ueRepo,
            CoursRepository coursRepo,
            SalleRepository salleRepo,
            GroupeRepository groupeRepo,
            EtudiantRepository etudiantRepo,
            DisponibiliteEnseignantRepository disponibiliteRepo,
            CreneauDisponibiliteRepository creneauDispoRepo,
            PlageHoraireRepository plageHoraireRepo,
            PasswordResetTokenRepository passwordResetTokenRepo,
            NotificationRepository notificationRepo,
            SeanceClasseRepository seanceClasseRepo,
            EmploiDuTempsClasseRepository emploiDuTempsClasseRepo,
            EquipmentRepository equipmentRepo,
            EquipmentAssignmentRepository equipmentAssignmentRepo,
            TypeEquipementRepository typeEquipementRepo,
            CategorieEquipementRepository categorieEquipementRepo,
            DemandeReservationSalleRepository demandeReservationSalleRepo) {
        return args -> {
            log.info("=== INITIALISATION COMPLETE DE LA BASE DE DONNEES AVEC DONNEES FRONTEND ===");

            // VIDER COMPLETEMENT LA BASE DE DONNEES DANS LE BON ORDRE
            log.info("Suppression de toutes les donnees existantes...");

            // 1. Supprimer d'abord les dépendances les plus faibles (sans FK vers d'autres tables)
            equipmentAssignmentRepo.deleteAll();
            equipmentRepo.deleteAll();
            typeEquipementRepo.deleteAll();
            categorieEquipementRepo.deleteAll();
            plageHoraireRepo.deleteAll();
            creneauDispoRepo.deleteAll();
            disponibiliteRepo.deleteAll();
            passwordResetTokenRepo.deleteAll();
            notificationRepo.deleteAll();
            demandeReservationSalleRepo.deleteAll();

            // 2. Supprimer les cours AVANT les enseignants (car cours a FK vers enseignants)
            coursRepo.deleteAll();

            // 3. Supprimer les enseignants AVANT les UEs (car enseignant_ue a FK vers ues)
            enseignantRepo.deleteAll();

            // 4. Supprimer les UEs (enseignant_ue déjà vidée via cascade enseignants)
            ueRepo.deleteAll();

            // 5. Supprimer les séances et emplois du temps
            seanceClasseRepo.deleteAll();
            emploiDuTempsClasseRepo.deleteAll();

            // 6. Supprimer les étudiants (a FK vers classes et groupes)
            etudiantRepo.deleteAll();

            // 7. Supprimer les administrateurs
            adminRepo.deleteAll();

            // 8. Supprimer les utilisateurs (maintenant que enseignants, étudiants et admin sont supprimés)
            utilisateurRepo.deleteAll();

            // 9. Supprimer les salles
            salleRepo.deleteAll();

            // 10. Supprimer les classes (a FK vers ecoles et filieres)
            classeRepo.deleteAll();

            // 11. Supprimer les groupes
            groupeRepo.deleteAll();

            // 12. Supprimer les filières
            filiereRepo.deleteAll();

            // 13. Supprimer les écoles en dernier
            ecoleRepo.deleteAll();

                        log.info("Base de donnees videe avec succes !");

            // ========== 1. CREATION DE L'ECOLE SAINT JEAN INGENIEUR ==========
            log.info("Creation de l'ecole Saint Jean Ingenieur...");

            Ecole sji = new Ecole();
            sji.setNomEcole("Saint Jean Ingenieur");
            sji.setCode("SJI");
            sji.setCouleur("bg-blue-500");
            sji.setAdresse("Douala, Cameroun");
            sji.setTelephone("+237 233 42 15 67");
            sji.setEmail("contact@sji.edu.cm");
            ecoleRepo.save(sji);

            // ========== 2. CREATION DE LA FILIERE ISI ==========
            log.info("Creation de la filiere ISI...");

            Filiere filiereIsi = new Filiere();
            filiereIsi.setNomFiliere("ISI");
            filiereIsi.setDescription("Systèmes d'Information et Informatique");
            filiereIsi.setEcole(sji);
            filiereIsi.setNiveaux(java.util.List.of("Niveau 1", "Niveau 2", "Niveau 3", "Niveau 4"));
            filiereRepo.save(filiereIsi);

            // ========== 3. CREATION DES CLASSES INGE4 ISI FR ET INGE4 ISI EN ==========
            log.info("Creation des classes INGE4 ISI FR et EN...");

            Classe inge4IsiFr = new Classe();
            inge4IsiFr.setNom("INGE4 ISI FR");
            inge4IsiFr.setNiveau("Niveau 4");
            inge4IsiFr.setEcole(sji);
            inge4IsiFr.setFiliere(filiereIsi);
            inge4IsiFr.setEffectif(38);
            inge4IsiFr.setLangue("Francophone");
            classeRepo.save(inge4IsiFr);

            Classe inge4IsiEn = new Classe();
            inge4IsiEn.setNom("INGE4 ISI EN");
            inge4IsiEn.setNiveau("Niveau 4");
            inge4IsiEn.setEcole(sji);
            inge4IsiEn.setFiliere(filiereIsi);
            inge4IsiEn.setEffectif(38);
            inge4IsiEn.setLangue("Anglophone");
            classeRepo.save(inge4IsiEn);

            // ========== 3b. GROUPES ET ÉTUDIANTS (entités métier, sans compte utilisateur) ==========
            log.info("Creation des groupes et etudiants...");
            Groupe grpA = new Groupe();
            grpA.setNomGroupe("Groupe A");
            groupeRepo.save(grpA);
            Groupe grpB = new Groupe();
            grpB.setNomGroupe("Groupe B");
            groupeRepo.save(grpB);

            Etudiant et1 = new Etudiant();
            et1.setNom("Nkoulou");
            et1.setPrenom("Marc");
            et1.setNumeroEtudiant("SJI-ISI-2026-001");
            et1.setEmail("marc.nkoulou.contact@sji.edu.cm");
            et1.setTelephone("+237 677 01 02 03");
            et1.setClasse(inge4IsiFr);
            et1.setGroupe(grpA);
            etudiantRepo.save(et1);

            Etudiant et2 = new Etudiant();
            et2.setNom("Fotsing");
            et2.setPrenom("Audrey");
            et2.setNumeroEtudiant("SJI-ISI-2026-002");
            et2.setEmail("audrey.fotsing.contact@sji.edu.cm");
            et2.setTelephone("+237 677 04 05 06");
            et2.setClasse(inge4IsiFr);
            et2.setGroupe(grpB);
            etudiantRepo.save(et2);

            Etudiant et3 = new Etudiant();
            et3.setNom("Tchouassi");
            et3.setPrenom("Brice");
            et3.setNumeroEtudiant("SJI-ISI-2026-003");
            et3.setClasse(inge4IsiEn);
            et3.setGroupe(grpA);
            etudiantRepo.save(et3);

            Etudiant et4 = new Etudiant();
            et4.setNom("Nguepi");
            et4.setPrenom("Clarisse");
            et4.setNumeroEtudiant("SJI-ISI-2026-004");
            et4.setEmail("clarisse.nguepi.contact@sji.edu.cm");
            et4.setClasse(inge4IsiEn);
            et4.setGroupe(grpB);
            etudiantRepo.save(et4);

            // ========== 4. CREATION DES UEs POUR LES DEUX CLASSES ==========
            log.info("Creation des UEs pour INGE4 ISI FR et EN...");

            UE ue401 = new UE();
            ue401.setCode("ING4167");
            ue401.setIntitule("Technologie et programmation Web");
            ue401.setCredits(4);
            ue401.setSemestre(1);
            ue401.setDuree(40);
            ue401.setStatut(StatutUE.ACTIF);
            ue401.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue401);

            UE ue402 = new UE();
            ue402.setCode("ISI4217");
            ue402.setIntitule("Développement mobile");
            ue402.setCredits(4);
            ue402.setSemestre(1);
            ue402.setDuree(40);
            ue402.setStatut(StatutUE.ACTIF);
            ue402.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue402);

            UE ue403 = new UE();
            ue403.setCode("ING4178");
            ue403.setIntitule("Projet Transversal ISI");
            ue403.setCredits(4);
            ue403.setSemestre(2);
            ue403.setDuree(50);
            ue403.setStatut(StatutUE.ACTIF);
            ue403.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue403);

            UE ue404 = new UE();
            ue404.setCode("ISI4177");
            ue404.setIntitule("Recherche opérationnelle");
            ue404.setCredits(4);
            ue404.setSemestre(1);
            ue404.setDuree(40);
            ue404.setStatut(StatutUE.ACTIF);
            ue404.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue404);

            UE ue405 = new UE();
            ue405.setCode("ISI4197B");
            ue405.setIntitule("CI/CD et Conteneurisation");
            ue405.setCredits(2);
            ue405.setSemestre(1);
            ue405.setDuree(20);
            ue405.setStatut(StatutUE.ACTIF);
            ue405.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue405);

            UE ue406 = new UE();
            ue406.setCode("ISI4187B");
            ue406.setIntitule("Architecture des systèmes d'information");
            ue406.setCredits(4);
            ue406.setSemestre(1);
            ue406.setDuree(40);
            ue406.setStatut(StatutUE.ACTIF);
            ue406.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue406);

            UE ue407 = new UE();
            ue407.setCode("ISI4167B");
            ue407.setIntitule("Algorithme et structures de données Avancé");
            ue407.setCredits(4);
            ue407.setSemestre(1);
            ue407.setDuree(40);
            ue407.setStatut(StatutUE.ACTIF);
            ue407.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue407);

            UE ue408 = new UE();
            ue408.setCode("ISI4207");
            ue408.setIntitule("Analyse de Données");
            ue408.setCredits(4);
            ue408.setSemestre(1);
            ue408.setDuree(40);
            ue408.setStatut(StatutUE.ACTIF);
            ue408.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue408);

            UE ue409 = new UE();
            ue409.setCode("ISI4197A");
            ue409.setIntitule("Gestion agile des projets");
            ue409.setCredits(2);
            ue409.setSemestre(1);
            ue409.setDuree(20);
            ue409.setStatut(StatutUE.ACTIF);
            ue409.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue409);

            UE ue410 = new UE();
            ue410.setCode("SAGESSE4");
            ue410.setIntitule("Sagesse et Science");
            ue410.setCredits(0);
            ue410.setSemestre(1);
            ue410.setDuree(0);
            ue410.setStatut(StatutUE.ACTIF);
            ue410.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue410);

            UE ue411 = new UE();
            ue411.setCode("ISI4237");
            ue411.setIntitule("Marketing Digital");
            ue411.setCredits(1);
            ue411.setSemestre(1);
            ue411.setDuree(10);
            ue411.setStatut(StatutUE.ACTIF);
            ue411.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue411);

            UE ue412 = new UE();
            ue412.setCode("ECO4167");
            ue412.setIntitule("Initiation à l'analyse financière");
            ue412.setCredits(1);
            ue412.setSemestre(1);
            ue412.setDuree(10);
            ue412.setStatut(StatutUE.ACTIF);
            ue412.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ue412);

            UE ueToeic1 = new UE();
            ueToeic1.setCode("TOEIC4");
            ueToeic1.setIntitule("TOEIC PREPARATION");
            ueToeic1.setCredits(0);
            ueToeic1.setSemestre(1);
            ueToeic1.setDuree(20);
            ueToeic1.setStatut(StatutUE.ACTIF);
            ueToeic1.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueToeic1);

            UE ueToeic2 = new UE();
            ueToeic2.setCode("TOEIC4");
            ueToeic2.setIntitule("TOEIC PREPARATION");
            ueToeic2.setCredits(0);
            ueToeic2.setSemestre(2);
            ueToeic2.setDuree(20);
            ueToeic2.setStatut(StatutUE.ACTIF);
            ueToeic2.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueToeic2);

            // Semestre 2
            UE ueComp = new UE();
            ueComp.setCode("ISI4168");
            ueComp.setIntitule("Compilation");
            ueComp.setCredits(3);
            ueComp.setSemestre(2);
            ueComp.setDuree(45);
            ueComp.setStatut(StatutUE.ACTIF);
            ueComp.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueComp);

            UE ueSecu = new UE();
            ueSecu.setCode("ISI4178");
            ueSecu.setIntitule("Concepts de bases de la sécurité informatique");
            ueSecu.setCredits(3);
            ueSecu.setSemestre(2);
            ueSecu.setDuree(45);
            ueSecu.setStatut(StatutUE.ACTIF);
            ueSecu.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueSecu);

            UE ueMl = new UE();
            ueMl.setCode("ISI4188");
            ueMl.setIntitule("Machine Learning");
            ueMl.setCredits(3);
            ueMl.setSemestre(2);
            ueMl.setDuree(45);
            ueMl.setStatut(StatutUE.ACTIF);
            ueMl.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueMl);

            UE ueQa = new UE();
            ueQa.setCode("ISI4198");
            ueQa.setIntitule("Test Logiciel et Assurance Qualité");
            ueQa.setCredits(3);
            ueQa.setSemestre(2);
            ueQa.setDuree(45);
            ueQa.setStatut(StatutUE.ACTIF);
            ueQa.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueQa);

            UE ueCloud = new UE();
            ueCloud.setCode("ISI4228");
            ueCloud.setIntitule("Cloud, Virtualisation et datacenter");
            ueCloud.setCredits(3);
            ueCloud.setSemestre(2);
            ueCloud.setDuree(45);
            ueCloud.setStatut(StatutUE.ACTIF);
            ueCloud.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueCloud);

            UE ueEntre = new UE();
            ueEntre.setCode("HUM4168");
            ueEntre.setIntitule("Entrepreneuriat");
            ueEntre.setCredits(1);
            ueEntre.setSemestre(2);
            ueEntre.setDuree(10);
            ueEntre.setStatut(StatutUE.ACTIF);
            ueEntre.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueEntre);

            UE ueRecherche = new UE();
            ueRecherche.setCode("IRS4168");
            ueRecherche.setIntitule("Initiation à la recherche");
            ueRecherche.setCredits(3);
            ueRecherche.setSemestre(2);
            ueRecherche.setDuree(45);
            ueRecherche.setStatut(StatutUE.ACTIF);
            ueRecherche.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueRecherche);

            UE ueReflexion = new UE();
            ueReflexion.setCode("HUM4198");
            ueReflexion.setIntitule("Reflexion Humaine");
            ueReflexion.setCredits(1);
            ueReflexion.setSemestre(2);
            ueReflexion.setDuree(10);
            ueReflexion.setStatut(StatutUE.ACTIF);
            ueReflexion.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueReflexion);

            UE uePsy = new UE();
            uePsy.setCode("HUM4198B");
            uePsy.setIntitule("Psychologie du travail");
            uePsy.setCredits(1);
            uePsy.setSemestre(2);
            uePsy.setDuree(20);
            uePsy.setStatut(StatutUE.ACTIF);
            uePsy.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(uePsy);

            UE ueEthique = new UE();
            ueEthique.setCode("HUM4188");
            ueEthique.setIntitule("Ethique de l'ingénieur");
            ueEthique.setCredits(1);
            ueEthique.setSemestre(2);
            ueEthique.setDuree(10);
            ueEthique.setStatut(StatutUE.ACTIF);
            ueEthique.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueEthique);

            // Semestre 1 - Projet
            UE ueProjet = new UE();
            ueProjet.setCode("ING4178");
            ueProjet.setIntitule("Projet Transversal ISI");
            ueProjet.setCredits(4);
            ueProjet.setSemestre(1);
            ueProjet.setDuree(40);
            ueProjet.setStatut(StatutUE.ACTIF);
            ueProjet.setClasses(Set.of(inge4IsiFr, inge4IsiEn));
            ueRepo.save(ueProjet);

            log.info("UEs creees : 34 UEs");

            // ========== 7b. CREATION DES COURS A PARTIR DES UEs ==========
            log.info("Creation des cours à partir des UEs fournies...");
            
            // Specification des cours : code UE -> {CM: {dureeTotal, dureeSeanceParJour}, TP: {...}, TD: {...}}
            // Basé sur les données fournies : Technologies et programmation Web ING4167 4h cours, 4h tp, etc.
            Map<String, Map<String, Map<String, Integer>>> coursSpec = Map.ofEntries(
                // Technologies et programmation Web - ING4167 - 4h cours, 4h tp
                Map.entry("ING4167", Map.of(
                    "CM", Map.of("dureeTotal", 32, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 8, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Développement mobile - ISI4217 - 4h cours, 4h tp
                Map.entry("ISI4217", Map.of(
                    "CM", Map.of("dureeTotal", 32, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 8, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Recherche opérationnelle - ISI4177 - 4h cours, 4h tp
                Map.entry("ISI4177", Map.of(
                    "CM", Map.of("dureeTotal", 32, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 8, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // CI/CD et Conteneurisation - ISI4197B - 4h cours
                Map.entry("ISI4197B", Map.of(
                    "CM", Map.of("dureeTotal", 20, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Architecture des systèmes d'information - ISI4187B - 4h cours, 4h tp
                Map.entry("ISI4187B", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Algorithme et structures de données Avancé - ISI4167B - 4h cours, 4h tp
                Map.entry("ISI4167B", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Analyse de Données - ISI4207 - 4h cours
                Map.entry("ISI4207", Map.of(
                    "CM", Map.of("dureeTotal", 40, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Gestion agile des projets - ISI4197A - 4h cours
                Map.entry("ISI4197A", Map.of(
                    "CM", Map.of("dureeTotal", 20, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Sagesse et Science - SAGESSE4 - 2h cours
                Map.entry("SAGESSE4", Map.of(
                    "CM", Map.of("dureeTotal", 8, "dureeSeanceParJour", 2),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Marketing Digital - ISI4237 - 4h cours
                Map.entry("ISI4237", Map.of(
                    "CM", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Initiation à l'analyse financière - ECO4167 - 4h cours
                Map.entry("ECO4167", Map.of(
                    "CM", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // TOEIC PREPARATION - TOEIC4 - 4h cours
                Map.entry("TOEIC4", Map.of(
                    "CM", Map.of("dureeTotal", 16, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Compilation - ISI4168 - 4h cours, 4h tp
                Map.entry("ISI4168", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Concepts de bases de la sécurité informatique - ISI4178 - 4h cours, 4h tp
                Map.entry("ISI4178", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Machine Learning - ISI4188 - 4h cours, 4h tp
                Map.entry("ISI4188", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Test Logiciel et Assurance Qualité - ISI4198 - 4h cours
                Map.entry("ISI4198", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Cloud, Virtualisation et datacenter - ISI4228 - 4h cours
                Map.entry("ISI4228", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Projet Transversal ISI - ING4178 - 2h cours
                Map.entry("ING4178", Map.of(
                    "CM", Map.of("dureeTotal", 32, "dureeSeanceParJour", 2),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Entrepreneuriat 1 - HUM4168 - 4h cours
                Map.entry("HUM4168", Map.of(
                    "CM", Map.of("dureeTotal", 16, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Initiation à la recherche - IRS4168 - 4h cours
                Map.entry("IRS4168", Map.of(
                    "CM", Map.of("dureeTotal", 36, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Reflexion Humaine 3 - HUM4198 - 1h cours
                Map.entry("HUM4198", Map.of(
                    "CM", Map.of("dureeTotal", 4, "dureeSeanceParJour", 1),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Psychologie du travail - HUM4198B - 4h cours
                Map.entry("HUM4198B", Map.of(
                    "CM", Map.of("dureeTotal", 16, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                )),
                // Ethique de l'ingénieur - HUM4188 - 4h cours
                Map.entry("HUM4188", Map.of(
                    "CM", Map.of("dureeTotal", 4, "dureeSeanceParJour", 4),
                    "TP", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0),
                    "TD", Map.of("dureeTotal", 0, "dureeSeanceParJour", 0)
                ))
            );
            
            List<Cours> coursSeed = new ArrayList<>();
            List<Classe> classesPourCours = List.of(inge4IsiFr, inge4IsiEn);  // Les deux classes pour avoir les cours pour FR et EN
            
            // Itérer sur toutes les UEs de la base de données (pour éviter les doublons comme TOEIC4)
            List<UE> toutesLesUEs = ueRepo.findAll();
            for (UE ue : toutesLesUEs) {
                String codeUE = ue.getCode();
                Map<String, Map<String, Integer>> typesCoursSpec = coursSpec.get(codeUE);
                if (typesCoursSpec == null) {
                    log.debug("Specification de cours non trouvée pour UE : {} - Pas de cours créés", codeUE);
                    continue;
                }
                
                // Créer les CM et TP pour chaque classe
                for (Classe classe : classesPourCours) {
                    // Créer CM
                    Map<String, Integer> cmSpec = typesCoursSpec.get("CM");
                    if (cmSpec != null && cmSpec.get("dureeTotal") > 0) {
                        Cours coursCM = new Cours();
                        coursCM.setIntitule(ue.getIntitule() + " - CM");
                        coursCM.setTypeCours(TypeCours.CM);
                        coursCM.setDureeTotal(cmSpec.get("dureeTotal"));
                        coursCM.setDureeRestante(cmSpec.get("dureeTotal"));
                        coursCM.setDureeSeanceParJour(cmSpec.get("dureeSeanceParJour"));
                        coursCM.setUe(ue);
                        coursCM.setClasse(classe);
                        coursCM.setDescription("Cours magistral créé depuis la configuration initiale");
                        coursSeed.add(coursCM);
                    }
                    
                    // Créer TP
                    Map<String, Integer> tpSpec = typesCoursSpec.get("TP");
                    if (tpSpec != null && tpSpec.get("dureeTotal") > 0) {
                        Cours coursTP = new Cours();
                        coursTP.setIntitule(ue.getIntitule() + " - TP");
                        coursTP.setTypeCours(TypeCours.TP);
                        coursTP.setDureeTotal(tpSpec.get("dureeTotal"));
                        coursTP.setDureeRestante(tpSpec.get("dureeTotal"));
                        coursTP.setDureeSeanceParJour(tpSpec.get("dureeSeanceParJour"));
                        coursTP.setUe(ue);
                        coursTP.setClasse(classe);
                        coursTP.setDescription("Travaux pratiques créés depuis la configuration initiale");
                        coursSeed.add(coursTP);
                    }
                    
                    // Créer TD
                    Map<String, Integer> tdSpec = typesCoursSpec.get("TD");
                    if (tdSpec != null && tdSpec.get("dureeTotal") > 0) {
                        Cours coursTD = new Cours();
                        coursTD.setIntitule(ue.getIntitule() + " - TD");
                        coursTD.setTypeCours(TypeCours.TD);
                        coursTD.setDureeTotal(tdSpec.get("dureeTotal"));
                        coursTD.setDureeRestante(tdSpec.get("dureeTotal"));
                        coursTD.setDureeSeanceParJour(tdSpec.get("dureeSeanceParJour"));
                        coursTD.setUe(ue);
                        coursTD.setClasse(classe);
                        coursTD.setDescription("Travaux dirigés créés depuis la configuration initiale");
                        coursSeed.add(coursTD);
                    }
                }
            }
            
            coursRepo.saveAll(coursSeed);
            log.info("Cours créés : {} cours (CM + TP + TD)", coursSeed.size());
            
            // Comptage par type
            long cmCount = coursSeed.stream().filter(c -> c.getTypeCours() == TypeCours.CM).count();
            long tpCount = coursSeed.stream().filter(c -> c.getTypeCours() == TypeCours.TP).count();
            long tdCount = coursSeed.stream().filter(c -> c.getTypeCours() == TypeCours.TD).count();
            log.info("Détail : {} CM + {} TP + {} TD = {} cours total", cmCount, tpCount, tdCount, coursSeed.size());

            // ========== 8. CREATION DES ENSEIGNANTS ==========
            log.info("Creation des enseignants depuis la liste fournie...");

            List<Enseignant> enseignants = List.of(
                createEnseignant("KACFAH", "", "kacfah@example.com", "237600000001", "Docteur"),
                createEnseignant("PESSA", "Arthur", "arthur.pessa@example.com", "237600000002", "Chargé de cours"),
                createEnseignant("KENGNE", "Willy", "willy.kengne@example.com", "237600000003", "Docteur"),
                createEnseignant("TOUSSILE", "", "toussile@example.com", "237600000004", "Docteur"),
                createEnseignant("BATCHATO", "", "batchato@example.com", "237600000005", "Docteur"),
                createEnseignant("MELATEGUIA", "", "melateguia@example.com", "237600000006", "Professeur"),
                createEnseignant("NGWOBELA", "", "ngwobela@example.com", "237600000007", "Docteur"),
                createEnseignant("GEORGES", "Fr", "georges@example.com", "237600000008", "Chargé de cours"),
                createEnseignant("MOUPGOU", "", "moupgou@example.com", "237600000009", "Docteur"),
                createEnseignant("AKONO", "", "akono@example.com", "237600000010", "Chargé de cours"),
                createEnseignant("KOUETA", "", "koueta@example.com", "237600000011", "Docteur"),
                createEnseignant("MBATCHOU", "", "mbatchou@example.com", "237600000012", "Docteur"),
                createEnseignant("ABDOURAMAN", "", "abduraman@example.com", "237600000013", "Docteur"),
                createEnseignant("KOUAMOU", "", "kouamou@example.com", "237600000014", "Professeur"),
                createEnseignant("MONDO", "", "mondo@example.com", "237600000015", "Professeur"),
                createEnseignant("TAMKO", "", "tamko@example.com", "237600000016", "Docteur"),
                createEnseignant("ATCHA", "", "atcha@example.com", "237600000017", "Docteur"),
                createEnseignant("TOUOYEM", "", "touoyem@example.com", "237600000018", "Professeur"),
                createEnseignant("MOYOU", "", "moyou@example.com", "237600000019", "Docteur"),
                createEnseignant("NGOLO", "", "ngolo@example.com", "237600000020", "Docteur")
            );

            enseignantRepo.saveAll(enseignants);
            log.info("Enseignants crees : {}", enseignants.size());

            // ========== 8b. LIAISON ENSEIGNANTS-UES ==========
            log.info("Liaison des enseignants avec leurs UEs...");
            
            Map<String, Enseignant> enseignantsByNom = new HashMap<>();
            enseignants.forEach(e -> enseignantsByNom.put(e.getNom(), e));
            
            Map<String, UE> uesByCode = new HashMap<>();
            ueRepo.findAll().forEach(u -> uesByCode.put(u.getCode(), u));
            
            // Mapping enseignant -> codes UEs
            Map<String, List<String>> enseignantUEs = Map.ofEntries(
                Map.entry("KACFAH", List.of("ISI4167B")),
                Map.entry("PESSA", List.of("ING4167", "ISI4217", "ING4178")),
                Map.entry("KENGNE", List.of("ISI4187B")),
                Map.entry("TOUSSILE", List.of("ISI4207")),
                Map.entry("BATCHATO", List.of("ISI4197B")),
                Map.entry("MELATEGUIA", List.of("ISI4177")),
                Map.entry("NGWOBELA", List.of("TOEIC4")),
                Map.entry("GEORGES", List.of("SAGESSE4", "HUM4198")),
                Map.entry("MOUPGOU", List.of("ING4178", "ISI4237")),
                Map.entry("AKONO", List.of("ISI4197A")),
                Map.entry("KOUETA", List.of("ISI4198")),
                Map.entry("MBATCHOU", List.of("ECO4167")),
                Map.entry("ABDOURAMAN", List.of("ISI4188")),
                Map.entry("KOUAMOU", List.of("ISI4168")),
                Map.entry("MONDO", List.of("HUM4168")),
                Map.entry("TAMKO", List.of("ISI4228")),
                Map.entry("ATCHA", List.of("HUM4198B", "HUM4188")),
                Map.entry("TOUOYEM", List.of("IRS4168")),
                Map.entry("MOYOU", List.of("ISI4178")),
                Map.entry("NGOLO", List.of("ISI4198"))
            );
            
            enseignantUEs.forEach((nomEns, codeUEs) -> {
                Enseignant ens = enseignantsByNom.get(nomEns);
                if (ens != null) {
                    Set<UE> ues = new HashSet<>();
                    for (String code : codeUEs) {
                        UE ue = uesByCode.get(code);
                        if (ue != null) {
                            ues.add(ue);
                            log.debug("Liaison : {} -> {}", nomEns, code);
                        } else {
                            log.warn("UE non trouvée : {}", code);
                        }
                    }
                    if (!ues.isEmpty()) {
                        ens.setUesEnseignees(ues);
                        enseignantRepo.save(ens);
                        log.info("Enseignant {} lié à {} UE(s)", nomEns, ues.size());
                    }
                }
            });
            
            log.info("Liaisons enseignants-UEs terminées");

            // ========== 8c. ASSIGNATION DES ENSEIGNANTS AUX COURS ==========
            log.info("Assignation des enseignants aux cours de INGE4 ISI FR...");
            
            // Récupérer la classe INGE4 ISI FR directement
            Classe classeInge4IsiFr = classeRepo.findAll().stream()
                    .filter(c -> "INGE4 ISI FR".equals(c.getNom()))
                    .findFirst()
                    .orElse(null);
            
            if (classeInge4IsiFr == null) {
                log.warn("Classe INGE4 ISI FR non trouvée, assignation des enseignants ignorée");
            } else {
                // Créer une map UE ID -> Enseignant pour éviter le lazy loading
                Map<UUID, Enseignant> ueIdToEnseignant = new HashMap<>();
                for (Enseignant ens : enseignants) {
                    if (ens.getUesEnseignees() != null) {
                        for (UE ue : ens.getUesEnseignees()) {
                            ueIdToEnseignant.put(ue.getIdUE(), ens);
                        }
                    }
                }
                
                // Récupérer tous les cours de cette classe
                List<Cours> coursInge4IsiFr = coursRepo.findAll().stream()
                        .filter(c -> c.getClasse() != null && c.getClasse().getIdClasse().equals(classeInge4IsiFr.getIdClasse()))
                        .toList();
                
                int coursAssignes = 0;
                for (Cours cours : coursInge4IsiFr) {
                    if (cours.getUe() == null) continue;
                    
                    UUID ueId = cours.getUe().getIdUE();
                    
                    // Trouver l'enseignant via la map
                    Enseignant enseignantTrouve = ueIdToEnseignant.get(ueId);
                    
                    if (enseignantTrouve != null) {
                        cours.setEnseignant(enseignantTrouve);
                        coursRepo.save(cours);
                        coursAssignes++;
                        log.debug("Cours '{}' assigné à {}", cours.getIntitule(), enseignantTrouve.getNom());
                    } else {
                        log.warn("Aucun enseignant trouvé pour l'UE ID {} du cours '{}'", ueId, cours.getIntitule());
                    }
                }
                
                log.info("Assignation terminée : {} cours assignés sur {} cours de INGE4 ISI FR", 
                        coursAssignes, coursInge4IsiFr.size());
            }

            // ========== 9. CREATION DES DISPONIBILITES ENSEIGNANTS ==========
            log.info("Creation des disponibilites des enseignants...");

            LocalDate debutSemaine = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate finSemaine = debutSemaine.plusDays(6);
            
            java.util.Random random = new java.util.Random();
            List<java.time.DayOfWeek> tousLesJours = List.of(
                java.time.DayOfWeek.MONDAY, 
                java.time.DayOfWeek.TUESDAY, 
                java.time.DayOfWeek.WEDNESDAY, 
                java.time.DayOfWeek.THURSDAY, 
                java.time.DayOfWeek.FRIDAY
            );

            for (Enseignant ens : enseignants) {
                DisponibiliteEnseignant dispo = new DisponibiliteEnseignant();
                dispo.setEnseignant(ens);
                dispo.setDateDebut(debutSemaine);
                dispo.setDateFin(finSemaine);
                disponibiliteRepo.save(dispo);

                // Choisir aléatoirement entre 2 et 4 jours
                int nombreJours = 2 + random.nextInt(3); // 2, 3 ou 4 jours
                List<java.time.DayOfWeek> joursChoisis = new ArrayList<>();
                List<java.time.DayOfWeek> joursDisponibles = new ArrayList<>(tousLesJours);
                
                for (int j = 0; j < nombreJours; j++) {
                    int index = random.nextInt(joursDisponibles.size());
                    joursChoisis.add(joursDisponibles.remove(index));
                }
                
                log.debug("Enseignant {} : {} jours de disponibilité", ens.getNom(), nombreJours);

                for (java.time.DayOfWeek jour : joursChoisis) {
                    LocalDate date = debutSemaine.plusDays(jour.getValue() - 1);
                    CreneauDisponibilite creneau = new CreneauDisponibilite();
                    creneau.setDisponibilite(dispo);
                    creneau.setDate(date);
                    creneauDispoRepo.save(creneau);

                    // Choisir aléatoirement une plage de 4h (matin ou après-midi)
                    boolean matin = random.nextBoolean();
                    
                    PlageHoraire plage = new PlageHoraire();
                    plage.setCreneauDisponibilite(creneau);
                    
                    if (matin) {
                        // Plage du matin : 8h-12h
                        plage.setHeureDebut(LocalTime.of(8, 0));
                        plage.setHeureFin(LocalTime.of(12, 0));
                    } else {
                        // Plage de l'après-midi : 13h-17h
                        plage.setHeureDebut(LocalTime.of(13, 0));
                        plage.setHeureFin(LocalTime.of(17, 0));
                    }
                    
                    plageHoraireRepo.save(plage);
                }
            }

            log.info("Disponibilites enseignants creees pour semaine du {} au {}", debutSemaine, finSemaine);

            // ========== 9b. CATEGORIES ET TYPES D'EQUIPEMENT ==========
            log.info("Creation des categories et types d'equipement...");
            java.util.Map<String, CategorieEquipement> catByCode = new java.util.LinkedHashMap<>();
            String[][] catSeeds = {
                    {"AUDIOVISUEL", "Audiovisuel", "1"},
                    {"INFORMATIQUE", "Informatique", "2"},
                    {"MOBILIER", "Mobilier", "3"},
                    {"CONNECTIVITE", "Connectivité", "4"},
                    {"ECRITURE", "Écriture", "5"},
                    {"CONFORT", "Confort", "6"},
                    {"LABORATOIRE", "Laboratoire", "7"},
                    {"AUTRE", "Autre", "99"}
            };
            for (String[] row : catSeeds) {
                CategorieEquipement c = new CategorieEquipement();
                c.setCode(row[0]);
                c.setNom(row[1]);
                c.setOrdre(Integer.parseInt(row[2]));
                catByCode.put(row[0], categorieEquipementRepo.save(c));
            }
            TypeEquipement tVid = new TypeEquipement();
            tVid.setNom("Vidéoprojecteur");
            tVid.setCategorie(catByCode.get("AUDIOVISUEL"));
            tVid.setDescription("Projection");
            typeEquipementRepo.save(tVid);
            TypeEquipement tPc = new TypeEquipement();
            tPc.setNom("PC portable");
            tPc.setCategorie(catByCode.get("INFORMATIQUE"));
            typeEquipementRepo.save(tPc);

            // ========== 10. CREATION DE L'ADMINISTRATEUR ==========
            log.info("Creation de l'administrateur...");

            Administrateur admin = new Administrateur();
            admin.setNom("Admin");
            admin.setPrenom("Système");
            admin.setEmail("admin@test.com");
            admin.setPhone("658236952");
            admin.setMotDePasse(passwordEncoder.encode("password123"));
            admin.setRole(Role.ADMINISTRATEUR);
            adminRepo.save(admin);

            log.info("Administrateur cree");
            log.info("Fin du DataInitializer avec admin uniquement.");
        };
    }

    private Enseignant createEnseignant(String nom, String prenom, String email, String phone, String grade) {
        Enseignant enseignant = new Enseignant();
        enseignant.setNom(nom);
        enseignant.setPrenom(prenom);
        enseignant.setEmail(email.toLowerCase());
        enseignant.setPhone(phone);
        enseignant.setGrade(grade);
        enseignant.setMotDePasse(passwordEncoder.encode("password123"));
        enseignant.setRole(Role.ENSEIGNANT);
        return enseignant;
    }
}
