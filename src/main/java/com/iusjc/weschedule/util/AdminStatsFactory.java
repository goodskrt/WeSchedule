package com.iusjc.weschedule.util;

import com.iusjc.weschedule.dto.AdminStatChip;
import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.enums.StatutSalle;
import com.iusjc.weschedule.enums.StatutUE;
import com.iusjc.weschedule.enums.TypeCours;
import com.iusjc.weschedule.models.Cours;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.*;

import java.util.List;

/**
 * Construit des listes homogènes de {@link AdminStatChip} pour le fragment Thymeleaf {@code admin-stats}.
 */
public final class AdminStatsFactory {

    private AdminStatsFactory() {}

    public static List<AdminStatChip> salles(SalleRepository salleRepository) {
        List<Salle> all = salleRepository.findAll();
        long total = all.size();
        long dispo = all.stream().filter(s -> s.getStatut() == StatutSalle.DISPONIBLE).count();
        long occ = all.stream().filter(s -> s.getStatut() == StatutSalle.OCCUPE).count();
        long maint = all.stream().filter(s -> s.getStatut() == StatutSalle.EN_MAINTENANCE).count();
        return List.of(
                new AdminStatChip("Total salles", String.valueOf(total), "fa-door-open"),
                new AdminStatChip("Disponibles", String.valueOf(dispo), "fa-check-circle"),
                new AdminStatChip("Occupées", String.valueOf(occ), "fa-user-clock"),
                new AdminStatChip("Maintenance", String.valueOf(maint), "fa-wrench")
        );
    }

    public static List<AdminStatChip> classes(ClasseRepository classeRepository, FiliereRepository filiereRepository, EcoleRepository ecoleRepository) {
        long total = classeRepository.count();
        long filieres = filiereRepository.count();
        long ecoles = ecoleRepository.count();
        return List.of(
                new AdminStatChip("Classes", String.valueOf(total), "fa-layer-group"),
                new AdminStatChip("Filières", String.valueOf(filieres), "fa-sitemap"),
                new AdminStatChip("Écoles", String.valueOf(ecoles), "fa-school")
        );
    }

    public static List<AdminStatChip> cours(CoursRepository coursRepository) {
        List<Cours> all = coursRepository.findAll();
        long total = all.size();
        long cm = all.stream().filter(c -> c.getTypeCours() == TypeCours.CM).count();
        long td = all.stream().filter(c -> c.getTypeCours() == TypeCours.TD).count();
        long tp = all.stream().filter(c -> c.getTypeCours() == TypeCours.TP).count();
        long avecEns = all.stream().filter(c -> c.getEnseignant() != null).count();
        return List.of(
                new AdminStatChip("Total cours", String.valueOf(total), "fa-book-open"),
                new AdminStatChip("CM", String.valueOf(cm), "fa-chalkboard-teacher"),
                new AdminStatChip("TD", String.valueOf(td), "fa-chalkboard"),
                new AdminStatChip("TP", String.valueOf(tp), "fa-flask"),
                new AdminStatChip("Avec enseignant", String.valueOf(avecEns), "fa-user-tie")
        );
    }

    public static List<AdminStatChip> ues(UERepository ueRepository, CoursRepository coursRepository) {
        List<UE> ues = ueRepository.findAll();
        long total = ues.size();
        long actifs = ues.stream().filter(u -> u.getStatut() == StatutUE.ACTIF).count();
        long totalCours = coursRepository.count();
        return List.of(
                new AdminStatChip("UEs", String.valueOf(total), "fa-book"),
                new AdminStatChip("UEs actives", String.valueOf(actifs), "fa-toggle-on"),
                new AdminStatChip("Cours liés", String.valueOf(totalCours), "fa-link")
        );
    }

    public static List<AdminStatChip> emploisDuTemps(ClasseRepository classeRepository, EmploiDuTempsClasseRepository edtRepo, SeanceClasseRepository seanceRepo) {
        long classes = classeRepository.count();
        long edt = edtRepo.count();
        long seances = seanceRepo.count();
        return List.of(
                new AdminStatChip("Classes", String.valueOf(classes), "fa-users"),
                new AdminStatChip("Emplois créés", String.valueOf(edt), "fa-calendar-alt"),
                new AdminStatChip("Séances planifiées", String.valueOf(seances), "fa-clock")
        );
    }

    public static List<AdminStatChip> enseignants(EnseignantRepository enseignantRepository) {
        long total = enseignantRepository.count();
        return List.of(
                new AdminStatChip("Enseignants", String.valueOf(total), "fa-chalkboard-teacher"),
                new AdminStatChip("Comptes", String.valueOf(total), "fa-id-card", "Profils enregistrés")
        );
    }

    public static List<AdminStatChip> etudiants(EtudiantRepository etudiantRepository, ClasseRepository classeRepository, GroupeRepository groupeRepository) {
        long total = etudiantRepository.count();
        long avecClasse = etudiantRepository.countByClasseIsNotNull();
        long classes = classeRepository.count();
        long groupes = groupeRepository.count();
        return List.of(
                new AdminStatChip("Étudiants", String.valueOf(total), "fa-user-graduate"),
                new AdminStatChip("Affectés à une classe", String.valueOf(avecClasse), "fa-link"),
                new AdminStatChip("Classes", String.valueOf(classes), "fa-layer-group"),
                new AdminStatChip("Groupes", String.valueOf(groupes), "fa-object-group")
        );
    }

    public static List<AdminStatChip> equipements(EquipmentRepository equipmentRepository, TypeEquipementRepository typeRepo, CategorieEquipementRepository categorieRepo) {
        long total = equipmentRepository.count();
        long dispo = equipmentRepository.countByStatut(StatutEquipement.DISPONIBLE);
        long service = equipmentRepository.countByStatut(StatutEquipement.EN_SERVICE);
        long maint = equipmentRepository.countByStatut(StatutEquipement.EN_MAINTENANCE);
        long hors = equipmentRepository.countByStatut(StatutEquipement.HORS_SERVICE);
        long stock = equipmentRepository.countBySalleIsNull();
        long types = typeRepo.count();
        long cats = categorieRepo.count();
        return List.of(
                new AdminStatChip("Total", String.valueOf(total), "fa-boxes-stacked"),
                new AdminStatChip("Disponibles", String.valueOf(dispo), "fa-check-circle"),
                new AdminStatChip("En service", String.valueOf(service), "fa-plug"),
                new AdminStatChip("Maintenance", String.valueOf(maint), "fa-wrench"),
                new AdminStatChip("Hors service", String.valueOf(hors), "fa-ban"),
                new AdminStatChip("Sans salle", String.valueOf(stock), "fa-warehouse"),
                new AdminStatChip("Types / cat.", types + " / " + cats, "fa-tags")
        );
    }

    public static List<AdminStatChip> typesEquipement(EquipmentRepository equipmentRepository, TypeEquipementRepository typeRepo, CategorieEquipementRepository categorieRepo) {
        return List.of(
                new AdminStatChip("Types", String.valueOf(typeRepo.count()), "fa-tags"),
                new AdminStatChip("Catégories", String.valueOf(categorieRepo.count()), "fa-folder-tree"),
                new AdminStatChip("Équipements", String.valueOf(equipmentRepository.count()), "fa-laptop")
        );
    }

    /** Vue synthétique pour les pages « Rapports » / « Paramètres ». */
    public static List<AdminStatChip> syntheseGlobale(
            SalleRepository salles,
            ClasseRepository classes,
            CoursRepository cours,
            UERepository ues,
            EnseignantRepository ens,
            EquipmentRepository equipements,
            EtudiantRepository etudiants) {
        return List.of(
                new AdminStatChip("Salles", String.valueOf(salles.count()), "fa-door-open"),
                new AdminStatChip("Classes", String.valueOf(classes.count()), "fa-layer-group"),
                new AdminStatChip("Étudiants", String.valueOf(etudiants.count()), "fa-user-graduate"),
                new AdminStatChip("Cours", String.valueOf(cours.count()), "fa-book-open"),
                new AdminStatChip("UEs", String.valueOf(ues.count()), "fa-book"),
                new AdminStatChip("Enseignants", String.valueOf(ens.count()), "fa-chalkboard-teacher"),
                new AdminStatChip("Équipements", String.valueOf(equipements.count()), "fa-laptop")
        );
    }
}
