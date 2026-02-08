import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Teacher {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  specialization: string;
  department: string;
  schools: string[];
  status: 'active' | 'inactive' | 'on-leave';
  courses: string[];
  joinDate: string;
  qualifications: string[];
  avatar?: string;
  totalStudents: number;
  weeklyHours: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProfesseurService {
  private apiUrl = `${environment.apiUrl}/api/admin/enseignants`;

  constructor(private http: HttpClient) {}

  getAllProfesseurs(): Observable<Teacher[]> {
    return this.http.get<Teacher[]>(this.apiUrl);
  }

  getProfesseurById(id: string): Observable<Teacher> {
    return this.http.get<Teacher>(`${this.apiUrl}/${id}`);
  }

  createProfesseur(professeur: Partial<Teacher>): Observable<Teacher> {
    return this.http.post<Teacher>(this.apiUrl, professeur);
  }

  updateProfesseur(id: string, professeur: Partial<Teacher>): Observable<Teacher> {
    return this.http.put<Teacher>(`${this.apiUrl}/${id}`, professeur);
  }

  deleteProfesseur(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
