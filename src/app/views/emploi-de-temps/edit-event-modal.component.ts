import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges } from '@angular/core';
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
      <div class="modal-overlay modal-top">
        <div class="modal-content max-w-sm w-full">
          <!-- Header -->
          <div class="p-3 border-b border-gray-200 bg-gradient-to-r from-primary-50 to-blue-50">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-base font-bold text-gray-900">Modifier le cours</h2>
                <p class="text-gray-600 text-xs mt-1">{{ selectedSlot?.day }} {{ selectedSlot?.time }}</p>
              </div>
              <button
                (click)="onClose()"
                class="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-full hover:bg-white/50"
                title="Fermer"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Content -->
          <form (ngSubmit)="onSave()" class="p-3 space-y-3">
            <!-- Subject -->
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Matière *</label>
              <input
                type="text"
                [(ngModel)]="localEventData.subject"
                name="subject"
                required
                class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
              >
            </div>

            <!-- Teacher -->
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Enseignant *</label>
              <input
                type="text"
                [(ngModel)]="localEventData.teacher"
                name="teacher"
                required
                class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
              >
            </div>

            <!-- Room -->
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Salle *</label>
              <input
                type="text"
                [(ngModel)]="localEventData.room"
                name="room"
                required
                class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
              >
            </div>

            <!-- Type and Students -->
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="block text-xs font-semibold text-gray-700 mb-1">Type</label>
                <select
                  [(ngModel)]="localEventData.type"
                  name="type"
                  class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                >
                  <option value="CM">CM</option>
                  <option value="TD">TD</option>
                  <option value="TP">TP</option>
                  <option value="Exam">Examen</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-semibold text-gray-700 mb-1">Étudiants</label>
                <input
                  type="number"
                  [(ngModel)]="localEventData.students"
                  name="students"
                  min="1"
                  max="200"
                  class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                >
              </div>
            </div>

            <!-- Time -->
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="block text-xs font-semibold text-gray-700 mb-1">Début</label>
                <input
                  type="time"
                  [(ngModel)]="localEventData.startTime"
                  name="startTime"
                  class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                >
              </div>

              <div>
                <label class="block text-xs font-semibold text-gray-700 mb-1">Fin</label>
                <input
                  type="time"
                  [(ngModel)]="localEventData.endTime"
                  name="endTime"
                  class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                >
              </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-between pt-3 border-t border-gray-200 mt-4">
              <button
                type="button"
                (click)="onDelete()"
                class="px-3 py-1.5 bg-red-100 text-red-700 hover:bg-red-200 rounded-md transition-colors text-xs font-medium"
                title="Supprimer ce cours"
              >
                🗑️ Supprimer
              </button>
              <div class="flex gap-1.5">
                <button
                  type="button"
                  (click)="onClose()"
                  class="px-3 py-1.5 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-xs font-medium"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  [disabled]="isLoading"
                  class="px-3 py-1.5 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-400 text-white rounded-md transition-colors text-xs font-medium flex items-center gap-1"
                >
                  @if (isLoading) {
                    <div class="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Modification...</span>
                  } @else {
                    <span>Modifier</span>
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                    </svg>
                  }
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class EditEventModalComponent implements OnInit, OnDestroy, OnChanges {
  @Input() show = false;
  @Input() selectedSlot: {day: string, time: string} | null = null;
  @Input() eventData: TimeSlot | null = null;
  
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<TimeSlot>();
  @Output() delete = new EventEmitter<void>();

  localEventData: Partial<TimeSlot> = {};
  isLoading = false;

  ngOnInit() {
    if (this.show) {
      // Bloquer le scroll de la page principale
      document.body.classList.add('modal-open');
    }
  }

  ngOnDestroy() {
    // Restaurer le scroll de la page principale
    document.body.classList.remove('modal-open');
  }

  ngOnChanges() {
    if (this.show && this.eventData) {
      this.localEventData = { ...this.eventData };
      // Bloquer le scroll
      document.body.classList.add('modal-open');
    }
  }

  onClose() {
    // Restaurer le scroll avant de fermer
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  onSave() {
    if (this.localEventData.subject && this.localEventData.teacher && this.localEventData.room && this.eventData) {
      this.isLoading = true;
      
      // Simulation de modification
      setTimeout(() => {
        const updatedEvent: TimeSlot = {
          ...this.eventData!,
          ...this.localEventData
        } as TimeSlot;
        this.save.emit(updatedEvent);
        this.isLoading = false;
        this.onClose();
      }, 1000);
    }
  }

  onDelete() {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce cours ?')) {
      this.delete.emit();
      this.onClose();
    }
  }
}