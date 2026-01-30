import { Injectable, signal } from '@angular/core';

export interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
  classes: string[];
}

export interface ClasseModel {
  id: string;
  nom: string;
  niveau: string;
  ecole: string;
  semestre: number;
  effectif: number;
  effectifMax: number;
  responsable?: string;
  description?: string;
  specialite?: string;
  ues: string[];
}

@Injectable({
  providedIn: 'root'
})
export class DataSyncService {
  // Signals partagés pour les données
  private readonly _ues = signal<UEModel[]>([]);
  private readonly _classes = signal<ClasseModel[]>([]);

  // Getters publics
  get ues() {
    return this._ues.asReadonly();
  }

  get classes() {
    return this._classes.asReadonly();
  }

  // Méthodes pour mettre à jour les UE
  setUEs(ues: UEModel[]) {
    this._ues.set(ues);
  }

  addUE(ue: UEModel) {
    this._ues.update(ues => [...ues, ue]);
  }

  updateUE(updatedUE: UEModel) {
    this._ues.update(ues => 
      ues.map(ue => ue.id === updatedUE.id ? updatedUE : ue)
    );
  }

  deleteUE(ueId: string) {
    // Supprimer l'UE
    this._ues.update(ues => ues.filter(ue => ue.id !== ueId));
    
    // Supprimer l'UE des classes qui l'utilisent
    this._classes.update(classes =>
      classes.map(classe => ({
        ...classe,
        ues: classe.ues.filter(id => id !== ueId)
      }))
    );
  }

  // Méthodes pour mettre à jour les classes
  setClasses(classes: ClasseModel[]) {
    this._classes.set(classes);
  }

  addClasse(classe: ClasseModel) {
    this._classes.update(classes => [...classes, classe]);
  }

  updateClasse(updatedClasse: ClasseModel) {
    this._classes.update(classes => 
      classes.map(classe => classe.id === updatedClasse.id ? updatedClasse : classe)
    );
  }

  deleteClasse(classeId: string) {
    // Supprimer la classe
    this._classes.update(classes => classes.filter(classe => classe.id !== classeId));
    
    // Supprimer la classe des UE qui l'utilisent
    this._ues.update(ues =>
      ues.map(ue => ({
        ...ue,
        classes: ue.classes.filter(id => id !== classeId)
      }))
    );
  }

  // Méthodes pour gérer les associations UE-Classe
  associateUEToClasse(ueId: string, classeId: string) {
    // Ajouter la classe à l'UE
    this._ues.update(ues =>
      ues.map(ue => 
        ue.id === ueId && !ue.classes.includes(classeId)
          ? { ...ue, classes: [...ue.classes, classeId] }
          : ue
      )
    );

    // Ajouter l'UE à la classe
    this._classes.update(classes =>
      classes.map(classe => 
        classe.id === classeId && !classe.ues.includes(ueId)
          ? { ...classe, ues: [...classe.ues, ueId] }
          : classe
      )
    );
  }

  dissociateUEFromClasse(ueId: string, classeId: string) {
    // Supprimer la classe de l'UE
    this._ues.update(ues =>
      ues.map(ue => 
        ue.id === ueId
          ? { ...ue, classes: ue.classes.filter(id => id !== classeId) }
          : ue
      )
    );

    // Supprimer l'UE de la classe
    this._classes.update(classes =>
      classes.map(classe => 
        classe.id === classeId
          ? { ...classe, ues: classe.ues.filter(id => id !== ueId) }
          : classe
      )
    );
  }

  // Méthodes utilitaires
  getUEsForClasse(classeId: string): UEModel[] {
    const classe = this._classes().find(c => c.id === classeId);
    if (!classe) return [];
    
    return this._ues().filter(ue => classe.ues.includes(ue.id));
  }

  getClassesForUE(ueId: string): ClasseModel[] {
    const ue = this._ues().find(u => u.id === ueId);
    if (!ue) return [];
    
    return this._classes().filter(classe => ue.classes.includes(classe.id));
  }

  getUEsByEcoleAndSemestre(ecoleId: string, semestre: number): UEModel[] {
    return this._ues().filter(ue => 
      ue.ecole === ecoleId && ue.semestre === semestre
    );
  }

  getClassesByEcoleAndSemestre(ecoleId: string, semestre: number): ClasseModel[] {
    return this._classes().filter(classe => 
      classe.ecole === ecoleId && classe.semestre === semestre
    );
  }

  // Méthodes de statistiques
  getTotalUEs(): number {
    return this._ues().length;
  }

  getTotalClasses(): number {
    return this._classes().length;
  }

  getTotalEffectif(): number {
    return this._classes().reduce((total, classe) => total + classe.effectif, 0);
  }

  getAverageUEsPerClasse(): number {
    const classes = this._classes();
    if (classes.length === 0) return 0;
    
    const totalUEs = classes.reduce((total, classe) => total + classe.ues.length, 0);
    return Math.round(totalUEs / classes.length * 100) / 100;
  }

  getAverageClassesPerUE(): number {
    const ues = this._ues();
    if (ues.length === 0) return 0;
    
    const totalClasses = ues.reduce((total, ue) => total + ue.classes.length, 0);
    return Math.round(totalClasses / ues.length * 100) / 100;
  }
}