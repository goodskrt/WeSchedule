import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Room {
  id: string;
  name: string;
  type: 'cours' | 'td' | 'labo' | 'info' | 'conference';
  capacity: number;
  equipment: EquipmentItem[];
  building: 'ancien' | 'nouveau';
  floor: 'rez' | 'un' | 'deux' | 'trois';
  status: 'available' | 'occupied' | 'maintenance';
}

export interface EquipmentItem {
  id: string;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class SalleService {
  private apiUrl = `${environment.apiUrl}/api/salles`;

  constructor(private http: HttpClient) {}

  getAllSalles(): Observable<Room[]> {
    return this.http.get<Room[]>(this.apiUrl);
  }

  getSalleById(id: string): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/${id}`);
  }

  createSalle(salle: Partial<Room>): Observable<Room> {
    return this.http.post<Room>(this.apiUrl, salle);
  }

  updateSalle(id: string, salle: Partial<Room>): Observable<Room> {
    return this.http.put<Room>(`${this.apiUrl}/${id}`, salle);
  }

  deleteSalle(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
