import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface TimeSlot {
  id: string;
  startTime: string;
  endTime: string;
  subject: string;
  teacher: string;
  room: string;
  type: 'CM' | 'TD' | 'TP' | 'Exam';
  school: string;
  students: number;
  color: string;
}

@Component({
  selector: 'app-edit-event-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (show && eventData) {
      <!-- Modal Backdrop -->
      <div class="modal-overlay">
        <div class="modal-content max-w-md w-full">
          <!-- Header -->
          <div class="flex items-center justify-between p-6 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">
              Modifier le cours - {{ selectedSlot?.day }} {{ selectedSlot?.time }}
            </h2>
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
          <form (ngSubmit)="onSave()" class="p-6 space-y-4">
            <!-- Subject -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Matière *</label>
              <input
                type="text"
                [(ngModel)]="localEventData.subject"
                name="subject"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Ex: Algorithmique Avancée"
              >
            </div>

            <!-- Teacher -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Enseignant *</label>
              <input
                type="text"
                [(ngModel)]="localEventData.teacher"
                name="teacher"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Ex: Dr. Martin"
              >
            </div>

            <!-- Room -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Salle *</label>
              <input
                type="text"
                [(ngModel)]="localEventData.room"
                name="room"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Ex: Salle 101"
              >
            </div>

            <!-- Type -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Type de cours *</label>
              <select
                [(ngModel)]="localEventData.type"
                name="type"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="CM">Cours Magistral (CM)</option>
                <option value="TD">Travaux Dirigés (TD)</option>
                <option value="TP">Travaux Pratiques (TP)</option>
                <option value="Exam">Examen</option>
              </select>
            </div>

            <!-- School -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">École *</label>
              <select
                [(ngModel)]="localEventData.school"
                name="school"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="sji">SJI</option>
                <option value="sjm">SJM</option>
                <option value="prepa">PrepaVogt</option>
                <option value="cpge">CPGE</option>
              </select>
            </div>

            <!-- Students -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nombre d'étudiants</label>
              <input
                type="number"
                [(ngModel)]="localEventData.students"
                name="students"
                min="1"
                max="200"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Ex: 30"
              >
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-between pt-4 border-t border-gray-200">
              <button
                type="button"
                (click)="onDelete()"
                class="px-4 py-2 text-red-700 bg-red-100 hover:bg-red-200 rounded-lg transition-colors"
              >
                Supprimer
              </button>
              <div class="flex gap-3">
                <button
                  type="button"
                  (click)="onClose()"
                  class="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-lg transition-colors"
                >
                  Modifier
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class EditEventModalComponent {
  @Input() show = false;
  @Input() selectedSlot: {day: string, time: string} | null = null;
  @Input() eventData: TimeSlot | null = null;
  
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<TimeSlot>();
  @Output() delete = new EventEmitter<void>();

  localEventData: Partial<TimeSlot> = {};

  ngOnChanges() {
    if (this.show && this.eventData) {
      this.localEventData = { ...this.eventData };
    }
  }

  onClose() {
    this.close.emit();
  }

  onSave() {
    if (this.localEventData.subject && this.localEventData.teacher && this.localEventData.room && this.eventData) {
      const updatedEvent: TimeSlot = {
        ...this.eventData,
        ...this.localEventData
      } as TimeSlot;
      this.save.emit(updatedEvent);
    }
  }

  onDelete() {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce cours ?')) {
      this.delete.emit();
    }
  }
}