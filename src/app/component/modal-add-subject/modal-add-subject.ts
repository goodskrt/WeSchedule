import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface SubjectForm {
  name: string;
  code: string;
  department: string;
  school: string;
  type: 'CM' | 'TD' | 'TP' | 'Exam';
  credits: number;
  semester: number;
  description: string;
  objectives: string;
  prerequisites: string;
  evaluation: string;
  resources: string;
}

@Component({
  selector: 'app-modal-add-subject',
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-add-subject.html',
  styleUrl: './modal-add-subject.scss',
})
export class ModalAddSubject implements OnInit, OnDestroy {
  @Input() subject: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() subjectAdded = new EventEmitter<any>();
  @Output() subjectUpdated = new EventEmitter<any>();
  
  protected readonly currentStep = signal<1 | 2>(1);
  protected readonly subjectForm = signal<SubjectForm>({
    name: '',
    code: '',
    department: '',
    school: '',
    type: 'CM',
    credits: 3,
    semester: 1,
    description: '',
    objectives: '',
    prerequisites: '',
    evaluation: '',
    resources: ''
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});

  protected readonly schools = [
    { id: 'sji', name: 'Saint Jean International (SJI)' },
    { id: 'sjm', name: 'Saint Jean Management (SJM)' },
    { id: 'prepa', name: 'PrepaVogt' },
    { id: 'cpge', name: 'Classes Préparatoires (CPGE)' }
  ];

  protected readonly departments = [
    'Informatique',
    'Mathématiques',
    'Physique',
    'Chimie',
    'Gestion',
    'Marketing',
    'Finance',
    'Économie',
    'Droit',
    'Langues'
  ];

  protected readonly courseTypes = [
    { value: 'CM', label: 'Cours Magistral', description: 'Enseignement théorique en amphithéâtre' },
    { value: 'TD', label: 'Travaux Dirigés', description: 'Exercices pratiques en petits groupes' },
    { value: 'TP', label: 'Travaux Pratiques', description: 'Manipulation et expérimentation' },
    { value: 'Exam', label: 'Examen', description: 'Évaluation des connaissances' }
  ];

  ngOnInit() {
    if (this.subject) {
      this.subjectForm.set({
        name: this.subject.name || '',
        code: this.subject.code || '',
        department: this.subject.department || '',
        school: this.subject.school || '',
        type: this.subject.type || 'CM',
        credits: this.subject.credits || 3,
        semester: this.subject.semester || 1,
        description: this.subject.description || '',
        objectives: this.subject.objectives || '',
        prerequisites: this.subject.prerequisites || '',
        evaluation: this.subject.evaluation || '',
        resources: this.subject.resources || ''
      });
    }
    
    // Bloquer le scroll de la page principale
    document.body.classList.add('modal-open');
  }

  ngOnDestroy() {
    // Restaurer le scroll de la page principale
    document.body.classList.remove('modal-open');
  }

  updateForm(field: keyof SubjectForm, value: any) {
    this.subjectForm.update(form => ({
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

  onNameInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('name', target.value);
    
    // Auto-generate code if empty
    if (!this.subjectForm().code) {
      const code = this.generateCode(target.value);
      this.updateForm('code', code);
    }
  }

  onCodeInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('code', target.value.toUpperCase());
  }

  onDepartmentChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('department', target.value);
  }

  onSchoolChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('school', target.value);
  }

  onTypeChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('type', target.value);
  }

  onCreditsInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('credits', +target.value);
  }

  onSemesterInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateForm('semester', +target.value);
  }

  onDescriptionChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('description', target.value);
  }

  onObjectivesChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('objectives', target.value);
  }

  onPrerequisitesChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('prerequisites', target.value);
  }

  onEvaluationChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('evaluation', target.value);
  }

  onResourcesChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('resources', target.value);
  }

  generateCode(name: string): string {
    if (!name) return '';
    
    const words = name.trim().split(' ');
    let code = '';
    
    if (words.length === 1) {
      code = words[0].substring(0, 3).toUpperCase();
    } else {
      code = words.map(word => word.charAt(0)).join('').toUpperCase();
    }
    
    // Add random number
    const randomNum = Math.floor(Math.random() * 900) + 100;
    return `${code}${randomNum}`;
  }

  validateStep1(): boolean {
    const form = this.subjectForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.name.trim()) {
      newErrors['name'] = 'Le nom de la matière est requis';
    } else if (form.name.trim().length < 3) {
      newErrors['name'] = 'Le nom doit contenir au moins 3 caractères';
    }

    if (!form.code.trim()) {
      newErrors['code'] = 'Le code de la matière est requis';
    } else if (!/^[A-Z]{2,4}\d{3}$/.test(form.code)) {
      newErrors['code'] = 'Format invalide (ex: INFO301, MATH101)';
    }

    if (!form.department) {
      newErrors['department'] = 'Le département est requis';
    }

    if (!form.school) {
      newErrors['school'] = 'L\'école est requise';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  validateStep2(): boolean {
    const form = this.subjectForm();
    const newErrors: {[key: string]: string} = {};

    if (form.credits < 1 || form.credits > 10) {
      newErrors['credits'] = 'Les crédits doivent être entre 1 et 10';
    }

    if (form.semester < 1 || form.semester > 8) {
      newErrors['semester'] = 'Le semestre doit être entre 1 et 8';
    }

    if (!form.description.trim()) {
      newErrors['description'] = 'La description est requise';
    } else if (form.description.trim().length < 20) {
      newErrors['description'] = 'La description doit contenir au moins 20 caractères';
    }

    if (!form.objectives.trim()) {
      newErrors['objectives'] = 'Les objectifs sont requis';
    }

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
    
    // Simulation d'ajout/modification de matière
    setTimeout(() => {
      this.isLoading.set(false);
      
      const subjectData = {
        id: this.subject?.id || Date.now().toString(),
        ...this.subjectForm(),
        createdAt: this.subject?.createdAt || new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      if (this.subject) {
        this.subjectUpdated.emit(subjectData);
      } else {
        this.subjectAdded.emit(subjectData);
      }
      
      this.onClose();
    }, 1500);
  }

  onClose() {
    // Restaurer le scroll avant de fermer
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  getModalTitle(): string {
    const baseTitle = this.subject ? 'Modifier la Matière' : 'Ajouter une Nouvelle Matière';
    return `${baseTitle} - Étape ${this.currentStep()}/2`;
  }

  getStepTitle(): string {
    return this.currentStep() === 1 ? 'Informations générales' : 'Détails pédagogiques';
  }

  getSubmitButtonText(): string {
    if (this.isLoading()) {
      return this.subject ? 'Modification...' : 'Ajout en cours...';
    }
    return this.subject ? 'Modifier la matière' : 'Ajouter la matière';
  }

  isStep1Valid(): boolean {
    const form = this.subjectForm();
    return form.name.trim() !== '' && 
           form.code.trim() !== '' && 
           form.department !== '' && 
           form.school !== '' &&
           /^[A-Z]{2,4}\d{3}$/.test(form.code);
  }

  resetForm() {
    this.subjectForm.set({
      name: '',
      code: '',
      department: '',
      school: '',
      type: 'CM',
      credits: 3,
      semester: 1,
      description: '',
      objectives: '',
      prerequisites: '',
      evaluation: '',
      resources: ''
    });
    this.errors.set({});
    this.currentStep.set(1);
  }

  getTypeDescription(type: string): string {
    const typeInfo = this.courseTypes.find(t => t.value === type);
    return typeInfo?.description || '';
  }
}