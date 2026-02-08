package com.iusjc.weschedule.config;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.enums.StatutCours;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
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
            log.info("=== INITIALISATION COMPLETE DE LA BASE DE DONNEES AVEC DONNEES FRONTEND ===");
            
            // VIDER COMPLETEMENT LA BASE DE DONNEES
            log.info("Suppression de toutes les donnees existantes...");
            plageHoraireRepo.deleteAll();
            creneauDispoRepo.deleteAll();
            disponibiliteRepo.deleteAll();
            passwordResetTokenRepo.deleteAll();
            etudiantRepo.deleteAll();
            enseignantRepo.deleteAll();
            adminRepo.deleteAll();
            utilisateurRepo.deleteAll();
            seanceClasseRepo.deleteAll();
            emploiDuTempsClasseRepo.deleteAll();
            coursRepo.deleteAll();
            salleRepo.deleteAll();
            ueRepo.deleteAll();
            classeRepo.deleteAll();
            groupeRepo.deleteAll();
            filiereRepo.deleteAll();
            ecoleRepo.deleteAll();
            log.info("Base de donnees videe avec succes !");

            // ========== 1. CREATION DES ECOLES (depuis frontend) ==========
            log.info("Creation des ecoles...");
            
            Ecole sji = new Ecole();
            sji.setNomEcole("Saint Jean Ingénieur");
            sji.setCode("SJI");
            sji.setCouleur("bg-blue-500");
            sji.setAdresse("Douala, Cameroun");
            sji.setTelephone("+237 233 42 15 67");
            sji.setEmail("contact@sji.edu.cm");
            ecoleRepo.save(sji);

            Ecole sjm = new Ecole();
            sjm.setNomEcole("Saint Jean Management");
            sjm.setCode("SJM");
            sjm.setCouleur("bg-green-500");
            sjm.setAdresse("Yaounde, Cameroun");
            sjm.setTelephone("+237 222 31 45 89");
            sjm.setEmail("contact@sjm.edu.cm");
            ecoleRepo.save(sjm);

            Ecole prepa = new Ecole();
            prepa.setNomEcole("PrepaVogt");
            prepa.setCode("PV");
            prepa.setCouleur("bg-purple-500");
            prepa.setAdresse("Douala, Cameroun");
            prepa.setTelephone("+237 233 42 15 68");
            prepa.setEmail("contact@prepavogt.edu.cm");
            ecoleRepo.save(prepa);

            Ecole cpge = new Ecole();
            cpge.setNomEcole("Classes Préparatoires");
            cpge.setCode("CPGE");
            cpge.setCouleur("bg-orange-500");
            cpge.setAdresse("Douala, Cameroun");
            cpge.setTelephone("+237 233 42 15 69");
            cpge.setEmail("contact@cpge.edu.cm");
            ecoleRepo.save(cpge);

            log.info("Ecoles creees : 4 ecoles");


            // ========== 2. CREATION DES FILIERES ==========
            log.info("Creation des filieres...");
            
            Filiere filiereInfo = new Filiere();
            filiereInfo.setNomFiliere("Informatique");
            filiereInfo.setDescription("Formation en développement logiciel et systèmes d'information");
            filiereRepo.save(filiereInfo);

            Filiere filiereGestion = new Filiere();
            filiereGestion.setNomFiliere("Gestion");
            filiereGestion.setDescription("Formation en management et gestion d'entreprise");
            filiereRepo.save(filiereGestion);

            Filiere filiereMarketing = new Filiere();
            filiereMarketing.setNomFiliere("Marketing");
            filiereMarketing.setDescription("Formation en marketing digital et communication");
            filiereRepo.save(filiereMarketing);

            Filiere filiereSciences = new Filiere();
            filiereSciences.setNomFiliere("Sciences");
            filiereSciences.setDescription("Formation scientifique préparatoire");
            filiereRepo.save(filiereSciences);

            log.info("Filieres creees : 4 filieres");

            // ========== 3. CREATION DES GROUPES ==========
            log.info("Creation des groupes...");
            
            Groupe groupeA = new Groupe();
            groupeA.setNomGroupe("Groupe A");
            groupeA.setEffectif(25);
            groupeRepo.save(groupeA);

            Groupe groupeB = new Groupe();
            groupeB.setNomGroupe("Groupe B");
            groupeB.setEffectif(25);
            groupeRepo.save(groupeB);

            log.info("Groupes crees : 2 groupes");


            // ========== 4. CREATION DES CLASSES (depuis frontend) ==========
            log.info("Creation des classes...");
            
            Classe infoL1A = new Classe();
            infoL1A.setNom("Informatique L1A");
            infoL1A.setNiveau("L1");
            infoL1A.setEcole(sji);
            infoL1A.setFiliere(filiereInfo);
            infoL1A.setSemestre(1);
            infoL1A.setEffectif(45);
            infoL1A.setEffectifMax(50);
            classeRepo.save(infoL1A);

            Classe infoL1B = new Classe();
            infoL1B.setNom("Informatique L1B");
            infoL1B.setNiveau("L1");
            infoL1B.setEcole(sji);
            infoL1B.setFiliere(filiereInfo);
            infoL1B.setSemestre(1);
            infoL1B.setEffectif(42);
            infoL1B.setEffectifMax(50);
            classeRepo.save(infoL1B);

            Classe infoL2 = new Classe();
            infoL2.setNom("Informatique L2");
            infoL2.setNiveau("L2");
            infoL2.setEcole(sji);
            infoL2.setFiliere(filiereInfo);
            infoL2.setSemestre(3);
            infoL2.setEffectif(38);
            infoL2.setEffectifMax(45);
            classeRepo.save(infoL2);

            Classe gestionL1 = new Classe();
            gestionL1.setNom("Gestion L1");
            gestionL1.setNiveau("L1");
            gestionL1.setEcole(sjm);
            gestionL1.setFiliere(filiereGestion);
            gestionL1.setSemestre(1);
            gestionL1.setEffectif(50);
            gestionL1.setEffectifMax(55);
            classeRepo.save(gestionL1);

            Classe marketingL2 = new Classe();
            marketingL2.setNom("Marketing L2");
            marketingL2.setNiveau("L2");
            marketingL2.setEcole(sjm);
            marketingL2.setFiliere(filiereMarketing);
            marketingL2.setSemestre(3);
            marketingL2.setEffectif(35);
            marketingL2.setEffectifMax(40);
            classeRepo.save(marketingL2);

            Classe prepaScientifique1A = new Classe();
            prepaScientifique1A.setNom("Prépa Scientifique 1A");
            prepaScientifique1A.setNiveau("Prépa");
            prepaScientifique1A.setEcole(prepa);
            prepaScientifique1A.setFiliere(filiereSciences);
            prepaScientifique1A.setSemestre(1);
            prepaScientifique1A.setEffectif(30);
            prepaScientifique1A.setEffectifMax(35);
            classeRepo.save(prepaScientifique1A);

            Classe mpsi = new Classe();
            mpsi.setNom("MPSI");
            mpsi.setNiveau("CPGE");
            mpsi.setEcole(cpge);
            mpsi.setFiliere(filiereSciences);
            mpsi.setSemestre(1);
            mpsi.setEffectif(35);
            mpsi.setEffectifMax(40);
            classeRepo.save(mpsi);

            log.info("Classes creees : 7 classes");


            // ========== 5. CREATION DES ENSEIGNANTS (depuis frontend) ==========
            log.info("Creation des enseignants...");
            
            Enseignant dupont = new Enseignant();
            dupont.setNom("Dupont");
            dupont.setPrenom("Martin");
            dupont.setEmail("martin.dupont@iu-saintjean.cm");
            dupont.setPhone("677123456");
            dupont.setMotDePasse(passwordEncoder.encode("password123"));
            dupont.setRole(Role.ENSEIGNANT);
            dupont.setGrade("Maître de conférences");
            Set<String> specialitesDupont = new HashSet<>();
            specialitesDupont.add("Informatique");
            specialitesDupont.add("Programmation");
            dupont.setSpecialites(specialitesDupont);
            Set<Ecole> ecolesDupont = new HashSet<>();
            ecolesDupont.add(sji);
            dupont.setEcoles(ecolesDupont);
            enseignantRepo.save(dupont);

            Enseignant laurent = new Enseignant();
            laurent.setNom("Laurent");
            laurent.setPrenom("Sophie");
            laurent.setEmail("sophie.laurent@iu-saintjean.cm");
            laurent.setPhone("677234567");
            laurent.setMotDePasse(passwordEncoder.encode("password123"));
            laurent.setRole(Role.ENSEIGNANT);
            laurent.setGrade("Professeur");
            Set<String> specialitesLaurent = new HashSet<>();
            specialitesLaurent.add("Mathématiques");
            specialitesLaurent.add("Algorithmique");
            laurent.setSpecialites(specialitesLaurent);
            Set<Ecole> ecolesLaurent = new HashSet<>();
            ecolesLaurent.add(sji);
            laurent.setEcoles(ecolesLaurent);
            enseignantRepo.save(laurent);

            Enseignant moreau = new Enseignant();
            moreau.setNom("Moreau");
            moreau.setPrenom("Jean");
            moreau.setEmail("jean.moreau@iu-saintjean.cm");
            moreau.setPhone("677345678");
            moreau.setMotDePasse(passwordEncoder.encode("password123"));
            moreau.setRole(Role.ENSEIGNANT);
            moreau.setGrade("Maître de conférences");
            Set<String> specialitesMoreau = new HashSet<>();
            specialitesMoreau.add("Gestion");
            specialitesMoreau.add("Management");
            moreau.setSpecialites(specialitesMoreau);
            Set<Ecole> ecolesMoreau = new HashSet<>();
            ecolesMoreau.add(sjm);
            moreau.setEcoles(ecolesMoreau);
            enseignantRepo.save(moreau);

            Enseignant dubois = new Enseignant();
            dubois.setNom("Dubois");
            dubois.setPrenom("Marie");
            dubois.setEmail("marie.dubois@iu-saintjean.cm");
            dubois.setPhone("677456789");
            dubois.setMotDePasse(passwordEncoder.encode("password123"));
            dubois.setRole(Role.ENSEIGNANT);
            dubois.setGrade("Professeur");
            Set<String> specialitesDubois = new HashSet<>();
            specialitesDubois.add("Marketing");
            specialitesDubois.add("Communication");
            dubois.setSpecialites(specialitesDubois);
            Set<Ecole> ecolesDubois = new HashSet<>();
            ecolesDubois.add(sjm);
            dubois.setEcoles(ecolesDubois);
            enseignantRepo.save(dubois);

            Enseignant bernard = new Enseignant();
            bernard.setNom("Bernard");
            bernard.setPrenom("Sophie");
            bernard.setEmail("sophie.bernard@iu-saintjean.cm");
            bernard.setPhone("677567890");
            bernard.setMotDePasse(passwordEncoder.encode("password123"));
            bernard.setRole(Role.ENSEIGNANT);
            bernard.setGrade("Professeur");
            Set<String> specialitesBernard = new HashSet<>();
            specialitesBernard.add("Mathématiques");
            bernard.setSpecialites(specialitesBernard);
            Set<Ecole> ecolesBernard = new HashSet<>();
            ecolesBernard.add(prepa);
            ecolesBernard.add(cpge);
            bernard.setEcoles(ecolesBernard);
            enseignantRepo.save(bernard);

            Enseignant leroy = new Enseignant();
            leroy.setNom("Leroy");
            leroy.setPrenom("Pierre");
            leroy.setEmail("pierre.leroy@iu-saintjean.cm");
            leroy.setPhone("677678901");
            leroy.setMotDePasse(passwordEncoder.encode("password123"));
            leroy.setRole(Role.ENSEIGNANT);
            leroy.setGrade("Maître de conférences");
            Set<String> specialitesLeroy = new HashSet<>();
            specialitesLeroy.add("Chimie");
            leroy.setSpecialites(specialitesLeroy);
            Set<Ecole> ecolesLeroy = new HashSet<>();
            ecolesLeroy.add(prepa);
            leroy.setEcoles(ecolesLeroy);
            enseignantRepo.save(leroy);

            log.info("Enseignants crees : 6 enseignants");


            // ========== 6. CREATION DES UEs (depuis frontend) ==========
            log.info("Creation des UEs...");
            
            UE inf101 = new UE();
            inf101.setCode("INF101");
            inf101.setIntitule("Introduction à la Programmation");
            inf101.setCredits(6);
            inf101.setSemestre(1);
            inf101.setEcole(sji);
            inf101.setDuree(60);
            inf101.setStatut(StatutUE.ACTIF);
            Set<Classe> classesInf101 = new HashSet<>();
            classesInf101.add(infoL1A);
            classesInf101.add(infoL1B);
            inf101.setClasses(classesInf101);
            ueRepo.save(inf101);

            UE mat101 = new UE();
            mat101.setCode("MAT101");
            mat101.setIntitule("Mathématiques Fondamentales");
            mat101.setCredits(6);
            mat101.setSemestre(1);
            mat101.setEcole(sji);
            mat101.setDuree(60);
            mat101.setStatut(StatutUE.ACTIF);
            Set<Classe> classesMat101 = new HashSet<>();
            classesMat101.add(infoL1A);
            classesMat101.add(infoL1B);
            mat101.setClasses(classesMat101);
            ueRepo.save(mat101);

            UE inf201 = new UE();
            inf201.setCode("INF201");
            inf201.setIntitule("Programmation Orientée Objet");
            inf201.setCredits(6);
            inf201.setSemestre(3);
            inf201.setEcole(sji);
            inf201.setDuree(60);
            inf201.setStatut(StatutUE.ACTIF);
            Set<Classe> classesInf201 = new HashSet<>();
            classesInf201.add(infoL2);
            inf201.setClasses(classesInf201);
            ueRepo.save(inf201);

            UE ges101 = new UE();
            ges101.setCode("GES101");
            ges101.setIntitule("Principes de Gestion");
            ges101.setCredits(4);
            ges101.setSemestre(1);
            ges101.setEcole(sjm);
            ges101.setDuree(50);
            ges101.setStatut(StatutUE.ACTIF);
            Set<Classe> classesGes101 = new HashSet<>();
            classesGes101.add(gestionL1);
            ges101.setClasses(classesGes101);
            ueRepo.save(ges101);

            UE mkt201 = new UE();
            mkt201.setCode("MKT201");
            mkt201.setIntitule("Marketing Digital");
            mkt201.setCredits(5);
            mkt201.setSemestre(3);
            mkt201.setEcole(sjm);
            mkt201.setDuree(55);
            mkt201.setStatut(StatutUE.ACTIF);
            Set<Classe> classesMkt201 = new HashSet<>();
            classesMkt201.add(marketingL2);
            mkt201.setClasses(classesMkt201);
            ueRepo.save(mkt201);

            UE mat201 = new UE();
            mat201.setCode("MAT201");
            mat201.setIntitule("Mathématiques Supérieures");
            mat201.setCredits(8);
            mat201.setSemestre(1);
            mat201.setEcole(prepa);
            mat201.setDuree(80);
            mat201.setStatut(StatutUE.ACTIF);
            Set<Classe> classesMat201 = new HashSet<>();
            classesMat201.add(prepaScientifique1A);
            classesMat201.add(mpsi);
            mat201.setClasses(classesMat201);
            ueRepo.save(mat201);

            UE phy101 = new UE();
            phy101.setCode("PHY101");
            phy101.setIntitule("Physique Générale");
            phy101.setCredits(6);
            phy101.setSemestre(1);
            phy101.setEcole(cpge);
            phy101.setDuree(60);
            phy101.setStatut(StatutUE.ACTIF);
            Set<Classe> classesPhy101 = new HashSet<>();
            classesPhy101.add(mpsi);
            phy101.setClasses(classesPhy101);
            ueRepo.save(phy101);

            UE chi101 = new UE();
            chi101.setCode("CHI101");
            chi101.setIntitule("Chimie Générale");
            chi101.setCredits(6);
            chi101.setSemestre(1);
            chi101.setEcole(prepa);
            chi101.setDuree(60);
            chi101.setStatut(StatutUE.ACTIF);
            Set<Classe> classesChi101 = new HashSet<>();
            classesChi101.add(prepaScientifique1A);
            chi101.setClasses(classesChi101);
            ueRepo.save(chi101);

            log.info("UEs creees : 8 UEs");


            // ========== 7. CREATION DES COURS (depuis frontend) ==========
            log.info("Creation des cours...");
            
            Cours cours1 = new Cours();
            cours1.setIntitule("CM - Introduction à la Programmation");
            cours1.setTypeCours(TypeCours.CM);
            cours1.setUe(inf101);
            cours1.setEnseignant(dupont);
            Set<Classe> classesCours1 = new HashSet<>();
            classesCours1.add(infoL1A);
            classesCours1.add(infoL1B);
            cours1.setClasses(classesCours1);
            cours1.setDuree(30);
            cours1.setDescription("Cours magistral d'introduction à la programmation");
            cours1.setStatut(StatutCours.ACTIF);
            coursRepo.save(cours1);

            Cours cours2 = new Cours();
            cours2.setIntitule("TD - Introduction à la Programmation");
            cours2.setTypeCours(TypeCours.TD);
            cours2.setUe(inf101);
            cours2.setEnseignant(dupont);
            Set<Classe> classesCours2 = new HashSet<>();
            classesCours2.add(infoL1A);
            cours2.setClasses(classesCours2);
            cours2.setDuree(20);
            cours2.setDescription("Travaux dirigés de programmation");
            cours2.setStatut(StatutCours.ACTIF);
            coursRepo.save(cours2);

            Cours cours3 = new Cours();
            cours3.setIntitule("TP - Introduction à la Programmation");
            cours3.setTypeCours(TypeCours.TP);
            cours3.setUe(inf101);
            cours3.setEnseignant(dupont);
            Set<Classe> classesCours3 = new HashSet<>();
            classesCours3.add(infoL1B);
            cours3.setClasses(classesCours3);
            cours3.setDuree(40);
            cours3.setDescription("Travaux pratiques de programmation");
            cours3.setStatut(StatutCours.ACTIF);
            coursRepo.save(cours3);

            Cours cours4 = new Cours();
            cours4.setIntitule("CM - Mathématiques Fondamentales");
            cours4.setTypeCours(TypeCours.CM);
            cours4.setUe(mat101);
            cours4.setEnseignant(laurent);
            Set<Classe> classesCours4 = new HashSet<>();
            classesCours4.add(infoL1A);
            classesCours4.add(infoL1B);
            cours4.setClasses(classesCours4);
            cours4.setDuree(30);
            cours4.setDescription("Cours magistral de mathématiques");
            cours4.setStatut(StatutCours.ACTIF);
            coursRepo.save(cours4);

            Cours cours5 = new Cours();
            cours5.setIntitule("CM - Principes de Gestion");
            cours5.setTypeCours(TypeCours.CM);
            cours5.setUe(ges101);
            cours5.setEnseignant(moreau);
            Set<Classe> classesCours5 = new HashSet<>();
            classesCours5.add(gestionL1);
            cours5.setClasses(classesCours5);
            cours5.setDuree(25);
            cours5.setDescription("Cours magistral de gestion");
            cours5.setStatut(StatutCours.ACTIF);
            coursRepo.save(cours5);

            Cours cours6 = new Cours();
            cours6.setIntitule("CM - Marketing Digital");
            cours6.setTypeCours(TypeCours.CM);
            cours6.setUe(mkt201);
            cours6.setEnseignant(moreau);
            Set<Classe> classesCours6 = new HashSet<>();
            classesCours6.add(marketingL2);
            cours6.setClasses(classesCours6);
            cours6.setDuree(30);
            cours6.setDescription("Cours magistral de marketing digital");
            cours6.setStatut(StatutCours.ACTIF);
            coursRepo.save(cours6);

            log.info("Cours crees : 6 cours");


            // ========== 8. CREATION DES SALLES ==========
            log.info("Creation des salles...");


            Salle salleCours1 = new Salle();
            salleCours1.setNomSalle("Salle de Cours 101");
            salleCours1.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            salleCours1.setCapacite(60);
            salleRepo.save(salleCours1);

            Salle salleCours2 = new Salle();
            salleCours2.setNomSalle("Salle de Cours 102");
            salleCours2.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            salleCours2.setCapacite(55);
            salleRepo.save(salleCours2);

            Salle salleTD1 = new Salle();
            salleTD1.setNomSalle("Salle TD 201");
            salleTD1.setTypeSalle(TypeSalle.SALLE_DE_TD);
            salleTD1.setCapacite(40);
            salleRepo.save(salleTD1);

            Salle salleTD2 = new Salle();
            salleTD2.setNomSalle("Salle TD 202");
            salleTD2.setTypeSalle(TypeSalle.SALLE_DE_TD);
            salleTD2.setCapacite(35);
            salleRepo.save(salleTD2);

            Salle salleInfo1 = new Salle();
            salleInfo1.setNomSalle("Salle Informatique 301");
            salleInfo1.setTypeSalle(TypeSalle.SALLE_INFORMATIQUE);
            salleInfo1.setCapacite(30);
            salleRepo.save(salleInfo1);

            Salle salleInfo2 = new Salle();
            salleInfo2.setNomSalle("Salle Informatique 302");
            salleInfo2.setTypeSalle(TypeSalle.SALLE_INFORMATIQUE);
            salleInfo2.setCapacite(25);
            salleRepo.save(salleInfo2);

            Salle labo1 = new Salle();
            labo1.setNomSalle("Laboratoire Chimie");
            labo1.setTypeSalle(TypeSalle.LABORATOIRE);
            labo1.setCapacite(20);
            salleRepo.save(labo1);

            Salle labo2 = new Salle();
            labo2.setNomSalle("Laboratoire Physique");
            labo2.setTypeSalle(TypeSalle.LABORATOIRE);
            labo2.setCapacite(20);
            salleRepo.save(labo2);

            log.info("Salles creees : 10 salles");

            // ========== 9. CREATION DE L'ADMINISTRATEUR ==========
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

            // ========== 10. CREATION DES ETUDIANTS ==========
            log.info("Creation des etudiants...");
            
            Etudiant etudiant1 = new Etudiant();
            etudiant1.setNom("Dupont");
            etudiant1.setPrenom("Jean");
            etudiant1.setEmail("jean.dupont@student.com");
            etudiant1.setPhone("677123456");
            etudiant1.setMotDePasse(passwordEncoder.encode("password123"));
            etudiant1.setRole(Role.ETUDIANT);
            etudiant1.setClasse(infoL1A);
            etudiant1.setGroupe(groupeA);
            etudiantRepo.save(etudiant1);

            Etudiant etudiant2 = new Etudiant();
            etudiant2.setNom("Martin");
            etudiant2.setPrenom("Marie");
            etudiant2.setEmail("marie.martin@student.com");
            etudiant2.setPhone("677654321");
            etudiant2.setMotDePasse(passwordEncoder.encode("password123"));
            etudiant2.setRole(Role.ETUDIANT);
            etudiant2.setClasse(gestionL1);
            etudiant2.setGroupe(groupeB);
            etudiantRepo.save(etudiant2);

            log.info("Etudiants crees : 2 etudiants");


            // ========== 11. CREATION DES DISPONIBILITES ==========
            log.info("Creation des disponibilites pour les enseignants...");
            
            LocalDate debutSemaine = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate finSemaine = debutSemaine.plusDays(6);

            // Disponibilités pour Dupont
            DisponibiliteEnseignant dispoDupont = new DisponibiliteEnseignant();
            dispoDupont.setEnseignant(dupont);
            dispoDupont.setDateDebut(debutSemaine);
            dispoDupont.setDateFin(finSemaine);
            disponibiliteRepo.save(dispoDupont);

            for (int i = 0; i < 5; i++) {
                LocalDate jour = debutSemaine.plusDays(i);
                
                CreneauDisponibilite creneau = new CreneauDisponibilite();
                creneau.setDisponibilite(dispoDupont);
                creneau.setDate(jour);
                creneauDispoRepo.save(creneau);

                PlageHoraire plage1 = new PlageHoraire();
                plage1.setCreneauDisponibilite(creneau);
                plage1.setHeureDebut(LocalTime.of(8, 0));
                plage1.setHeureFin(LocalTime.of(10, 0));
                plageHoraireRepo.save(plage1);

                PlageHoraire plage2 = new PlageHoraire();
                plage2.setCreneauDisponibilite(creneau);
                plage2.setHeureDebut(LocalTime.of(10, 15));
                plage2.setHeureFin(LocalTime.of(12, 0));
                plageHoraireRepo.save(plage2);

                PlageHoraire plage3 = new PlageHoraire();
                plage3.setCreneauDisponibilite(creneau);
                plage3.setHeureDebut(LocalTime.of(14, 0));
                plage3.setHeureFin(LocalTime.of(16, 0));
                plageHoraireRepo.save(plage3);

                PlageHoraire plage4 = new PlageHoraire();
                plage4.setCreneauDisponibilite(creneau);
                plage4.setHeureDebut(LocalTime.of(16, 15));
                plage4.setHeureFin(LocalTime.of(18, 0));
                plageHoraireRepo.save(plage4);
            }

            log.info("Disponibilites creees pour la semaine du {} au {}", debutSemaine, finSemaine);

            // ========== RESUME ==========
            log.info("");
            log.info("=== BASE DE DONNEES INITIALISEE AVEC SUCCES ===");
            log.info("Ecoles: 4 | Filieres: 4 | Classes: 7 | Groupes: 2");
            log.info("Enseignants: 6 | Etudiants: 2 | Administrateurs: 1");
            log.info("UEs: 8 | Cours: 6 | Salles: 10");
            log.info("Disponibilites: Semaine complete avec creneaux detailles");
            log.info("");
            log.info("=== IDENTIFIANTS DE CONNEXION ===");
            log.info("Admin: admin@test.com / password123");
            log.info("Enseignant 1: martin.dupont@iu-saintjean.cm / password123");
            log.info("Enseignant 2: sophie.laurent@iu-saintjean.cm / password123");
            log.info("Enseignant 3: jean.moreau@iu-saintjean.cm / password123");
            log.info("Enseignant 4: marie.dubois@iu-saintjean.cm / password123");
            log.info("Enseignant 5: sophie.bernard@iu-saintjean.cm / password123");
            log.info("Enseignant 6: pierre.leroy@iu-saintjean.cm / password123");
            log.info("Etudiant 1: jean.dupont@student.com / password123");
            log.info("Etudiant 2: marie.martin@student.com / password123");
            log.info("===========================================");
        };
    }
}
