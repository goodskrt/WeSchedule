import { Injectable, signal } from '@angular/core';

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

export interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
  classes: string[];
}

export interface TypeCours {
  id: string;
  nom: string;
  code: string;
  description: string;
  couleur: string;
  dureeDefaut: number;
}

export interface Professeur {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  specialites: string[];
  ecoles?: string[];
}

export interface Classe {
  id: string;
  nom: string;
  niveau: string;
  ecole: string;
  semestre: number;
  effectif: number;
  effectifMax?: number;
  ues?: string[];
}

export interface Ecole {
  id: string;
  nom: string;
  code: string;
  couleur: string;
}

@Injectable({
  providedIn: 'root'
})
export class CoursDataService {
  // Données partagées
  private readonly _cours = signal<CoursModel[]>([]);
  private readonly _ues = signal<UEModel[]>([]);
  private readonly _typesCours = signal<TypeCours[]>([]);
  private readonly _professeurs = signal<Professeur[]>([]);
  private readonly _classes = signal<Classe[]>([]);
  private readonly _ecoles = signal<Ecole[]>([]);

  constructor() {
    this.initializeData();
  }

  // Getters publics
  get cours() {
    return this._cours.asReadonly();
  }

  get ues() {
    return this._ues.asReadonly();
  }

  get typesCours() {
    return this._typesCours.asReadonly();
  }

  get professeurs() {
    return this._professeurs.asReadonly();
  }

  get classes() {
    return this._classes.asReadonly();
  }

  get ecoles() {
    return this._ecoles.asReadonly();
  }

  // Initialisation des données
  private initializeData() {
    // Écoles
    this._ecoles.set([
      { id: 'sji', nom: 'Saint Jean Ingénieur', code: 'SJI', couleur: 'bg-blue-500' },
      { id: 'sjm', nom: 'Saint Jean Management', code: 'SJM', couleur: 'bg-green-500' },
      { id: 'prepa', nom: 'PrepaVogt', code: 'PV', couleur: 'bg-purple-500' },
      { id: 'cpge', nom: 'Classes Préparatoires', code: 'CPGE', couleur: 'bg-orange-500' }
    ]);

    // Types de cours
    this._typesCours.set([
      { id: '1', nom: 'Cours Magistral', code: 'CM', description: 'Cours théorique en amphithéâtre', couleur: 'bg-blue-500', dureeDefaut: 30 },
      { id: '2', nom: 'Travaux Dirigés', code: 'TD', description: 'Exercices dirigés en petits groupes', couleur: 'bg-green-500', dureeDefaut: 20 },
      { id: '3', nom: 'Travaux Pratiques', code: 'TP', description: 'Travaux pratiques en laboratoire', couleur: 'bg-orange-500', dureeDefaut: 40 },
      { id: '4', nom: 'Projet', code: 'PROJ', description: 'Projet encadré', couleur: 'bg-purple-500', dureeDefaut: 60 },
      { id: '5', nom: 'Séminaire', code: 'SEM', description: 'Séminaire spécialisé', couleur: 'bg-indigo-500', dureeDefaut: 15 }
    ]);

    // Professeurs
    this._professeurs.set([
      { id: '1', nom: 'Dupont', prenom: 'Martin', email: 'martin.dupont@iu-saintjean.cm', specialites: ['Informatique', 'Programmation'], ecoles: ['sji'] },
      { id: '2', nom: 'Laurent', prenom: 'Sophie', email: 'sophie.laurent@iu-saintjean.cm', specialites: ['Mathématiques', 'Algorithmique'], ecoles: ['sji'] },
      { id: '3', nom: 'Moreau', prenom: 'Jean', email: 'jean.moreau@iu-saintjean.cm', specialites: ['Gestion', 'Management'], ecoles: ['sjm'] },
      { id: '4', nom: 'Dubois', prenom: 'Marie', email: 'marie.dubois@iu-saintjean.cm', specialites: ['Marketing', 'Communication'], ecoles: ['sjm'] },
      { id: '5', nom: 'Bernard', prenom: 'Sophie', email: 'sophie.bernard@iu-saintjean.cm', specialites: ['Mathématiques'], ecoles: ['prepa', 'cpge'] },
      { id: '6', nom: 'Leroy', prenom: 'Pierre', email: 'pierre.leroy@iu-saintjean.cm', specialites: ['Chimie'], ecoles: ['prepa'] }
    ]);

    // Classes
    this._classes.set([
      { id: '1', nom: 'Informatique L1A', niveau: 'L1', ecole: 'sji', semestre: 1, effectif: 45, effectifMax: 50, ues: ['1', '2'] },
      { id: '2', nom: 'Informatique L1B', niveau: 'L1', ecole: 'sji', semestre: 1, effectif: 42, effectifMax: 50, ues: ['1', '2'] },
      { id: '3', nom: 'Informatique L2', niveau: 'L2', ecole: 'sji', semestre: 3, effectif: 38, effectifMax: 45, ues: ['3'] },
      { id: '7', nom: 'Gestion L1', niveau: 'L1', ecole: 'sjm', semestre: 1, effectif: 50, effectifMax: 55, ues: ['4'] },
      { id: '9', nom: 'Marketing L2', niveau: 'L2', ecole: 'sjm', semestre: 3, effectif: 35, effectifMax: 40, ues: ['5'] },
      { id: '11', nom: 'Prépa Scientifique 1A', niveau: 'Prépa', ecole: 'prepa', semestre: 1, effectif: 30, effectifMax: 35, ues: ['6', '8'] },
      { id: '13', nom: 'MPSI', niveau: 'CPGE', ecole: 'cpge', semestre: 1, effectif: 35, effectifMax: 40, ues: ['6', '7'] }
    ]);

    // UEs
    this._ues.set([
      { id: '1', code: 'INF101', nom: 'Introduction à la Programmation', credits: 6, semestre: 1, ecole: 'sji', classes: ['1', '2'] },
      { id: '2', code: 'MAT101', nom: 'Mathématiques Fondamentales', credits: 6, semestre: 1, ecole: 'sji', classes: ['1', '2'] },
      { id: '3', code: 'INF201', nom: 'Programmation Orientée Objet', credits: 6, semestre: 3, ecole: 'sji', classes: ['3'] },
      { id: '4', code: 'GES101', nom: 'Principes de Gestion', credits: 4, semestre: 1, ecole: 'sjm', classes: ['7'] },
      { id: '5', code: 'MKT201', nom: 'Marketing Digital', credits: 5, semestre: 3, ecole: 'sjm', classes: ['9'] },
      { id: '6', code: 'MAT201', nom: 'Mathématiques Supérieures', credits: 8, semestre: 1, ecole: 'prepa', classes: ['11', '13'] },
      { id: '7', code: 'PHY101', nom: 'Physique Générale', credits: 6, semestre: 1, ecole: 'cpge', classes: ['13'] },
      { id: '8', code: 'CHI101', nom: 'Chimie Générale', credits: 6, semestre: 1, ecole: 'prepa', classes: ['11'] }
    ]);

    // Cours
    this._cours.set([
      { id: '1', ueId: '1', typeId: '1', professeurId: '1', classes: ['1', '2'], duree: 30, description: 'Cours magistral d\'introduction à la programmation', statut: 'actif', createdAt: new Date('2024-01-15'), updatedAt: new Date('2024-01-15') },
      { id: '2', ueId: '1', typeId: '2', professeurId: '1', classes: ['1'], duree: 20, description: 'Travaux dirigés de programmation', statut: 'actif', createdAt: new Date('2024-01-15'), updatedAt: new Date('2024-01-15') },
      { id: '3', ueId: '1', typeId: '3', professeurId: '1', classes: ['2'], duree: 40, description: 'Travaux pratiques de programmation', statut: 'actif', createdAt: new Date('2024-01-15'), updatedAt: new Date('2024-01-15') },
      { id: '4', ueId: '2', typeId: '1', professeurId: '2', classes: ['1', '2'], duree: 30, description: 'Cours magistral de mathématiques', statut: 'actif', createdAt: new Date('2024-01-15'), updatedAt: new Date('2024-01-15') },
      { id: '5', ueId: '4', typeId: '1', professeurId: '3', classes: ['7'], duree: 25, description: 'Cours magistral de gestion', statut: 'actif', createdAt: new Date('2024-01-15'), updatedAt: new Date('2024-01-15') },
      { id: '6', ueId: '5', typeId: '1', professeurId: '3', classes: ['9'], duree: 30, description: 'Cours magistral de marketing digital', statut: 'actif', createdAt: new Date('2024-01-15'), updatedAt: new Date('2024-01-15') }
    ]);
  }

  // Méthodes pour gérer les cours
  setCours(cours: CoursModel[]) {
    this._cours.set(cours);
  }

  addCours(cours: CoursModel) {
    this._cours.update(list => [...list, cours]);
  }

  updateCours(updatedCours: CoursModel) {
    this._cours.update(list => 
      list.map(c => c.id === updatedCours.id ? updatedCours : c)
    );
  }

  deleteCours(coursId: string) {
    this._cours.update(list => list.filter(c => c.id !== coursId));
  }

  // Méthodes utilitaires
  getCoursForClass(classeId: string): CoursModel[] {
    return this._cours().filter(c => c.classes.includes(classeId) && c.statut === 'actif');
  }

  getCoursForSchool(ecoleId: string): CoursModel[] {
    return this._cours().filter(c => {
      const ue = this._ues().find(u => u.id === c.ueId);
      return ue?.ecole === ecoleId && c.statut === 'actif';
    });
  }

  getUE(ueId: string): UEModel | undefined {
    return this._ues().find(u => u.id === ueId);
  }

  getTypeCours(typeId: string): TypeCours | undefined {
    return this._typesCours().find(t => t.id === typeId);
  }

  getProfesseur(professeurId: string): Professeur | undefined {
    return this._professeurs().find(p => p.id === professeurId);
  }

  getClasse(classeId: string): Classe | undefined {
    return this._classes().find(c => c.id === classeId);
  }

  getEcole(ecoleId: string): Ecole | undefined {
    return this._ecoles().find(e => e.id === ecoleId);
  }

  // Méthode pour obtenir le nom complet d'un cours
  getCoursName(cours: CoursModel): string {
    const ue = this.getUE(cours.ueId);
    const type = this.getTypeCours(cours.typeId);
    return `${ue?.nom || 'UE inconnue'} (${type?.code || 'Type inconnu'})`;
  }

  // Méthode pour vérifier la disponibilité d'un professeur
  checkProfesseurAvailability(professeurId: string, day: string, timeSlot: string, existingSchedule: any): {
    available: boolean;
    conflictingCourse?: string;
  } {
    // Vérifier si le professeur a déjà un cours à ce créneau
    for (const dayKey of Object.keys(existingSchedule)) {
      const slots = existingSchedule[dayKey];
      for (const slotKey of Object.keys(slots)) {
        const slot = slots[slotKey];
        if (slot && slot.teacherId === professeurId && dayKey === day && slotKey === timeSlot) {
          return {
            available: false,
            conflictingCourse: slot.subject
          };
        }
      }
    }
    
    return { available: true };
  }
}
