import { Component, signal, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface TeacherForm {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  specialization: string;
  department: string;
  schools: string[];
  status: 'active' | 'inactive' | 'on-leave';
  qualifications: string[];
  joinDate: string;
}

interface School {
  id: string;
  name: string;
  abbreviation: string;
}

@Component({
  selector: 'app-ajouter-enseignant',
  imports: [CommonModule, FormsModule],
  templateUrl: './ajouter-enseignant.html',
  styleUrl: './ajouter-enseignant.scss',
})
export class AjouterEnseignant implements OnInit {
  @Input() teacher: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() teacherAdded = new EventEmitter<any>();

  protected readonly teacherForm = signal<TeacherForm>({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    specialization: '',
    department: '',
    schools: [],
    status: 'active',
    qualifications: [],
    joinDate: new Date().toISOString().split('T')[0]
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});
  protected readonly currentStep = signal(1);
  protected readonly newQualification = signal('');

  protected readonly schools: School[] = [
    { id: 'sji', name: 'saint jean International (SJI)', abbreviation: 'SJI' },
    { id: 'sjm', name: 'saint jean Management (SJM)', abbreviation: 'SJM' },
    { id: 'prepa', name: 'PrepaVogt', abbreviation: 'PV' },
    { id: 'cpge', name: 'Classes Préparatoires (CPGE)', abbreviation: 'CPGE' }
  ];

  protected readonly specializations = [
    'Informatique',
    'Mathématiques',
    'Physique',
    'Chimie',
    'Gestion',
    'Marketing',
    'Finance',
    'Ressources Humaines',
    'Sciences de l\'Ingénieur',
    'Électronique',
    'Télécommunications',
    'Génie Civil',
    'Autre'
  ];

  protected readonly departments = [
    'Sciences et Technologies',
    'Management',
    'Sciences Fondamentales',
    'Génie Civil',
    'Électronique et Télécommunications',
    'Autre'
  ];

  ngOnInit() {
    if (this.teacher) {
      this.teacherForm.set({
        firstName: this.teacher.firstName || '',
        lastName: this.teacher.lastName || '',
        email: this.teacher.email || '',
        phone: this.teacher.phone || '',
        specialization: this.teacher.specialization || '',
        department: this.teacher.department || '',
        schools: this.teacher.schools || [],
        status: this.teacher.status || 'active',
        qualifications: [...(this.teacher.qualifications || [])],
        joinDate: this.teacher.joinDate || new Date().toISOString().split('T')[0]
      });
    }
  }

  updateForm(field: keyof TeacherForm, value: any) {
    this.teacherForm.update(form => ({
      ...form,
      [field]: value
    }));
    
    // Clear error when user starts typing
    if (this.errors()[field]) {
      this.errors.update(errors => {
        const newErrors = { ...errors };
        delete newErrors[field];
        return newErrors;
      });
    }
  }

  onSchoolChange(schoolId: string, checked: boolean) {
    this.teacherForm.update(form => ({
      ...form,
      schools: checked 
        ? [...form.schools, schoolId]
        : form.schools.filter(id => id !== schoolId)
    }));
  }

  isSchoolSelected(schoolId: string): boolean {
    return this.teacherForm().schools.includes(schoolId);
  }

  addQualification() {
    const qualification = this.newQualification().trim();
    if (qualification && !this.teacherForm().qualifications.includes(qualification)) {
      this.teacherForm.update(form => ({
        ...form,
        qualifications: [...form.qualifications, qualification]
      }));
      this.newQualification.set('');
    }
  }

  removeQualification(index: number) {
    this.teacherForm.update(form => ({
      ...form,
      qualifications: form.qualifications.filter((_, i) => i !== index)
    }));
  }

  validateStep(step: number): boolean {
    const form = this.teacherForm();
    const newErrors: {[key: string]: string} = {};

    if (step === 1) {
      if (!form.firstName.trim()) newErrors['firstName'] = 'Le prénom est requis';
      if (!form.lastName.trim()) newErrors['lastName'] = 'Le nom est requis';
      if (!form.email.trim()) {
        newErrors['email'] = 'L\'email est requis';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
        newErrors['email'] = 'Format d\'email invalide';
      }
      if (!form.phone.trim()) {
        newErrors['phone'] = 'Le téléphone est requis';
      } else if (!/^[+]?[0-9\s-()]{8,}$/.test(form.phone)) {
        newErrors['phone'] = 'Format de téléphone invalide';
      }
    }

    if (step === 2) {
      if (!form.specialization) newErrors['specialization'] = 'La spécialisation est requise';
      if (!form.department) newErrors['department'] = 'Le département est requis';
      if (form.schools.length === 0) newErrors['schools'] = 'Au moins une école doit être sélectionnée';
      if (!form.joinDate) newErrors['joinDate'] = 'La date d\'embauche est requise';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  nextStep() {
    if (this.validateStep(this.currentStep())) {
      this.currentStep.update(step => Math.min(step + 1, 2));
    }
  }

  previousStep() {
    this.currentStep.update(step => Math.max(step - 1, 1));
  }

  onSubmit() {
    if (!this.validateStep(2)) return;

    this.isLoading.set(true);
    
    // Simulation d'ajout/modification d'enseignant
    setTimeout(() => {
      this.isLoading.set(false);
      const action = this.teacher ? 'modifié' : 'ajouté';
      alert(`Enseignant ${action} avec succès !`);
      this.teacherAdded.emit(this.teacherForm());
      this.resetForm();
    }, 2000);
  }

  resetForm() {
    this.teacherForm.set({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      specialization: '',
      department: '',
      schools: [],
      status: 'active',
      qualifications: [],
      joinDate: new Date().toISOString().split('T')[0]
    });
    this.errors.set({});
    this.currentStep.set(1);
    this.newQualification.set('');
  }

  onClose() {
    this.close.emit();
  }

  getModalTitle(): string {
    return this.teacher ? 'Modifier l\'Enseignant' : 'Ajouter un Nouvel Enseignant';
  }

  getSubmitButtonText(): string {
    if (this.isLoading()) {
      return this.teacher ? 'Modification en cours...' : 'Ajout en cours...';
    }
    return this.teacher ? 'Modifier l\'enseignant' : 'Ajouter l\'enseignant';
  }

  onSpecializationChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('specialization', target.value);
  }

  onDepartmentChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('department', target.value);
  }

  onStatusChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('status', target.value);
  }

  onJoinDateChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('joinDate', target.value);
  }

  onQualificationKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addQualification();
    }
  }
}