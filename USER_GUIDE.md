# 🚀 Guide d'Utilisation - Interface WeSchedule

## 🌐 Pages Publiques

### 1. **Page d'Accueil** (`/`)
La première impression est importante ! La page d'accueil présente :

- **Navigation principale** : Accès aux différentes sections et CTA de connexion
- **Section Héros** : Titre accrocheur et description succincte
- **Fonctionnalités** : 6 cartes décrivant les capacités principales
- **Avantages** : Pourquoi choisir WeSchedule
- **Statistiques** : Crédibilité et adoption
- **Call-to-Action** : Bouton de connexion

**Accès** : Tous les visiteurs non authentifiés
**Responsive** : ✅ Mobile, Tablet, Desktop

---

### 2. **Page de Connexion** (`/login`)
Formulaire de connexion sécurisé et professionnel :

#### 📝 **Champs**
- **Email** : Votre adresse email institutionnelle
- **Mot de passe** : Mot de passe sécurisé
- **Afficher/Masquer** : Toggle pour voir le mot de passe

#### 🔐 **Fonctionnalités de Sécurité**
- **Mot de passe oublié** : Cliquez sur le lien pour récupérer accès
  1. Entrez votre email
  2. Vérifiez votre boîte mail
  3. Suivez les instructions reçues

#### 💾 **Comptes de Test**
```
Admin:      admin@test.com / password123
Enseignant: teacher@test.com / password123
```

**Accès** : Visiteurs non authentifiés uniquement
**Sécurité** : HTTPS requis en production

---

### 3. **Page d'Inscription** (`/register`)
**Accès restreint** - Cette page indique clairement que :

- ✋ L'inscription self-service est **désactivée**
- 👤 Seuls **Admin** et **Enseignants** peuvent accéder
- 📞 Les comptes sont créés par l'administration
- 📧 Contactez votre responsable IT pour demander un compte

---

## 🔐 **Authentification et Accès**

### **Utilisateurs Autorisés**
1. **Administrateurs** ✅
   - Accès complet au système
   - Gestion des utilisateurs et emplois du temps
   - Dashboard administrateur

2. **Enseignants** ✅
   - Accès à leurs emplois du temps
   - Consultation des ressources
   - Dashboard enseignant

### **Utilisateurs Non Autorisés**
- ❌ **Étudiants** : N'ont pas accès à l'application (voir page `/register`)
- ❌ **Comptes génériques** : Création sélective uniquement

---

## 🎨 **Design et Expérience Utilisateur**

### **Thème Visuel**
- **Couleurs** : Gradient Violet → Violet Foncé
- **Police** : Segoe UI (moderne et lisible)
- **Icônes** : Font Awesome 6.4
- **Animations** : Fluides et professionnelles

### **Responsive Design**
- **Mobile** : Navigation hamburger, layout vertical
- **Tablette** : Adaptation flexible
- **Desktop** : 2 colonnes optimales

### **Accessibilité**
- ♿ Navigation au clavier complète
- 👁️ Contraste conforme WCAG
- 🔊 Lecteur d'écran compatible
- 🌐 Multilingue prêt (FR/EN)

---

## ⚡ **Performance**

- **Chargement rapide** : Optimisé pour 3G+
- **Animations GPU** : Fluides à 60fps
- **SEO-friendly** : Meta tags optimisés
- **Cache** : Mise en cache des assets statiques

---

## 📱 **Accès Mobile**

WeSchedule est **100% responsive** :

```
📱 iPhone 5/SE         → Portrait et Landscape ✅
📱 iPhone 12/13        → Portrait et Landscape ✅
📱 iPad Mini/Air       → Portrait et Landscape ✅
📱 Android 4.4+        → Tous les appareils ✅
💻 Desktop 1024px+     → Expérience optimale ✅
```

---

## 🔧 **Dépannage**

### **"J'ai oublié mon mot de passe"**
1. Allez sur `/login`
2. Cliquez sur "Mot de passe oublié ?"
3. Entrez votre email
4. Vérifiez votre boîte mail (incluant spam)
5. Cliquez le lien de réinitialisation

### **"Je ne peux pas me connecter"**
- Vérifiez votre email exact
- Vérifiez CAPS LOCK
- Essayez depuis un navigateur différent
- Contactez l'administrateur système

### **"Je dois créer un compte"**
- L'inscription self-service est **désactivée** pour sécurité
- Contactez : `it-support@institution.edu`
- L'admin créera votre compte et vous enverra les identifiants

---

## 🌟 **Nouveautés dans cette Version**

### ✨ **Page d'Accueil Améliorée**
- Landing page professionnelle complète
- Présentation claire des fonctionnalités
- Appel à l'action visuel

### 🎨 **Login Redesigné**
- Interface moderne et sécurisée
- Récupération de mot de passe intégrée
- Animations fluides

### 🛡️ **Page d'Inscription Sécurisée**
- Message d'accès restreint clair
- Redirection vers les bonnes ressources
- Design cohérent avec le reste

---

## 📞 **Support**

Pour toute question :
- 📧 Email : support@weschedule.local
- 📞 Téléphone : +33 (0)X XX XX XX XX
- 💬 Chat : Disponible dans le dashboard
- 📚 FAQ : https://help.weschedule.local

---

**Dernière mise à jour** : 24 Novembre 2024
**Version** : 2.1
**Statut** : Production
