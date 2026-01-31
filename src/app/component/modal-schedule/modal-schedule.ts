import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface ScheduleForm {
  ueId: string;
  subject: string;
  teacher: string;
  teacherId: string;
  type: 'CM' | 'TD' | 'TP' | 'Exam';
  school: string;
  duration: number; // Duration in hours (1-8)
  description: string;
  color: string;
}

interface Subject {
  id: string;
  name: string;
  code: string;
  school: string;
  type: 'CM' | 'TD' | 'TP';
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

interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
  type: 'CM' | 'TD' | 'TP';
  professeurId: string; // Professor assigned to this UE
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
  @Input() availableUEs: UEModel[] = [];
  @Input() availableProfesseurs: Professeur[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() eventAdded = new EventEmitter<any>();
  @Output() eventUpdated = new EventEmitter<any>();

  protected readonly scheduleForm = signal<ScheduleForm>({
    ueId: '',
    subject: '',
    teacher: '',
    teacherId: '',
    type: 'CM',
    school: '',
    duration: 1, // Duration in hours
    description: '',
    color: 'bg-blue-100 border-blue-300 text-blue-800'
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});
  protected readonly isEditMode = signal(false);

  // Données disponibles
  protected readonly availableSubjects = signal<Subject[]>([
    { id: '1', name: 'Algorithmique', code: 'ALG101', school: 'sji', type: 'CM' },
    { id: '2', name: 'Base de Données', code: 'BDD201', school: 'sji', type: 'TP' },
    { id: '3', name: 'Gestion Financière', code: 'GF301', school: 'sjm', type: 'CM' },
    { id: '4', name: 'Mathématiques', code: 'MATH101', school: 'prepa', type: 'TD' },
    { id: '5', name: 'Physique Quantique', code: 'PHY401', school: 'cpge', type: 'CM' },
    { id: '6', name: 'Réseaux', code: 'RES301', school: 'sji', type: 'TP' },
    { id: '7', name: 'Marketing Digital', code: 'MKT201', school: 'sjm', type: 'CM' },
    { id: '8', name: 'Chimie Organique', code: 'CHI301', school: 'prepa', type: 'TP' },
    { id: '9', name: 'Intelligence Artificielle', code: 'IA401', school: 'sji', type: 'CM' },
    { id: '10', name: 'Comptabilité', code: 'CPT101', school: 'sjm', type: 'TD' },
    { id: '11', name: 'Programmation Web', code: 'WEB201', school: 'sji', type: 'TP' },
    { id: '12', name: 'Économie', code: 'ECO101', school: 'sjm', type: 'CM' },
    { id: '13', name: 'Statistiques', code: 'STAT201', school: 'prepa', type: 'TD' },
    { id: '14', name: 'Électronique', code: 'ELEC301', school: 'cpge', type: 'TP' },
    { id: '15', name: 'Droit des Affaires', code: 'DRT201', school: 'sjm', type: 'CM' }
  ]);

  protected readonly availableTeachers = signal<Teacher[]>([
    { id: '1', name: 'Dr. Martin', speciality: 'Informatique', school: 'sji' },
    { id: '2', name: 'Prof. Dubois', speciality: 'Base de Données', school: 'sji' },
    { id: '3', name: 'Dr. Nguyen', speciality: 'Gestion', school: 'sjm' },
    { id: '4', name: 'Prof. Bernard', speciality: 'Mathématiques', school: 'prepa' },
    { id: '5', name: 'Dr. Laurent', speciality: 'Physique', school: 'cpge' },
    { id: '6', name: 'Prof. Moreau', speciality: 'Chimie', school: 'prepa' },
    { id: '7', name: 'Dr. Patel', speciality: 'Marketing', school: 'sjm' },
    { id: '8', name: 'Dr. Smith', speciality: 'Intelligence Artificielle', school: 'sji' },
    { id: '9', name: 'Prof. Leroy', speciality: 'Économie', school: 'sjm' },
    { id: '10', name: 'Dr. Garcia', speciality: 'Électronique', school: 'cpge' }
  ]);

  protected readonly availableRooms = signal<Room[]>([
    { id: '1', name: 'Salle 101', capacity: 40, type: 'Cours', equipment: ['Projecteur', 'Tableau'] },
    { id: '2', name: 'Salle 102', capacity: 35, type: 'Cours', equipment: ['Projecteur', 'Tableau'] },
    { id: '3', name: 'Salle 205', capacity: 30, type: 'Cours', equipment: ['Projecteur', 'Tableau'] },
    { id: '4', name: 'Salle 301', capacity: 25, type: 'Cours', equipment: ['Projecteur', 'Tableau'] },
    { id: '5', name: 'Lab Info 1', capacity: 20, type: 'Informatique', equipment: ['Ordinateurs', 'Projecteur'] },
    { id: '6', name: 'Lab Info 2', capacity: 25, type: 'Informatique', equipment: ['Ordinateurs', 'Projecteur'] },
    { id: '7', name: 'Lab Réseau', capacity: 15, type: 'Réseau', equipment: ['Équipements réseau', 'Ordinateurs'] },
    { id: '8', name: 'Lab Chimie', capacity: 20, type: 'Laboratoire', equipment: ['Équipements chimie', 'Hotte'] },
    { id: '9', name: 'Amphi A', capacity: 100, type: 'Amphithéâtre', equipment: ['Sono', 'Projecteur', 'Écran géant'] },
    { id: '10', name: 'Amphi B', capacity: 80, type: 'Amphithéâtre', equipment: ['Sono', 'Projecteur', 'Écran géant'] }
  ]);

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
    { id: 'Exam', name: 'Examen', icon: 'clipboard', color: 'bg-red-100 border-red-300 text-red-800' }
  ];

  ngOnInit() {
    if (this.existingEvent) {
      this.isEditMode.set(true);
      this.scheduleForm.set({
        ueId: this.existingEvent.ueId || '',
        subject: this.existingEvent.subject || '',
        teacher: this.existingEvent.teacher || '',
        teacherId: this.existingEvent.teacherId || '',
        type: this.existingEvent.type || 'CM',
        school: this.existingEvent.school || '',
        duration: this.existingEvent.duration || 1,
        description: this.existingEvent.description || '',
        color: this.existingEvent.color || 'bg-blue-100 border-blue-300 text-blue-800'
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

  // UE Selection
  onUEChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    const ueId = target.value;
    if (ueId) {
      const selectedUE = this.getAvailableUEs().find(ue => ue.id === ueId);
      if (selectedUE) {
        this.selectUE(selectedUE);
      }
    }
  }

  selectUE(ue: UEModel) {
    // Find the professor for this UE
    const professor = this.availableProfesseurs.find(prof => prof.id === ue.professeurId);
    
    this.scheduleForm.update(form => ({
      ...form,
      ueId: ue.id,
      subject: ue.nom,
      teacher: professor ? `${professor.prenom} ${professor.nom}` : 'Professeur non assigné',
      teacherId: ue.professeurId,
      type: ue.type,
      school: ue.ecole,
      color: this.getColorForType(ue.type)
    }));
    
    this.validateForm();
  }

  // Get available UEs for the selected class
  getAvailableUEs(): UEModel[] {
    return this.availableUEs || [];
  }

  // Check if UEs are available
  hasAvailableUEs(): boolean {
    return this.availableUEs && this.availableUEs.length > 0;
  }

  // Validation
  validateForm(): boolean {
    const form = this.scheduleForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.ueId) {
      newErrors['ue'] = 'Veuillez sélectionner une UE';
    }

    if (form.duration < 1 || form.duration > 8) {
      newErrors['duration'] = 'La durée doit être comprise entre 1 et 8 heures';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  checkConflicts() {
    // Simplified conflict detection since we removed complex validation
    // In a real application, this would check for teacher/room conflicts
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
        ueId: form.ueId,
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
