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

@Component({
  selector: 'app-emploi-de-temps',
  imports: [CommonModule, FormsModule, SvgIconComponent, ModalSchedule],
  templateUrl: './emploi-de-temps.html',
  styleUrl: './emploi-de-temps.scss',
})
export class EmploiDeTemps {
  protected readonly currentView = signal<'week' | 'month' | 'day'>('week');
  protected readonly selectedWeek = signal(new Date());
  protected readonly selectedSchool = signal<string>('all');
  protected readonly selectedTeacher = signal<string>('all');
  protected readonly selectedRoom = signal<string>('all');
  protected readonly showConflicts = signal(false);
  protected readonly showScheduleModal = signal(false);
  protected readonly isEditMode = signal(false);
  protected readonly selectedTimeSlot = signal<{day: string, time: string} | null>(null);
  protected readonly currentEditingCourse = signal<any>(null);
  protected readonly searchQuery = signal<string>('');
  protected readonly showAdvancedFilters = signal(false);

  // Liste des matières disponibles
  protected readonly availableSubjects = signal([
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

  // Liste des enseignants disponibles
  protected readonly availableTeachers = signal([
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
        subject: 'Algorithmique',
        teacher: 'Dr. Martin',
        room: 'Salle 101',
        type: 'CM',
        school: 'sji',
        students: 35,
        color: 'bg-blue-100 border-blue-300 text-blue-800'
      },
      '09:00-10:00': null,
      '10:00-11:00': {
        id: '2',
        startTime: '10:00',
        endTime: '11:00',
        subject: 'Base de Données',
        teacher: 'Prof. Dubois',
        room: 'Lab Info 2',
        type: 'TP',
        school: 'sji',
        students: 28,
        color: 'bg-purple-100 border-purple-300 text-purple-800'
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
        subject: 'Gestion Financière',
        teacher: 'Dr. Nguyen',
        room: 'Amphi A',
        type: 'CM',
        school: 'sjm',
        students: 45,
        color: 'bg-green-100 border-green-300 text-green-800'
      },
      '10:00-11:00': null,
      '11:00-12:00': null,
      '13:00-14:00': {
        id: '4',
        startTime: '13:00',
        endTime: '14:00',
        subject: 'Mathématiques',
        teacher: 'Prof. Bernard',
        room: 'Salle 205',
        type: 'TD',
        school: 'prepa',
        students: 32,
        color: 'bg-yellow-100 border-yellow-300 text-yellow-800'
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
        subject: 'Physique Quantique',
        teacher: 'Dr. Laurent',
        room: 'Salle 301',
        type: 'CM',
        school: 'cpge',
        students: 25,
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
        subject: 'Réseaux',
        teacher: 'Prof. Martin',
        room: 'Lab Réseau',
        type: 'TP',
        school: 'sji',
        students: 20,
        color: 'bg-blue-100 border-blue-300 text-blue-800'
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
        teacher: 'Dr. Patel',
        room: 'Salle 102',
        type: 'CM',
        school: 'sjm',
        students: 38,
        color: 'bg-green-100 border-green-300 text-green-800'
      },
      '11:00-12:00': null,
      '13:00-14:00': null,
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    },
    friday: {
      '08:00-09:00': {
        id: '8',
        startTime: '08:00',
        endTime: '09:00',
        subject: 'Chimie Organique',
        teacher: 'Prof. Moreau',
        room: 'Lab Chimie',
        type: 'TP',
        school: 'prepa',
        students: 24,
        color: 'bg-purple-100 border-purple-300 text-purple-800'
      },
      '09:00-10:00': null,
      '10:00-11:00': null,
      '11:00-12:00': null,
      '13:00-14:00': {
        id: '9',
        startTime: '13:00',
        endTime: '14:00',
        subject: 'Examen Final',
        teacher: 'Multiple',
        room: 'Amphi B',
        type: 'Exam',
        school: 'cpge',
        students: 50,
        color: 'bg-red-100 border-red-300 text-red-800'
      },
      '14:00-15:00': null,
      '15:00-16:00': null,
      '16:00-17:00': null
    },
    saturday: {
      '08:00-09:00': null,
      '09:00-10:00': {
        id: '10',
        startTime: '09:00',
        endTime: '10:00',
        subject: 'Conférence IA',
        teacher: 'Dr. Smith',
        room: 'Amphi A',
        type: 'CM',
        school: 'sji',
        students: 100,
        color: 'bg-indigo-100 border-indigo-300 text-indigo-800'
      },
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
      start: new Date(2024, 11, 15, 14, 0),
      end: new Date(2024, 11, 15, 16, 0),
      type: 'meeting',
      location: 'Salle de Conférence',
      description: 'Réunion mensuelle des enseignants',
      color: 'bg-blue-500'
    },
    {
      id: '2',
      title: 'Journée Portes Ouvertes',
      start: new Date(2024, 11, 20, 9, 0),
      end: new Date(2024, 11, 20, 17, 0),
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
  }

  onTeacherFilterChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.setTeacherFilter(target.value || 'all');
  }

  onRoomFilterChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.setRoomFilter(target.value || 'all');
  }

  setView(view: 'week' | 'month' | 'day') {
    this.currentView.set(view);
  }

  setSchoolFilter(school: string) {
    this.selectedSchool.set(school);
  }

  setTeacherFilter(teacher: string) {
    this.selectedTeacher.set(teacher);
  }

  setRoomFilter(room: string) {
    this.selectedRoom.set(room);
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
    this.selectedSchool.set('all');
    this.selectedTeacher.set('all');
    this.selectedRoom.set('all');
    this.searchQuery.set('');
    this.showConflicts.set(false);
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
    
    if (this.selectedSchool() === 'all' && 
        this.selectedTeacher() === 'all' && 
        this.selectedRoom() === 'all' && 
        !this.searchQuery().trim()) {
      return schedule;
    }

    const filtered: WeekSchedule = {};
    const searchTerm = this.searchQuery().toLowerCase();
    
    Object.keys(schedule).forEach(day => {
      filtered[day] = {};
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          let include = true;
          
          // Filtre par école
          if (this.selectedSchool() !== 'all' && slot.school !== this.selectedSchool()) {
            include = false;
          }
          
          // Filtre par enseignant
          if (this.selectedTeacher() !== 'all' && !slot.teacher.toLowerCase().includes(this.selectedTeacher().toLowerCase())) {
            include = false;
          }
          
          // Filtre par salle
          if (this.selectedRoom() !== 'all' && !slot.room.toLowerCase().includes(this.selectedRoom().toLowerCase())) {
            include = false;
          }

          // Filtre par recherche
          if (searchTerm && include) {
            const searchableText = `${slot.subject} ${slot.teacher} ${slot.room}`.toLowerCase();
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
        if (slot) {
          teachers.add(slot.teacher);
        }
      });
    });
    
    return Array.from(teachers).sort();
  }

  getUniqueRooms(): string[] {
    const schedule = this.schedule();
    const rooms = new Set<string>();
    
    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        if (slot) {
          rooms.add(slot.room);
        }
      });
    });
    
    return Array.from(rooms).sort();
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
    
    this.schedule.update(schedule => {
      const newSchedule = { ...schedule };
      newSchedule[day] = { ...newSchedule[day] };
      newSchedule[day][time] = {
        ...eventData,
        id: eventData.id || Date.now().toString()
      };
      return newSchedule;
    });

    // Afficher un message de confirmation
    const action = this.currentEditingCourse() ? 'modifié' : 'ajouté';
    alert(`Cours "${eventData.subject}" ${action} avec succès !`);
  }

  deleteTimeSlot(day: string, timeSlot: string) {
    this.schedule.update(schedule => {
      const newSchedule = { ...schedule };
      newSchedule[day] = { ...newSchedule[day] };
      newSchedule[day][timeSlot] = null;
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