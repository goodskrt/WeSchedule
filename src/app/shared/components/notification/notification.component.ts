import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SvgIconComponent } from '../../svg-icon/svg-icon.component';
import { NotificationService, Notification } from '../../services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule, SvgIconComponent],
  template: `
    <div class="fixed top-4 right-4 z-50 space-y-2">
      @for (notification of notifications(); track notification.id) {
        <div 
          class="max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden transform transition-all duration-300 ease-in-out"
          [class]="getNotificationClasses(notification.type)"
        >
          <div class="p-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <app-svg-icon 
                  [icon]="getIconName(notification.type)"
                  [classes]="getIconClasses(notification.type) + ' w-5 h-5'"
                />
              </div>
              <div class="ml-3 w-0 flex-1 pt-0.5">
                <p class="text-sm font-medium text-gray-900">
                  {{ notification.title }}
                </p>
                <p class="mt-1 text-sm text-gray-500">
                  {{ notification.message }}
                </p>
              </div>
              <div class="ml-4 flex-shrink-0 flex">
                <button
                  class="bg-white rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                  (click)="removeNotification(notification.id)"
                >
                  <span class="sr-only">Fermer</span>
                  <app-svg-icon icon="close" classes="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .notification-success {
      @apply border-l-4 border-green-400;
    }
    .notification-error {
      @apply border-l-4 border-red-400;
    }
    .notification-warning {
      @apply border-l-4 border-yellow-400;
    }
    .notification-info {
      @apply border-l-4 border-blue-400;
    }
  `]
})
export class NotificationComponent {
  notifications = computed(() => this.notificationService.getNotifications()());

  constructor(private notificationService: NotificationService) {}

  removeNotification(id: string): void {
    this.notificationService.remove(id);
  }

  getNotificationClasses(type: Notification['type']): string {
    return `notification-${type}`;
  }

  getIconName(type: Notification['type']): string {
    switch (type) {
      case 'success':
        return 'check';
      case 'error':
        return 'close';
      case 'warning':
        return 'warning';
      case 'info':
        return 'info';
      default:
        return 'info';
    }
  }

  getIconClasses(type: Notification['type']): string {
    switch (type) {
      case 'success':
        return 'text-green-400';
      case 'error':
        return 'text-red-400';
      case 'warning':
        return 'text-yellow-400';
      case 'info':
        return 'text-blue-400';
      default:
        return 'text-blue-400';
    }
  }
}