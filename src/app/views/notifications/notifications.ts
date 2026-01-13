import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { SettingsModalComponent } from './settings-modal.component';

interface Notification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  timestamp: Date;
  read: boolean;
  category: 'course' | 'room' | 'system' | 'schedule';
  actionUrl?: string;
}

@Component({
  selector: 'app-notifications',
  imports: [CommonModule, SvgIconComponent, SettingsModalComponent],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss',
})
export class Notifications {
  protected readonly selectedFilter = signal<string>('all');
  protected readonly showOnlyUnread = signal<boolean>(false);
  protected readonly showSettings = signal<boolean>(false);
  protected readonly searchQuery = signal<string>('');
  protected readonly notificationSettings = signal({
    emailNotifications: true,
    pushNotifications: true,
    courseReminders: true,
    roomUpdates: true,
    scheduleChanges: true,
    systemAlerts: true
  });

  protected readonly notifications = signal<Notification[]>([
    {
      id: '1',
      title: 'Nouveau cours ajouté',
      message: 'Le cours "Algorithmique Avancée" a été ajouté à votre emploi du temps pour demain à 9h00.',
      type: 'info',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 heures
      read: false,
      category: 'course'
    },
    {
      id: '2',
      title: 'Salle réservée avec succès',
      message: 'La salle Amphithéâtre A a été réservée pour votre conférence du 15 janvier.',
      type: 'success',
      timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000), // 4 heures
      read: false,
      category: 'room'
    },
    {
      id: '3',
      title: 'Changement d\'horaire',
      message: 'Le cours de Physique Quantique a été reporté de 14h à 16h aujourd\'hui.',
      type: 'warning',
      timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000), // 6 heures
      read: true,
      category: 'schedule'
    },
    {
      id: '4',
      title: 'Maintenance programmée',
      message: 'Le système sera en maintenance ce soir de 22h à 2h du matin.',
      type: 'warning',
      timestamp: new Date(Date.now() - 8 * 60 * 60 * 1000), // 8 heures
      read: true,
      category: 'system'
    },
    {
      id: '5',
      title: 'Erreur de réservation',
      message: 'La réservation de la salle 101 a échoué. Veuillez réessayer.',
      type: 'error',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 jour
      read: true,
      category: 'room'
    }
  ]);

  getTypeInfo(type: string) {
    switch (type) {
      case 'info': return { icon: 'info', color: 'bg-blue-100 text-blue-800', bgColor: 'bg-blue-50 border-blue-200' };
      case 'success': return { icon: 'success', color: 'bg-green-100 text-green-800', bgColor: 'bg-green-50 border-green-200' };
      case 'warning': return { icon: 'warning', color: 'bg-yellow-100 text-yellow-800', bgColor: 'bg-yellow-50 border-yellow-200' };
      case 'error': return { icon: 'error', color: 'bg-red-100 text-red-800', bgColor: 'bg-red-50 border-red-200' };
      default: return { icon: 'info', color: 'bg-gray-100 text-gray-800', bgColor: 'bg-gray-50 border-gray-200' };
    }
  }

  getCategoryInfo(category: string) {
    switch (category) {
      case 'course': return { name: 'Cours', icon: 'book', color: 'bg-blue-100 text-blue-800' };
      case 'room': return { name: 'Salle', icon: 'building', color: 'bg-green-100 text-green-800' };
      case 'schedule': return { name: 'Horaire', icon: 'clock', color: 'bg-purple-100 text-purple-800' };
      case 'system': return { name: 'Système', icon: 'cog', color: 'bg-orange-100 text-orange-800' };
      default: return { name: 'Général', icon: 'clipboard', color: 'bg-gray-100 text-gray-800' };
    }
  }

  formatTimestamp(timestamp: Date): string {
    const now = new Date();
    const diff = now.getTime() - timestamp.getTime();
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (minutes < 60) {
      return `Il y a ${minutes} minute${minutes > 1 ? 's' : ''}`;
    } else if (hours < 24) {
      return `Il y a ${hours} heure${hours > 1 ? 's' : ''}`;
    } else {
      return `Il y a ${days} jour${days > 1 ? 's' : ''}`;
    }
  }

  filteredNotifications() {
    let filtered = this.notifications();
    
    // Apply search filter
    if (this.searchQuery().trim()) {
      const searchTerm = this.searchQuery().toLowerCase();
      filtered = filtered.filter(notification =>
        notification.title.toLowerCase().includes(searchTerm) ||
        notification.message.toLowerCase().includes(searchTerm)
      );
    }
    
    // Apply category filter
    if (this.selectedFilter() !== 'all') {
      filtered = filtered.filter(notification => notification.category === this.selectedFilter());
    }
    
    // Apply read/unread filter
    if (this.showOnlyUnread()) {
      filtered = filtered.filter(notification => !notification.read);
    }
    
    return filtered.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
  }

  setFilter(filter: string) {
    this.selectedFilter.set(filter);
  }

  toggleUnreadFilter() {
    this.showOnlyUnread.update(value => !value);
  }

  markAsRead(notificationId: string) {
    this.notifications.update(notifications =>
      notifications.map(n =>
        n.id === notificationId ? { ...n, read: true } : n
      )
    );
  }

  markAllAsRead() {
    this.notifications.update(notifications =>
      notifications.map(n => ({ ...n, read: true }))
    );
  }

  deleteNotification(notificationId: string) {
    this.notifications.update(notifications =>
      notifications.filter(n => n.id !== notificationId)
    );
  }

  getUnreadCount(): number {
    return this.notifications().filter(n => !n.read).length;
  }

  clearAllNotifications() {
    if (confirm('Êtes-vous sûr de vouloir supprimer toutes les notifications ?')) {
      this.notifications.set([]);
    }
  }

  openSettings() {
    this.showSettings.set(true);
  }

  closeSettings() {
    this.showSettings.set(false);
  }

  updateSetting(setting: string, value: boolean) {
    this.notificationSettings.update(settings => ({
      ...settings,
      [setting]: value
    }));
  }

  onSettingsSave(newSettings: any) {
    this.notificationSettings.set(newSettings);
    this.closeSettings();
  }

  setSearchQuery(query: string) {
    this.searchQuery.set(query);
  }

  clearSearch() {
    this.searchQuery.set('');
  }

  exportNotifications() {
    const notifications = this.notifications();
    const csvContent = [
      'Date,Type,Catégorie,Titre,Message,Lu',
      ...notifications.map(n => 
        `${n.timestamp.toISOString()},${n.type},${n.category},"${n.title}","${n.message}",${n.read ? 'Oui' : 'Non'}`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'notifications.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  getNotificationsByType() {
    const notifications = this.notifications();
    const types = ['info', 'success', 'warning', 'error'];
    return types.map(type => ({
      type,
      count: notifications.filter(n => n.type === type).length,
      info: this.getTypeInfo(type)
    }));
  }

  getNotificationsByCategory() {
    const notifications = this.notifications();
    const categories = ['course', 'room', 'schedule', 'system'];
    return categories.map(category => ({
      category,
      count: notifications.filter(n => n.category === category).length,
      info: this.getCategoryInfo(category)
    }));
  }

  addTestNotification() {
    const testNotification: Notification = {
      id: Date.now().toString(),
      title: 'Notification de test',
      message: 'Ceci est une notification de test générée automatiquement.',
      type: 'info',
      timestamp: new Date(),
      read: false,
      category: 'system'
    };
    
    this.notifications.update(notifications => [testNotification, ...notifications]);
  }

  clearAll() {
    if (confirm('Êtes-vous sûr de vouloir supprimer toutes les notifications ?')) {
      this.notifications.set([]);
    }
  }

  bulkMarkAsRead(notificationIds: string[]) {
    this.notifications.update(notifications =>
      notifications.map(n =>
        notificationIds.includes(n.id) ? { ...n, read: true } : n
      )
    );
  }

  bulkDelete(notificationIds: string[]) {
    this.notifications.update(notifications =>
      notifications.filter(n => !notificationIds.includes(n.id))
    );
  }

  getNotificationStats() {
    const notifications = this.notifications();
    return {
      total: notifications.length,
      unread: notifications.filter(n => !n.read).length,
      byType: this.getNotificationsByType(),
      byCategory: this.getNotificationsByCategory(),
      todayCount: notifications.filter(n => {
        const today = new Date();
        const notifDate = new Date(n.timestamp);
        return notifDate.toDateString() === today.toDateString();
      }).length
    };
  }

  scheduleNotification(title: string, message: string, type: 'info' | 'success' | 'warning' | 'error', category: 'course' | 'room' | 'system' | 'schedule', delay: number) {
    setTimeout(() => {
      const notification: Notification = {
        id: Date.now().toString(),
        title,
        message,
        type,
        timestamp: new Date(),
        read: false,
        category
      };
      this.notifications.update(notifications => [notification, ...notifications]);
    }, delay);
  }

  searchNotifications(query: string) {
    if (!query.trim()) return this.filteredNotifications();
    
    const searchTerm = query.toLowerCase();
    return this.filteredNotifications().filter(notification =>
      notification.title.toLowerCase().includes(searchTerm) ||
      notification.message.toLowerCase().includes(searchTerm)
    );
  }
}
