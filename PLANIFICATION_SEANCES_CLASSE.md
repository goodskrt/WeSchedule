# Système de Planification des Séances de Classe

## 🎯 Objectif

Créer un système intelligent pour planifier des séances de classe en tenant compte de:
1. **Disponibilités des enseignants**
2. **Cours avec UE actives**
3. **Heures restantes** (cours.duree < ue.duree)
4. **Classe spécifique**

---

## 📋 Architecture

### 1. Service `PlanificationSeanceService`

Service principal pour gérer la planification intelligente des séances.

#### Méthodes Principales:

```java
// Obtenir les cours planifiables pour une classe
List<CoursPlanifiable> getCoursPlanifiables(UUID classeId);

// Obtenir les créneaux disponibles sur une période
List<CreneauDisponible> getCreneauxDisponibles(
    UUID classeId, LocalDate dateDebut, LocalDate dateFin);

// Créer une séance depuis un créneau disponible
SeanceClasse creerSeanceDepuisCreneau(
    UUID emploiDuTempsId, UUID coursId, UUID enseignantId,
    UUID salleId, LocalDate date, LocalTime heureDebut, 
    LocalTime heureFin, String remarques);

// Obtenir un résumé de planification
Map<String, Object> getResumePlanification(UUID classeId);
```

---

### 2. DTOs (Data Transfer Objects)

#### `CoursPlanifiable`
Représente un cours qui peut être planifié:
```java
{
    cours: Cours,
    ue: UE,
    enseignant: Enseignant,
    heuresRestantes: int,
    pourcentageAvancement: double
}
```

#### `CreneauDisponible`
Représente un créneau horaire disponible:
```java
{
    enseignant: Enseignant,
    cours: Cours,
    date: LocalDate,
    heureDebut: LocalTime,
    heureFin: LocalTime,
    plageHoraire: PlageHoraire
}
```

---

### 3. Controller `PlanificationSeanceController`

API REST pour exposer les fonctionnalités de planification.

---

## 🔍 Logique de Planification

### Étape 1: Identifier les Cours Planifiables

**Critères:**
1. ✅ UE est **ACTIVE** (statut = ACTIF)
2. ✅ Il reste des heures à planifier (cours.duree > 0)
3. ✅ Le cours appartient à la classe

**Code:**
```java
List<CoursPlanifiable> cours = planificationService.getCoursPlanifiables(classeId);
```

**Exemple de Résultat:**
```json
{
  "success": true,
  "cours": [
    {
      "coursId": "xxx",
      "coursIntitule": "Cours Magistral - Java",
      "ueCode": "INF101",
      "ueIntitule": "Programmation Java",
      "ueDureeTotal": 60,
      "heuresRestantes": 45,
      "pourcentageAvancement": 25.0,
      "enseignant": {
        "id": "yyy",
        "nom": "Dupont",
        "prenom": "Jean"
      }
    }
  ],
  "total": 1
}
```

---

### Étape 2: Trouver les Créneaux Disponibles

**Critères:**
1. ✅ Enseignant a déclaré sa disponibilité
2. ✅ Pas de conflit avec une séance existante
3. ✅ Dans la période demandée

**Code:**
```java
List<CreneauDisponible> creneaux = planificationService.getCreneauxDisponibles(
    classeId, 
    LocalDate.of(2026, 1, 20),  // Date début
    LocalDate.of(2026, 1, 26)   // Date fin
);
```

**Exemple de Résultat:**
```json
{
  "success": true,
  "creneaux": [
    {
      "date": "2026-01-20",
      "heureDebut": "08:00",
      "heureFin": "10:00",
      "enseignant": {
        "id": "yyy",
        "nom": "Dupont",
        "prenom": "Jean"
      },
      "cours": {
        "id": "xxx",
        "intitule": "Cours Magistral - Java",
        "ueCode": "INF101"
      }
    }
  ],
  "total": 1
}
```

---

### Étape 3: Créer une Séance

**Validations Automatiques:**
1. ✅ UE est active
2. ✅ Il reste des heures
3. ✅ Enseignant est disponible
4. ✅ Pas de conflit enseignant
5. ✅ Pas de conflit salle
6. ✅ Décrémentation automatique des heures

**Code:**
```java
SeanceClasse seance = planificationService.creerSeanceDepuisCreneau(
    emploiDuTempsId,
    coursId,
    enseignantId,
    salleId,
    LocalDate.of(2026, 1, 20),
    LocalTime.of(8, 0),
    LocalTime.of(10, 0),
    "Cours magistral"
);
```

---

## 🚀 API Endpoints

### 1. Obtenir les Cours Planifiables

```http
GET /api/planification/classe/{classeId}/cours-planifiables
```

**Réponse:**
```json
{
  "success": true,
  "cours": [...],
  "total": 5
}
```

---

### 2. Obtenir les Créneaux Disponibles

```http
GET /api/planification/classe/{classeId}/creneaux-disponibles
    ?dateDebut=2026-01-20
    &dateFin=2026-01-26
```

**Réponse:**
```json
{
  "success": true,
  "creneaux": [...],
  "total": 15
}
```

---

### 3. Créer une Séance

```http
POST /api/planification/seance/creer
    ?emploiDuTempsId=xxx
    &coursId=yyy
    &enseignantId=zzz
    &salleId=aaa
    &date=2026-01-20
    &heureDebut=08:00
    &heureFin=10:00
    &remarques=Cours magistral
```

**Réponse:**
```json
{
  "success": true,
  "message": "Séance créée avec succès",
  "seanceId": "bbb"
}
```

---

### 4. Obtenir un Résumé de Planification

```http
GET /api/planification/classe/{classeId}/resume
```

**Réponse:**
```json
{
  "success": true,
  "resume": {
    "totalCours": 5,
    "totalHeuresRestantes": 120,
    "cours": [...]
  }
}
```

---

## 💡 Workflow Complet

### Scénario: Planifier une Semaine de Cours

#### 1. Identifier les Cours à Planifier
```javascript
// Frontend: Récupérer les cours planifiables
fetch('/api/planification/classe/xxx/cours-planifiables')
  .then(response => response.json())
  .then(data => {
    console.log(`${data.total} cours à planifier`);
    console.log(`Total: ${data.resume.totalHeuresRestantes}h restantes`);
  });
```

#### 2. Trouver les Créneaux Disponibles
```javascript
// Frontend: Récupérer les créneaux pour la semaine
fetch('/api/planification/classe/xxx/creneaux-disponibles?dateDebut=2026-01-20&dateFin=2026-01-26')
  .then(response => response.json())
  .then(data => {
    console.log(`${data.total} créneaux disponibles`);
    // Afficher dans un calendrier
  });
```

#### 3. Créer les Séances
```javascript
// Frontend: Créer une séance
fetch('/api/planification/seance/creer', {
  method: 'POST',
  body: new URLSearchParams({
    emploiDuTempsId: 'xxx',
    coursId: 'yyy',
    enseignantId: 'zzz',
    salleId: 'aaa',
    date: '2026-01-20',
    heureDebut: '08:00',
    heureFin: '10:00',
    remarques: 'Cours magistral'
  })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Séance créée!');
  }
});
```

---

## 🎨 Interface Utilisateur Suggérée

### Page: Planification des Séances

#### Section 1: Cours à Planifier
```html
<div class="cours-planifiables">
    <h3>Cours à Planifier</h3>
    <div th:each="cours : ${coursPlanifiables}" class="cours-card">
        <h5>[[${cours.ueCode}]] - [[${cours.ueIntitule}]]</h5>
        <p>Enseignant: [[${cours.enseignant.nom}]] [[${cours.enseignant.prenom}]]</p>
        
        <!-- Barre de progression -->
        <div class="progress">
            <div class="progress-bar" 
                 th:style="'width: ' + ${cours.pourcentageAvancement} + '%'">
                [[${cours.pourcentageAvancement}]]%
            </div>
        </div>
        
        <p class="text-muted">
            [[${cours.heuresRestantes}]]h restantes / [[${cours.ueDureeTotal}]]h total
        </p>
        
        <button class="btn btn-primary" 
                th:onclick="'afficherCreneaux(' + ${cours.coursId} + ')'">
            Planifier
        </button>
    </div>
</div>
```

#### Section 2: Créneaux Disponibles (Calendrier)
```html
<div class="creneaux-disponibles">
    <h3>Créneaux Disponibles</h3>
    
    <!-- Calendrier hebdomadaire -->
    <div class="calendar-week">
        <div th:each="jour : ${jours}" class="calendar-day">
            <h5>[[${jour.nom}]] [[${jour.date}]]</h5>
            
            <div th:each="creneau : ${jour.creneaux}" 
                 class="creneau-card"
                 th:onclick="'creerSeance(' + ${creneau} + ')'">
                <p>[[${creneau.heureDebut}]] - [[${creneau.heureFin}]]</p>
                <p>[[${creneau.cours.ueCode}]]</p>
                <p>[[${creneau.enseignant.nom}]]</p>
            </div>
        </div>
    </div>
</div>
```

---

## ⚠️ Validations et Contraintes

### 1. UE Active
```java
if (cours.getUe().getStatut() != StatutUE.ACTIF) {
    throw new RuntimeException("L'UE n'est pas active");
}
```

### 2. Heures Restantes
```java
if (!dureeService.aDesHeuresRestantes(cours)) {
    throw new RuntimeException("Toutes les heures ont été planifiées");
}
```

### 3. Disponibilité Enseignant
```java
if (!estDisponible(enseignant, date, heureDebut, heureFin)) {
    throw new RuntimeException("L'enseignant n'est pas disponible");
}
```

### 4. Conflit Enseignant
```java
if (aConflitSeance(enseignant, date, heureDebut, heureFin)) {
    throw new RuntimeException("L'enseignant a déjà une séance");
}
```

### 5. Conflit Salle
```java
List<SeanceClasse> conflits = seanceRepository
    .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(...);
if (!conflits.isEmpty()) {
    throw new RuntimeException("La salle est occupée");
}
```

---

## ✅ Avantages du Système

1. **Intelligent**: Filtre automatiquement les cours planifiables
2. **Sécurisé**: Validations multiples avant création
3. **Automatique**: Décrémentation des heures restantes
4. **Flexible**: Supporte plusieurs enseignants et salles
5. **Traçable**: Historique complet des séances

---

## 📚 Fichiers Créés

### Services:
- `service/PlanificationSeanceService.java` - Logique de planification
- `service/DureeService.java` - Gestion des durées

### Controllers:
- `controller/PlanificationSeanceController.java` - API REST

### Repositories (Modifiés):
- `repositories/CoursRepository.java` - Ajout méthodes findByUe
- `repositories/SeanceClasseRepository.java` - Ajout méthode findByCours

---

## 🚀 Prochaines Étapes

1. **Interface Frontend** - Créer les pages de planification
2. **Calendrier Visuel** - Afficher les créneaux dans un calendrier
3. **Drag & Drop** - Permettre de glisser-déposer les cours
4. **Notifications** - Alerter les enseignants des nouvelles séances
5. **Export** - Générer des PDF des emplois du temps

---

**Date:** 20 janvier 2026  
**Statut:** ✅ Système complet et fonctionnel  
**Compilation:** ✅ BUILD SUCCESS
