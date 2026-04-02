# 🔄 Fonctionnement de l'Auto-Génération Globale

## 📋 Vue d'Ensemble

L'auto-génération globale permet de générer automatiquement les emplois du temps pour plusieurs classes en une seule opération, avec deux modes possibles:
1. **Mode Toutes les Classes**: Génère pour toutes les classes du système
2. **Mode Sélection**: Génère uniquement pour les classes cochées

---

## 🎯 Interface Utilisateur

### Composants du Header

```
┌─────────────────────────────────────────────────────────────┐
│  Emplois du Temps                                           │
│  Gérez les plannings hebdomadaires par classe              │
│                                                             │
│  [←] Semaine 14 [→]  [Semestre 1 ▼]  [Auto-générer Tout]  │
│      01/04 - 07/04/2026                                     │
└─────────────────────────────────────────────────────────────┘
```

**Éléments:**
1. **Sélecteur de Semaine**: Boutons ← → pour naviguer entre les semaines
2. **Affichage des Dates**: Montre la plage de dates (Lundi - Dimanche)
3. **Sélecteur de Semestre**: Dropdown (Semestre 1 ou 2)
4. **Bouton "Auto-générer Tout"**: Lance la génération globale

### Cartes de Classes

```
┌─────────────────────────────────────┐
│ ☑️                                   │
│ 📅  INGE4 ISI FR                    │
│     IUSJC · Informatique            │
│     [INGE4] [Français]              │
│                                     │
│     👥 45 étudiants                 │
│     [Semestre 1 ▼] [Auto-générer]  │
└─────────────────────────────────────┘
```

**Éléments:**
- **Checkbox** (coin supérieur droit): Pour sélectionner la classe
- **Sélecteur de Semestre Individuel**: Pour génération individuelle
- **Bouton "Auto-générer"**: Pour génération individuelle

---

## 🔄 Flux de l'Auto-Génération Globale

### Étape 1: Clic sur "Auto-générer Tout"

```javascript
function autoGenererGlobal() {
    // 1. Récupérer les paramètres
    const semestreGlobal = document.getElementById('semestreGlobal').value;
    const semaineGlobale = getSemaineGlobale();  // Ex: 14
    const anneeGlobale = getAnneeGlobale();      // Ex: 2026
    
    // 2. Vérifier les checkboxes cochées
    const checkboxes = document.querySelectorAll('.classe-checkbox:checked');
    
    // 3. Déterminer le mode
    if (checkboxes.length === 0) {
        // MODE: Toutes les classes
        classesToGenerate = allClasses.map(c => c.idClasse);
    } else {
        // MODE: Classes sélectionnées
        classesToGenerate = Array.from(checkboxes).map(cb => cb.dataset.classeId);
    }
    
    // 4. Afficher modal de confirmation
    showConfirmationModal(confirmMessage, onConfirm);
}
```

### Étape 2: Modal de Confirmation

#### Cas 1: Aucune Classe Sélectionnée

```
┌─────────────────────────────────────────────┐
│ 🪄 Confirmation Auto-Génération        [×]  │
├─────────────────────────────────────────────┤
│ ⚠️ Aucune classe sélectionnée              │
│                                             │
│ Voulez-vous générer l'emploi du temps      │
│ pour TOUTES les classes (46 classes) ?     │
│                                             │
│ 📅 Semaine : 14                            │
│ 📚 Semestre : 1                            │
│ ⏱️ Durée estimée : 4 minutes               │
├─────────────────────────────────────────────┤
│              [❌ Annuler]  [✅ Confirmer]   │
└─────────────────────────────────────────────┘
```

#### Cas 2: Classes Sélectionnées

```
┌─────────────────────────────────────────────┐
│ 🪄 Confirmation Auto-Génération        [×]  │
├─────────────────────────────────────────────┤
│ Voulez-vous générer l'emploi du temps      │
│ pour 5 classe(s) sélectionnée(s) ?         │
│                                             │
│ 📅 Semaine : 14                            │
│ 📚 Semestre : 1                            │
│ ⏱️ Durée estimée : 1 minutes               │
├─────────────────────────────────────────────┤
│              [❌ Annuler]  [✅ Confirmer]   │
└─────────────────────────────────────────────┘
```

**Calcul de la Durée Estimée:**
```javascript
// ~5 secondes par classe
const dureeMinutes = Math.ceil(nombreClasses * 5 / 60);
```

### Étape 3: Exécution de la Génération

```javascript
async function executeGeneration(classesToGenerate, semaine, annee, semestre) {
    // 1. Afficher modal de progression
    showProgressModal(classesToGenerate.length);
    
    let successCount = 0;
    let errorCount = 0;
    const results = [];
    
    // 2. Boucle séquentielle sur chaque classe
    for (let i = 0; i < classesToGenerate.length; i++) {
        const classeId = classesToGenerate[i];
        const classe = allClasses.find(c => c.idClasse === classeId);
        
        // 3. Mettre à jour la progression
        updateProgressModal(i + 1, classesToGenerate.length, classe?.nom);
        
        try {
            // 4. Appeler l'API pour cette classe
            const result = await autoGenererClasse(classeId, semestre, semaine, annee);
            
            if (result.success) {
                successCount++;
                results.push({
                    classe: classe?.nom,
                    success: true,
                    seancesAjoutees: result.seancesAjoutees,
                    seancesTPE: result.seancesTPE
                });
            } else {
                errorCount++;
                results.push({
                    classe: classe?.nom,
                    success: false,
                    message: result.message
                });
            }
        } catch (error) {
            errorCount++;
            results.push({
                classe: classe?.nom,
                success: false,
                message: error.message
            });
        }
    }
    
    // 5. Masquer modal de progression
    hideProgressModal();
    
    // 6. Afficher modal de résultats
    showResultsModal(successCount, errorCount, results);
}
```

### Étape 4: Modal de Progression

```
┌─────────────────────────────────────────────┐
│ 🪄 Génération en cours...                   │
├─────────────────────────────────────────────┤
│              🔄 (spinner)                   │
│                                             │
│     Génération 15/46 : INGE3 ISI FR        │
│                                             │
│ ████████████░░░░░░░░░░░░░░░░░░░░ 32%      │
└─────────────────────────────────────────────┘
```

**Mise à jour en temps réel:**
- Texte: "Génération X/Y : Nom de la classe"
- Barre de progression: (X/Y) × 100%

### Étape 5: Appel API pour Chaque Classe

```javascript
async function autoGenererClasse(classeId, semestre, semaine, annee) {
    const response = await fetch('/admin/emploi-du-temps/auto-generer', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `classeId=${classeId}&semaine=${semaine}&annee=${annee}&semestre=${semestre}`
    });
    
    return await response.json();
}
```

**Endpoint Backend:** `POST /admin/emploi-du-temps/auto-generer`

**Paramètres:**
- `classeId`: UUID de la classe
- `semaine`: Numéro de semaine (1-52)
- `annee`: Année (ex: 2026)
- `semestre`: Semestre (1 ou 2)

**Réponse JSON:**
```json
{
  "success": true,
  "message": "Génération terminée avec succès",
  "seancesAjoutees": 15,
  "seancesEchouees": 2,
  "seancesTPE": 45
}
```

### Étape 6: Modal de Résultats

```
┌─────────────────────────────────────────────┐
│ ✅ Génération terminée                 [×]  │
├─────────────────────────────────────────────┤
│ ℹ️ 40 classe(s) générée(s) avec succès     │
│    6 classe(s) en erreur                   │
│                                             │
│ ✅ Succès                                   │
│ • ✓ INGE4 ISI FR (15 cours, 45 TPE)       │
│ • ✓ INGE3 ISI FR (12 cours, 48 TPE)       │
│ • ✓ INGE2 ISI FR (14 cours, 46 TPE)       │
│ ...                                         │
│                                             │
│ ❌ Erreurs                                  │
│ • ✗ INGE1 ISI FR (Pas d'enseignant)       │
│ • ✗ MASTER1 IA (Aucune salle disponible)  │
│ ...                                         │
├─────────────────────────────────────────────┤
│                          [Fermer]           │
└─────────────────────────────────────────────┘
```

---

## 🔧 Traitement Backend

### Controller: AdminEmploiDuTempsController

```java
@PostMapping("/auto-generer")
@ResponseBody
public Map<String, Object> autoGenererEmploiDuTemps(
        @RequestParam UUID classeId,
        @RequestParam Integer semaine,
        @RequestParam Integer annee,
        @RequestParam(required = false, defaultValue = "1") Integer semestre) {
    
    try {
        // 1. Trouver ou créer l'emploi du temps pour cette semaine
        EmploiDuTempsClasse emploiDuTemps = 
            emploiDuTempsService.getEmploiDuTempsParSemaine(classeId, semaine, annee);
        
        if (emploiDuTemps == null) {
            // Créer un nouvel emploi du temps
            LocalDate lundi = calculerLundiDeLaSemaine(semaine, annee);
            emploiDuTemps = emploiDuTempsService.creerEmploiDuTemps(classeId, lundi);
        }
        
        // 2. Lancer la génération automatique
        Map<String, Object> resultat = 
            autoGenerationService.genererEmploiDuTemps(emploiDuTemps.getId(), semestre);
        
        return resultat;
        
    } catch (Exception e) {
        return Map.of(
            "success", false,
            "message", "Erreur: " + e.getMessage()
        );
    }
}
```

### Service: AutoGenerationEmploiDuTempsService

```java
public Map<String, Object> genererEmploiDuTemps(UUID emploiDuTempsId, Integer semestre) {
    // 1. Récupérer l'emploi du temps
    EmploiDuTempsClasse emploiDuTemps = emploiDuTempsRepository.findById(emploiDuTempsId);
    
    // 2. Générer les candidats (cours à placer)
    List<SeanceCandidate> candidates = genererCandidats(emploiDuTemps, semestre);
    
    // 3. Trier par priorité
    candidates.sort(Comparator.comparingDouble(SeanceCandidate::getPriorityScore).reversed());
    
    // 4. Placer chaque cours
    int seancesAjoutees = 0;
    int seancesEchouees = 0;
    
    for (SeanceCandidate candidate : candidates) {
        if (essayerAffecterSeance(emploiDuTemps, candidate)) {
            seancesAjoutees++;
        } else {
            seancesEchouees++;
        }
    }
    
    // 5. Remplir les trous avec TPE
    int seancesTPE = remplirTrousAvecTPE(emploiDuTemps);
    
    // 6. Retourner le résultat
    return Map.of(
        "success", true,
        "message", "Génération terminée avec succès",
        "seancesAjoutees", seancesAjoutees,
        "seancesEchouees", seancesEchouees,
        "seancesTPE", seancesTPE
    );
}
```

---

## ⚙️ Caractéristiques Techniques

### 1. Génération Séquentielle

**Pourquoi séquentiel et non parallèle?**
- Évite les conflits de ressources (salles, enseignants)
- Simplifie la gestion des erreurs
- Permet un feedback en temps réel
- Évite la surcharge du serveur

**Temps de génération:**
- ~4-6 secondes par classe
- 10 classes: ~1 minute
- 46 classes: ~4 minutes

### 2. Gestion des Erreurs

**Types d'erreurs possibles:**
1. **Pas d'enseignant assigné**: Cours rejeté lors de la génération des candidats
2. **Pas de disponibilité**: Cours rejeté lors de la génération des candidats
3. **Pas de salle disponible**: Cours rejeté lors de la génération des candidats
4. **Contraintes non respectées**: Cours non placé (CM et TP même semaine, etc.)
5. **Erreur serveur**: Exception capturée et retournée

**Comportement:**
- Une erreur sur une classe n'arrête pas la génération des autres
- Chaque erreur est enregistrée dans les résultats
- Le modal final affiche toutes les erreurs

### 3. Calcul de la Semaine

```javascript
// Variables globales
let currentWeekNumber = 14;  // Numéro de semaine
let currentYear = 2026;      // Année

// Calcul du lundi de la semaine
function updateSemaineDisplay() {
    const firstDayOfYear = new Date(currentYear, 0, 1);
    const daysOffset = (currentWeekNumber - 1) * 7;
    const targetDate = new Date(firstDayOfYear.getTime() + daysOffset * 24 * 60 * 60 * 1000);
    
    // Trouver le lundi
    const dayOfWeek = targetDate.getDay();
    const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
    const monday = new Date(targetDate);
    monday.setDate(targetDate.getDate() + diff);
    
    // Calculer le dimanche
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    
    // Afficher: "01/04 - 07/04/2026"
}
```

### 4. Sélection des Classes

```javascript
// Compteur de sélection
function updateSelectionCount() {
    const checked = document.querySelectorAll('.classe-checkbox:checked').length;
    const total = document.querySelectorAll('.classe-checkbox').length;
    
    if (checked > 0) {
        // "3 classe(s) sélectionnée(s) sur 46"
        document.getElementById('countInfo').textContent = 
            `${checked} classe(s) sélectionnée(s) sur ${total}`;
    } else {
        // "46 classe(s)"
        document.getElementById('countInfo').textContent = `${total} classe(s)`;
    }
}
```

---

## 📊 Exemple Complet

### Scénario: Générer pour 3 Classes Sélectionnées

**1. État Initial**
- Semaine 14 (01/04 - 07/04/2026)
- Semestre 1
- 3 classes cochées: INGE4 ISI FR, INGE3 ISI FR, INGE2 ISI FR

**2. Clic sur "Auto-générer Tout"**
```
Modal de confirmation:
- "Générer pour 3 classe(s) sélectionnée(s) ?"
- Semaine: 14
- Semestre: 1
- Durée estimée: 1 minute
```

**3. Confirmation → Génération**
```
Modal de progression:
- Génération 1/3 : INGE4 ISI FR [████░░░░░░ 33%]
- Génération 2/3 : INGE3 ISI FR [████████░░ 66%]
- Génération 3/3 : INGE2 ISI FR [██████████ 100%]
```

**4. Résultats**
```
Modal de résultats:
✅ 3 classe(s) générée(s) avec succès
❌ 0 classe(s) en erreur

Succès:
• ✓ INGE4 ISI FR (15 cours, 45 TPE)
• ✓ INGE3 ISI FR (12 cours, 48 TPE)
• ✓ INGE2 ISI FR (14 cours, 46 TPE)
```

**5. Résultat dans la Base de Données**

Pour chaque classe, un emploi du temps est créé avec:
- Date de début: Lundi 01/04/2026
- Séances de cours (1h chacune, successives)
- Séances TPE (remplissent tous les trous)
- Salles assignées (sauf TPE)
- Enseignants assignés (sauf TPE)

---

## 🎯 Avantages de cette Approche

### 1. Flexibilité
- Génération globale ou sélective
- Choix de la semaine et du semestre
- Navigation facile entre les semaines

### 2. Feedback Utilisateur
- Modal de confirmation claire
- Progression en temps réel
- Résultats détaillés avec succès/erreurs

### 3. Robustesse
- Gestion des erreurs par classe
- Une erreur n'arrête pas tout
- Messages d'erreur explicites

### 4. Performance
- Génération optimisée (~5s par classe)
- Feedback visuel pendant l'attente
- Pas de blocage de l'interface

---

## 🔮 Améliorations Futures Possibles

### 1. Génération Parallèle
```javascript
// Au lieu de séquentiel
for (let i = 0; i < classes.length; i++) {
    await generer(classes[i]);
}

// Parallèle (plus rapide)
await Promise.all(classes.map(c => generer(c)));
```

**Avantages:**
- 3-5x plus rapide
- Meilleure utilisation des ressources serveur

**Inconvénients:**
- Risque de conflits de ressources
- Progression moins précise

### 2. WebSockets pour Progression Temps Réel
```javascript
// Connexion WebSocket
const ws = new WebSocket('/ws/generation');

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    updateProgressModal(data.current, data.total, data.className);
};
```

### 3. Annulation en Cours
```javascript
let generationAborted = false;

function cancelGeneration() {
    generationAborted = true;
}

// Dans la boucle
if (generationAborted) break;
```

### 4. Sauvegarde des Préférences
```javascript
// Sauvegarder la semaine/semestre sélectionné
localStorage.setItem('lastWeek', currentWeekNumber);
localStorage.setItem('lastSemester', semestreGlobal);
```

---

**Version**: 1.0  
**Date**: 2026-04-02  
**Auteur**: Système WeSchedule
