import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface ScheduleForm {
  coursId: string;
  subject: string;
  teacher: string;
  teacherId: string;
  type: 'CM' | 'TD' | 'TP' | 'PROJ' | 'SEM';
  school: string;
  duration: number; // Duration in hours (1-8)
  description: string;
  color: string;
  selectedTypes: string[]; // Selected course types for filtering
}

interface CoursModel {
  id: string;
  ueId: string;
  typeId: string;
  professeurId?: string;
  classes: string[];
  duree: number;
  description?: string;
  statut: 'actif' | 'annule' | 'termine' | 'planifie';
  createdAt: Date;
  updatedAt: Date;
  // Additional properties for display (enriched data)
  nom?: string;
  ue?: any;
  type?: any;
  ecole?: string;
  professeur?: any;
}

interface TypeCours {
  id: string;
  nom: string;
  code: string;
  description: string;
  couleur: string;
  dureeDefaut: number;
}

interface Professeur {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  specialites: string[];
  ecoles: string[];
}

@Component({
  selector: 'app-modal-schedule',
  imports: [CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './modal-schedule.html',
  styleUrl: './modal-schedule.scss',
})
export class ModalSchedule implements OnInit, OnDestroy {
  @Input() existingEvent: any = null;
  @Input() selectedTimeSlot: {day: string, time: string} | null = null;
  @Input() selectedClass: any = null;
  @Input() availableCours: CoursModel[] = [];
  @Input() availableTypesCours: TypeCours[] = [];
  @Input() availableProfesseurs: Professeur[] = [];
  @Input() existingSchedule: any = {}; // Pour vérifier les conflits
  @Output() close = new EventEmitter<void>();
  @Output() eventAdded = new EventEmitter<any>();
  @Output() eventUpdated = new EventEmitter<any>();

  protected readonly scheduleForm = signal<ScheduleForm>({
    coursId: '',
    subject: '',
    teacher: '',
    teacherId: '',
    type: 'CM',
    school: '',
    duration: 1, // Duration in hours
    description: '',
    color: 'bg-blue-100 border-blue-300 text-blue-800',
    selectedTypes: [] // No filter by default
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});
  protected readonly warnings = signal<{[key: string]: string}>({});
  protected readonly isEditMode = signal(false);

  protected readonly schools = [
    { id: 'sji', name: 'Saint Jean Ingénieur (SJI)', color: 'bg-blue-500' },
    { id: 'sjm', name: 'Saint Jean Management (SJM)', color: 'bg-green-500' },
    { id: 'prepa', name: 'PrepaVogt', color: 'bg-purple-500' },
    { id: 'cpge', name: 'Classes Préparatoires (CPGE)', color: 'bg-orange-500' }
  ];

  protected readonly courseTypes = [
    { id: 'CM', name: 'Cours Magistral', icon: 'book', color: 'bg-blue-100 border-blue-300 text-blue-800' },
    { id: 'TD', name: 'Travaux Dirigés', icon: 'edit', color: 'bg-green-100 border-green-300 text-green-800' },
    { id: 'TP', name: 'Travaux Pratiques', icon: 'flask', color: 'bg-purple-100 border-purple-300 text-purple-800' },
    { id: 'PROJ', name: 'Projet', icon: 'briefcase', color: 'bg-orange-100 border-orange-300 text-orange-800' },
    { id: 'SEM', name: 'Séminaire', icon: 'users', color: 'bg-indigo-100 border-indigo-300 text-indigo-800' }
  ];

  ngOnInit() {
    if (this.existingEvent) {
      this.isEditMode.set(true);
      this.scheduleForm.set({
        coursId: this.existingEvent.coursId || '',
        subject: this.existingEvent.subject || '',
        teacher: this.existingEvent.teacher || '',
        teacherId: this.existingEvent.teacherId || '',
        type: this.existingEvent.type || 'CM',
        school: this.existingEvent.school || '',
        duration: this.existingEvent.duration || 1,
        description: this.existingEvent.description || '',
        color: this.existingEvent.color || 'bg-blue-100 border-blue-300 text-blue-800',
        selectedTypes: []
      });
    } else if (this.selectedClass) {
      // Set default values from selected class
      this.scheduleForm.update(form => ({
        ...form,
        school: this.selectedClass.ecole
      }));
    }
  }

  ngOnDestroy() {
    // Component cleanup
  }

  // Course Selection
  onCoursChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    const coursId = target.value;
    if (coursId) {
      const selectedCours = this.getAvailableCours().find(cours => cours.id === coursId);
      if (selectedCours) {
        this.selectCours(selectedCours);
      }
    }
  }

  selectCours(cours: CoursModel) {
    // Find the professor for this course
    const professor = this.availableProfesseurs.find(prof => prof.id === cours.professeurId);
    
    // Get UE and type information
    const ue = cours.ue; // UE should be included in course data
    const type = cours.type; // Type should be included in course data
    
    this.scheduleForm.update(form => ({
      ...form,
      coursId: cours.id,
      subject: cours.nom || `${ue?.nom} (${type?.code})`,
      teacher: professor ? `${professor.prenom} ${professor.nom}` : 'Professeur non assigné',
      teacherId: cours.professeurId || '',
      type: type?.code || 'CM',
      school: cours.ecole || this.selectedClass?.ecole || '',
      duration: Math.ceil((cours.duree || 30) / 60), // Convert minutes to hours
      color: this.getColorForType(type?.code || 'CM')
    }));
    
    // Vérifier la disponibilité du professeur
    this.checkProfesseurAvailability();
    
    this.validateForm();
  }

  // Vérifier la disponibilité du professeur
  checkProfesseurAvailability() {
    const form = this.scheduleForm();
    const warnings: {[key: string]: string} = {};
    
    if (!form.teacherId || !this.selectedTimeSlot || !this.existingSchedule) {
      this.warnings.set(warnings);
      return;
    }

    const { day, time } = this.selectedTimeSlot;
    
    // Vérifier tous les créneaux qui seront occupés par ce cours
    const timeSlots = this.getTimeSlots();
    const timeSlotIndex = timeSlots.indexOf(time);
    
    if (timeSlotIndex === -1) {
      this.warnings.set(warnings);
      return;
    }

    // Vérifier chaque créneau que le cours va occuper
    for (let i = 0; i < form.duration; i++) {
      const slotTime = timeSlots[timeSlotIndex + i];
      if (!slotTime) continue;

      // Vérifier si le professeur a déjà un cours à ce créneau
      for (const dayKey of Object.keys(this.existingSchedule)) {
        const slots = this.existingSchedule[dayKey];
        if (!slots) continue;

        for (const slotKey of Object.keys(slots)) {
          const slot = slots[slotKey];
          if (slot && 
              slot.teacherId === form.teacherId && 
              dayKey === day && 
              slotKey === slotTime &&
              slot.id !== this.existingEvent?.id) {
            warnings['professeur'] = `⚠️ Le professeur ${form.teacher} a déjà un cours "${slot.subject}" à ce créneau (${this.getDayName(dayKey)} ${slotTime})`;
            break;
          }
        }
        if (warnings['professeur']) break;
      }
      if (warnings['professeur']) break;
    }
    
    this.warnings.set(warnings);
  }

  // Obtenir les créneaux horaires
  private getTimeSlots(): string[] {
    return [
      '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00',
      '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'
    ];
  }

  // Obtenir le nom du jour en français
  private getDayName(dayKey: string): string {
    const days: {[key: string]: string} = {
      'monday': 'Lundi',
      'tuesday': 'Mardi',
      'wednesday': 'Mercredi',
      'thursday': 'Jeudi',
      'friday': 'Vendredi',
      'saturday': 'Samedi'
    };
    return days[dayKey] || dayKey;
  }

  // Get available courses filtered by selected types
  getAvailableCours(): CoursModel[] {
    if (!this.availableCours) return [];
    
    let filtered = this.availableCours;
    
    // Filter by selected course types if any are selected
    const selectedTypes = this.scheduleForm().selectedTypes;
    if (selectedTypes.length > 0) {
      filtered = filtered.filter(cours => {
        const type = cours.type;
        return selectedTypes.includes(type?.code || type?.id || '');
      });
    }
    
    return filtered;
  }

  // Check if courses are available
  hasAvailableCours(): boolean {
    return this.getAvailableCours().length > 0;
  }

  // Toggle course type filter
  toggleCourseType(typeId: string) {
    this.scheduleForm.update(form => {
      const selectedTypes = [...form.selectedTypes];
      const index = selectedTypes.indexOf(typeId);
      
      if (index > -1) {
        selectedTypes.splice(index, 1);
      } else {
        selectedTypes.push(typeId);
      }
      
      return { ...form, selectedTypes };
    });
  }

  // Validation
  validateForm(): boolean {
    const form = this.scheduleForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.coursId) {
      newErrors['cours'] = 'Veuillez sélectionner un cours';
    }

    if (form.duration < 1 || form.duration > 8) {
      newErrors['duration'] = 'La durée doit être comprise entre 1 et 8 heures';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  // Utilitaires
  getColorForType(type: string): string {
    const typeConfig = this.courseTypes.find(t => t.id === type);
    return typeConfig?.color || 'bg-gray-100 border-gray-300 text-gray-800';
  }

  getSchoolName(schoolId: string): string {
    const school = this.schools.find(s => s.id === schoolId);
    return school?.name || schoolId;
  }

  getSchoolColor(schoolId: string): string {
    const school = this.schools.find(s => s.id === schoolId);
    return school?.color || 'bg-gray-500';
  }

  updateDuration(value: string) {
    const duration = Math.max(1, Math.min(8, +value)); // Clamp between 1 and 8
    this.scheduleForm.update(form => ({
      ...form,
      duration: duration
    }));
    // Revérifier la disponibilité du professeur avec la nouvelle durée
    this.checkProfesseurAvailability();
    this.validateForm();
  }

  updateDescription(value: string) {
    this.scheduleForm.update(form => ({
      ...form,
      description: value
    }));
  }

  updateCourseType(typeId: string, color: string) {
    this.scheduleForm.update(form => ({
      ...form,
      type: typeId as any,
      color: color
    }));
  }

  // Actions principales
  onClose() {
    this.close.emit();
  }

  onSave() {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading.set(true);

    // Simuler une sauvegarde
    setTimeout(() => {
      const form = this.scheduleForm();
      const eventData = {
        id: this.existingEvent?.id || Date.now().toString(),
        startTime: this.selectedTimeSlot?.time.split('-')[0] || '08:00',
        endTime: this.selectedTimeSlot?.time.split('-')[1] || '09:00',
        coursId: form.coursId,
        subject: form.subject,
        teacher: form.teacher,
        teacherId: form.teacherId,
        type: form.type,
        school: form.school,
        color: form.color,
        description: form.description,
        duration: form.duration
      };

      if (this.isEditMode()) {
        this.eventUpdated.emit(eventData);
      } else {
        this.eventAdded.emit(eventData);
      }

      this.isLoading.set(false);
      this.onClose();
    }, 1000);
  }

  onDelete() {
    if (this.isEditMode() && confirm('Êtes-vous sûr de vouloir supprimer ce cours ?')) {
      // Émettre un événement de suppression
      this.eventUpdated.emit(null);
      this.onClose();
    }
  }
}
