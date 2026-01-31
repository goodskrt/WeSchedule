import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface RoomForm {
  name: string;
  type: 'cours' | 'td' | 'labo' | 'info' | 'conference';
  capacity: number;
  building: 'ancien' | 'nouveau';
  floor: 'rez' | 'un' | 'deux' | 'trois';
  equipment: EquipmentItem[];
  description: string;
  status: 'available' | 'occupied' | 'maintenance';
}

interface EquipmentItem {
  id: string;
  quantity: number;
}

interface Equipment {
  id: string;
  name: string;
  icon: string;
  category: string;
}

@Component({
  selector: 'app-ajouter-salle',
  imports: [CommonModule, FormsModule],
  templateUrl: './ajouter-salle.html',
  styleUrl: './ajouter-salle.scss',
})
export class AjouterSalle implements OnInit, OnDestroy {
  @Input() room: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() roomAdded = new EventEmitter<any>();

  protected readonly roomForm = signal<RoomForm>({
    name: '',
    type: 'cours',
    capacity: 30,
    building: 'ancien',
    floor: 'rez',
    equipment: [],
    description: '',
    status: 'available'
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});
  protected readonly currentStep = signal<1 | 2 | 3>(1);

  protected readonly buildings = [
    { id: 'ancien', name: 'Ancien Bâtiment' },
    { id: 'nouveau', name: 'Nouveau Bâtiment' }
  ];

  protected readonly floors = [
    { id: 'rez', name: 'Rez de chaussée' },
    { id: 'un', name: 'Premier étage' },
    { id: 'deux', name: 'Deuxième étage' },
    { id: 'trois', name: 'Troisième étage' }
  ];

  protected readonly roomTypes = [
    { id: 'cours', name: 'Salle de cours', icon: '🏫', description: 'Salle standard pour cours magistraux' },
    { id: 'td', name: 'Salle TD', icon: '📚', description: 'Salle pour travaux dirigés en petits groupes' },
    { id: 'labo', name: 'Laboratoire', icon: '🔬', description: 'Laboratoire pour travaux pratiques' },
    { id: 'info', name: 'Salle informatique', icon: '💻', description: 'Salle équipée d\'ordinateurs' },
    { id: 'conference', name: 'Salle de conférence', icon: '🏢', description: 'Salle équipée pour réunions et présentations' }
  ];

  protected readonly equipmentList = signal<Equipment[]>([
    { id: 'projector', name: 'Projecteur', icon: '📽️', category: 'Audiovisuel' },
    { id: 'computer', name: 'Ordinateurs', icon: '💻', category: 'Informatique' },
    { id: 'whiteboard', name: 'Tableau blanc', icon: '📋', category: 'Écriture' },
    { id: 'smartboard', name: 'Tableau interactif', icon: '📱', category: 'Écriture' },
    { id: 'speakers', name: 'Haut-parleurs', icon: '🔊', category: 'Audiovisuel' },
    { id: 'microphone', name: 'Microphone', icon: '🎤', category: 'Audiovisuel' },
    { id: 'camera', name: 'Caméra', icon: '📹', category: 'Audiovisuel' },
    { id: 'printer', name: 'Imprimante', icon: '🖨️', category: 'Informatique' },
    { id: 'scanner', name: 'Scanner', icon: '📄', category: 'Informatique' },
    { id: 'airconditioner', name: 'Climatisation', icon: '❄️', category: 'Confort' },
    { id: 'wifi', name: 'WiFi', icon: '📶', category: 'Connectivité' },
    { id: 'ethernet', name: 'Ethernet', icon: '🔌', category: 'Connectivité' }
  ]);

  ngOnInit() {
    if (this.room) {
      this.roomForm.set({
        name: this.room.name || '',
        type: this.room.type || 'cours',
        capacity: this.room.capacity || 30,
        building: this.room.building || 'ancien',
        floor: this.room.floor || 'rez',
        equipment: [...(this.room.equipment || [])],
        description: this.room.description || '',
        status: this.room.status || 'available'
      });
    }
    
    // Bloquer le scroll de la page principale
    document.body.classList.add('modal-open');
  }

  ngOnDestroy() {
    // Restaurer le scroll de la page principale
    document.body.classList.remove('modal-open');
  }

  updateForm(field: keyof RoomForm, value: any) {
    this.roomForm.update(form => ({
      ...form,
      [field]: value
    }));
    
    // Clear error when user starts typing
    if (this.errors()[field]) {
      this.errors.update(errors => {
        const newErrors = { ...errors };
        delete newErrors[field];
        return newErrors;
      });
    }
  }

  onEquipmentChange(equipmentId: string, quantity: number) {
    this.roomForm.update(form => {
      const equipment = [...form.equipment];
      const existingIndex = equipment.findIndex(e => e.id === equipmentId);
      
      if (quantity > 0) {
        if (existingIndex > -1) {
          equipment[existingIndex].quantity = quantity;
        } else {
          equipment.push({ id: equipmentId, quantity });
        }
      } else {
        if (existingIndex > -1) {
          equipment.splice(existingIndex, 1);
        }
      }
      
      return { ...form, equipment };
    });
  }

  getEquipmentQuantity(equipmentId: string): number {
    const equipment = this.roomForm().equipment.find(e => e.id === equipmentId);
    return equipment ? equipment.quantity : 0;
  }

  getEquipmentsByCategory() {
    const equipment = this.equipmentList();
    const categories = [...new Set(equipment.map(e => e.category))];
    
    return categories.map(category => ({
      name: category,
      items: equipment.filter(e => e.category === category)
    }));
  }

  getRoomTypeInfo(type: string) {
    return this.roomTypes.find(t => t.id === type);
  }

  validateStep(step: number): boolean {
    const form = this.roomForm();
    const newErrors: {[key: string]: string} = {};

    if (step === 1) {
      if (!form.name.trim()) newErrors['name'] = 'Le nom de la salle est requis';
    }

    if (step === 2) {
      if (form.capacity < 10) newErrors['capacity'] = 'La capacité doit être d\'au moins 10 places';
      if (form.capacity > 300) newErrors['capacity'] = 'La capacité ne peut pas dépasser 300 places';
    }

    // Step 3 has no required validations (equipment is optional)

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  nextStep() {
    if (this.validateStep(this.currentStep())) {
      this.currentStep.update(step => Math.min(step + 1, 3) as 1 | 2 | 3);
    }
  }

  previousStep() {
    this.currentStep.update(step => Math.max(step - 1, 1) as 1 | 2 | 3);
  }

  onSubmit() {
    if (!this.validateStep(3)) return;

    this.isLoading.set(true);
    
    // Simulation d'ajout/modification de salle
    setTimeout(() => {
      this.isLoading.set(false);
      const action = this.room ? 'modifiée' : 'ajoutée';
      alert(`Salle ${action} avec succès !`);
      this.roomAdded.emit(this.roomForm());
      this.resetForm();
    }, 2000);
  }

  resetForm() {
    this.roomForm.set({
      name: '',
      type: 'cours',
      capacity: 30,
      building: 'ancien',
      floor: 'rez',
      equipment: [],
      description: '',
      status: 'available'
    });
    this.errors.set({});
    this.currentStep.set(1);
  }

  onClose() {
    // Restaurer le scroll avant de fermer
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  getModalTitle(): string {
    const baseTitle = this.room ? 'Modifier la Salle' : 'Ajouter une Salle';
    return `${baseTitle} - Étape ${this.currentStep()}/3`;
  }

  getStepTitle(): string {
    switch (this.currentStep()) {
      case 1: return 'Informations de base';
      case 2: return 'Configuration';
      case 3: return 'Équipements';
      default: return '';
    }
  }

  getSubmitButtonText(): string {
    if (this.isLoading()) {
      return this.room ? 'Modification en cours...' : 'Ajout en cours...';
    }
    return this.room ? 'Modifier la salle' : 'Ajouter la salle';
  }

  onTypeChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('type', target.value);
  }

  onBuildingChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('building', target.value);
  }

  onFloorChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('floor', target.value);
  }

  onStatusChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.updateForm('status', target.value);
  }

  onDescriptionChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.updateForm('description', target.value);
  }

  getSelectedEquipmentCount(): number {
    return this.roomForm().equipment.length;
  }

  isStep1Valid(): boolean {
    const form = this.roomForm();
    return form.name.trim() !== '';
  }

  isStep2Valid(): boolean {
    const form = this.roomForm();
    return form.capacity >= 10 && form.capacity <= 300;
  }
}