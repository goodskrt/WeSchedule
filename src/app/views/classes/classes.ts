import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface ClasseModel {
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
  ues: string[]; // IDs des UE associées
  createdAt: Date;
  updatedAt: Date;
}

interface Ecole {
  id: string;
  nom: string;
  code: string;
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

interface Professeur {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  specialites: string[];
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
  protected readonly selectedSemestre = signal(0);
  protected readonly selectedNiveau = signal('');
  protected readonly currentView = signal<'grid' | 'list'>('grid');

  // Données
  protected readonly classes = signal<ClasseModel[]>([]);
  protected readonly ecoles = signal<Ecole[]>([]);
  protected readonly ues = signal<UEModel[]>([]);
  protected readonly professeurs = signal<Professeur[]>([]);
  protected readonly selectedClasse = signal<ClasseModel | null>(null);

  // Formulaire
  protected readonly classeForm = signal({
    id: '',
    nom: '',
    niveau: '',
    ecole: '',
    semestre: 1,
    effectif: 0,
    effectifMax: 50,
    responsable: '',
    description: '',
    specialite: '',
    ues: [] as string[]
  });

  protected readonly errors = signal<{[key: string]: string}>({});

  ngOnInit() {
    this.loadInitialData();
  }

  private loadInitialData() {
    // Charger les écoles
    const ecoles: Ecole[] = [
      {
        id: '1',
        nom: 'École d\'Ingénierie',
        code: 'EI',
        semestres: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
      },
      {
        id: '2',
        nom: 'École de Commerce',
        code: 'EC',
        semestres: [1, 2, 3, 4, 5, 6]
      },
      {
        id: '3',
        nom: 'École de Médecine',
        code: 'EM',
        semestres: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
      }
    ];
    this.ecoles.set(ecoles);

    // Charger les professeurs
    this.loadProfesseurs();
    
    // Charger les UE
    this.loadUEs();
    
    // Charger les classes
    this.loadClasses();
  }

  private loadProfesseurs() {
    const professeurs: Professeur[] = [
      {
        id: '1',
        nom: 'Dupont',
        prenom: 'Martin',
        email: 'martin.dupont@iu-saintjean.cm',
        specialites: ['Informatique', 'Programmation']
      },
      {
        id: '2',
        nom: 'Laurent',
        prenom: 'Sophie',
        email: 'sophie.laurent@iu-saintjean.cm',
        specialites: ['Mathématiques', 'Algorithmique']
      },
      {
        id: '3',
        nom: 'Moreau',
        prenom: 'Jean',
        email: 'jean.moreau@iu-saintjean.cm',
        specialites: ['Gestion', 'Management']
      },
      {
        id: '4',
        nom: 'Dubois',
        prenom: 'Marie',
        email: 'marie.dubois@iu-saintjean.cm',
        specialites: ['Marketing', 'Communication']
      }
    ];
    this.professeurs.set(professeurs);
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
      // École d'Ingénierie
      { 
        id: '1', 
        nom: 'Informatique L1A', 
        niveau: 'L1', 
        ecole: '1', 
        semestre: 1, 
        effectif: 45, 
        effectifMax: 50,
        responsable: '1',
        specialite: 'Informatique',
        description: 'Première année de licence en informatique - Groupe A',
        ues: ['1', '2'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      { 
        id: '2', 
        nom: 'Informatique L1B', 
        niveau: 'L1', 
        ecole: '1', 
        semestre: 1, 
        effectif: 42, 
        effectifMax: 50,
        responsable: '1',
        specialite: 'Informatique',
        description: 'Première année de licence en informatique - Groupe B',
        ues: ['1', '2'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      { 
        id: '3', 
        nom: 'Informatique L2', 
        niveau: 'L2', 
        ecole: '1', 
        semestre: 3, 
        effectif: 38, 
        effectifMax: 45,
        responsable: '2',
        specialite: 'Informatique',
        description: 'Deuxième année de licence en informatique',
        ues: ['3'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      
      // École de Commerce
      { 
        id: '4', 
        nom: 'Gestion L1', 
        niveau: 'L1', 
        ecole: '2', 
        semestre: 1, 
        effectif: 50, 
        effectifMax: 55,
        responsable: '3',
        specialite: 'Gestion',
        description: 'Première année de licence en gestion',
        ues: ['4'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      { 
        id: '5', 
        nom: 'Marketing L2', 
        niveau: 'L2', 
        ecole: '2', 
        semestre: 3, 
        effectif: 35, 
        effectifMax: 40,
        responsable: '4',
        specialite: 'Marketing',
        description: 'Deuxième année spécialisée en marketing',
        ues: ['5'],
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
      niveau: classe.niveau,
      ecole: classe.ecole,
      semestre: classe.semestre,
      effectif: classe.effectif,
      effectifMax: classe.effectifMax,
      responsable: classe.responsable || '',
      description: classe.description || '',
      specialite: classe.specialite || '',
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
      niveau: '',
      ecole: '',
      semestre: 1,
      effectif: 0,
      effectifMax: 50,
      responsable: '',
      description: '',
      specialite: '',
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

    if (!form.niveau) {
      newErrors['niveau'] = 'Le niveau est requis';
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
        classe.niveau.toLowerCase().includes(term) ||
        (classe.specialite && classe.specialite.toLowerCase().includes(term)) ||
        this.getProfesseurName(classe.responsable).toLowerCase().includes(term)
      );
    }

    // Filtre par école
    if (this.selectedEcole()) {
      filtered = filtered.filter(classe => classe.ecole === this.selectedEcole());
    }

    // Filtre par semestre
    if (this.selectedSemestre()) {
      filtered = filtered.filter(classe => classe.semestre === this.selectedSemestre());
    }

    // Filtre par niveau
    if (this.selectedNiveau()) {
      filtered = filtered.filter(classe => classe.niveau === this.selectedNiveau());
    }

    return filtered;
  }

  // Utilitaires
  getEcoleName(ecoleId: string): string {
    const ecole = this.ecoles().find(e => e.id === ecoleId);
    return ecole ? ecole.nom : 'École inconnue';
  }

  getProfesseurName(professeurId?: string): string {
    if (!professeurId) return 'Non assigné';
    const prof = this.professeurs().find(p => p.id === professeurId);
    return prof ? `${prof.prenom} ${prof.nom}` : 'Professeur inconnu';
  }

  getUENames(ueIds: string[]): string {
    const ues = this.ues().filter(ue => ueIds.includes(ue.id));
    return ues.map(ue => ue.code).join(', ') || 'Aucune UE';
  }

  getUEsForClasse(classe: ClasseModel): UEModel[] {
    return this.ues().filter(ue => 
      ue.ecole === classe.ecole && 
      ue.semestre === classe.semestre
    );
  }

  getAvailableUEs(): UEModel[] {
    const form = this.classeForm();
    if (!form.ecole || !form.semestre) return [];
    
    return this.ues().filter(ue => 
      ue.ecole === form.ecole && ue.semestre === form.semestre
    );
  }

  getSemestresForEcole(ecoleId: string): number[] {
    const ecole = this.ecoles().find(e => e.id === ecoleId);
    return ecole ? ecole.semestres : [];
  }

  getNiveaux(): string[] {
    return ['L1', 'L2', 'L3', 'M1', 'M2'];
  }

  getSpecialites(): string[] {
    return ['Informatique', 'Mathématiques', 'Gestion', 'Marketing', 'Commerce', 'Médecine', 'Droit'];
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
    this.selectedSemestre.set(0);
    this.selectedNiveau.set('');
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