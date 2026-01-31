import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface CoursModel {
  id: string;
  ueId: string; // Référence vers l'UE
  typeId: string; // Référence vers le type de cours
  professeurId?: string;
  classes: string[]; // IDs des classes associées
  duree: number; // en heures
  description?: string;
  statut: 'actif' | 'annule' | 'termine' | 'planifie';
  createdAt: Date;
  updatedAt: Date;
}

interface UEModel {
  id: string;
  code: string;
  nom: string;
  credits: number;
  semestre: number;
  ecole: string;
  classes: string[];
}

interface TypeCours {
  id: string;
  nom: string;
  code: string;
  description: string;
  couleur: string;
  dureeDefaut: number; // durée par défaut en heures
}

interface Professeur {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  specialites: string[];
}

interface Classe {
  id: string;
  nom: string;
  niveau: string;
  ecole: string;
  semestre: number;
  effectif: number;
}

interface Salle {
  id: string;
  nom: string;
  capacite: number;
  type: string;
  equipements: string[];
}

interface Ecole {
  id: string;
  nom: string;
  code: string;
  couleur: string;
}

@Component({
  selector: 'app-cours',
  standalone: true,
  imports: [CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './cours.html',
  styleUrl: './cours.scss'
})
export class Cours implements OnInit {
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
  protected readonly selectedType = signal('');
  protected readonly selectedStatut = signal('');
  protected readonly selectedProfesseur = signal('');
  protected readonly selectedClasse = signal('');
  protected readonly currentView = signal<'grid' | 'list'>('grid');

  // Données
  protected readonly cours = signal<CoursModel[]>([]);
  protected readonly ues = signal<UEModel[]>([]);
  protected readonly typesCours = signal<TypeCours[]>([]);
  protected readonly professeurs = signal<Professeur[]>([]);
  protected readonly classes = signal<Classe[]>([]);
  protected readonly salles = signal<Salle[]>([]);
  protected readonly ecoles = signal<Ecole[]>([]);
  protected readonly selectedCours = signal<CoursModel | null>(null);

  // Formulaire
  protected readonly coursForm = signal({
    id: '',
    ueId: '',
    typeId: '',
    professeurId: '',
    classes: [] as string[],
    duree: 4,
    description: '',
    statut: 'planifie' as 'actif' | 'annule' | 'termine' | 'planifie'
  });

  protected readonly errors = signal<{[key: string]: string}>({});

  ngOnInit() {
    this.loadInitialData();
  }

  private loadInitialData() {
    // Charger les écoles avec les mêmes styles que la section professeurs
    const ecoles: Ecole[] = [
      { id: 'sji', nom: 'Saint Jean Ingénieur', code: 'SJI', couleur: 'bg-blue-500' },
      { id: 'sjm', nom: 'Saint Jean Management', code: 'SJM', couleur: 'bg-green-500' },
      { id: 'prepa', nom: 'PrepaVogt', code: 'PV', couleur: 'bg-purple-500' },
      { id: 'cpge', nom: 'Classes Préparatoires', code: 'CPGE', couleur: 'bg-orange-500' }
    ];
    this.ecoles.set(ecoles);

    // Charger les types de cours
    this.loadTypesCours();
    
    // Charger les professeurs
    this.loadProfesseurs();
    
    // Charger les classes
    this.loadClasses();
    
    // Charger les salles (gardé pour compatibilité)
    this.loadSalles();
    
    // Charger les UE
    this.loadUEs();
    
    // Charger les cours
    this.loadCours();
  }

  private loadTypesCours() {
    const types: TypeCours[] = [
      {
        id: '1',
        nom: 'Cours Magistral',
        code: 'CM',
        description: 'Cours théorique en amphithéâtre',
        couleur: 'bg-blue-500',
        dureeDefaut: 30
      },
      {
        id: '2',
        nom: 'Travaux Dirigés',
        code: 'TD',
        description: 'Exercices dirigés en petits groupes',
        couleur: 'bg-green-500',
        dureeDefaut: 20
      },
      {
        id: '3',
        nom: 'Travaux Pratiques',
        code: 'TP',
        description: 'Travaux pratiques en laboratoire',
        couleur: 'bg-orange-500',
        dureeDefaut: 40
      },
      {
        id: '4',
        nom: 'Projet',
        code: 'PROJ',
        description: 'Projet encadré',
        couleur: 'bg-purple-500',
        dureeDefaut: 60
      },
      {
        id: '5',
        nom: 'Séminaire',
        code: 'SEM',
        description: 'Séminaire spécialisé',
        couleur: 'bg-indigo-500',
        dureeDefaut: 15
      }
    ];
    this.typesCours.set(types);
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

  private loadSalles() {
    const salles: Salle[] = [
      {
        id: '1',
        nom: 'Amphithéâtre A',
        capacite: 100,
        type: 'Amphithéâtre',
        equipements: ['Projecteur', 'Micro', 'Tableau']
      },
      {
        id: '2',
        nom: 'Salle 101',
        capacite: 40,
        type: 'Salle de cours',
        equipements: ['Projecteur', 'Tableau']
      },
      {
        id: '3',
        nom: 'Lab Info 1',
        capacite: 25,
        type: 'Laboratoire',
        equipements: ['Ordinateurs', 'Projecteur', 'Réseau']
      },
      {
        id: '4',
        nom: 'Salle TD 205',
        capacite: 30,
        type: 'Salle TD',
        equipements: ['Tableau', 'Projecteur']
      }
    ];
    this.salles.set(salles);
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
        classes: ['1', '2']
      },
      {
        id: '2',
        code: 'MAT101',
        nom: 'Mathématiques Fondamentales',
        credits: 6,
        semestre: 1,
        ecole: 'sji',
        classes: ['1', '2']
      },
      {
        id: '3',
        code: 'INF201',
        nom: 'Programmation Orientée Objet',
        credits: 6,
        semestre: 3,
        ecole: 'sji',
        classes: ['3']
      },
      {
        id: '4',
        code: 'GES101',
        nom: 'Principes de Gestion',
        credits: 4,
        semestre: 1,
        ecole: 'sjm',
        classes: ['4']
      },
      {
        id: '5',
        code: 'MKT201',
        nom: 'Marketing Digital',
        credits: 5,
        semestre: 3,
        ecole: 'sjm',
        classes: ['5']
      }
    ];
    this.ues.set(ues);
  }

  private loadCours() {
    const cours: CoursModel[] = [
      {
        id: '1',
        ueId: '1',
        typeId: '1', // CM
        professeurId: '1',
        classes: ['1', '2'], // Informatique L1A et L1B
        duree: 30,
        description: 'Cours magistral d\'introduction à la programmation',
        statut: 'actif',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '2',
        ueId: '1',
        typeId: '2', // TD
        professeurId: '1',
        classes: ['1'], // Informatique L1A
        duree: 20,
        description: 'Travaux dirigés de programmation',
        statut: 'actif',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '3',
        ueId: '1',
        typeId: '3', // TP
        professeurId: '1',
        classes: ['2'], // Informatique L1B
        duree: 40,
        description: 'Travaux pratiques de programmation',
        statut: 'actif',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '4',
        ueId: '2',
        typeId: '1', // CM
        professeurId: '2',
        classes: ['1', '2'], // Informatique L1A et L1B
        duree: 30,
        description: 'Cours magistral de mathématiques',
        statut: 'actif',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '5',
        ueId: '4',
        typeId: '1', // CM
        professeurId: '3',
        classes: ['7'], // Gestion L1
        duree: 25,
        description: 'Cours magistral de gestion',
        statut: 'planifie',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      }
    ];
    this.cours.set(cours);
  }

  // Gestion des modales
  openAddModal() {
    this.resetForm();
    this.showAddModal.set(true);
  }

  openEditModal(cours: CoursModel) {
    this.selectedCours.set(cours);
    this.coursForm.set({
      id: cours.id,
      ueId: cours.ueId,
      typeId: cours.typeId,
      professeurId: cours.professeurId || '',
      classes: [...cours.classes],
      duree: cours.duree,
      description: cours.description || '',
      statut: cours.statut
    });
    this.showEditModal.set(true);
  }

  openDetailsModal(cours: CoursModel) {
    this.selectedCours.set(cours);
    this.showDetailsModal.set(true);
  }

  closeModals() {
    this.showAddModal.set(false);
    this.showEditModal.set(false);
    this.showDetailsModal.set(false);
    this.selectedCours.set(null);
    this.resetForm();
  }

  private resetForm() {
    this.coursForm.set({
      id: '',
      ueId: '',
      typeId: '',
      professeurId: '',
      classes: [],
      duree: 4,
      description: '',
      statut: 'planifie'
    });
    this.errors.set({});
  }

  // Gestion du formulaire
  updateForm(field: string, value: any) {
    this.coursForm.update(form => ({
      ...form,
      [field]: value
    }));
    
    // Mettre à jour la durée par défaut si le type change
    if (field === 'typeId') {
      const type = this.typesCours().find(t => t.id === value);
      if (type) {
        this.coursForm.update(form => ({
          ...form,
          duree: type.dureeDefaut
        }));
      }
    }
    
    // Effacer l'erreur du champ
    if (this.errors()[field]) {
      this.errors.update(errors => {
        const newErrors = { ...errors };
        delete newErrors[field];
        return newErrors;
      });
    }
  }

  // Validation et soumission
  validateForm(): boolean {
    const form = this.coursForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.ueId) {
      newErrors['ueId'] = 'L\'UE est requise';
    }

    if (!form.typeId) {
      newErrors['typeId'] = 'Le type de cours est requis';
    }

    if (form.classes.length === 0) {
      newErrors['classes'] = 'Au moins une classe doit être sélectionnée';
    }

    if (form.duree < 4) {
      newErrors['duree'] = 'La durée doit être d\'au moins 4 heures';
    }

    if (form.duree > 250) {
      newErrors['duree'] = 'La durée ne peut pas dépasser 250 heures';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  onSubmit() {
    if (!this.validateForm()) return;

    this.isLoading.set(true);
    const form = this.coursForm();

    setTimeout(() => {
      if (form.id) {
        // Modification
        this.cours.update(cours => 
          cours.map(c => 
            c.id === form.id 
              ? { 
                  ...c, 
                  ...form, 
                  updatedAt: new Date() 
                }
              : c
          )
        );
      } else {
        // Ajout
        const newCours: CoursModel = {
          ...form,
          id: Date.now().toString(),
          createdAt: new Date(),
          updatedAt: new Date()
        };
        this.cours.update(cours => [...cours, newCours]);
      }

      this.isLoading.set(false);
      this.closeModals();
    }, 1000);
  }

  deleteCours(cours: CoursModel) {
    const ue = this.getUE(cours.ueId);
    const type = this.getTypeCours(cours.typeId);
    const coursName = `${ue?.nom} (${type?.code})`;
    
    if (confirm(`Êtes-vous sûr de vouloir supprimer le cours "${coursName}" ?`)) {
      this.cours.update(coursList => coursList.filter(c => c.id !== cours.id));
    }
  }

  // Filtres et recherche
  getFilteredCours(): CoursModel[] {
    let filtered = this.cours();

    // Filtre par terme de recherche
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(cours => {
        const ue = this.getUE(cours.ueId);
        const type = this.getTypeCours(cours.typeId);
        const prof = this.getProfesseur(cours.professeurId);
        const classesNames = this.getClasseNames(cours.classes);
        
        return (
          ue?.nom.toLowerCase().includes(term) ||
          ue?.code.toLowerCase().includes(term) ||
          type?.nom.toLowerCase().includes(term) ||
          type?.code.toLowerCase().includes(term) ||
          prof?.nom.toLowerCase().includes(term) ||
          prof?.prenom.toLowerCase().includes(term) ||
          classesNames.toLowerCase().includes(term) ||
          (cours.description && cours.description.toLowerCase().includes(term))
        );
      });
    }

    // Filtre par école
    if (this.selectedEcole()) {
      filtered = filtered.filter(cours => {
        const ue = this.getUE(cours.ueId);
        return ue?.ecole === this.selectedEcole();
      });
    }

    // Filtre par semestre
    if (this.selectedSemestre()) {
      filtered = filtered.filter(cours => {
        const ue = this.getUE(cours.ueId);
        return ue?.semestre === this.selectedSemestre();
      });
    }

    // Filtre par type
    if (this.selectedType()) {
      filtered = filtered.filter(cours => cours.typeId === this.selectedType());
    }

    // Filtre par statut
    if (this.selectedStatut()) {
      filtered = filtered.filter(cours => cours.statut === this.selectedStatut());
    }

    // Filtre par professeur
    if (this.selectedProfesseur()) {
      filtered = filtered.filter(cours => cours.professeurId === this.selectedProfesseur());
    }

    // Filtre par classe
    if (this.selectedClasse()) {
      filtered = filtered.filter(cours => cours.classes.includes(this.selectedClasse()));
    }

    return filtered;
  }

  // Utilitaires
  getUE(ueId?: string): UEModel | undefined {
    if (!ueId) return undefined;
    return this.ues().find(ue => ue.id === ueId);
  }

  getTypeCours(typeId?: string): TypeCours | undefined {
    if (!typeId) return undefined;
    return this.typesCours().find(type => type.id === typeId);
  }

  getProfesseur(professeurId?: string): Professeur | undefined {
    if (!professeurId) return undefined;
    return this.professeurs().find(prof => prof.id === professeurId);
  }

  getClasse(classeId?: string): Classe | undefined {
    if (!classeId) return undefined;
    return this.classes().find(classe => classe.id === classeId);
  }

  getClasseNames(classeIds: string[]): string {
    const classes = this.classes().filter(c => classeIds.includes(c.id));
    return classes.map(c => c.nom).join(', ') || 'Aucune classe';
  }

  getFilteredClasses(): Classe[] {
    let filtered = this.classes();
    
    // Filtre par école sélectionnée
    if (this.selectedEcole()) {
      filtered = filtered.filter(c => c.ecole === this.selectedEcole());
    }
    
    // Filtre par semestre sélectionné
    if (this.selectedSemestre()) {
      filtered = filtered.filter(c => c.semestre === this.selectedSemestre());
    }
    
    return filtered;
  }

  getAvailableClasses(): Classe[] {
    const form = this.coursForm();
    if (!form.ueId) return [];
    
    const ue = this.getUE(form.ueId);
    if (!ue) return [];
    
    return this.classes().filter(c => 
      c.ecole === ue.ecole && c.semestre === ue.semestre
    );
  }

  toggleClasse(classeId: string) {
    this.coursForm.update(form => {
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

  getSalle(salleId?: string): Salle | undefined {
    if (!salleId) return undefined;
    return this.salles().find(salle => salle.id === salleId);
  }

  getEcole(ecoleId: string): Ecole | undefined {
    return this.ecoles().find(ecole => ecole.id === ecoleId);
  }

  getCoursName(cours: CoursModel): string {
    const ue = this.getUE(cours.ueId);
    const type = this.getTypeCours(cours.typeId);
    return `${ue?.nom || 'UE inconnue'} (${type?.code || 'Type inconnu'})`;
  }

  getCoursCode(cours: CoursModel): string {
    const ue = this.getUE(cours.ueId);
    const type = this.getTypeCours(cours.typeId);
    return `${ue?.code || 'UE'}-${type?.code || 'TYPE'}`;
  }

  getStatutColor(statut: string): string {
    const colors = {
      actif: 'bg-green-100 text-green-800',
      planifie: 'bg-blue-100 text-blue-800',
      annule: 'bg-red-100 text-red-800',
      termine: 'bg-gray-100 text-gray-800'
    };
    return colors[statut as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  }

  getStatutLabel(statut: string): string {
    const labels = {
      actif: 'Actif',
      planifie: 'Planifié',
      annule: 'Annulé',
      termine: 'Terminé'
    };
    return labels[statut as keyof typeof labels] || statut;
  }

  formatDuree(heures: number): string {
    if (heures >= 1) {
      return heures % 1 === 0 ? `${heures}h` : `${heures}h`;
    }
    return `${heures}h`;
  }

  clearFilters() {
    this.searchTerm.set('');
    this.selectedEcole.set('');
    this.selectedSemestre.set(0);
    this.selectedType.set('');
    this.selectedStatut.set('');
    this.selectedProfesseur.set('');
    this.selectedClasse.set('');
  }

  exportCours() {
    console.log('Export des cours:', this.getFilteredCours());
    alert('Export réalisé avec succès !');
  }

  importCours() {
    alert('Fonctionnalité d\'import à implémenter');
  }

  // Statistiques
  getTotalCours(): number {
    return this.cours().length;
  }

  getCoursActifs(): number {
    return this.cours().filter(c => c.statut === 'actif').length;
  }

  getStatistiquesParType() {
    const stats = this.typesCours().map(type => {
      const coursType = this.cours().filter(c => c.typeId === type.id);
      return {
        type: type.nom,
        code: type.code,
        couleur: type.couleur,
        count: coursType.length
      };
    });
    return stats.filter(s => s.count > 0);
  }
}
