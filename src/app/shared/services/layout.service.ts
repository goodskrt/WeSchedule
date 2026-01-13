import { Injectable, signal, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class LayoutService {
  private isBrowser: boolean;

  private readonly _sidebarCollapsed = signal(false);
  private readonly _sidebarMode = signal<'normal' | 'mini' | 'overlay'>('normal');
  private readonly _theme = signal('light');
  private readonly _language = signal('fr');

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    this.loadPreferences();
  }

  // Getters pour les signals
  get sidebarCollapsed() {
    return this._sidebarCollapsed.asReadonly();
  }

  get sidebarMode() {
    return this._sidebarMode.asReadonly();
  }

  get theme() {
    return this._theme.asReadonly();
  }

  get language() {
    return this._language.asReadonly();
  }

  // Setters
  setSidebarCollapsed(collapsed: boolean) {
    this._sidebarCollapsed.set(collapsed);
    this.savePreferences();
  }

  setSidebarMode(mode: 'normal' | 'mini' | 'overlay') {
    this._sidebarMode.set(mode);
    this.savePreferences();
  }

  setTheme(theme: string) {
    this._theme.set(theme);
    this.savePreferences();
  }

  setLanguage(language: string) {
    this._language.set(language);
    this.savePreferences();
  }

  // Méthodes utilitaires
  toggleSidebar() {
    this.setSidebarCollapsed(!this._sidebarCollapsed());
  }

  adaptToScreenSize() {
    if (this.isBrowser) {
      const width = window.innerWidth;
      if (width < 768) {
        this._sidebarMode.set('overlay');
        this._sidebarCollapsed.set(true);
      } else if (width < 1024) {
        this._sidebarMode.set('mini');
        this._sidebarCollapsed.set(true);
      } else {
        this._sidebarMode.set('normal');
        this._sidebarCollapsed.set(false);
      }
    }
  }

  private loadPreferences() {
    if (this.isBrowser) {
      const collapsed = localStorage.getItem('sidebarCollapsed');
      const mode = localStorage.getItem('sidebarMode');
      const theme = localStorage.getItem('theme');
      const language = localStorage.getItem('language');

      if (collapsed !== null) {
        this._sidebarCollapsed.set(collapsed === 'true');
      }
      if (mode) {
        this._sidebarMode.set(mode as 'normal' | 'mini' | 'overlay');
      }
      if (theme) {
        this._theme.set(theme);
      }
      if (language) {
        this._language.set(language);
      }
    }
  }

  private savePreferences() {
    if (this.isBrowser) {
      localStorage.setItem('sidebarCollapsed', this._sidebarCollapsed().toString());
      localStorage.setItem('sidebarMode', this._sidebarMode());
      localStorage.setItem('theme', this._theme());
      localStorage.setItem('language', this._language());
    }
  }
}