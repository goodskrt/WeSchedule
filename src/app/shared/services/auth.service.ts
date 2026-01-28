import { Injectable, Inject, PLATFORM_ID, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';
import { NotificationService } from './notification.service';
import { 
  LoginRequest, 
  AuthResponse, 
  User, 
  RegisterRequest, 
  ForgotPasswordRequest, 
  ResetPasswordRequest 
} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isBrowser: boolean;
  private apiUrl = environment.apiUrl;
  
  // Signals pour l'état d'authentification
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  // Signals Angular 17+
  public readonly isAuthenticated = signal(false);
  public readonly currentUser = signal<User | null>(null);
  public readonly isLoading = signal(false);

  constructor(
    private http: HttpClient,
    private router: Router,
    private tokenService: TokenService,
    private notificationService: NotificationService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    this.initializeAuthState();
  }

  /**
   * Initialise l'état d'authentification au démarrage
   */
  private initializeAuthState(): void {
    if (this.isBrowser) {
      const token = this.tokenService.getToken();
      if (token && this.tokenService.isValidToken()) {
        const payload = this.tokenService.getTokenPayload(token);
        if (payload) {
          const user: User = {
            idUser: payload.sub,
            email: payload.email,
            nom: payload.nom,
            prenom: payload.prenom,
            role: payload.role,
            phone: payload.phone,
            grade: payload.grade
          };
          this.setCurrentUser(user);
        }
      }
    }
  }

  /**
   * Connexion utilisateur
   */
  login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    this.isLoading.set(true);
    
    const loginRequest: LoginRequest = {
      email: credentials.email,
      motDePasse: credentials.password
    };

    return this.http.post<AuthResponse>(`${this.apiUrl}/api/auth/login`, loginRequest)
      .pipe(
        tap(response => {
          if (response.success && response.token) {
            // Sauvegarder le token
            this.tokenService.setToken(response.token);
            
            // Créer l'objet utilisateur
            const user: User = {
              idUser: response.idUser,
              email: response.email,
              nom: response.nom,
              prenom: response.prenom,
              role: response.role,
              phone: response.phone || undefined,
              grade: response.grade || undefined
            };
            
            this.setCurrentUser(user);
          }
          this.isLoading.set(false);
        }),
        catchError(error => {
          this.isLoading.set(false);
          console.error('Erreur de connexion:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Connexion via formulaire Spring Security (fallback)
   */
  loginWithForm(credentials: { email: string; password: string }): Observable<any> {
    this.isLoading.set(true);
    
    const formData = new FormData();
    formData.append('username', credentials.email);
    formData.append('password', credentials.password);

    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });

    return this.http.post(`${this.apiUrl}/login`, formData, { 
      headers,
      observe: 'response',
      responseType: 'text'
    }).pipe(
      tap(response => {
        // Si la connexion réussit, rediriger vers le dashboard
        if (response.status === 200) {
          this.router.navigate(['/app/dashboard']);
        }
        this.isLoading.set(false);
      }),
      catchError(error => {
        this.isLoading.set(false);
        return throwError(() => error);
      })
    );
  }

  /**
   * Inscription (pour les administrateurs et enseignants uniquement)
   */
  register(registerData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/api/register`, registerData)
      .pipe(
        catchError(error => {
          console.error('Erreur d\'inscription:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Mot de passe oublié
   */
  forgotPassword(email: string): Observable<any> {
    const request: ForgotPasswordRequest = { email };
    return this.http.post(`${this.apiUrl}/api/forgot-password`, request)
      .pipe(
        catchError(error => {
          console.error('Erreur mot de passe oublié:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Réinitialisation du mot de passe avec token
   */
  resetPassword(resetData: ResetPasswordRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/reset-password-with-token`, resetData)
      .pipe(
        catchError(error => {
          console.error('Erreur réinitialisation mot de passe:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Déconnexion
   */
  logout(): void {
    this.tokenService.removeToken();
    this.setCurrentUser(null);
    
    if (this.isBrowser) {
      localStorage.removeItem('rememberedEmail');
    }
    
    this.router.navigate(['/connexion']);
  }

  /**
   * Vérifier si l'utilisateur est connecté
   */
  isLoggedIn(): boolean {
    return this.isAuthenticated();
  }

  /**
   * Obtenir l'utilisateur actuel
   */
  getCurrentUser(): User | null {
    return this.currentUser();
  }

  /**
   * Vérifier le rôle de l'utilisateur
   */
  hasRole(role: 'ADMINISTRATEUR' | 'ENSEIGNANT' | 'ETUDIANT'): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  /**
   * Vérifier si l'utilisateur est administrateur
   */
  isAdmin(): boolean {
    return this.hasRole('ADMINISTRATEUR');
  }

  /**
   * Vérifier si l'utilisateur est enseignant
   */
  isTeacher(): boolean {
    return this.hasRole('ENSEIGNANT');
  }

  /**
   * Vérifier si l'utilisateur est étudiant
   */
  isStudent(): boolean {
    return this.hasRole('ETUDIANT');
  }

  /**
   * Obtenir les headers d'authentification
   */
  getAuthHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });
    }
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  /**
   * Définir l'utilisateur actuel
   */
  private setCurrentUser(user: User | null): void {
    this.currentUser.set(user);
    this.isAuthenticated.set(user !== null);
    this.currentUserSubject.next(user);
  }

  /**
   * Redirection selon le rôle après connexion
   */
  redirectAfterLogin(): void {
    const user = this.getCurrentUser();
    if (!user) return;

    switch (user.role) {
      case 'ADMINISTRATEUR':
        this.router.navigate(['/app/dashboard']);
        break;
      case 'ENSEIGNANT':
        this.router.navigate(['/app/enseignant/dashboard']);
        break;
      case 'ETUDIANT':
        this.router.navigate(['/app/dashboard']);
        break;
      default:
        this.router.navigate(['/app/dashboard']);
    }
  }
}