import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { filter } from 'rxjs/operators';
import { Sidebar } from '../sidebar/sidebar';
import { Navbar } from '../navbar/navbar';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { LayoutService } from '../../shared/services/layout.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, CommonModule, Sidebar, Navbar, SvgIconComponent],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss'
})
export class MainLayout implements OnInit {
  private isBrowser: boolean;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router,
    @Inject(LayoutService) private layoutService: LayoutService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  // Utiliser les signals du service de layout
  protected readonly sidebarCollapsed = signal(false);
  protected readonly sidebarMode = signal<'normal' | 'mini' | 'overlay'>('normal');
  protected readonly currentPageTitle = signal('Tableau de bord');

  // Mapping des routes vers les titres
  private readonly routeTitles: { [key: string]: string } = {
    '/app/dashboard': 'Tableau de bord',
    '/app/ue': 'Gestion des UE',
    '/app/classes': 'Gestion des classes',
    '/app/cours': 'Gestion des cours',
    '/app/professeurs': 'Gestion des professeurs',
    '/app/emploi-de-temps': 'Emploi du temps',
    '/app/salles': 'Gestion des salles',
    '/app/equipements': 'Gestion des équipements',
    '/app/notifications': 'Notifications',
    '/app/rapports': 'Rapports',
    '/connexion': 'Connexion',
    '/inscription': 'Inscription',
    '/forgot-password': 'Mot de passe oublié'
  };

  ngOnInit() {
    if (this.isBrowser) {
      // Synchroniser avec le service de layout
      this.sidebarCollapsed.set(this.layoutService.sidebarCollapsed());
      this.sidebarMode.set(this.layoutService.sidebarMode());
      
      // Écouter les changements du service
      setInterval(() => {
        this.sidebarCollapsed.set(this.layoutService.sidebarCollapsed());
        this.sidebarMode.set(this.layoutService.sidebarMode());
      }, 100);
      
      // Écouter les changements de route pour mettre à jour le titre
      this.router.events
        .pipe(filter(event => event instanceof NavigationEnd))
        .subscribe((event: NavigationEnd) => {
          this.updatePageTitle(event.url);
        });

      // Initialiser le titre de la page actuelle
      this.updatePageTitle(this.router.url);
    }
  }

  private updatePageTitle(url: string) {
    // Nettoyer l'URL (enlever les paramètres de requête et fragments)
    const cleanUrl = url.split('?')[0].split('#')[0];
    const title = this.routeTitles[cleanUrl] || 'Page';
    this.currentPageTitle.set(title);
  }

  getCurrentPageTitle(): string {
    return this.currentPageTitle();
  }

  closeSidebar() {
    this.layoutService.setSidebarCollapsed(true);
    this.sidebarCollapsed.set(true);
  }

  onSidebarToggle(collapsed: boolean) {
    this.layoutService.setSidebarCollapsed(collapsed);
    this.sidebarCollapsed.set(collapsed);
  }

  testNotifications() {
    // Émettre un événement personnalisé pour ajouter une notification
    if (this.isBrowser) {
      const event = new CustomEvent('addTestNotification', {
        detail: {
          title: 'Notification de test',
          message: `Notification générée depuis le dashboard à ${new Date().toLocaleTimeString('fr-FR')}`,
          type: Math.random() > 0.5 ? 'info' : 'success'
        }
      });
      window.dispatchEvent(event);
    }
  }
}