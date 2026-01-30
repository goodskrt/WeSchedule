import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface ScheduleForm {
  subject: string;
  subjectId: string;
  teacher: string;
  teacherId: string;
  room: string;
  roomId: string;
  type: 'CM' | 'TD' | 'TP' | 'Exam';
  school: string;
  students: number;
  duration: number;
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

@Component({
  selector: 'app-modal-schedule',
  imports: [CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './modal-schedule.html',
  styleUrl: './modal-schedule.scss',
})
export class ModalSchedule implements OnInit, OnDestroy {
  @Input() existingEvent: any = null;
  @Input() selectedTimeSlot: {day: string, time: string} | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() eventAdded = new EventEmitter<any>();
  @Output() eventUpdated = new EventEmitter<any>();

  protected readonly scheduleForm = signal<ScheduleForm>({
    subject: '',
    subjectId: '',
    teacher: '',
    teacherId: '',
    room: '',
    roomId: '',
    type: 'CM',
    school: '',
    students: 30,
    duration: 60,
    description: '',
    color: 'bg-blue-100 border-blue-300 text-blue-800'
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});
  protected readonly currentStep = signal<1 | 2>(1);
  protected readonly isEditMode = signal(false);
  protected readonly searchSubject = signal('');
  protected readonly searchTeacher = signal('');
  protected readonly searchRoom = signal('');
  protected readonly showConflicts = signal(false);
  protected readonly detectedConflicts = signal<string[]>([]);

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
        subject: this.existingEvent.subject || '',
        subjectId: this.existingEvent.subjectId || '',
        teacher: this.existingEvent.teacher || '',
        teacherId: this.existingEvent.teacherId || '',
        room: this.existingEvent.room || '',
        roomId: this.existingEvent.roomId || '',
        type: this.existingEvent.type || 'CM',
        school: this.existingEvent.school || '',
        students: this.existingEvent.students || 30,
        duration: this.existingEvent.duration || 60,
        description: this.existingEvent.description || '',
        color: this.existingEvent.color || 'bg-blue-100 border-blue-300 text-blue-800'
      });
    }
    
    // Bloquer le scroll de la page principale
    document.body.classList.add('modal-open');
  }

  ngOnDestroy() {
    // Restaurer le scroll de la page principale
    document.body.classList.remove('modal-open');
  }

  // Filtrage des données
  getFilteredSubjects() {
    const search = this.searchSubject().toLowerCase();
    return this.availableSubjects().filter(subject => 
      subject.name.toLowerCase().includes(search) ||
      subject.code.toLowerCase().includes(search)
    );
  }

  getFilteredTeachers() {
    const search = this.searchTeacher().toLowerCase();
    const form = this.scheduleForm();
    return this.availableTeachers().filter(teacher => {
      const matchesSearch = teacher.name.toLowerCase().includes(search) ||
                           teacher.speciality.toLowerCase().includes(search);
      const matchesSchool = !form.school || teacher.school === form.school;
      return matchesSearch && matchesSchool;
    });
  }

  getFilteredRooms() {
    const search = this.searchRoom().toLowerCase();
    const form = this.scheduleForm();
    return this.availableRooms().filter(room => {
      const matchesSearch = room.name.toLowerCase().includes(search) ||
                           room.type.toLowerCase().includes(search);
      const matchesCapacity = room.capacity >= form.students;
      return matchesSearch && matchesCapacity;
    });
  }

  // Sélection des éléments
  selectSubject(subject: Subject) {
    this.scheduleForm.update(form => ({
      ...form,
      subject: subject.name,
      subjectId: subject.id,
      school: subject.school,
      type: subject.type,
      color: this.getColorForType(subject.type)
    }));
    this.searchSubject.set('');
    this.validateStep1();
  }

  selectTeacher(teacher: Teacher) {
    this.scheduleForm.update(form => ({
      ...form,
      teacher: teacher.name,
      teacherId: teacher.id
    }));
    this.searchTeacher.set('');
    this.validateStep1();
  }

  selectRoom(room: Room) {
    this.scheduleForm.update(form => ({
      ...form,
      room: room.name,
      roomId: room.id
    }));
    this.searchRoom.set('');
    this.validateStep2();
  }

  // Validation
  validateStep1(): boolean {
    const form = this.scheduleForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.subject.trim()) {
      newErrors['subject'] = 'La matière est obligatoire';
    }

    if (!form.teacher.trim()) {
      newErrors['teacher'] = 'L\'enseignant est obligatoire';
    }

    if (!form.school.trim()) {
      newErrors['school'] = 'L\'école est obligatoire';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  validateStep2(): boolean {
    const form = this.scheduleForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.room.trim()) {
      newErrors['room'] = 'La salle est obligatoire';
    }

    if (form.students <= 0) {
      newErrors['students'] = 'Le nombre d\'étudiants doit être supérieur à 0';
    }

    if (form.duration < 30) {
      newErrors['duration'] = 'La durée doit être d\'au moins 30 minutes';
    }

    // Vérifier la capacité de la salle
    const selectedRoom = this.availableRooms().find(r => r.name === form.room);
    if (selectedRoom && form.students > selectedRoom.capacity) {
      newErrors['students'] = `La salle ${selectedRoom.name} ne peut accueillir que ${selectedRoom.capacity} étudiants`;
    }

    this.errors.set(newErrors);
    this.checkConflicts();
    return Object.keys(newErrors).length === 0;
  }

  checkConflicts() {
    const form = this.scheduleForm();
    const conflicts: string[] = [];

    if (this.selectedTimeSlot && form.teacher && form.room) {
      // Simuler la détection de conflits
      // Dans une vraie application, ceci ferait appel à un service
      if (form.teacher === 'Dr. Martin' && this.selectedTimeSlot.time === '10:00-11:00') {
        conflicts.push(`${form.teacher} a déjà un cours à ${this.selectedTimeSlot.time}`);
      }
      
      if (form.room === 'Salle 101' && this.selectedTimeSlot.day === 'monday') {
        conflicts.push(`${form.room} est déjà occupée le ${this.selectedTimeSlot.day} à ${this.selectedTimeSlot.time}`);
      }
    }

    this.detectedConflicts.set(conflicts);
    this.showConflicts.set(conflicts.length > 0);
  }

  // Navigation entre étapes
  nextStep() {
    if (this.currentStep() === 1 && this.validateStep1()) {
      this.currentStep.set(2);
    }
  }

  previousStep() {
    if (this.currentStep() === 2) {
      this.currentStep.set(1);
    }
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

  getTeacherSpeciality(teacherName: string): string {
    const teacher = this.availableTeachers().find(t => t.name === teacherName);
    return teacher?.speciality || '';
  }

  getRoomCapacity(roomName: string): number {
    const room = this.availableRooms().find(r => r.name === roomName);
    return room?.capacity || 0;
  }

  updateStudents(value: string) {
    this.scheduleForm.update(form => ({
      ...form,
      students: +value
    }));
    this.validateStep2();
  }

  updateDuration(value: string) {
    this.scheduleForm.update(form => ({
      ...form,
      duration: +value
    }));
    this.validateStep2();
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
    if (!this.validateStep1() || !this.validateStep2()) {
      return;
    }

    if (this.detectedConflicts().length > 0) {
      const confirmSave = confirm(
        `Des conflits ont été détectés :\n${this.detectedConflicts().join('\n')}\n\nVoulez-vous continuer ?`
      );
      if (!confirmSave) {
        return;
      }
    }

    this.isLoading.set(true);

    // Simuler une sauvegarde
    setTimeout(() => {
      const form = this.scheduleForm();
      const eventData = {
        id: this.existingEvent?.id || Date.now().toString(),
        startTime: this.selectedTimeSlot?.time.split('-')[0] || '08:00',
        endTime: this.selectedTimeSlot?.time.split('-')[1] || '09:00',
        subject: form.subject,
        teacher: form.teacher,
        room: form.room,
        type: form.type,
        school: form.school,
        students: form.students,
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
