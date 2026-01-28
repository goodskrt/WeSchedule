import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { User } from '../../../shared/models/auth.models';

interface DashboardStats {
  totalCours: number;
  coursAujourdhui: number;
  heuresRestantes: number;
  prochainCours: string;
}

interface CoursAujourdhui {
  nom: string;
  heure: string;
  salle: string;
  type: 'CM' | 'TD' | 'TP';
  classe: string;
}

@Component({
  selector: 'app-enseignant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class EnseignantDashboard implements OnInit {
  protected readonly currentUser = signal<User | null>(null);
  protected readonly stats = signal<DashboardStats>({
    totalCours: 0,
    coursAujourdhui: 0,
    heuresRestantes: 0,
    prochainCours: ''
  });
  protected readonly coursAujourdhui = signal<CoursAujourdhui[]>([]);
  protected readonly isLoading = signal(true);

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.loadUserData();
    this.loadDashboardData();
  }

  private loadUserData() {
    const user = this.authService.getCurrentUser();
    this.currentUser.set(user);
  }

  private loadDashboardData() {
    // Simulation de données - à remplacer par des appels API réels
    setTimeout(() => {
      this.stats.set({
        totalCours: 8,
        coursAujourdhui: 3,
        heuresRestantes: 45,
        prochainCours: 'Algorithmique - 14h00'
      });

      this.coursAujourdhui.set([
        {
          nom: 'Algorithmique',
          heure: '08h00 - 10h00',
          salle: 'A101',
          type: 'CM',
          classe: 'L1 Informatique'
        },
        {
          nom: 'Mathématiques',
          heure: '14h00 - 16h00',
          salle: 'B201',
          type: 'TD',
          classe: 'L1 Informatique'
        },
        {
          nom: 'Base de Données',
          heure: '16h30 - 18h30',
          salle: 'Labo Info',
          type: 'TP',
          classe: 'L2 Informatique'
        }
      ]);

      this.isLoading.set(false);
    }, 1000);
  }

  getTypeColor(type: string): string {
    switch (type) {
      case 'CM': return 'bg-blue-100 text-blue-800';
      case 'TD': return 'bg-green-100 text-green-800';
      case 'TP': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }
}
