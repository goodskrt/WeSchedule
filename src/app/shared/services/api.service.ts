import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // ==================== ENSEIGNANTS ====================

  /**
   * Récupérer tous les enseignants (Admin uniquement)
   */
  getAllEnseignants(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/admin/enseignants`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Récupérer un enseignant par ID
   */
  getEnseignantById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/api/admin/enseignants/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Créer un nouvel enseignant
   */
  createEnseignant(enseignant: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/admin/enseignants`, enseignant, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Mettre à jour un enseignant
   */
  updateEnseignant(id: string, enseignant: any): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.apiUrl}/api/admin/enseignants/${id}`, enseignant, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Supprimer un enseignant
   */
  deleteEnseignant(id: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/api/admin/enseignants/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Réinitialiser le mot de passe d'un enseignant
   */
  resetEnseignantPassword(id: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/admin/enseignants/${id}/reset-password`, {}, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // ==================== UES ====================

  /**
   * Récupérer toutes les UEs
   */
  getAllUEs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/admin/ues`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // ==================== DISPONIBILITÉS ====================

  /**
   * Récupérer les disponibilités d'un enseignant
   */
  getEnseignantDisponibilites(enseignantId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/admin/enseignants/${enseignantId}/disponibilites`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Créer une nouvelle disponibilité
   */
  createDisponibilite(disponibilite: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/create`, disponibilite, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Mettre à jour une disponibilité
   */
  updateDisponibilite(id: string, disponibilite: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/${id}/update`, disponibilite, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Supprimer une disponibilité
   */
  deleteDisponibilite(id: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Ajouter un créneau à une disponibilité
   */
  addCreneauToDisponibilite(disponibiliteId: string, creneau: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/${disponibiliteId}/creneaux`, creneau, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Supprimer une plage horaire
   */
  deletePlageHoraire(id: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/dashboard/enseignant/api/plages-horaires/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // ==================== EMPLOIS DU TEMPS ====================

  /**
   * Créer un emploi du temps
   */
  createEmploiDuTemps(emploi: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/emplois-du-temps/creer`, emploi, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Récupérer l'emploi du temps d'une classe pour une semaine
   */
  getEmploiDuTempsSemaine(classeId: string, semaine: number, annee: number): Observable<any> {
    const params = new HttpParams()
      .set('semaine', semaine.toString())
      .set('annee', annee.toString());

    return this.http.get<any>(`${this.apiUrl}/api/emplois-du-temps/classe/${classeId}/semaine`, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  /**
   * Récupérer tous les emplois du temps d'une classe
   */
  getEmploisDuTempsClasse(classeId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/emplois-du-temps/classe/${classeId}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Ajouter une séance à un emploi du temps
   */
  addSeanceToEmploi(emploiId: string, seance: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/emplois-du-temps/${emploiId}/seances`, seance, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Mettre à jour une séance
   */
  updateSeance(seanceId: string, seance: any): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.apiUrl}/api/emplois-du-temps/seances/${seanceId}`, seance, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Supprimer une séance
   */
  deleteSeance(seanceId: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/api/emplois-du-temps/seances/${seanceId}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Supprimer un emploi du temps
   */
  deleteEmploiDuTemps(emploiId: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/api/emplois-du-temps/${emploiId}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Dupliquer un emploi du temps
   */
  duplicateEmploiDuTemps(emploiId: string, nouvelleDateDebut: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/emplois-du-temps/${emploiId}/dupliquer`, 
      { nouvelleDateDebut }, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // ==================== PLANIFICATION ====================

  /**
   * Récupérer les cours planifiables pour une classe
   */
  getCoursPlanifiables(classeId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/planification/classe/${classeId}/cours-planifiables`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Récupérer les créneaux disponibles pour une classe
   */
  getCreneauxDisponibles(classeId: string, dateDebut: string, dateFin: string): Observable<any[]> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    return this.http.get<any[]>(`${this.apiUrl}/api/planification/classe/${classeId}/creneaux-disponibles`, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  /**
   * Créer une séance depuis un créneau disponible
   */
  createSeanceFromCreneau(seanceData: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/planification/seance/creer`, seanceData, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Récupérer le résumé de planification d'une classe
   */
  getResumePlanification(classeId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/api/planification/classe/${classeId}/resume`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Récupérer les salles disponibles pour une séance
   */
  getSallesDisponibles(classeId: string, date: string, heureDebut: string, heureFin: string): Observable<any[]> {
    const params = new HttpParams()
      .set('date', date)
      .set('heureDebut', heureDebut)
      .set('heureFin', heureFin);

    return this.http.get<any[]>(`${this.apiUrl}/api/planification/classe/${classeId}/salles-disponibles`, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  /**
   * Vérifier la capacité d'une salle pour une classe
   */
  verifierCapaciteSalle(salleId: string, classeId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/api/planification/salle/${salleId}/verifier-capacite/${classeId}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Réserver la meilleure salle pour une classe
   */
  reserverMeilleureSalle(classeId: string, reservationData: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/api/planification/classe/${classeId}/reserver-meilleure-salle`, reservationData, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /**
   * Récupérer les statistiques des salles
   */
  getStatistiquesSalles(classeId: string, date: string, heureDebut: string, heureFin: string): Observable<any> {
    const params = new HttpParams()
      .set('date', date)
      .set('heureDebut', heureDebut)
      .set('heureFin', heureFin);

    return this.http.get<any>(`${this.apiUrl}/api/planification/classe/${classeId}/statistiques-salles`, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  // ==================== EXCEL ====================

  /**
   * Télécharger le modèle Excel pour les disponibilités
   */
  downloadExcelTemplate(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/excel/modele`, {
      headers: this.authService.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  /**
   * Exporter une disponibilité vers Excel
   */
  exportDisponibiliteToExcel(disponibiliteId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/${disponibiliteId}/excel/export`, {
      headers: this.authService.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  /**
   * Importer des disponibilités depuis Excel
   */
  importDisponibiliteFromExcel(file: File): Observable<ApiResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ApiResponse>(`${this.apiUrl}/dashboard/enseignant/api/disponibilites/excel/import`, formData, {
      headers: this.authService.getAuthHeaders()
    });
  }
}