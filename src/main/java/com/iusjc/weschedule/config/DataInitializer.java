package com.iusjc.weschedule.config;

import com.iusjc.weschedule.enums.Role;
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
            GroupeRepository groupeRepo,
            EtudiantRepository etudiantRepo,
            DisponibiliteEnseignantRepository disponibiliteRepo,
            CreneauDisponibiliteRepository creneauDispoRepo,
            PlageHoraireRepository plageHoraireRepo) {
        return args -> {
            log.info("Initialisation complete de la base de donnees...");

            // Vérifier si les données existent déjà
            if (adminRepo.count() > 0) {
                log.info("Base de donnees deja initialisee, verification des disponibilites...");
                
                // Vérifier si l'enseignant a des disponibilités
                Optional<Utilisateur> utilisateurOpt = utilisateurRepo.findByEmail("goodskrt2.0@gmail.com");
                if (utilisateurOpt.isPresent() && utilisateurOpt.get() instanceof Enseignant enseignant) {
                    List<DisponibiliteEnseignant> disponibilites = disponibiliteRepo.findByEnseignant(enseignant);
                    
                    if (disponibilites.isEmpty()) {
                        log.info("Aucune disponibilite trouvee, creation d'une disponibilite de test...");
                        
                        // Créer une disponibilité pour cette semaine
                        LocalDate debutSemaine = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
                        LocalDate finSemaine = debutSemaine.plusDays(6);

                        DisponibiliteEnseignant disponibilite = new DisponibiliteEnseignant();
                        disponibilite.setEnseignant(enseignant);
                        disponibilite.setDateDebut(debutSemaine);
                        disponibilite.setDateFin(finSemaine);
                        disponibiliteRepo.save(disponibilite);

                        // Créer quelques créneaux pour lundi et mardi
                        for (int i = 0; i < 2; i++) {
                            LocalDate jour = debutSemaine.plusDays(i);
                            
                            CreneauDisponibilite creneau = new CreneauDisponibilite();
                            creneau.setDisponibilite(disponibilite);
                            creneau.setDate(jour);
                            creneauDispoRepo.save(creneau);

                            // Matin : 8h-10h
                            PlageHoraire plageMatin = new PlageHoraire();
                            plageMatin.setCreneauDisponibilite(creneau);
                            plageMatin.setHeureDebut(LocalTime.of(8, 0));
                            plageMatin.setHeureFin(LocalTime.of(10, 0));
                            plageHoraireRepo.save(plageMatin);

                            // Après-midi : 14h-16h
                            PlageHoraire plageApresMidi = new PlageHoraire();
                            plageApresMidi.setCreneauDisponibilite(creneau);
                            plageApresMidi.setHeureDebut(LocalTime.of(14, 0));
                            plageApresMidi.setHeureFin(LocalTime.of(16, 0));
                            plageHoraireRepo.save(plageApresMidi);
                        }
                        
                        log.info("Disponibilite de test creee pour la semaine du {} au {}", debutSemaine, finSemaine);
                    } else {
                        log.info("L'enseignant a deja {} disponibilite(s)", disponibilites.size());
                    }
                }
                return;
            }

            // 1. Creer les ecoles
            Ecole ecole1 = new Ecole();
            ecole1.setNomEcole("Institut Universitaire Saint Jean de Cronstadt");
            ecole1.setAdresse("Douala, Cameroun");
            ecole1.setTelephone("+237 233 42 15 67");
            ecole1.setEmail("contact@iusjc.edu.cm");
            ecoleRepo.save(ecole1);

            Ecole ecole2 = new Ecole();
            ecole2.setNomEcole("Ecole Superieure de Technologie");
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

            // 5. Creer les UE
            UE ueAlgo = new UE();
            ueAlgo.setIntitule("Algorithmique et Programmation");
            ueAlgo.setCode("INF101");
            ueAlgo.setDuree(60);
            Set<Classe> classesAlgo = new HashSet<>();
            classesAlgo.add(classeL1Info);
            ueAlgo.setClasses(classesAlgo);
            ueRepo.save(ueAlgo);

            UE ueBDD = new UE();
            ueBDD.setIntitule("Base de Donnees");
            ueBDD.setCode("INF201");
            ueBDD.setDuree(45);
            Set<Classe> classesBDD = new HashSet<>();
            classesBDD.add(classeL2Info);
            ueBDD.setClasses(classesBDD);
            ueRepo.save(ueBDD);

            UE ueCompta = new UE();
            ueCompta.setIntitule("Comptabilite Generale");
            ueCompta.setCode("GES101");
            ueCompta.setDuree(40);
            Set<Classe> classesCompta = new HashSet<>();
            classesCompta.add(classeL1Gestion);
            ueCompta.setClasses(classesCompta);
            ueRepo.save(ueCompta);

            UE ueMaths = new UE();
            ueMaths.setIntitule("Mathematiques pour l'Informatique");
            ueMaths.setCode("MAT101");
            ueMaths.setDuree(50);
            Set<Classe> classesMaths = new HashSet<>();
            classesMaths.add(classeL1Info);
            classesMaths.add(classeL2Info);
            ueMaths.setClasses(classesMaths);
            ueRepo.save(ueMaths);

            log.info("UE creees");

            // 6. Creer l'administrateur
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
            enseignant.setNom("MOUPS");
            enseignant.setPrenom("Moups");
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
            log.info("Ecoles: 2 | Filieres: 3 | Classes: 3 | UE: 4");
            log.info("Disponibilites: Semaine complete avec creneaux detailles");
        };
    }
}