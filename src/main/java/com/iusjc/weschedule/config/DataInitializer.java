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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
            SeanceClasseRepository seanceClasseRepo,
            EmploiDuTempsClasseRepository emploiDuTempsClasseRepo) {
        return args -> {
            log.info("=== INITIALISATION COMPLETE DE LA BASE DE DONNEES ===");
            
            // VIDER COMPLETEMENT LA BASE DE DONNEES
            log.info("Suppression de toutes les donnees existantes...");
            
            // Supprimer dans l'ordre inverse des dépendances
            log.info("Suppression des disponibilites...");
            plageHoraireRepo.deleteAll();
            creneauDispoRepo.deleteAll();
            disponibiliteRepo.deleteAll();
            
            log.info("Suppression des tokens de reset de mot de passe...");
            passwordResetTokenRepo.deleteAll();
            
            log.info("Suppression des utilisateurs...");
            etudiantRepo.deleteAll();
            enseignantRepo.deleteAll();
            adminRepo.deleteAll();
            utilisateurRepo.deleteAll();
            
            log.info("Suppression des cours et salles...");
            coursRepo.deleteAll();
            salleRepo.deleteAll();
            
            log.info("Suppression des UE...");
            ueRepo.deleteAll();
            
            log.info("Suppression des emplois du temps et seances...");
            seanceClasseRepo.deleteAll();
            emploiDuTempsClasseRepo.deleteAll();
            
            log.info("Suppression des classes...");
            classeRepo.deleteAll();
            
            log.info("Suppression des groupes...");
            groupeRepo.deleteAll();
            
            log.info("Suppression des filieres...");
            filiereRepo.deleteAll();
            
            log.info("Suppression des ecoles...");
            ecoleRepo.deleteAll();
            
            log.info("Base de donnees videe avec succes !");
            log.info("");

            // 1. Creer les ecoles
            Ecole ecole1 = new Ecole();
            ecole1.setNomEcole("Saint Jean Ingenieur");
            ecole1.setAdresse("Douala, Cameroun");
            ecole1.setTelephone("+237 233 42 15 67");
            ecole1.setEmail("contact@iusjc.edu.cm");
            ecoleRepo.save(ecole1);

            Ecole ecole2 = new Ecole();
            ecole2.setNomEcole("Saint Jean Management");
            ecole2.setAdresse("Yaounde, Cameroun");
            ecole2.setTelephone("+237 222 31 45 89");
            ecole2.setEmail("info@est.edu.cm");
            ecoleRepo.save(ecole2);

            log.info("Ecoles creees");

            // 2. Creer les filieres
            Filiere filiereInfo = new Filiere();
            filiereInfo.setNomFiliere("Informatique");
            filiereInfo.setDescription("Formation en developpement logiciel et systemes d'information");
            filiereRepo.save(filiereInfo);

            Filiere filiereGestion = new Filiere();
            filiereGestion.setNomFiliere("Gestion");
            filiereGestion.setDescription("Formation en management et gestion d'entreprise");
            filiereRepo.save(filiereGestion);

            Filiere filiereMarketing = new Filiere();
            filiereMarketing.setNomFiliere("Marketing");
            filiereMarketing.setDescription("Formation en marketing digital et communication");
            filiereRepo.save(filiereMarketing);

            log.info("Filieres creees");

            // 3. Creer les groupes
            Groupe groupeA = new Groupe();
            groupeA.setNomGroupe("Groupe A");
            groupeA.setEffectif(25);
            groupeRepo.save(groupeA);

            Groupe groupeB = new Groupe();
            groupeB.setNomGroupe("Groupe B");
            groupeB.setEffectif(30);
            groupeRepo.save(groupeB);

            log.info("Groupes crees");

            // 4. Creer les classes
            Classe classeL1Info = new Classe();
            classeL1Info.setNom("L1 Informatique");
            classeL1Info.setEcole(ecole1);
            classeL1Info.setFiliere(filiereInfo);
            classeL1Info.setNiveau("L1");
            classeL1Info.setEffectif(45);
            classeRepo.save(classeL1Info);

            Classe classeL2Info = new Classe();
            classeL2Info.setNom("L2 Informatique");
            classeL2Info.setEcole(ecole1);
            classeL2Info.setFiliere(filiereInfo);
            classeL2Info.setNiveau("L2");
            classeL2Info.setEffectif(38);
            classeRepo.save(classeL2Info);

            Classe classeL1Gestion = new Classe();
            classeL1Gestion.setNom("L1 Gestion");
            classeL1Gestion.setEcole(ecole1);
            classeL1Gestion.setFiliere(filiereGestion);
            classeL1Gestion.setNiveau("L1");
            classeL1Gestion.setEffectif(52);
            classeRepo.save(classeL1Gestion);

            log.info("Classes creees");

            // 5. Créer les salles
            Salle salleCours1 = new Salle();
            salleCours1.setNomSalle("Salle de Cours A");
            salleCours1.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            salleCours1.setCapacite(60);
            salleRepo.save(salleCours1);

            Salle salleCours2 = new Salle();
            salleCours2.setNomSalle("Salle de Cours B");
            salleCours2.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            salleCours2.setCapacite(55);
            salleRepo.save(salleCours2);

            Salle salleTD1 = new Salle();
            salleTD1.setNomSalle("Salle TD 101");
            salleTD1.setTypeSalle(TypeSalle.SALLE_DE_TD);
            salleTD1.setCapacite(40);
            salleRepo.save(salleTD1);

            Salle salleTD2 = new Salle();
            salleTD2.setNomSalle("Salle TD 102");
            salleTD2.setTypeSalle(TypeSalle.SALLE_DE_TD);
            salleTD2.setCapacite(35);
            salleRepo.save(salleTD2);

            Salle salleInfo1 = new Salle();
            salleInfo1.setNomSalle("Salle Informatique 1");
            salleInfo1.setTypeSalle(TypeSalle.SALLE_INFORMATIQUE);
            salleInfo1.setCapacite(30);
            salleRepo.save(salleInfo1);

            Salle salleInfo2 = new Salle();
            salleInfo2.setNomSalle("Salle Informatique 2");
            salleInfo2.setTypeSalle(TypeSalle.SALLE_INFORMATIQUE);
            salleInfo2.setCapacite(25);
            salleRepo.save(salleInfo2);

            Salle labo1 = new Salle();
            labo1.setNomSalle("Laboratoire Chimie");
            labo1.setTypeSalle(TypeSalle.LABORATOIRE);
            labo1.setCapacite(20);
            salleRepo.save(labo1);

            log.info("Salles creees : 7 salles");

            // 6. Supprimer les anciennes UE et en créer de nouvelles avec semestre et statut
            log.info("Suppression des anciennes UE...");
            ueRepo.deleteAll();
            
            // UE pour L1 Informatique
            UE ueAlgo = new UE();
            ueAlgo.setIntitule("Algorithmique et Programmation");
            ueAlgo.setCode("INF101");
            ueAlgo.setDuree(60);
            ueAlgo.setSemestre(1);
            ueAlgo.setStatut(StatutUE.ACTIF);
            Set<Classe> classesAlgo = new HashSet<>();
            classesAlgo.add(classeL1Info);
            ueAlgo.setClasses(classesAlgo);
            ueRepo.save(ueAlgo);

            UE ueMaths = new UE();
            ueMaths.setIntitule("Mathematiques pour l'Informatique");
            ueMaths.setCode("MAT101");
            ueMaths.setDuree(50);
            ueMaths.setSemestre(1);
            ueMaths.setStatut(StatutUE.ACTIF);
            Set<Classe> classesMaths = new HashSet<>();
            classesMaths.add(classeL1Info);
            ueMaths.setClasses(classesMaths);
            ueRepo.save(ueMaths);
            
            UE ueArchitecture = new UE();
            ueArchitecture.setIntitule("Architecture des Ordinateurs");
            ueArchitecture.setCode("INF102");
            ueArchitecture.setDuree(45);
            ueArchitecture.setSemestre(1);
            ueArchitecture.setStatut(StatutUE.ACTIF);
            Set<Classe> classesArchi = new HashSet<>();
            classesArchi.add(classeL1Info);
            ueArchitecture.setClasses(classesArchi);
            ueRepo.save(ueArchitecture);

            // UE pour L2 Informatique
            UE ueBDD = new UE();
            ueBDD.setIntitule("Base de Donnees");
            ueBDD.setCode("INF201");
            ueBDD.setDuree(60);
            ueBDD.setSemestre(1);
            ueBDD.setStatut(StatutUE.ACTIF);
            Set<Classe> classesBDD = new HashSet<>();
            classesBDD.add(classeL2Info);
            ueBDD.setClasses(classesBDD);
            ueRepo.save(ueBDD);
            
            UE uePOO = new UE();
            uePOO.setIntitule("Programmation Orientee Objet");
            uePOO.setCode("INF202");
            uePOO.setDuree(60);
            uePOO.setSemestre(1);
            uePOO.setStatut(StatutUE.ACTIF);
            Set<Classe> classesPOO = new HashSet<>();
            classesPOO.add(classeL2Info);
            uePOO.setClasses(classesPOO);
            ueRepo.save(uePOO);
            
            UE ueReseaux = new UE();
            ueReseaux.setIntitule("Reseaux et Telecommunications");
            ueReseaux.setCode("INF203");
            ueReseaux.setDuree(45);
            ueReseaux.setSemestre(2);
            ueReseaux.setStatut(StatutUE.ACTIF);
            Set<Classe> classesReseaux = new HashSet<>();
            classesReseaux.add(classeL2Info);
            ueReseaux.setClasses(classesReseaux);
            ueRepo.save(ueReseaux);

            // UE pour L1 Gestion
            UE ueCompta = new UE();
            ueCompta.setIntitule("Comptabilite Generale");
            ueCompta.setCode("GES101");
            ueCompta.setDuree(45);
            ueCompta.setSemestre(1);
            ueCompta.setStatut(StatutUE.ACTIF);
            Set<Classe> classesCompta = new HashSet<>();
            classesCompta.add(classeL1Gestion);
            ueCompta.setClasses(classesCompta);
            ueRepo.save(ueCompta);
            
            UE ueEconomie = new UE();
            ueEconomie.setIntitule("Economie d'Entreprise");
            ueEconomie.setCode("GES102");
            ueEconomie.setDuree(40);
            ueEconomie.setSemestre(1);
            ueEconomie.setStatut(StatutUE.ACTIF);
            Set<Classe> classesEco = new HashSet<>();
            classesEco.add(classeL1Gestion);
            ueEconomie.setClasses(classesEco);
            ueRepo.save(ueEconomie);
            
            UE ueManagement = new UE();
            ueManagement.setIntitule("Management des Organisations");
            ueManagement.setCode("GES103");
            ueManagement.setDuree(45);
            ueManagement.setSemestre(2);
            ueManagement.setStatut(StatutUE.ACTIF);
            Set<Classe> classesMgt = new HashSet<>();
            classesMgt.add(classeL1Gestion);
            ueManagement.setClasses(classesMgt);
            ueRepo.save(ueManagement);

            log.info("UE creees : 9 unites d'enseignement avec semestre et statut");

            // 7. Créer les cours
            Cours coursAlgoCM = new Cours();
            coursAlgoCM.setIntitule("Cours Magistral - Algorithmique");
            coursAlgoCM.setTypeCours(TypeCours.CM);
            coursAlgoCM.setUe(ueAlgo);
            coursAlgoCM.setDuree(45); // 45h restantes sur 60h total
            coursRepo.save(coursAlgoCM);

            Cours coursAlgoTD = new Cours();
            coursAlgoTD.setIntitule("TD - Algorithmique");
            coursAlgoTD.setTypeCours(TypeCours.TD);
            coursAlgoTD.setUe(ueAlgo);
            coursAlgoTD.setDuree(15); // 15h restantes
            coursRepo.save(coursAlgoTD);

            Cours coursMathsCM = new Cours();
            coursMathsCM.setIntitule("Cours Magistral - Mathematiques");
            coursMathsCM.setTypeCours(TypeCours.CM);
            coursMathsCM.setUe(ueMaths);
            coursMathsCM.setDuree(50); // Toutes les heures restantes
            coursRepo.save(coursMathsCM);

            Cours coursBDDCM = new Cours();
            coursBDDCM.setIntitule("Cours Magistral - Base de Donnees");
            coursBDDCM.setTypeCours(TypeCours.CM);
            coursBDDCM.setUe(ueBDD);
            coursBDDCM.setDuree(40); // 40h restantes sur 60h
            coursRepo.save(coursBDDCM);

            Cours coursBDDTP = new Cours();
            coursBDDTP.setIntitule("TP - Base de Donnees");
            coursBDDTP.setTypeCours(TypeCours.TP);
            coursBDDTP.setUe(ueBDD);
            coursBDDTP.setDuree(20); // 20h restantes
            coursRepo.save(coursBDDTP);

            log.info("Cours crees : 5 cours avec heures restantes");

            // 8. Creer l'administrateur
            Administrateur admin = new Administrateur();
            admin.setNom("Admin");
            admin.setPrenom("Test");
            admin.setEmail("admin@test.com");
            admin.setPhone("658236952");
            admin.setMotDePasse(passwordEncoder.encode("password123"));
            admin.setRole(Role.ADMINISTRATEUR);
            adminRepo.save(admin);

            // 7. Creer l'enseignant avec l'email specifie
            Enseignant enseignant = new Enseignant();
            enseignant.setNom("PESSA");
            enseignant.setPrenom("Arthur");
            enseignant.setEmail("goodskrt2.0@gmail.com");
            enseignant.setPhone("673807864");
            enseignant.setMotDePasse(passwordEncoder.encode("password123"));
            enseignant.setRole(Role.ENSEIGNANT);
            enseignant.setGrade("Maitre de conferences");
            
            // Associer les UE que l'enseignant peut enseigner
            Set<UE> uesEnseignees = new HashSet<>();
            uesEnseignees.add(ueAlgo);
            uesEnseignees.add(ueMaths);
            enseignant.setUesEnseignees(uesEnseignees);
            enseignantRepo.save(enseignant);

            log.info("Enseignant cree : goodskrt2.0@gmail.com");

            // 8. Creer quelques etudiants
            Etudiant etudiant1 = new Etudiant();
            etudiant1.setNom("Dupont");
            etudiant1.setPrenom("Jean");
            etudiant1.setEmail("jean.dupont@student.com");
            etudiant1.setPhone("677123456");
            etudiant1.setMotDePasse(passwordEncoder.encode("password123"));
            etudiant1.setRole(Role.ETUDIANT);
            etudiant1.setClasse(classeL1Info);
            etudiant1.setGroupe(groupeA);
            etudiantRepo.save(etudiant1);

            Etudiant etudiant2 = new Etudiant();
            etudiant2.setNom("Martin");
            etudiant2.setPrenom("Marie");
            etudiant2.setEmail("marie.martin@student.com");
            etudiant2.setPhone("677654321");
            etudiant2.setMotDePasse(passwordEncoder.encode("password123"));
            etudiant2.setRole(Role.ETUDIANT);
            etudiant2.setClasse(classeL1Gestion);
            etudiant2.setGroupe(groupeB);
            etudiantRepo.save(etudiant2);

            log.info("Etudiants crees");

            // 9. Creer les disponibilites pour cette semaine
            LocalDate debutSemaine = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate finSemaine = debutSemaine.plusDays(6);

            DisponibiliteEnseignant disponibilite = new DisponibiliteEnseignant();
            disponibilite.setEnseignant(enseignant);
            disponibilite.setDateDebut(debutSemaine);
            disponibilite.setDateFin(finSemaine);
            disponibiliteRepo.save(disponibilite);

            // Creer les creneaux pour chaque jour de la semaine (Lundi a Vendredi)
            for (int i = 0; i < 5; i++) {
                LocalDate jour = debutSemaine.plusDays(i);
                
                CreneauDisponibilite creneau = new CreneauDisponibilite();
                creneau.setDisponibilite(disponibilite);
                creneau.setDate(jour);
                creneauDispoRepo.save(creneau);

                // Creer les plages horaires pour chaque jour
                // Matin : 8h-12h
                PlageHoraire plageMatinDebut = new PlageHoraire();
                plageMatinDebut.setCreneauDisponibilite(creneau);
                plageMatinDebut.setHeureDebut(LocalTime.of(8, 0));
                plageMatinDebut.setHeureFin(LocalTime.of(10, 0));
                plageHoraireRepo.save(plageMatinDebut);

                PlageHoraire plageMatinFin = new PlageHoraire();
                plageMatinFin.setCreneauDisponibilite(creneau);
                plageMatinFin.setHeureDebut(LocalTime.of(10, 15));
                plageMatinFin.setHeureFin(LocalTime.of(12, 0));
                plageHoraireRepo.save(plageMatinFin);

                // Apres-midi : 14h-18h
                PlageHoraire plageApresMidiDebut = new PlageHoraire();
                plageApresMidiDebut.setCreneauDisponibilite(creneau);
                plageApresMidiDebut.setHeureDebut(LocalTime.of(14, 0));
                plageApresMidiDebut.setHeureFin(LocalTime.of(16, 0));
                plageHoraireRepo.save(plageApresMidiDebut);

                PlageHoraire plageApresMidiFin = new PlageHoraire();
                plageApresMidiFin.setCreneauDisponibilite(creneau);
                plageApresMidiFin.setHeureDebut(LocalTime.of(16, 15));
                plageApresMidiFin.setHeureFin(LocalTime.of(18, 0));
                plageHoraireRepo.save(plageApresMidiFin);
            }

            log.info("Disponibilites creees pour la semaine du {} au {}", debutSemaine, finSemaine);

            log.info("BASE DE DONNEES INITIALISEE AVEC SUCCES");
            log.info("Admin: admin@test.com / password123");
            log.info("Enseignant: goodskrt2.0@gmail.com / password123");
            log.info("Etudiant 1: jean.dupont@student.com / password123");
            log.info("Etudiant 2: marie.martin@student.com / password123");
            log.info("Ecoles: 2 | Filieres: 3 | Classes: 3 | UE: 9 | Cours: 5 | Salles: 7");
            log.info("Disponibilites: Semaine complete avec creneaux detailles");
        };
    }
}