import { Component, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface RegistrationForm {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
  role: 'teacher' | 'admin' | 'student';
  school: string;
  department: string;
  acceptTerms: boolean;
}

interface School {
  id: string;
  name: string;
  departments: string[];
}

@Component({
  selector: 'app-inscription',
  imports: [RouterLink, CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './inscription.html',
  styleUrl: './inscription.scss',
})
export class Inscription {
  constructor(private router: Router) {}

  protected readonly registrationForm = signal<RegistrationForm>({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    role: 'teacher',
    school: '',
    department: '',
    acceptTerms: false
  });

  protected readonly isLoading = signal(false);
  protected readonly showPassword = signal(false);
  protected readonly showConfirmPassword = signal(false);
  protected readonly currentStep = signal(1);
  protected readonly errors = signal<{[key: string]: string}>({});

  protected readonly schools = signal<School[]>([
    {
      id: 'sji',
      name: 'saint jean International (SJI)',
      departments: ['Informatique', 'Génie Civil', 'Électronique', 'Télécommunications']
    },
    {
      id: 'sjm',
      name: 'saint jean Management (SJM)',
      departments: ['Gestion', 'Marketing', 'Finance', 'Ressources Humaines']
    },
    {
      id: 'prepa',
      name: 'PrepaVogt',
      departments: ['Mathématiques', 'Physique', 'Chimie', 'Sciences de l\'Ingénieur']
    },
    {
      id: 'cpge',
      name: 'Classes Préparatoires (CPGE)',
      departments: ['MPSI', 'PCSI', 'ECS', 'ECE']
    }
  ]);

  onSchoolChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('school', target.value);
    this.updateForm('department', '');
  }

  onDepartmentChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('department', target.value);
  }

  updateForm(field: keyof RegistrationForm, value: any) {
    this.registrationForm.update(form => ({
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

  togglePasswordVisibility(field: 'password' | 'confirmPassword') {
    if (field === 'password') {
      this.showPassword.update(value => !value);
    } else {
      this.showConfirmPassword.update(value => !value);
    }
  }

  validateStep(step: number): boolean {
    const form = this.registrationForm();
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
      if (!form.role) newErrors['role'] = 'Le rôle est requis';
      if (!form.school) newErrors['school'] = 'L\'école est requise';
      if (!form.department) newErrors['department'] = 'Le département est requis';
    }

    if (step === 3) {
      if (!form.password) {
        newErrors['password'] = 'Le mot de passe est requis';
      } else if (form.password.length < 8) {
        newErrors['password'] = 'Le mot de passe doit contenir au moins 8 caractères';
      } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(form.password)) {
        newErrors['password'] = 'Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre';
      }
      
      if (form.password !== form.confirmPassword) {
        newErrors['confirmPassword'] = 'Les mots de passe ne correspondent pas';
      }
      
      if (!form.acceptTerms) {
        newErrors['acceptTerms'] = 'Vous devez accepter les conditions d\'utilisation';
      }
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  nextStep() {
    if (this.validateStep(this.currentStep())) {
      this.currentStep.update(step => Math.min(step + 1, 3));
    }
  }

  previousStep() {
    this.currentStep.update(step => Math.max(step - 1, 1));
  }

  getAvailableDepartments(): string[] {
    const selectedSchool = this.schools().find(s => s.id === this.registrationForm().school);
    return selectedSchool?.departments || [];
  }

  onSubmit() {
    if (!this.validateStep(3)) return;

    this.isLoading.set(true);
    
    // Simulation d'inscription avec sauvegarde des données
    setTimeout(() => {
      const form = this.registrationForm();
      
      // Sauvegarder les informations utilisateur
      const userData = {
        id: Date.now().toString(),
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        phone: form.phone,
        role: form.role,
        school: form.school,
        department: form.department,
        createdAt: new Date().toISOString()
      };
      
      // Simuler la sauvegarde en localStorage
      const existingUsers = JSON.parse(localStorage.getItem('registeredUsers') || '[]');
      existingUsers.push(userData);
      localStorage.setItem('registeredUsers', JSON.stringify(existingUsers));
      
      this.isLoading.set(false);
      
      // Afficher un message de succès et rediriger
      alert(`Inscription réussie ! Bienvenue ${form.firstName} ${form.lastName}. Vous allez recevoir un email de confirmation à ${form.email}.`);
      
      // Redirection vers la page de connexion
      this.router.navigate(['/']);
    }, 2000);
  }

  getRoleDisplayName(role: string): string {
    switch (role) {
      case 'teacher': return 'Enseignant';
      case 'admin': return 'Administrateur';
      case 'student': return 'Étudiant';
      default: return role;
    }
  }
}
