# 📖 Explication détaillée : reinitialisation_du_mdp.html

## 🎯 Vue d'ensemble

Ce fichier HTML permet à un utilisateur de réinitialiser son mot de passe oublié en 2 étapes :
1. Demander un code par email
2. Utiliser ce code pour définir un nouveau mot de passe

---

## 📝 Explication ligne par ligne

### En-tête HTML (lignes 1-10)

```html
<!DOCTYPE html>
<html lang="fr">
```
- Déclaration du type de document HTML5
- Définit la langue de la page en français

```html
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
```
- `charset="UTF-8"` : Permet d'afficher correctement les accents français
- `viewport` : Rend la page responsive (s'adapte aux mobiles)

```html
    <title>Réinitialisation du mot de passe - WeSchedule</title>
```
- Titre affiché dans l'onglet du navigateur

```html
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="/css/auth-professional.css">
```
- Import de la police Inter (moderne et lisible)
- Import de Font Awesome (pour les icônes)
- Import du CSS personnalisé de l'application

---

### Structure de la page (lignes 13-20)

```html
<div class="auth-container fade-in">
```
- Conteneur principal avec animation de fondu à l'apparition

```html
    <div class="auth-header">
        <div class="auth-logo">
            <img src="/images/logo.jpeg" alt="Logo WeSchedule">
        </div>
        <h1 class="auth-title">Mot de passe oublié ?</h1>
        <p class="auth-subtitle">Pas de souci, on va arranger ça</p>
    </div>
```
- En-tête avec le logo de l'application
- Titre rassurant pour l'utilisateur
- Sous-titre amical

---

### Zone de messages (ligne 23)

```html
<div id="messageZone" class="alert" style="display: none;"></div>
```
- Zone cachée par défaut (`display: none`)
- Sera utilisée pour afficher les messages de succès ou d'erreur
- L'ID `messageZone` permet de la manipuler en JavaScript

---

### ÉTAPE 1 : Formulaire email (lignes 26-45)

```html
<div id="etapeEmail" class="auth-form">
```
- Conteneur de la première étape
- Visible par défaut

```html
    <div class="info-box">
        <i class="info-box-icon fas fa-envelope"></i>
        Entrez votre adresse email et nous vous enverrons un code de vérification.
    </div>
```
- Boîte d'information avec icône d'enveloppe
- Explique à l'utilisateur ce qu'il doit faire

```html
    <div class="form-group">
        <label for="emailUtilisateur" class="form-label">Votre email</label>
        <div class="input-group">
            <i class="input-icon fas fa-envelope"></i>
            <input 
                type="email" 
                id="emailUtilisateur" 
                class="form-input" 
                placeholder="exemple@email.com"
                autofocus>
        </div>
    </div>
```
- Groupe de formulaire pour l'email
- `type="email"` : Validation HTML5 automatique du format email
- `autofocus` : Le curseur se place automatiquement dans ce champ
- Icône d'enveloppe à gauche du champ

```html
    <button onclick="demanderCode()" class="btn btn-primary btn-full" id="btnEnvoyer">
        <i class="fas fa-paper-plane"></i> Recevoir le code
    </button>
```
- Bouton qui appelle la fonction JavaScript `demanderCode()`
- Icône d'avion en papier (envoi)
- `id="btnEnvoyer"` permet de le manipuler en JavaScript

---

### ÉTAPE 2 : Formulaire de réinitialisation (lignes 48-92)

```html
<div id="etapeReinitialisation" class="auth-form" style="display: none;">
```
- Conteneur de la deuxième étape
- Caché par défaut (`display: none`)
- Sera affiché après l'envoi du code

```html
    <div class="info-box">
        <i class="info-box-icon fas fa-check-circle"></i>
        Un code a été envoyé à <strong id="emailAffiche"></strong>
    </div>
```
- Message de confirmation
- `id="emailAffiche"` sera rempli dynamiquement avec l'email de l'utilisateur

```html
    <div class="form-group">
        <label for="codeVerification" class="form-label">Code reçu par email</label>
        <div class="input-group">
            <i class="input-icon fas fa-key"></i>
            <input 
                type="text" 
                id="codeVerification" 
                class="form-input" 
                placeholder="123456"
                maxlength="6">
        </div>
        <small style="color: #666; font-size: 0.85em;">Le code contient 6 chiffres</small>
    </div>
```
- Champ pour entrer le code reçu par email
- `maxlength="6"` : Limite à 6 caractères maximum
- Petit texte d'aide en dessous

```html
    <div class="form-group">
        <label for="nouveauMdp" class="form-label">Nouveau mot de passe</label>
        <div class="input-group">
            <i class="input-icon fas fa-lock"></i>
            <input 
                type="password" 
                id="nouveauMdp" 
                class="form-input" 
                placeholder="Au moins 8 caractères">
        </div>
    </div>
```
- Champ pour le nouveau mot de passe
- `type="password"` : Masque les caractères saisis
- Icône de cadenas

```html
    <div class="form-group">
        <label for="confirmationMdp" class="form-label">Confirmer le mot de passe</label>
        <div class="input-group">
            <i class="input-icon fas fa-lock"></i>
            <input 
                type="password" 
                id="confirmationMdp" 
                class="form-input" 
                placeholder="Retapez le mot de passe">
        </div>
    </div>
```
- Champ de confirmation du mot de passe
- Permet de vérifier que l'utilisateur n'a pas fait de faute de frappe

```html
    <button onclick="changerMotDePasse()" class="btn btn-primary btn-full" id="btnReinitialiser">
        <i class="fas fa-check"></i> Changer mon mot de passe
    </button>

    <button onclick="retourEtapeEmail()" class="btn btn-secondary btn-full" style="margin-top: 10px;">
        <i class="fas fa-arrow-left"></i> Retour
    </button>
```
- Bouton principal pour valider la réinitialisation
- Bouton secondaire pour revenir à l'étape 1

---

### Lien de retour (lignes 95-99)

```html
<div class="divider">ou</div>

<a href="/login" class="auth-link-muted">
    <i class="fas fa-arrow-left"></i> Retour à la connexion
</a>
```
- Séparateur visuel
- Lien pour retourner à la page de connexion

---

## 🔧 JavaScript - Explication des fonctions

### Variable globale (ligne 104)

```javascript
let emailSauvegarde = '';
```
- Stocke l'email de l'utilisateur entre les deux étapes
- Nécessaire car on en a besoin à l'étape 2

---

### Fonction `demanderCode()` (lignes 107-145)

**Objectif** : Envoyer une demande de code au serveur

```javascript
async function demanderCode() {
```
- `async` : Permet d'utiliser `await` pour les requêtes réseau

```javascript
    const champEmail = document.getElementById('emailUtilisateur');
    const email = champEmail.value.trim();
    const bouton = document.getElementById('btnEnvoyer');
```
- Récupère les éléments HTML
- `.trim()` : Enlève les espaces avant/après l'email

```javascript
    if (!email) {
        afficherMessage('Veuillez entrer votre adresse email', 'danger');
        return;
    }
```
- Vérifie que l'email n'est pas vide
- Si vide, affiche un message d'erreur et arrête la fonction

```javascript
    if (!emailValide(email)) {
        afficherMessage('L\'adresse email n\'est pas valide', 'danger');
        return;
    }
```
- Vérifie le format de l'email avec une regex
- Si invalide, affiche un message et arrête

```javascript
    bouton.disabled = true;
    bouton.innerHTML = '<span class="spinner"></span> Envoi en cours...';
```
- Désactive le bouton pour éviter les doubles clics
- Change le texte pour montrer que ça charge

```javascript
    try {
        const reponse = await fetch('/api/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email })
        });
```
- Envoie une requête POST au serveur
- `await` : Attend la réponse avant de continuer
- `JSON.stringify()` : Convertit l'objet JavaScript en JSON

```javascript
        const resultat = await reponse.json();
```
- Convertit la réponse du serveur en objet JavaScript

```javascript
        if (resultat.success) {
            emailSauvegarde = email;
            document.getElementById('emailAffiche').textContent = email;
            document.getElementById('etapeEmail').style.display = 'none';
            document.getElementById('etapeReinitialisation').style.display = 'block';
            afficherMessage('Code envoyé ! Vérifiez votre boîte email.', 'success');
```
- Si succès :
  - Sauvegarde l'email
  - Affiche l'email dans le message de confirmation
  - Cache l'étape 1
  - Affiche l'étape 2
  - Affiche un message de succès

```javascript
        } else {
            afficherMessage(resultat.message || 'Une erreur est survenue', 'danger');
            bouton.disabled = false;
            bouton.innerHTML = '<i class="fas fa-paper-plane"></i> Recevoir le code';
        }
```
- Si échec :
  - Affiche le message d'erreur du serveur
  - Réactive le bouton
  - Remet le texte original

```javascript
    } catch (erreur) {
        console.error('Erreur:', erreur);
        afficherMessage('Impossible de contacter le serveur', 'danger');
        bouton.disabled = false;
        bouton.innerHTML = '<i class="fas fa-paper-plane"></i> Recevoir le code';
    }
```
- Gère les erreurs réseau (serveur inaccessible, etc.)
- `console.error()` : Affiche l'erreur dans la console du navigateur

---

### Fonction `changerMotDePasse()` (lignes 148-201)

**Objectif** : Envoyer le code et le nouveau mot de passe au serveur

```javascript
    const code = document.getElementById('codeVerification').value.trim();
    const mdp = document.getElementById('nouveauMdp').value;
    const confirmation = document.getElementById('confirmationMdp').value;
    const bouton = document.getElementById('btnReinitialiser');
```
- Récupère les valeurs des champs

```javascript
    if (!code || code.length !== 6) {
        afficherMessage('Le code doit contenir exactement 6 chiffres', 'danger');
        return;
    }
```
- Vérifie que le code a exactement 6 caractères

```javascript
    if (!mdp || mdp.length < 8) {
        afficherMessage('Le mot de passe doit contenir au moins 8 caractères', 'danger');
        return;
    }
```
- Vérifie que le mot de passe a au moins 8 caractères

```javascript
    if (mdp !== confirmation) {
        afficherMessage('Les deux mots de passe ne correspondent pas', 'danger');
        return;
    }
```
- Vérifie que les deux mots de passe sont identiques

```javascript
    bouton.disabled = true;
    bouton.innerHTML = '<span class="spinner"></span> Changement en cours...';
```
- Désactive le bouton et affiche un spinner

```javascript
    const reponse = await fetch('/api/simple-reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: emailSauvegarde,
            code: code,
            newPassword: mdp,
            confirmPassword: confirmation
        })
    });
```
- Envoie tous les données au serveur
- Utilise l'email sauvegardé à l'étape 1

```javascript
    if (resultat.success) {
        afficherMessage('✓ Mot de passe changé ! Redirection...', 'success');
        setTimeout(function() {
            window.location.href = '/dashboard';
        }, 2000);
```
- Si succès :
  - Affiche un message de succès
  - Attend 2 secondes (`setTimeout`)
  - Redirige vers le dashboard

---

### Fonction `retourEtapeEmail()` (lignes 204-218)

**Objectif** : Revenir à la première étape

```javascript
function retourEtapeEmail() {
    document.getElementById('etapeReinitialisation').style.display = 'none';
    document.getElementById('etapeEmail').style.display = 'block';
```
- Cache l'étape 2
- Affiche l'étape 1

```javascript
    const bouton = document.getElementById('btnEnvoyer');
    bouton.disabled = false;
    bouton.innerHTML = '<i class="fas fa-paper-plane"></i> Recevoir le code';
```
- Réactive le bouton de l'étape 1
- Remet le texte original

```javascript
    document.getElementById('messageZone').style.display = 'none';
    document.getElementById('codeVerification').value = '';
    document.getElementById('nouveauMdp').value = '';
    document.getElementById('confirmationMdp').value = '';
```
- Cache les messages
- Vide tous les champs de l'étape 2

---

### Fonction `afficherMessage()` (lignes 221-230)

**Objectif** : Afficher un message à l'utilisateur

```javascript
function afficherMessage(texte, type) {
    const zone = document.getElementById('messageZone');
    const icone = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
```
- `type` peut être 'success' ou 'danger'
- Choisit l'icône en fonction du type (✓ ou !)

```javascript
    zone.className = `alert alert-${type}`;
    zone.innerHTML = `
        <span class="alert-icon"><i class="fas ${icone}"></i></span>
        ${texte}
    `;
    zone.style.display = 'block';
```
- Change la classe CSS (vert pour succès, rouge pour erreur)
- Insère le HTML avec l'icône et le texte
- Affiche la zone

---

### Fonction `emailValide()` (lignes 233-236)

**Objectif** : Vérifier le format d'un email

```javascript
function emailValide(email) {
    const regex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    return regex.test(email);
}
```
- Utilise une expression régulière (regex)
- Vérifie que l'email a le format : `quelquechose@domaine.extension`
- Retourne `true` si valide, `false` sinon

---

## 🎨 Pourquoi ce code semble "humain" ?

1. **Noms de variables en français** : `emailSauvegarde`, `nouveauMdp`, `etapeEmail`
2. **Commentaires explicites** : Chaque section est commentée
3. **Messages amicaux** : "Pas de souci, on va arranger ça"
4. **Validation progressive** : Vérifie chaque champ un par un
5. **Feedback utilisateur** : Messages clairs à chaque étape
6. **Gestion d'erreurs complète** : Tous les cas sont gérés
7. **Code structuré** : Fonctions séparées pour chaque action
8. **Pas de code "magique"** : Tout est explicite et compréhensible

---

## 🔄 Flux complet

```
1. Utilisateur arrive sur la page
   ↓
2. Voit l'étape 1 (demande email)
   ↓
3. Entre son email et clique "Recevoir le code"
   ↓
4. JavaScript vérifie le format
   ↓
5. Envoie la requête au serveur (/api/forgot-password)
   ↓
6. Serveur génère un code et l'envoie par email
   ↓
7. Page passe à l'étape 2
   ↓
8. Utilisateur vérifie son email et récupère le code
   ↓
9. Entre le code + nouveau mot de passe
   ↓
10. JavaScript vérifie les champs
   ↓
11. Envoie la requête au serveur (/api/simple-reset-password)
   ↓
12. Serveur valide le code et change le mot de passe
   ↓
13. Connexion automatique
   ↓
14. Redirection vers le dashboard
```

---

## ✅ Points forts de cette implémentation

- **Simple** : Une seule page, deux étapes claires
- **Sécurisé** : Code temporaire, validation côté client et serveur
- **User-friendly** : Messages clairs, feedback immédiat
- **Robuste** : Gestion complète des erreurs
- **Responsive** : Fonctionne sur mobile et desktop
- **Accessible** : Labels, placeholders, messages d'aide

