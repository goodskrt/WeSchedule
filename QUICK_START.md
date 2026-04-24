# WeSchedule - Guide de Démarrage Rapide

## 🎯 Vue d'ensemble

WeSchedule est une application de gestion d'emploi du temps construite avec Spring Boot 3.5.8 et Thymeleaf.

## 🚀 Démarrage Rapide

### Prérequis
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Configuration de la Base de Données

1. **Créer la base de données MySQL :**
```sql
CREATE DATABASE IF NOT EXISTS weshedule;
```

2. **Configurer les paramètres de connexion dans `application.properties` :**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/weshedule?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe
```

### Démarrage de l'Application

```bash
# Via Maven Wrapper
./mvnw spring-boot:run

# Ou via Maven standard
mvn spring-boot:run
```

L'application sera accessible à `http://localhost:8080`

## 🔐 Authentification et Inscription

### Comptes de Test (Créés automatiquement)

| Rôle | Email | Mot de passe | Dashboard |
|------|-------|------------|-----------|
| 👤 Administrateur | admin@test.com | password123 | `/dashboard/admin` |
| 👨‍🏫 Enseignant | teacher@test.com | password123 | `/dashboard/enseignant` |
| 👨‍🎓 Étudiant | student@test.com | password123 | `/dashboard/etudiant` |

### Pages Principales

- **Accueil** : `http://localhost:8080/`
- **Connexion** : `http://localhost:8080/login`
- **Inscription (Étudiant)** : `http://localhost:8080/register`
- **Inscription (Admin)** : `http://localhost:8080/admin/signup`

## 📋 Workflow

### 1. Inscription d'un nouvel étudiant
```
1. Visiter http://localhost:8080/register
2. Remplir le formulaire (nom, prénom, email, téléphone, mot de passe)
3. Soumettre
4. Redirection vers la page de connexion
5. Se connecter avec ses identifiants
```

### 2. Connexion
```
1. Visiter http://localhost:8080/login
2. Entrer l'email et le mot de passe
3. Cliquer sur "Se connecter"
4. Redirection automatique vers le dashboard approprié selon le rôle
```

### 3. Navigation
- Chaque rôle a son propre dashboard avec menu de navigation
- Cliquer sur "Déconnexion" pour se déconnecter

## 🏗️ Architecture

### Structure du Projet

```
src/main/java/com/iusjc/weschedule/
├── controller/        # Contrôleurs (AuthController, etc.)
├── service/          # Services métier (AuthService, UserDetailsServiceImpl)
├── security/         # Configuration de sécurité et JWT
├── dto/             # Data Transfer Objects
├── models/          # Entités JPA
├── repositories/    # Accès aux données
├── enums/           # Énumérations (Role, etc.)
└── config/          # Configuration (DataInitializer, etc.)

src/main/resources/
├── application.properties    # Configuration de l'application
├── templates/               # Templates Thymeleaf
│   ├── login.html
│   ├── register.html
│   ├── admin/
│   │   ├── register.html
│   │   └── dashboard-admin.html
│   └── dashboard/
│       ├── dashboard-enseignant.html
│       └── dashboard-etudiant.html
└── static/                 # Assets statiques (CSS, JS, images)
```

## 🔐 Sécurité

### Authentification
- ✅ **Hashage des mots de passe** : BCryptPasswordEncoder
- ✅ **Sessions HTTP** : Gestion sécurisée des sessions
- ✅ **Authentification par formulaire** : Intégré à Spring Security

### Autorisation
- ✅ **Contrôle d'accès basé sur les rôles** (RBAC)
- ✅ **Redirection automatique selon le rôle**
- ✅ **Protection des pages sensibles**

### Routes Publiques
```
GET  /                    # Redirection vers /login ou /dashboard
GET  /login               # Page de connexion
POST /login               # Traitement de la connexion
GET  /register            # Inscription étudiant
POST /register            # Traitement de l'inscription
GET  /admin/signup        # Inscription admin
POST /admin/signup        # Traitement inscription admin
GET  /logout              # Déconnexion
```

### Routes Protégées
```
GET  /dashboard           # Redirection selon le rôle
GET  /dashboard/admin     # Dashboard administrateur
GET  /dashboard/enseignant # Dashboard enseignant
GET  /dashboard/etudiant  # Dashboard étudiant
```

## 🎨 Templates Thymeleaf

Tous les templates utilisent :
- **Bootstrap 5.3** pour le design responsive
- **Thymeleaf** pour la logique côté serveur
- **Validation HTML5** côté client

### Fonctionnalités
- ✅ Responsive design (mobile, tablette, desktop)
- ✅ Validation côté client
- ✅ Messages d'erreur contextualisés
- ✅ Accessibilité (labels, alt text, etc.)

## 📊 Base de Données

### Tables Principales
- `utilisateurs` : Table parente pour tous les utilisateurs
- `administrateurs` : Administrateurs du système
- `enseignants` : Professeurs et formateurs
- `etudiants` : Étudiants
- Autres tables pour les entités métier (Cours, Salle, Réservation, etc.)

### Configuration JPA
```properties
spring.jpa.hibernate.ddl-auto=create-drop    # Créer les tables à chaque démarrage
spring.jpa.show-sql=true                     # Afficher les requêtes SQL
```

## 🧪 Tests

### Test Manual
```bash
1. Démarrer l'application
2. Visiter http://localhost:8080
3. Se connecter avec un compte de test
4. Vérifier la redirection vers le bon dashboard
5. Accéder au menu de navigation
6. Se déconnecter
```

### Test d'Inscription
```bash
1. Aller à http://localhost:8080/register
2. Remplir tous les champs correctement
3. Soumettre
4. Vérifier que l'utilisateur peut se connecter
5. Tester avec des données invalides (email déjà utilisé, etc.)
```

## ⚙️ Configuration Thymeleaf

```properties
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false              # À false en développement
```

## 📝 Logs

### Affichage des Logs
```properties
logging.level.root=INFO
logging.level.com.iusjc.weschedule=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Logs Importants
- `DataInitializer` : Affiche les comptes créés au démarrage
- `UserDetailsServiceImpl` : Affiche les utilisateurs chargés
- `AuthService` : Affiche les enregistrements et authentifications

## 🚀 Déploiement

### Créer un JAR exécutable
```bash
./mvnw clean package
java -jar target/weschedule-0.0.1-SNAPSHOT.jar
```

### Docker (Optionnel)
Créer un `Dockerfile` pour containeriser l'application.

## 📚 Documentation Complète

Consultez `AUTHENTICATION_DOCUMENTATION.md` pour une documentation détaillée de l'implémentation.

## ❓ FAQ

### Q: Pourquoi mes données disparaissent au redémarrage ?
**R:** La configuration utilise `ddl-auto=create-drop`, qui recrée les tables à chaque démarrage. Changez en `update` en production.

### Q: Comment ajouter un nouvel utilisateur en BD ?
**R:** Aller à `/register` pour ajouter un étudiant ou `/admin/signup` pour un administrateur.

### Q: Puis-je changer le mot de passe d'un utilisateur ?
**R:** Actuellement, non. Une page de profil avec changement de mot de passe peut être ajoutée.

### Q: Quels sont les navigateurs supportés ?
**R:** Chrome, Firefox, Safari, Edge (versions récentes). Bootstrap 5 assure la compatibilité.

## 🤝 Support

Pour toute question ou problème :
1. Vérifier la console des logs
2. Vérifier la configuration de la base de données
3. S'assurer que le port 8080 est disponible

## 📄 Licences

Spring Boot, Bootstrap, et Thymeleaf sont sous licences open-source.

---

**Bonne utilisation de WeSchedule !** 🎉
