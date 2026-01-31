import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { ModalSchedule } from '../../component/modal-schedule/modal-schedule';

interface TimeSlot {
  id: string;
  startTime: string;
  endTime: string;
  subject: string;
  teacher: string;
  room: string;
  type: 'CM' | 'TD' | 'TP' | 'Exam';
  school: string;
  classId: string;
  students: number;
  color: string;
}

interface WeekSchedule {
  [day: string]: {
    [timeSlot: string]: TimeSlot | null;
  };
}

interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  type: 'course' | 'exam' | 'meeting' | 'event';
  location: string;
  description: string;
  color: string;
}

interface ClasseModel {
  id: string;
  nom: string;
  niveau: string;
  ecole: string;
  semestre: number;
  effectif: number;
  effectifMax: number;
  professeurs: string[];
  specialite?: string;
  ues: string[];
}

interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
  type: 'CM' | 'TD' | 'TP';
  professeurId: string;
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
  selector: 'app-emploi-de-temps',
  imports: [CommonModule, FormsModule, SvgIconComponent, ModalSchedule],
  templateUrl: './emploi-de-temps.html',
  styleUrl: './emploi-de-temps.scss',
})
export class EmploiDeTemps {
  protected readonly currentView = signal<'week' | 'month' | 'day'>('week');
  protected readonly selectedWeek = signal(new Date());
  protected readonly selectedSchool = signal<string>('sji');
  protected readonly selectedClass = signal<string>('1'); // Default to first class
  protected readonly selectedTeacher = signal<string>('all');
  protected readonly showConflicts = signal(false);
  protected readonly showScheduleModal = signal(false);
  protected readonly isEditMode = signal(false);
  protected readonly selectedTimeSlot = signal<{day: string, time: string} | null>(null);
  protected readonly currentEditingCourse = signal<any>(null);
  protected readonly searchQuery = signal<string>('');
  protected readonly showAdvancedFilters = signal(false);

  // Données des classes et UE
  protected readonly classes = signal<ClasseModel[]>([
    {
      id: '1',
      nom: 'Informatique L1A',
      niveau: 'L1',
      ecole: 'sji',
      semestre: 1,
      effectif: 45,
      effectifMax: 50,
      professeurs: ['1', '2'],
      specialite: 'Informatique',
      ues: ['1', '2'] // INF101, MAT101
    },
    {
      id: '2',
      nom: 'Informatique L1B',
      niveau: 'L1',
      ecole: 'sji',
      semestre: 1,
      effectif: 42,
      effectifMax: 50,
      professeurs: ['1'],
      specialite: 'Informatique',
      ues: ['1', '2'] // INF101, MAT101
    },
    {
      id: '3',
      nom: 'Gestion L1',
      niveau: 'L1',
      ecole: 'sjm',
      semestre: 1,
      effectif: 50,
      effectifMax: 55,
      professeurs: ['3'],
      specialite: 'Gestion',
      ues: ['4'] // GES101
    },
    {
      id: '4',
      nom: 'Marketing L2',
      niveau: 'L2',
      ecole: 'sjm',
      semestre: 3,
      effectif: 35,
      effectifMax: 40,
      professeurs: ['3'],
      specialite: 'Marketing',
      ues: ['5'] // MKT201
    },
    {
      id: '5',
      nom: 'Prépa Scientifique 1A',
      niveau: 'Prépa',
      ecole: 'prepa',
      semestre: 1,
      effectif: 30,
      effectifMax: 35,
      professeurs: ['4', '6'],
      specialite: 'Sciences',
      ues: ['6', '8'] // MAT201, CHI101
    },
    {
      id: '6',
      nom: 'MPSI',
      niveau: 'CPGE',
      ecole: 'cpge',
      semestre: 1,
      effectif: 35,
      effectifMax: 40,
      professeurs: ['4', '5'],
      specialite: 'Mathématiques-Physique',
      ues: ['6', '7'] // MAT201, PHY101
    }
  ]);

  protected readonly ues = signal<UEModel[]>([
    { id: '1', code: 'INF101', nom: 'Introduction à la Programmation', credits: 6, semestre: 1, ecole: 'sji', type: 'CM', professeurId: '1' },
    { id: '2', code: 'MAT101', nom: 'Mathématiques Fondamentales', credits: 6, semestre: 1, ecole: 'sji', type: 'TD', professeurId: '2' },
    { id: '3', code: 'INF201', nom: 'Programmation Orientée Objet', credits: 6, semestre: 3, ecole: 'sji', type: 'TP', professeurId: '1' },
    { id: '4', code: 'GES101', nom: 'Principes de Gestion', credits: 4, semestre: 1, ecole: 'sjm', type: 'CM', professeurId: '3' },
    { id: '5', code: 'MKT201', nom: 'Marketing Digital', credits: 5, semestre: 3, ecole: 'sjm', type: 'CM', professeurId: '3' },
    { id: '6', code: 'MAT201', nom: 'Mathématiques Supérieures', credits: 8, semestre: 1, ecole: 'prepa', type: 'CM', professeurId: '4' },
    { id: '7', code: 'PHY101', nom: 'Physique Générale', credits: 6, semestre: 1, ecole: 'cpge', type: 'CM', professeurId: '5' },
    { id: '8', code: 'CHI101', nom: 'Chimie Générale', credits: 6, semestre: 1, ecole: 'prepa', type: 'TP', professeurId: '6' }
  ]);

  protected readonly professeurs = signal<Professeur[]>([
    { id: '1', nom: 'Dubois', prenom: 'Martin', email: 'martin.dubois@saintfomekong.edu', specialites: ['Informatique'], ecoles: ['sji'] },
    { id: '2', nom: 'Laurent', prenom: 'Marie', email: 'marie.laurent@saintfomekong.edu', specialites: ['Mathématiques'], ecoles: ['sji'] },
    { id: '3', nom: 'Nguyen', prenom: 'Paul', email: 'paul.nguyen@saintfomekong.edu', specialites: ['Gestion', 'Marketing'], ecoles: ['sjm'] },
    { id: '4', nom: 'Bernard', prenom: 'Sophie', email: 'sophie.bernard@saintfomekong.edu', specialites: ['Mathématiques'], ecoles: ['prepa', 'cpge'] },
    { id: '5', nom: 'Moreau', prenom: 'Jean', email: 'jean.moreau@saintfomekong.edu', specialites: ['Physique'], ecoles: ['cpge'] },
    { id: '6', nom: 'Leroy', prenom: 'Pierre', email: 'pierre.leroy@saintfomekong.edu', specialites: ['Chimie'], ecoles: ['prepa'] }
  ]);

  // Liste des salles disponibles
  protected readonly availableRooms = signal([
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

  protected readonly timeSlots = [
    '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00',
    '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'
  ];

  protected readonly days = [
    { key: 'monday', name: 'Lundi', short: 'Lun' },
    { key: 'tuesday', name: 'Mardi', short: 'Mar' },
    { key: 'wednesday', name: 'Mercredi', short: 'Mer' },
    { key: 'thursday', name: 'Jeudi', short: 'Jeu' },
    { key: 'friday', name: 'Vendredi', short: 'Ven' },
    { key: 'saturday', name: 'Samedi', short: 'Sam' }
  ];

  protected readonly schools = [
    { id: 'sji', name: 'SJI', color: 'bg-blue-500' },
    { id: 'sjm', name: 'SJM', color: 'bg-green-500' },
    { id: 'prepa', name: 'PrepaVogt', color: 'bg-purple-500' },
    { id: 'cpge', name: 'CPGE', color: 'bg-orange-500' }
  ];

  protected readonly schedule = signal<WeekSchedule>({
    monday: {
      '08:00-09:00': {
        id: '1',
        startTime: '08:00',
        endTime: '09:00',
        subject: 'Introduction à la Programmation',
        teacher: 'Martin Dubois',
        room: 'Salle 101',
        type: 'CM',
        school: 'sji',
        classId: '1',
        students: 45,
        color: 'bg-blue-100 border-blue-300 text-blue-800'
      },
      '09:00-10:00': null,
      '10:00-11:00': {
        id: '2',
        startTime: '10:00',
        endTime: '11:00',
        subject: 'Mathématiques Fondamentales',
        teacher: 'Marie Laurent',
        room: 'Salle 205',
        type: 'TD',
        school: 'sji',
        classId: '2',
        students: 42,
        color: 'bg-green-100 border-green-300 text-green-800'
      },
      '11:00-12:00': null,
      '13:00-14:00': null,
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    },
    tuesday: {
      '08:00-09:00': null,
      '09:00-10:00': {
        id: '3',
        startTime: '09:00',
        endTime: '10:00',
        subject: 'Principes de Gestion',
        teacher: 'Paul Nguyen',
        room: 'Amphi A',
        type: 'CM',
        school: 'sjm',
        classId: '3',
        students: 50,
        color: 'bg-green-100 border-green-300 text-green-800'
      },
      '10:00-11:00': null,
      '11:00-12:00': null,
      '13:00-14:00': {
        id: '4',
        startTime: '13:00',
        endTime: '14:00',
        subject: 'Mathématiques Supérieures',
        teacher: 'Sophie Bernard',
        room: 'Salle 301',
        type: 'CM',
        school: 'prepa',
        classId: '5',
        students: 30,
        color: 'bg-purple-100 border-purple-300 text-purple-800'
      },
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    },
    wednesday: {
      '08:00-09:00': {
        id: '5',
        startTime: '08:00',
        endTime: '09:00',
        subject: 'Mathématiques Supérieures',
        teacher: 'Sophie Bernard',
        room: 'Salle 301',
        type: 'CM',
        school: 'cpge',
        classId: '6',
        students: 35,
        color: 'bg-orange-100 border-orange-300 text-orange-800'
      },
      '09:00-10:00': null,
      '10:00-11:00': null,
      '11:00-12:00': null,
      '13:00-14:00': null,
      '14:00-15:00': null,
      '15:00-16:00': {
        id: '6',
        startTime: '15:00',
        endTime: '16:00',
        subject: 'Programmation Orientée Objet',
        teacher: 'Martin Dubois',
        room: 'Lab Info 1',
        type: 'TP',
        school: 'sji',
        classId: '1',
        students: 45,
        color: 'bg-purple-100 border-purple-300 text-purple-800'
      },
      '16:00-17:00': null
    },
    thursday: {
      '08:00-09:00': null,
      '09:00-10:00': null,
      '10:00-11:00': {
        id: '7',
        startTime: '10:00',
        endTime: '11:00',
        subject: 'Marketing Digital',
        teacher: 'Paul Nguyen',
        room: 'Salle 102',
        type: 'CM',
        school: 'sjm',
        classId: '4',
        students: 35,
        color: 'bg-green-100 border-green-300 text-green-800'
      },
      '11:00-12:00': null,
      '13:00-14:00': null,
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    },
    friday: {
      '08:00-09:00': null,
      '09:00-10:00': null,
      '10:00-11:00': null,
      '11:00-12:00': null,
      '13:00-14:00': {
        id: '8',
        startTime: '13:00',
        endTime: '14:00',
        subject: 'Examen Final',
        teacher: 'Multiple',
        room: 'Amphi B',
        type: 'Exam',
        school: 'cpge',
        classId: '6',
        students: 35,
        color: 'bg-red-100 border-red-300 text-red-800'
      },
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    },
    saturday: {
      '08:00-09:00': null,
      '09:00-10:00': null,
      '10:00-11:00': null,
      '11:00-12:00': null,
      '13:00-14:00': null,
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    }
  });

  protected readonly upcomingEvents = signal<CalendarEvent[]>([
    {
      id: '1',
      title: 'Réunion Pédagogique',
      start: new Date(2026, 11, 15, 14, 0),
      end: new Date(2026, 11, 15, 16, 0),
      type: 'meeting',
      location: 'Salle de Conférence',
      description: 'Réunion mensuelle des enseignants',
      color: 'bg-blue-500'
    },
    {
      id: '2',
      title: 'Journée Portes Ouvertes',
      start: new Date(2026, 11, 20, 9, 0),
      end: new Date(2026, 11, 20, 17, 0),
      type: 'event',
      location: 'Campus',
      description: 'Présentation des formations aux futurs étudiants',
      color: 'bg-green-500'
    }
  ]);

  // Méthodes de filtrage
  onSchoolFilterChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setSchoolFilter(target.value);
    // Set first class of the new school as default
    const classesForSchool = this.getClassesForSchool();
    if (classesForSchool.length > 0) {
      this.selectedClass.set(classesForSchool[0].id);
    }
  }

  onClassFilterChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setClassFilter(target.value);
  }

  onTeacherFilterChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.setTeacherFilter(target.value || 'all');
  }

  setView(view: 'week' | 'month' | 'day') {
    this.currentView.set(view);
  }

  setSchoolFilter(school: string) {
    this.selectedSchool.set(school);
  }

  setClassFilter(classId: string) {
    this.selectedClass.set(classId);
  }

  setTeacherFilter(teacher: string) {
    this.selectedTeacher.set(teacher);
  }

  setSearchQuery(query: string) {
    this.searchQuery.set(query);
  }

  clearSearch() {
    this.searchQuery.set('');
  }

  toggleConflicts() {
    this.showConflicts.update(value => !value);
  }

  resetAllFilters() {
    this.selectedSchool.set('sji');
    const classesForSchool = this.getClassesForSchool();
    if (classesForSchool.length > 0) {
      this.selectedClass.set(classesForSchool[0].id);
    }
    this.selectedTeacher.set('all');
    this.searchQuery.set('');
    this.showConflicts.set(false);
  }

  // Get classes for selected school
  getClassesForSchool(): ClasseModel[] {
    return this.classes().filter(classe => classe.ecole === this.selectedSchool());
  }

  // Get selected class details
  getSelectedClass(): ClasseModel | null {
    if (!this.selectedClass()) return null;
    return this.classes().find(classe => classe.id === this.selectedClass()) || null;
  }

  // Get UEs for selected class
  getUEsForSelectedClass(): UEModel[] {
    const selectedClass = this.getSelectedClass();
    if (!selectedClass) return [];
    
    // Filter UEs that are associated with the selected class
    return this.ues().filter(ue => selectedClass.ues.includes(ue.id));
  }

  // Get professors for selected class
  getProfesseursForSelectedClass(): Professeur[] {
    const selectedClass = this.getSelectedClass();
    if (!selectedClass) return [];
    return this.professeurs().filter(prof => selectedClass.professeurs.includes(prof.id));
  }

  // Get class name
  getClassName(classId: string): string {
    const classe = this.classes().find(c => c.id === classId);
    return classe ? classe.nom : 'Classe inconnue';
  }

  // Get professor full name
  getProfesseurFullName(professeurId: string): string {
    const prof = this.professeurs().find(p => p.id === professeurId);
    return prof ? `${prof.prenom} ${prof.nom}` : 'Professeur inconnu';
  }

  // Navigation
  previousWeek() {
    this.selectedWeek.update(date => {
      const newDate = new Date(date);
      newDate.setDate(newDate.getDate() - 7);
      return newDate;
    });
  }

  nextWeek() {
    this.selectedWeek.update(date => {
      const newDate = new Date(date);
      newDate.setDate(newDate.getDate() + 7);
      return newDate;
    });
  }

  goToToday() {
    this.selectedWeek.set(new Date());
  }

  getWeekRange(): string {
    const startOfWeek = new Date(this.selectedWeek());
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1);
    startOfWeek.setDate(diff);
    
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    
    const options: Intl.DateTimeFormatOptions = { 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    };
    
    return `${startOfWeek.toLocaleDateString('fr-FR', options)} - ${endOfWeek.toLocaleDateString('fr-FR', options)}`;
  }

  // Filtrage des données
  getFilteredSchedule(): WeekSchedule {
    const schedule = this.schedule();
    
    if (this.selectedTeacher() === 'all' && !this.searchQuery().trim()) {
      // Show schedule for selected class only
      const filtered: WeekSchedule = {};
      Object.keys(schedule).forEach(day => {
        filtered[day] = {};
        Object.keys(schedule[day]).forEach(timeSlot => {
          const slot = schedule[day][timeSlot];
          if (slot && slot.classId === this.selectedClass()) {
            filtered[day][timeSlot] = slot;
          } else {
            filtered[day][timeSlot] = null;
          }
        });
      });
      return filtered;
    }

    const filtered: WeekSchedule = {};
    const searchTerm = this.searchQuery().toLowerCase();
    
    Object.keys(schedule).forEach(day => {
      filtered[day] = {};
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          let include = true;
          
          // Filtre par classe (toujours appliqué)
          if (slot.classId !== this.selectedClass()) {
            include = false;
          }
          
          // Filtre par enseignant
          if (this.selectedTeacher() !== 'all' && !slot.teacher.toLowerCase().includes(this.selectedTeacher().toLowerCase())) {
            include = false;
          }

          // Filtre par recherche
          if (searchTerm && include) {
            const searchableText = `${slot.subject} ${slot.teacher}`.toLowerCase();
            if (!searchableText.includes(searchTerm)) {
              include = false;
            }
          }
          
          filtered[day][timeSlot] = include ? slot : null;
        } else {
          filtered[day][timeSlot] = null;
        }
      });
    });
    
    return filtered;
  }

  getSchoolColor(schoolId: string): string {
    return this.schools.find(s => s.id === schoolId)?.color || 'bg-gray-500';
  }

  getUniqueTeachers(): string[] {
    const schedule = this.schedule();
    const teachers = new Set<string>();
    
    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        if (slot && slot.classId === this.selectedClass()) {
          teachers.add(slot.teacher);
        }
      });
    });
    
    return Array.from(teachers).sort();
  }

  // Gestion des conflits
  hasConflict(day: string, timeSlot: string): boolean {
    const slot = this.schedule()[day][timeSlot];
    if (!slot) return false;
    
    const schedule = this.schedule();
    
    // Vérifier les conflits d'enseignant : même enseignant à la même heure
    for (const dayKey of Object.keys(schedule)) {
      for (const timeKey of Object.keys(schedule[dayKey])) {
        const otherSlot = schedule[dayKey][timeKey];
        if (otherSlot && otherSlot.id !== slot.id && 
            otherSlot.teacher === slot.teacher && 
            timeKey === timeSlot) {
          return true;
        }
      }
    }
    
    // Vérifier les conflits de salle : même salle à la même heure
    for (const dayKey of Object.keys(schedule)) {
      for (const timeKey of Object.keys(schedule[dayKey])) {
        const otherSlot = schedule[dayKey][timeKey];
        if (otherSlot && otherSlot.id !== slot.id && 
            otherSlot.room === slot.room && 
            timeKey === timeSlot) {
          return true;
        }
      }
    }
    
    return false;
  }

  detectConflicts() {
    const conflicts: Array<{day: string, time: string, type: string, details: string}> = [];
    const schedule = this.schedule();
    
    Object.keys(schedule).forEach(day => {
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot && this.hasConflict(day, timeSlot)) {
          conflicts.push({
            day,
            time: timeSlot,
            type: 'Conflit détecté',
            details: `${slot.subject} - ${slot.teacher} - ${slot.room}`
          });
        }
      });
    });
    
    if (conflicts.length > 0) {
      alert(`${conflicts.length} conflit(s) détecté(s) dans l'emploi du temps`);
    } else {
      alert('Aucun conflit détecté dans l\'emploi du temps');
    }
    
    return conflicts;
  }

  // Gestion de la modale
  addEvent() {
    this.selectedTimeSlot.set(null);
    this.currentEditingCourse.set(null);
    this.showScheduleModal.set(true);
  }

  addEventToSlot(day: string, timeSlot: string) {
    this.selectedTimeSlot.set({ day, time: timeSlot });
    this.currentEditingCourse.set(null);
    this.showScheduleModal.set(true);
  }

  editTimeSlot(day: string, timeSlot: string) {
    const slot = this.schedule()[day][timeSlot];
    if (slot) {
      this.selectedTimeSlot.set({ day, time: timeSlot });
      this.currentEditingCourse.set(slot);
      this.showScheduleModal.set(true);
    }
  }

  closeScheduleModal() {
    this.showScheduleModal.set(false);
    this.selectedTimeSlot.set(null);
    this.currentEditingCourse.set(null);
  }

  onScheduleSave(eventData: any) {
    if (!eventData) {
      // Suppression du cours
      if (this.selectedTimeSlot()) {
        const { day, time } = this.selectedTimeSlot()!;
        this.deleteTimeSlot(day, time);
      }
      return;
    }

    if (!this.selectedTimeSlot()) {
      alert('Erreur: Aucun créneau sélectionné');
      return;
    }

    const { day, time } = this.selectedTimeSlot()!;
    const duration = eventData.duration || 1;
    
    // Calculate which time slots to occupy based on duration
    const timeSlotIndex = this.timeSlots.indexOf(time);
    if (timeSlotIndex === -1) {
      alert('Erreur: Créneau horaire invalide');
      return;
    }

    // Check if we have enough consecutive slots
    if (timeSlotIndex + duration > this.timeSlots.length) {
      alert(`Impossible de programmer un cours de ${duration}h à partir de ${time}. Pas assez de créneaux disponibles.`);
      return;
    }

    // Check for conflicts in the required time slots
    const conflictingSlots = [];
    for (let i = 0; i < duration; i++) {
      const slotTime = this.timeSlots[timeSlotIndex + i];
      if (this.schedule()[day][slotTime] && this.schedule()[day][slotTime]?.id !== eventData.id) {
        conflictingSlots.push(slotTime);
      }
    }

    if (conflictingSlots.length > 0) {
      const confirmOverwrite = confirm(
        `Les créneaux suivants sont déjà occupés: ${conflictingSlots.join(', ')}\n\nVoulez-vous les remplacer ?`
      );
      if (!confirmOverwrite) {
        return;
      }
    }

    this.schedule.update(schedule => {
      const newSchedule = { ...schedule };
      newSchedule[day] = { ...newSchedule[day] };
      
      // Clear any existing slots for this course (in case of editing)
      Object.keys(newSchedule[day]).forEach(timeSlot => {
        if (newSchedule[day][timeSlot]?.id === eventData.id) {
          newSchedule[day][timeSlot] = null;
        }
      });

      // Set the course in all required time slots
      for (let i = 0; i < duration; i++) {
        const slotTime = this.timeSlots[timeSlotIndex + i];
        const startTime = slotTime.split('-')[0];
        const endTime = i === duration - 1 ? 
          this.timeSlots[timeSlotIndex + i].split('-')[1] : 
          this.timeSlots[timeSlotIndex + duration - 1].split('-')[1];

        newSchedule[day][slotTime] = {
          ...eventData,
          id: eventData.id || Date.now().toString(),
          classId: this.selectedClass(),
          school: this.selectedSchool(),
          startTime: startTime,
          endTime: endTime,
          students: this.getSelectedClass()?.effectif || 0,
          room: 'Salle à définir' // Placeholder since room is not selected in form
        };
      }
      
      return newSchedule;
    });

    // Afficher un message de confirmation
    const action = this.currentEditingCourse() ? 'modifié' : 'ajouté';
    const durationText = duration > 1 ? ` (${duration}h)` : '';
    alert(`Cours "${eventData.subject}"${durationText} ${action} avec succès !`);
  }

  deleteTimeSlot(day: string, timeSlot: string) {
    const slotToDelete = this.schedule()[day][timeSlot];
    if (!slotToDelete) return;

    this.schedule.update(schedule => {
      const newSchedule = { ...schedule };
      newSchedule[day] = { ...newSchedule[day] };
      
      // Delete all slots with the same course ID (for multi-hour courses)
      Object.keys(newSchedule[day]).forEach(slot => {
        if (newSchedule[day][slot]?.id === slotToDelete.id) {
          newSchedule[day][slot] = null;
        }
      });
      
      return newSchedule;
    });
  }

  // Méthode pour dupliquer un cours
  duplicateCourse(day: string, timeSlot: string) {
    const slot = this.schedule()[day][timeSlot];
    if (!slot) return;

    // Trouver le prochain créneau libre
    const days = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
    const timeSlots = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00', '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'];
    
    let foundSlot = false;
    for (const d of days) {
      for (const t of timeSlots) {
        if (!this.schedule()[d][t]) {
          // Créneau libre trouvé
          this.schedule.update(schedule => {
            const newSchedule = { ...schedule };
            newSchedule[d] = { ...newSchedule[d] };
            newSchedule[d][t] = {
              ...slot,
              id: Date.now().toString()
            };
            return newSchedule;
          });
          
          alert(`Cours dupliqué vers ${d} ${t}`);
          foundSlot = true;
          break;
        }
      }
      if (foundSlot) break;
    }

    if (!foundSlot) {
      alert('Aucun créneau libre disponible pour la duplication.');
    }
  }

  // Statistiques
  getAdvancedStatistics() {
    const schedule = this.getFilteredSchedule();
    let totalCourses = 0;
    let totalStudents = 0;
    const coursesByType = { CM: 0, TD: 0, TP: 0, Exam: 0 };
    
    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        if (slot) {
          totalCourses++;
          totalStudents += slot.students;
          coursesByType[slot.type]++;
        }
      });
    });

    const totalSlots = Object.keys(schedule).length * this.timeSlots.length;
    const occupancyRate = Math.round((totalCourses / totalSlots) * 100);
    const averageStudentsPerCourse = totalCourses > 0 ? Math.round(totalStudents / totalCourses) : 0;

    return {
      totalCourses,
      totalStudents,
      occupancyRate,
      averageStudentsPerCourse,
      coursesByType
    };
  }

  // Actions d'export/import
  exportSchedule() {
    alert('Export de l\'emploi du temps en cours...');
  }

  printSchedule() {
    window.print();
  }

  importSchedule() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.xlsx,.xls,.ics';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        alert(`Import du fichier "${file.name}" en cours...`);
      }
    };
    input.click();
  }

  exportDetailedSchedule() {
    const schedule = this.getFilteredSchedule();
    const data = JSON.stringify(schedule, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'emploi-du-temps-detaille.json';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  showValidationReport() {
    const conflicts = this.detectConflicts();
    const stats = this.getAdvancedStatistics();
    
    let report = `=== RAPPORT DE VALIDATION ===\n\n`;
    report += `Statistiques générales:\n`;
    report += `- Total des cours: ${stats.totalCourses}\n`;
    report += `- Taux d'occupation: ${stats.occupancyRate}%\n`;
    report += `- Total étudiants: ${stats.totalStudents}\n\n`;
    
    if (conflicts.length > 0) {
      report += `Conflits détectés (${conflicts.length}):\n`;
      conflicts.forEach((conflict, i) => {
        report += `${i + 1}. ${conflict.details} (${conflict.day} ${conflict.time})\n`;
      });
    } else {
      report += `✅ Aucun conflit détecté\n`;
    }
    
    alert(report);
  }

  generateAutoSchedule() {
    alert('Génération automatique d\'emploi du temps...');
  }

  createTemplate() {
    const templateName = prompt('Nom du template:');
    if (!templateName) return;

    const template = {
      name: templateName,
      schedule: this.schedule(),
      createdAt: new Date().toISOString()
    };

    const templates = JSON.parse(localStorage.getItem('scheduleTemplates') || '[]');
    templates.push(template);
    localStorage.setItem('scheduleTemplates', JSON.stringify(templates));
    
    alert(`Template "${templateName}" créé avec succès !`);
  }

  loadTemplate() {
    const templates = JSON.parse(localStorage.getItem('scheduleTemplates') || '[]');
    
    if (templates.length === 0) {
      alert('Aucun template disponible.');
      return;
    }

    const templateList = templates.map((t: any, i: number) => 
      `${i + 1}. ${t.name} (${new Date(t.createdAt).toLocaleDateString('fr-FR')})`
    ).join('\n');

    const choice = prompt(`Choisissez un template:\n\n${templateList}\n\nEntrez le numéro:`);
    
    if (choice && !isNaN(Number(choice))) {
      const index = Number(choice) - 1;
      if (index >= 0 && index < templates.length) {
        if (confirm(`Charger le template "${templates[index].name}" ?\n\nCela remplacera l'emploi du temps actuel.`)) {
          this.schedule.set(templates[index].schedule);
          alert('Template chargé avec succès !');
        }
      }
    }
  }

  saveScheduleToFile() {
    const data = {
      schedule: this.schedule(),
      metadata: {
        exportDate: new Date().toISOString(),
        version: '1.0'
      }
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `emploi-du-temps-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  loadScheduleFromFile() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          try {
            const data = JSON.parse(e.target?.result as string);
            if (data.schedule) {
              this.schedule.set(data.schedule);
              alert('Emploi du temps chargé avec succès !');
            } else {
              alert('Format de fichier invalide.');
            }
          } catch (error) {
            alert('Erreur lors du chargement du fichier.');
          }
        };
        reader.readAsText(file);
      }
    };
    input.click();
  }

  scheduleReminder(day: string, timeSlot: string) {
    const slot = this.schedule()[day][timeSlot];
    if (!slot) return;

    const reminderTime = prompt('Dans combien de minutes voulez-vous être rappelé pour ce cours ?', '15');
    if (!reminderTime || isNaN(Number(reminderTime))) return;

    const minutes = Number(reminderTime);
    setTimeout(() => {
      alert(`🔔 Rappel: Le cours "${slot.subject}" avec ${slot.teacher} commence dans ${minutes} minutes en ${slot.room} !`);
    }, minutes * 60 * 1000);

    alert(`Rappel programmé dans ${minutes} minutes pour le cours "${slot.subject}".`);
  }
}