# WeSchedule - Application de Gestion d'Emploi du Temps

> **Système d'authentification complet avec Thymeleaf et Spring Boot**

## 🚀 Démarrage Rapide

### Via PowerShell (Recommandé pour Windows)
```powershell
.\start.ps1
```

### Via Batch (Alternative)
```cmd
start.bat
```

### Manuellement
```bash
./mvnw spring-boot:run
# ou
mvn spring-boot:run
```

L'application est accessible sur **http://localhost:8080**

---

## 📋 Comptes de Test

L'application crée automatiquement 3 comptes de test au démarrage :

| Rôle | Email | Mot de passe | Dashboard |
|------|-------|--------------|-----------|
| Administrateur | `admin@test.com` | `password123` | `/dashboard/admin` |
| Enseignant | `teacher@test.com` | `password123` | `/dashboard/enseignant` |
| Étudiant | `student@test.com` | `password123` | `/dashboard/etudiant` |

---

## 🔐 Fonctionnalités Implémentées

### Backend
- ✅ Service d'authentification avec BCrypt
- ✅ Service d'enregistrement des utilisateurs
- ✅ Spring Security avec DaoAuthenticationProvider
- ✅ Héritage JPA avec stratégie JOINED
- ✅ Validation des données (JSR-303)
- ✅ Gestion des rôles (ADMINISTRATEUR, ENSEIGNANT, ÉTUDIANT)

### Frontend
- ✅ Formulaire de connexion (Thymeleaf)
- ✅ Formulaire d'enregistrement étudiant
- ✅ Formulaire de création d'administrateur
- ✅ 3 dashboards distincts par rôle
- ✅ Redirection automatique basée sur le rôle
- ✅ Design responsive avec Bootstrap 5.3

### Configuration
- ✅ Thymeleaf 3.1+
- ✅ Spring Security
- ✅ JPA/Hibernate
- ✅ MySQL 8.0+
- ✅ Lombok
- ✅ Maven

---

## 📁 Structure des Fichiers

```
weschedule/
├── src/main/java/com/iusjc/weschedule/
│   ├── controller/
│   │   └── AuthController.java          # Endpoints auth & dashboards
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   └── RegisterRequest.java
│   ├── models/
│   │   └── Utilisateur.java (+ sous-classes)
│   ├── repositories/
│   │   └── UtilisateurRepository.java
│   ├── security/
│   │   ├── SecurityConfig.java          # Configuration Spring Security
│   │   ├── UserPrincipal.java           # Implémentation UserDetails
│   │   └── UserDetailsServiceImpl.java
│   ├── service/
│   │   ├── AuthService.java             # Logique auth & registration
│   │   └── DataInitializer.java         # Création données de test
│   └── WescheduleApplication.java
├── src/main/resources/
│   ├── application.properties            # Config (base de données, Thymeleaf)
│   ├── templates/
│   │   ├── login.html                   # Page de connexion
│   │   ├── register.html                # Formulaire inscription étudiant
│   │   ├── admin/
│   │   │   ├── register.html            # Formulaire création admin
│   │   │   └── dashboard-admin.html     # Dashboard administrateur
│   │   └── dashboard/
│   │       ├── dashboard-enseignant.html
│   │       └── dashboard-etudiant.html
│   └── static/                          # CSS, JS, images
├── pom.xml                              # Dépendances Maven
├── start.ps1                            # Script PowerShell
├── start.bat                            # Script Batch
├── QUICK_START.md                       # Guide rapide
├── AUTHENTICATION_DOCUMENTATION.md      # Documentation technique
├── README_DOCUMENTATION.md              # Index documentation
└── ... autres fichiers de doc
```

---

## 🔧 Configuration

### Base de Données

1. **Créer la base de données** :
   ```sql
   CREATE DATABASE IF NOT EXISTS weschedule;
   ```

2. **Éditer `application.properties`** :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/weschedule
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=create-drop  # ou 'update' en production
   ```

### Thymeleaf

La configuration est automatique via les starters Spring Boot.
Éditer `application.properties` pour les paramètres avancés :

```properties
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.cache=false  # false en développement
```

---

## 🧪 Tests

### Tester l'authentification
1. Naviguer vers http://localhost:8080/login
2. Entrer : `admin@test.com` / `password123`
3. Vérifier la redirection vers `/dashboard/admin`

### Tester l'enregistrement
1. Naviguer vers http://localhost:8080/register
2. Remplir le formulaire
3. Les mots de passe sont hachés avec BCrypt

### Tester les dashboards
- Admin : http://localhost:8080/dashboard/admin (ADMINISTRATEUR uniquement)
- Enseignant : http://localhost:8080/dashboard/enseignant (ENSEIGNANT uniquement)
- Étudiant : http://localhost:8080/dashboard/etudiant (ÉTUDIANT uniquement)

---

## 🛠️ Commandes Utiles

```bash
# Démarrer l'application
./mvnw spring-boot:run

# Compiler uniquement
./mvnw clean compile

# Build complet (JAR)
./mvnw clean package

# Exécuter les tests
./mvnw test

# Voir la structure des dépendances
./mvnw dependency:tree

# Nettoyer les fichiers compilés
./mvnw clean

# Exécuter un goal Maven spécifique
./mvnw <goal>
```

---

## 📚 Documentation Complète

- **[QUICK_START.md](./QUICK_START.md)** - Guide de démarrage rapide étape par étape
- **[AUTHENTICATION_DOCUMENTATION.md](./AUTHENTICATION_DOCUMENTATION.md)** - Documentation technique détaillée
- **[DATA_INITIALIZATION.md](./DATA_INITIALIZATION.md)** - Documentation sur l'initialisation des données
- **[TECHNICAL_NOTES.md](./TECHNICAL_NOTES.md)** - Notes techniques avancées
- **[IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)** - Résumé de l'implémentation
- **[README_DOCUMENTATION.md](./README_DOCUMENTATION.md)** - Index de toute la documentation
- **[IMPLEMENTATION_CHECKLIST.md](./IMPLEMENTATION_CHECKLIST.md)** - Checklist de vérification
- **[USEFUL_COMMANDS.md](./USEFUL_COMMANDS.md)** - Référence des commandes

---

## 🔄 Flux d'Authentification

```
User → /login (GET)
   ↓
Affiche formulaire login.html
   ↓
User soumit email + mot de passe (POST)
   ↓
AuthService.authenticate() ← BCrypt.matches() validation
   ↓
Spring Security crée SecurityContext
   ↓
Redirection basée sur rôle
   ├─ ADMINISTRATEUR → /dashboard/admin
   ├─ ENSEIGNANT → /dashboard/enseignant
   └─ ÉTUDIANT → /dashboard/etudiant
```

---

## 🔐 Sécurité

### Implémenté
- ✅ BCryptPasswordEncoder (force 10)
- ✅ CSRF protection (activée par défaut)
- ✅ Session management
- ✅ Role-based access control (RBAC)
- ✅ Validation des entrées (JSR-303)
- ✅ Héritage polymorphe sécurisé (JPA JOINED)

### Non implémenté (À faire)
- ❌ JWT tokens
- ❌ OAuth2
- ❌ HTTPS forcing
- ❌ Rate limiting
- ❌ Email confirmation
- ❌ Password reset

---

## 🚨 Troubleshooting

### "Connection refused" pour la base de données
- Vérifier que MySQL est lancé
- Vérifier les paramètres dans `application.properties`
- Créer la base de données

### Erreur 404 sur /login
- Vérifier que les templates existent dans `src/main/resources/templates/`
- Vérifier la configuration de Thymeleaf

### Mot de passe invalide même avec password123
- Les mots de passe sont hachés avec BCrypt
- Vérifier que le compte existe dans la base de données
- Regarder les logs : `logging.level.com.iusjc.weschedule=DEBUG`

### L'application ne se compile pas
- Exécuter `./mvnw clean`
- Vérifier que Java 17+ est installé
- Vérifier `java -version`

---

## 📊 Architecture

```
┌─────────────────────────────────────────────┐
│         Client (Navigateur)                 │
│  login.html | register.html | dashboards   │
└────────────────┬────────────────────────────┘
                 │
┌────────────────v────────────────────────────┐
│     Spring Security Filter Chain            │
│  (Authentication, Authorization)           │
└────────────────┬────────────────────────────┘
                 │
┌────────────────v────────────────────────────┐
│         AuthController                      │
│  /login | /register | /dashboard/*          │
└────────────────┬────────────────────────────┘
                 │
┌────────────────v────────────────────────────┐
│         AuthService                         │
│  authenticate() | register()                │
└────────────────┬────────────────────────────┘
                 │
┌────────────────v────────────────────────────┐
│      UserDetailsServiceImpl                  │
│  loadUserByUsername() → UserPrincipal       │
└────────────────┬────────────────────────────┘
                 │
┌────────────────v────────────────────────────┐
│         Repositories                        │
│  UtilisateurRepository.findByEmail()        │
└────────────────┬────────────────────────────┘
                 │
┌────────────────v────────────────────────────┐
│      MySQL Database                         │
│  utilisateur | administrateur | enseignant │
│  étudiant | calendrier | cours ...          │
└─────────────────────────────────────────────┘
```

---

## 📦 Dépendances Principales

```xml
<!-- Spring Boot -->
<spring-boot-starter-web>
<spring-boot-starter-security>
<spring-boot-starter-data-jpa>
<spring-boot-starter-thymeleaf>
<spring-boot-starter-validation>

<!-- Base de données -->
<mysql-connector-java>

<!-- Utilities -->
<lombok>

<!-- Tests -->
<spring-boot-starter-test>
```

---

## 📝 Licence

Projet académique - IUSJC 2024

---

## 👥 Équipe

- Développeur principal : Vous
- Framework : Spring Boot 3.5.8
- Base de données : MySQL 8.0+
- Frontend : Thymeleaf + Bootstrap 5.3

---

## ❓ FAQ

**Q: Comment désactiver la création de comptes de test ?**
A: Commentez la ligne `@Bean` dans `DataInitializer.java`

**Q: Comment ajouter un nouveau rôle ?**
A: 
1. Ajouter dans l'enum `Role.java`
2. Créer une classe modèle correspondante
3. Ajouter le repository
4. Mettre à jour `AuthService`

**Q: Comment changer le secret BCrypt ?**
A: Éditer la force du BCryptPasswordEncoder dans `SecurityConfig.java` (actuellement 10)

**Q: Comment activer le cache Thymeleaf en production ?**
A: Éditer `spring.thymeleaf.cache=true` dans `application.properties`

---

## 🎯 Prochaines Étapes

1. ✅ Authentification et registration
2. ✅ Dashboards par rôle
3. ⏳ Implémenter les fonctionnalités métier (emploi du temps, cours, etc.)
4. ⏳ Ajouter une API REST
5. ⏳ Implémenter JWT pour l'authentification API
6. ⏳ Ajouter les tests unitaires et d'intégration

---

**Besoin d'aide ?** Consultez les fichiers de documentation dans le répertoire racine.
