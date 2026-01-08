# Notes Techniques - Héritage JPA et Structure de la Sécurité

## 🏗️ Structure de l'Héritage

### Architecture Hiérarchique

```
┌─────────────────────────┐
│    Utilisateur          │ (Parent - Table parente)
│─────────────────────────│
│ idUser (PK)             │
│ nom                     │
│ prenom                  │
│ email (unique)          │
│ phone                   │
│ motDePasse              │ (Hashé avec BCrypt)
│ role                    │ (ADMINISTRATEUR, ENSEIGNANT, ETUDIANT)
│ DTYPE                   │ (Discriminant pour l'héritage)
└─────────────────────────┘
        △
        │
    ┌───┴───────────────┬─────────────────┐
    │                   │                 │
    ▼                   ▼                 ▼
┌──────────┐    ┌────────────┐    ┌───────────┐
│Admin.    │    │Enseignant  │    │Etudiant   │
│────────  │    │────────────│    │───────────│
│(héritage)│    │grade       │    │filiere    │
│          │    │matiere     │    │niveau     │
│          │    │disponibilite    │groupe     │
└──────────┘    └────────────┘    └───────────┘
```

### Implémentation JPA

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "utilisateurs")
public class Utilisateur {
    // Attributs communs
}

@Entity
@Table(name = "administrateurs")
@DiscriminatorValue("ADMINISTRATEUR")
public class Administrateur extends Utilisateur { }

@Entity
@Table(name = "enseignants")
@DiscriminatorValue("ENSEIGNANT")
public class Enseignant extends Utilisateur { }

@Entity
@Table(name = "etudiants")
@DiscriminatorValue("ETUDIANT")
public class Etudiant extends Utilisateur { }
```

## 🔐 Flux de Sécurité

### 1. Enregistrement

```
Input (RegisterRequest)
    ↓
AuthService.register()
    ├─ Validation des données
    ├─ Vérification unicité email
    ├─ Création d'instance (Administrateur/Enseignant/Etudiant)
    ├─ Hashage mot de passe (BCryptPasswordEncoder)
    └─ Sauvegarde en BD via repository approprié
        ↓
Output (AuthResponse)
```

#### Code d'Enregistrement

```java
// Exemple : Enregistrement d'un étudiant
Etudiant etudiant = new Etudiant();
etudiant.setNom(request.getNom());
etudiant.setPrenom(request.getPrenom());
etudiant.setEmail(request.getEmail());
etudiant.setPhone(request.getPhone());
// BCryptPasswordEncoder hash automatiquement
etudiant.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
etudiant.setRole(Role.ETUDIANT);
etudiantRepository.save(etudiant);
```

### 2. Authentification

```
POST /login (email + motDePasse)
    ↓
AuthenticationManager
    ↓
DaoAuthenticationProvider
    ├─ UserDetailsServiceImpl.loadUserByUsername(email)
    │   └─ UtilisateurRepository.findByEmail(email)
    │       └─ Charge depuis la BD (polymorphe)
    ├─ Vérifie le mot de passe (BCrypt.matches)
    └─ Crée Authentication object
        ↓
UserPrincipal (wraps Utilisateur)
    ├─ getAuthorities() → ROLE_ADMINISTRATEUR / ROLE_ENSEIGNANT / ROLE_ETUDIANT
    ├─ getPassword() → mot de passe hashé
    └─ getUsername() → email
        ↓
Session HTTP créée
    ↓
Redirection vers /dashboard
```

## 🔍 Chargement Polymorphe

### Comment Spring charge le bon type

```java
// UtilisateurRepository.findByEmail("teacher@test.com")
// Retourne une instance de Enseignant (polymorphiquement)

Optional<Utilisateur> optionalUser = utilisateurRepository.findByEmail(email);
// optionalUser.get() est une instance de Enseignant

// Possible d'y accéder comme Enseignant
if (optionalUser.get() instanceof Enseignant) {
    Enseignant enseignant = (Enseignant) optionalUser.get();
    String matiere = enseignant.getMatiere();
}
```

### Requête SQL Générée

```sql
SELECT u.* 
FROM utilisateurs u
LEFT JOIN administrateurs a ON u.id_user = a.id_user
LEFT JOIN enseignants e ON u.id_user = e.id_user
LEFT JOIN etudiants et ON u.id_user = et.id_user
WHERE u.email = 'teacher@test.com'
```

L'ORM Hibernate joint automatiquement les sous-tables pour recréer l'objet complet.

## 🔐 Sécurité Spring Security

### DaoAuthenticationProvider

```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}
```

**Étapes** :
1. Charge l'utilisateur via UserDetailsService
2. Récupère le mot de passe hashé
3. Utilise PasswordEncoder.matches() pour vérifier
4. Si OK → crée un Authentication token
5. Si KO → throw BadCredentialsException

### UserPrincipal

```java
public class UserPrincipal implements UserDetails {
    private Utilisateur utilisateur;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertit Role.ENSEIGNANT → ROLE_ENSEIGNANT
        return List.of(new SimpleGrantedAuthority(
            "ROLE_" + utilisateur.getRole().name()
        ));
    }
}
```

## 🎯 Contrôle d'Accès

### SecurityConfig

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/", "/login", "/register", "/admin/signup").permitAll()
    .requestMatchers("/dashboard/admin").hasRole("ADMINISTRATEUR")
    .requestMatchers("/dashboard/enseignant").hasRole("ENSEIGNANT")
    .requestMatchers("/dashboard/etudiant").hasRole("ETUDIANT")
    .anyRequest().authenticated()
)
```

### AuthController

```java
@GetMapping("/dashboard")
public String dashboard(Authentication auth, Model model) {
    UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
    Role role = userPrincipal.getUtilisateur().getRole();
    
    switch(role) {
        case ADMINISTRATEUR:
            return "redirect:/dashboard/admin";
        case ENSEIGNANT:
            return "redirect:/dashboard/enseignant";
        case ETUDIANT:
            return "redirect:/dashboard/etudiant";
    }
}
```

## 📊 Structure de BD

### Table Utilisateurs (Parente)

```sql
CREATE TABLE utilisateurs (
    id_user BINARY(16) PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('ADMINISTRATEUR', 'ENSEIGNANT', 'ETUDIANT') NOT NULL,
    dtype VARCHAR(31) NOT NULL
);
```

### Table Enfants

```sql
CREATE TABLE administrateurs (
    id_user BINARY(16) PRIMARY KEY,
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);

CREATE TABLE enseignants (
    id_user BINARY(16) PRIMARY KEY,
    grade VARCHAR(100),
    matiere VARCHAR(100),
    disponibilite VARCHAR(255),
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);

CREATE TABLE etudiants (
    id_user BINARY(16) PRIMARY KEY,
    filiere VARCHAR(100),
    niveau VARCHAR(50),
    groupe VARCHAR(50),
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);
```

## 🔄 Cycle de Vie d'une Authentification

### Exemple Complet

1. **Utilisateur remplit le formulaire login**
   ```html
   <form th:action="@{/login}" method="post">
       <input name="email" value="teacher@test.com">
       <input name="motDePasse" value="password123">
   </form>
   ```

2. **Spring Security intercepte le POST /login**
   ```
   UsernamePasswordAuthenticationFilter.doFilter()
   ```

3. **DaoAuthenticationProvider cherche l'utilisateur**
   ```java
   UserDetails userDetails = userDetailsService
       .loadUserByUsername("teacher@test.com");
   // Retourne UserPrincipal(Enseignant)
   ```

4. **Vérifie le mot de passe**
   ```java
   if (!passwordEncoder.matches("password123", 
       userDetails.getPassword())) {
       // "$2a$10$..." (BCrypt hash)
       throw new BadCredentialsException(...);
   }
   ```

5. **Crée une session HTTP**
   ```
   Session créée avec l'Authentication
   ```

6. **Redirige vers /dashboard**
   ```
   Détecte le rôle ENSEIGNANT
   Redirige vers /dashboard/enseignant
   ```

7. **Chargement du dashboard**
   ```java
   Authentication auth = SecurityContextHolder
       .getContext().getAuthentication();
   UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
   // Accès à l'utilisateur et ses données
   ```

## ⚠️ Points Important

### 1. Polymorphisme
Les repositories peuvent retourner des instances de Utilisateur, mais ce seront en réalité des instances d'Administrateur, Enseignant ou Etudiant selon le DTYPE.

### 2. Héritage JOINED
Chaque sous-classe a sa propre table, liée à la table parente par une clé étrangère. C'est l'approche la plus flexible pour les requêtes.

### 3. Unicité de l'email
L'email est unique au niveau de la table parente `utilisateurs`. Un email ne peut pas être utilisé pour deux rôles différents.

### 4. InstanceOf
```java
if (utilisateur instanceof Enseignant) {
    Enseignant ens = (Enseignant) utilisateur;
    String matiere = ens.getMatiere();
}
```

### 5. Hashage du Mot de Passe
BCryptPasswordEncoder génère un hash différent chaque fois, donc le même mot de passe aura des hashs différents. C'est normal et voulu pour la sécurité.

---

Pour plus d'informations, consulter la documentation officielle de Spring Security et Hibernate ORM.
