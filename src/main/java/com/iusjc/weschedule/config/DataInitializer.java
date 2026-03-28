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
            EmploiDuTempsClasseRepository emploiDuTempsClasseRepo,
            EquipmentRepository equipmentRepo,
            EquipmentAssignmentRepository equipmentAssignmentRepo) {
        return args -> {
            log.info("=== INITIALISATION COMPLETE DE LA BASE DE DONNEES AVEC DONNEES FRONTEND ===");

            // VIDER COMPLETEMENT LA BASE DE DONNEES DANS LE BON ORDRE
            log.info("Suppression de toutes les donnees existantes...");

            // 1. Supprimer d'abord les dépendances les plus faibles (sans FK vers d'autres tables)
            equipmentAssignmentRepo.deleteAll();
            equipmentRepo.deleteAll();
            plageHoraireRepo.deleteAll();
            creneauDispoRepo.deleteAll();
            disponibiliteRepo.deleteAll();
            passwordResetTokenRepo.deleteAll();

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
            filiereInfo.setEcole(sji);
            filiereInfo.setNiveaux(java.util.List.of("Niveau 1","Niveau 2","Niveau 3","Niveau 4","Niveau 5"));
            filiereRepo.save(filiereInfo);

            Filiere filiereGestion = new Filiere();
            filiereGestion.setNomFiliere("Gestion");
            filiereGestion.setDescription("Formation en management et gestion d'entreprise");
            filiereGestion.setEcole(sjm);
            filiereGestion.setNiveaux(java.util.List.of("Niveau 1","Niveau 2","Niveau 3"));
            filiereRepo.save(filiereGestion);

            Filiere filiereMarketing = new Filiere();
            filiereMarketing.setNomFiliere("Marketing");
            filiereMarketing.setDescription("Formation en marketing digital et communication");
            filiereMarketing.setEcole(sjm);
            filiereMarketing.setNiveaux(java.util.List.of("Niveau 1","Niveau 2","Niveau 3"));
            filiereRepo.save(filiereMarketing);

            Filiere filiereSciences = new Filiere();
            filiereSciences.setNomFiliere("Sciences");
            filiereSciences.setDescription("Formation scientifique préparatoire");
            filiereSciences.setEcole(prepa);
            filiereSciences.setNiveaux(java.util.List.of("Niveau 1","Niveau 2"));
            filiereRepo.save(filiereSciences);

            Filiere filiereScencesCpge = new Filiere();
            filiereScencesCpge.setNomFiliere("Sciences CPGE");
            filiereScencesCpge.setDescription("Classes préparatoires scientifiques");
            filiereScencesCpge.setEcole(cpge);
            filiereScencesCpge.setNiveaux(java.util.List.of("Niveau 1","Niveau 2","Niveau 3"));
            filiereRepo.save(filiereScencesCpge);

            log.info("Filieres creees : 5 filieres");

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
            infoL1A.setNiveau("Niveau 1");
            infoL1A.setEcole(sji);
            infoL1A.setFiliere(filiereInfo);
            infoL1A.setEffectif(45);
            infoL1A.setLangue("Francophone");
            classeRepo.save(infoL1A);

            Classe infoL1B = new Classe();
            infoL1B.setNom("Informatique L1B");
            infoL1B.setNiveau("Niveau 1");
            infoL1B.setEcole(sji);
            infoL1B.setFiliere(filiereInfo);
            infoL1B.setEffectif(42);
            infoL1B.setLangue("Anglophone");
            classeRepo.save(infoL1B);

            Classe infoL2 = new Classe();
            infoL2.setNom("Informatique L2");
            infoL2.setNiveau("Niveau 2");
            infoL2.setEcole(sji);
            infoL2.setFiliere(filiereInfo);
            infoL2.setEffectif(38);
            infoL2.setLangue("Francophone");
            classeRepo.save(infoL2);

            Classe gestionL1 = new Classe();
            gestionL1.setNom("Gestion L1");
            gestionL1.setNiveau("Niveau 1");
            gestionL1.setEcole(sjm);
            gestionL1.setFiliere(filiereGestion);
            gestionL1.setEffectif(50);
            gestionL1.setLangue("Francophone");
            classeRepo.save(gestionL1);

            Classe marketingL2 = new Classe();
            marketingL2.setNom("Marketing L2");
            marketingL2.setNiveau("Niveau 2");
            marketingL2.setEcole(sjm);
            marketingL2.setFiliere(filiereMarketing);
            marketingL2.setEffectif(35);
            marketingL2.setLangue("Anglophone");
            classeRepo.save(marketingL2);

            Classe prepaScientifique1A = new Classe();
            prepaScientifique1A.setNom("Prépa Scientifique 1A");
            prepaScientifique1A.setNiveau("Niveau 1");
            prepaScientifique1A.setEcole(prepa);
            prepaScientifique1A.setFiliere(filiereSciences);
            prepaScientifique1A.setEffectif(30);
            prepaScientifique1A.setLangue("Francophone");
            classeRepo.save(prepaScientifique1A);

            Classe mpsi = new Classe();
            mpsi.setNom("MPSI");
            mpsi.setNiveau("Niveau 3");
            mpsi.setEcole(cpge);
            mpsi.setFiliere(filiereSciences);
            mpsi.setEffectif(35);
            mpsi.setLangue("Francophone");
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

            // NOUVEAUX ENSEIGNANTS POUR INFORMATIQUE L1A
            Enseignant garcia = new Enseignant();
            garcia.setNom("Garcia");
            garcia.setPrenom("Carlos");
            garcia.setEmail("carlos.garcia@iu-saintjean.cm");
            garcia.setPhone("677789012");
            garcia.setMotDePasse(passwordEncoder.encode("password123"));
            garcia.setRole(Role.ENSEIGNANT);
            garcia.setGrade("Professeur");
            Set<String> specialitesGarcia = new HashSet<>();
            specialitesGarcia.add("Bases de données");
            specialitesGarcia.add("SQL");
            garcia.setSpecialites(specialitesGarcia);
            Set<Ecole> ecolesGarcia = new HashSet<>();
            ecolesGarcia.add(sji);
            garcia.setEcoles(ecolesGarcia);
            enseignantRepo.save(garcia);

            Enseignant rousseau = new Enseignant();
            rousseau.setNom("Rousseau");
            rousseau.setPrenom("Isabelle");
            rousseau.setEmail("isabelle.rousseau@iu-saintjean.cm");
            rousseau.setPhone("677890123");
            rousseau.setMotDePasse(passwordEncoder.encode("password123"));
            rousseau.setRole(Role.ENSEIGNANT);
            rousseau.setGrade("Maître de conférences");
            Set<String> specialitesRousseau = new HashSet<>();
            specialitesRousseau.add("Réseaux");
            specialitesRousseau.add("Systèmes");
            rousseau.setSpecialites(specialitesRousseau);
            Set<Ecole> ecolesRousseau = new HashSet<>();
            ecolesRousseau.add(sji);
            rousseau.setEcoles(ecolesRousseau);
            enseignantRepo.save(rousseau);

            Enseignant petit = new Enseignant();
            petit.setNom("Petit");
            petit.setPrenom("Thomas");
            petit.setEmail("thomas.petit@iu-saintjean.cm");
            petit.setPhone("677901234");
            petit.setMotDePasse(passwordEncoder.encode("password123"));
            petit.setRole(Role.ENSEIGNANT);
            petit.setGrade("Professeur");
            Set<String> specialitesPetit = new HashSet<>();
            specialitesPetit.add("Web");
            specialitesPetit.add("JavaScript");
            petit.setSpecialites(specialitesPetit);
            Set<Ecole> ecolesPetit = new HashSet<>();
            ecolesPetit.add(sji);
            petit.setEcoles(ecolesPetit);
            enseignantRepo.save(petit);

            log.info("Enseignants crees : 9 enseignants");

            // ========== 6. CREATION DES UEs (depuis frontend) ==========
            log.info("Creation des UEs...");

            UE inf101 = new UE();
            inf101.setCode("INF101");
            inf101.setIntitule("Introduction à la Programmation");
            inf101.setCredits(6);
            inf101.setSemestre(1);
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
            inf201.setSemestre(2);
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
            mkt201.setSemestre(2);
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
            chi101.setDuree(60);
            chi101.setStatut(StatutUE.ACTIF);
            Set<Classe> classesChi101 = new HashSet<>();
            classesChi101.add(prepaScientifique1A);
            chi101.setClasses(classesChi101);
            ueRepo.save(chi101);

            // NOUVELLES UEs POUR INFORMATIQUE L1A
            UE inf102 = new UE();
            inf102.setCode("INF102");
            inf102.setIntitule("Bases de Données");
            inf102.setCredits(5);
            inf102.setSemestre(1);
            inf102.setDuree(50);
            inf102.setStatut(StatutUE.ACTIF);
            Set<Classe> classesInf102 = new HashSet<>();
            classesInf102.add(infoL1A);
            inf102.setClasses(classesInf102);
            ueRepo.save(inf102);

            UE inf103 = new UE();
            inf103.setCode("INF103");
            inf103.setIntitule("Réseaux et Systèmes");
            inf103.setCredits(5);
            inf103.setSemestre(1);
            inf103.setDuree(50);
            inf103.setStatut(StatutUE.ACTIF);
            Set<Classe> classesInf103 = new HashSet<>();
            classesInf103.add(infoL1A);
            inf103.setClasses(classesInf103);
            ueRepo.save(inf103);

            UE inf104 = new UE();
            inf104.setCode("INF104");
            inf104.setIntitule("Développement Web");
            inf104.setCredits(6);
            inf104.setSemestre(1);
            inf104.setDuree(60);
            inf104.setStatut(StatutUE.ACTIF);
            Set<Classe> classesInf104 = new HashSet<>();
            classesInf104.add(infoL1A);
            inf104.setClasses(classesInf104);
            ueRepo.save(inf104);

            log.info("UEs creees : 11 UEs");

            // ========== ASSIGNATION DES UEs AUX ENSEIGNANTS ==========
            log.info("Assignation des UEs aux enseignants...");

            // Dupont enseigne INF101 et INF201
            Set<UE> uesDupont = new HashSet<>();
            uesDupont.add(inf101);
            uesDupont.add(inf201);
            dupont.setUesEnseignees(uesDupont);
            enseignantRepo.save(dupont);

            // Laurent enseigne MAT101 et MAT201
            Set<UE> uesLaurent = new HashSet<>();
            uesLaurent.add(mat101);
            uesLaurent.add(mat201);
            laurent.setUesEnseignees(uesLaurent);
            enseignantRepo.save(laurent);

            // Moreau enseigne GES101
            Set<UE> uesMoreau = new HashSet<>();
            uesMoreau.add(ges101);
            moreau.setUesEnseignees(uesMoreau);
            enseignantRepo.save(moreau);

            // Dubois enseigne MKT201
            Set<UE> uesDubois = new HashSet<>();
            uesDubois.add(mkt201);
            dubois.setUesEnseignees(uesDubois);
            enseignantRepo.save(dubois);

            // Bernard enseigne MAT201 et PHY101
            Set<UE> uesBernard = new HashSet<>();
            uesBernard.add(mat201);
            uesBernard.add(phy101);
            bernard.setUesEnseignees(uesBernard);
            enseignantRepo.save(bernard);

            // Leroy enseigne CHI101 et PHY101
            Set<UE> uesLeroy = new HashSet<>();
            uesLeroy.add(chi101);
            uesLeroy.add(phy101);
            leroy.setUesEnseignees(uesLeroy);
            enseignantRepo.save(leroy);

            // Garcia enseigne INF102 (Bases de Données)
            Set<UE> uesGarcia = new HashSet<>();
            uesGarcia.add(inf102);
            garcia.setUesEnseignees(uesGarcia);
            enseignantRepo.save(garcia);

            // Rousseau enseigne INF103 (Réseaux et Systèmes)
            Set<UE> uesRousseau = new HashSet<>();
            uesRousseau.add(inf103);
            rousseau.setUesEnseignees(uesRousseau);
            enseignantRepo.save(rousseau);

            // Petit enseigne INF104 (Développement Web)
            Set<UE> uesPetit = new HashSet<>();
            uesPetit.add(inf104);
            petit.setUesEnseignees(uesPetit);
            enseignantRepo.save(petit);

            log.info("UEs assignees aux enseignants avec succes");

            // ========== 7. CREATION DES COURS (depuis frontend) ==========
            log.info("Creation des cours...");

            // INF101 - Introduction à la Programmation (3 types)
            Cours cours1 = new Cours();
            cours1.setIntitule("CM - Introduction à la Programmation");
            cours1.setTypeCours(TypeCours.CM);
            cours1.setUe(inf101);
            cours1.setClasse(infoL1A);
            cours1.setEnseignant(dupont);
            cours1.setDureeTotal(30);
            cours1.setDureeRestante(30);
            cours1.setDescription("Cours magistral d'introduction à la programmation");
            coursRepo.save(cours1);

            Cours cours2 = new Cours();
            cours2.setIntitule("TD - Introduction à la Programmation");
            cours2.setTypeCours(TypeCours.TD);
            cours2.setUe(inf101);
            cours2.setClasse(infoL1A);
            cours2.setEnseignant(dupont);
            cours2.setDureeTotal(20);
            cours2.setDureeRestante(20);
            cours2.setDescription("Travaux dirigés de programmation");
            coursRepo.save(cours2);

            Cours cours3 = new Cours();
            cours3.setIntitule("TP - Introduction à la Programmation");
            cours3.setTypeCours(TypeCours.TP);
            cours3.setUe(inf101);
            cours3.setClasse(infoL1A);
            cours3.setEnseignant(dupont);
            cours3.setDureeTotal(30);
            cours3.setDureeRestante(30);
            cours3.setDescription("Travaux pratiques de programmation");
            coursRepo.save(cours3);

            // MAT101 - Mathématiques Fondamentales (2 types)
            Cours cours4 = new Cours();
            cours4.setIntitule("CM - Mathématiques Fondamentales");
            cours4.setTypeCours(TypeCours.CM);
            cours4.setUe(mat101);
            cours4.setClasse(infoL1A);
            cours4.setEnseignant(laurent);
            cours4.setDureeTotal(40);
            cours4.setDureeRestante(40);
            cours4.setDescription("Cours magistral de mathématiques");
            coursRepo.save(cours4);

            Cours cours5 = new Cours();
            cours5.setIntitule("TD - Mathématiques Fondamentales");
            cours5.setTypeCours(TypeCours.TD);
            cours5.setUe(mat101);
            cours5.setClasse(infoL1A);
            cours5.setEnseignant(laurent);
            cours5.setDureeTotal(20);
            cours5.setDureeRestante(20);
            cours5.setDescription("Travaux dirigés de mathématiques");
            coursRepo.save(cours5);

            // INF201 - Programmation Orientée Objet (2 types)
            Cours cours6 = new Cours();
            cours6.setIntitule("CM - Programmation Orientée Objet");
            cours6.setTypeCours(TypeCours.CM);
            cours6.setUe(inf201);
            cours6.setClasse(infoL2);
            cours6.setEnseignant(dupont);
            cours6.setDureeTotal(30);
            cours6.setDureeRestante(30);
            cours6.setDescription("Cours magistral de POO");
            coursRepo.save(cours6);

            Cours cours7 = new Cours();
            cours7.setIntitule("TP - Programmation Orientée Objet");
            cours7.setTypeCours(TypeCours.TP);
            cours7.setUe(inf201);
            cours7.setClasse(infoL2);
            cours7.setEnseignant(dupont);
            cours7.setDureeTotal(30);
            cours7.setDureeRestante(30);
            cours7.setDescription("Travaux pratiques de POO");
            coursRepo.save(cours7);

            // GES101 - Principes de Gestion (2 types)
            Cours cours8 = new Cours();
            cours8.setIntitule("CM - Principes de Gestion");
            cours8.setTypeCours(TypeCours.CM);
            cours8.setUe(ges101);
            cours8.setClasse(gestionL1);
            cours8.setEnseignant(moreau);
            cours8.setDureeTotal(30);
            cours8.setDureeRestante(30);
            cours8.setDescription("Cours magistral de gestion");
            coursRepo.save(cours8);

            Cours cours9 = new Cours();
            cours9.setIntitule("TD - Principes de Gestion");
            cours9.setTypeCours(TypeCours.TD);
            cours9.setUe(ges101);
            cours9.setClasse(gestionL1);
            cours9.setEnseignant(moreau);
            cours9.setDureeTotal(20);
            cours9.setDureeRestante(20);
            cours9.setDescription("Travaux dirigés de gestion");
            coursRepo.save(cours9);

            // MKT201 - Marketing Digital (2 types)
            Cours cours10 = new Cours();
            cours10.setIntitule("CM - Marketing Digital");
            cours10.setTypeCours(TypeCours.CM);
            cours10.setUe(mkt201);
            cours10.setClasse(marketingL2);
            cours10.setEnseignant(dubois);
            cours10.setDureeTotal(35);
            cours10.setDureeRestante(35);
            cours10.setDescription("Cours magistral de marketing digital");
            coursRepo.save(cours10);

            Cours cours11 = new Cours();
            cours11.setIntitule("TP - Marketing Digital");
            cours11.setTypeCours(TypeCours.TP);
            cours11.setUe(mkt201);
            cours11.setClasse(marketingL2);
            cours11.setEnseignant(dubois);
            cours11.setDureeTotal(20);
            cours11.setDureeRestante(20);
            cours11.setDescription("Travaux pratiques de marketing digital");
            coursRepo.save(cours11);

            // MAT201 - Mathématiques Supérieures (2 types)
            Cours cours12 = new Cours();
            cours12.setIntitule("CM - Mathématiques Supérieures");
            cours12.setTypeCours(TypeCours.CM);
            cours12.setUe(mat201);
            cours12.setClasse(prepaScientifique1A);
            cours12.setEnseignant(bernard);
            cours12.setDureeTotal(50);
            cours12.setDureeRestante(50);
            cours12.setDescription("Cours magistral de mathématiques supérieures");
            coursRepo.save(cours12);

            Cours cours13 = new Cours();
            cours13.setIntitule("TD - Mathématiques Supérieures");
            cours13.setTypeCours(TypeCours.TD);
            cours13.setUe(mat201);
            cours13.setClasse(prepaScientifique1A);
            cours13.setEnseignant(bernard);
            cours13.setDureeTotal(30);
            cours13.setDureeRestante(30);
            cours13.setDescription("Travaux dirigés de mathématiques supérieures");
            coursRepo.save(cours13);

            // PHY101 - Physique Générale (2 types)
            Cours cours14 = new Cours();
            cours14.setIntitule("CM - Physique Générale");
            cours14.setTypeCours(TypeCours.CM);
            cours14.setUe(phy101);
            cours14.setClasse(mpsi);
            cours14.setEnseignant(bernard);
            cours14.setDureeTotal(40);
            cours14.setDureeRestante(40);
            cours14.setDescription("Cours magistral de physique");
            coursRepo.save(cours14);

            Cours cours15 = new Cours();
            cours15.setIntitule("TP - Physique Générale");
            cours15.setTypeCours(TypeCours.TP);
            cours15.setUe(phy101);
            cours15.setClasse(mpsi);
            cours15.setEnseignant(leroy);
            cours15.setDureeTotal(20);
            cours15.setDureeRestante(20);
            cours15.setDescription("Travaux pratiques de physique");
            coursRepo.save(cours15);

            // CHI101 - Chimie Générale (2 types)
            Cours cours16 = new Cours();
            cours16.setIntitule("CM - Chimie Générale");
            cours16.setTypeCours(TypeCours.CM);
            cours16.setUe(chi101);
            cours16.setClasse(prepaScientifique1A);
            cours16.setEnseignant(leroy);
            cours16.setDureeTotal(40);
            cours16.setDureeRestante(40);
            cours16.setDescription("Cours magistral de chimie");
            coursRepo.save(cours16);

            Cours cours17 = new Cours();
            cours17.setIntitule("TP - Chimie Générale");
            cours17.setTypeCours(TypeCours.TP);
            cours17.setUe(chi101);
            cours17.setClasse(prepaScientifique1A);
            cours17.setEnseignant(leroy);
            cours17.setDureeTotal(20);
            cours17.setDureeRestante(20);
            cours17.setDescription("Travaux pratiques de chimie");
            coursRepo.save(cours17);

            // INF102 - Bases de Données (3 types)
            Cours cours18 = new Cours();
            cours18.setIntitule("CM - Bases de Données");
            cours18.setTypeCours(TypeCours.CM);
            cours18.setUe(inf102);
            cours18.setClasse(infoL1A);
            cours18.setEnseignant(garcia);
            cours18.setDureeTotal(25);
            cours18.setDureeRestante(25);
            cours18.setDescription("Cours magistral de bases de données");
            coursRepo.save(cours18);

            Cours cours19 = new Cours();
            cours19.setIntitule("TD - Bases de Données");
            cours19.setTypeCours(TypeCours.TD);
            cours19.setUe(inf102);
            cours19.setClasse(infoL1A);
            cours19.setEnseignant(garcia);
            cours19.setDureeTotal(15);
            cours19.setDureeRestante(15);
            cours19.setDescription("Travaux dirigés de bases de données");
            coursRepo.save(cours19);

            Cours cours20 = new Cours();
            cours20.setIntitule("TP - Bases de Données");
            cours20.setTypeCours(TypeCours.TP);
            cours20.setUe(inf102);
            cours20.setClasse(infoL1A);
            cours20.setEnseignant(garcia);
            cours20.setDureeTotal(10);
            cours20.setDureeRestante(10);
            cours20.setDescription("Travaux pratiques SQL");
            coursRepo.save(cours20);

            // INF103 - Réseaux et Systèmes (3 types)
            Cours cours21 = new Cours();
            cours21.setIntitule("CM - Réseaux et Systèmes");
            cours21.setTypeCours(TypeCours.CM);
            cours21.setUe(inf103);
            cours21.setClasse(infoL1A);
            cours21.setEnseignant(rousseau);
            cours21.setDureeTotal(25);
            cours21.setDureeRestante(25);
            cours21.setDescription("Cours magistral de réseaux");
            coursRepo.save(cours21);

            Cours cours22 = new Cours();
            cours22.setIntitule("TD - Réseaux et Systèmes");
            cours22.setTypeCours(TypeCours.TD);
            cours22.setUe(inf103);
            cours22.setClasse(infoL1A);
            cours22.setEnseignant(rousseau);
            cours22.setDureeTotal(15);
            cours22.setDureeRestante(15);
            cours22.setDescription("Travaux dirigés de réseaux");
            coursRepo.save(cours22);

            Cours cours23 = new Cours();
            cours23.setIntitule("TP - Réseaux et Systèmes");
            cours23.setTypeCours(TypeCours.TP);
            cours23.setUe(inf103);
            cours23.setClasse(infoL1A);
            cours23.setEnseignant(rousseau);
            cours23.setDureeTotal(10);
            cours23.setDureeRestante(10);
            cours23.setDescription("Travaux pratiques de configuration réseau");
            coursRepo.save(cours23);

            // INF104 - Développement Web (3 types)
            Cours cours24 = new Cours();
            cours24.setIntitule("CM - Développement Web");
            cours24.setTypeCours(TypeCours.CM);
            cours24.setUe(inf104);
            cours24.setClasse(infoL1A);
            cours24.setEnseignant(petit);
            cours24.setDureeTotal(30);
            cours24.setDureeRestante(30);
            cours24.setDescription("Cours magistral de développement web");
            coursRepo.save(cours24);

            Cours cours25 = new Cours();
            cours25.setIntitule("TD - Développement Web");
            cours25.setTypeCours(TypeCours.TD);
            cours25.setUe(inf104);
            cours25.setClasse(infoL1A);
            cours25.setEnseignant(petit);
            cours25.setDureeTotal(15);
            cours25.setDureeRestante(15);
            cours25.setDescription("Travaux dirigés HTML/CSS");
            coursRepo.save(cours25);

            Cours cours26 = new Cours();
            cours26.setIntitule("TP - Développement Web");
            cours26.setTypeCours(TypeCours.TP);
            cours26.setUe(inf104);
            cours26.setClasse(infoL1A);
            cours26.setEnseignant(petit);
            cours26.setDureeTotal(15);
            cours26.setDureeRestante(15);
            cours26.setDescription("Travaux pratiques JavaScript");
            coursRepo.save(cours26);

            log.info("Cours crees : 26 cours (au moins 2 types par UE)");

            // ========== 8. CREATION DES SALLES ==========
            log.info("Creation des salles...");

            Salle salleCours1 = new Salle();
            salleCours1.setNomSalle("Salle de Cours 101");
            salleCours1.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            salleCours1.setCapacite(60);
            salleCours1.setEtage("1er étage");
            salleCours1.setBatiment("Nouveau bâtiment");
            salleRepo.save(salleCours1);

            Salle salleCours2 = new Salle();
            salleCours2.setNomSalle("Salle de Cours 102");
            salleCours2.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            salleCours2.setCapacite(55);
            salleCours2.setEtage("1er étage");
            salleCours2.setBatiment("Nouveau bâtiment");
            salleRepo.save(salleCours2);

            Salle salleTD1 = new Salle();
            salleTD1.setNomSalle("Salle TD 201");
            salleTD1.setTypeSalle(TypeSalle.SALLE_DE_TD);
            salleTD1.setCapacite(40);
            salleTD1.setEtage("2ème étage");
            salleTD1.setBatiment("Nouveau bâtiment");
            salleRepo.save(salleTD1);

            Salle salleTD2 = new Salle();
            salleTD2.setNomSalle("Salle TD 202");
            salleTD2.setTypeSalle(TypeSalle.SALLE_DE_TD);
            salleTD2.setCapacite(35);
            salleTD2.setEtage("2ème étage");
            salleTD2.setBatiment("Ancien bâtiment");
            salleRepo.save(salleTD2);

            Salle salleInfo1 = new Salle();
            salleInfo1.setNomSalle("Salle Informatique 301");
            salleInfo1.setTypeSalle(TypeSalle.SALLE_INFORMATIQUE);
            salleInfo1.setCapacite(30);
            salleInfo1.setEtage("3ème étage");
            salleInfo1.setBatiment("Nouveau bâtiment");
            salleRepo.save(salleInfo1);

            Salle salleInfo2 = new Salle();
            salleInfo2.setNomSalle("Salle Informatique 302");
            salleInfo2.setTypeSalle(TypeSalle.SALLE_INFORMATIQUE);
            salleInfo2.setCapacite(25);
            salleInfo2.setEtage("3ème étage");
            salleInfo2.setBatiment("Ancien bâtiment");
            salleRepo.save(salleInfo2);

            Salle labo1 = new Salle();
            labo1.setNomSalle("Laboratoire Chimie");
            labo1.setTypeSalle(TypeSalle.LABORATOIRE);
            labo1.setCapacite(20);
            labo1.setEtage("RDC");
            labo1.setBatiment("Ancien bâtiment");
            salleRepo.save(labo1);

            Salle labo2 = new Salle();
            labo2.setNomSalle("Laboratoire Physique");
            labo2.setTypeSalle(TypeSalle.LABORATOIRE);
            labo2.setCapacite(20);
            labo2.setEtage("RDC");
            labo2.setBatiment("Ancien bâtiment");
            salleRepo.save(labo2);

            Salle amphitheatreA = new Salle();
            amphitheatreA.setNomSalle("Amphithéâtre A");
            amphitheatreA.setTypeSalle(TypeSalle.SALLE_DE_COURS);
            amphitheatreA.setCapacite(200);
            amphitheatreA.setEtage("RDC");
            amphitheatreA.setBatiment("Nouveau bâtiment");
            salleRepo.save(amphitheatreA);

            log.info("Salles creees : 9 salles");

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

            // Méthode helper pour créer des disponibilités complètes
            java.util.function.BiConsumer<Enseignant, String> creerDisponibilites = (enseignant, nom) -> {
                DisponibiliteEnseignant dispo = new DisponibiliteEnseignant();
                dispo.setEnseignant(enseignant);
                dispo.setDateDebut(debutSemaine);
                dispo.setDateFin(finSemaine);
                disponibiliteRepo.save(dispo);

                // Créer des créneaux pour Lundi à Samedi (6 jours)
                for (int i = 0; i < 6; i++) {
                    LocalDate jour = debutSemaine.plusDays(i);

                    CreneauDisponibilite creneau = new CreneauDisponibilite();
                    creneau.setDisponibilite(dispo);
                    creneau.setDate(jour);
                    creneauDispoRepo.save(creneau);

                    // Plage matin : 8h-12h
                    PlageHoraire plageMatin = new PlageHoraire();
                    plageMatin.setCreneauDisponibilite(creneau);
                    plageMatin.setHeureDebut(LocalTime.of(8, 0));
                    plageMatin.setHeureFin(LocalTime.of(12, 0));
                    plageHoraireRepo.save(plageMatin);

                    // Plage après-midi : 13h-17h
                    PlageHoraire plageApresMidi = new PlageHoraire();
                    plageApresMidi.setCreneauDisponibilite(creneau);
                    plageApresMidi.setHeureDebut(LocalTime.of(13, 0));
                    plageApresMidi.setHeureFin(LocalTime.of(17, 0));
                    plageHoraireRepo.save(plageApresMidi);
                }
                log.info("Disponibilites creees pour {} (Lun-Sam, 8h-12h et 13h-17h)", nom);
            };

            // Créer les disponibilités pour tous les enseignants
            creerDisponibilites.accept(dupont, "Dupont");
            creerDisponibilites.accept(laurent, "Laurent");
            creerDisponibilites.accept(moreau, "Moreau");
            creerDisponibilites.accept(dubois, "Dubois");
            creerDisponibilites.accept(bernard, "Bernard");
            creerDisponibilites.accept(leroy, "Leroy");
            creerDisponibilites.accept(garcia, "Garcia");
            creerDisponibilites.accept(rousseau, "Rousseau");
            creerDisponibilites.accept(petit, "Petit");

            log.info("Disponibilites creees pour la semaine du {} au {} (tous les enseignants)", debutSemaine, finSemaine);

            // ========== 12. CREATION DES EQUIPEMENTS (depuis frontend) ==========
            log.info("Creation des equipements...");

            Equipment projecteur = new Equipment();
            projecteur.setName("Projecteur");
            projecteur.setCategory("audiovisuel");
            projecteur.setIcon("📽️");
            projecteur.setDescription("Projecteur haute définition pour présentations");
            projecteur.setTotalQuantity(25);
            projecteur.setAvailableQuantity(18);
            projecteur.setStatus("active");
            equipmentRepo.save(projecteur);

            Equipment ordinateurs = new Equipment();
            ordinateurs.setName("Ordinateurs");
            ordinateurs.setCategory("informatique");
            ordinateurs.setIcon("💻");
            ordinateurs.setDescription("Ordinateurs de bureau pour salles informatiques");
            ordinateurs.setTotalQuantity(120);
            ordinateurs.setAvailableQuantity(90);
            ordinateurs.setStatus("active");
            equipmentRepo.save(ordinateurs);

            Equipment tableauBlanc = new Equipment();
            tableauBlanc.setName("Tableau blanc");
            tableauBlanc.setCategory("ecriture");
            tableauBlanc.setIcon("📋");
            tableauBlanc.setDescription("Tableau blanc effaçable pour écriture");
            tableauBlanc.setTotalQuantity(45);
            tableauBlanc.setAvailableQuantity(32);
            tableauBlanc.setStatus("active");
            equipmentRepo.save(tableauBlanc);

            Equipment tableauInteractif = new Equipment();
            tableauInteractif.setName("Tableau interactif");
            tableauInteractif.setCategory("ecriture");
            tableauInteractif.setIcon("📱");
            tableauInteractif.setDescription("Tableau numérique interactif tactile");
            tableauInteractif.setTotalQuantity(15);
            tableauInteractif.setAvailableQuantity(12);
            tableauInteractif.setStatus("active");
            equipmentRepo.save(tableauInteractif);

            Equipment hautParleurs = new Equipment();
            hautParleurs.setName("Haut-parleurs");
            hautParleurs.setCategory("audiovisuel");
            hautParleurs.setIcon("🔊");
            hautParleurs.setDescription("Système audio pour amplification sonore");
            hautParleurs.setTotalQuantity(30);
            hautParleurs.setAvailableQuantity(28);
            hautParleurs.setStatus("active");
            equipmentRepo.save(hautParleurs);

            Equipment microphone = new Equipment();
            microphone.setName("Microphone");
            microphone.setCategory("audiovisuel");
            microphone.setIcon("🎤");
            microphone.setDescription("Microphone sans fil pour présentations");
            microphone.setTotalQuantity(20);
            microphone.setAvailableQuantity(19);
            microphone.setStatus("active");
            equipmentRepo.save(microphone);

            Equipment camera = new Equipment();
            camera.setName("Caméra");
            camera.setCategory("audiovisuel");
            camera.setIcon("📹");
            camera.setDescription("Caméra pour enregistrement et visioconférence");
            camera.setTotalQuantity(10);
            camera.setAvailableQuantity(8);
            camera.setStatus("active");
            equipmentRepo.save(camera);

            Equipment imprimante = new Equipment();
            imprimante.setName("Imprimante");
            imprimante.setCategory("informatique");
            imprimante.setIcon("🖨️");
            imprimante.setDescription("Imprimante laser couleur");
            imprimante.setTotalQuantity(12);
            imprimante.setAvailableQuantity(9);
            imprimante.setStatus("active");
            equipmentRepo.save(imprimante);

            Equipment scanner = new Equipment();
            scanner.setName("Scanner");
            scanner.setCategory("informatique");
            scanner.setIcon("📄");
            scanner.setDescription("Scanner de documents haute résolution");
            scanner.setTotalQuantity(8);
            scanner.setAvailableQuantity(6);
            scanner.setStatus("active");
            equipmentRepo.save(scanner);

            Equipment climatisation = new Equipment();
            climatisation.setName("Climatisation");
            climatisation.setCategory("confort");
            climatisation.setIcon("❄️");
            climatisation.setDescription("Système de climatisation réversible");
            climatisation.setTotalQuantity(35);
            climatisation.setAvailableQuantity(28);
            climatisation.setStatus("active");
            equipmentRepo.save(climatisation);

            Equipment wifi = new Equipment();
            wifi.setName("WiFi");
            wifi.setCategory("connectivite");
            wifi.setIcon("📶");
            wifi.setDescription("Point d'accès WiFi haute performance");
            wifi.setTotalQuantity(50);
            wifi.setAvailableQuantity(42);
            wifi.setStatus("active");
            equipmentRepo.save(wifi);

            Equipment ethernet = new Equipment();
            ethernet.setName("Ethernet");
            ethernet.setCategory("connectivite");
            ethernet.setIcon("🔌");
            ethernet.setDescription("Prises réseau Ethernet gigabit");
            ethernet.setTotalQuantity(200);
            ethernet.setAvailableQuantity(170);
            ethernet.setStatus("active");
            equipmentRepo.save(ethernet);

            log.info("Equipements crees : 12 equipements");

            // ========== 13. CREATION DES AFFECTATIONS D'EQUIPEMENTS ==========
            log.info("Creation des affectations d'equipements...");

            // Affectation 1: Projecteur à Salle 101
            EquipmentAssignment affectation1 = new EquipmentAssignment();
            affectation1.setEquipment(projecteur);
            affectation1.setAssignmentType("room");
            affectation1.setTargetId(salleCours1.getIdSalle()); // ✅ targetId défini
            affectation1.setQuantity(1);
            affectation1.setStartDate(LocalDate.of(2024, 1, 15));
            affectation1.setDuration("permanent");
            affectation1.setReason("Équipement fixe de la salle");
            affectation1.setStatus("active");
            affectation1.setAssignedBy("Admin");
            affectation1.setNotes("Projecteur installé au plafond");
            equipmentAssignmentRepo.save(affectation1);

            // Affectation 2: Ordinateurs à Lab Info 1
            EquipmentAssignment affectation2 = new EquipmentAssignment();
            affectation2.setEquipment(ordinateurs);
            affectation2.setAssignmentType("room");
            affectation2.setTargetId(salleInfo1.getIdSalle()); // ✅ targetId défini
            affectation2.setQuantity(30);
            affectation2.setStartDate(LocalDate.of(2024, 1, 15));
            affectation2.setDuration("permanent");
            affectation2.setReason("Équipement de laboratoire informatique");
            affectation2.setStatus("active");
            affectation2.setAssignedBy("Admin");
            equipmentAssignmentRepo.save(affectation2);

            // Affectation 3: Haut-parleurs à Informatique L1A (temporaire)
            EquipmentAssignment affectation3 = new EquipmentAssignment();
            affectation3.setEquipment(hautParleurs);
            affectation3.setAssignmentType("class");
            affectation3.setTargetId(infoL1A.getIdClasse()); // ✅ targetId défini
            affectation3.setQuantity(2);
            affectation3.setStartDate(LocalDate.of(2024, 1, 20));
            affectation3.setEndDate(LocalDate.of(2024, 6, 30));
            affectation3.setDuration("temporary");
            affectation3.setReason("Présentation de projet de fin de semestre");
            affectation3.setStatus("active");
            affectation3.setAssignedBy("Prof. Martin");
            affectation3.setNotes("Pour les soutenances de projets");
            equipmentAssignmentRepo.save(affectation3);

            // Affectation 4: Microphone à Gestion L1 (temporaire)
            EquipmentAssignment affectation4 = new EquipmentAssignment();
            affectation4.setEquipment(microphone);
            affectation4.setAssignmentType("class");
            affectation4.setTargetId(gestionL1.getIdClasse()); // ✅ targetId défini
            affectation4.setQuantity(1);
            affectation4.setStartDate(LocalDate.of(2024, 2, 1));
            affectation4.setEndDate(LocalDate.of(2024, 2, 15));
            affectation4.setDuration("temporary");
            affectation4.setReason("Cours de communication orale");
            affectation4.setStatus("active");
            affectation4.setAssignedBy("Prof. Sophie");
            equipmentAssignmentRepo.save(affectation4);

            // Affectation 5: Projecteur à Amphithéâtre A - CORRIGÉ
            EquipmentAssignment affectation5 = new EquipmentAssignment();
            affectation5.setEquipment(projecteur);
            affectation5.setAssignmentType("room");
            affectation5.setTargetId(amphitheatreA.getIdSalle()); // ✅ AJOUT DU targetId
            affectation5.setQuantity(2);
            affectation5.setStartDate(LocalDate.of(2024, 1, 15));
            affectation5.setDuration("permanent");
            affectation5.setReason("Équipement fixe de l'amphithéâtre");
            affectation5.setStatus("active");
            affectation5.setAssignedBy("Admin");
            equipmentAssignmentRepo.save(affectation5);

            // Affectation 6: Tableau blanc à Salle TD 1
            EquipmentAssignment affectation6 = new EquipmentAssignment();
            affectation6.setEquipment(tableauBlanc);
            affectation6.setAssignmentType("room");
            affectation6.setTargetId(salleTD1.getIdSalle()); // ✅ targetId défini
            affectation6.setQuantity(3);
            affectation6.setStartDate(LocalDate.of(2024, 1, 15));
            affectation6.setDuration("permanent");
            affectation6.setReason("Équipement fixe de la salle");
            affectation6.setStatus("active");
            affectation6.setAssignedBy("Admin");
            equipmentAssignmentRepo.save(affectation6);

            log.info("Affectations d'equipements creees : 6 affectations");

            // ========== RESUME ==========
            log.info("");
            log.info("=== BASE DE DONNEES INITIALISEE AVEC SUCCES ===");
            log.info("Ecoles: 4 | Filieres: 5 | Classes: 7 | Groupes: 2");
            log.info("Enseignants: 9 | Etudiants: 2 | Administrateurs: 1");
            log.info("UEs: 11 | Cours: 26 (au moins 2 types par UE) | Salles: 9");
            log.info("Equipements: 12 | Affectations: 6");
            log.info("Disponibilites: Tous les enseignants (Lun-Sam, 8h-12h et 13h-17h)");
            log.info("");
            log.info("=== COURS POUR INFORMATIQUE L1A ===");
            log.info("INF101 - Introduction à la Programmation (CM 30h, TD 20h, TP 30h) - Dupont");
            log.info("MAT101 - Mathématiques Fondamentales (CM 40h, TD 20h) - Laurent");
            log.info("INF102 - Bases de Données (CM 25h, TD 15h, TP 10h) - Garcia");
            log.info("INF103 - Réseaux et Systèmes (CM 25h, TD 15h, TP 10h) - Rousseau");
            log.info("INF104 - Développement Web (CM 30h, TD 15h, TP 15h) - Petit");
            log.info("TOTAL: 5 UEs, 15 cours, 260 heures");
            log.info("");
            log.info("=== IDENTIFIANTS DE CONNEXION ===");
            log.info("Admin: admin@test.com / password123");
            log.info("Enseignant 1: martin.dupont@iu-saintjean.cm / password123");
            log.info("Enseignant 2: sophie.laurent@iu-saintjean.cm / password123");
            log.info("Enseignant 3: jean.moreau@iu-saintjean.cm / password123");
            log.info("Enseignant 4: marie.dubois@iu-saintjean.cm / password123");
            log.info("Enseignant 5: sophie.bernard@iu-saintjean.cm / password123");
            log.info("Enseignant 6: pierre.leroy@iu-saintjean.cm / password123");
            log.info("Enseignant 7: carlos.garcia@iu-saintjean.cm / password123");
            log.info("Enseignant 8: isabelle.rousseau@iu-saintjean.cm / password123");
            log.info("Enseignant 9: thomas.petit@iu-saintjean.cm / password123");
            log.info("Etudiant 1: jean.dupont@student.com / password123");
            log.info("Etudiant 2: marie.martin@student.com / password123");
            log.info("===========================================");
        };
    }
}