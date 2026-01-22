# Guide de Test - Mode Manuel Emploi du Temps

## Date: 20 Janvier 2026

## Objectif

Vérifier que toutes les corrections apportées au mode manuel fonctionnent correctement.

## Prérequis

1. Application démarrée sur `http://localhost:8080`
2. Base de données initialisée avec des données de test
3. Navigateur avec console développeur (F12)

## Étapes de Test

### 1. Accès à la Page Mode Manuel

1. Se connecter en tant qu'administrateur
2. Aller dans **Emplois du Temps**
3. Sélectionner une classe
4. Cliquer sur **Mode Manuel** pour une semaine

**Vérifications**:
- ✅ La page se charge sans erreur
- ✅ La grille s'affiche avec 12 créneaux horaires (8h-20h)
- ✅ Les 7 jours de la semaine sont affichés

**Console (F12)**:
```
Données chargées: {cours: X, enseignants: Y, salles: Z}
DOM chargé, initialisation...
Grille initialisée avec 12 créneaux
Initialisation terminée
```

### 2. Test du Filtrage des Cours

1. Cliquer sur un créneau vide
2. Observer la liste des cours dans le modal

**Vérifications**:
- ✅ Seuls les cours de la classe sont affichés
- ✅ Les cours sont liés aux UEs de la classe
- ✅ Aucun cours d'autres classes n'apparaît

### 3. Test du Filtrage des Enseignants

1. Dans le modal, sélectionner un cours
2. Observer la liste des enseignants

**Vérifications**:
- ✅ La liste des enseignants se met à jour automatiquement
- ✅ Seuls les enseignants qui enseignent l'UE du cours sont affichés
- ✅ Si aucun enseignant n'enseigne l'UE, la liste est vide (sauf "Aucun")

**Console**:
```
Enseignants filtrés: X pour le cours [UUID]
```

### 4. Test du Filtrage des Salles

1. Ajouter une première séance avec une salle (ex: Salle A101)
2. Cliquer sur un autre créneau **au même horaire** (même jour, même heure)
3. Observer la liste des salles

**Vérifications**:
- ✅ La salle A101 n'apparaît plus dans la liste
- ✅ Les autres salles sont disponibles
- ✅ Si on modifie la première séance, la salle A101 reste disponible

### 5. Test de l'Ajout de Séances

1. Ajouter plusieurs séances:
   - Lundi 8h-9h: Cours 1, Enseignant 1, Salle A101
   - Lundi 9h-10h: Cours 2, Enseignant 2, Salle A102
   - Mardi 8h-9h: Cours 3, Enseignant 1, Salle A103
   - Mardi 8h-9h: Cours 4, Enseignant 3, Salle A104 (même horaire, salle différente)

**Vérifications**:
- ✅ Chaque séance s'affiche dans la grille
- ✅ Le nom du cours apparaît dans la cellule
- ✅ La cellule change de couleur (vert)
- ✅ On peut cliquer sur une séance pour la modifier

### 6. Test de la Modification de Séance

1. Cliquer sur une séance existante
2. Modifier le cours, l'enseignant ou la salle
3. Valider

**Vérifications**:
- ✅ Le modal s'ouvre avec les données de la séance
- ✅ Le bouton "Supprimer" est visible
- ✅ Les modifications sont appliquées dans la grille
- ✅ Les filtres fonctionnent toujours

### 7. Test de la Suppression de Séance

1. Cliquer sur une séance existante
2. Cliquer sur "Supprimer"

**Vérifications**:
- ✅ La séance disparaît de la grille
- ✅ La cellule redevient vide
- ✅ La salle redevient disponible pour ce créneau

### 8. Test de l'Enregistrement

1. Ajouter au moins 3 séances
2. Ouvrir la console (F12)
3. Cliquer sur **Enregistrer l'emploi du temps**

**Vérifications**:
- ✅ Un message de chargement apparaît
- ✅ La console affiche les logs détaillés
- ✅ Un message de succès s'affiche
- ✅ Redirection vers la page de l'emploi du temps

**Console**:
```
=== DÉBUT ENREGISTREMENT ===
Nombre de séances: 3
Séances: {...}
Création de l'emploi du temps...
Réponse création emploi du temps: 200
Emploi du temps créé, ajout des séances...
Ajout séance: 0-0 2026-01-20 08:00-09:00
Réponse ajout séance 0-0: 200
Résultat séance 0-0: {success: true, message: "Séance ajoutée avec succès"}
...
Toutes les séances ajoutées avec succès
```

### 9. Vérification des Réservations

1. Aller dans la page des **Salles** ou **Réservations**
2. Vérifier les réservations créées

**Vérifications**:
- ✅ Une réservation existe pour chaque séance avec salle
- ✅ Le statut est **CONFIRMEE**
- ✅ La salle, le cours et l'enseignant sont corrects

### 10. Test des Conflits (Backend)

1. Essayer d'ajouter deux séances avec:
   - Même enseignant, même horaire, salles différentes
   - Ou même salle, même horaire, enseignants différents

**Vérifications**:
- ✅ Le backend détecte le conflit
- ✅ Un message d'erreur s'affiche
- ✅ La séance n'est pas créée

## Cas d'Erreur à Tester

### Erreur 1: Aucune séance ajoutée

1. Cliquer sur "Enregistrer" sans ajouter de séance

**Résultat attendu**:
- ❌ Message d'erreur: "Veuillez ajouter au moins une séance"

### Erreur 2: Cours non sélectionné

1. Ouvrir le modal d'ajout
2. Ne pas sélectionner de cours
3. Cliquer sur "Valider"

**Résultat attendu**:
- ❌ Message d'erreur: "Veuillez sélectionner un cours"

### Erreur 3: Conflit enseignant

1. Ajouter une séance: Lundi 8h-9h, Enseignant A
2. Enregistrer
3. Créer un nouvel emploi du temps pour une autre classe
4. Ajouter une séance: Lundi 8h-9h, Enseignant A

**Résultat attendu**:
- ❌ Message d'erreur: "L'enseignant a déjà une séance à cet horaire"

### Erreur 4: Conflit salle

1. Ajouter une séance: Lundi 8h-9h, Salle A101
2. Enregistrer
3. Créer un nouvel emploi du temps pour une autre classe
4. Ajouter une séance: Lundi 8h-9h, Salle A101

**Résultat attendu**:
- ❌ Message d'erreur: "La salle est déjà occupée à cet horaire"

## Checklist Finale

- [ ] La grille s'affiche correctement
- [ ] Les cours sont filtrés par classe
- [ ] Les enseignants sont filtrés par cours
- [ ] Les salles sont filtrées par disponibilité
- [ ] L'ajout de séances fonctionne
- [ ] La modification de séances fonctionne
- [ ] La suppression de séances fonctionne
- [ ] L'enregistrement fonctionne
- [ ] Les réservations sont créées
- [ ] Les conflits sont détectés
- [ ] Les logs de débogage sont présents
- [ ] Aucune erreur dans la console

## Problèmes Connus

Aucun problème connu après les corrections du 20 janvier 2026.

## Support

En cas de problème:
1. Ouvrir la console du navigateur (F12)
2. Copier les logs et messages d'erreur
3. Vérifier les logs du serveur Spring Boot
4. Consulter `AMELIORATIONS_MODE_MANUEL.md` pour plus de détails

## Améliorations Futures

- [ ] Afficher les disponibilités des enseignants
- [ ] Vérifier la capacité des salles vs effectif de la classe
- [ ] Vérifier le type de salle vs type de cours
- [ ] Export PDF de l'emploi du temps
- [ ] Drag & drop pour déplacer les séances
- [ ] Vue mensuelle en plus de la vue hebdomadaire
