import { Component, signal, OnInit, Inject, PLATFORM_ID, HostListener } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { LayoutService } from '../../shared/services/layout.service';

interface User {
  name: string;
  email: string;
  role: string;
  avatar?: string;
  status: 'online' | 'away' | 'busy' | 'offline';
  lastSeen?: Date;
}

interface Theme {
  id: string;
  name: string;
  class: string;
  icon: string;
  gradient: string;
}

interface Language {
  code: string;
  name: string;
  flag: string;
}

interface Notification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  timestamp: Date;
  read: boolean;
  actionUrl?: string;
}

interface QuickSearch {
  id: string;
  title: string;
  type: 'page' | 'course' | 'teacher' | 'room';
  url: string;
  icon: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, SvgIconComponent],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class Navbar implements OnInit {
  private isBrowser: boolean;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router,
    @Inject(LayoutService) private layoutService: LayoutService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  // Utiliser les signals du service de layout
  protected readonly currentTheme = signal('light');
  protected readonly currentLanguage = signal('fr');

  // Getter pour l'utilisateur actuel
  currentUser(): User {
    return {
      name: 'Dr. fomekong miguel',
      email: 'fomekong.miguel@iu-saintfomekong.cm',
      role: 'Enseignant',
      avatar: '',
      status: 'online',
      lastSeen: new Date()
    };
  }

  // Getter pour l'état de la sidebar
  sidebarCollapsed(): boolean {
    return this.layoutService.sidebarCollapsed();
  }
  protected readonly isFullscreen = signal(false);
  protected readonly showUserMenu = signal(false);
  protected readonly showThemeMenu = signal(false);
  protected readonly showLanguageMenu = signal(false);
  protected readonly showNotifications = signal(false);
  protected readonly showSearchResults = signal(false);
  protected readonly searchTerm = signal('');
  protected readonly notificationCount = signal(3);
  protected readonly isSearchFocused = signal(false);
  protected readonly currentTime = signal(new Date());

  protected readonly themes: Theme[] = [
    { 
      id: 'light', 
      name: 'Clair', 
      class: 'bg-white', 
      icon: 'info',
      gradient: 'from-blue-400 to-blue-600'
    },
    { 
      id: 'dark', 
      name: 'Sombre', 
      class: 'bg-gray-900', 
      icon: 'info',
      gradient: 'from-gray-700 to-gray-900'
    },
    { 
      id: 'blue', 
      name: 'Océan', 
      class: 'bg-blue-600', 
      icon: 'info',
      gradient: 'from-blue-500 to-cyan-500'
    },
    { 
      id: 'purple', 
      name: 'Cosmos', 
      class: 'bg-purple-600', 
      icon: 'info',
      gradient: 'from-purple-500 to-pink-500'
    },
    { 
      id: 'green', 
      name: 'Nature', 
      class: 'bg-green-600', 
      icon: 'info',
      gradient: 'from-green-500 to-emerald-500'
    }
  ];

  protected readonly languages: Language[] = [
    { code: 'fr', name: 'Français', flag: '🇫🇷' },
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
    { code: 'es', name: 'Español', flag: '🇪🇸' },
    { code: 'it', name: 'Italiano', flag: '🇮🇹' }
  ];

  protected readonly notifications = signal<Notification[]>([
    {
      id: '1',
      title: 'Nouveau cours ajouté',
      message: 'Le cours "Algorithmique Avancée" a été ajouté à votre emploi du temps.',
      type: 'info',
      timestamp: new Date(Date.now() - 5 * 60 * 1000),
      read: false
    },
    {
      id: '2',
      title: 'Salle réservée',
      message: 'La salle Amphithéâtre A a été réservée avec succès.',
      type: 'success',
      timestamp: new Date(Date.now() - 15 * 60 * 1000),
      read: false
    },
    {
      id: '3',
      title: 'Changement d\'horaire',
      message: 'Le cours de Physique a été reporté à 16h.',
      type: 'warning',
      timestamp: new Date(Date.now() - 30 * 60 * 1000),
      read: true
    }
  ]);

  protected readonly quickSearchResults = signal<QuickSearch[]>([]);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('.navbar-dropdown')) {
      this.closeAllMenus();
    }
  }

  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent) {
    // Raccourcis clavier
    if (event.ctrlKey || event.metaKey) {
      switch (event.key) {
        case 'k':
          event.preventDefault();
          this.focusSearch();
          break;
        case 'b':
          event.preventDefault();
          this.toggleSidebar();
          break;
        case 'f':
          event.preventDefault();
          this.toggleFullscreen();
          break;
      }
    }
    
    if (event.key === 'Escape') {
      this.closeAllMenus();
    }
  }

  @HostListener('window:scroll')
  onWindowScroll() {
    if (this.isBrowser) {
      const navbar = document.querySelector('.navbar-container');
      if (navbar) {
        if (window.scrollY > 10) {
          navbar.classList.add('scrolled');
        } else {
          navbar.classList.remove('scrolled');
        }
      }
    }
  }

  ngOnInit() {
    if (this.isBrowser) {
      // Mettre à jour l'heure toutes les minutes
      setInterval(() => {
        this.currentTime.set(new Date());
      }, 60000);

      // Restaurer les préférences
      this.loadPreferences();
      
      // Écouter les changements de fullscreen
      document.addEventListener('fullscreenchange', () => {
        this.isFullscreen.set(!!document.fullscreenElement);
      });

      // Simuler l'arrivée de nouvelles notifications
      this.simulateNotifications();

      // Écouter les événements de test de notifications
      window.addEventListener('addTestNotification', (event: any) => {
        this.addTestNotification(event.detail);
      });
    }
  }

  private simulateNotifications() {
    // Ajouter une nouvelle notification toutes les 2 minutes (pour démonstration)
    setInterval(() => {
      const newNotification: Notification = {
        id: Date.now().toString(),
        title: 'Notification automatique',
        message: 'Notification générée automatiquement à ' + new Date().toLocaleTimeString('fr-FR'),
        type: Math.random() > 0.5 ? 'info' : 'success',
        timestamp: new Date(),
        read: false
      };
      
      this.notifications.update(notifications => [newNotification, ...notifications]);
      this.updateNotificationCount();
    }, 120000); // 2 minutes
  }

  private loadPreferences() {
    if (this.isBrowser) {
      const savedTheme = localStorage.getItem('selectedTheme') || this.layoutService.theme();
      const savedLanguage = localStorage.getItem('selectedLanguage') || this.layoutService.language();
      
      this.currentTheme.set(savedTheme);
      this.currentLanguage.set(savedLanguage);
      this.applyTheme(savedTheme);
      
      // Synchroniser avec le service
      this.layoutService.setTheme(savedTheme);
      this.layoutService.setLanguage(savedLanguage);
    }
  }

  private applyTheme(themeId: string) {
    if (this.isBrowser) {
      const body = document.body;
      body.className = body.className.replace(/theme-\w+/g, '');
      body.classList.add(`theme-${themeId}`);
    }
  }

  toggleUserMenu() {
    this.showUserMenu.update(value => !value);
    this.showThemeMenu.set(false);
    this.showLanguageMenu.set(false);
    this.showNotifications.set(false);
  }

  toggleThemeMenu() {
    this.showThemeMenu.update(value => !value);
    this.showUserMenu.set(false);
    this.showLanguageMenu.set(false);
    this.showNotifications.set(false);
  }

  toggleLanguageMenu() {
    this.showLanguageMenu.update(value => !value);
    this.showUserMenu.set(false);
    this.showThemeMenu.set(false);
    this.showNotifications.set(false);
  }

  toggleNotifications() {
    this.showNotifications.update(value => !value);
    this.showUserMenu.set(false);
    this.showThemeMenu.set(false);
    this.showLanguageMenu.set(false);
  }

  closeAllMenus() {
    this.showUserMenu.set(false);
    this.showThemeMenu.set(false);
    this.showLanguageMenu.set(false);
    this.showNotifications.set(false);
    this.showSearchResults.set(false);
  }

  selectTheme(themeId: string) {
    this.currentTheme.set(themeId);
    this.showThemeMenu.set(false);
    this.applyTheme(themeId);
    this.layoutService.setTheme(themeId);
    
    if (this.isBrowser) {
      localStorage.setItem('selectedTheme', themeId);
    }
  }

  selectLanguage(languageCode: string) {
    this.currentLanguage.set(languageCode);
    this.showLanguageMenu.set(false);
    this.layoutService.setLanguage(languageCode);
    
    if (this.isBrowser) {
      localStorage.setItem('selectedLanguage', languageCode);
    }
  }

  onSearch() {
    const term = this.searchTerm().trim();
    if (term) {
      this.performSearch(term);
    } else {
      this.quickSearchResults.set([]);
      this.showSearchResults.set(false);
    }
  }

  private performSearch(term: string) {
    // Simulation de recherche rapide
    const mockResults: QuickSearch[] = [
      { id: '1', title: 'Tableau de bord', type: 'page', url: '/dashboard', icon: 'info' },
      { id: '2', title: 'Gestion des cours', type: 'page', url: '/cours', icon: 'book' },
      { id: '3', title: 'Algorithmique', type: 'course', url: '/cours/1', icon: 'book' },
      { id: '4', title: 'Dr. Martin', type: 'teacher', url: '/professeurs/1', icon: 'user' },
      { id: '5', title: 'Salle 101', type: 'room', url: '/salles/1', icon: 'building' }
    ];

    const filtered = mockResults.filter(item => 
      item.title.toLowerCase().includes(term.toLowerCase())
    );

    this.quickSearchResults.set(filtered);
    this.showSearchResults.set(filtered.length > 0);
  }

  clearSearch() {
    this.searchTerm.set('');
    this.quickSearchResults.set([]);
    this.showSearchResults.set(false);
  }

  focusSearch() {
    if (this.isBrowser) {
      const searchInput = document.querySelector('#global-search') as HTMLInputElement;
      if (searchInput) {
        searchInput.focus();
      }
    }
  }

  onSearchFocus() {
    this.isSearchFocused.set(true);
    if (this.searchTerm().trim()) {
      this.showSearchResults.set(true);
    }
  }

  onSearchBlur() {
    setTimeout(() => {
      this.isSearchFocused.set(false);
      this.showSearchResults.set(false);
    }, 200);
  }

  selectSearchResult(result: QuickSearch) {
    this.router.navigate([result.url]);
    this.clearSearch();
  }

  markNotificationAsRead(notificationId: string) {
    this.notifications.update(notifications =>
      notifications.map(n =>
        n.id === notificationId ? { ...n, read: true } : n
      )
    );
    this.updateNotificationCount();
    
    // Animation de feedback
    if (this.isBrowser) {
      const notificationElement = document.querySelector(`[data-notification-id="${notificationId}"]`);
      if (notificationElement) {
        notificationElement.classList.add('animate-pulse');
        setTimeout(() => {
          notificationElement.classList.remove('animate-pulse');
        }, 500);
      }
    }
  }

  private updateNotificationCount() {
    const unreadCount = this.notifications().filter(n => !n.read).length;
    this.notificationCount.set(unreadCount);
    
    // Émettre un événement pour informer la sidebar
    if (this.isBrowser) {
      const event = new CustomEvent('notificationCountChanged', {
        detail: { count: unreadCount }
      });
      window.dispatchEvent(event);
    }
  }

  getUserInitials(): string {
    return this.currentUser().name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase();
  }

  getUserStatusColor(): string {
    const status = this.currentUser().status;
    const colors = {
      online: 'bg-green-500',
      away: 'bg-yellow-500',
      busy: 'bg-red-500',
      offline: 'bg-gray-500'
    };
    return colors[status];
  }

  toggleFullscreen() {
    if (this.isBrowser) {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().then(() => {
          this.isFullscreen.set(true);
        });
      } else {
        document.exitFullscreen().then(() => {
          this.isFullscreen.set(false);
        });
      }
    }
  }

  toggleSidebar() {
    this.layoutService.toggleSidebar();
  }

  logout() {
    if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      this.router.navigate(['/connexion']);
    }
  }

  goToProfile() {
    this.closeAllMenus();
    this.router.navigate(['/profile']);
  }

  goToSettings() {
    this.closeAllMenus();
    this.router.navigate(['/settings']);
  }

  getCurrentTheme(): Theme {
    return this.themes.find(t => t.id === this.currentTheme()) || this.themes[0];
  }

  getCurrentLanguage(): Language {
    return this.languages.find(l => l.code === this.currentLanguage()) || this.languages[0];
  }

  getFormattedTime(): string {
    return this.currentTime().toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatNotificationTime(timestamp: Date): string {
    const now = new Date();
    const diff = now.getTime() - timestamp.getTime();
    const minutes = Math.floor(diff / (1000 * 60));
    
    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes}min`;
    
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `Il y a ${hours}h`;
    
    const days = Math.floor(hours / 24);
    return `Il y a ${days}j`;
  }

  getSearchResultIcon(type: string): string {
    const icons = {
      page: 'info',
      course: 'book',
      teacher: 'user',
      room: 'building'
    };
    return icons[type as keyof typeof icons] || 'info';
  }

  getSearchResultTypeLabel(type: string): string {
    const labels = {
      page: 'Page',
      course: 'Cours',
      teacher: 'Professeur',
      room: 'Salle'
    };
    return labels[type as keyof typeof labels] || type;
  }

  getNotificationBadgeText(): string {
    const count = this.notificationCount();
    if (count > 99) {
      return '99+';
    }
    return count.toString();
  }

  // Méthode pour ajouter une notification (pour test)
  addTestNotification(customData?: any) {
    const types: ('info' | 'success' | 'warning' | 'error')[] = ['info', 'success', 'warning', 'error'];
    const randomType = types[Math.floor(Math.random() * types.length)];
    
    const newNotification: Notification = {
      id: Date.now().toString(),
      title: customData?.title || 'Notification de test',
      message: customData?.message || `Ceci est une notification de type ${randomType} générée à ${new Date().toLocaleTimeString('fr-FR')}`,
      type: customData?.type || randomType,
      timestamp: new Date(),
      read: false
    };
    
    this.notifications.update(notifications => [newNotification, ...notifications]);
    this.updateNotificationCount();

    // Animation du badge
    if (this.isBrowser) {
      const badge = document.querySelector('.notification-badge');
      if (badge) {
        badge.classList.add('animate-bounce');
        setTimeout(() => {
          badge.classList.remove('animate-bounce');
        }, 1000);
      }
    }
  }

  // Méthode pour marquer toutes les notifications comme lues avec animation
  markAllNotificationsAsRead() {
    this.notifications.update(notifications =>
      notifications.map(n => ({ ...n, read: true }))
    );
    this.notificationCount.set(0);
    
    // Animation de feedback
    if (this.isBrowser) {
      const badge = document.querySelector('.notification-badge');
      if (badge) {
        badge.classList.add('animate-bounce');
        setTimeout(() => {
          badge.classList.remove('animate-bounce');
        }, 1000);
      }
    }
  }
}