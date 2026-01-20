import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges } from '@angular/core';
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
      <div class="modal-overlay modal-top">
        <div class="modal-content max-w-sm w-full">
          <!-- Header -->
          <div class="p-3 border-b border-gray-200 bg-gradient-to-r from-primary-50 to-blue-50">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-base font-bold text-gray-900">{{ getModalTitle() }}</h2>
                <p class="text-gray-600 text-xs mt-1">{{ getStepTitle() }}</p>
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
            
            <!-- Progress Steps -->
            <div class="flex items-center mt-3">
              <div class="flex items-center">
                <!-- Step 1 -->
                <div 
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold transition-all duration-300"
                  [class.bg-primary-600]="currentStep >= 1"
                  [class.text-white]="currentStep >= 1"
                  [class.bg-gray-200]="currentStep < 1"
                  [class.text-gray-600]="currentStep < 1"
                  [class.shadow-md]="currentStep >= 1"
                >
                  @if (currentStep > 1) {
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                    </svg>
                  } @else {
                    1
                  }
                </div>
                <div 
                  class="w-6 h-0.5 mx-1 transition-all duration-300"
                  [class.bg-primary-600]="currentStep > 1"
                  [class.bg-gray-200]="currentStep <= 1"
                ></div>
                
                <!-- Step 2 -->
                <div 
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold transition-all duration-300"
                  [class.bg-primary-600]="currentStep >= 2"
                  [class.text-white]="currentStep >= 2"
                  [class.bg-gray-200]="currentStep < 2"
                  [class.text-gray-600]="currentStep < 2"
                  [class.shadow-md]="currentStep >= 2"
                >
                  @if (currentStep > 2) {
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                    </svg>
                  } @else {
                    2
                  }
                </div>
                <div 
                  class="w-6 h-0.5 mx-1 transition-all duration-300"
                  [class.bg-primary-600]="currentStep > 2"
                  [class.bg-gray-200]="currentStep <= 2"
                ></div>
                
                <!-- Step 3 -->
                <div 
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold transition-all duration-300"
                  [class.bg-primary-600]="currentStep >= 3"
                  [class.text-white]="currentStep >= 3"
                  [class.bg-gray-200]="currentStep < 3"
                  [class.text-gray-600]="currentStep < 3"
                  [class.shadow-md]="currentStep >= 3"
                >
                  3
                </div>
              </div>
            </div>
          </div>

          <!-- Content -->
          <form (ngSubmit)="currentStep < 3 ? nextStep() : onSave()" class="p-3">
            <!-- Step 1: Subject Selection -->
            @if (currentStep === 1) {
              <div class="space-y-3">
                <!-- Subject -->
                <div>
                  <label class="block text-xs font-semibold text-gray-700 mb-1">Matière *</label>
                  <select
                    [value]="eventData.subjectId"
                    (change)="onSubjectChange($any($event.target).value)"
                    required
                    class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                    [class.border-red-300]="errors['subject']"
                  >
                    <option value="">Sélectionner une matière</option>
                    @for (subject of availableSubjects; track subject.id) {
                      <option [value]="subject.id">{{ subject.name }} ({{ subject.code }})</option>
                    }
                  </select>
                  @if (errors['subject']) {
                    <p class="text-red-600 text-xs mt-0.5">{{ errors['subject'] }}</p>
                  }
                  @if (eventData.subject) {
                    <p class="mt-1 text-xs text-gray-600">{{ eventData.subject }}</p>
                  }
                </div>

                <!-- Type (Auto-filled based on subject) -->
                <div>
                  <label class="block text-xs font-semibold text-gray-700 mb-1">Type de cours</label>
                  <select
                    [(ngModel)]="eventData.type"
                    name="type"
                    class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                  >
                    <option value="CM">Cours Magistral (CM)</option>
                    <option value="TD">Travaux Dirigés (TD)</option>
                    <option value="TP">Travaux Pratiques (TP)</option>
                    <option value="Exam">Examen</option>
                  </select>
                </div>

                <!-- School (Auto-filled based on subject) -->
                <div>
                  <label class="block text-xs font-semibold text-gray-700 mb-1">École</label>
                  <select
                    [(ngModel)]="eventData.school"
                    name="school"
                    disabled
                    class="w-full px-2.5 py-2 border border-gray-300 rounded-md bg-gray-100 text-gray-600 text-xs"
                  >
                    <option value="sji">SJI</option>
                    <option value="sjm">SJM</option>
                    <option value="prepa">PrepaVogt</option>
                    <option value="cpge">CPGE</option>
                  </select>
                  <p class="mt-1 text-xs text-gray-500">École automatiquement sélectionnée</p>
                </div>
              </div>
            }

            <!-- Step 2: Teacher Selection -->
            @if (currentStep === 2) {
              <div class="space-y-3">
                <!-- Teacher -->
                <div>
                  <label class="block text-xs font-semibold text-gray-700 mb-1">Enseignant *</label>
                  <select
                    [value]="eventData.teacherId"
                    (change)="onTeacherChange($any($event.target).value)"
                    required
                    class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                    [class.border-red-300]="errors['teacher']"
                  >
                    <option value="">Sélectionner un enseignant</option>
                    @for (teacher of getFilteredTeachers(); track teacher.id) {
                      <option [value]="teacher.id">{{ teacher.name }}</option>
                    }
                  </select>
                  @if (errors['teacher']) {
                    <p class="text-red-600 text-xs mt-0.5">{{ errors['teacher'] }}</p>
                  }
                  @if (eventData.teacherId) {
                    @for (teacher of availableTeachers; track teacher.id) {
                      @if (teacher.id === eventData.teacherId) {
                        <p class="mt-1 text-xs text-gray-600">{{ teacher.speciality }}</p>
                      }
                    }
                  }
                </div>

                <!-- Students -->
                <div>
                  <label class="block text-xs font-semibold text-gray-700 mb-1">Nombre d'étudiants</label>
                  <input
                    type="number"
                    [(ngModel)]="eventData.students"
                    name="students"
                    min="1"
                    max="200"
                    class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                    placeholder="Ex: 30"
                  >
                </div>
              </div>
            }

            <!-- Step 3: Room Selection -->
            @if (currentStep === 3) {
              <div class="space-y-3">
                <!-- Room -->
                <div>
                  <label class="block text-xs font-semibold text-gray-700 mb-1">Salle *</label>
                  <select
                    [value]="eventData.roomId"
                    (change)="onRoomChange($any($event.target).value)"
                    required
                    class="w-full px-2.5 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-primary-500 focus:border-primary-500 text-xs"
                    [class.border-red-300]="errors['room']"
                  >
                    <option value="">Sélectionner une salle</option>
                    @for (room of getFilteredRooms(); track room.id) {
                      <option [value]="room.id">{{ room.name }} ({{ room.capacity }} places)</option>
                    }
                  </select>
                  @if (errors['room']) {
                    <p class="text-red-600 text-xs mt-0.5">{{ errors['room'] }}</p>
                  }
                </div>

                @if (eventData.roomId) {
                  <div class="bg-blue-50 border border-blue-200 rounded-md p-2">
                    @for (room of availableRooms; track room.id) {
                      @if (room.id === eventData.roomId) {
                        <div class="text-xs text-blue-800">
                          <p><strong>Capacité:</strong> {{ room.capacity }} étudiants</p>
                          <p><strong>Type:</strong> {{ room.type }}</p>
                          <p><strong>Équipements:</strong> {{ room.equipment.slice(0, 3).join(', ') }}{{ room.equipment.length > 3 ? '...' : '' }}</p>
                        </div>
                        @if (eventData.students > room.capacity) {
                          <p class="mt-1 text-xs text-red-600">⚠️ Nombre d'étudiants > capacité salle</p>
                        }
                      }
                    }
                  </div>
                }
              </div>
            }

            <!-- Actions -->
            <div class="flex justify-between pt-3 border-t border-gray-200 mt-4">
              @if (currentStep === 1) {
                <button
                  type="button"
                  (click)="onClose()"
                  class="px-3 py-1.5 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-xs font-medium"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  [disabled]="!isStep1Valid()"
                  class="px-3 py-1.5 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-400 text-white rounded-md transition-colors text-xs font-medium flex items-center gap-1"
                >
                  <span>Suivant</span>
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                  </svg>
                </button>
              } @else if (currentStep === 2) {
                <button
                  type="button"
                  (click)="previousStep()"
                  class="px-3 py-1.5 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-xs font-medium flex items-center gap-1"
                >
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
                  </svg>
                  <span>Retour</span>
                </button>
                <button
                  type="submit"
                  [disabled]="!isStep2Valid()"
                  class="px-3 py-1.5 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-400 text-white rounded-md transition-colors text-xs font-medium flex items-center gap-1"
                >
                  <span>Suivant</span>
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                  </svg>
                </button>
              } @else {
                <button
                  type="button"
                  (click)="previousStep()"
                  class="px-3 py-1.5 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-xs font-medium flex items-center gap-1"
                >
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
                  </svg>
                  <span>Retour</span>
                </button>
                <div class="flex gap-1.5">
                  <button
                    type="button"
                    (click)="onSuggestSlot()"
                    [disabled]="!eventData.teacherId || !eventData.roomId"
                    class="px-3 py-1.5 bg-blue-100 text-blue-700 hover:bg-blue-200 rounded-md transition-colors disabled:opacity-50 text-xs font-medium"
                    title="Suggérer un créneau"
                  >
                    💡
                  </button>
                  <button
                    type="submit"
                    [disabled]="!isStep3Valid() || isLoading"
                    class="px-3 py-1.5 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-400 text-white rounded-md transition-colors text-xs font-medium flex items-center gap-1"
                  >
                    @if (isLoading) {
                      <div class="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      <span>Ajout...</span>
                    } @else {
                      <span>Ajouter</span>
                      <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                      </svg>
                    }
                  </button>
                </div>
              }
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class AddEventModalComponent implements OnInit, OnDestroy, OnChanges {
  @Input() show = false;
  @Input() selectedSlot: {day: string, time: string} | null = null;
  @Input() availableSubjects: Subject[] = [];
  @Input() availableTeachers: Teacher[] = [];
  @Input() availableRooms: Room[] = [];
  
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<EventData>();
  @Output() suggestSlot = new EventEmitter<{type: string, teacher: string, room: string}>();

  currentStep: 1 | 2 | 3 = 1;
  isLoading = false;
  errors: {[key: string]: string} = {};

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
      this.currentStep = 1;
      this.errors = {};
      this.isLoading = false;
      
      // Bloquer le scroll
      document.body.classList.add('modal-open');
    }
  }

  // Navigation entre les étapes
  nextStep() {
    if (this.validateStep(this.currentStep)) {
      this.currentStep = Math.min(this.currentStep + 1, 3) as 1 | 2 | 3;
    }
  }

  previousStep() {
    this.currentStep = Math.max(this.currentStep - 1, 1) as 1 | 2 | 3;
  }

  // Validation des étapes
  validateStep(step: number): boolean {
    this.errors = {};
    
    if (step === 1) {
      if (!this.eventData.subjectId) {
        this.errors['subject'] = 'La matière est requise';
        return false;
      }
    }
    
    if (step === 2) {
      if (!this.eventData.teacherId) {
        this.errors['teacher'] = 'L\'enseignant est requis';
        return false;
      }
    }
    
    if (step === 3) {
      if (!this.eventData.roomId) {
        this.errors['room'] = 'La salle est requise';
        return false;
      }
    }
    
    return true;
  }

  // Validation des étapes individuelles
  isStep1Valid(): boolean {
    return this.eventData.subjectId !== '';
  }

  isStep2Valid(): boolean {
    return this.eventData.teacherId !== '';
  }

  isStep3Valid(): boolean {
    return this.eventData.roomId !== '';
  }

  // Titres des étapes
  getModalTitle(): string {
    const baseTitle = this.selectedSlot 
      ? `Ajouter un cours - ${this.selectedSlot.day} ${this.selectedSlot.time}`
      : 'Ajouter un événement';
    return `${baseTitle} - Étape ${this.currentStep}/3`;
  }

  getStepTitle(): string {
    switch (this.currentStep) {
      case 1: return 'Matière et type';
      case 2: return 'Enseignant';
      case 3: return 'Salle';
      default: return '';
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
    // Restaurer le scroll avant de fermer
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  onSave() {
    if (this.validateStep(3) && this.eventData.subjectId && this.eventData.teacherId && this.eventData.roomId) {
      this.isLoading = true;
      
      // Simulation d'ajout
      setTimeout(() => {
        this.save.emit(this.eventData);
        this.isLoading = false;
        this.onClose();
      }, 1000);
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