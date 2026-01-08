#!/bin/bash
# Script de Référence - Commandes Utiles pour WeSchedule

## 🚀 DÉMARRAGE DE L'APPLICATION

# Démarrer l'application avec Maven
./mvnw spring-boot:run

# Ou créer un JAR et le lancer
./mvnw clean package
java -jar target/weschedule-0.0.1-SNAPSHOT.jar

## 🏗️ COMPILATION ET BUILD

# Compilation complète
./mvnw clean compile

# Compilation avec tests
./mvnw clean package

# Compilation rapide (sans nettoyage)
./mvnw compile

# Compiler un profil spécifique
./mvnw -Pdev clean compile

## 🧹 NETTOYAGE

# Nettoyer les fichiers générés
./mvnw clean

# Nettoyer la cache Maven
./mvnw clean -U

## 🗄️ BASE DE DONNÉES

# Créer la base de données MySQL
mysql -u root -p
CREATE DATABASE IF NOT EXISTS weshedule;
CREATE DATABASE IF NOT EXISTS weshedule_test;

# Ou en une ligne
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS weshedule;"

## 📝 LOGS ET DEBUG

# Voir les logs détaillés de compilation
./mvnw -X clean compile

# Afficher les logs du test
./mvnw test -DreuseForks=false -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

## 🧪 TESTS

# Exécuter tous les tests
./mvnw test

# Exécuter un test spécifique
./mvnw test -Dtest=TestClass

# Ignorer les tests pendant la compilation
./mvnw package -DskipTests

# Couverture de code
./mvnw test jacoco:report

## 🔧 DÉVELOPPEMENT

# Lancer en mode développement avec rechargement automatique
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Lancer avec un profil spécifique
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Lancer sur un port spécifique
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"

## 📊 RAPPORTS

# Générer le rapport de site Maven
./mvnw site

# Générer la javadoc
./mvnw javadoc:javadoc

## 🔍 INSPECTION DU CODE

# Analyser le code avec FindBugs
./mvnw findbugs:check

# Vérifier la qualité du code (PMD)
./mvnw pmd:check

# SpotBugs (alternative FindBugs)
./mvnw com.github.spotbugs:spotbugs-maven-plugin:check

## 📦 DÉPENDANCES

# Lister les dépendances
./mvnw dependency:tree

# Afficher les dépendances non utilisées
./mvnw dependency:analyze

# Mettre à jour les dépendances
./mvnw versions:display-dependency-updates

## 🔐 SÉCURITÉ

# Vérifier les vulnérabilités (OWASP Dependency Check)
./mvnw org.owasp:dependency-check-maven:check

# Vérifier les dépendances avec vulnerabilities
./mvnw security:check

## 📋 AUDIT

# Vérifier la licence des dépendances
./mvnw license:check

# Générer un rapport de licence
./mvnw license:aggregate-report

## 🚀 DÉPLOIEMENT

# Créer un JAR exécutable
./mvnw clean package

# Créer un WAR pour déploiement
mvn clean package -DskipTests=true

# Déployer vers un serveur
# (Nécessite configuration dans pom.xml)
./mvnw deploy

## 🐳 DOCKER (si Dockerfile existe)

# Construire l'image Docker
docker build -t weschedule:1.0 .

# Lancer le conteneur
docker run -p 8080:8080 weschedule:1.0

## 🔄 INTÉGRATION CONTINUE

# Faire tout (clean, compile, test, package)
./mvnw clean install

# Profil CI
./mvnw clean install -Pci

## 🎯 RACCOURCIS UTILES

# Build rapide (pas de tests, pas de site)
alias build-quick='./mvnw clean compile'

# Build complet
alias build-full='./mvnw clean install'

# Démarrer l'appli
alias start-app='./mvnw spring-boot:run'

# Voir l'arborescence des dépendances
alias deps='./mvnw dependency:tree'

## 💡 COMMANDES AVANCÉES

# Exécuter des tests spécifiques avec un pattern
./mvnw test -Dtest=*AuthController*

# Compiler en mode release
./mvnw clean compile -Prelease

# Sauter la validation de la javadoc
./mvnw clean install -Dmaven.javadoc.skip=true

# Augmenter la mémoire pour Maven
MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
export MAVEN_OPTS
./mvnw clean install

## 📱 ACCÈS À L'APPLICATION

# URL locale
http://localhost:8080/

# Page de connexion
http://localhost:8080/login

# Page d'inscription
http://localhost:8080/register

# Inscription admin
http://localhost:8080/admin/signup

# Comptes de test
# Email: admin@test.com      / Mot de passe: password123
# Email: teacher@test.com    / Mot de passe: password123
# Email: student@test.com    / Mot de passe: password123

## 🔧 CONFIGURATION MAVEN

# Afficher les propriétés Maven
./mvnw help:describe

# Afficher la version
./mvnw --version

# Afficher l'aide
./mvnw help:help

## 🐛 TROUBLESHOOTING

# Si port 8080 déjà utilisé, chercher le processus
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac:
lsof -i :8080
kill -9 <PID>

# Si MySQL non accessible:
# Vérifier la connexion
mysql -h localhost -u root -p

# Si compilation échoue:
./mvnw clean
./mvnw compile -U

## 📚 DOCUMENTATION

# Consulter la documentation officielle Maven
https://maven.apache.org/

# Documentation Spring Boot
https://spring.io/projects/spring-boot

# Documentation Spring Security
https://spring.io/projects/spring-security

---

## 💾 VARIABLES D'ENVIRONNEMENT UTILES

# Windows PowerShell:
$env:MAVEN_OPTS = "-Xmx1024m"
./mvnw clean compile

# Linux/Mac:
export MAVEN_OPTS="-Xmx1024m"
./mvnw clean compile

# Définir le profil actif
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run

---

## 📝 NOTES

- `mvn` = Maven (si installé globalement)
- `./mvnw` = Maven Wrapper (local, recommandé)
- `-q` = Mode silencieux (moins de logs)
- `-X` = Debug (plus de logs)
- `-U` = Force mise à jour des dépendances
- `-o` = Mode offline (utilise cache)

---

## 🎯 WORKFLOW COURANT

1. Développer les features
   ./mvnw clean compile

2. Tester
   ./mvnw test

3. Compiler complètement
   ./mvnw clean package

4. Lancer l'application
   ./mvnw spring-boot:run

5. Accéder à http://localhost:8080

---

**Date** : 24 novembre 2025  
**Projet** : WeSchedule v1.0  
**Framework** : Spring Boot 3.5.8  
**JDK** : Java 17+
