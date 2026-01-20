import { Component, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface RoomForm {
  name: string;
  type: 'classroom' | 'lab' | 'amphitheater' | 'conference';
  capacity: number;
  building: string;
  floor: number;
  equipment: string[];
  description: string;
  status: 'available' | 'occupied' | 'maintenance';
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
    type: 'classroom',
    capacity: 30,
    building: '',
    floor: 1,
    equipment: [],
    description: '',
    status: 'available'
  });

  protected readonly isLoading = signal(false);
  protected readonly errors = signal<{[key: string]: string}>({});
  protected readonly currentStep = signal<1 | 2 | 3>(1);

  protected readonly buildings = [
    'Bâtiment A - Administration',
    'Bâtiment B - Sciences et Technologies',
    'Bâtiment C - Management',
    'Bâtiment D - Laboratoires',
    'Bâtiment E - Amphithéâtres'
  ];

  protected readonly roomTypes = [
    { id: 'classroom', name: 'Salle de cours', icon: '🏫', description: 'Salle standard pour cours magistraux et TD' },
    { id: 'lab', name: 'Laboratoire', icon: '🔬', description: 'Laboratoire pour travaux pratiques' },
    { id: 'amphitheater', name: 'Amphithéâtre', icon: '🎭', description: 'Grande salle pour conférences et cours magistraux' },
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
        type: this.room.type || 'classroom',
        capacity: this.room.capacity || 30,
        building: this.room.building || '',
        floor: this.room.floor || 1,
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

  onEquipmentChange(equipmentId: string, checked: boolean) {
    this.roomForm.update(form => ({
      ...form,
      equipment: checked 
        ? [...form.equipment, equipmentId]
        : form.equipment.filter(id => id !== equipmentId)
    }));
  }

  isEquipmentSelected(equipmentId: string): boolean {
    return this.roomForm().equipment.includes(equipmentId);
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
      if (!form.building) newErrors['building'] = 'Le bâtiment est requis';
    }

    if (step === 2) {
      if (form.capacity < 1) newErrors['capacity'] = 'La capacité doit être supérieure à 0';
      if (form.floor < 0) newErrors['floor'] = 'L\'étage ne peut pas être négatif';
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
      type: 'classroom',
      capacity: 30,
      building: '',
      floor: 1,
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
    return form.name.trim() !== '' && form.building !== '';
  }

  isStep2Valid(): boolean {
    const form = this.roomForm();
    return form.capacity > 0 && form.floor >= 0;
  }
}