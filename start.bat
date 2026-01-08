@echo off
REM WeSchedule - Script de Démarrage Windows Batch
REM Ce script facilite le démarrage et la gestion de l'application

setlocal enabledelayedexpansion
color 0A

:menu
cls
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                  WeSchedule - Démarrage                        ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Que voulez-vous faire ?
echo.
echo 1 - Demarrer l'application (spring-boot:run)
echo 2 - Compiler le projet (clean compile)
echo 3 - Build complet (clean package)
echo 4 - Executer les tests
echo 5 - Voir les dependencies
echo 6 - Nettoyer les fichiers compiles (mvn clean)
echo 7 - Afficher les instructions
echo 0 - Quitter
echo.
set /p choice="Votre choix: "

if "%choice%"=="1" goto start_app
if "%choice%"=="2" goto compile
if "%choice%"=="3" goto build
if "%choice%"=="4" goto tests
if "%choice%"=="5" goto dependencies
if "%choice%"=="6" goto clean
if "%choice%"=="7" goto instructions
if "%choice%"=="0" goto end
echo Choix invalide, reessayez.
pause
goto menu

:start_app
cls
echo.
echo [INFO] Demarrage de l'application WeSchedule...
echo [INFO] Application disponible sur http://localhost:8080
echo [INFO] Appuyez sur Ctrl+C pour arreter l'application
echo.
if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else (
    call mvn spring-boot:run
)
pause
goto menu

:compile
cls
echo.
echo [INFO] Compilation du projet...
echo.
if exist "mvnw.cmd" (
    call mvnw.cmd clean compile
) else (
    call mvn clean compile
)
echo.
echo Compilation terminee !
pause
goto menu

:build
cls
echo.
echo [INFO] Build complet du projet (peut prendre du temps)...
echo.
if exist "mvnw.cmd" (
    call mvnw.cmd clean package -DskipTests
) else (
    call mvn clean package -DskipTests
)
echo.
echo Build reussi !
pause
goto menu

:tests
cls
echo.
echo [INFO] Execution des tests...
echo.
if exist "mvnw.cmd" (
    call mvnw.cmd test
) else (
    call mvn test
)
echo.
pause
goto menu

:dependencies
cls
echo.
echo [INFO] Affichage des dependencies...
echo.
if exist "mvnw.cmd" (
    call mvnw.cmd dependency:tree
) else (
    call mvn dependency:tree
)
echo.
pause
goto menu

:clean
cls
echo.
echo [INFO] Nettoyage du projet...
echo.
if exist "mvnw.cmd" (
    call mvnw.cmd clean
) else (
    call mvn clean
)
echo.
echo Nettoyage termine !
pause
goto menu

:instructions
cls
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║               INSTRUCTIONS DE DEMARRAGE                        ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo 1. Configuration de la Base de Donnees
echo    - Assurez-vous que MySQL est lance
echo    - Executez: mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS weschedule;"
echo.
echo 2. Verifier la configuration
echo    - Editez src/main/resources/application.properties
echo    - Verifiez les parametres de connexion MySQL
echo.
echo 3. Demarrer l'application
echo    - Selectionnez l'option 1 dans le menu
echo    - Ou executez: mvnw spring-boot:run
echo.
echo 4. Acceder a l'application
echo    - Ouvrez http://localhost:8080 dans votre navigateur
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                  COMPTES DE TEST                               ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo   ADMINISTRATEUR
echo   Email: admin@test.com
echo   Mot de passe: password123
echo.
echo   ENSEIGNANT
echo   Email: teacher@test.com
echo   Mot de passe: password123
echo.
echo   ETUDIANT
echo   Email: student@test.com
echo   Mot de passe: password123
echo.
pause
goto menu

:end
echo.
echo Au revoir !
echo.
endlocal
