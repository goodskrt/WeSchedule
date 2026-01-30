import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AjouterSalle } from '../../component/ajouter-salle/ajouter-salle';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface Room {
  id: string;
  name: string;
  type: 'classroom' | 'lab' | 'amphitheater' | 'conference';
  capacity: number;
  equipment: string[];
  building: string;
  floor: number;
  status: 'available' | 'occupied' | 'maintenance';
  currentBooking?: {
    course: string;
    teacher: string;
    time: string;
    endTime: string;
    studentCount: number;
  };
  nextBooking?: {
    course: string;
    teacher: string;
    time: string;
  };
  maintenanceInfo?: {
    reason: string;
    startDate: string;
    endDate: string;
    technician: string;
  };
  features?: string[];
  accessibility?: boolean;
  lastCleaned?: string;
  temperature?: number;
  occupancyRate?: number;
}

interface Equipment {
  id: string;
  name: string;
  icon: string;
}

interface Reservation {
  id: string;
  roomId: string;
  course: string;
  teacher: string;
  startTime: string;
  endTime: string;
  date: string;
  studentCount: number;
  status: 'confirmed' | 'pending' | 'cancelled';
  recurring?: boolean;
  notes?: string;
}

interface RoomStatistics {
  totalRooms: number;
  availableRooms: number;
  occupiedRooms: number;
  maintenanceRooms: number;
  averageOccupancy: number;
  mostUsedRoom: string;
  leastUsedRoom: string;
  totalCapacity: number;
  utilizationRate: number;
}

@Component({
  selector: 'app-salles',
  imports: [CommonModule, AjouterSalle, SvgIconComponent],
  templateUrl: './salles.html',
  styleUrl: './salles.scss',
})
export class Salles {
  protected readonly selectedFilter = signal<string>('all');
  protected readonly selectedBuilding = signal<string>('all');
  protected readonly searchTerm = signal<string>('');
  protected readonly showAddModal = signal<boolean>(false);
  protected readonly selectedRoom = signal<Room | null>(null);
  protected readonly showDetailsModal = signal<boolean>(false);
  protected readonly showReservationModal = signal<boolean>(false);
  protected readonly showCalendarModal = signal<boolean>(false);
  protected readonly showStatisticsModal = signal<boolean>(false);
  protected readonly showFloorPlanModal = signal<boolean>(false);
  protected readonly viewMode = signal<'grid' | 'list' | 'map'>('grid');
  protected readonly selectedDate = signal<string>(new Date().toISOString().split('T')[0]);
  protected readonly selectedTimeSlot = signal<string>('');

  protected readonly equipmentList = signal<Equipment[]>([
    { id: 'projector', name: 'Projecteur', icon: '📽️' },
    { id: 'computer', name: 'Ordinateurs', icon: '💻' },
    { id: 'whiteboard', name: 'Tableau blanc', icon: '📋' },
    { id: 'smartboard', name: 'Tableau interactif', icon: '📱' },
    { id: 'speakers', name: 'Haut-parleurs', icon: '🔊' },
    { id: 'microphone', name: 'Microphone', icon: '🎤' },
    { id: 'camera', name: 'Caméra', icon: '📹' },
    { id: 'printer', name: 'Imprimante', icon: '🖨️' },
    { id: 'scanner', name: 'Scanner', icon: '📄' },
    { id: 'airconditioner', name: 'Climatisation', icon: '❄️' },
    { id: 'wifi', name: 'WiFi', icon: '📶' },
    { id: 'ethernet', name: 'Ethernet', icon: '🔌' }
  ]);

  protected readonly reservations = signal<Reservation[]>([
    {
      id: '1',
      roomId: '1',
      course: 'Algorithmique Avancée',
      teacher: 'Dr. Martin Dubois',
      startTime: '09:00',
      endTime: '11:00',
      date: '2026-01-08',
      studentCount: 32,
      status: 'confirmed',
      recurring: true,
      notes: 'Cours avec TP pratique'
    },
    {
      id: '2',
      roomId: '2',
      course: 'Base de Données',
      teacher: 'Prof. Sarah Johnson',
      startTime: '14:00',
      endTime: '16:00',
      date: '2026-01-08',
      studentCount: 25,
      status: 'confirmed',
      recurring: false
    }
  ]);

  protected readonly rooms = signal<Room[]>([
    {
      id: '1',
      name: 'Salle 101',
      type: 'classroom',
      capacity: 35,
      equipment: ['projector', 'whiteboard', 'speakers', 'wifi'],
      building: 'Bâtiment A',
      floor: 1,
      status: 'occupied',
      currentBooking: {
        course: 'Algorithmique Avancée',
        teacher: 'Dr. Martin Dubois',
        time: '09:00 - 11:00',
        endTime: '11:00',
        studentCount: 32
      },
      nextBooking: {
        course: 'Mathématiques Discrètes',
        teacher: 'Prof. Claire Moreau',
        time: '14:00 - 16:00'
      },
      features: ['Éclairage LED', 'Insonorisation'],
      accessibility: true,
      lastCleaned: '2026-01-07T18:00:00Z',
      temperature: 22,
      occupancyRate: 85
    },
    {
      id: '2',
      name: 'Lab Info 2',
      type: 'lab',
      capacity: 28,
      equipment: ['computer', 'projector', 'whiteboard', 'printer', 'scanner', 'wifi', 'ethernet'],
      building: 'Bâtiment B',
      floor: 2,
      status: 'available',
      features: ['Postes informatiques', 'Serveur local', 'Logiciels spécialisés'],
      accessibility: true,
      lastCleaned: '2026-01-07T20:00:00Z',
      temperature: 21,
      occupancyRate: 70
    },
    {
      id: '3',
      name: 'Amphithéâtre A',
      type: 'amphitheater',
      capacity: 150,
      equipment: ['projector', 'microphone', 'speakers', 'camera', 'smartboard', 'wifi'],
      building: 'Bâtiment C',
      floor: 0,
      status: 'available',
      features: ['Système de diffusion', 'Enregistrement vidéo', 'Éclairage scénique'],
      accessibility: true,
      lastCleaned: '2026-01-07T19:30:00Z',
      temperature: 20,
      occupancyRate: 60
    },
    {
      id: '4',
      name: 'Salle de Conférence',
      type: 'conference',
      capacity: 50,
      equipment: ['projector', 'microphone', 'speakers', 'whiteboard', 'camera', 'wifi'],
      building: 'Bâtiment A',
      floor: 3,
      status: 'maintenance',
      maintenanceInfo: {
        reason: 'Remplacement du système de climatisation',
        startDate: '2026-01-08',
        endDate: '2026-01-10',
        technician: 'Service Technique Campus'
      },
      features: ['Table de conférence', 'Système de visioconférence'],
      accessibility: true,
      lastCleaned: '2026-01-06T16:00:00Z',
      temperature: 18,
      occupancyRate: 45
    },
    {
      id: '5',
      name: 'Lab Chimie 1',
      type: 'lab',
      capacity: 20,
      equipment: ['projector', 'whiteboard', 'airconditioner', 'wifi'],
      building: 'Bâtiment D',
      floor: 1,
      status: 'available',
      features: ['Hottes aspirantes', 'Éviers spécialisés', 'Armoires sécurisées'],
      accessibility: false,
      lastCleaned: '2026-01-07T17:00:00Z',
      temperature: 19,
      occupancyRate: 55
    },
    {
      id: '6',
      name: 'Salle 205',
      type: 'classroom',
      capacity: 40,
      equipment: ['projector', 'smartboard', 'speakers', 'wifi'],
      building: 'Bâtiment B',
      floor: 2,
      status: 'available',
      features: ['Mobilier modulable', 'Prises électriques individuelles'],
      accessibility: true,
      lastCleaned: '2026-01-07T18:30:00Z',
      temperature: 23,
      occupancyRate: 75
    }
  ]);

  getTypeInfo(type: string) {
    switch (type) {
      case 'classroom': return { name: 'Salle de cours', icon: '🏫', color: 'bg-blue-100 text-blue-800' };
      case 'lab': return { name: 'Laboratoire', icon: '🔬', color: 'bg-purple-100 text-purple-800' };
      case 'amphitheater': return { name: 'Amphithéâtre', icon: '🎭', color: 'bg-green-100 text-green-800' };
      case 'conference': return { name: 'Salle de conférence', icon: '🏢', color: 'bg-orange-100 text-orange-800' };
      default: return { name: 'Salle', icon: '🏫', color: 'bg-gray-100 text-gray-800' };
    }
  }

  getStatusInfo(status: string) {
    switch (status) {
      case 'available': return { name: 'Disponible', color: 'bg-green-100 text-green-800' };
      case 'occupied': return { name: 'Occupée', color: 'bg-red-100 text-red-800' };
      case 'maintenance': return { name: 'Maintenance', color: 'bg-yellow-100 text-yellow-800' };
      default: return { name: 'Inconnu', color: 'bg-gray-100 text-gray-800' };
    }
  }

  getEquipmentIcon(equipmentId: string): string {
    return this.equipmentList().find(e => e.id === equipmentId)?.icon || '📦';
  }

  filteredRooms() {
    let filtered = this.rooms();
    
    if (this.selectedFilter() !== 'all') {
      if (this.selectedFilter() === 'available') {
        filtered = filtered.filter(room => room.status === 'available');
      } else {
        filtered = filtered.filter(room => room.type === this.selectedFilter());
      }
    }
    
    if (this.selectedBuilding() !== 'all') {
      filtered = filtered.filter(room => room.building === this.selectedBuilding());
    }
    
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(room => 
        room.name.toLowerCase().includes(term) ||
        room.building.toLowerCase().includes(term)
      );
    }
    
    return filtered;
  }

  setFilter(filter: string) {
    this.selectedFilter.set(filter);
  }

  setBuildingFilter(building: string) {
    this.selectedBuilding.set(building);
  }

  updateSearchTerm(term: string) {
    this.searchTerm.set(term);
  }

  getAvailableRoomsCount(): number {
    return this.rooms().filter(r => r.status === 'available').length;
  }

  getOccupiedRoomsCount(): number {
    return this.rooms().filter(r => r.status === 'occupied').length;
  }

  getMaintenanceRoomsCount(): number {
    return this.rooms().filter(r => r.status === 'maintenance').length;
  }

  getEquipmentName(equipmentId: string): string {
    return this.equipmentList().find(e => e.id === equipmentId)?.name || 'Équipement';
  }

  onBuildingChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setBuildingFilter(target.value);
  }

  getUniqueBuildings() {
    return [...new Set(this.rooms().map(room => room.building))];
  }

  // Modal management methods
  openAddModal() {
    this.selectedRoom.set(null);
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
    this.selectedRoom.set(null);
  }

  openDetailsModal(room: Room) {
    this.selectedRoom.set(room);
    this.showDetailsModal.set(true);
  }

  closeDetailsModal() {
    this.showDetailsModal.set(false);
    this.selectedRoom.set(null);
  }

  openReservationModal(room: Room) {
    this.selectedRoom.set(room);
    this.showReservationModal.set(true);
  }

  closeReservationModal() {
    this.showReservationModal.set(false);
    this.selectedRoom.set(null);
  }

  editRoom(room: Room) {
    this.selectedRoom.set(room);
    this.showAddModal.set(true);
  }

  deleteRoom(roomId: string) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette salle ?')) {
      this.rooms.update(rooms => rooms.filter(r => r.id !== roomId));
    }
  }

  reserveRoom(room: Room) {
    this.openReservationModal(room);
  }

  onRoomAdded(roomData: any) {
    if (this.selectedRoom()) {
      // Update existing room
      this.rooms.update(rooms => 
        rooms.map(r => r.id === this.selectedRoom()!.id ? { ...r, ...roomData } : r)
      );
    } else {
      // Add new room
      const newRoom: Room = {
        id: Date.now().toString(),
        ...roomData
      };
      this.rooms.update(rooms => [...rooms, newRoom]);
    }
    this.closeAddModal();
  }

  getTotalCapacity(): number {
    return this.rooms().reduce((total, room) => total + room.capacity, 0);
  }

  getAverageCapacity(): number {
    const rooms = this.rooms();
    if (rooms.length === 0) return 0;
    return Math.round(this.getTotalCapacity() / rooms.length);
  }

  getRoomsByType() {
    const rooms = this.rooms();
    const types = ['classroom', 'lab', 'amphitheater', 'conference'];
    return types.map(type => ({
      type,
      count: rooms.filter(r => r.type === type).length,
      info: this.getTypeInfo(type)
    }));
  }

  importRooms() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.xlsx,.xls';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        alert(`Import du fichier "${file.name}" en cours...`);
        // Ici vous implémenteriez la logique d'import réelle
      }
    };
    input.click();
  }

  exportRooms() {
    const rooms = this.filteredRooms();
    const csvContent = [
      'Nom,Bâtiment,Étage,Capacité,Type,Statut,Équipements',
      ...rooms.map(r => 
        `"${r.name}","${r.building}",${r.floor},${r.capacity},"${r.type}","${r.status}","${r.equipment.join(';')}"`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `salles_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  onSearchInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.updateSearchTerm(target.value);
  }

  // Nouvelles méthodes avancées
  getRoomStatistics(): RoomStatistics {
    const rooms = this.rooms();
    const totalRooms = rooms.length;
    const availableRooms = rooms.filter(r => r.status === 'available').length;
    const occupiedRooms = rooms.filter(r => r.status === 'occupied').length;
    const maintenanceRooms = rooms.filter(r => r.status === 'maintenance').length;
    const totalCapacity = rooms.reduce((sum, room) => sum + room.capacity, 0);
    const averageOccupancy = rooms.reduce((sum, room) => sum + (room.occupancyRate || 0), 0) / totalRooms;
    
    const roomsByOccupancy = rooms.sort((a, b) => (b.occupancyRate || 0) - (a.occupancyRate || 0));
    const mostUsedRoom = roomsByOccupancy[0]?.name || 'N/A';
    const leastUsedRoom = roomsByOccupancy[roomsByOccupancy.length - 1]?.name || 'N/A';
    
    const utilizationRate = (occupiedRooms / totalRooms) * 100;

    return {
      totalRooms,
      availableRooms,
      occupiedRooms,
      maintenanceRooms,
      averageOccupancy: Math.round(averageOccupancy),
      mostUsedRoom,
      leastUsedRoom,
      totalCapacity,
      utilizationRate: Math.round(utilizationRate)
    };
  }

  getTimeSlots(): string[] {
    return [
      '08:00 - 09:30',
      '09:45 - 11:15',
      '11:30 - 13:00',
      '14:00 - 15:30',
      '15:45 - 17:15',
      '17:30 - 19:00',
      '19:15 - 20:45'
    ];
  }

  isRoomAvailableAtTime(roomId: string, date: string, timeSlot: string): boolean {
    const reservations = this.reservations().filter(r => 
      r.roomId === roomId && 
      r.date === date && 
      r.status === 'confirmed'
    );
    
    const [startTime] = timeSlot.split(' - ');
    return !reservations.some(r => r.startTime === startTime);
  }

  getRoomOccupancyForWeek(roomId: string): number[] {
    // Simulation des données d'occupation pour la semaine
    return [85, 70, 90, 65, 80, 45, 20]; // Lun-Dim
  }

  getMaintenanceSchedule(): any[] {
    return this.rooms()
      .filter(room => room.maintenanceInfo)
      .map(room => ({
        room: room.name,
        building: room.building,
        reason: room.maintenanceInfo!.reason,
        startDate: room.maintenanceInfo!.startDate,
        endDate: room.maintenanceInfo!.endDate,
        technician: room.maintenanceInfo!.technician
      }));
  }

  getUpcomingReservations(): Reservation[] {
    const today = new Date().toISOString().split('T')[0];
    return this.reservations()
      .filter(r => r.date >= today && r.status === 'confirmed')
      .sort((a, b) => {
        if (a.date !== b.date) return a.date.localeCompare(b.date);
        return a.startTime.localeCompare(b.startTime);
      })
      .slice(0, 5);
  }

  // Gestion des modales avancées
  openCalendarModal() {
    this.showCalendarModal.set(true);
  }

  closeCalendarModal() {
    this.showCalendarModal.set(false);
  }

  openStatisticsModal() {
    this.showStatisticsModal.set(true);
  }

  closeStatisticsModal() {
    this.showStatisticsModal.set(false);
  }

  openFloorPlanModal(building: string) {
    this.selectedBuilding.set(building);
    this.showFloorPlanModal.set(true);
  }

  closeFloorPlanModal() {
    this.showFloorPlanModal.set(false);
  }

  setViewMode(mode: 'grid' | 'list' | 'map') {
    this.viewMode.set(mode);
  }

  // Gestion des réservations avancées
  createReservation(roomId: string, reservationData: Partial<Reservation>) {
    const newReservation: Reservation = {
      id: Date.now().toString(),
      roomId,
      course: reservationData.course || '',
      teacher: reservationData.teacher || '',
      startTime: reservationData.startTime || '',
      endTime: reservationData.endTime || '',
      date: reservationData.date || this.selectedDate(),
      studentCount: reservationData.studentCount || 0,
      status: 'pending',
      recurring: reservationData.recurring || false,
      notes: reservationData.notes || ''
    };

    this.reservations.update(reservations => [...reservations, newReservation]);
    
    // Mettre à jour le statut de la salle si nécessaire
    const now = new Date();
    const reservationDate = new Date(newReservation.date + 'T' + newReservation.startTime);
    const endDate = new Date(newReservation.date + 'T' + newReservation.endTime);
    
    if (now >= reservationDate && now <= endDate) {
      this.updateRoomStatus(roomId, 'occupied', {
        course: newReservation.course,
        teacher: newReservation.teacher,
        time: `${newReservation.startTime} - ${newReservation.endTime}`,
        endTime: newReservation.endTime,
        studentCount: newReservation.studentCount
      });
    }
  }

  updateRoomStatus(roomId: string, status: Room['status'], bookingInfo?: any) {
    this.rooms.update(rooms => 
      rooms.map(room => 
        room.id === roomId 
          ? { ...room, status, currentBooking: bookingInfo }
          : room
      )
    );
  }

  checkForConflicts(roomId: string, date: string, startTime: string, endTime: string): boolean {
    const existingReservations = this.reservations().filter(r => 
      r.roomId === roomId && 
      r.date === date && 
      r.status !== 'cancelled'
    );

    return existingReservations.some(r => {
      const existingStart = r.startTime;
      const existingEnd = r.endTime;
      
      return (startTime < existingEnd && endTime > existingStart);
    });
  }

  generateRoomReport(): any {
    const stats = this.getRoomStatistics();
    const rooms = this.rooms();
    
    return {
      summary: stats,
      roomDetails: rooms.map(room => ({
        name: room.name,
        building: room.building,
        capacity: room.capacity,
        occupancyRate: room.occupancyRate,
        status: room.status,
        lastCleaned: room.lastCleaned,
        temperature: room.temperature,
        equipmentCount: room.equipment.length,
        accessibility: room.accessibility
      })),
      maintenanceSchedule: this.getMaintenanceSchedule(),
      upcomingReservations: this.getUpcomingReservations()
    };
  }

  exportDetailedReport() {
    const report = this.generateRoomReport();
    const csvContent = [
      'Nom,Bâtiment,Étage,Capacité,Type,Statut,Taux d\'occupation,Température,Accessibilité,Équipements',
      ...report.roomDetails.map((r: any) => 
        `"${r.name}","${r.building}",${this.rooms().find(room => room.name === r.name)?.floor},"${r.capacity}","${this.rooms().find(room => room.name === r.name)?.type}","${r.status}","${r.occupancyRate}%","${r.temperature}°C","${r.accessibility ? 'Oui' : 'Non'}","${r.equipmentCount} équipements"`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rapport_salles_detaille_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  // Helper method pour éviter les erreurs de template
  getRoomNameById(roomId: string): string {
    return this.rooms().find(r => r.id === roomId)?.name || 'Salle inconnue';
  }

  // Helper pour Math.round dans les templates
  round(value: number): number {
    return Math.round(value);
  }
}
