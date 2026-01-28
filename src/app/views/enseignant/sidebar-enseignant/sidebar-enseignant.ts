import { Component, Input, Output, EventEmitter, signal, OnInit, OnChanges, Inject, PLATFORM_ID, HostListener } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { User } from '../../../shared/models/auth.models';
import { SvgIconComponent } from '../../../shared/svg-icon/svg-icon.component';
import { LayoutService } from '../../../shared/services/layout.service';

interface MenuItem {
  path: string;
  label: string;
  icon: string;
  badge?: number;
  subItems?: SubMenuItem[];
  category?: string;
}

interface SubMenuItem {
  path: string;
  label: string;
  icon: string;
}

interface QuickAction {
  id: string;
  label: string;
  icon: string;
  action: () => void;
  color: string;
}

@Component({
  selector: 'app-sidebar-enseignant',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, SvgIconComponent],
  templateUrl: './sidebar-enseignant.html',
  styleUrl: './sidebar-enseignant.scss',
})
export class SidebarEnseignant implements OnInit, OnChanges {
  @Input() collapsed = false;
  @Input() mode: 'normal' | 'mini' | 'overlay' = 'normal';
  @Output() toggleSidebar = new EventEmitter<boolean>();

  private isBrowser: boolean;

  constructor(
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object,
    @Inject(LayoutService) private layoutService: LayoutService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    this.loadUserData();
  }

  protected readonly currentUser = signal<User | null>(null);
  protected readonly isHovered = signal(false);
  protected readonly expandedItems = signal<string[]>([]);
  protected readonly currentTime = signal(new Date());
  protected readonly showQuickActions = signal(false);
  protected readonly notificationCount = signal(0);

  // Utiliser les signals du service de layout
  protected readonly isCollapsed = signal(false);
  protected readonly sidebarMode = signal<'normal' | 'mini' | 'overlay'>('normal');

  // Menu items spécifiques aux enseignants
  protected readonly menuItems = signal<MenuItem[]>([
    { 
      path: '/app/enseignant/dashboard', 
      label: 'Dashboard', 
      icon: 'info',
      category: 'principal'
    },
    { 
      path: '/app/enseignant/mes-cours', 
      label: 'Mes Cours', 
      icon: 'book',
      category: 'enseignement',
      subItems: [
        { path: '/app/enseignant/mes-cours', label: 'Liste de mes cours', icon: 'clipboard' },
        { path: '/app/enseignant/mes-cours/planning', label: 'Planning des cours', icon: 'clock' }
      ]
    },
    { 
      path: '/app/enseignant/mon-emploi-de-temps', 
      label: 'Mon Emploi du Temps', 
      icon: 'clock',
      category: 'enseignement'
    },
    { 
      path: '/app/enseignant/mes-disponibilites', 
      label: 'Mes Disponibilités', 
      icon: 'clock',
      category: 'planning'
    },
    { 
      path: '/app/enseignant/mon-profil', 
      label: 'Mon Profil', 
      icon: 'user',
      category: 'personnel'
    }
  ]);

  protected readonly quickActions: QuickAction[] = [
    {
      id: 'add-availability',
      label: 'Ajouter disponibilité',
      icon: 'clock',
      color: 'bg-blue-500',
      action: () => this.router.navigate(['/app/enseignant/mes-disponibilites'])
    },
    {
      id: 'view-schedule',
      label: 'Voir mon planning',
      icon: 'clock',
      color: 'bg-green-500',
      action: () => this.router.navigate(['/app/enseignant/mon-emploi-de-temps'])
    },
    {
      id: 'manage-courses',
      label: 'Gérer mes cours',
      icon: 'book',
      color: 'bg-purple-500',
      action: () => this.router.navigate(['/app/enseignant/mes-cours'])
    },
    {
      id: 'edit-profile',
      label: 'Modifier profil',
      icon: 'user',
      color: 'bg-orange-500',
      action: () => this.router.navigate(['/app/enseignant/mon-profil'])
    }
  ];

  @HostListener('window:resize')
  onWindowResize() {
    this.layoutService.adaptToScreenSize();
  }

  ngOnInit() {
    // Mettre à jour l'heure toutes les minutes
    if (this.isBrowser) {
      setInterval(() => {
        this.currentTime.set(new Date());
      }, 60000);

      // Synchroniser les signaux internes avec les props Input
      this.isCollapsed.set(this.collapsed);
      this.sidebarMode.set(this.mode);
      
      // Adapter le layout à la taille d'écran
      this.layoutService.adaptToScreenSize();
    }
  }

  // Synchroniser avec les changements des props Input
  ngOnChanges() {
    this.isCollapsed.set(this.collapsed);
    this.sidebarMode.set(this.mode);
  }

  private loadUserData() {
    const user = this.authService.getCurrentUser();
    this.currentUser.set(user);
  }

  onToggleSidebar() {
    // Inverser l'état actuel
    const newCollapsedState = !this.isCollapsed();
    
    console.log('Toggle sidebar - Current:', this.isCollapsed(), 'New:', newCollapsedState);
    
    // Mettre à jour les signaux internes
    this.isCollapsed.set(newCollapsedState);
    
    // Synchroniser avec le service
    this.layoutService.setSidebarCollapsed(newCollapsedState);
    
    // Émettre l'événement pour le parent
    this.toggleSidebar.emit(newCollapsedState);
  }

  // Méthode pour forcer l'état collapsed
  setCollapsed(collapsed: boolean) {
    this.isCollapsed.set(collapsed);
    this.layoutService.setSidebarCollapsed(collapsed);
    this.toggleSidebar.emit(collapsed);
  }

  // Méthode pour obtenir l'état actuel
  getCollapsedState(): boolean {
    return this.isCollapsed();
  }

  onMouseEnter() {
    this.isHovered.set(true);
  }

  onMouseLeave() {
    this.isHovered.set(false);
  }

  toggleExpanded(itemPath: string) {
    this.expandedItems.update(items => {
      const index = items.indexOf(itemPath);
      if (index > -1) {
        return items.filter(item => item !== itemPath);
      } else {
        return [...items, itemPath];
      }
    });
  }

  isExpanded(itemPath: string): boolean {
    return this.expandedItems().includes(itemPath);
  }

  toggleQuickActions() {
    this.showQuickActions.update(value => !value);
  }

  executeQuickAction(action: QuickAction) {
    action.action();
    this.showQuickActions.set(false);
  }

  getTimeGreeting(): string {
    const hour = this.currentTime().getHours();
    if (hour < 12) return 'Bonjour';
    if (hour < 18) return 'Bon après-midi';
    return 'Bonsoir';
  }

  getFormattedTime(): string {
    return this.currentTime().toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getFormattedDate(): string {
    return this.currentTime().toLocaleDateString('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long'
    });
  }

  shouldShowExpanded(): boolean {
    return !(this.collapsed || this.isCollapsed()) || (this.collapsed || this.isCollapsed()) && this.isHovered();
  }

  getMenuItemsByCategory(category: string): MenuItem[] {
    return this.menuItems().filter((item: MenuItem) => item.category === category);
  }

  getCategories(): string[] {
    const categories = [...new Set(this.menuItems().map((item: MenuItem) => item.category || 'autre'))];
    // S'assurer que "principal" est en premier
    const orderedCategories = ['principal', 'enseignement', 'planning', 'personnel'];
    return orderedCategories.filter(cat => categories.includes(cat));
  }

  getCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      'principal': 'Principal',
      'enseignement': 'Enseignement',
      'planning': 'Planning',
      'personnel': 'Personnel',
      'autre': 'Autre'
    };
    return labels[category] || category;
  }

  getCategoryIcon(category: string): string {
    const icons: { [key: string]: string } = {
      'principal': 'info',
      'enseignement': 'book',
      'planning': 'clock',
      'personnel': 'user',
      'autre': 'info'
    };
    return icons[category] || 'info';
  }

  logout() {
    this.authService.logout();
  }

  isActiveRoute(route: string): boolean {
    return this.router.url === route;
  }
}
