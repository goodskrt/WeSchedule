import { Component, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface EmailForm {
  email: string;
}

interface CodeForm {
  code: string;
}

interface ResetForm {
  newPassword: string;
  confirmPassword: string;
}

@Component({
  selector: 'app-forgot-password',
  imports: [RouterLink, CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {
  constructor(private router: Router) {}

  protected readonly currentStep = signal<'email' | 'code' | 'reset' | 'success'>('email');
  protected readonly isLoading = signal(false);
  protected readonly showPassword = signal(false);
  protected readonly showConfirmPassword = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly generatedCode = signal('');
  
  protected readonly emailForm = signal<EmailForm>({
    email: ''
  });
  
  protected readonly codeForm = signal<CodeForm>({
    code: ''
  });
  
  protected readonly resetForm = signal<ResetForm>({
    newPassword: '',
    confirmPassword: ''
  });
  
  protected readonly errors = signal<{[key: string]: string}>({});

  updateEmailForm(field: keyof EmailForm, value: string) {
    this.emailForm.update(form => ({
      ...form,
      [field]: value
    }));
    this.clearFieldError(field);
  }

  updateCodeForm(field: keyof CodeForm, value: string) {
    this.codeForm.update(form => ({
      ...form,
      [field]: value
    }));
    this.clearFieldError(field);
  }

  updateResetForm(field: keyof ResetForm, value: string) {
    this.resetForm.update(form => ({
      ...form,
      [field]: value
    }));
    this.clearFieldError(field);
  }

  private clearFieldError(field: string) {
    if (this.errors()[field]) {
      this.errors.update(errors => {
        const newErrors = { ...errors };
        delete newErrors[field];
        return newErrors;
      });
    }
    
    if (this.errorMessage()) {
      this.errorMessage.set('');
    }
  }

  // Étape 1: Saisie de l'email
  sendResetCode() {
    if (!this.validateEmailForm()) return;

    this.isLoading.set(true);
    this.errorMessage.set('');
    
    const email = this.emailForm().email;
    
    setTimeout(() => {
      // Vérifier si l'email existe (simulation)
      const registeredUsers = JSON.parse(localStorage.getItem('registeredUsers') || '[]');
      const userExists = registeredUsers.some((user: any) => user.email === email) || 
                        ['admin@iu-saintfomekong.cm', 'prof@iu-saintfomekong.cm', 'test@test.com'].includes(email);
      
      if (userExists) {
        // Générer un code de vérification
        const code = Math.floor(100000 + Math.random() * 900000).toString();
        this.generatedCode.set(code);
        
        // Simuler l'envoi d'email
        console.log(`Code de vérification envoyé à ${email}: ${code}`);
        
        this.isLoading.set(false);
        this.currentStep.set('code');
      } else {
        this.isLoading.set(false);
        this.errorMessage.set('Aucun compte associé à cette adresse email.');
      }
    }, 2000);
  }

  // Étape 2: Vérification du code
  verifyCode() {
    if (!this.validateCodeForm()) return;

    this.isLoading.set(true);
    this.errorMessage.set('');
    
    const enteredCode = this.codeForm().code;
    const correctCode = this.generatedCode();
    
    setTimeout(() => {
      if (enteredCode === correctCode) {
        this.isLoading.set(false);
        this.currentStep.set('reset');
      } else {
        this.isLoading.set(false);
        this.errorMessage.set('Code de vérification incorrect. Veuillez réessayer.');
      }
    }, 1500);
  }

  // Étape 3: Réinitialisation du mot de passe
  resetPassword() {
    if (!this.validateResetForm()) return;

    this.isLoading.set(true);
    this.errorMessage.set('');
    
    const form = this.resetForm();
    const email = this.emailForm().email;
    
    setTimeout(() => {
      // Sauvegarder le nouveau mot de passe (simulation)
      localStorage.setItem(`password_${email}`, form.newPassword);
      
      this.isLoading.set(false);
      this.currentStep.set('success');
    }, 2000);
  }

  // Validations
  validateEmailForm(): boolean {
    const form = this.emailForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.email.trim()) {
      newErrors['email'] = 'L\'email est requis';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      newErrors['email'] = 'Format d\'email invalide';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  validateCodeForm(): boolean {
    const form = this.codeForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.code.trim()) {
      newErrors['code'] = 'Le code de vérification est requis';
    } else if (form.code.length !== 6) {
      newErrors['code'] = 'Le code doit contenir 6 chiffres';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  validateResetForm(): boolean {
    const form = this.resetForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.newPassword) {
      newErrors['newPassword'] = 'Le nouveau mot de passe est requis';
    } else if (form.newPassword.length < 6) {
      newErrors['newPassword'] = 'Le mot de passe doit contenir au moins 6 caractères';
    }

    if (form.newPassword !== form.confirmPassword) {
      newErrors['confirmPassword'] = 'Les mots de passe ne correspondent pas';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  // Utilitaires
  togglePasswordVisibility(field: 'newPassword' | 'confirmPassword') {
    if (field === 'newPassword') {
      this.showPassword.update(value => !value);
    } else {
      this.showConfirmPassword.update(value => !value);
    }
  }

  isEmailFormValid(): boolean {
    const form = this.emailForm();
    return form.email.trim() !== '' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email);
  }

  isCodeFormValid(): boolean {
    const form = this.codeForm();
    return form.code.trim() !== '' && form.code.length === 6;
  }

  isResetFormValid(): boolean {
    const form = this.resetForm();
    return form.newPassword.length >= 6 && form.newPassword === form.confirmPassword;
  }

  // Navigation
  goBackToEmail() {
    this.currentStep.set('email');
    this.errorMessage.set('');
  }

  goBackToCode() {
    this.currentStep.set('code');
    this.errorMessage.set('');
  }

  resendCode() {
    this.sendResetCode();
  }
}