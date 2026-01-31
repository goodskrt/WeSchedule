import { Component, signal, OnInit, Inject, PLATFORM_ID, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { LayoutService } from '../../shared/services/layout.service';

interface MenuItem {
  path: string;
  label: string;
  icon: string;
  badge?: number;
  category?: string;
}

interface QuickAction {
  id: string;
  label: string;
  icon: string;
  action: () => void;
  color: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule, SvgIconComponent],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar implements OnInit {
  private isBrowser: boolean;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router,
    @Inject(LayoutService) private layoutService: LayoutService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  protected readonly isHovered = signal(false);
  protected readonly currentTime = signal(new Date());
  protected readonly showQuickActions = signal(false);
  protected readonly notificationCount = signal(0);

  // Utiliser les signals du service de layout
  protected readonly isCollapsed = signal(false);
  protected readonly sidebarMode = signal<'normal' | 'mini' | 'overlay'>('normal');
  
  protected readonly menuItems = signal<MenuItem[]>([
    { 
      path: '/app/dashboard', 
      label: 'Tableau de bord', 
      icon: 'info',
      category: 'principal'
    },
    { 
      path: '/app/emploi-de-temps', 
      label: 'Emploi du temps', 
      icon: 'clock',
      category: 'principal'
    },
    { 
      path: '/app/ue', 
      label: 'Gestion des UE', 
      icon: 'academic-cap',
      category: 'gestion'
    },
    { 
      path: '/app/classes', 
      label: 'Gestion des classes', 
      icon: 'user-group',
      category: 'gestion'
    },
    { 
      path: '/app/cours', 
      label: 'Gestion des cours', 
      icon: 'book',
      category: 'gestion'
    },
    { 
      path: '/app/professeurs', 
      label: 'Gestion des professeurs', 
      icon: 'user',
      category: 'gestion'
    },
    { 
      path: '/app/salles', 
      label: 'Gestion des salles', 
      icon: 'building',
      category: 'gestion'
    },
    { 
      path: '/app/equipements', 
      label: 'Gestion des équipements', 
      icon: 'cog',
      category: 'gestion'
    },
    { 
      path: '/app/notifications', 
      label: 'Notifications', 
      icon: 'info', 
      badge: 0,
      category: 'communication'
    },
    { 
      path: '/app/rapports', 
      label: 'Rapports', 
      icon: 'clipboard',
      category: 'analyse'
    }
  ]);

  protected readonly quickActions: QuickAction[] = [
    {
      id: 'add-ue',
      label: 'Nouvelle UE',
      icon: 'academic-cap',
      color: 'bg-indigo-500',
      action: () => this.router.navigate(['/app/ue'])
    },
    {
      id: 'add-classe',
      label: 'Nouvelle classe',
      icon: 'user-group',
      color: 'bg-teal-500',
      action: () => this.router.navigate(['/app/classes'])
    },
    {
      id: 'add-course',
      label: 'Nouveau cours',
      icon: 'info',
      color: 'bg-blue-500',
      action: () => this.router.navigate(['/app/cours'])
    },
    {
      id: 'add-teacher',
      label: 'Nouveau professeur',
      icon: 'user',
      color: 'bg-green-500',
      action: () => this.router.navigate(['/app/professeurs'])
    },
    {
      id: 'add-room',
      label: 'Nouvelle salle',
      icon: 'building',
      color: 'bg-purple-500',
      action: () => this.router.navigate(['/app/salles'])
    },
    {
      id: 'add-equipment',
      label: 'Nouvel équipement',
      icon: 'cog',
      color: 'bg-gray-500',
      action: () => this.router.navigate(['/app/equipements'])
    },
    {
      id: 'schedule',
      label: 'Planning',
      icon: 'clock',
      color: 'bg-orange-500',
      action: () => this.router.navigate(['/app/emploi-de-temps'])
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

      // Adapter le layout à la taille d'écran
      this.layoutService.adaptToScreenSize();
      
      // Initialiser avec les valeurs du service
      this.isCollapsed.set(this.layoutService.sidebarCollapsed());
      this.sidebarMode.set(this.layoutService.sidebarMode());

      // Écouter les changements de notifications
      window.addEventListener('notificationCountChanged', (event: any) => {
        this.notificationCount.set(event.detail.count);
        this.updateNotificationBadge();
      });
    }
  }

  private updateNotificationBadge() {
    this.menuItems.update(items => 
      items.map(item => 
        item.path === '/notifications' 
          ? { ...item, badge: this.notificationCount() }
          : item
      )
    );
  }

  toggleSidebar() {
    // Toggle direct du signal local
    this.isCollapsed.update(value => !value);
    // Synchroniser avec le service
    this.layoutService.setSidebarCollapsed(this.isCollapsed());
  }

  onMouseEnter() {
    this.isHovered.set(true);
  }

  onMouseLeave() {
    this.isHovered.set(false);
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
    return !this.isCollapsed() || (this.isCollapsed() && this.isHovered());
  }

  getMenuItemsByCategory(category: string): MenuItem[] {
    return this.menuItems().filter((item: MenuItem) => item.category === category);
  }

  getCategories(): string[] {
    const categories = [...new Set(this.menuItems().map((item: MenuItem) => item.category || 'autre'))];
    // S'assurer que "principal" est en premier
    const orderedCategories = ['principal', 'gestion', 'communication', 'analyse'];
    return orderedCategories.filter(cat => categories.includes(cat));
  }

  getCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      'principal': 'Principal',
      'gestion': 'Gestion',
      'communication': 'Communication',
      'analyse': 'Analyse',
      'autre': 'Autre'
    };
    return labels[category] || category;
  }

  getCategoryIcon(category: string): string {
    const icons: { [key: string]: string } = {
      'principal': 'info',
      'gestion': 'cog',
      'communication': 'info',
      'analyse': 'clipboard',
      'autre': 'info'
    };
    return icons[category] || 'info';
  }
}