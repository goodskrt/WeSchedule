import { Routes } from '@angular/router';
import { Connexion } from './views/connexion/connexion';
import { Inscription } from './views/inscription/inscription';
import { Dashboard } from './views/dashboard/dashboard';
import { Cours } from './views/cours/cours';
import { Rapports } from './views/rapports/rapports';
import { Notifications } from './views/notifications/notifications';
import { Salles } from './views/salles/salles';
import { EmploiDeTemps } from './views/emploi-de-temps/emploi-de-temps';
import { Professeurs } from './views/professeurs/professeurs';
import { MainLayout } from './layout/main-layout/main-layout';
import { EnseignantLayout } from './layout/enseignant-layout/enseignant-layout';
import { ForgotPassword } from './views/forgot-password/forgot-password';

// Import des composants enseignant
import { EnseignantDashboard } from './views/enseignant/dashboard/dashboard';

export const routes: Routes = [
    // Route par défaut - redirection vers connexion
    {
        path: '',
        redirectTo: 'connexion',
        pathMatch: 'full'
    },
    
    // Routes publiques (sans layout)
    {
        path: 'connexion',
        component: Connexion
    },
    {
        path: 'inscription',
        component: Inscription
    },
    {
        path: 'forgot-password',
        component: ForgotPassword
    },
    
    // Routes avec layout principal (administrateur/étudiant)
    {
        path: 'app',
        component: MainLayout,
        children: [
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
            },
            {
                path: 'dashboard',
                component: Dashboard
            },
            {
                path: 'cours',
                component: Cours
            },
            {
                path: 'professeurs',
                component: Professeurs
            },
            {
                path: 'salles',
                component: Salles
            },
            {
                path: 'emploi-de-temps',
                component: EmploiDeTemps
            },
            {
                path: 'notifications',
                component: Notifications
            },
            {
                path: 'rapports',
                component: Rapports
            }
        ]
    },
    
    // Routes avec layout enseignant
    {
        path: 'app/enseignant',
        component: EnseignantLayout,
        children: [
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
            },
            {
                path: 'dashboard',
                component: EnseignantDashboard
            }
            // TODO: Ajouter les autres composants enseignant
            // {
            //     path: 'mes-cours',
            //     component: MesCours
            // },
            // {
            //     path: 'mon-emploi-de-temps',
            //     component: MonEmploiDeTemps
            // },
            // {
            //     path: 'mes-disponibilites',
            //     component: MesDisponibilites
            // },
            // {
            //     path: 'mon-profil',
            //     component: MonProfil
            // }
        ]
    }
];
