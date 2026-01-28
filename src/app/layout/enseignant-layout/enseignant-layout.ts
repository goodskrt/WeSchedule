import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { filter } from 'rxjs/operators';
import { SidebarEnseignant } from '../../views/enseignant/sidebar-enseignant/sidebar-enseignant';
import { Navbar } from '../navbar/navbar';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { LayoutService } from '../../shared/services/layout.service';

@Component({
  selector: 'app-enseignant-layout',
  standalone: true,
  imports: [RouterOutlet, CommonModule, SidebarEnseignant, Navbar, SvgIconComponent],
  templateUrl: './enseignant-layout.html',
  styleUrl: './enseignant-layout.scss'
})
export class EnseignantLayout implements OnInit {
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
  protected readonly currentPageTitle = signal('Dashboard Enseignant');

  // Mapping des routes enseignant vers les titres
  private readonly routeTitles: { [key: string]: string } = {
    '/app/enseignant/dashboard': 'Dashboard Enseignant',
    '/app/enseignant/mes-cours': 'Mes Cours',
    '/app/enseignant/mes-disponibilites': 'Mes Disponibilités',
    '/app/enseignant/mon-emploi-de-temps': 'Mon Emploi du Temps',
    '/app/enseignant/mon-profil': 'Mon Profil'
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
    const title = this.routeTitles[cleanUrl] || 'Espace Enseignant';
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
}