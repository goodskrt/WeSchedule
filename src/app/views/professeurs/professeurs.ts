import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AjouterEnseignant } from '../../component/ajouter-enseignant/ajouter-enseignant';
import { ProfesseurService, Teacher } from '../../shared/services/professeur.service';

interface School {
  id: string;
  name: string;
  abbreviation: string;
  color: string;
}

@Component({
  selector: 'app-professeurs',
  imports: [CommonModule, AjouterEnseignant],
  templateUrl: './professeurs.html',
  styleUrl: './professeurs.scss',
})
export class Professeurs implements OnInit {
  constructor(private professeurService: ProfesseurService) {}
  protected readonly selectedView = signal<'grid' | 'list'>('grid');
  protected readonly selectedSchool = signal<string>('all');
  protected readonly selectedStatus = signal<string>('all');
  protected readonly searchTerm = signal<string>('');
  protected readonly showAddModal = signal<boolean>(false);
  protected readonly selectedTeacher = signal<Teacher | null>(null);
  protected readonly showDetailsModal = signal<boolean>(false);

  protected readonly schools = signal<School[]>([
    { id: 'sji', name: 'Saint Jean Ingénieur', abbreviation: 'SJI', color: 'bg-blue-500' },
    { id: 'sjm', name: 'saint jean Management', abbreviation: 'SJM', color: 'bg-green-500' },
    { id: 'prepa', name: 'PrepaVogt', abbreviation: 'PV', color: 'bg-purple-500' },
    { id: 'cpge', name: 'Classes Préparatoires', abbreviation: 'CPGE', color: 'bg-orange-500' }
  ]);

  protected readonly teachers = signal<Teacher[]>([]);
  protected readonly isLoading = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit() {
    this.loadTeachers();
  }

  private loadTeachers() {
    this.isLoading.set(true);
    this.error.set(null);
    
    this.professeurService.getAllProfesseurs().subscribe({
      next: (teachers) => {
        this.teachers.set(teachers);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des professeurs:', err);
        this.error.set('Impossible de charger les professeurs. Veuillez réessayer.');
        this.isLoading.set(false);
      }
    });
  }

  getSchoolInfo(schoolId: string) {
    return this.schools().find(s => s.id === schoolId);
  }

  getStatusInfo(status: string) {
    switch (status) {
      case 'active': return { name: 'Actif', color: 'bg-green-100 text-green-800' };
      case 'inactive': return { name: 'Inactif', color: 'bg-gray-100 text-gray-800' };
      case 'on-leave': return { name: 'En congé', color: 'bg-yellow-100 text-yellow-800' };
      default: return { name: 'Inconnu', color: 'bg-gray-100 text-gray-800' };
    }
  }

  getTeacherSchools(teacher: Teacher) {
    return teacher.schools.map(schoolId => this.getSchoolInfo(schoolId)).filter(Boolean);
  }

  filteredTeachers() {
    let filtered = this.teachers();
    
    if (this.selectedSchool() !== 'all') {
      filtered = filtered.filter(teacher => teacher.schools.includes(this.selectedSchool()));
    }
    
    if (this.selectedStatus() !== 'all') {
      filtered = filtered.filter(teacher => teacher.status === this.selectedStatus());
    }
    
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(teacher => 
        teacher.firstName.toLowerCase().includes(term) ||
        teacher.lastName.toLowerCase().includes(term) ||
        teacher.email.toLowerCase().includes(term) ||
        teacher.specialization.toLowerCase().includes(term) ||
        teacher.department.toLowerCase().includes(term)
      );
    }
    
    return filtered;
  }

  setView(view: 'grid' | 'list') {
    this.selectedView.set(view);
  }

  setSchoolFilter(schoolId: string) {
    this.selectedSchool.set(schoolId);
  }

  setStatusFilter(status: string) {
    this.selectedStatus.set(status);
  }

  updateSearchTerm(term: string) {
    this.searchTerm.set(term);
  }

  openAddModal() {
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
  }

  openDetailsModal(teacher: Teacher) {
    this.selectedTeacher.set(teacher);
    this.showDetailsModal.set(true);
  }

  closeDetailsModal() {
    this.showDetailsModal.set(false);
    this.selectedTeacher.set(null);
  }

  editTeacher(teacher: Teacher) {
    this.selectedTeacher.set(teacher);
    this.showAddModal.set(true);
  }

  deleteTeacher(teacherId: string) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce professeur ?')) {
      this.isLoading.set(true);
      this.professeurService.deleteProfesseur(teacherId).subscribe({
        next: () => {
          this.teachers.update(teachers => teachers.filter(t => t.id !== teacherId));
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          alert('Erreur lors de la suppression du professeur');
          this.isLoading.set(false);
        }
      });
    }
  }

  getActiveTeachersCount(): number {
    return this.teachers().filter(t => t.status === 'active').length;
  }

  getInactiveTeachersCount(): number {
    return this.teachers().filter(t => t.status === 'inactive').length;
  }

  getOnLeaveTeachersCount(): number {
    return this.teachers().filter(t => t.status === 'on-leave').length;
  }

  getTotalStudents(): number {
    return this.teachers().reduce((total, teacher) => total + teacher.totalStudents, 0);
  }

  getAverageWeeklyHours(): number {
    const activeTeachers = this.teachers().filter(t => t.status === 'active');
    if (activeTeachers.length === 0) return 0;
    const totalHours = activeTeachers.reduce((total, teacher) => total + teacher.weeklyHours, 0);
    return Math.round(totalHours / activeTeachers.length);
  }

  getFullName(teacher: Teacher): string {
    return `${teacher.firstName} ${teacher.lastName}`;
  }

  getInitials(teacher: Teacher): string {
    return `${teacher.firstName.charAt(0)}${teacher.lastName.charAt(0)}`;
  }

  importTeachers() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.xlsx,.xls';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        alert(`Import du fichier "${file.name}" en cours...`);
        // Ici vous implémenteriez la logique d'import réelle
      }
    };
    input.click();
  }

  exportTeachers() {
    const teachers = this.filteredTeachers();
    const csvContent = [
      'Prénom,Nom,Email,Téléphone,Spécialisation,Département,Statut,Écoles,Cours,Étudiants,Heures/Semaine',
      ...teachers.map(t => 
        `"${t.firstName}","${t.lastName}","${t.email}","${t.phone}","${t.specialization}","${t.department}","${t.status}","${t.schools.join(';')}","${t.courses.join(';')}",${t.totalStudents},${t.weeklyHours}`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `professeurs_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  onSearchInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateSearchTerm(target.value);
  }
}