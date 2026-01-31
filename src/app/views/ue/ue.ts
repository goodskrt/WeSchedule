import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface UEModel {
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

interface Classe {
  id: string;
  nom: string;
  niveau: string;
  ecole: string;
  semestre: number;
  effectif: number;
}

interface Ecole {
  id: string;
  nom: string;
  code: string;
  couleur: string;
  semestres: number[];
}

interface CourseType {
  id: string;
  nom: string;
  code: string;
  description: string;
  couleur: string;
}

@Component({
  selector: 'app-ue',
  standalone: true,
  imports: [CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './ue.html',
  styleUrl: './ue.scss'
})
export class UEComponent implements OnInit {
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  // Signals pour l'état du composant
  protected readonly showAddModal = signal(false);
  protected readonly showEditModal = signal(false);
  protected readonly showDetailsModal = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly searchTerm = signal('');
  protected readonly selectedEcole = signal('');
  protected readonly selectedSemestre = signal(0);
  protected readonly selectedClasse = signal('');
  protected readonly currentView = signal<'grid' | 'list'>('grid');

  // Données
  protected readonly ues = signal<UEModel[]>([]);
  protected readonly classes = signal<Classe[]>([]);
  protected readonly ecoles = signal<Ecole[]>([]);
  protected readonly courseTypes = signal<CourseType[]>([]);
  protected readonly selectedUE = signal<UEModel | null>(null);

  // Formulaire
  protected readonly ueForm = signal({
    id: '',
    code: '',
    nom: '',
    credits: 3,
    semestre: 1,
    ecole: '',
    classes: [] as string[],
    description: '',
    prerequis: [] as string[],
    objectifs: [] as string[]
  });

  protected readonly errors = signal<{[key: string]: string}>({});

  ngOnInit() {
    this.loadInitialData();
  }

  private loadInitialData() {
    // Charger les écoles
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

    // Charger les types de cours
    const courseTypes: CourseType[] = [
      { id: '1', nom: 'Cours Magistral', code: 'CM', description: 'Cours théorique en amphithéâtre', couleur: 'bg-blue-500' },
      { id: '2', nom: 'Travaux Dirigés', code: 'TD', description: 'Exercices dirigés en petits groupes', couleur: 'bg-green-500' },
      { id: '3', nom: 'Travaux Pratiques', code: 'TP', description: 'Travaux pratiques en laboratoire', couleur: 'bg-orange-500' },
      { id: '4', nom: 'Projet', code: 'PROJ', description: 'Projet encadré', couleur: 'bg-purple-500' },
      { id: '5', nom: 'séminaire', code: 'séminaire', description: 'séminaire en entreprise', couleur: 'bg-red-500' }
    ];
    this.courseTypes.set(courseTypes);

    // Charger les classes
    this.loadClasses();
    
    // Charger les UE
    this.loadUEs();
  }

  private loadClasses() {
    const classes: Classe[] = [
      // Saint Jean Ingénieur (SJI)
      { id: '1', nom: 'Informatique L1A', niveau: 'L1', ecole: 'sji', semestre: 1, effectif: 45 },
      { id: '2', nom: 'Informatique L1B', niveau: 'L1', ecole: 'sji', semestre: 1, effectif: 42 },
      { id: '3', nom: 'Informatique L2', niveau: 'L2', ecole: 'sji', semestre: 3, effectif: 38 },
      { id: '4', nom: 'Informatique L2', niveau: 'L2', ecole: 'sji', semestre: 4, effectif: 38 },
      { id: '5', nom: 'Informatique L3', niveau: 'L3', ecole: 'sji', semestre: 5, effectif: 35 },
      { id: '6', nom: 'Informatique L3', niveau: 'L3', ecole: 'sji', semestre: 6, effectif: 35 },
      
      // Saint Jean Management (SJM)
      { id: '7', nom: 'Gestion L1', niveau: 'L1', ecole: 'sjm', semestre: 1, effectif: 50 },
      { id: '8', nom: 'Gestion L1', niveau: 'L1', ecole: 'sjm', semestre: 2, effectif: 50 },
      { id: '9', nom: 'Marketing L2', niveau: 'L2', ecole: 'sjm', semestre: 3, effectif: 35 },
      { id: '10', nom: 'Marketing L2', niveau: 'L2', ecole: 'sjm', semestre: 4, effectif: 35 },
      
      // PrepaVogt (PV)
      { id: '11', nom: 'Prépa Scientifique 1A', niveau: 'Prépa', ecole: 'prepa', semestre: 1, effectif: 30 },
      { id: '12', nom: 'Prépa Scientifique 2A', niveau: 'Prépa', ecole: 'prepa', semestre: 3, effectif: 28 },
      
      // Classes Préparatoires (CPGE)
      { id: '13', nom: 'MPSI', niveau: 'CPGE', ecole: 'cpge', semestre: 1, effectif: 35 },
      { id: '14', nom: 'PCSI', niveau: 'CPGE', ecole: 'cpge', semestre: 1, effectif: 32 }
    ];
    this.classes.set(classes);
  }

  private loadUEs() {
    const ues: UEModel[] = [
      {
        id: '1',
        code: 'INF101',
        nom: 'Introduction à la Programmation',
        credits: 6,
        semestre: 1,
        ecole: 'sji',
        classes: ['1'],
        description: 'Initiation aux concepts de base de la programmation',
        prerequis: [],
        objectifs: ['Maîtriser les structures de base', 'Écrire des algorithmes simples'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '2',
        code: 'MAT101',
        nom: 'Mathématiques Fondamentales',
        credits: 6,
        semestre: 1,
        ecole: 'sji',
        classes: ['1'],
        description: 'Bases mathématiques pour l\'informatique',
        prerequis: [],
        objectifs: ['Maîtriser l\'algèbre linéaire', 'Comprendre les fonctions'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '3',
        code: 'GES101',
        nom: 'Principes de Gestion',
        credits: 4,
        semestre: 1,
        ecole: 'sjm',
        classes: ['7'],
        description: 'Introduction aux concepts de gestion d\'entreprise',
        prerequis: [],
        objectifs: ['Comprendre les bases de la gestion', 'Analyser les organisations'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '4',
        code: 'MKT201',
        nom: 'Marketing Digital',
        credits: 5,
        semestre: 3,
        ecole: 'sjm',
        classes: ['9'],
        description: 'Stratégies marketing dans l\'ère numérique',
        prerequis: ['3'],
        objectifs: ['Maîtriser les outils digitaux', 'Développer des stratégies marketing'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '5',
        code: 'PHY101',
        nom: 'Physique Générale',
        credits: 6,
        semestre: 1,
        ecole: 'prepa',
        classes: ['11'],
        description: 'Concepts fondamentaux de physique',
        prerequis: [],
        objectifs: ['Comprendre la mécanique', 'Maîtriser la thermodynamique'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '6',
        code: 'MATH201',
        nom: 'Mathématiques Supérieures',
        credits: 8,
        semestre: 1,
        ecole: 'cpge',
        classes: ['13', '14'],
        description: 'Mathématiques avancées pour classes préparatoires',
        prerequis: [],
        objectifs: ['Maîtriser l\'analyse', 'Comprendre l\'algèbre avancée'],
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      }
    ];
    this.ues.set(ues);
  }

  // Gestion des modales
  openAddModal() {
    this.resetForm();
    this.showAddModal.set(true);
  }

  openEditModal(ue: UEModel) {
    this.selectedUE.set(ue);
    this.ueForm.set({
      id: ue.id,
      code: ue.code,
      nom: ue.nom,
      credits: ue.credits,
      semestre: ue.semestre,
      ecole: ue.ecole,
      classes: [...ue.classes],
      description: ue.description || '',
      prerequis: [...(ue.prerequis || [])],
      objectifs: [...(ue.objectifs || [])]
    });
    this.showEditModal.set(true);
  }

  openDetailsModal(ue: UEModel) {
    this.selectedUE.set(ue);
    this.showDetailsModal.set(true);
  }

  closeModals() {
    this.showAddModal.set(false);
    this.showEditModal.set(false);
    this.showDetailsModal.set(false);
    this.selectedUE.set(null);
    this.resetForm();
  }

  private resetForm() {
    this.ueForm.set({
      id: '',
      code: '',
      nom: '',
      credits: 3,
      semestre: 1,
      ecole: '',
      classes: [],
      description: '',
      prerequis: [],
      objectifs: []
    });
    this.errors.set({});
  }

  // Gestion du formulaire
  updateForm(field: string, value: any) {
    this.ueForm.update(form => ({
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

  toggleClasse(classeId: string) {
    this.ueForm.update(form => {
      const classes = [...form.classes];
      const index = classes.indexOf(classeId);
      if (index > -1) {
        classes.splice(index, 1);
      } else {
        classes.push(classeId);
      }
      return { ...form, classes };
    });
  }

  // Validation et soumission
  validateForm(): boolean {
    const form = this.ueForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.code.trim()) {
      newErrors['code'] = 'Le code UE est requis';
    }

    if (!form.nom.trim()) {
      newErrors['nom'] = 'Le nom de l\'UE est requis';
    }

    if (!form.ecole) {
      newErrors['ecole'] = 'L\'école est requise';
    }

    if (form.classes.length === 0) {
      newErrors['classes'] = 'Au moins une classe doit être sélectionnée';
    }

    if (form.credits < 1 || form.credits > 10) {
      newErrors['credits'] = 'Les crédits doivent être entre 1 et 10';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  onSubmit() {
    if (!this.validateForm()) return;

    this.isLoading.set(true);
    const form = this.ueForm();

    setTimeout(() => {
      if (form.id) {
        // Modification
        this.ues.update(ues => 
          ues.map(ue => 
            ue.id === form.id 
              ? { 
                  ...ue, 
                  ...form, 
                  updatedAt: new Date() 
                }
              : ue
          )
        );
      } else {
        // Ajout
        const newUE: UEModel = {
          ...form,
          id: Date.now().toString(),
          createdAt: new Date(),
          updatedAt: new Date()
        };
        this.ues.update(ues => [...ues, newUE]);
      }

      this.isLoading.set(false);
      this.closeModals();
    }, 1000);
  }

  deleteUE(ue: UEModel) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'UE "${ue.nom}" ?`)) {
      this.ues.update(ues => ues.filter(u => u.id !== ue.id));
    }
  }

  // Filtres et recherche
  getFilteredUEs(): UEModel[] {
    let filtered = this.ues();

    // Filtre par terme de recherche
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(ue => 
        ue.nom.toLowerCase().includes(term) ||
        ue.code.toLowerCase().includes(term)
      );
    }

    // Filtre par école
    if (this.selectedEcole()) {
      filtered = filtered.filter(ue => ue.ecole === this.selectedEcole());
    }

    // Filtre par semestre
    if (this.selectedSemestre()) {
      filtered = filtered.filter(ue => ue.semestre === this.selectedSemestre());
    }

    // Filtre par classe
    if (this.selectedClasse()) {
      filtered = filtered.filter(ue => ue.classes.includes(this.selectedClasse()));
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

  getClasseNames(classeIds: string[]): string {
    if (!classeIds || classeIds.length === 0) {
      return '';
    }
    const classes = this.classes().filter(c => classeIds.includes(c.id));
    return classes.map(c => c.nom).join(', ');
  }

  getClasseName(classeId: string): string {
    const classe = this.classes().find(c => c.id === classeId);
    return classe ? classe.nom : 'Classe inconnue';
  }

  getAvailableClasses(): Classe[] {
    const form = this.ueForm();
    if (!form.ecole || !form.semestre) return [];
    
    return this.classes().filter(c => 
      c.ecole === form.ecole && c.semestre === form.semestre
    );
  }

  getClassesForFilter(): Classe[] {
    let filteredClasses = this.classes();
    
    // Filtre par école si sélectionnée
    if (this.selectedEcole()) {
      filteredClasses = filteredClasses.filter(c => c.ecole === this.selectedEcole());
    }
    
    // Filtre par semestre si sélectionné
    if (this.selectedSemestre()) {
      filteredClasses = filteredClasses.filter(c => c.semestre === this.selectedSemestre());
    }
    
    return filteredClasses;
  }

  getSemestresForEcole(ecoleId: string): number[] {
    const ecole = this.ecoles().find(e => e.id === ecoleId);
    return ecole ? ecole.semestres : [];
  }

  clearFilters() {
    this.searchTerm.set('');
    this.selectedEcole.set('');
    this.selectedSemestre.set(0);
    this.selectedClasse.set('');
  }

  exportUEs() {
    // Simulation d'export
    console.log('Export des UE:', this.getFilteredUEs());
    alert('Export réalisé avec succès !');
  }

  importUEs() {
    // Simulation d'import
    alert('Fonctionnalité d\'import à implémenter');
  }
}