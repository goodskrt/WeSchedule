import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface ClasseModel {
  id: string;
  nom: string;
  ecole: string;
  effectif: number;
  effectifMax: number;
  description?: string;
  ues: string[]; // IDs des UE associées
  createdAt: Date;
  updatedAt: Date;
}

interface Ecole {
  id: string;
  nom: string;
  code: string;
  couleur: string;
  semestres: number[];
}

interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
}

@Component({
  selector: 'app-classes',
  standalone: true,
  imports: [CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './classes.html',
  styleUrl: './classes.scss'
})
export class ClassesComponent implements OnInit {
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  // Signals pour l'état du composant
  protected readonly showAddModal = signal(false);
  protected readonly showEditModal = signal(false);
  protected readonly showDetailsModal = signal(false);
  protected readonly showUEModal = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly searchTerm = signal('');
  protected readonly selectedEcole = signal('');
  protected readonly currentView = signal<'grid' | 'list'>('grid');

  // Données
  protected readonly classes = signal<ClasseModel[]>([]);
  protected readonly ecoles = signal<Ecole[]>([]);
  protected readonly ues = signal<UEModel[]>([]);
  protected readonly selectedClasse = signal<ClasseModel | null>(null);

  // Formulaire
  protected readonly classeForm = signal({
    id: '',
    nom: '',
    ecole: '',
    effectif: 0,
    effectifMax: 50,
    description: '',
    ues: [] as string[]
  });

  protected readonly errors = signal<{[key: string]: string}>({});

  ngOnInit() {
    this.loadInitialData();
  }

  private loadInitialData() {
    // Charger les écoles avec les mêmes styles que la section professeurs
    const ecoles: Ecole[] = [
      {
        id: 'sji',
        nom: 'Saint Jean Ingénieur',
        code: 'SJI',
        couleur: 'bg-blue-500',
        semestres: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
      },
      {
        id: 'sjm',
        nom: 'Saint Jean Management',
        code: 'SJM',
        couleur: 'bg-green-500',
        semestres: [1, 2, 3, 4, 5, 6]
      },
      {
        id: 'prepa',
        nom: 'PrepaVogt',
        code: 'PV',
        couleur: 'bg-purple-500',
        semestres: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
      },
      {
        id: 'cpge',
        nom: 'Classes Préparatoires',
        code: 'CPGE',
        couleur: 'bg-orange-500',
        semestres: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
      }
    ];
    this.ecoles.set(ecoles);

    // Charger les UE
    this.loadUEs();
    
    // Charger les classes
    this.loadClasses();
  }

  private loadUEs() {
    const ues: UEModel[] = [
      {
        id: '1',
        code: 'INF101',
        nom: 'Introduction à la Programmation',
        credits: 6,
        semestre: 1,
        ecole: '1'
      },
      {
        id: '2',
        code: 'MAT101',
        nom: 'Mathématiques Fondamentales',
        credits: 6,
        semestre: 1,
        ecole: '1'
      },
      {
        id: '3',
        code: 'INF201',
        nom: 'Programmation Orientée Objet',
        credits: 6,
        semestre: 3,
        ecole: '1'
      },
      {
        id: '4',
        code: 'GES101',
        nom: 'Principes de Gestion',
        credits: 4,
        semestre: 1,
        ecole: '2'
      },
      {
        id: '5',
        code: 'MKT201',
        nom: 'Marketing Digital',
        credits: 5,
        semestre: 3,
        ecole: '2'
      }
    ];
    this.ues.set(ues);
  }

  private loadClasses() {
    const classes: ClasseModel[] = [
      // Saint Jean Ingénieur (SJI)
      { 
        id: '1', 
        nom: 'Informatique L1A', 
        ecole: 'sji', 
        effectif: 45, 
        effectifMax: 50,
        description: 'Première année de licence en informatique - Groupe A',
        ues: ['1', '2'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      { 
        id: '2', 
        nom: 'Informatique L1B', 
        ecole: 'sji', 
        effectif: 42, 
        effectifMax: 50,
        description: 'Première année de licence en informatique - Groupe B',
        ues: ['1', '2'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      { 
        id: '3', 
        nom: 'Informatique L2', 
        ecole: 'sji', 
        effectif: 38, 
        effectifMax: 45,
        description: 'Deuxième année de licence en informatique',
        ues: ['3'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      
      // Saint Jean Management (SJM)
      { 
        id: '4', 
        nom: 'Gestion L1', 
        ecole: 'sjm', 
        effectif: 50, 
        effectifMax: 55,
        description: 'Première année de licence en gestion',
        ues: ['4'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      { 
        id: '5', 
        nom: 'Marketing L2', 
        ecole: 'sjm', 
        effectif: 35, 
        effectifMax: 40,
        description: 'Deuxième année spécialisée en marketing',
        ues: ['5'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      
      // PrepaVogt (PV)
      { 
        id: '6', 
        nom: 'Prépa Scientifique 1A', 
        ecole: 'prepa', 
        effectif: 30, 
        effectifMax: 35,
        description: 'Première année de classe préparatoire scientifique',
        ues: ['6'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      
      // Classes Préparatoires (CPGE)
      { 
        id: '7', 
        nom: 'MPSI', 
        ecole: 'cpge', 
        effectif: 35, 
        effectifMax: 40,
        description: 'Mathématiques, Physique et Sciences de l\'Ingénieur',
        ues: ['6'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      }
    ];
    this.classes.set(classes);
  }

  // Gestion des modales
  openAddModal() {
    this.resetForm();
    this.showAddModal.set(true);
  }

  openEditModal(classe: ClasseModel) {
    this.selectedClasse.set(classe);
    this.classeForm.set({
      id: classe.id,
      nom: classe.nom,
      ecole: classe.ecole,
      effectif: classe.effectif,
      effectifMax: classe.effectifMax,
      description: classe.description || '',
      ues: [...classe.ues]
    });
    this.showEditModal.set(true);
  }

  openDetailsModal(classe: ClasseModel) {
    this.selectedClasse.set(classe);
    this.showDetailsModal.set(true);
  }

  openUEModal(classe: ClasseModel) {
    this.selectedClasse.set(classe);
    this.classeForm.update(form => ({
      ...form,
      ues: [...classe.ues]
    }));
    this.showUEModal.set(true);
  }

  closeModals() {
    this.showAddModal.set(false);
    this.showEditModal.set(false);
    this.showDetailsModal.set(false);
    this.showUEModal.set(false);
    this.selectedClasse.set(null);
    this.resetForm();
  }

  private resetForm() {
    this.classeForm.set({
      id: '',
      nom: '',
      ecole: '',
      effectif: 0,
      effectifMax: 50,
      description: '',
      ues: []
    });
    this.errors.set({});
  }

  // Gestion du formulaire
  updateForm(field: string, value: any) {
    this.classeForm.update(form => ({
      ...form,
      [field]: value
    }));
    
    // Effacer l'erreur du champ
    if (this.errors()[field]) {
      this.errors.update(errors => {
        const newErrors = { ...errors };
        delete newErrors[field];
        return newErrors;
      });
    }
  }

  toggleUE(ueId: string) {
    this.classeForm.update(form => {
      const ues = [...form.ues];
      const index = ues.indexOf(ueId);
      if (index > -1) {
        ues.splice(index, 1);
      } else {
        ues.push(ueId);
      }
      return { ...form, ues };
    });
  }

  // Validation et soumission
  validateForm(): boolean {
    const form = this.classeForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.nom.trim()) {
      newErrors['nom'] = 'Le nom de la classe est requis';
    }

    if (!form.ecole) {
      newErrors['ecole'] = 'L\'école est requise';
    }

    if (form.effectifMax < 1) {
      newErrors['effectifMax'] = 'L\'effectif maximum doit être supérieur à 0';
    }

    if (form.effectif > form.effectifMax) {
      newErrors['effectif'] = 'L\'effectif ne peut pas dépasser l\'effectif maximum';
    }

    if (form.effectif < 0) {
      newErrors['effectif'] = 'L\'effectif ne peut pas être négatif';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  onSubmit() {
    if (!this.validateForm()) return;

    this.isLoading.set(true);
    const form = this.classeForm();

    setTimeout(() => {
      if (form.id) {
        // Modification
        this.classes.update(classes => 
          classes.map(classe => 
            classe.id === form.id 
              ? { 
                  ...classe, 
                  ...form, 
                  updatedAt: new Date() 
                }
              : classe
          )
        );
      } else {
        // Ajout
        const newClasse: ClasseModel = {
          ...form,
          id: Date.now().toString(),
          createdAt: new Date(),
          updatedAt: new Date()
        };
        this.classes.update(classes => [...classes, newClasse]);
      }

      this.isLoading.set(false);
      this.closeModals();
    }, 1000);
  }

  saveUEAssociations() {
    if (!this.selectedClasse()) return;

    this.isLoading.set(true);
    const form = this.classeForm();

    setTimeout(() => {
      this.classes.update(classes => 
        classes.map(classe => 
          classe.id === this.selectedClasse()!.id 
            ? { 
                ...classe, 
                ues: [...form.ues],
                updatedAt: new Date() 
              }
            : classe
        )
      );

      this.isLoading.set(false);
      this.closeModals();
    }, 500);
  }

  deleteClasse(classe: ClasseModel) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer la classe "${classe.nom}" ?`)) {
      this.classes.update(classes => classes.filter(c => c.id !== classe.id));
    }
  }

  // Filtres et recherche
  getFilteredClasses(): ClasseModel[] {
    let filtered = this.classes();

    // Filtre par terme de recherche
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(classe => 
        classe.nom.toLowerCase().includes(term) ||
        (classe.description && classe.description.toLowerCase().includes(term))
      );
    }

    // Filtre par école
    if (this.selectedEcole()) {
      filtered = filtered.filter(classe => classe.ecole === this.selectedEcole());
    }

    return filtered;
  }

  // Utilitaires
  getEcoleName(ecoleId: string): string {
    const ecole = this.ecoles().find(e => e.id === ecoleId);
    return ecole ? ecole.nom : 'École inconnue';
  }

  getEcole(ecoleId: string) {
    return this.ecoles().find(e => e.id === ecoleId);
  }

  getUENames(ueIds: string[]): string {
    const ues = this.ues().filter(ue => ueIds.includes(ue.id));
    return ues.map(ue => ue.code).join(', ') || 'Aucune UE';
  }

  getAvailableUEs(): UEModel[] {
    const form = this.classeForm();
    if (!form.ecole) return [];
    
    return this.ues().filter(ue => ue.ecole === form.ecole);
  }

  getEffectifPercentage(classe: ClasseModel): number {
    return Math.round((classe.effectif / classe.effectifMax) * 100);
  }

  getEffectifStatus(classe: ClasseModel): 'low' | 'medium' | 'high' | 'full' {
    const percentage = this.getEffectifPercentage(classe);
    if (percentage >= 100) return 'full';
    if (percentage >= 80) return 'high';
    if (percentage >= 50) return 'medium';
    return 'low';
  }

  getEffectifStatusColor(status: string): string {
    const colors = {
      low: 'bg-red-100 text-red-800',
      medium: 'bg-yellow-100 text-yellow-800',
      high: 'bg-blue-100 text-blue-800',
      full: 'bg-green-100 text-green-800'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  }

  getEffectifStatusLabel(status: string): string {
    const labels = {
      low: 'Faible',
      medium: 'Moyen',
      high: 'Élevé',
      full: 'Complet'
    };
    return labels[status as keyof typeof labels] || 'Inconnu';
  }

  clearFilters() {
    this.searchTerm.set('');
    this.selectedEcole.set('');
  }

  exportClasses() {
    // Simulation d'export
    console.log('Export des classes:', this.getFilteredClasses());
    alert('Export réalisé avec succès !');
  }

  importClasses() {
    // Simulation d'import
    alert('Fonctionnalité d\'import à implémenter');
  }

  // Statistiques
  getTotalEffectif(): number {
    return this.classes().reduce((total, classe) => total + classe.effectif, 0);
  }

  getTotalEffectifMax(): number {
    return this.classes().reduce((total, classe) => total + classe.effectifMax, 0);
  }

  getAverageEffectif(): number {
    const classes = this.classes();
    if (classes.length === 0) return 0;
    return Math.round(this.getTotalEffectif() / classes.length);
  }

  getClassesByEcole(ecoleId: string): ClasseModel[] {
    return this.classes().filter(classe => classe.ecole === ecoleId);
  }
}