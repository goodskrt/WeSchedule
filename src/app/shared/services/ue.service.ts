import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
  classes: string[];
  description?: string;
  prerequis?: string[];
  objectifs?: string[];
  createdAt: Date;
  updatedAt: Date;
}

@Injectable({
  providedIn: 'root'
})
export class UEService {
  private apiUrl = `${environment.apiUrl}/api/ues`;

  constructor(private http: HttpClient) {}

  getAllUEs(): Observable<UEModel[]> {
    return this.http.get<UEModel[]>(this.apiUrl);
  }

  getUEById(id: string): Observable<UEModel> {
    return this.http.get<UEModel>(`${this.apiUrl}/${id}`);
  }

  createUE(ue: Partial<UEModel>): Observable<UEModel> {
    return this.http.post<UEModel>(this.apiUrl, ue);
  }

  updateUE(id: string, ue: Partial<UEModel>): Observable<UEModel> {
    return this.http.put<UEModel>(`${this.apiUrl}/${id}`, ue);
  }

  deleteUE(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
