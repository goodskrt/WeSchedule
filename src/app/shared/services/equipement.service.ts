import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EquipmentModel {
  id: string;
  name: string;
  category: string;
  icon: string;
  description?: string;
  totalQuantity: number;
  availableQuantity: number;
  status: 'active' | 'maintenance' | 'retired';
  createdAt: Date;
  updatedAt: Date;
}

export interface EquipmentAssignment {
  id: string;
  equipmentId: string;
  assignmentType: 'room' | 'class';
  targetId: string;
  quantity: number;
  startDate: string;
  endDate?: string;
  duration: 'permanent' | 'temporary';
  reason: string;
  status: 'active' | 'expired' | 'cancelled';
  assignedBy: string;
  assignedAt: Date;
  notes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class EquipementService {
  private apiUrl = `${environment.apiUrl}/api/equipments`;
  private assignmentUrl = `${environment.apiUrl}/api/equipment-assignments`;

  constructor(private http: HttpClient) {}

  getAllEquipments(): Observable<EquipmentModel[]> {
    return this.http.get<EquipmentModel[]>(this.apiUrl);
  }

  getEquipmentById(id: string): Observable<EquipmentModel> {
    return this.http.get<EquipmentModel>(`${this.apiUrl}/${id}`);
  }

  createEquipment(equipment: Partial<EquipmentModel>): Observable<EquipmentModel> {
    return this.http.post<EquipmentModel>(this.apiUrl, equipment);
  }

  updateEquipment(id: string, equipment: Partial<EquipmentModel>): Observable<EquipmentModel> {
    return this.http.put<EquipmentModel>(`${this.apiUrl}/${id}`, equipment);
  }

  deleteEquipment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Assignment methods
  getAllAssignments(): Observable<EquipmentAssignment[]> {
    return this.http.get<EquipmentAssignment[]>(this.assignmentUrl);
  }

  getAssignmentById(id: string): Observable<EquipmentAssignment> {
    return this.http.get<EquipmentAssignment>(`${this.assignmentUrl}/${id}`);
  }

  createAssignment(assignment: Partial<EquipmentAssignment>): Observable<EquipmentAssignment> {
    return this.http.post<EquipmentAssignment>(this.assignmentUrl, assignment);
  }

  updateAssignment(id: string, assignment: Partial<EquipmentAssignment>): Observable<EquipmentAssignment> {
    return this.http.put<EquipmentAssignment>(`${this.assignmentUrl}/${id}`, assignment);
  }

  deleteAssignment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.assignmentUrl}/${id}`);
  }
}
