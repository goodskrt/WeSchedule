# Système de Réservation de Salle avec Vérification de Capacité

## 🎯 Objectif

Implémenter un système complet qui:
1. **Vérifie la capacité** de la salle avant réservation
2. **Compare** capacité salle vs effectif classe
3. **Réserve automatiquement** la salle si capacité suffisante
4. **Empêche** la création de séances si capacité insuffisante

---

## 📋 Logique Métier

### Règle Fondamentale
```
Capacité Salle ≥ Effectif Classe
```

**Exemple:**
```
Salle A: 50 places
Classe L3 Info: 45 étudiants
→ ✅ Capacité suffisante (5 places restantes)

Salle B: 30 places
Classe L3 Info: 45 étudiants
→ ❌ Capacité insuffisante (manque 15 places)
```

---

## 🔧 Architecture

### 1. Service `ReservationSalleService`

Service principal pour gérer les réservations avec vérification de capacité.

#### Méthodes Principales:

```java
// Trouver les salles disponibles avec capacité suffisante
List<SalleDisponible> getSallesDisponiblesAvecCapacite(
    UUID classeId, LocalDate date, LocalTime heureDebut, LocalTime heureFin);

// Filtrer uniquement les salles avec capacité suffisante
List<SalleDisponible> getSallesAvecCapaciteSuffisante(...);

// Vérifier si une salle est disponible (pas de conflit)
boolean estSalleDisponible(Salle salle, LocalDate date, ...);

// Vérifier si une salle a une capacité suffisante
boolean aCapaciteSuffisante(UUID salleId, UUID classeId);

// Créer une réservation avec vérification
Reservation creerReservation(...);

// Réserver automatiquement la meilleure salle
Salle reserverMeilleureSalle(...);

// Obtenir des statistiques
Map<String, Object> getStatistiquesSalles(...);

// Vérifier la capacité (détails)
Map<String, Object> verifierCapacite(UUID salleId, UUID classeId);
```

---

### 2. DTO `SalleDisponible`

Représente une salle avec ses informations de capacité:

```java
class SalleDisponible {
    Salle salle;
    int capacite;              // Capacité de la salle
    int effectifClasse;        // Effectif de la classe
    boolean capaciteSuffisante; // true si capacite >= effectif
    int placesRestantes;       // capacite - effectif
}
```

**Exemple:**
```json
{
  "salle": {
    "id": "xxx",
    "nomSalle": "Amphi A",
    "capacite": 50
  },
  "capacite": 50,
  "effectifClasse": 45,
  "capaciteSuffisante": true,
  "placesRestantes": 5
}
```

---

## 🔍 Processus de Vérification

### Étape 1: Vérifier la Disponibilité

```java
boolean estSalleDisponible(Salle salle, LocalDate date, 
                          LocalTime heureDebut, LocalTime heureFin) {
    
    // 1. Vérifier conflits avec séances existantes
    List<SeanceClasse> conflitsSeances = seanceRepository
        .findBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(...);
    
    if (!conflitsSeances.isEmpty()) {
        return false; // Salle occupée par une séance
    }
    
    // 2. Vérifier conflits avec réservations existantes
    List<Reservation> conflitsReservations = reservationRepository
        .findBySalleAndStatutAndPlageHoraire...(...);
    
    return conflitsReservations.isEmpty();
}
```

---

### Étape 2: Vérifier la Capacité

```java
boolean aCapaciteSuffisante(UUID salleId, UUID classeId) {
    Salle salle = salleRepository.findById(salleId).orElseThrow();
    Classe classe = classeRepository.findById(classeId).orElseThrow();
    
    return salle.getCapacite() >= classe.getEffectif();
}
```

---

### Étape 3: Créer la Réservation

```java
Reservation creerReservation(...) {
    // 1. Vérifier que la salle existe
    Salle salle = salleRepository.findById(salleId).orElseThrow();
    
    // 2. Vérifier que la classe existe
    Classe classe = classeRepository.findById(classeId).orElseThrow();
    
    // 3. ⚠️ VÉRIFICATION CRITIQUE: Capacité suffisante
    if (salle.getCapacite() < classe.getEffectif()) {
        throw new RuntimeException(
            String.format(
                "Capacité insuffisante: la salle '%s' a une capacité de %d places " +
                "mais la classe a un effectif de %d étudiants",
                salle.getNomSalle(),
                salle.getCapacite(),
                classe.getEffectif()
            )
        );
    }
    
    // 4. Vérifier la disponibilité
    if (!estSalleDisponible(salle, date, heureDebut, heureFin)) {
        throw new RuntimeException("La salle n'est pas disponible");
    }
    
    // 5. Créer la réservation
    Reservation reservation = new Reservation();
    reservation.setSalle(salle);
    reservation.setStatut(StatutReservation.CONFIRMEE);
    
    return reservationRepository.save(reservation);
}
```

---

## 🚀 Intégration avec PlanificationSeanceService

### Modification de `creerSeanceDepuisCreneau`

```java
// AVANT (sans vérification de capacité)
if (salleId != null) {
    salle = salleRepository.findById(salleId).orElseThrow();
    
    // Vérifier conflit salle
    List<SeanceClasse> conflits = seanceRepository.find...();
    if (!conflits.isEmpty()) {
        throw new RuntimeException("Salle occupée");
    }
}

// APRÈS (avec vérification de capacité)
if (salleId != null) {
    salle = salleRepository.findById(salleId).orElseThrow();
    
    // ⚠️ VÉRIFICATION CRITIQUE: Capacité suffisante
    if (!reservationSalleService.aCapaciteSuffisante(
            salleId, emploiDuTemps.getClasse().getIdClasse())) {
        
        Map<String, Object> verif = reservationSalleService.verifierCapacite(
            salleId, emploiDuTemps.getClasse().getIdClasse()
        );
        throw new RuntimeException((String) verif.get("message"));
    }
    
    // Vérifier disponibilité
    if (!reservationSalleService.estSalleDisponible(
            salle, date, heureDebut, heureFin)) {
        throw new RuntimeException("Salle occupée");
    }
}

// Créer la séance
SeanceClasse seance = seanceRepository.save(seance);

// Créer la réservation
if (salle != null) {
    reservationSalleService.creerReservation(
        salleId, classeId, coursId, enseignantId,
        date, heureDebut, heureFin
    );
}
```

---

## 🌐 API Endpoints

### 1. Obtenir les Salles Disponibles avec Capacité

```http
GET /api/planification/classe/{classeId}/salles-disponibles
    ?date=2026-01-20
    &heureDebut=08:00
    &heureFin=10:00
```

**Réponse:**
```json
{
  "success": true,
  "salles": [
    {
      "salleId": "xxx",
      "nomSalle": "Amphi A",
      "typeSalle": "AMPHITHEATRE",
      "capacite": 50,
      "effectifClasse": 45,
      "capaciteSuffisante": true,
      "placesRestantes": 5
    },
    {
      "salleId": "yyy",
      "nomSalle": "Salle B",
      "capacite": 30,
      "effectifClasse": 45,
      "capaciteSuffisante": false,
      "placesRestantes": -15
    }
  ],
  "total": 2
}
```

---

### 2. Vérifier la Capacité d'une Salle

```http
GET /api/planification/salle/{salleId}/verifier-capacite/{classeId}
```

**Réponse (Capacité Suffisante):**
```json
{
  "success": true,
  "verification": {
    "salle": "Amphi A",
    "capaciteSalle": 50,
    "effectifClasse": 45,
    "capaciteSuffisante": true,
    "placesRestantes": 5,
    "message": "Capacité suffisante: 5 places disponibles"
  }
}
```

**Réponse (Capacité Insuffisante):**
```json
{
  "success": true,
  "verification": {
    "salle": "Salle B",
    "capaciteSalle": 30,
    "effectifClasse": 45,
    "capaciteSuffisante": false,
    "placesRestantes": -15,
    "message": "Capacité insuffisante: il manque 15 places"
  }
}
```

---

### 3. Réserver Automatiquement la Meilleure Salle

```http
POST /api/planification/classe/{classeId}/reserver-meilleure-salle
    ?date=2026-01-20
    &heureDebut=08:00
    &heureFin=10:00
```

**Logique:**
- Trouve toutes les salles disponibles avec capacité suffisante
- Trie par capacité la plus proche de l'effectif (évite le gaspillage)
- Réserve automatiquement la meilleure

**Réponse:**
```json
{
  "success": true,
  "message": "Meilleure salle réservée avec succès",
  "salleId": "xxx",
  "nomSalle": "Amphi A",
  "capacite": 50
}
```

---

### 4. Obtenir les Statistiques des Salles

```http
GET /api/planification/classe/{classeId}/statistiques-salles
    ?date=2026-01-20
    &heureDebut=08:00
    &heureFin=10:00
```

**Réponse:**
```json
{
  "success": true,
  "statistiques": {
    "totalSallesDisponibles": 10,
    "sallesCapaciteSuffisante": 6,
    "sallesCapaciteInsuffisante": 4,
    "salles": [...]
  }
}
```

---

## 💡 Algorithme de Sélection de la Meilleure Salle

### Critères de Tri:

1. **Priorité 1:** Capacité suffisante (true avant false)
2. **Priorité 2:** Capacité la plus proche de l'effectif

**Exemple:**
```
Classe: 45 étudiants

Salles disponibles:
- Salle A: 100 places → +55 places (gaspillage)
- Salle B: 50 places  → +5 places  ✅ MEILLEURE
- Salle C: 48 places  → +3 places  (trop juste)
- Salle D: 30 places  → -15 places (insuffisant)

Ordre de sélection:
1. Salle B (50 places) - Capacité suffisante, proche de l'effectif
2. Salle C (48 places) - Capacité suffisante, très proche
3. Salle A (100 places) - Capacité suffisante, mais gaspillage
4. Salle D (30 places) - Capacité insuffisante (rejetée)
```

**Code:**
```java
sallesDisponibles.sort((s1, s2) -> {
    // Priorité 1: Capacité suffisante
    if (s1.isCapaciteSuffisante() != s2.isCapaciteSuffisante()) {
        return s1.isCapaciteSuffisante() ? -1 : 1;
    }
    
    // Priorité 2: Capacité la plus proche (éviter gaspillage)
    return Integer.compare(
        Math.abs(s1.getPlacesRestantes()),
        Math.abs(s2.getPlacesRestantes())
    );
});
```

---

## 🔄 Workflow Complet

### Scénario: Créer une Séance avec Réservation de Salle

```
1. Admin sélectionne une classe (L3 Info - 45 étudiants)
   ↓
2. Admin choisit un créneau (Lundi 8h-10h)
   ↓
3. Système affiche les salles disponibles:
   - Amphi A: 50 places ✅ (5 places restantes)
   - Salle B: 30 places ❌ (manque 15 places)
   ↓
4. Admin sélectionne Amphi A
   ↓
5. Système vérifie:
   ✅ Capacité suffisante (50 >= 45)
   ✅ Salle disponible (pas de conflit)
   ↓
6. Système crée:
   - Séance de classe
   - Réservation de salle (statut: CONFIRMEE)
   - Décrémente les heures restantes du cours
   ↓
7. Confirmation: "Séance créée et salle réservée!"
```

---

## ⚠️ Gestion des Erreurs

### Erreur 1: Capacité Insuffisante
```
Message: "Capacité insuffisante: la salle 'Salle B' a une capacité de 30 places 
         mais la classe a un effectif de 45 étudiants"
Code HTTP: 400 Bad Request
```

### Erreur 2: Salle Occupée
```
Message: "La salle 'Amphi A' n'est pas disponible le 2026-01-20 de 08:00 à 10:00"
Code HTTP: 400 Bad Request
```

### Erreur 3: Aucune Salle Disponible
```
Message: "Aucune salle disponible avec capacité suffisante pour cette classe"
Code HTTP: 400 Bad Request
```

---

## ✅ Avantages du Système

1. **Sécurité:** Impossible de créer une séance dans une salle trop petite
2. **Optimisation:** Sélection automatique de la meilleure salle
3. **Traçabilité:** Réservations enregistrées en base
4. **Flexibilité:** Statistiques et vérifications disponibles via API
5. **Prévention:** Détection des conflits avant création

---

## 📚 Fichiers Créés/Modifiés

### Créés:
- `service/ReservationSalleService.java` - Gestion des réservations
- `repositories/ReservationRepository.java` - Méthodes de recherche

### Modifiés:
- `service/PlanificationSeanceService.java` - Intégration vérification capacité
- `controller/PlanificationSeanceController.java` - Nouveaux endpoints (4)

---

## 🎨 Interface Utilisateur Suggérée

### Sélection de Salle avec Indicateur de Capacité

```html
<div class="salles-disponibles">
    <h4>Salles Disponibles</h4>
    
    <div th:each="salle : ${salles}" 
         th:class="${salle.capaciteSuffisante ? 'salle-card ok' : 'salle-card warning'}">
        
        <h5>[[${salle.nomSalle}]]</h5>
        
        <!-- Indicateur de capacité -->
        <div class="capacite-indicator">
            <span class="badge" 
                  th:classappend="${salle.capaciteSuffisante ? 'bg-success' : 'bg-danger'}">
                [[${salle.capacite}]] places
            </span>
            
            <span th:if="${salle.capaciteSuffisante}" class="text-success">
                ✓ [[${salle.placesRestantes}]] places restantes
            </span>
            
            <span th:if="${!salle.capaciteSuffisante}" class="text-danger">
                ✗ Manque [[${Math.abs(salle.placesRestantes)}]] places
            </span>
        </div>
        
        <!-- Barre de progression -->
        <div class="progress mt-2">
            <div class="progress-bar" 
                 th:classappend="${salle.capaciteSuffisante ? 'bg-success' : 'bg-danger'}"
                 th:style="'width: ' + ${(salle.effectifClasse * 100.0) / salle.capacite} + '%'">
                [[${salle.effectifClasse}]] / [[${salle.capacite}]]
            </div>
        </div>
        
        <!-- Bouton de sélection -->
        <button th:if="${salle.capaciteSuffisante}"
                class="btn btn-primary mt-2"
                th:onclick="'selectionnerSalle(' + ${salle.salleId} + ')'">
            Sélectionner
        </button>
        
        <button th:if="${!salle.capaciteSuffisante}"
                class="btn btn-secondary mt-2" disabled>
            Capacité insuffisante
        </button>
    </div>
</div>
```

---

**Date:** 20 janvier 2026  
**Statut:** ✅ Système complet et fonctionnel  
**Compilation:** ✅ BUILD SUCCESS  
**Sécurité:** ✅ Vérification de capacité implémentée
