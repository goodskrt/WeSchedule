package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.TypeCours;
import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import com.iusjc.weschedule.util.FordFulkerson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Service de génération automatique des emplois du temps.
 *
 * Algorithme : Ford-Fulkerson (Edmonds-Karp) appliqué créneau par créneau.
 *
 * Réseau de flot par créneau horaire (slot t) :
 *
 *   Source (S)
 *     ──[cap=1]──► (UE_i, Classe_j)           pour chaque paire dont la classe est libre à t
 *        ──[cap=1]──► Enseignant_k_in          si l'enseignant peut enseigner UE_i et est dispo à t
 *           ──[cap=1]──► Enseignant_k_out      nœud de division → anti double-réservation
 *              ──[cap=1]──► Salle_r            si salle_r.capacité ≥ effectif(Classe_j)
 *                 ──[cap=1]──► Sink (T)        anti double-réservation salle
 *
 * La valeur du flot maximum = nombre de séances planifiables à ce créneau.
 * On remonte les chemins de flot pour extraire les affectations concrètes.
 *
 * PROBLÈME du nœud enseignant → salle : après le nœud divisé de l'enseignant,
 * on perd l'information sur la classe (nécessaire pour vérifier la capacité salle).
 * Solution : on ajoute un nœud intermédiaire (Enseignant_k, Classe_j) entre
 * Enseignant_k_out et Salle_r, ce qui permet de filtrer les salles par effectif
 * tout en maintenant la contrainte de non-double-réservation de l'enseignant.
 *
 * Réseau complet par slot :
 *
 *   S ──► UC_ij ──► T_k_in ──► T_k_out ──► TC_kj ──► R_r ──► Sink
 *
 *   Où TC_kj = nœud de liaison (Teacher k, Classe j) qui connecte uniquement
 *   aux salles ayant une capacité suffisante pour Classe_j.
 */
@Service
@Slf4j
public class EmploiDeTempsService {

    private static final List<LocalTime[]> CRENEAUX_JOURNEE = List.of(
            new LocalTime[]{LocalTime.of(8,  0), LocalTime.of(10, 0)},
            new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(12, 0)},
            new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(16, 0)},
            new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(18, 0)}
    );

    @Autowired private EmploiDeTempsRepository emploiDeTempsRepository;
    @Autowired private CreneauEmploiRepository  creneauEmploiRepository;
    @Autowired private UERepository             ueRepository;
    @Autowired private SalleRepository          salleRepository;
    @Autowired private PlageHoraireRepository   plageHoraireRepository;
    @Autowired private EnseignantRepository     enseignantRepository;

    // ==================== LECTURE ====================

    public List<EmploiDeTemps> getAllEmplois() {
        return emploiDeTempsRepository.findAll();
    }

    public Optional<EmploiDeTemps> getById(UUID id) {
        return emploiDeTempsRepository.findById(id);
    }

    public List<CreneauEmploi> getCreneauxByEmploi(UUID edtId) {
        return creneauEmploiRepository.findByEmploiDeTempsIdEDTOrderByDateAscHeureDebutAsc(edtId);
    }

    // ==================== SUPPRESSION ====================

    @Transactional
    public void supprimerEmploi(UUID id) {
        EmploiDeTemps edt = emploiDeTempsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Emploi du temps introuvable"));
        creneauEmploiRepository.deleteByEmploiDeTemps(edt);
        emploiDeTempsRepository.delete(edt);
    }

    // ==================== GÉNÉRATION AUTOMATIQUE (FORD-FULKERSON) ====================

    @Transactional
    public GenerationResult genererEmploiDeTemps(LocalDate debut, LocalDate fin) {
        log.info("Génération Ford-Fulkerson : {} → {}", debut, fin);

        EmploiDeTemps edt = new EmploiDeTemps();
        edt.setPeriodeDebut(debut);
        edt.setPeriodeFin(fin);
        edt = emploiDeTempsRepository.save(edt);

        List<UE>    toutesLesUEs    = ueRepository.findAll();
        List<Salle> toutesLesSalles = salleRepository.findAll();
        List<String> avertissements  = new ArrayList<>();

        if (toutesLesUEs.isEmpty()) {
            avertissements.add("Aucune UE configurée dans le système.");
            return new GenerationResult(edt, 0, 0, avertissements);
        }

        // --- Construire les séances à planifier ---
        List<SeanceAPlanifier> seances = new ArrayList<>();
        for (UE ue : toutesLesUEs) {
            if (ue.getClasses() == null || ue.getClasses().isEmpty()) {
                avertissements.add("UE \"" + ue.getIntitule() + "\" : aucune classe associée.");
                continue;
            }
            List<Enseignant> enseignants = enseignantRepository.findByUEEnseignee(ue);
            if (enseignants.isEmpty()) {
                avertissements.add("UE \"" + ue.getIntitule() + "\" : aucun enseignant assigné.");
                continue;
            }
            int sessions = calculerNombreSeances(ue);
            for (Classe classe : ue.getClasses()) {
                seances.add(new SeanceAPlanifier(ue, classe, enseignants, sessions));
            }
        }

        int totalSeances      = seances.stream().mapToInt(s -> s.sessionsRestantes).sum();
        int seancesPlanifiees = 0;

        // --- Itération sur les jours et créneaux ---
        LocalDate jour = debut;
        while (!jour.isAfter(fin)) {
            if (estJourOuvre(jour)) {
                for (LocalTime[] creneau : CRENEAUX_JOURNEE) {
                    LocalTime hD = creneau[0];
                    LocalTime hF = creneau[1];

                    List<Affectation> affectations = planifierSlotFF(
                            seances, toutesLesSalles, jour, hD, hF);

                    for (Affectation a : affectations) {
                        CreneauEmploi c = new CreneauEmploi();
                        c.setEmploiDeTemps(edt);
                        c.setUe(a.seance.ue);
                        c.setEnseignant(a.enseignant);
                        c.setClasse(a.seance.classe);
                        c.setSalle(a.salle);
                        c.setTypeCours(TypeCours.CM);
                        c.setDate(jour);
                        c.setHeureDebut(hD);
                        c.setHeureFin(hF);
                        creneauEmploiRepository.save(c);

                        a.seance.sessionsRestantes--;
                        seancesPlanifiees++;
                    }
                }
            }
            jour = jour.plusDays(1);
        }

        // --- Recenser les séances non planifiées ---
        for (SeanceAPlanifier s : seances) {
            if (s.sessionsRestantes > 0) {
                avertissements.add(String.format(
                        "UE \"%s\" – Classe \"%s\" : %d séance(s) non planifiée(s).",
                        s.ue.getIntitule(), s.classe.getNom(), s.sessionsRestantes));
            }
        }

        log.info("Génération terminée : {}/{} séances, {} avertissements",
                seancesPlanifiees, totalSeances, avertissements.size());

        return new GenerationResult(edt, totalSeances, seancesPlanifiees, avertissements);
    }

    // ==================== FORD-FULKERSON PAR CRÉNEAU ====================

    /**
     * Applique Ford-Fulkerson sur un créneau horaire pour maximiser les affectations.
     *
     * Construction du réseau de flot :
     *
     *  Numérotation des nœuds :
     *    0                          = Source
     *    1 .. numUC                 = (UE_i, Classe_j) — paires valides pour ce créneau
     *    numUC+1 .. numUC+numT      = Enseignant_k_in  (entrée du nœud divisé)
     *    numUC+numT+1 .. +2*numT    = Enseignant_k_out (sortie du nœud divisé)
     *    suivants .. +numTC         = Nœuds (Enseignant_k, Classe_j) — liaison ens→salle
     *    suivants .. +numR          = Salle_r
     *    dernier                    = Sink
     */
    private List<Affectation> planifierSlotFF(
            List<SeanceAPlanifier> toutesSeances,
            List<Salle> toutesLesSalles,
            LocalDate date,
            LocalTime hD,
            LocalTime hF) {

        // Filtrer les séances : classe libre à ce créneau + sessions restantes
        List<SeanceAPlanifier> seances = toutesSeances.stream()
                .filter(s -> s.sessionsRestantes > 0)
                .filter(s -> !creneauEmploiRepository.existsConflitClasse(s.classe, date, hD, hF))
                .toList();
        if (seances.isEmpty()) return List.of();

        // Enseignants disponibles à ce créneau (dispo + pas encore assignés)
        List<Enseignant> enseignantsDispos = new ArrayList<>();
        Set<UUID> dejaVus = new HashSet<>();
        for (SeanceAPlanifier s : seances) {
            for (Enseignant e : s.enseignants) {
                if (!dejaVus.contains(e.getIdUser())) {
                    dejaVus.add(e.getIdUser());
                    if (plageHoraireRepository.isEnseignantDisponible(e, date, hD, hF) &&
                        !creneauEmploiRepository.existsConflitEnseignant(e, date, hD, hF)) {
                        enseignantsDispos.add(e);
                    }
                }
            }
        }
        if (enseignantsDispos.isEmpty()) return List.of();

        // Salles libres à ce créneau
        List<Salle> sallesLibres = toutesLesSalles.stream()
                .filter(r -> !creneauEmploiRepository.existsConflitSalle(r, date, hD, hF))
                .toList();

        int numUC = seances.size();
        int numT  = enseignantsDispos.size();
        int numR  = sallesLibres.size();

        // Nœuds (Enseignant_k, Classe_j) — un par combinaison valide
        // On les indexe dynamiquement ci-dessous.
        // Calcul des offsets :
        //   0           = Source
        //   1..numUC    = UC nodes
        //   nUC+1..+T   = T_in
        //   nUC+T+1..+2T= T_out
        //   nUC+2T+1..  = TC nodes (Enseignant×Classe paires valides)
        //   ..+numR     = Room nodes
        //   last        = Sink

        // Pré-construire les paires (Ens, Classe) valides pour ce créneau
        record TCPair(int teacherIdx, int ucIdx) {}
        List<TCPair> tcPairs = new ArrayList<>();
        for (int ucIdx = 0; ucIdx < seances.size(); ucIdx++) {
            SeanceAPlanifier s = seances.get(ucIdx);
            for (int tIdx = 0; tIdx < enseignantsDispos.size(); tIdx++) {
                Enseignant ens = enseignantsDispos.get(tIdx);
                if (s.enseignants.stream().anyMatch(e -> e.getIdUser().equals(ens.getIdUser()))) {
                    tcPairs.add(new TCPair(tIdx, ucIdx));
                }
            }
        }

        int offsetUC   = 1;
        int offsetTin  = offsetUC  + numUC;
        int offsetTout = offsetTin + numT;
        int offsetTC   = offsetTout + numT;
        int offsetR    = offsetTC  + tcPairs.size();
        int sink       = offsetR   + numR;
        int totalNodes = sink + 1;

        FordFulkerson ff = new FordFulkerson(totalNodes);

        // Source → UC (cap=1 : une séance par classe par créneau)
        for (int i = 0; i < numUC; i++) {
            ff.addEdge(0, offsetUC + i, 1);
        }

        // UC → T_in (cap=1 par paire valide)
        for (int tcIdx = 0; tcIdx < tcPairs.size(); tcIdx++) {
            TCPair tc = tcPairs.get(tcIdx);
            ff.addEdge(offsetUC + tc.ucIdx(), offsetTin + tc.teacherIdx(), 1);
        }

        // T_in → T_out (cap=1 : anti double-réservation enseignant)
        for (int tIdx = 0; tIdx < numT; tIdx++) {
            ff.addEdge(offsetTin + tIdx, offsetTout + tIdx, 1);
        }

        // T_out → TC (nœud de liaison pour conserver l'info classe → salle)
        for (int tcIdx = 0; tcIdx < tcPairs.size(); tcIdx++) {
            TCPair tc = tcPairs.get(tcIdx);
            ff.addEdge(offsetTout + tc.teacherIdx(), offsetTC + tcIdx, 1);
        }

        // TC → Salle (cap=1 si salle.capacite >= effectif classe)
        for (int tcIdx = 0; tcIdx < tcPairs.size(); tcIdx++) {
            TCPair tc = tcPairs.get(tcIdx);
            Classe classe  = seances.get(tc.ucIdx()).classe;
            int effectif   = classe.getEffectif() != null ? classe.getEffectif() : 0;
            for (int rIdx = 0; rIdx < numR; rIdx++) {
                Salle salle = sallesLibres.get(rIdx);
                if (salle.getCapacite() == null || salle.getCapacite() >= effectif) {
                    ff.addEdge(offsetTC + tcIdx, offsetR + rIdx, 1);
                }
            }
        }

        // Salle → Sink (cap=1 : anti double-réservation salle)
        for (int rIdx = 0; rIdx < numR; rIdx++) {
            ff.addEdge(offsetR + rIdx, sink, 1);
        }

        // ---- Calcul du flot maximum ----
        ff.maxFlow(0, sink);

        // ---- Extraction des affectations (remontée des chemins de flot) ----
        List<Affectation> result = new ArrayList<>();
        for (int ucIdx = 0; ucIdx < numUC; ucIdx++) {
            if (!ff.isEdgeUsed(0, offsetUC + ucIdx)) continue;

            // Trouver quel enseignant a été choisi
            int teacherIdx = -1;
            for (int tIdx = 0; tIdx < numT; tIdx++) {
                if (ff.isEdgeUsed(offsetUC + ucIdx, offsetTin + tIdx)) {
                    teacherIdx = tIdx;
                    break;
                }
            }
            if (teacherIdx == -1) continue;

            // Trouver le nœud TC correspondant
            int tcNodeIdx = -1;
            for (int tcIdx = 0; tcIdx < tcPairs.size(); tcIdx++) {
                TCPair tc = tcPairs.get(tcIdx);
                if (tc.teacherIdx() == teacherIdx && tc.ucIdx() == ucIdx
                        && ff.isEdgeUsed(offsetTout + teacherIdx, offsetTC + tcIdx)) {
                    tcNodeIdx = tcIdx;
                    break;
                }
            }
            if (tcNodeIdx == -1) continue;

            // Trouver quelle salle a été choisie
            int roomIdx = -1;
            for (int rIdx = 0; rIdx < numR; rIdx++) {
                if (ff.isEdgeUsed(offsetTC + tcNodeIdx, offsetR + rIdx)) {
                    roomIdx = rIdx;
                    break;
                }
            }

            result.add(new Affectation(
                    seances.get(ucIdx),
                    enseignantsDispos.get(teacherIdx),
                    roomIdx >= 0 ? sallesLibres.get(roomIdx) : null
            ));
        }

        return result;
    }

    // ==================== UTILITAIRES ====================

    private boolean estJourOuvre(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }

    private int calculerNombreSeances(UE ue) {
        if (ue.getDuree() == null || ue.getDuree() <= 0) return 1;
        return (int) Math.ceil(ue.getDuree() / 2.0);
    }

    // ==================== CLASSES INTERNES ====================

    static class SeanceAPlanifier {
        final UE ue;
        final Classe classe;
        final List<Enseignant> enseignants;
        int sessionsRestantes;

        SeanceAPlanifier(UE ue, Classe classe, List<Enseignant> enseignants, int sessions) {
            this.ue = ue;
            this.classe = classe;
            this.enseignants = enseignants;
            this.sessionsRestantes = sessions;
        }
    }

    private record Affectation(SeanceAPlanifier seance, Enseignant enseignant, Salle salle) {}

    public record GenerationResult(
            EmploiDeTemps emploiDeTemps,
            int totalSeances,
            int seancesPlanifiees,
            List<String> avertissements
    ) {}
}
