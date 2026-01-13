import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface LoginForm {
  email: string;
  password: string;
  rememberMe: boolean;
}

@Component({
  selector: 'app-connexion',
  imports: [RouterLink, CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './connexion.html',
  styleUrl: './connexion.scss',
})
export class Connexion implements OnInit {
  private isBrowser: boolean;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  protected readonly loginForm = signal<LoginForm>({
    email: '',
    password: '',
    rememberMe: false
  });

  protected readonly isLoading = signal(false);
  protected readonly showPassword = signal(false);
  protected readonly errorMessage = signal('');

  ngOnInit() {
    // Charger l'email sauvegardé si disponible (côté client uniquement)
    if (this.isBrowser) {
      const rememberedEmail = localStorage.getItem('rememberedEmail');
      if (rememberedEmail) {
        this.updateForm('email', rememberedEmail);
        this.updateForm('rememberMe', true);
      }
    }
  }

  onSubmit() {
    console.log('Form submitted:', this.loginForm());
    
    if (!this.isFormValid()) {
      this.errorMessage.set('Veuillez remplir tous les champs requis.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    
    const form = this.loginForm();
    
    // Simulation simple de connexion
    setTimeout(() => {
      console.log('Login successful');
      
      // Sauvegarder les informations si "Se souvenir de moi" est coché
      if (this.isBrowser) {
        if (form.rememberMe) {
          localStorage.setItem('rememberedEmail', form.email);
        } else {
          localStorage.removeItem('rememberedEmail');
        }
      }
      
      this.isLoading.set(false);
      
      // Redirection vers le dashboard
      console.log('Redirecting to dashboard...');
      this.router.navigate(['/app/dashboard']).then(success => {
        console.log('Navigation success:', success);
      }).catch(error => {
        console.error('Navigation error:', error);
      });
    }, 1000);
  }

  togglePasswordVisibility() {
    this.showPassword.update(value => !value);
  }

  updateForm(field: keyof LoginForm, value: any) {
    this.loginForm.update(form => ({
      ...form,
      [field]: value
    }));
    
    // Effacer le message d'erreur quand l'utilisateur tape
    if (this.errorMessage()) {
      this.errorMessage.set('');
    }
  }

  isFormValid(): boolean {
    const form = this.loginForm();
    return form.email.trim() !== '' && 
           form.password.trim() !== '' && 
           this.isValidEmail(form.email);
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}
