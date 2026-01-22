# ✅ Clarification: Logique de Durée UE et Cours

## 🎯 Logique Métier Clarifiée

### Distinction Importante

| Entité | Champ | Signification | Exemple |
|--------|-------|---------------|---------|
| **UE** | `duree` | Nombre d'heures **TOTAL** pour l'UE | 60h |
| **Cours** | `duree` | Nombre d'heures **RESTANTES** pour l'UE | 45h |

### Formule Clé
```
Heures effectuées = UE.duree - Cours.duree
Pourcentage d'avancement = (Heures effectuées / UE.duree) × 100
```

---

## 📝 Modifications Effectuées

### 1. **Commentaires Clarifiés**

#### Dans `UE.java`:
```java
private Integer duree; // Nombre d'heures TOTAL pour cette UE (ex: 60h)
```

#### Dans `Cours.java`:
```java
private Integer duree; // Nombre d'heures RESTANTES pour cette UE (ex: 45h restantes sur 60h total)
```

### 2. **Service `DureeService` Créé**

Un service complet pour gérer cette logique:

```java
@Service
public class DureeService {
    // Calculer les heures effectuées
    int calculerHeuresEffectuees(Cours cours);
    
    // Calculer le pourcentage d'avancement
    double calculerPourcentageAvancement(Cours cours);
    
    // Vérifier si terminé
    boolean estTermine(Cours cours);
    
    // Décrémenter les heures après une séance
    void decrementerHeures(Cours cours, int heures);
    
    // Calculer la durée d'une séance
    long calculerDureeSeance(LocalTime debut, LocalTime fin);
    
    // Valider la durée d'un cours
    void validerDureeCours(Cours cours);
    
    // Réinitialiser les heures
    void reinitialiserHeures(Cours cours);
    
    // Obtenir un résumé
    String obtenirResume(Cours cours);
}
```

---

## 💡 Exemples d'Utilisation

### 1. Créer une UE et un Cours
```java
// Créer l'UE avec durée totale
UE ue = new UE();
ue.setCode("INF101");
ue.setIntitule("Programmation Java");
ue.setDuree(60); // 60 heures TOTAL
ue.setSemestre(1);
ue.setStatut(StatutUE.ACTIF);
ueRepository.save(ue);

// Créer le cours avec heures restantes
Cours cours = new Cours();
cours.setIntitule("Cours Magistral - Java");
cours.setTypeCours(TypeCours.MAGISTRAL);
cours.setUe(ue);
cours.setDuree(60); // 60h restantes (début du semestre)
coursRepository.save(cours);
```

### 2. Calculer l'Avancement
```java
@Autowired
private DureeService dureeService;

// Obtenir l'avancement
Cours cours = coursRepository.findById(coursId).orElseThrow();

int heuresEffectuees = dureeService.calculerHeuresEffectuees(cours);
// Résultat: 15h (si 15h ont été faites)

double pourcentage = dureeService.calculerPourcentageAvancement(cours);
// Résultat: 25.0 (si 15h sur 60h)

String resume = dureeService.obtenirResume(cours);
// Résultat: "INF101: 15h effectuées / 60h total (25.0%) - 45h restantes"
```

### 3. Décrémenter Après une Séance
```java
// Après une séance de 2h
SeanceClasse seance = new SeanceClasse();
seance.setCours(cours);
seance.setHeureDebut(LocalTime.of(8, 0));
seance.setHeureFin(LocalTime.of(10, 0));
seanceRepository.save(seance);

// Décrémenter automatiquement
dureeService.decrementerApresSeance(seance);
// cours.duree passe de 60h à 58h
```

### 4. Afficher dans l'Interface
```html
<div th:with="heuresEffectuees=${ue.duree - cours.duree},
              pourcentage=${(heuresEffectuees * 100.0) / ue.duree}">
    
    <h5>[[${ue.code}]] - [[${ue.intitule}]]</h5>
    
    <!-- Barre de progression -->
    <div class="progress">
        <div class="progress-bar bg-success" 
             th:style="'width: ' + ${pourcentage} + '%'">
            [[${#numbers.formatDecimal(pourcentage, 1, 1)}]]%
        </div>
    </div>
    
    <!-- Détails -->
    <p class="text-muted">
        [[${heuresEffectuees}]]h / [[${ue.duree}]]h
        <br>
        <strong>[[${cours.duree}]]h restantes</strong>
    </p>
</div>
```

---

## 🔄 Workflow Complet

### Début du Semestre
```
UE.duree = 60h (total)
Cours.duree = 60h (rien n'a été fait)
Heures effectuées = 0h
Avancement = 0%
```

### Après 15h de Cours
```
UE.duree = 60h (total - ne change pas)
Cours.duree = 45h (60 - 15)
Heures effectuées = 15h
Avancement = 25%
```

### Après 30h de Cours
```
UE.duree = 60h (total - ne change pas)
Cours.duree = 30h (60 - 30)
Heures effectuées = 30h
Avancement = 50%
```

### Fin du Semestre
```
UE.duree = 60h (total - ne change pas)
Cours.duree = 0h (tout est fait)
Heures effectuées = 60h
Avancement = 100%
```

---

## ⚠️ Validations Importantes

### 1. Cours.duree ne peut pas dépasser UE.duree
```java
if (cours.getDuree() > cours.getUe().getDuree()) {
    throw new RuntimeException("Heures restantes > heures totales");
}
```

### 2. Cours.duree ne peut pas être négatif
```java
if (cours.getDuree() < 0) {
    throw new RuntimeException("Heures restantes négatives");
}
```

### 3. Initialisation d'un nouveau cours
```java
// Toujours initialiser avec la durée totale de l'UE
Cours cours = new Cours();
cours.setUe(ue);
cours.setDuree(ue.getDuree()); // Important!
```

---

## 📊 Cas d'Usage Avancés

### 1. Tableau de Bord Enseignant
```java
@GetMapping("/dashboard/enseignant")
public String dashboard(Model model) {
    List<Cours> mesCours = coursRepository.findByEnseignant(enseignant);
    
    List<Map<String, Object>> coursAvecAvancement = mesCours.stream()
        .map(cours -> {
            Map<String, Object> data = new HashMap<>();
            data.put("cours", cours);
            data.put("heuresEffectuees", dureeService.calculerHeuresEffectuees(cours));
            data.put("pourcentage", dureeService.calculerPourcentageAvancement(cours));
            data.put("estTermine", dureeService.estTermine(cours));
            return data;
        })
        .collect(Collectors.toList());
    
    model.addAttribute("cours", coursAvecAvancement);
    return "dashboard/enseignant";
}
```

### 2. Validation Avant Création de Séance
```java
@PostMapping("/seances/creer")
public ResponseEntity<?> creerSeance(@RequestBody SeanceRequest request) {
    Cours cours = coursRepository.findById(request.getCoursId()).orElseThrow();
    
    // Vérifier s'il reste des heures
    if (!dureeService.aDesHeuresRestantes(cours)) {
        return ResponseEntity.badRequest()
            .body("Toutes les heures ont été planifiées pour ce cours");
    }
    
    // Calculer la durée de la séance
    long dureeSeance = dureeService.calculerDureeSeance(
        request.getHeureDebut(), 
        request.getHeureFin()
    );
    
    // Vérifier qu'il reste assez d'heures
    if (dureeSeance > cours.getDuree()) {
        return ResponseEntity.badRequest()
            .body("Il ne reste que " + cours.getDuree() + "h à planifier");
    }
    
    // Créer la séance et décrémenter
    SeanceClasse seance = creerSeance(request);
    dureeService.decrementerApresSeance(seance);
    
    return ResponseEntity.ok(seance);
}
```

---

## ✅ Vérifications

- [x] Commentaires clarifiés dans `UE.java`
- [x] Commentaires clarifiés dans `Cours.java`
- [x] Service `DureeService` créé avec toutes les méthodes utiles
- [x] Compilation réussie ✅
- [x] Documentation complète créée
- [x] Exemples d'utilisation fournis

---

## 📚 Fichiers Créés/Modifiés

### Modifiés:
- `models/UE.java` - Commentaire clarifié
- `models/Cours.java` - Commentaire clarifié

### Créés:
- `service/DureeService.java` - Service pour gérer la logique de durée
- `LOGIQUE_DUREE_UE_COURS.md` - Documentation détaillée
- `CLARIFICATION_DUREE_FINALE.md` - Ce document

---

**Date:** 20 janvier 2026  
**Statut:** ✅ Clarification complète et service créé  
**Compilation:** ✅ BUILD SUCCESS  
**Importance:** 🔴 CRITIQUE - Logique métier fondamentale
