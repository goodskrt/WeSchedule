import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { User } from '../../../shared/models/auth.models';
import { SvgIconComponent } from '../../../shared/svg-icon/svg-icon.component';

interface MenuItem {
  label: string;
  route: string;
  icon: string;
  description?: string;
}

@Component({
  selector: 'app-sidebar-enseignant',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, SvgIconComponent],
  templateUrl: './sidebar-enseignant.html',
  styleUrl: './sidebar-enseignant.scss',
})
export class SidebarEnseignant {
  @Input() collapsed = false;
  @Input() mode: 'normal' | 'mini' | 'overlay' = 'normal';
  @Output() toggleSidebar = new EventEmitter<boolean>();

  protected readonly currentUser = signal<User | null>(null);

  // Menu items spécifiques aux enseignants
  protected readonly menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      route: '/app/enseignant/dashboard',
      icon: 'dashboard',
      description: 'Vue d\'ensemble de vos activités'
    },
    {
      label: 'Mes Cours',
      route: '/app/enseignant/mes-cours',
      icon: 'book',
      description: 'Gestion de vos cours et UE'
    },
    {
      label: 'Mon Emploi du Temps',
      route: '/app/enseignant/mon-emploi-de-temps',
      icon: 'calendar',
      description: 'Votre planning de cours'
    },
    {
      label: 'Mes Disponibilités',
      route: '/app/enseignant/mes-disponibilites',
      icon: 'clock',
      description: 'Gérer vos créneaux disponibles'
    },
    {
      label: 'Mon Profil',
      route: '/app/enseignant/mon-profil',
      icon: 'user',
      description: 'Informations personnelles'
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.loadUserData();
  }

  private loadUserData() {
    const user = this.authService.getCurrentUser();
    this.currentUser.set(user);
  }

  onToggleSidebar() {
    this.toggleSidebar.emit(!this.collapsed);
  }

  logout() {
    this.authService.logout();
  }

  isActiveRoute(route: string): boolean {
    return this.router.url === route;
  }
}
