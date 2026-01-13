import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { AddEventModalComponent } from './add-event-modal.component';
import { EditEventModalComponent } from './edit-event-modal.component';

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
  imports: [CommonModule, FormsModule, SvgIconComponent, AddEventModalComponent, EditEventModalComponent],
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
  protected readonly showAddEventModal = signal(false);
  protected readonly showEditModal = signal(false);
  protected readonly selectedTimeSlot = signal<{day: string, time: string} | null>(null);
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

  toggleConflicts() {
    this.showConflicts.update(value => !value);
  }

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

  exportSchedule() {
    // Simulation d'export
    alert('Export de l\'emploi du temps en cours...');
  }

  printSchedule() {
    window.print();
  }

  addEvent() {
    this.showAddEventModal.set(true);
  }

  closeAddEventModal() {
    this.showAddEventModal.set(false);
    this.selectedTimeSlot.set(null);
  }

  closeEditModal() {
    this.showEditModal.set(false);
    this.selectedTimeSlot.set(null);
  }

  onEventEdited(updatedEvent: TimeSlot) {
    if (this.selectedTimeSlot()) {
      const {day, time} = this.selectedTimeSlot()!;
      this.schedule.update(schedule => {
        const newSchedule = { ...schedule };
        newSchedule[day] = { ...newSchedule[day] };
        newSchedule[day][time] = {
          ...updatedEvent,
          color: this.getColorForSchool(updatedEvent.school)
        };
        return newSchedule;
      });
    }
    this.closeEditModal();
  }

  onEventDeleted() {
    if (this.selectedTimeSlot()) {
      const {day, time} = this.selectedTimeSlot()!;
      this.deleteTimeSlot(day, time);
    }
    this.closeEditModal();
  }

  onSuggestSlot(criteria: {type: string, teacher: string, room: string}) {
    const suggestion = this.suggestOptimalSlot(criteria.type, criteria.teacher, criteria.room);
    if (suggestion) {
      const message = `Créneau suggéré: ${suggestion.dayName} ${suggestion.time}\n\nVoulez-vous utiliser ce créneau?`;
      if (confirm(message)) {
        this.closeAddEventModal();
        this.selectedTimeSlot.set({day: suggestion.day, time: suggestion.time});
        this.showAddEventModal.set(true);
      }
    } else {
      alert('Aucun créneau libre trouvé pour cet enseignant et cette salle.');
    }
  }

  // Méthodes pour la recherche et les filtres
  setSearchQuery(query: string) {
    this.searchQuery.set(query);
  }

  clearSearch() {
    this.searchQuery.set('');
  }

  toggleAdvancedFilters() {
    this.showAdvancedFilters.update(value => !value);
  }

  resetAllFilters() {
    this.selectedSchool.set('all');
    this.selectedTeacher.set('all');
    this.selectedRoom.set('all');
    this.searchQuery.set('');
    this.showConflicts.set(false);
  }

  // Méthode pour obtenir les enseignants uniques dans l'emploi du temps
  getUniqueTeachers(): string[] {
    const teachers = new Set<string>();
    const schedule = this.schedule();
    
    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        if (slot) {
          teachers.add(slot.teacher);
        }
      });
    });
    
    return Array.from(teachers).sort();
  }

  // Méthode pour obtenir les salles uniques dans l'emploi du temps
  getUniqueRooms(): string[] {
    const rooms = new Set<string>();
    const schedule = this.schedule();
    
    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        if (slot) {
          rooms.add(slot.room);
        }
      });
    });
    
    return Array.from(rooms).sort();
  }

  getCurrentEditingEvent(): TimeSlot | null {
    if (this.selectedTimeSlot()) {
      const {day, time} = this.selectedTimeSlot()!;
      return this.schedule()[day][time] || null;
    }
    return null;
  }

  addEventToSlot(day: string, timeSlot: string) {
    this.selectedTimeSlot.set({day, time: timeSlot});
    this.showAddEventModal.set(true);
  }

  onEventAdded(eventData: any) {
    if (this.selectedTimeSlot()) {
      const {day, time} = this.selectedTimeSlot()!;
      
      // Vérifier les conflits avant d'ajouter
      const conflicts = this.checkConflictsForNewEvent(day, time, eventData);
      if (conflicts.length > 0) {
        const conflictMessage = conflicts.join('\n');
        if (!confirm(`Attention! Des conflits ont été détectés:\n\n${conflictMessage}\n\nVoulez-vous continuer quand même?`)) {
          return;
        }
      }
      
      const newEvent: TimeSlot = {
        id: Date.now().toString(),
        startTime: time.split('-')[0],
        endTime: time.split('-')[1],
        subject: eventData.subject,
        teacher: eventData.teacher,
        room: eventData.room,
        type: eventData.type,
        school: eventData.school,
        students: eventData.students || 30,
        color: this.getColorForSchool(eventData.school)
      };
      
      this.schedule.update(schedule => {
        const newSchedule = { ...schedule };
        newSchedule[day] = { ...newSchedule[day] };
        newSchedule[day][time] = newEvent;
        return newSchedule;
      });
    }
    this.closeAddEventModal();
  }

  checkConflictsForNewEvent(day: string, timeSlot: string, eventData: any): string[] {
    const conflicts: string[] = [];
    const schedule = this.schedule();
    
    // Vérifier les conflits d'enseignant
    for (const dayKey of Object.keys(schedule)) {
      for (const timeKey of Object.keys(schedule[dayKey])) {
        const slot = schedule[dayKey][timeKey];
        if (slot && slot.teacher === eventData.teacher && timeKey === timeSlot) {
          conflicts.push(`• L'enseignant ${eventData.teacher} a déjà cours le ${dayKey} à ${timeSlot} (${slot.subject})`);
        }
      }
    }
    
    // Vérifier les conflits de salle
    for (const dayKey of Object.keys(schedule)) {
      for (const timeKey of Object.keys(schedule[dayKey])) {
        const slot = schedule[dayKey][timeKey];
        if (slot && slot.room === eventData.room && timeKey === timeSlot) {
          conflicts.push(`• La salle ${eventData.room} est déjà occupée le ${dayKey} à ${timeSlot} (${slot.subject})`);
        }
      }
    }
    
    return conflicts;
  }

  getColorForSchool(schoolId: string): string {
    switch (schoolId) {
      case 'sji': return 'bg-blue-100 border-blue-300 text-blue-800';
      case 'sjm': return 'bg-green-100 border-green-300 text-green-800';
      case 'prepa': return 'bg-purple-100 border-purple-300 text-purple-800';
      case 'cpge': return 'bg-orange-100 border-orange-300 text-orange-800';
      default: return 'bg-gray-100 border-gray-300 text-gray-800';
    }
  }

  getWeekStats() {
    const schedule = this.getFilteredSchedule();
    let totalCourses = 0;
    let totalStudents = 0;
    const teacherSet = new Set<string>();
    const roomSet = new Set<string>();

    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        if (slot) {
          totalCourses++;
          totalStudents += slot.students;
          teacherSet.add(slot.teacher);
          roomSet.add(slot.room);
        }
      });
    });

    return {
      totalCourses,
      totalStudents,
      uniqueTeachers: teacherSet.size,
      uniqueRooms: roomSet.size
    };
  }

  editTimeSlot(day: string, timeSlot: string) {
    const slot = this.schedule()[day][timeSlot];
    if (slot) {
      // Éditer un créneau existant
      this.selectedTimeSlot.set({day, time: timeSlot});
      this.showEditModal.set(true);
    } else {
      // Ajouter un nouveau créneau
      this.addEventToSlot(day, timeSlot);
    }
  }

  deleteTimeSlot(day: string, timeSlot: string) {
    this.schedule.update(schedule => {
      const newSchedule = { ...schedule };
      newSchedule[day] = { ...newSchedule[day] };
      newSchedule[day][timeSlot] = null;
      return newSchedule;
    });
  }

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

  importSchedule() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.xlsx,.xls,.ics';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        alert(`Import du fichier "${file.name}" en cours...`);
        // Ici vous implémenteriez la logique d'import réelle
      }
    };
    input.click();
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

  generateOptimizedSchedule() {
    // Algorithme d'optimisation basique
    alert('Génération d\'un emploi du temps optimisé...');
    // Ici vous implémenteriez un algorithme d'optimisation
  }

  getScheduleStatistics() {
    const stats = this.getWeekStats();
    const schedule = this.schedule();
    
    // Calcul du taux d'occupation
    let totalSlots = 0;
    let occupiedSlots = 0;
    
    Object.values(schedule).forEach(day => {
      Object.values(day).forEach(slot => {
        totalSlots++;
        if (slot) occupiedSlots++;
      });
    });
    
    const occupancyRate = Math.round((occupiedSlots / totalSlots) * 100);
    
    return {
      ...stats,
      occupancyRate,
      totalSlots,
      occupiedSlots,
      freeSlots: totalSlots - occupiedSlots
    };
  }

  exportToCalendar() {
    // Export vers format iCal
    const schedule = this.getFilteredSchedule();
    let icalContent = 'BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Campus//Emploi du Temps//FR\n';
    
    Object.keys(schedule).forEach(day => {
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          const startTime = slot.startTime.replace(':', '');
          const endTime = slot.endTime.replace(':', '');
          
          icalContent += `BEGIN:VEVENT\n`;
          icalContent += `SUMMARY:${slot.subject}\n`;
          icalContent += `DESCRIPTION:Enseignant: ${slot.teacher}\\nSalle: ${slot.room}\\nÉtudiants: ${slot.students}\n`;
          icalContent += `LOCATION:${slot.room}\n`;
          icalContent += `DTSTART:20241216T${startTime}00\n`;
          icalContent += `DTEND:20241216T${endTime}00\n`;
          icalContent += `END:VEVENT\n`;
        }
      });
    });
    
    icalContent += 'END:VCALENDAR';
    
    const blob = new Blob([icalContent], { type: 'text/calendar' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'emploi-du-temps.ics';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  bulkEditTimeSlots(selectedSlots: Array<{day: string, time: string}>, newData: Partial<TimeSlot>) {
    this.schedule.update(schedule => {
      const newSchedule = { ...schedule };
      
      selectedSlots.forEach(({day, time}) => {
        if (newSchedule[day][time]) {
          newSchedule[day] = { ...newSchedule[day] };
          newSchedule[day][time] = { ...newSchedule[day][time]!, ...newData };
        }
      });
      
      return newSchedule;
    });
  }

  findAvailableSlots(teacher?: string, room?: string): Array<{day: string, time: string, dayName: string}> {
    const availableSlots: Array<{day: string, time: string, dayName: string}> = [];
    const schedule = this.schedule();
    
    Object.keys(schedule).forEach(day => {
      const dayName = this.days.find(d => d.key === day)?.name || day;
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        
        if (!slot) {
          // Créneau libre
          let isAvailable = true;
          
          // Vérifier si l'enseignant est libre
          if (teacher) {
            for (const checkDay of Object.keys(schedule)) {
              const checkSlot = schedule[checkDay][timeSlot];
              if (checkSlot && checkSlot.teacher === teacher) {
                isAvailable = false;
                break;
              }
            }
          }
          
          // Vérifier si la salle est libre
          if (room && isAvailable) {
            for (const checkDay of Object.keys(schedule)) {
              const checkSlot = schedule[checkDay][timeSlot];
              if (checkSlot && checkSlot.room === room) {
                isAvailable = false;
                break;
              }
            }
          }
          
          if (isAvailable) {
            availableSlots.push({day, time: timeSlot, dayName});
          }
        }
      });
    });
    
    return availableSlots;
  }

  suggestOptimalSlot(subjectType: string, teacherName: string, roomName: string): {day: string, time: string, dayName: string} | null {
    const availableSlots = this.findAvailableSlots(teacherName, roomName);
    
    if (availableSlots.length === 0) return null;
    
    // Préférences selon le type de cours
    const preferences = {
      'CM': ['08:00-09:00', '09:00-10:00', '10:00-11:00'], // Matin pour les CM
      'TD': ['13:00-14:00', '14:00-15:00', '15:00-16:00'], // Après-midi pour les TD
      'TP': ['14:00-15:00', '15:00-16:00', '16:00-17:00'], // Fin d'après-midi pour les TP
      'Exam': ['08:00-09:00', '09:00-10:00'] // Matin pour les examens
    };
    
    const preferredTimes = preferences[subjectType as keyof typeof preferences] || this.timeSlots;
    
    // Chercher le meilleur créneau selon les préférences
    for (const preferredTime of preferredTimes) {
      const slot = availableSlots.find(s => s.time === preferredTime);
      if (slot) return slot;
    }
    
    // Si aucun créneau préféré, retourner le premier disponible
    return availableSlots[0];
  }

  syncWithExternalCalendar() {
    // Synchronisation avec calendriers externes (Google Calendar, Outlook, etc.)
    alert('Synchronisation avec les calendriers externes...');
  }

  // Nouvelle fonctionnalité : Duplication de cours
  duplicateCourse(day: string, timeSlot: string) {
    const slot = this.schedule()[day][timeSlot];
    if (!slot) return;

    const availableSlots = this.findAvailableSlots(slot.teacher, slot.room);
    if (availableSlots.length === 0) {
      alert('Aucun créneau libre disponible pour dupliquer ce cours.');
      return;
    }

    const slotOptions = availableSlots.map(s => `${s.dayName} ${s.time}`).join('\n');
    const choice = prompt(`Choisissez un créneau pour dupliquer le cours "${slot.subject}":\n\n${slotOptions}\n\nEntrez le numéro (1-${availableSlots.length}):`);
    
    if (choice && !isNaN(Number(choice))) {
      const index = Number(choice) - 1;
      if (index >= 0 && index < availableSlots.length) {
        const targetSlot = availableSlots[index];
        const duplicatedCourse: TimeSlot = {
          ...slot,
          id: Date.now().toString()
        };

        this.schedule.update(schedule => {
          const newSchedule = { ...schedule };
          newSchedule[targetSlot.day] = { ...newSchedule[targetSlot.day] };
          newSchedule[targetSlot.day][targetSlot.time] = duplicatedCourse;
          return newSchedule;
        });

        alert(`Cours dupliqué avec succès le ${targetSlot.dayName} à ${targetSlot.time}`);
      }
    }
  }

  // Nouvelle fonctionnalité : Génération automatique d'emploi du temps
  generateAutoSchedule() {
    if (!confirm('Cette action va remplacer l\'emploi du temps actuel. Continuer ?')) {
      return;
    }

    const subjects = this.availableSubjects();
    const teachers = this.availableTeachers();
    const rooms = this.availableRooms();
    
    // Réinitialiser l'emploi du temps
    const newSchedule: WeekSchedule = {};
    this.days.forEach(day => {
      newSchedule[day.key] = {};
      this.timeSlots.forEach(timeSlot => {
        newSchedule[day.key][timeSlot] = null;
      });
    });

    // Algorithme de génération automatique
    let courseCount = 0;
    const maxCoursesPerWeek = 20;

    for (let i = 0; i < maxCoursesPerWeek && courseCount < subjects.length; i++) {
      const subject = subjects[i % subjects.length];
      const teacher = teachers.find(t => t.school === subject.school) || teachers[0];
      const room = this.selectOptimalRoom(subject.type, rooms);

      if (room) {
        const optimalSlot = this.findOptimalSlotForSubject(newSchedule, subject.type);
        if (optimalSlot) {
          const newCourse: TimeSlot = {
            id: `auto-${Date.now()}-${i}`,
            startTime: optimalSlot.time.split('-')[0],
            endTime: optimalSlot.time.split('-')[1],
            subject: subject.name,
            teacher: teacher.name,
            room: room.name,
            type: subject.type as 'CM' | 'TD' | 'TP' | 'Exam',
            school: subject.school,
            students: Math.min(30, room.capacity),
            color: this.getColorForSchool(subject.school)
          };

          newSchedule[optimalSlot.day][optimalSlot.time] = newCourse;
          courseCount++;
        }
      }
    }

    this.schedule.set(newSchedule);
    alert(`Emploi du temps généré automatiquement avec ${courseCount} cours.`);
  }

  private selectOptimalRoom(courseType: string, rooms: any[]): any {
    if (courseType === 'TP') {
      return rooms.find(r => r.type === 'Informatique' || r.type === 'Laboratoire') || rooms[0];
    } else if (courseType === 'CM') {
      return rooms.find(r => r.type === 'Amphithéâtre') || rooms.find(r => r.type === 'Cours') || rooms[0];
    }
    return rooms.find(r => r.type === 'Cours') || rooms[0];
  }

  private findOptimalSlotForSubject(schedule: WeekSchedule, courseType: string): {day: string, time: string} | null {
    const preferences = {
      'CM': ['08:00-09:00', '09:00-10:00', '10:00-11:00'],
      'TD': ['13:00-14:00', '14:00-15:00'],
      'TP': ['14:00-15:00', '15:00-16:00', '16:00-17:00']
    };

    const preferredTimes = preferences[courseType as keyof typeof preferences] || this.timeSlots;

    for (const time of preferredTimes) {
      for (const day of this.days) {
        if (!schedule[day.key][time]) {
          return {day: day.key, time};
        }
      }
    }

    // Si aucun créneau préféré, chercher n'importe quel créneau libre
    for (const day of this.days) {
      for (const time of this.timeSlots) {
        if (!schedule[day.key][time]) {
          return {day: day.key, time};
        }
      }
    }

    return null;
  }

  // Nouvelle fonctionnalité : Validation de l'emploi du temps
  validateSchedule(): {isValid: boolean, issues: string[]} {
    const issues: string[] = [];
    const schedule = this.schedule();

    // Vérifier les conflits d'enseignants
    const teacherConflicts = new Map<string, string[]>();
    
    Object.keys(schedule).forEach(day => {
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          const key = `${slot.teacher}-${timeSlot}`;
          if (!teacherConflicts.has(key)) {
            teacherConflicts.set(key, []);
          }
          teacherConflicts.get(key)!.push(`${day} ${timeSlot}`);
        }
      });
    });

    teacherConflicts.forEach((slots, key) => {
      if (slots.length > 1) {
        const teacher = key.split('-')[0];
        issues.push(`Conflit enseignant: ${teacher} a plusieurs cours en même temps: ${slots.join(', ')}`);
      }
    });

    // Vérifier les conflits de salles
    const roomConflicts = new Map<string, string[]>();
    
    Object.keys(schedule).forEach(day => {
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          const key = `${slot.room}-${timeSlot}`;
          if (!roomConflicts.has(key)) {
            roomConflicts.set(key, []);
          }
          roomConflicts.get(key)!.push(`${day} ${timeSlot}`);
        }
      });
    });

    roomConflicts.forEach((slots, key) => {
      if (slots.length > 1) {
        const room = key.split('-')[0];
        issues.push(`Conflit salle: ${room} est occupée plusieurs fois en même temps: ${slots.join(', ')}`);
      }
    });

    // Vérifier la capacité des salles
    Object.keys(schedule).forEach(day => {
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          const room = this.availableRooms().find(r => r.name === slot.room);
          if (room && slot.students > room.capacity) {
            issues.push(`Capacité dépassée: ${slot.subject} (${slot.students} étudiants) dans ${room.name} (capacité: ${room.capacity})`);
          }
        }
      });
    });

    return {
      isValid: issues.length === 0,
      issues
    };
  }

  // Nouvelle fonctionnalité : Rapport de validation
  showValidationReport() {
    const validation = this.validateSchedule();
    
    if (validation.isValid) {
      alert('✅ L\'emploi du temps est valide ! Aucun conflit détecté.');
    } else {
      const report = `❌ Problèmes détectés dans l'emploi du temps:\n\n${validation.issues.join('\n\n')}`;
      alert(report);
    }
  }

  // Nouvelle fonctionnalité : Statistiques avancées
  getAdvancedStatistics() {
    const schedule = this.schedule();
    const stats = {
      totalCourses: 0,
      coursesByType: { CM: 0, TD: 0, TP: 0, Exam: 0 },
      coursesBySchool: { sji: 0, sjm: 0, prepa: 0, cpge: 0 },
      teacherWorkload: new Map<string, number>(),
      roomUsage: new Map<string, number>(),
      timeSlotUsage: new Map<string, number>(),
      averageStudentsPerCourse: 0,
      totalStudents: 0,
      occupancyRate: 0
    };

    let totalSlots = 0;
    let occupiedSlots = 0;
    let totalStudentsSum = 0;

    Object.keys(schedule).forEach(day => {
      Object.keys(schedule[day]).forEach(timeSlot => {
        totalSlots++;
        const slot = schedule[day][timeSlot];
        
        if (slot) {
          occupiedSlots++;
          stats.totalCourses++;
          stats.coursesByType[slot.type]++;
          stats.coursesBySchool[slot.school as keyof typeof stats.coursesBySchool]++;
          
          // Charge de travail des enseignants
          const currentLoad = stats.teacherWorkload.get(slot.teacher) || 0;
          stats.teacherWorkload.set(slot.teacher, currentLoad + 1);
          
          // Utilisation des salles
          const currentUsage = stats.roomUsage.get(slot.room) || 0;
          stats.roomUsage.set(slot.room, currentUsage + 1);
          
          // Utilisation des créneaux horaires
          const currentTimeUsage = stats.timeSlotUsage.get(timeSlot) || 0;
          stats.timeSlotUsage.set(timeSlot, currentTimeUsage + 1);
          
          totalStudentsSum += slot.students;
        }
      });
    });

    stats.occupancyRate = Math.round((occupiedSlots / totalSlots) * 100);
    stats.averageStudentsPerCourse = stats.totalCourses > 0 ? Math.round(totalStudentsSum / stats.totalCourses) : 0;
    stats.totalStudents = totalStudentsSum;

    return stats;
  }

  // Nouvelle fonctionnalité : Export détaillé
  exportDetailedSchedule() {
    const schedule = this.getFilteredSchedule();
    const stats = this.getAdvancedStatistics();
    
    let csvContent = 'Emploi du Temps Détaillé\n\n';
    csvContent += 'Jour,Heure,Matière,Enseignant,Salle,Type,École,Étudiants\n';
    
    Object.keys(schedule).forEach(day => {
      const dayName = this.days.find(d => d.key === day)?.name || day;
      Object.keys(schedule[day]).forEach(timeSlot => {
        const slot = schedule[day][timeSlot];
        if (slot) {
          csvContent += `${dayName},${timeSlot},${slot.subject},${slot.teacher},${slot.room},${slot.type},${slot.school},${slot.students}\n`;
        }
      });
    });
    
    csvContent += '\n\nStatistiques:\n';
    csvContent += `Total des cours,${stats.totalCourses}\n`;
    csvContent += `Taux d'occupation,${stats.occupancyRate}%\n`;
    csvContent += `Moyenne étudiants/cours,${stats.averageStudentsPerCourse}\n`;
    csvContent += `Total étudiants,${stats.totalStudents}\n`;
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `emploi-du-temps-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  // Nouvelle fonctionnalité : Sauvegarde et chargement
  saveScheduleToFile() {
    const scheduleData = {
      schedule: this.schedule(),
      metadata: {
        createdAt: new Date().toISOString(),
        version: '1.0',
        totalCourses: this.getAdvancedStatistics().totalCourses,
        schools: this.schools.map(s => s.name)
      }
    };

    const jsonContent = JSON.stringify(scheduleData, null, 2);
    const blob = new Blob([jsonContent], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `emploi-du-temps-sauvegarde-${new Date().toISOString().split('T')[0]}.json`;
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
        reader.onload = (e: any) => {
          try {
            const data = JSON.parse(e.target.result);
            if (data.schedule && data.metadata) {
              if (confirm(`Charger l'emploi du temps sauvegardé le ${new Date(data.metadata.createdAt).toLocaleDateString('fr-FR')} ?\n\nCela remplacera l'emploi du temps actuel.`)) {
                this.schedule.set(data.schedule);
                alert('Emploi du temps chargé avec succès !');
              }
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

  // Nouvelle fonctionnalité : Templates d'emploi du temps
  createTemplate() {
    const templateName = prompt('Nom du template:');
    if (!templateName) return;

    const template = {
      name: templateName,
      schedule: this.schedule(),
      createdAt: new Date().toISOString()
    };

    // Sauvegarder dans le localStorage (en production, utiliser une API)
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

  // Nouvelle fonctionnalité : Notification de rappel
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
