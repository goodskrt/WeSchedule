import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ClasseModel {
  id: string;
  nom: string;
  ecole: string;
  effectif: number;
  effectifMax: number;
  description?: string;
  ues: string[];
  createdAt: Date;
  updatedAt: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ClasseService {
  private apiUrl = `${environment.apiUrl}/api/classes`;

  constructor(private http: HttpClient) {}

  getAllClasses(): Observable<ClasseModel[]> {
    return this.http.get<ClasseModel[]>(this.apiUrl);
  }

  getClasseById(id: string): Observable<ClasseModel> {
    return this.http.get<ClasseModel>(`${this.apiUrl}/${id}`);
  }

  createClasse(classe: Partial<ClasseModel>): Observable<ClasseModel> {
    return this.http.post<ClasseModel>(this.apiUrl, classe);
  }

  updateClasse(id: string, classe: Partial<ClasseModel>): Observable<ClasseModel> {
    return this.http.put<ClasseModel>(`${this.apiUrl}/${id}`, classe);
  }

  deleteClasse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
