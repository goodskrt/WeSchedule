import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CoursModel {
  id: string;
  ueId: string;
  typeId: string;
  professeurId?: string;
  classes: string[];
  duree: number;
  description?: string;
  statut: 'actif' | 'annule' | 'termine' | 'planifie';
  createdAt: Date;
  updatedAt: Date;
}

@Injectable({
  providedIn: 'root'
})
export class CoursService {
  private apiUrl = `${environment.apiUrl}/api/cours`;

  constructor(private http: HttpClient) {}

  getAllCours(): Observable<CoursModel[]> {
    return this.http.get<CoursModel[]>(this.apiUrl);
  }

  getCoursById(id: string): Observable<CoursModel> {
    return this.http.get<CoursModel>(`${this.apiUrl}/${id}`);
  }

  createCours(cours: Partial<CoursModel>): Observable<CoursModel> {
    return this.http.post<CoursModel>(this.apiUrl, cours);
  }

  updateCours(id: string, cours: Partial<CoursModel>): Observable<CoursModel> {
    return this.http.put<CoursModel>(`${this.apiUrl}/${id}`, cours);
  }

  deleteCours(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
