import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface NotificationSettings {
  emailNotifications: boolean;
  pushNotifications: boolean;
  courseReminders: boolean;
  roomUpdates: boolean;
  scheduleChanges: boolean;
  systemAlerts: boolean;
}

@Component({
  selector: 'app-settings-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, SvgIconComponent],
  template: `
    @if (show) {
      <!-- Modal Backdrop -->
      <div class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-xl shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
          <!-- Header -->
          <div class="flex items-center justify-between p-6 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">Paramètres de Notification</h2>
            <button
              (click)="onClose()"
              class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              title="Fermer"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <!-- Content -->
          <div class="p-6 space-y-6">
            <!-- General Settings -->
            <div>
              <h3 class="text-lg font-medium text-gray-900 mb-4">Paramètres Généraux</h3>
              <div class="space-y-4">
                <label class="flex items-center justify-between cursor-pointer">
                  <div class="flex items-center gap-3">
                    <app-svg-icon icon="info" classes="w-5 h-5 text-blue-500"></app-svg-icon>
                    <div>
                      <div class="font-medium text-gray-900">Notifications par email</div>
                      <div class="text-sm text-gray-500">Recevoir les notifications par email</div>
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    [(ngModel)]="localSettings.emailNotifications"
                    class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  >
                </label>

                <label class="flex items-center justify-between cursor-pointer">
                  <div class="flex items-center gap-3">
                    <app-svg-icon icon="info" classes="w-5 h-5 text-green-500"></app-svg-icon>
                    <div>
                      <div class="font-medium text-gray-900">Notifications push</div>
                      <div class="text-sm text-gray-500">Recevoir les notifications push dans le navigateur</div>
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    [(ngModel)]="localSettings.pushNotifications"
                    class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  >
                </label>
              </div>
            </div>

            <!-- Category Settings -->
            <div>
              <h3 class="text-lg font-medium text-gray-900 mb-4">Catégories</h3>
              <div class="space-y-4">
                <label class="flex items-center justify-between cursor-pointer">
                  <div class="flex items-center gap-3">
                    <app-svg-icon icon="book" classes="w-5 h-5 text-blue-500"></app-svg-icon>
                    <div>
                      <div class="font-medium text-gray-900">Rappels de cours</div>
                      <div class="text-sm text-gray-500">Notifications pour les cours à venir</div>
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    [(ngModel)]="localSettings.courseReminders"
                    class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  >
                </label>

                <label class="flex items-center justify-between cursor-pointer">
                  <div class="flex items-center gap-3">
                    <app-svg-icon icon="building" classes="w-5 h-5 text-green-500"></app-svg-icon>
                    <div>
                      <div class="font-medium text-gray-900">Mises à jour des salles</div>
                      <div class="text-sm text-gray-500">Notifications pour les changements de salles</div>
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    [(ngModel)]="localSettings.roomUpdates"
                    class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  >
                </label>

                <label class="flex items-center justify-between cursor-pointer">
                  <div class="flex items-center gap-3">
                    <app-svg-icon icon="clock" classes="w-5 h-5 text-purple-500"></app-svg-icon>
                    <div>
                      <div class="font-medium text-gray-900">Changements d'horaires</div>
                      <div class="text-sm text-gray-500">Notifications pour les modifications d'horaires</div>
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    [(ngModel)]="localSettings.scheduleChanges"
                    class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  >
                </label>

                <label class="flex items-center justify-between cursor-pointer">
                  <div class="flex items-center gap-3">
                    <app-svg-icon icon="cog" classes="w-5 h-5 text-orange-500"></app-svg-icon>
                    <div>
                      <div class="font-medium text-gray-900">Alertes système</div>
                      <div class="text-sm text-gray-500">Notifications système importantes</div>
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    [(ngModel)]="localSettings.systemAlerts"
                    class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                  >
                </label>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex items-center justify-end gap-3 p-6 border-t border-gray-200">
            <button
              (click)="onClose()"
              class="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Annuler
            </button>
            <button
              (click)="onSave()"
              class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-lg transition-colors"
            >
              Enregistrer
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class SettingsModalComponent {
  @Input() show = false;
  @Input() settings: NotificationSettings = {
    emailNotifications: true,
    pushNotifications: true,
    courseReminders: true,
    roomUpdates: true,
    scheduleChanges: true,
    systemAlerts: true
  };
  
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<NotificationSettings>();

  localSettings: NotificationSettings = { ...this.settings };

  ngOnChanges() {
    this.localSettings = { ...this.settings };
  }

  onClose() {
    this.close.emit();
  }

  onSave() {
    this.save.emit(this.localSettings);
  }
}