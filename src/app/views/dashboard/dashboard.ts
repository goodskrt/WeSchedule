import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { SvgIconComponent } from '../../shared/svg-icon/svg-icon.component';

interface StatCard {
  title: string;
  value: number;
  icon: string;
  color: string;
  change: number;
  route: string;
}

interface RecentActivity {
  id: string;
  type: 'course' | 'room' | 'notification';
  title: string;
  description: string;
  time: string;
  icon: string;
}

interface UpcomingClass {
  id: string;
  subject: string;
  room: string;
  time: string;
  students: number;
  teacher: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, SvgIconComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  constructor(private router: Router) {}

  protected readonly stats = signal<StatCard[]>([
    {
      title: 'Cours aujourd\'hui',
      value: 8,
      icon: 'book-open',
      color: 'bg-blue-500',
      change: 12,
      route: '/cours'
    },
    {
      title: 'Salles disponibles',
      value: 15,
      icon: 'building',
      color: 'bg-green-500',
      change: -5,
      route: '/salles'
    },
    {
      title: 'Enseignants actifs',
      value: 42,
      icon: 'user-group',
      color: 'bg-purple-500',
      change: 8,
      route: '/professeurs'
    },
    {
      title: 'Étudiants inscrits',
      value: 1250,
      icon: 'academic-cap',
      color: 'bg-orange-500',
      change: 15,
      route: '/dashboard'
    }
  ]);

  protected readonly recentActivities = signal<RecentActivity[]>([
    {
      id: '1',
      type: 'course',
      title: 'Nouveau cours ajouté',
      description: 'Mathématiques Avancées - L3 Informatique',
      time: 'Il y a 2 heures',
      icon: 'book-open'
    },
    {
      id: '2',
      type: 'room',
      title: 'Salle réservée',
      description: 'Amphithéâtre A - Conférence sur l\'IA',
      time: 'Il y a 4 heures',
      icon: 'building'
    },
    {
      id: '3',
      type: 'notification',
      title: 'Changement d\'horaire',
      description: 'Cours de Physique reporté à 14h',
      time: 'Il y a 6 heures',
      icon: 'bell'
    },
    {
      id: '4',
      type: 'course',
      title: 'Nouveau professeur',
      description: 'Dr. Sophie Martin rejoint l\'équipe',
      time: 'Il y a 1 jour',
      icon: 'user-plus'
    }
  ]);

  protected readonly upcomingClasses = signal<UpcomingClass[]>([
    {
      id: '1',
      subject: 'Algorithmique',
      room: 'Salle 101',
      time: '09:00 - 11:00',
      students: 35,
      teacher: 'Dr. Martin'
    },
    {
      id: '2',
      subject: 'Base de Données',
      room: 'Lab Info 2',
      time: '14:00 - 16:00',
      students: 28,
      teacher: 'Prof. Dubois'
    },
    {
      id: '3',
      subject: 'Réseaux',
      room: 'Salle 205',
      time: '16:30 - 18:30',
      students: 42,
      teacher: 'Dr. Laurent'
    }
  ]);

  navigateToSection(route: string) {
    this.router.navigate([route]);
  }

  viewClassDetails(classId: string) {
    this.router.navigate(['/cours', classId]);
  }

  viewActivity(activityId: string) {
    const activity = this.recentActivities().find(a => a.id === activityId);
    if (activity) {
      switch (activity.type) {
        case 'course':
          this.router.navigate(['/cours']);
          break;
        case 'room':
          this.router.navigate(['/salles']);
          break;
        case 'notification':
          this.router.navigate(['/notifications']);
          break;
      }
    }
  }

  quickAction(action: string) {
    switch (action) {
      case 'add-course':
        this.router.navigate(['/cours']);
        break;
      case 'reserve-room':
        this.router.navigate(['/salles']);
        break;
      case 'view-reports':
        this.router.navigate(['/rapports']);
        break;
    }
  }
}
