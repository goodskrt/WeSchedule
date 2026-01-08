# WeSchedule - Script de Démarrage PowerShell
# Ce script facilite le démarrage de l'application

# Configurer la couleur de la console
function Write-Header {
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║                  WeSchedule - Démarrage                        ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
}

function Write-Info {
    param([string]$message)
    Write-Host "ℹ️  $message" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$message)
    Write-Host "✅ $message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$message)
    Write-Host "⚠️  $message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$message)
    Write-Host "❌ $message" -ForegroundColor Red
}

# Menu principal
function Show-Menu {
    Write-Host "`n═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "Que voulez-vous faire ?" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "1️⃣  Démarrer l'application (spring-boot:run)" -ForegroundColor Green
    Write-Host "2️⃣  Compiler le projet (clean compile)" -ForegroundColor Yellow
    Write-Host "3️⃣  Build complet (clean package)" -ForegroundColor Yellow
    Write-Host "4️⃣  Exécuter les tests (mvn test)" -ForegroundColor Blue
    Write-Host "5️⃣  Voir les dépendances (dependency:tree)" -ForegroundColor Magenta
    Write-Host "6️⃣  Nettoyer les fichiers compilés (mvn clean)" -ForegroundColor Gray
    Write-Host "0️⃣  Quitter" -ForegroundColor Red
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
}

# Vérifier Maven
function Check-Maven {
    Write-Info "Vérification de Maven..."
    
    # Vérifier si mvnw existe
    if (Test-Path -Path "./mvnw.cmd") {
        Write-Success "Maven Wrapper trouvé (mvnw)"
        return $true
    } elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Success "Maven installé globalement"
        return $true
    } else {
        Write-Error "Maven non trouvé ! Installez Maven ou le Maven Wrapper"
        return $false
    }
}

# Vérifier Java
function Check-Java {
    Write-Info "Vérification de Java..."
    
    if (Get-Command java -ErrorAction SilentlyContinue) {
        $javaVersion = & java -version 2>&1
        Write-Success "Java trouvé:"
        Write-Host $javaVersion[0] -ForegroundColor Gray
        return $true
    } else {
        Write-Error "Java non trouvé ! Installez Java 17+ "
        return $false
    }
}

# Vérifier MySQL
function Check-MySQL {
    Write-Info "Vérification de MySQL..."
    
    try {
        if (Get-Command mysql -ErrorAction SilentlyContinue) {
            Write-Success "MySQL client trouvé"
            return $true
        } else {
            Write-Warning "MySQL client non trouvé (mais peut-être pas nécessaire si BD sur autre machine)"
            return $false
        }
    } catch {
        Write-Warning "Impossible de vérifier MySQL"
        return $false
    }
}

# Faire du nettoyage
function Clean-Project {
    Write-Header
    Write-Info "Nettoyage du projet..."
    
    if (Test-Path "./mvnw.cmd") {
        & .\mvnw.cmd clean
    } else {
        & mvn clean
    }
    
    Write-Success "Nettoyage terminé !"
}

# Compiler le projet
function Compile-Project {
    Write-Header
    Write-Info "Compilation du projet..."
    
    if (Test-Path "./mvnw.cmd") {
        & .\mvnw.cmd clean compile
    } else {
        & mvn clean compile
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Compilation réussie !"
    } else {
        Write-Error "Erreur de compilation"
    }
}

# Build complet (package)
function Build-Project {
    Write-Header
    Write-Info "Build complet du projet (peut prendre du temps)..."
    
    if (Test-Path "./mvnw.cmd") {
        & .\mvnw.cmd clean package -DskipTests
    } else {
        & mvn clean package -DskipTests
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Build réussie !"
        Write-Info "Le JAR est disponible dans : target/weschedule-0.0.1-SNAPSHOT.jar"
    } else {
        Write-Error "Erreur lors du build"
    }
}

# Exécuter les tests
function Run-Tests {
    Write-Header
    Write-Info "Exécution des tests..."
    
    if (Test-Path "./mvnw.cmd") {
        & .\mvnw.cmd test
    } else {
        & mvn test
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Tests réussis !"
    } else {
        Write-Warning "Certains tests ont échoué"
    }
}

# Voir les dépendances
function Show-Dependencies {
    Write-Header
    Write-Info "Affichage des dépendances..."
    
    if (Test-Path "./mvnw.cmd") {
        & .\mvnw.cmd dependency:tree
    } else {
        & mvn dependency:tree
    }
}

# Démarrer l'application
function Start-Application {
    Write-Header
    Write-Info "Démarrage de l'application WeSchedule..."
    Write-Warning "L'application se lance sur http://localhost:8080"
    Write-Info "Appuyez sur Ctrl+C pour arrêter l'application"
    Write-Host ""
    
    Start-Sleep -Seconds 2
    
    if (Test-Path "./mvnw.cmd") {
        & .\mvnw.cmd spring-boot:run
    } else {
        & mvn spring-boot:run
    }
}

# Afficher les instructions
function Show-Instructions {
    Write-Header
    Write-Host "`n📋 INSTRUCTIONS DE DÉMARRAGE" -ForegroundColor Cyan
    Write-Host "`n1️⃣  Configuration de la Base de Données" -ForegroundColor Green
    Write-Host "   Assurez-vous que MySQL est lancé et exécutez :"
    Write-Host "   mysql -u root -p -e 'CREATE DATABASE IF NOT EXISTS weshedule;'"
    
    Write-Host "`n2️⃣  Vérifier la configuration" -ForegroundColor Green
    Write-Host "   Éditez src/main/resources/application.properties"
    Write-Host "   Vérifiez les paramètres de connexion MySQL"
    
    Write-Host "`n3️⃣  Démarrer l'application" -ForegroundColor Green
    Write-Host "   Sélectionnez l'option 1 dans le menu"
    Write-Host "   Ou exécutez : mvnw spring-boot:run"
    
    Write-Host "`n4️⃣  Accéder à l'application" -ForegroundColor Green
    Write-Host "   Ouvrez http://localhost:8080 dans votre navigateur"
    
    Write-Host "`n📝 Comptes de Test" -ForegroundColor Yellow
    Write-Host "   Email: admin@test.com"
    Write-Host "   Email: teacher@test.com"
    Write-Host "   Email: student@test.com"
    Write-Host "   Mot de passe: password123"
    
    Write-Host "`n📚 Documentation" -ForegroundColor Cyan
    Write-Host "   - QUICK_START.md : Guide de démarrage rapide"
    Write-Host "   - AUTHENTICATION_DOCUMENTATION.md : Documentation technique"
    Write-Host "   - README_DOCUMENTATION.md : Index de documentation"
    Write-Host ""
}

# Programme principal
function Main {
    Write-Header
    
    # Vérifications préalables
    Write-Info "Exécution des vérifications préalables..."
    Write-Host ""
    
    $java_ok = Check-Java
    Write-Host ""
    
    $maven_ok = Check-Maven
    Write-Host ""
    
    $mysql_ok = Check-MySQL
    Write-Host ""
    
    if (-not $java_ok -or -not $maven_ok) {
        Write-Error "Prérequis manquants !"
        exit 1
    }
    
    # Afficher les instructions
    Show-Instructions
    
    # Boucle du menu
    $continue = $true
    while ($continue) {
        Show-Menu
        $choice = Read-Host "Votre choix"
        
        switch ($choice) {
            "1" { Start-Application }
            "2" { Compile-Project }
            "3" { Build-Project }
            "4" { Run-Tests }
            "5" { Show-Dependencies }
            "6" { Clean-Project }
            "0" { 
                Write-Success "Au revoir !"
                $continue = $false 
            }
            default { Write-Warning "Choix invalide, réessayez" }
        }
        
        if ($choice -ne "0") {
            Write-Host ""
            Read-Host "Appuyez sur Entrée pour continuer"
        }
    }
}

# Lancer le programme principal
Main
