import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AjouterCours } from '../../component/ajouter-cours/ajouter-cours';

interface Course {
  id: string;
  name: string;
  code: string;
  teacher: string;
  school: string;
  students: number;
  room: string;
  schedule: string;
  type: 'CM' | 'TD' | 'TP';
  status: 'active' | 'cancelled' | 'completed';
  favorite?: boolean;
  duration?: number;
  description?: string;
  prerequisites?: string;
}

interface School {
  id: string;
  name: string;
  abbreviation: string;
  color: string;
}

@Component({
  selector: 'app-cours',
  imports: [CommonModule, AjouterCours],
  templateUrl: './cours.html',
  styleUrl: './cours.scss',
})
export class Cours {
  protected readonly selectedView = signal<'grid' | 'list'>('grid');
  protected readonly selectedSchool = signal<string>('all');
  protected readonly selectedStatus = signal<string>('all');
  protected readonly selectedType = signal<string>('all');
  protected readonly selectedTeacher = signal<string>('all');
  protected readonly searchTerm = signal<string>('');
  protected readonly showAddModal = signal<boolean>(false);
  protected readonly selectedCourse = signal<Course | null>(null);
  protected readonly showDetailsModal = signal<boolean>(false);
  protected readonly showAdvancedFilters = signal<boolean>(false);
  protected readonly sortBy = signal<string>('name');
  protected readonly sortOrder = signal<'asc' | 'desc'>('asc');
  protected readonly selectedCourseIds = signal<string[]>([]);

  protected readonly schools = signal<School[]>([
    { id: 'sji', name: 'saint jean International', abbreviation: 'SJI', color: 'bg-blue-500' },
    { id: 'sjm', name: 'saint jean Management', abbreviation: 'SJM', color: 'bg-green-500' },
    { id: 'prepa', name: 'PrepaVogt', abbreviation: 'PV', color: 'bg-purple-500' },
    { id: 'cpge', name: 'Classes Préparatoires', abbreviation: 'CPGE', color: 'bg-orange-500' }
  ]);

  protected readonly courses = signal<Course[]>([
    {
      id: '1',
      name: 'Algorithmique et Structures de Données',
      code: 'INFO301',
      teacher: 'Dr. Martin Dubois',
      school: 'sji',
      students: 35,
      room: 'Salle 101',
      schedule: 'Lun 09:00-11:00',
      type: 'CM',
      status: 'active',
      favorite: true,
      duration: 90,
      description: 'Introduction aux algorithmes fondamentaux et structures de données',
      prerequisites: 'Programmation de base'
    },
    {
      id: '2',
      name: 'Base de Données Avancées',
      code: 'INFO302',
      teacher: 'Prof. Marie Laurent',
      school: 'sji',
      students: 28,
      room: 'Lab Info 2',
      schedule: 'Mar 14:00-16:00',
      type: 'TP',
      status: 'active',
      favorite: false,
      duration: 120,
      description: 'Conception et optimisation de bases de données',
      prerequisites: 'Base de données relationnelles'
    },
    {
      id: '3',
      name: 'Gestion Financière',
      code: 'GEST201',
      teacher: 'Dr. Paul Nguyen',
      school: 'sjm',
      students: 45,
      room: 'Amphi A',
      schedule: 'Mer 10:00-12:00',
      type: 'CM',
      status: 'active',
      favorite: false,
      duration: 120,
      description: 'Principes de gestion financière d\'entreprise',
      prerequisites: 'Comptabilité générale'
    },
    {
      id: '4',
      name: 'Mathématiques Supérieures',
      code: 'MATH101',
      teacher: 'Prof. Sophie Bernard',
      school: 'prepa',
      students: 32,
      room: 'Salle 205',
      schedule: 'Jeu 08:00-10:00',
      type: 'TD',
      status: 'active',
      favorite: true,
      duration: 120,
      description: 'Mathématiques avancées pour classes préparatoires',
      prerequisites: 'Mathématiques terminale'
    },
    {
      id: '5',
      name: 'Physique Quantique',
      code: 'PHYS301',
      teacher: 'Dr. fomekong Laurent',
      school: 'cpge',
      students: 25,
      room: 'Salle 301',
      schedule: 'Ven 14:00-16:00',
      type: 'CM',
      status: 'cancelled',
      favorite: false,
      duration: 120,
      description: 'Introduction à la mécanique quantique',
      prerequisites: 'Physique classique'
    },
    {
      id: '6',
      name: 'Programmation Web',
      code: 'INFO201',
      teacher: 'Prof. Anne Moreau',
      school: 'sji',
      students: 40,
      room: 'Lab Info 1',
      schedule: 'Lun 14:00-17:00',
      type: 'TP',
      status: 'completed',
      favorite: false,
      duration: 180,
      description: 'Développement d\'applications web modernes',
      prerequisites: 'HTML, CSS, JavaScript'
    }
  ]);

  getSchoolInfo(schoolId: string) {
    return this.schools().find(s => s.id === schoolId);
  }

  getStatusColor(status: string) {
    switch (status) {
      case 'active': return 'bg-green-100 text-green-800';
      case 'cancelled': return 'bg-red-100 text-red-800';
      case 'completed': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getTypeColor(type: string) {
    switch (type) {
      case 'CM': return 'bg-blue-100 text-blue-800';
      case 'TD': return 'bg-yellow-100 text-yellow-800';
      case 'TP': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  filteredCourses() {
    let filtered = this.courses();
    
    // Filter by school
    if (this.selectedSchool() !== 'all') {
      filtered = filtered.filter(course => course.school === this.selectedSchool());
    }
    
    // Filter by status
    if (this.selectedStatus() !== 'all') {
      filtered = filtered.filter(course => course.status === this.selectedStatus());
    }
    
    // Filter by type
    if (this.selectedType() !== 'all') {
      filtered = filtered.filter(course => course.type === this.selectedType());
    }
    
    // Filter by teacher
    if (this.selectedTeacher() !== 'all') {
      filtered = filtered.filter(course => course.teacher === this.selectedTeacher());
    }
    
    // Filter by search term
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(course => 
        course.name.toLowerCase().includes(term) ||
        course.code.toLowerCase().includes(term) ||
        course.teacher.toLowerCase().includes(term) ||
        course.description?.toLowerCase().includes(term) ||
        course.prerequisites?.toLowerCase().includes(term)
      );
    }
    
    // Sort results
    filtered = this.sortCourses(filtered);
    
    return filtered;
  }

  sortCourses(courses: Course[]): Course[] {
    const sortBy = this.sortBy();
    const order = this.sortOrder();
    
    return courses.sort((a, b) => {
      let aValue: any, bValue: any;
      
      switch (sortBy) {
        case 'name':
          aValue = a.name.toLowerCase();
          bValue = b.name.toLowerCase();
          break;
        case 'code':
          aValue = a.code.toLowerCase();
          bValue = b.code.toLowerCase();
          break;
        case 'teacher':
          aValue = a.teacher.toLowerCase();
          bValue = b.teacher.toLowerCase();
          break;
        case 'students':
          aValue = a.students;
          bValue = b.students;
          break;
        case 'school':
          aValue = this.getSchoolInfo(a.school)?.name || '';
          bValue = this.getSchoolInfo(b.school)?.name || '';
          break;
        default:
          return 0;
      }
      
      if (aValue < bValue) return order === 'asc' ? -1 : 1;
      if (aValue > bValue) return order === 'asc' ? 1 : -1;
      return 0;
    });
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

  setTypeFilter(type: string) {
    this.selectedType.set(type);
  }

  setTeacherFilter(teacher: string) {
    this.selectedTeacher.set(teacher);
  }

  setSortBy(sortBy: string) {
    if (this.sortBy() === sortBy) {
      // Toggle sort order if same field
      this.sortOrder.update(order => order === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortBy.set(sortBy);
      this.sortOrder.set('asc');
    }
  }

  toggleAdvancedFilters() {
    this.showAdvancedFilters.update(value => !value);
  }

  resetFilters() {
    this.selectedSchool.set('all');
    this.selectedStatus.set('all');
    this.selectedType.set('all');
    this.selectedTeacher.set('all');
    this.searchTerm.set('');
    this.sortBy.set('name');
    this.sortOrder.set('asc');
  }

  saveFilters() {
    const filters = {
      school: this.selectedSchool(),
      status: this.selectedStatus(),
      type: this.selectedType(),
      teacher: this.selectedTeacher(),
      search: this.searchTerm(),
      sortBy: this.sortBy(),
      sortOrder: this.sortOrder()
    };
    localStorage.setItem('courseFilters', JSON.stringify(filters));
    alert('Filtres sauvegardés avec succès !');
  }

  loadSavedFilters() {
    const saved = localStorage.getItem('courseFilters');
    if (saved) {
      const filters = JSON.parse(saved);
      this.selectedSchool.set(filters.school || 'all');
      this.selectedStatus.set(filters.status || 'all');
      this.selectedType.set(filters.type || 'all');
      this.selectedTeacher.set(filters.teacher || 'all');
      this.searchTerm.set(filters.search || '');
      this.sortBy.set(filters.sortBy || 'name');
      this.sortOrder.set(filters.sortOrder || 'asc');
    }
  }

  clearSearch() {
    this.searchTerm.set('');
  }

  updateSearchTerm(term: string) {
    this.searchTerm.set(term);
  }

  onStatusFilterChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setStatusFilter(target.value);
  }

  onTypeFilterChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setTypeFilter(target.value);
  }

  onTeacherFilterChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setTeacherFilter(target.value);
  }

  onSortByChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setSortBy(target.value);
  }

  onSearchInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateSearchTerm(target.value);
  }

  // Modal management methods
  openAddModal() {
    this.selectedCourse.set(null);
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
    this.selectedCourse.set(null);
  }

  openDetailsModal(course: Course) {
    this.selectedCourse.set(course);
    this.showDetailsModal.set(true);
  }

  closeDetailsModal() {
    this.showDetailsModal.set(false);
    this.selectedCourse.set(null);
  }

  editCourse(course: Course) {
    this.selectedCourse.set(course);
    this.showAddModal.set(true);
  }

  deleteCourse(courseId: string) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce cours ?')) {
      this.courses.update(courses => courses.filter(c => c.id !== courseId));
    }
  }

  onCourseAdded(courseData: any) {
    if (this.selectedCourse()) {
      // Update existing course
      this.courses.update(courses => 
        courses.map(c => c.id === this.selectedCourse()!.id ? { ...c, ...courseData } : c)
      );
    } else {
      // Add new course
      const newCourse: Course = {
        id: Date.now().toString(),
        ...courseData,
        students: courseData.capacity || 0,
        room: 'À assigner',
        schedule: 'À planifier',
        status: 'active'
      };
      this.courses.update(courses => [...courses, newCourse]);
    }
    this.closeAddModal();
  }

  duplicateCourse(course: Course) {
    const duplicatedCourse: Course = {
      ...course,
      id: Date.now().toString(),
      name: `${course.name} (Copie)`,
      code: `${course.code}_COPY`,
      status: 'active'
    };
    this.courses.update(courses => [...courses, duplicatedCourse]);
  }

  getActiveCourses(): Course[] {
    return this.courses().filter(c => c.status === 'active');
  }

  getCancelledCourses(): Course[] {
    return this.courses().filter(c => c.status === 'cancelled');
  }

  getCompletedCourses(): Course[] {
    return this.courses().filter(c => c.status === 'completed');
  }

  getTotalStudents(): number {
    return this.courses().reduce((total, course) => total + course.students, 0);
  }

  getCoursesByType() {
    const courses = this.courses();
    const types = ['CM', 'TD', 'TP'];
    return types.map(type => ({
      type,
      count: courses.filter(c => c.type === type).length,
      color: this.getTypeColor(type)
    }));
  }

  // New methods for additional functionality
  toggleCourseFavorite(courseId: string) {
    this.courses.update(courses =>
      courses.map(c =>
        c.id === courseId ? { ...c, favorite: !c.favorite } : c
      )
    );
  }

  getUniqueTeachers(): string[] {
    const teachers = this.courses().map(c => c.teacher);
    return [...new Set(teachers)].sort();
  }

  getTotalWeeklyHours(): number {
    return this.courses()
      .filter(c => c.status === 'active')
      .reduce((total, course) => total + (course.duration || 90), 0) / 60;
  }

  importCourses() {
    // Create file input
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.xlsx,.xls';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        // Simulate import process
        alert(`Import du fichier "${file.name}" en cours...`);
        // Here you would implement actual file parsing
      }
    };
    input.click();
  }

  exportCourses() {
    const courses = this.filteredCourses();
    const csvContent = [
      'Code,Nom,Enseignant,École,Type,Statut,Étudiants,Horaire,Salle',
      ...courses.map(c => 
        `${c.code},"${c.name}","${c.teacher}","${this.getSchoolInfo(c.school)?.name}",${c.type},${c.status},${c.students},"${c.schedule}","${c.room}"`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cours_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  bulkUpdateStatus(status: 'active' | 'cancelled' | 'completed') {
    const selectedIds = this.getSelectedCourseIds();
    if (selectedIds.length === 0) {
      alert('Veuillez sélectionner au moins un cours');
      return;
    }
    
    if (confirm(`Changer le statut de ${selectedIds.length} cours vers "${status}" ?`)) {
      this.courses.update(courses =>
        courses.map(c =>
          selectedIds.includes(c.id) ? { ...c, status } : c
        )
      );
    }
  }

  bulkDelete() {
    const selectedIds = this.getSelectedCourseIds();
    if (selectedIds.length === 0) {
      alert('Veuillez sélectionner au moins un cours');
      return;
    }
    
    if (confirm(`Supprimer définitivement ${selectedIds.length} cours ?`)) {
      this.courses.update(courses =>
        courses.filter(c => !selectedIds.includes(c.id))
      );
    }
  }

  bulkExport() {
    const selectedIds = this.getSelectedCourseIds();
    if (selectedIds.length === 0) {
      alert('Veuillez sélectionner au moins un cours');
      return;
    }
    
    const selectedCourses = this.courses().filter(c => selectedIds.includes(c.id));
    const csvContent = [
      'Code,Nom,Enseignant,École,Type,Statut,Étudiants,Horaire,Salle',
      ...selectedCourses.map(c => 
        `${c.code},"${c.name}","${c.teacher}","${this.getSchoolInfo(c.school)?.name}",${c.type},${c.status},${c.students},"${c.schedule}","${c.room}"`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cours_selection_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    
    // Clear selection after export
    this.clearSelection();
  }

  clearSelection() {
    this.selectedCourseIds.set([]);
  }

  getSelectedCourseIds(): string[] {
    return this.selectedCourseIds();
  }

  toggleCourseSelection(courseId: string) {
    this.selectedCourseIds.update(ids => {
      if (ids.includes(courseId)) {
        return ids.filter(id => id !== courseId);
      } else {
        return [...ids, courseId];
      }
    });
  }

  toggleSelectAll() {
    const filteredIds = this.filteredCourses().map(c => c.id);
    const selectedIds = this.selectedCourseIds();
    
    if (filteredIds.every(id => selectedIds.includes(id))) {
      // All filtered courses are selected, deselect all
      this.selectedCourseIds.set(selectedIds.filter(id => !filteredIds.includes(id)));
    } else {
      // Not all filtered courses are selected, select all
      const newSelection = [...new Set([...selectedIds, ...filteredIds])];
      this.selectedCourseIds.set(newSelection);
    }
  }

  isAllSelected(): boolean {
    const filteredIds = this.filteredCourses().map(c => c.id);
    const selectedIds = this.selectedCourseIds();
    return filteredIds.length > 0 && filteredIds.every(id => selectedIds.includes(id));
  }

  isCourseSelected(courseId: string): boolean {
    return this.selectedCourseIds().includes(courseId);
  }

  getCoursesStats() {
    const courses = this.courses();
    return {
      total: courses.length,
      active: courses.filter(c => c.status === 'active').length,
      cancelled: courses.filter(c => c.status === 'cancelled').length,
      completed: courses.filter(c => c.status === 'completed').length,
      favorites: courses.filter(c => c.favorite).length,
      totalStudents: courses.reduce((sum, c) => sum + c.students, 0),
      averageStudents: Math.round(courses.reduce((sum, c) => sum + c.students, 0) / courses.length),
      bySchool: this.schools().map(school => ({
        school: school.name,
        count: courses.filter(c => c.school === school.id).length
      }))
    };
  }
}
