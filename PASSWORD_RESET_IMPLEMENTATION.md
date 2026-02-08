# 🔐 Implémentation - Mot de Passe Oublié avec Code Email

## 📋 Résumé

Implémentation complète du système de réinitialisation de mot de passe avec:
- ✅ Envoi de code par email Gmail
- ✅ Validation du code (6 chiffres)
- ✅ Réinitialisation sécurisée du mot de passe
- ✅ Interface à 2 étapes

---

## 📦 Dépendances Ajoutées

### pom.xml
```xml
<!-- Spring Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## ⚙️ Configuration Email (application.properties)

```properties
# ==================== EMAIL CONFIGURATION ====================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=iulp562@gmail.com
spring.mail.password=tnlf dzxa rqvt tryx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

---

## 📁 Fichiers Créés

### 1. **PasswordResetToken.java** (Modèle)
- **Chemin**: `src/main/java/.../models/PasswordResetToken.java`
- **Table**: `password_reset_tokens`
- **Colonnes**:
  - `id` (PK)
  - `token` (Code à 6 chiffres, unique)
  - `utilisateur_id` (FK)
  - `expiryDate` (Expire après 15 minutes)
  - `used` (Marqué comme utilisé après réinitialisation)
  - `createdAt` (Timestamp de création)
- **Méthodes**: `isExpired()` - vérifie si le token a expiré

### 2. **PasswordResetTokenRepository.java** (Repository)
- Méthodes:
  - `findByToken(String token)` - Récupère le token par code
  - `findByUtilisateur(Utilisateur u)` - Récupère le token d'un utilisateur
  - `deleteByUtilisateur(Utilisateur u)` - Supprime les tokens antérieurs

### 3. **EmailService.java** (Service)
- **Méthode principale**: `sendPasswordResetEmail(email, name, code)`
- **Fonctionnalités**:
  - Utilise `JavaMailSender` de Spring
  - Génère un email formaté avec le code
  - Gère les exceptions
  - Logs des opérations

### 4. **PasswordResetService.java** (Service)
- **Méthodes**:

#### `initiatePasswordReset(email)`
- Vérifie l'existence de l'email en base
- Génère un code aléatoire à 6 chiffres
- Crée un PasswordResetToken avec expiry 15 minutes
- Envoie l'email
- Ne révèle pas si l'email existe (sécurité)

#### `validateResetCode(email, code)`
- Valide que le code correspond
- Vérifie que le code n'est pas expiré
- Vérifie que le code n'a pas déjà été utilisé
- Retourne `true/false`

#### `resetPassword(email, code, newPassword)`
- Valide le code
- Encode le nouveau mot de passe en BCrypt
- Met à jour l'utilisateur
- Marque le token comme utilisé

#### `generateRandomCode()`
- Génère un code aléatoire à 6 chiffres

### 5. **ForgotPasswordRequest.java** (DTO)
```json
{
  "email": "utilisateur@iusjc.edu"
}
```

### 6. **ResetPasswordRequest.java** (DTO)
```json
{
  "email": "utilisateur@iusjc.edu",
  "code": "123456",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

---

## 🔌 Endpoints API

### 1. Demander un Code
```
POST /api/forgot-password
Content-Type: application/json

{
  "email": "user@iusjc.edu"
}
```

**Réponse (200)**:
```json
{
  "success": true,
  "message": "Un code de réinitialisation a été envoyé à votre email si le compte existe"
}
```

### 2. Vérifier le Code
```
POST /api/verify-reset-code
Content-Type: application/json

{
  "email": "user@iusjc.edu",
  "code": "123456"
}
```

**Réponse (200)**:
```json
{
  "success": true,
  "message": "Code valide"
}
```

### 3. Réinitialiser le Mot de Passe
```
POST /api/reset-password
Content-Type: application/json

{
  "email": "user@iusjc.edu",
  "code": "123456",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Réponse (200)**:
```json
{
  "success": true,
  "message": "Votre mot de passe a été réinitialisé avec succès. Veuillez vous connecter."
}
```

---

## 🎨 Interface Utilisateur (login.html)

### Modal de Réinitialisation
- **2 étapes**:

#### Étape 1: Demander l'Email
- Champ email
- Bouton "Envoyer le code"
- Message de confirmation

#### Étape 2: Réinitialiser
- Champ code (6 chiffres)
- Champ nouveau mot de passe (min 8 caractères)
- Champ confirmation mot de passe
- Bouton "Réinitialiser"
- Bouton "Retour"

### Transitions
- Email reçoit un code → passage à l'étape 2
- Code validé & passwords OK → réinitialisation réussie
- Succès → fermeture du modal + message global

---

## 🔒 Sécurité

### Mesures de Sécurité Implémentées

1. **Code de Réinitialisation**
   - Code aléatoire à 6 chiffres
   - Unique et one-time use
   - Expire après 15 minutes

2. **Mot de Passe**
   - Minimum 8 caractères requis
   - Encodage BCrypt
   - Confirmation requise

3. **Email**
   - Ne révèle pas si l'email existe
   - SMTP sécurisé (TLS)
   - Authentification nécessaire

4. **Token**
   - Stocké en base avec utilisateur
   - Marqué comme utilisé après réinitialisation
   - Suppression des anciens tokens

5. **Validation**
   - Validation côté serveur complète
   - Gestion d'exceptions appropriée
   - Logs des opérations sensibles

---

## 📧 Format de l'Email Reçu

```
Sujet: IUSJC - Réinitialisation de votre mot de passe

---

Bonjour [Prénom Nom],

Vous avez demandé la réinitialisation de votre mot de passe pour IUSJC WeSchedule.

Voici votre code de réinitialisation :
==========================================
[123456]
==========================================

Ce code est valable pendant 15 minutes.

Instructions :
1. Rendez-vous sur la page de réinitialisation
2. Entrez votre email
3. Entrez le code reçu
4. Créez un nouveau mot de passe

Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.

Cordialement,
L'équipe IUSJC

---
Message automatique - Veuillez ne pas répondre à cet email.
```

---

## 🔄 Flux Utilisateur Complet

```
1. Utilisateur sur page login
   ↓
2. Clique sur "Mot de passe oublié?"
   ↓
3. Modal s'ouvre - Étape 1
   ↓
4. Entre son email
   ↓
5. Clique "Envoyer le code"
   ↓
6. Email reçoit le code
   ↓
7. Frontend passe à l'étape 2
   ↓
8. Utilisateur entre le code
   ↓
9. Utilisateur entre nouveau mot de passe
   ↓
10. Clique "Réinitialiser"
   ↓
11. Backend valide le code
   ↓
12. Backend met à jour le mot de passe
   ↓
13. Backend marque le token comme utilisé
   ↓
14. Utilisateur reçoit message de succès
   ↓
15. Modal ferme
   ↓
16. Utilisateur peut se connecter avec nouveau mot de passe
```

---

## 📋 Validations

### Côté Frontend
- ✅ Email non vide et valide
- ✅ Code = 6 chiffres
- ✅ Mot de passe ≥ 8 caractères
- ✅ Passwords correspondent
- ✅ Messages d'erreur clairs

### Côté Backend
- ✅ Email existe en base
- ✅ Code valide et non expiré
- ✅ Code pas déjà utilisé
- ✅ Mot de passe ≥ 8 caractères
- ✅ Encodage BCrypt

---

## ⚠️ Points Importants

1. **Expiration Code**
   - Code valide 15 minutes
   - Après expiration, demander un nouveau code

2. **One-Time Use**
   - Code ne peut être utilisé qu'une fois
   - Token marqué comme `used = true` après utilisation

3. **Email Sécurisé**
   - App password Gmail utilisé (pas mot de passe principal)
   - TLS activé
   - Auth requise

4. **Anciens Tokens**
   - Supprimés automatiquement lors d'une nouvelle demande
   - Un seul token actif par utilisateur

5. **Confidentialité**
   - Ne révèle pas si email existe
   - Message générique pour tous les cas

---

## 🧪 Test de Fonctionnalité

### Scénario 1: Happy Path
```
1. POST /api/forgot-password avec email valide
   ✓ Reçoit email avec code
2. POST /api/verify-reset-code avec code correct
   ✓ Code validé
3. POST /api/reset-password avec code + nouveau password
   ✓ Mot de passe changé
4. Connexion avec nouveau mot de passe
   ✓ Succès
```

### Scénario 2: Code Invalide
```
1. POST /api/forgot-password
   ✓ Email envoyé
2. POST /api/reset-password avec mauvais code
   ✗ Code invalide ou expiré (erreur 400)
```

### Scénario 3: Code Expiré
```
1. POST /api/forgot-password
   ✓ Email envoyé
2. Attendre 15+ minutes
3. POST /api/reset-password
   ✗ Code invalide ou expiré (erreur 400)
   → Demander nouveau code
```

---

## 📊 Base de Données

### Table: password_reset_tokens
```sql
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
);
```

---

## 🚀 Déploiement

### Avant de déployer
1. ✅ Vérifier credentials Gmail
2. ✅ Vérifier app password (pas mot de passe principal)
3. ✅ Tester envoi d'email
4. ✅ Vérifier TLS/SMTP settings
5. ✅ Vérifier la BD est à jour

### Commandes
```bash
mvn clean package
java -jar target/weschedule.jar
```

---

## 📚 Technologies Utilisées

- **Backend**: Spring Boot 3.5.8, Spring Security
- **Email**: JavaMailSender (Spring Mail)
- **Encodage**: BCrypt
- **Base**: MySQL
- **Frontend**: HTML5, Bootstrap 5.3.3, Vanilla JS
- **API**: REST

---

## 📝 Notes Supplémentaires

- Code généré aléatoirement (100000-999999)
- Expiry: 15 minutes (configurable via `RESET_TOKEN_EXPIRY_MINUTES`)
- Longueur code: 6 chiffres (configurable via `RESET_CODE_LENGTH`)
- Tous les endpoints sont `@PostMapping`
- Pas d'authentification requise pour réinitialiser
- Logs des opérations sensibles

---

**Date**: 24 Novembre 2026  
**Status**: ✅ Production Ready  
**Version**: 2.3
