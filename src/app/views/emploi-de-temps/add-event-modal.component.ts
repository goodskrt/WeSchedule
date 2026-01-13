import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface EventData {
  subjectId: string;
  subject: string;
  teacherId: string;
  teacher: string;
  roomId: string;
  room: string;
  type: 'CM' | 'TD' | 'TP' | 'Exam';
  school: string;
  students: number;
}

interface Subject {
  id: string;
  name: string;
  code: string;
  school: string;
  type: string;
}

interface Teacher {
  id: string;
  name: string;
  speciality: string;
  school: string;
}

interface Room {
  id: string;
  name: string;
  capacity: number;
  type: string;
  equipment: string[];
}

@Component({
  selector: 'app-add-event-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (show) {
      <!-- Modal Backdrop -->
      <div class="modal-overlay">
        <div class="modal-content max-w-md w-full">
          <!-- Header -->
          <div class="flex items-center justify-between p-6 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">
              @if (selectedSlot) {
                Ajouter un cours - {{ selectedSlot.day }} {{ selectedSlot.time }}
              } @else {
                Ajouter un événement
              }
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
              <select
                [value]="eventData.subjectId"
                (change)="onSubjectChange($any($event.target).value)"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">Sélectionner une matière</option>
                @for (subject of availableSubjects; track subject.id) {
                  <option [value]="subject.id">{{ subject.name }} ({{ subject.code }})</option>
                }
              </select>
              @if (eventData.subject) {
                <p class="mt-1 text-sm text-gray-600">{{ eventData.subject }}</p>
              }
            </div>

            <!-- Teacher -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Enseignant *</label>
              <select
                [value]="eventData.teacherId"
                (change)="onTeacherChange($any($event.target).value)"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">Sélectionner un enseignant</option>
                @for (teacher of getFilteredTeachers(); track teacher.id) {
                  <option [value]="teacher.id">{{ teacher.name }} - {{ teacher.speciality }}</option>
                }
              </select>
            </div>

            <!-- Room -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Salle *</label>
              <select
                [value]="eventData.roomId"
                (change)="onRoomChange($any($event.target).value)"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">Sélectionner une salle</option>
                @for (room of getFilteredRooms(); track room.id) {
                  <option [value]="room.id">{{ room.name }} ({{ room.capacity }} places - {{ room.type }})</option>
                }
              </select>
              @if (eventData.roomId) {
                <div class="mt-2">
                  @for (room of availableRooms; track room.id) {
                    @if (room.id === eventData.roomId) {
                      <div class="text-sm text-gray-600">
                        <p><strong>Capacité:</strong> {{ room.capacity }} étudiants</p>
                        <p><strong>Équipements:</strong> {{ room.equipment.join(', ') }}</p>
                      </div>
                    }
                  }
                </div>
              }
            </div>

            <!-- Type (Auto-filled based on subject) -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Type de cours</label>
              <select
                [(ngModel)]="eventData.type"
                name="type"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="CM">Cours Magistral (CM)</option>
                <option value="TD">Travaux Dirigés (TD)</option>
                <option value="TP">Travaux Pratiques (TP)</option>
                <option value="Exam">Examen</option>
              </select>
            </div>

            <!-- School (Auto-filled based on subject) -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">École</label>
              <select
                [(ngModel)]="eventData.school"
                name="school"
                disabled
                class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-gray-600"
              >
                <option value="sji">SJI</option>
                <option value="sjm">SJM</option>
                <option value="prepa">PrepaVogt</option>
                <option value="cpge">CPGE</option>
              </select>
              <p class="mt-1 text-xs text-gray-500">École automatiquement sélectionnée selon la matière</p>
            </div>

            <!-- Students -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nombre d'étudiants</label>
              <input
                type="number"
                [(ngModel)]="eventData.students"
                name="students"
                min="1"
                max="200"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Ex: 30"
              >
              @if (eventData.roomId) {
                @for (room of availableRooms; track room.id) {
                  @if (room.id === eventData.roomId && eventData.students > room.capacity) {
                    <p class="mt-1 text-sm text-red-600">⚠️ Attention: Le nombre d'étudiants dépasse la capacité de la salle ({{ room.capacity }})</p>
                  }
                }
              }
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-between pt-4 border-t border-gray-200">
              <button
                type="button"
                (click)="onSuggestSlot()"
                [disabled]="!eventData.teacher || !eventData.room"
                class="px-4 py-2 bg-blue-100 text-blue-700 hover:bg-blue-200 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                title="Suggérer un créneau optimal"
              >
                💡 Suggérer un créneau
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
                  [disabled]="!eventData.subjectId || !eventData.teacherId || !eventData.roomId"
                  class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Ajouter
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class AddEventModalComponent {
  @Input() show = false;
  @Input() selectedSlot: {day: string, time: string} | null = null;
  @Input() availableSubjects: Subject[] = [];
  @Input() availableTeachers: Teacher[] = [];
  @Input() availableRooms: Room[] = [];
  
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<EventData>();
  @Output() suggestSlot = new EventEmitter<{type: string, teacher: string, room: string}>();

  eventData: EventData = {
    subjectId: '',
    subject: '',
    teacherId: '',
    teacher: '',
    roomId: '',
    room: '',
    type: 'CM',
    school: 'sji',
    students: 30
  };

  showSuggestion = false;

  ngOnChanges() {
    if (this.show) {
      // Reset form when modal opens
      this.eventData = {
        subjectId: '',
        subject: '',
        teacherId: '',
        teacher: '',
        roomId: '',
        room: '',
        type: 'CM',
        school: 'sji',
        students: 30
      };
    }
  }

  onSubjectChange(subjectId: string) {
    const subject = this.availableSubjects.find(s => s.id === subjectId);
    if (subject) {
      this.eventData.subjectId = subjectId;
      this.eventData.subject = subject.name;
      this.eventData.school = subject.school;
      this.eventData.type = subject.type as 'CM' | 'TD' | 'TP' | 'Exam';
    }
  }

  onTeacherChange(teacherId: string) {
    const teacher = this.availableTeachers.find(t => t.id === teacherId);
    if (teacher) {
      this.eventData.teacherId = teacherId;
      this.eventData.teacher = teacher.name;
    }
  }

  onRoomChange(roomId: string) {
    const room = this.availableRooms.find(r => r.id === roomId);
    if (room) {
      this.eventData.roomId = roomId;
      this.eventData.room = room.name;
      // Suggérer le nombre d'étudiants basé sur la capacité de la salle
      this.eventData.students = Math.min(this.eventData.students, room.capacity);
    }
  }

  getFilteredTeachers() {
    if (!this.eventData.school) return this.availableTeachers;
    return this.availableTeachers.filter(teacher => teacher.school === this.eventData.school);
  }

  getFilteredRooms() {
    // Filtrer les salles selon le type de cours
    if (this.eventData.type === 'TP') {
      return this.availableRooms.filter(room => 
        room.type === 'Informatique' || room.type === 'Laboratoire' || room.type === 'Réseau'
      );
    } else if (this.eventData.type === 'CM') {
      return this.availableRooms.filter(room => 
        room.type === 'Amphithéâtre' || room.type === 'Cours'
      );
    }
    return this.availableRooms;
  }

  getSelectedRoomCapacity(): number {
    if (!this.eventData.roomId) return 200;
    const room = this.availableRooms.find(r => r.id === this.eventData.roomId);
    return room?.capacity || 200;
  }

  onClose() {
    this.close.emit();
  }

  onSave() {
    if (this.eventData.subjectId && this.eventData.teacherId && this.eventData.roomId) {
      this.save.emit(this.eventData);
    }
  }

  onSuggestSlot() {
    if (this.eventData.teacher && this.eventData.room) {
      this.suggestSlot.emit({
        type: this.eventData.type,
        teacher: this.eventData.teacher,
        room: this.eventData.room
      });
      this.showSuggestion = true;
    }
  }
}