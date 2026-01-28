import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';
import { AuthService } from '../../shared/services/auth.service';

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
    private route: ActivatedRoute,
    private authService: AuthService,
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
  protected readonly returnUrl = signal('');

  ngOnInit() {
    // Vérifier si l'utilisateur est déjà connecté
    if (this.authService.isLoggedIn()) {
      this.authService.redirectAfterLogin();
      return;
    }

    // Récupérer l'URL de retour
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/app/dashboard';
    this.returnUrl.set(returnUrl);

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
    
    // Authentification via le service
    this.authService.login({
      email: form.email,
      password: form.password
    }).subscribe({
      next: (response) => {
        console.log('Login successful:', response);
        
        // Sauvegarder les informations si "Se souvenir de moi" est coché
        if (this.isBrowser) {
          if (form.rememberMe) {
            localStorage.setItem('rememberedEmail', form.email);
          } else {
            localStorage.removeItem('rememberedEmail');
          }
        }
        
        this.isLoading.set(false);
        
        // Redirection selon le rôle ou vers l'URL de retour
        const returnUrl = this.returnUrl();
        if (returnUrl && returnUrl !== '/app/dashboard') {
          this.router.navigate([returnUrl]);
        } else {
          this.authService.redirectAfterLogin();
        }
      },
      error: (error) => {
        console.error('Login error:', error);
        this.isLoading.set(false);
        
        // Gestion des erreurs
        if (error.status === 401) {
          this.errorMessage.set('Email ou mot de passe incorrect.');
        } else if (error.status === 403) {
          this.errorMessage.set('Accès refusé. Contactez l\'administrateur.');
        } else if (error.status === 0) {
          this.errorMessage.set('Impossible de se connecter au serveur. Vérifiez votre connexion.');
        } else {
          this.errorMessage.set(error.error?.message || 'Une erreur est survenue lors de la connexion.');
        }
      }
    });
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
