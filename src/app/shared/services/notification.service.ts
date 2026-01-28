import { Injectable, signal } from '@angular/core';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  persistent?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = signal<Notification[]>([]);
  private nextId = 1;

  /**
   * Obtenir toutes les notifications
   */
  getNotifications() {
    return this.notifications.asReadonly();
  }

  /**
   * Afficher une notification de succès
   */
  success(title: string, message: string, duration: number = 5000): void {
    this.addNotification({
      type: 'success',
      title,
      message,
      duration
    });
  }

  /**
   * Afficher une notification d'erreur
   */
  error(title: string, message: string, persistent: boolean = false): void {
    this.addNotification({
      type: 'error',
      title,
      message,
      persistent,
      duration: persistent ? undefined : 8000
    });
  }

  /**
   * Afficher une notification d'avertissement
   */
  warning(title: string, message: string, duration: number = 6000): void {
    this.addNotification({
      type: 'warning',
      title,
      message,
      duration
    });
  }

  /**
   * Afficher une notification d'information
   */
  info(title: string, message: string, duration: number = 5000): void {
    this.addNotification({
      type: 'info',
      title,
      message,
      duration
    });
  }

  /**
   * Supprimer une notification
   */
  remove(id: string): void {
    this.notifications.update(notifications => 
      notifications.filter(n => n.id !== id)
    );
  }

  /**
   * Supprimer toutes les notifications
   */
  clear(): void {
    this.notifications.set([]);
  }

  /**
   * Ajouter une notification
   */
  private addNotification(notification: Omit<Notification, 'id'>): void {
    const newNotification: Notification = {
      ...notification,
      id: (this.nextId++).toString()
    };

    this.notifications.update(notifications => [...notifications, newNotification]);

    // Auto-suppression si une durée est définie
    if (newNotification.duration && !newNotification.persistent) {
      setTimeout(() => {
        this.remove(newNotification.id);
      }, newNotification.duration);
    }
  }
}