import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface CourseForm {
  name: string;
  code: string;
  teacher: string;
  school: string;
  department: string;
  type: 'CM' | 'TD' | 'TP';
  duration: number;
  capacity: number;
  description: string;
  prerequisites: string;
}

@Component({
  selector: 'app-ajouter-cours',
  imports: [CommonModule, FormsModule],
  templateUrl: './ajouter-cours.html',
  styleUrl: './ajouter-cours.scss',
})
export class AjouterCours implements OnInit, OnDestroy {
  @Input() course: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() courseAdded = new EventEmitter<any>();
  
  protected readonly currentStep = signal<1 | 2>(1);
  protected readonly courseForm = signal<CourseForm>({
    name: '',
    code: '',
    teacher: '',
    school: '',
    department: '',
    type: 'CM',
    duration: 90,
    capacity: 30,
    description: '',
    prerequisites: ''
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});

  protected readonly schools = [
    { id: 'sji', name: 'Saint Jean Ingénieur (SJI)' },
    { id: 'sjm', name: 'saint jean Management (SJM)' },
    { id: 'prepa', name: 'PrepaVogt' },
    { id: 'cpge', name: 'Classes Préparatoires (CPGE)' }
  ];

  protected readonly teachers = [
    'Dr. Martin Dubois',
    'Prof. Marie Laurent',
    'Dr. Paul Nguyen',
    'Prof. Sophie Bernard',
    'Dr. fomekong Laurent',
    'Prof. Anne Moreau'
  ];

  ngOnInit() {
    if (this.course) {
      this.courseForm.set({
        name: this.course.name || '',
        code: this.course.code || '',
        teacher: this.course.teacher || '',
        school: this.course.school || '',
        department: this.course.department || '',
        type: this.course.type || 'CM',
        duration: this.course.duration || 90,
        capacity: this.course.capacity || 30,
        description: this.course.description || '',
        prerequisites: this.course.prerequisites || ''
      });
    }
    
    // Bloquer le scroll de la page principale
    document.body.classList.add('modal-open');
  }

  ngOnDestroy() {
    // Restaurer le scroll de la page principale
    document.body.classList.remove('modal-open');
  }

  updateForm(field: keyof CourseForm, value: any) {
    this.courseForm.update(form => ({
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

  onTeacherChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('teacher', target.value);
  }

  onSchoolChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('school', target.value);
  }

  onDescriptionChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('description', target.value);
  }

  onNameInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('name', target.value);
  }

  onCodeInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('code', target.value);
  }

  onDepartmentInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('department', target.value);
  }

  onDurationInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('duration', +target.value);
  }

  onCapacityInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('capacity', +target.value);
  }

  onPrerequisitesInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('prerequisites', target.value);
  }

  validateStep1(): boolean {
    const form = this.courseForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.name.trim()) newErrors['name'] = 'Le nom du cours est requis';
    if (!form.code.trim()) newErrors['code'] = 'Le code du cours est requis';
    if (!form.teacher) newErrors['teacher'] = 'L\'enseignant est requis';
    if (!form.school) newErrors['school'] = 'L\'école est requise';
    if (!form.department.trim()) newErrors['department'] = 'Le département est requis';

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  validateStep2(): boolean {
    const form = this.courseForm();
    const newErrors: {[key: string]: string} = {};

    if (form.capacity < 1) newErrors['capacity'] = 'La capacité doit être supérieure à 0';
    if (form.duration < 30) newErrors['duration'] = 'La durée doit être d\'au moins 30 minutes';

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  nextStep() {
    if (this.validateStep1()) {
      this.currentStep.set(2);
    }
  }

  previousStep() {
    this.currentStep.set(1);
  }

  onSubmit() {
    if (!this.validateStep2()) return;

    this.isLoading.set(true);
    
    // Simulation d'ajout de cours
    setTimeout(() => {
      this.isLoading.set(false);
      const action = this.course ? 'modifié' : 'ajouté';
      alert(`Cours ${action} avec succès !`);
      this.courseAdded.emit(this.courseForm());
      this.resetForm();
    }, 2000);
  }

  onClose() {
    // Restaurer le scroll avant de fermer
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  getModalTitle(): string {
    const baseTitle = this.course ? 'Modifier le Cours' : 'Ajouter un Nouveau Cours';
    return `${baseTitle} - Étape ${this.currentStep()}/2`;
  }

  getStepTitle(): string {
    return this.currentStep() === 1 ? 'Informations de base' : 'Détails et paramètres';
  }

  getSubmitButtonText(): string {
    if (this.isLoading()) {
      return this.course ? 'Modification en cours...' : 'Ajout en cours...';
    }
    return this.course ? 'Modifier le cours' : 'Ajouter le cours';
  }

  isStep1Valid(): boolean {
    const form = this.courseForm();
    return form.name.trim() !== '' && 
           form.code.trim() !== '' && 
           form.teacher !== '' && 
           form.school !== '' && 
           form.department.trim() !== '';
  }

  resetForm() {
    this.courseForm.set({
      name: '',
      code: '',
      teacher: '',
      school: '',
      department: '',
      type: 'CM',
      duration: 90,
      capacity: 30,
      description: '',
      prerequisites: ''
    });
    this.errors.set({});
    this.currentStep.set(1);
  }
}
