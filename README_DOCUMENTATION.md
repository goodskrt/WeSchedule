# 📚 Index - Documentation WeSchedule

## 🎯 Démarrage Rapide

**→ [QUICK_START.md](QUICK_START.md)** ⭐
Guide complet pour démarrer l'application en 5 minutes.

---

## 📖 Documentation Principale

### 🔐 [AUTHENTICATION_DOCUMENTATION.md](AUTHENTICATION_DOCUMENTATION.md)
Documentation technique complète de l'implémentation :
- Architecture du système d'authentification
- Flux d'authentification détaillé
- Description des composants (Service, Controller, DTOs)
- Configuration de sécurité
- Routes et endpoints
- DTOs et modèles
- Bonnes pratiques de sécurité

### 🛠️ [TECHNICAL_NOTES.md](TECHNICAL_NOTES.md)
Notes techniques approfondies :
- Structure de l'héritage JPA (JOINED)
- Polymorphisme et chargement automatique
- Flux détaillé de Spring Security
- Cycle de vie de l'authentification
- Exemples de code avancés
- Points importants d'implémentation

### 📊 [DATA_INITIALIZATION.md](DATA_INITIALIZATION.md)
Gestion des données de test :
- Comptes créés automatiquement
- Comment désactiver l'initialisation
- Scripts SQL manuels
- Checklist de déploiement

### ✨ [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
Résumé complet de l'implémentation :
- Ce qui a été réalisé
- Fichiers créés/modifiés
- Architecture globale
- Fonctionnalités bonus
- Considérations production

---

## 🏗️ Architecture

### Système d'Authentification
```
Login → Spring Security → UserDetailsService → BCrypt → Session
     ↓
Redirection selon le rôle → Dashboard spécifique
```

### Rôles et Permissions
| Rôle | Email | Mot de passe | Dashboard |
|------|-------|------------|-----------|
| 👤 Admin | admin@test.com | password123 | `/dashboard/admin` |
| 👨‍🏫 Enseignant | teacher@test.com | password123 | `/dashboard/enseignant` |
| 👨‍🎓 Étudiant | student@test.com | password123 | `/dashboard/etudiant` |

---

## 📁 Structure des Fichiers Créés

### Backend Java
```
src/main/java/com/iusjc/weschedule/
├── dto/
│   ├── LoginRequest.java
│   └── AuthResponse.java
├── service/
│   ├── AuthService.java
│   └── UserDetailsServiceImpl.java
├── security/
│   └── UserPrincipal.java
├── config/
│   └── DataInitializer.java
└── controller/
    └── AuthController.java (MODIFIÉ)
```

### Frontend Thymeleaf
```
src/main/resources/templates/
├── login.html
├── register.html
├── admin/
│   ├── register.html
│   └── dashboard-admin.html
└── dashboard/
    ├── dashboard-enseignant.html
    └── dashboard-etudiant.html
```

### Configuration
```
src/main/resources/
└── application.properties (MODIFIÉ - Thymeleaf)
```

---

## 🔗 Routes Principales

### Routes Publiques
```
GET  /                    → Accueil (redirection)
GET  /login               → Page de connexion
POST /login               → Traitement login
GET  /register            → Inscription étudiant
POST /register            → Traitement inscription
GET  /admin/signup        → Inscription admin
POST /admin/signup        → Traitement
GET  /logout              → Déconnexion
```

### Routes Protégées
```
GET  /dashboard           → Redirection selon rôle
GET  /dashboard/admin     → Dashboard admin
GET  /dashboard/enseignant → Dashboard enseignant
GET  /dashboard/etudiant  → Dashboard étudiant
```

---

## 🔐 Sécurité Implémentée

✅ **BCryptPasswordEncoder** - Hashage sécurisé des mots de passe  
✅ **Spring Security** - Authentification et autorisation  
✅ **Sessions HTTP** - Gestion des sessions sécurisées  
✅ **Validation** - Côté serveur et client  
✅ **CSRF Protection** - Intégré à Thymeleaf  
✅ **Contrôle d'accès par rôle** - RBAC (Role-Based Access Control)  

---

## 📝 Compiles et Tests

### Vérifier la Compilation
```bash
./mvnw clean compile
```

### Démarrer l'Application
```bash
./mvnw spring-boot:run
```

### Accéder à l'Application
```
http://localhost:8080/
```

---

## 🎓 Pour Apprendre

### Comprendre le Système d'Authentification
1. Lire [QUICK_START.md](QUICK_START.md) - Vue d'ensemble
2. Lire [AUTHENTICATION_DOCUMENTATION.md](AUTHENTICATION_DOCUMENTATION.md) - Détails techniques
3. Lire [TECHNICAL_NOTES.md](TECHNICAL_NOTES.md) - Implémentation avancée

### Configurer une Nouvelle Installation
1. Suivre [QUICK_START.md](QUICK_START.md)
2. Consulter [DATA_INITIALIZATION.md](DATA_INITIALIZATION.md) pour les comptes test

### Déployer en Production
1. Lire [DATA_INITIALIZATION.md](DATA_INITIALIZATION.md) - Checklist
2. Consulter [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Considérations prod

---

## 🚀 Prochaines Étapes

### Fonctionnalités à Ajouter
- [ ] Changement de mot de passe
- [ ] Récupération de mot de passe (oublié)
- [ ] Email de confirmation
- [ ] Authentification à deux facteurs (2FA)
- [ ] Endpoints REST API
- [ ] Authentification JWT
- [ ] Gestion de profil utilisateur
- [ ] Logs d'activité

### Améliorations Possibles
- [ ] Intégration avec SSO (OAuth2, OIDC)
- [ ] Cache distributif (Redis)
- [ ] Monitoring et alerting
- [ ] Tests unitaires
- [ ] Tests d'intégration
- [ ] Documentation API Swagger/OpenAPI

---

## 💡 Astuces Utiles

### Voir les Logs de Démarrage
Dans `application.properties`, les logs affichent les comptes créés :
```
═══════════════════════════════════════════════════════════════
  COMPTES DE TEST CRÉÉS
═══════════════════════════════════════════════════════════════
  Admin:      admin@test.com / password123
  Enseignant: teacher@test.com / password123
  Étudiant:   student@test.com / password123
═══════════════════════════════════════════════════════════════
```

### Déboguer les Authentifications
```properties
logging.level.org.springframework.security=DEBUG
```

### Voir les Requêtes SQL
```properties
logging.level.org.hibernate.SQL=DEBUG
```

---

## 📞 Support et Questions

### Problèmes Courants

**Q: Les données disparaissent au redémarrage**  
A: C'est normal avec `ddl-auto=create-drop`. Les comptes de test sont recréés.

**Q: Je n'arrive pas à me connecter**  
A: Vérifier que la BD MySQL est lancée et la configuration dans application.properties.

**Q: Comment ajouter un utilisateur manuellement**  
A: Via `/register` pour étudiant ou `/admin/signup` pour admin.

---

## 📈 Statistiques du Projet

- **Fichiers créés** : 13
- **Fichiers modifiés** : 3
- **Lignes de code** : ~2000+
- **Lignes de documentation** : ~1500+
- **Compilation** : ✅ Succès
- **Templates HTML** : 7
- **Services** : 2
- **Contrôleurs** : 1 (AuthController)

---

## 📜 Historique

**v1.0 - 24 novembre 2025**
- ✅ Implémentation complète du système d'authentification
- ✅ Inscription pour les 3 rôles
- ✅ Dashboards personnalisés
- ✅ Documentation complète
- ✅ Comptes de test automatiques

---

## 📄 Fichiers de Documentation

1. **Ce fichier** : Index et navigation
2. **QUICK_START.md** : Démarrage rapide
3. **AUTHENTICATION_DOCUMENTATION.md** : Documentation technique
4. **TECHNICAL_NOTES.md** : Notes techniques avancées
5. **DATA_INITIALIZATION.md** : Gestion des données
6. **IMPLEMENTATION_SUMMARY.md** : Résumé complet

---

## 🎉 Vous êtes Prêt !

Tous les fichiers de documentation sont disponibles pour vous guider à travers :
- ✅ L'installation et le démarrage
- ✅ La compréhension de l'architecture
- ✅ L'utilisation des fonctionnalités
- ✅ Le déploiement en production
- ✅ L'extension future du système

**Bonne utilisation de WeSchedule !** 🚀
