import { Component, signal, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface EquipmentModel {
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

interface EquipmentCategory {
  id: string;
  name: string;
  icon: string;
  color: string;
}

interface EquipmentAssignment {
  id: string;
  equipmentId: string;
  assignmentType: 'room' | 'class';
  targetId: string; // roomId or classId
  quantity: number;
  startDate: string;
  endDate?: string; // Optional for permanent assignments
  duration: 'permanent' | 'temporary';
  reason: string;
  status: 'active' | 'expired' | 'cancelled';
  assignedBy: string;
  assignedAt: Date;
  notes?: string;
}

interface RoomInfo {
  id: string;
  name: string;
  building: string;
  floor: string;
  capacity: number;
}

interface ClassInfo {
  id: string;
  name: string;
  niveau: string;
  ecole: string;
  effectif: number;
}

@Component({
  selector: 'app-equipements',
  standalone: true,
  imports: [CommonModule, FormsModule, SvgIconComponent],
  templateUrl: './equipements.html',
  styleUrl: './equipements.scss'
})
export class EquipementsComponent implements OnInit {
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  // Signals pour l'état du composant
  protected readonly showAddModal = signal(false);
  protected readonly showEditModal = signal(false);
  protected readonly showDetailsModal = signal(false);
  protected readonly showAssignModal = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly searchTerm = signal('');
  protected readonly selectedCategory = signal('');
  protected readonly selectedStatus = signal('');
  protected readonly currentView = signal<'grid' | 'list'>('grid');

  // Données
  protected readonly equipments = signal<EquipmentModel[]>([]);
  protected readonly categories = signal<EquipmentCategory[]>([]);
  protected readonly assignments = signal<EquipmentAssignment[]>([]);
  protected readonly rooms = signal<RoomInfo[]>([]);
  protected readonly classes = signal<ClassInfo[]>([]);
  protected readonly selectedEquipment = signal<EquipmentModel | null>(null);

  // Formulaire
  protected readonly equipmentForm = signal({
    id: '',
    name: '',
    category: '',
    icon: '📦',
    description: '',
    totalQuantity: 1,
    status: 'active' as 'active' | 'maintenance' | 'retired'
  });

  protected readonly errors = signal<{[key: string]: string}>({});

  // Formulaire d'affectation
  protected readonly assignmentForm = signal({
    assignmentType: 'room' as 'room' | 'class',
    targetId: '',
    quantity: 1,
    duration: 'permanent' as 'permanent' | 'temporary',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    reason: '',
    notes: ''
  });

  ngOnInit() {
    this.loadInitialData();
  }

  private loadInitialData() {
    // Charger les catégories d'équipements
    const categories: EquipmentCategory[] = [
      { id: 'audiovisuel', name: 'Audiovisuel', icon: '📽️', color: 'bg-blue-500' },
      { id: 'informatique', name: 'Informatique', icon: '💻', color: 'bg-indigo-500' },
      { id: 'ecriture', name: 'Écriture', icon: '📋', color: 'bg-green-500' },
      { id: 'confort', name: 'Confort', icon: '❄️', color: 'bg-cyan-500' },
      { id: 'connectivite', name: 'Connectivité', icon: '📶', color: 'bg-purple-500' },
      { id: 'mobilier', name: 'Mobilier', icon: '🪑', color: 'bg-orange-500' }
    ];
    this.categories.set(categories);

    // Charger les équipements
    this.loadEquipments();
    
    // Charger les salles et classes
    this.loadRoomsAndClasses();
    
    // Charger les affectations
    this.loadAssignments();
  }

  private loadEquipments() {
    const equipments: EquipmentModel[] = [
      {
        id: '1',
        name: 'Projecteur',
        category: 'audiovisuel',
        icon: '📽️',
        description: 'Projecteur haute définition pour présentations',
        totalQuantity: 25,
        availableQuantity: 18,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '2',
        name: 'Ordinateurs',
        category: 'informatique',
        icon: '💻',
        description: 'Ordinateurs de bureau pour salles informatiques',
        totalQuantity: 120,
        availableQuantity: 95,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '3',
        name: 'Tableau blanc',
        category: 'ecriture',
        icon: '📋',
        description: 'Tableau blanc effaçable pour écriture',
        totalQuantity: 45,
        availableQuantity: 32,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '4',
        name: 'Tableau interactif',
        category: 'ecriture',
        icon: '📱',
        description: 'Tableau numérique interactif tactile',
        totalQuantity: 15,
        availableQuantity: 12,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '5',
        name: 'Haut-parleurs',
        category: 'audiovisuel',
        icon: '🔊',
        description: 'Système audio pour amplification sonore',
        totalQuantity: 30,
        availableQuantity: 22,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '6',
        name: 'Microphone',
        category: 'audiovisuel',
        icon: '🎤',
        description: 'Microphone sans fil pour présentations',
        totalQuantity: 20,
        availableQuantity: 15,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '7',
        name: 'Caméra',
        category: 'audiovisuel',
        icon: '📹',
        description: 'Caméra pour enregistrement et visioconférence',
        totalQuantity: 10,
        availableQuantity: 8,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '8',
        name: 'Imprimante',
        category: 'informatique',
        icon: '🖨️',
        description: 'Imprimante laser couleur',
        totalQuantity: 12,
        availableQuantity: 9,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '9',
        name: 'Scanner',
        category: 'informatique',
        icon: '📄',
        description: 'Scanner de documents haute résolution',
        totalQuantity: 8,
        availableQuantity: 6,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '10',
        name: 'Climatisation',
        category: 'confort',
        icon: '❄️',
        description: 'Système de climatisation réversible',
        totalQuantity: 35,
        availableQuantity: 28,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '11',
        name: 'WiFi',
        category: 'connectivite',
        icon: '📶',
        description: 'Point d\'accès WiFi haute performance',
        totalQuantity: 50,
        availableQuantity: 42,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      },
      {
        id: '12',
        name: 'Ethernet',
        category: 'connectivite',
        icon: '🔌',
        description: 'Prises réseau Ethernet gigabit',
        totalQuantity: 200,
        availableQuantity: 165,
        status: 'active',
        createdAt: new Date('2024-01-15'),
        updatedAt: new Date('2024-01-15')
      }
    ];
    this.equipments.set(equipments);
  }

  private loadRoomsAndClasses() {
    // Charger les salles disponibles
    const rooms: RoomInfo[] = [
      { id: '1', name: 'Salle 101', building: 'Ancien Bâtiment', floor: 'Premier étage', capacity: 35 },
      { id: '2', name: 'Lab Info 2', building: 'Nouveau Bâtiment', floor: 'Deuxième étage', capacity: 28 },
      { id: '3', name: 'Amphithéâtre A', building: 'Nouveau Bâtiment', floor: 'Rez de chaussée', capacity: 150 },
      { id: '4', name: 'Salle TD 205', building: 'Ancien Bâtiment', floor: 'Deuxième étage', capacity: 25 },
      { id: '5', name: 'Lab Chimie 1', building: 'Ancien Bâtiment', floor: 'Premier étage', capacity: 20 },
      { id: '6', name: 'Salle Conférence B', building: 'Nouveau Bâtiment', floor: 'Troisième étage', capacity: 80 }
    ];
    this.rooms.set(rooms);

    // Charger les classes disponibles
    const classes: ClassInfo[] = [
      { id: '1', name: 'Informatique L1A', niveau: 'L1', ecole: 'SJI', effectif: 45 },
      { id: '2', name: 'Informatique L1B', niveau: 'L1', ecole: 'SJI', effectif: 42 },
      { id: '3', name: 'Informatique L2', niveau: 'L2', ecole: 'SJI', effectif: 38 },
      { id: '4', name: 'Gestion L1', niveau: 'L1', ecole: 'SJM', effectif: 50 },
      { id: '5', name: 'Marketing L2', niveau: 'L2', ecole: 'SJM', effectif: 35 },
      { id: '6', name: 'Prépa Scientifique 1A', niveau: 'Prépa', ecole: 'PV', effectif: 30 },
      { id: '7', name: 'MPSI', niveau: 'CPGE', ecole: 'CPGE', effectif: 35 }
    ];
    this.classes.set(classes);
  }

  private loadAssignments() {
    // Simuler des affectations d'équipements
    const assignments: EquipmentAssignment[] = [
      {
        id: '1',
        equipmentId: '1', // Projecteur
        assignmentType: 'room',
        targetId: '1', // Salle 101
        quantity: 1,
        startDate: '2024-01-15',
        duration: 'permanent',
        reason: 'Équipement fixe de la salle',
        status: 'active',
        assignedBy: 'Admin',
        assignedAt: new Date('2024-01-15'),
        notes: 'Projecteur installé au plafond'
      },
      {
        id: '2',
        equipmentId: '2', // Ordinateurs
        assignmentType: 'room',
        targetId: '2', // Lab Info 2
        quantity: 30,
        startDate: '2024-01-15',
        duration: 'permanent',
        reason: 'Équipement de laboratoire informatique',
        status: 'active',
        assignedBy: 'Admin',
        assignedAt: new Date('2024-01-15')
      },
      {
        id: '3',
        equipmentId: '5', // Haut-parleurs
        assignmentType: 'class',
        targetId: '1', // Informatique L1A
        quantity: 2,
        startDate: '2024-01-20',
        endDate: '2024-06-30',
        duration: 'temporary',
        reason: 'Présentation de projet de fin de semestre',
        status: 'active',
        assignedBy: 'Prof. Martin',
        assignedAt: new Date('2024-01-20'),
        notes: 'Pour les soutenances de projets'
      },
      {
        id: '4',
        equipmentId: '6', // Microphones
        assignmentType: 'class',
        targetId: '4', // Gestion L1
        quantity: 1,
        startDate: '2024-02-01',
        endDate: '2024-02-15',
        duration: 'temporary',
        reason: 'Cours de communication orale',
        status: 'active',
        assignedBy: 'Prof. Sophie',
        assignedAt: new Date('2024-02-01')
      }
    ];
    this.assignments.set(assignments);
  }

  // Gestion des modales
  openAddModal() {
    this.resetForm();
    this.showAddModal.set(true);
  }

  openEditModal(equipment: EquipmentModel) {
    this.selectedEquipment.set(equipment);
    this.equipmentForm.set({
      id: equipment.id,
      name: equipment.name,
      category: equipment.category,
      icon: equipment.icon,
      description: equipment.description || '',
      totalQuantity: equipment.totalQuantity,
      status: equipment.status
    });
    this.showEditModal.set(true);
  }

  openDetailsModal(equipment: EquipmentModel) {
    this.selectedEquipment.set(equipment);
    this.showDetailsModal.set(true);
  }

  openAssignModal(equipment: EquipmentModel) {
    // Fermer d'abord tous les modals
    this.showDetailsModal.set(false);
    this.showEditModal.set(false);
    this.showAddModal.set(false);
    
    // Configurer l'équipement sélectionné et le formulaire
    this.selectedEquipment.set(equipment);
    this.assignmentForm.set({
      assignmentType: 'room',
      targetId: '',
      quantity: 1,
      duration: 'permanent',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '',
      reason: '',
      notes: ''
    });
    
    // Ouvrir le modal d'affectation
    this.showAssignModal.set(true);
  }

  closeModals() {
    this.showAddModal.set(false);
    this.showEditModal.set(false);
    this.showDetailsModal.set(false);
    this.showAssignModal.set(false);
    this.selectedEquipment.set(null);
    this.resetForm();
    this.resetAssignmentForm();
  }

  private resetAssignmentForm() {
    this.assignmentForm.set({
      assignmentType: 'room',
      targetId: '',
      quantity: 1,
      duration: 'permanent',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '',
      reason: '',
      notes: ''
    });
  }

  private resetForm() {
    this.equipmentForm.set({
      id: '',
      name: '',
      category: '',
      icon: '📦',
      description: '',
      totalQuantity: 1,
      status: 'active'
    });
    this.errors.set({});
  }

  // Gestion du formulaire
  updateForm(field: string, value: any) {
    this.equipmentForm.update(form => ({
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

  // Validation et soumission
  validateForm(): boolean {
    const form = this.equipmentForm();
    const newErrors: {[key: string]: string} = {};

    if (!form.name.trim()) {
      newErrors['name'] = 'Le nom de l\'équipement est requis';
    }

    if (!form.category) {
      newErrors['category'] = 'La catégorie est requise';
    }

    if (form.totalQuantity < 1) {
      newErrors['totalQuantity'] = 'La quantité doit être supérieure à 0';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  onSubmit() {
    if (!this.validateForm()) return;

    this.isLoading.set(true);
    const form = this.equipmentForm();

    setTimeout(() => {
      if (form.id) {
        // Modification
        this.equipments.update(equipments => 
          equipments.map(equipment => 
            equipment.id === form.id 
              ? { 
                  ...equipment, 
                  name: form.name,
                  category: form.category,
                  icon: form.icon,
                  description: form.description,
                  totalQuantity: form.totalQuantity,
                  status: form.status,
                  availableQuantity: equipment.availableQuantity + (form.totalQuantity - equipment.totalQuantity),
                  updatedAt: new Date() 
                }
              : equipment
          )
        );
      } else {
        // Ajout
        const newEquipment: EquipmentModel = {
          id: Date.now().toString(),
          name: form.name,
          category: form.category,
          icon: form.icon,
          description: form.description,
          totalQuantity: form.totalQuantity,
          availableQuantity: form.totalQuantity,
          status: form.status,
          createdAt: new Date(),
          updatedAt: new Date()
        };
        this.equipments.update(equipments => [...equipments, newEquipment]);
      }

      this.isLoading.set(false);
      this.closeModals();
    }, 1000);
  }

  deleteEquipment(equipment: EquipmentModel) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'équipement "${equipment.name}" ?`)) {
      this.equipments.update(equipments => equipments.filter(e => e.id !== equipment.id));
    }
  }

  // Filtres et recherche
  getFilteredEquipments(): EquipmentModel[] {
    let filtered = this.equipments();

    // Filtre par terme de recherche
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(equipment => 
        equipment.name.toLowerCase().includes(term) ||
        (equipment.description && equipment.description.toLowerCase().includes(term)) ||
        this.getCategoryName(equipment.category).toLowerCase().includes(term)
      );
    }

    // Filtre par catégorie
    if (this.selectedCategory()) {
      filtered = filtered.filter(equipment => equipment.category === this.selectedCategory());
    }

    // Filtre par statut
    if (this.selectedStatus()) {
      filtered = filtered.filter(equipment => equipment.status === this.selectedStatus());
    }

    return filtered;
  }

  // Utilitaires
  getCategoryName(categoryId: string): string {
    const category = this.categories().find(c => c.id === categoryId);
    return category ? category.name : 'Catégorie inconnue';
  }

  getCategory(categoryId: string): EquipmentCategory | undefined {
    return this.categories().find(c => c.id === categoryId);
  }

  getStatusColor(status: string): string {
    const colors = {
      active: 'bg-green-100 text-green-800',
      maintenance: 'bg-yellow-100 text-yellow-800',
      retired: 'bg-red-100 text-red-800'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels = {
      active: 'Actif',
      maintenance: 'Maintenance',
      retired: 'Retiré'
    };
    return labels[status as keyof typeof labels] || status;
  }

  getAvailabilityPercentage(equipment: EquipmentModel): number {
    return Math.round((equipment.availableQuantity / equipment.totalQuantity) * 100);
  }

  getAvailabilityStatus(equipment: EquipmentModel): 'high' | 'medium' | 'low' | 'empty' {
    const percentage = this.getAvailabilityPercentage(equipment);
    if (percentage === 0) return 'empty';
    if (percentage < 25) return 'low';
    if (percentage < 75) return 'medium';
    return 'high';
  }

  getAvailabilityStatusColor(status: string): string {
    const colors = {
      high: 'bg-green-100 text-green-800',
      medium: 'bg-yellow-100 text-yellow-800',
      low: 'bg-orange-100 text-orange-800',
      empty: 'bg-red-100 text-red-800'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  }

  clearFilters() {
    this.searchTerm.set('');
    this.selectedCategory.set('');
    this.selectedStatus.set('');
  }

  exportEquipments() {
    console.log('Export des équipements:', this.getFilteredEquipments());
    alert('Export réalisé avec succès !');
  }

  importEquipments() {
    alert('Fonctionnalité d\'import à implémenter');
  }

  // Statistiques
  getTotalEquipments(): number {
    return this.equipments().reduce((total, equipment) => total + equipment.totalQuantity, 0);
  }

  getAvailableEquipments(): number {
    return this.equipments().reduce((total, equipment) => total + equipment.availableQuantity, 0);
  }

  getAssignedEquipments(): number {
    return this.getTotalEquipments() - this.getAvailableEquipments();
  }

  getEquipmentsByCategory() {
    const equipments = this.equipments();
    return this.categories().map(category => ({
      category: category.name,
      color: category.color,
      count: equipments.filter(e => e.category === category.id).length,
      totalQuantity: equipments.filter(e => e.category === category.id).reduce((sum, e) => sum + e.totalQuantity, 0)
    }));
  }

  getEquipmentsByStatus() {
    const equipments = this.equipments();
    const statuses = ['active', 'maintenance', 'retired'];
    return statuses.map(status => ({
      status,
      label: this.getStatusLabel(status),
      count: equipments.filter(e => e.status === status).length,
      color: this.getStatusColor(status)
    }));
  }

  // Assignment form methods
  updateAssignmentForm(field: string, value: any) {
    this.assignmentForm.update(form => ({
      ...form,
      [field]: value
    }));
  }

  validateAssignmentForm(): boolean {
    const form = this.assignmentForm();
    const equipment = this.selectedEquipment();
    
    if (!equipment) return false;
    if (!form.targetId) return false;
    if (!form.reason.trim()) return false;
    if (form.quantity < 1) return false;
    if (form.quantity > equipment.availableQuantity) return false;
    if (form.duration === 'temporary' && !form.endDate) return false;
    if (form.endDate && form.endDate <= form.startDate) return false;
    
    return true;
  }

  submitAssignment() {
    if (!this.validateAssignmentForm() || !this.selectedEquipment()) return;
    
    const form = this.assignmentForm();
    const equipment = this.selectedEquipment()!;
    
    const newAssignment: EquipmentAssignment = {
      id: Date.now().toString(),
      equipmentId: equipment.id,
      assignmentType: form.assignmentType,
      targetId: form.targetId,
      quantity: form.quantity,
      startDate: form.startDate,
      endDate: form.duration === 'temporary' ? form.endDate : undefined,
      duration: form.duration,
      reason: form.reason,
      status: 'active',
      assignedBy: 'Utilisateur actuel', // À remplacer par l'utilisateur connecté
      assignedAt: new Date(),
      notes: form.notes
    };
    
    // Ajouter l'affectation
    this.assignments.update(assignments => [...assignments, newAssignment]);
    
    // Mettre à jour la quantité disponible de l'équipement
    this.equipments.update(equipments =>
      equipments.map(eq =>
        eq.id === equipment.id
          ? { ...eq, availableQuantity: eq.availableQuantity - form.quantity }
          : eq
      )
    );
    
    alert('Affectation créée avec succès !');
    this.closeModals();
  }

  getTargetName(assignment: EquipmentAssignment): string {
    if (assignment.assignmentType === 'room') {
      const room = this.rooms().find(r => r.id === assignment.targetId);
      return room ? room.name : 'Salle inconnue';
    } else {
      const classe = this.classes().find(c => c.id === assignment.targetId);
      return classe ? classe.name : 'Classe inconnue';
    }
  }

  getAssignmentsForEquipment(equipmentId: string): EquipmentAssignment[] {
    return this.assignments().filter(a => a.equipmentId === equipmentId && a.status === 'active');
  }

  cancelAssignment(assignment: EquipmentAssignment) {
    if (confirm('Êtes-vous sûr de vouloir annuler cette affectation ?')) {
      // Marquer l'affectation comme annulée
      this.assignments.update(assignments =>
        assignments.map(a =>
          a.id === assignment.id
            ? { ...a, status: 'cancelled' as const }
            : a
        )
      );
      
      // Remettre la quantité dans le stock disponible
      this.equipments.update(equipments =>
        equipments.map(eq =>
          eq.id === assignment.equipmentId
            ? { ...eq, availableQuantity: eq.availableQuantity + assignment.quantity }
            : eq
        )
      );
      
      alert('Affectation annulée avec succès !');
    }
  }

  getAvailableTargets(): (RoomInfo | ClassInfo)[] {
    const form = this.assignmentForm();
    if (form.assignmentType === 'room') {
      return this.rooms();
    } else {
      return this.classes();
    }
  }

  getDurationLabel(duration: string): string {
    return duration === 'permanent' ? 'Permanente' : 'Temporaire';
  }

  getAssignmentTypeLabel(type: string): string {
    return type === 'room' ? 'Salle' : 'Classe';
  }

  getRoomBuilding(target: any): string {
    return target.building || '';
  }

  getClassLevel(target: any): string {
    return target.niveau || '';
  }
}