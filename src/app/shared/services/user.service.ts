import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { User } from '../models/auth.models';

export interface UpdateProfileRequest {
  nom: string;
  prenom: string;
  email: string;
  phone?: string;
  grade?: string; // Pour les enseignants
}

export interface ChangePasswordRequest {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
  confirmationMotDePasse: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Mettre à jour le profil utilisateur
   */
  updateProfile(profileData: UpdateProfileRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/dashboard/enseignant/profil/update`, profileData, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Changer le mot de passe
   */
  changePassword(passwordData: ChangePasswordRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/dashboard/enseignant/profil/change-password`, passwordData, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Obtenir les informations du profil utilisateur
   */
  getProfile(): Observable<User> {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      throw new Error('Utilisateur non connecté');
    }

    // Pour l'instant, retourner les informations du token
    // Plus tard, on pourra faire un appel API pour récupérer des infos à jour
    return new Observable(observer => {
      observer.next(currentUser);
      observer.complete();
    });
  }

  /**
   * Vérifier si l'utilisateur peut accéder à une fonctionnalité selon son rôle
   */
  canAccess(requiredRoles: string[]): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    
    return requiredRoles.includes(currentUser.role);
  }

  /**
   * Obtenir le nom complet de l'utilisateur
   */
  getFullName(): string {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return '';
    
    return `${currentUser.prenom} ${currentUser.nom}`;
  }

  /**
   * Obtenir l'initiale de l'utilisateur pour l'avatar
   */
  getUserInitials(): string {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return '';
    
    const firstInitial = currentUser.prenom?.charAt(0)?.toUpperCase() || '';
    const lastInitial = currentUser.nom?.charAt(0)?.toUpperCase() || '';
    
    return `${firstInitial}${lastInitial}`;
  }
}