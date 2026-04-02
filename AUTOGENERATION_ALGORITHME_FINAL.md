# 🤖 Algorithme d'Auto-Génération d'Emploi du Temps - Documentation Complète

## 📋 Vue d'Ensemble

L'algorithme d'auto-génération crée automatiquement un emploi du temps pour une classe en respectant un ensemble de contraintes strictes. Il utilise une approche CSP (Constraint Satisfaction Problem) avec un algorithme glouton optimisé.

## ⚙️ Paramètres d'Entrée

- **Emploi du temps ID** : UUID de l'emploi du temps à générer
- **Semestre** : Numéro du semestre (1 ou 2)

## 🎯 Contraintes Respectées

### 1. Contraintes Temporelles
- ✅ **Horaires** : 8h-17h (9 heures par jour)
- ✅ **Pause déjeuner** : 12h-13h (aucune séance)
- ✅ **Jours** : Lundi à Vendredi pour les cours
- ✅ **Durée des séances** : 1 heure chacune

### 2. Contraintes de Cours
- ✅ **Un cours = un seul jour par semaine**
- ✅ **Séances successives** : Pas de trou entre les séances d'un même cours
- ✅ **Durée par jour** : Respecte `dureeSeanceParJour` (nombre de séances de 1h)
- ✅ **CM et TP** : Ne peuvent pas être programmés la même semaine

### 3. Contraintes de Ressources
- ✅ **Enseignant** : Doit être assigné et disponible
- ✅ **Salle** : Doit être disponible pour le créneau
- ✅ **Classe** : Ne peut avoir qu'un cours à la fois

### 4. Remplissage TPE
- ✅ **Toutes les cases vides** sont remplies avec TPE
- ✅ **Jours** : Lundi à Samedi (6 jours)
- ✅ **Sans salle** : Les séances TPE n'ont pas de salle assignée

## 🔄 Flux de l'Algorithme

### Phase 1 : Génération des Candidats

```
1. Récupérer tous les cours de la classe pour le semestre
2. Pour chaque cours :
   a. Vérifier volume restant > 0
   b. Vérifier dureeSeanceParJour définie
   c. Vérifier enseignant assigné
   d. Vérifier enseignant a des disponibilités
   e. Vérifier qu'il existe des salles
   f. Calculer score de priorité
3. Retourner liste de candidats
```

### Phase 2 : Tri par Priorité

```
Score de priorité = 
  + (Volume restant / Volume total) × 100  [Urgence]
  + (20 - dureeSeanceParJour) × 5          [Facilité de placement]

Tri décroissant par score
```

### Phase 3 : Placement des Cours

Pour chaque cours candidat (du plus prioritaire au moins prioritaire) :

```
1. Vérifier que le cours n'est pas déjà programmé cette semaine
2. Vérifier qu'aucun autre cours de la même UE n'est programmé cette semaine
3. Pour chaque jour (Lundi à Vendredi) :
   Pour chaque créneau de départ (8h ou 13h) :
     Essayer de placer les séances successives
```

### Phase 4 : Placement des Séances Successives

```
Entrée : Cours, Date, Heure de début, Durée par jour

1. volumeTraite = 0
2. currentHeure = heureDebut

3. Tant que volumeTraite < dureeSeanceParJour ET volumeTraite < volumeRestant :
   
   a. heureFin = currentHeure + 1h
   
   b. Si heureFin > 17h :
      ARRÊTER (fin de journée)
   
   c. Si chevauche pause déjeuner (12h-13h) :
      currentHeure = 13h
      CONTINUER
   
   d. Si créneau occupé (classe, enseignant ou salle) :
      ARRÊTER (séances doivent être successives)
   
   e. Créer séance de 1h (currentHeure → heureFin)
   f. volumeTraite += 1
   g. currentHeure = heureFin

4. Si volumeTraite < dureeSeanceParJour ET volumeTraite < volumeRestant :
   ÉCHEC (pas assez de place)

5. Trouver une salle disponible pour TOUTES les séances

6. Sauvegarder toutes les séances
7. Mettre à jour volumeRestant du cours
8. SUCCÈS
```

### Phase 5 : Remplissage TPE

```
1. Créer ou récupérer un cours TPE générique pour la classe

2. Pour chaque jour (Lundi à Samedi = 6 jours) :
   Pour chaque heure (8h à 17h) :
     
     a. Si c'est la pause déjeuner (12h-13h) ET jour < Samedi :
        Passer à 13h
     
     b. Si créneau libre :
        Créer séance TPE de 1h
        Sans salle
        Sans enseignant
```

## 📊 Exemple Concret

### Données d'Entrée

**Cours** : "Technologie Web - CM"
- `dureeTotal` : 32h
- `dureeRestante` : 32h
- `dureeSeanceParJour` : 4h
- `enseignant` : PESSA

### Semaine 1 - Génération

**Lundi** :
```
08h-09h : Technologie Web - CM (séance 1)
09h-10h : Technologie Web - CM (séance 2)
10h-11h : Technologie Web - CM (séance 3)
11h-12h : Technologie Web - CM (séance 4)
12h-13h : [PAUSE DÉJEUNER]
13h-14h : TPE
14h-15h : TPE
15h-16h : TPE
16h-17h : TPE
```

**Résultat** :
- 4 séances de 1h successives = 4h programmées
- Volume restant : 32h - 4h = 28h
- 4 séances TPE ajoutées

### Semaine 2 - Génération

Le cours "Technologie Web - CM" sera programmé un autre jour (ex: Mardi) car il ne peut être qu'un seul jour par semaine.

## 🚀 Optimisations Appliquées

### 1. Génération Unique des Candidats
**Avant** : Régénération à chaque itération (100 fois)
**Après** : Génération une seule fois au début

**Gain** : ~95% de réduction du temps

### 2. Créneaux Optimisés
**Avant** : Essai de toutes les heures (8h, 9h, 10h, 11h, 13h, 14h, 15h, 16h)
**Après** : Essai uniquement de 8h et 13h

**Gain** : ~75% de réduction des tentatives

### 3. Score de Priorité Simplifié
**Avant** : Calcul coûteux avec comptage de créneaux compatibles
**Après** : Calcul simple basé sur volume restant et durée

**Gain** : ~90% de réduction du temps de calcul

### 4. Pas de Backtracking
**Avant** : Boucle avec backtracking (100 tentatives max)
**Après** : Une seule passe sur tous les cours

**Gain** : Temps de génération prévisible et constant

## 📈 Performance

### Temps de Génération Estimé

| Nombre de cours | Avant | Après |
|----------------|-------|-------|
| 10 cours       | ~30s  | ~2s   |
| 20 cours       | ~90s  | ~4s   |
| 30 cours       | ~180s | ~6s   |

**Amélioration** : ~15x plus rapide

## 🔍 Logs de Diagnostic

### Logs Générés

```
=== DEBUT GENERATION CANDIDATS ===
Classe : INGE4 ISI FR
Semestre : 1
Cours trouvés pour la classe : 46
Cours du semestre 1 : 23
Analyse cours : Technologie et programmation Web - CM | Enseignant : PESSA | DureeRestante : 32 | DureeSeance : 4
  -> ACCEPTÉ comme candidat (score: 150.5)
...
=== FIN GENERATION CANDIDATS : 15 candidats ===

Tentative de placement de 15 cours
✓ Cours Technologie et programmation Web - CM placé avec succès
✓ 4 séances de 1h successives créées pour Technologie et programmation Web - CM (volume traité: 4h sur le jour 2026-03-31)
...

=== REMPLISSAGE DES TROUS AVEC TPE ===
TPE ajouté : 2026-03-31 de 13:00 à 14:00 (sans salle)
...
Séances TPE ajoutées : 45
```

## ❌ Cas d'Échec

### Cours Non Placé

Un cours peut ne pas être placé si :

1. **Pas d'enseignant assigné**
   ```
   Analyse cours : Développement Mobile - CM | Enseignant : NULL
     -> REJETÉ : Pas d'enseignant assigné
   ```

2. **Pas de disponibilité enseignant**
   ```
   -> REJETÉ : Enseignant MELATEGUIA n'a pas de disponibilité
   ```

3. **Pas de salle disponible**
   ```
   -> REJETÉ : Aucune salle disponible dans le système
   ```

4. **Cours de la même UE déjà programmé**
   ```
   Cours Technologie Web - TP ne peut pas être programmé : 
   Technologie Web - CM (même UE) est déjà programmé cette semaine
   ```

5. **Pas assez de créneaux successifs**
   ```
   Impossible de placer 6 séances successives pour Architecture SI - CM
   (seulement 4 placées)
   ```

## 🎓 Résultat Final

### Structure de l'Emploi du Temps

```
Lundi à Vendredi : 8h-17h
- Cours programmés avec séances de 1h successives
- Pause déjeuner 12h-13h
- Trous remplis avec TPE

Samedi : 8h-17h
- Uniquement TPE (si aucun cours programmé)
```

### Exemple Complet

**Lundi** :
```
08h-09h : Technologie Web - CM
09h-10h : Technologie Web - CM
10h-11h : Technologie Web - CM
11h-12h : Technologie Web - CM
12h-13h : [PAUSE DÉJEUNER]
13h-14h : TPE
14h-15h : TPE
15h-16h : TPE
16h-17h : TPE
```

**Mardi** :
```
08h-09h : Développement Mobile - CM
09h-10h : Développement Mobile - CM
10h-11h : Développement Mobile - CM
11h-12h : Développement Mobile - CM
12h-13h : [PAUSE DÉJEUNER]
13h-14h : TPE
14h-15h : TPE
15h-16h : TPE
16h-17h : TPE
```

**Samedi** :
```
08h-09h : TPE
09h-10h : TPE
10h-11h : TPE
11h-12h : TPE
12h-13h : TPE (pas de pause le samedi)
13h-14h : TPE
14h-15h : TPE
15h-16h : TPE
16h-17h : TPE
```

## 🔧 Configuration Requise

### Données Nécessaires

1. **Cours** :
   - `dureeTotal` : Volume horaire total
   - `dureeRestante` : Heures restant à planifier
   - `dureeSeanceParJour` : Nombre d'heures par jour
   - `enseignant` : Enseignant assigné
   - `ue` : Unité d'enseignement

2. **Enseignants** :
   - Disponibilités définies pour la période

3. **Salles** :
   - Au moins une salle disponible

### Prérequis

- ✅ Tous les cours ont un enseignant assigné
- ✅ Les enseignants ont des disponibilités
- ✅ Des salles existent dans le système
- ✅ `dureeSeanceParJour` est définie pour chaque cours

## 📞 Dépannage

### Problème : 0 séances ajoutées

**Causes possibles** :
1. Pas de salles dans le système
2. Enseignants non assignés aux cours
3. Pas de disponibilités pour la période
4. `dureeSeanceParJour` non définie

**Solution** : Consulter les logs détaillés pour identifier la cause exacte

### Problème : Génération trop lente

**Causes possibles** :
1. Trop de cours à placer
2. Contraintes trop restrictives

**Solution** : L'algorithme optimisé devrait prendre moins de 10 secondes pour 30 cours

---

**Version** : 2.0 (Optimisée)
**Date** : 2026-04-02
**Auteur** : Système WeSchedule
