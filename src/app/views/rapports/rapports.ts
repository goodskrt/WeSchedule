import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ReportCard {
  id: string;
  title: string;
  description: string;
  icon: string;
  color: string;
  type: 'usage' | 'schedule' | 'performance' | 'financial';
}

interface StatItem {
  label: string;
  value: string;
  change: number;
  icon: string;
}

@Component({
  selector: 'app-rapports',
  imports: [CommonModule],
  templateUrl: './rapports.html',
  styleUrl: './rapports.scss',
})
export class Rapports {
  protected readonly selectedPeriod = signal<string>('week');
  protected readonly selectedSchool = signal<string>('all');
  protected readonly showScheduleModal = signal<boolean>(false);
  protected readonly selectedReportType = signal<string>('');
  protected readonly isGenerating = signal<boolean>(false);

  protected readonly reportCards = signal<ReportCard[]>([
    {
      id: 'room-usage',
      title: 'Utilisation des Salles',
      description: 'Taux d\'occupation et statistiques d\'utilisation des salles par école',
      icon: 'chart',
      color: 'bg-blue-500',
      type: 'usage'
    },
    {
      id: 'schedule-conflicts',
      title: 'Conflits d\'Horaires',
      description: 'Analyse des chevauchements et conflits dans les emplois du temps',
      icon: 'warning',
      color: 'bg-yellow-500',
      type: 'schedule'
    },
    {
      id: 'teacher-workload',
      title: 'Charge de Travail',
      description: 'Répartition des heures d\'enseignement par professeur et école',
      icon: 'user',
      color: 'bg-green-500',
      type: 'performance'
    },
    {
      id: 'resource-efficiency',
      title: 'Efficacité des Ressources',
      description: 'Optimisation de l\'utilisation des équipements et infrastructures',
      icon: 'lightning',
      color: 'bg-purple-500',
      type: 'performance'
    },
    {
      id: 'attendance-stats',
      title: 'Statistiques de Présence',
      description: 'Taux de présence des étudiants par cours et par école',
      icon: 'trending',
      color: 'bg-indigo-500',
      type: 'performance'
    },
    {
      id: 'cost-analysis',
      title: 'Analyse des Coûts',
      description: 'Coûts d\'exploitation et rentabilité par école et ressource',
      icon: 'currency',
      color: 'bg-orange-500',
      type: 'financial'
    }
  ]);

  protected readonly quickStats = signal<StatItem[]>([
    {
      label: 'Taux d\'occupation moyen',
      value: '78%',
      change: 5.2,
      icon: 'building'
    },
    {
      label: 'Heures d\'enseignement',
      value: '1,247h',
      change: 12.8,
      icon: 'clock'
    },
    {
      label: 'Conflits résolus',
      value: '23',
      change: -15.3,
      icon: 'check'
    },
    {
      label: 'Économies réalisées',
      value: '€12,450',
      change: 8.7,
      icon: 'currency'
    }
  ]);

  protected readonly recentReports = signal([
    {
      id: '1',
      name: 'Rapport mensuel - Décembre 2026',
      type: 'Utilisation des salles',
      date: '2026-12-31',
      status: 'completed',
      size: '2.4 MB'
    },
    {
      id: '2',
      name: 'Analyse des conflits - Semaine 52',
      type: 'Conflits d\'horaires',
      date: '2026-12-29',
      status: 'completed',
      size: '1.8 MB'
    },
    {
      id: '3',
      name: 'Rapport trimestriel Q4 2026',
      type: 'Performance globale',
      date: '2026-12-28',
      status: 'processing',
      size: '5.2 MB'
    }
  ]);

  setPeriod(period: string) {
    this.selectedPeriod.set(period);
  }

  setSchool(school: string) {
    this.selectedSchool.set(school);
  }

  onSchoolChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.setSchool(target.value);
  }

  generateReport(reportId: string) {
    this.isGenerating.set(true);
    
    // Simulation de génération de rapport
    setTimeout(() => {
      this.isGenerating.set(false);
      
      const reportCard = this.reportCards().find(r => r.id === reportId);
      const newReport = {
        id: Date.now().toString(),
        name: `${reportCard?.title} - ${new Date().toLocaleDateString('fr-FR')}`,
        type: reportCard?.title || 'Rapport',
        date: new Date().toISOString().split('T')[0],
        status: 'completed',
        size: `${(Math.random() * 5 + 1).toFixed(1)} MB`
      };
      
      this.recentReports.update(reports => [newReport, ...reports]);
      alert('Rapport généré avec succès !');
    }, 3000);
  }

  scheduleReport(reportId?: string) {
    if (reportId) {
      this.selectedReportType.set(reportId);
    }
    this.showScheduleModal.set(true);
  }

  closeScheduleModal() {
    this.showScheduleModal.set(false);
    this.selectedReportType.set('');
  }

  onScheduleSubmit(scheduleData: any) {
    alert(`Rapport programmé avec succès pour ${scheduleData.frequency}`);
    this.closeScheduleModal();
  }

  downloadReport(reportId: string) {
    const report = this.recentReports().find(r => r.id === reportId);
    if (report) {
      // Simulation de téléchargement
      const link = document.createElement('a');
      link.href = '#';
      link.download = `${report.name}.pdf`;
      link.click();
      alert(`Téléchargement de "${report.name}" commencé`);
    }
  }

  deleteReport(reportId: string) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce rapport ?')) {
      this.recentReports.update(reports => reports.filter(r => r.id !== reportId));
    }
  }

  getIconSvg(iconType: string): string {
    const icons: { [key: string]: string } = {
      'building': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>',
      'clock': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>',
      'check': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>',
      'currency': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>',
      'chart': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>',
      'warning': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg>',
      'user': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>',
      'lightning': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>',
      'trending': '<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>'
    };
    return icons[iconType] || icons['chart'];
  }



  createNewReport() {
    // Ouvrir un modal pour créer un nouveau rapport
    alert('Fonctionnalité de création de rapport à implémenter');
  }

  getStatusInfo(status: string) {
    switch (status) {
      case 'completed': return { name: 'Terminé', color: 'bg-green-100 text-green-800' };
      case 'processing': return { name: 'En cours', color: 'bg-yellow-100 text-yellow-800' };
      case 'failed': return { name: 'Échec', color: 'bg-red-100 text-red-800' };
      default: return { name: 'Inconnu', color: 'bg-gray-100 text-gray-800' };
    }
  }

  exportAllReports() {
    alert('Export de tous les rapports en cours...');
  }

  getReportsByType() {
    const reports = this.recentReports();
    const types = [...new Set(reports.map(r => r.type))];
    return types.map(type => ({
      type,
      count: reports.filter(r => r.type === type).length
    }));
  }

  getCompletedReportsCount(): number {
    return this.recentReports().filter(r => r.status === 'completed').length;
  }

  getProcessingReportsCount(): number {
    return this.recentReports().filter(r => r.status === 'processing').length;
  }

  getFailedReportsCount(): number {
    return this.recentReports().filter(r => r.status === 'failed').length;
  }

  getTotalReportsSize(): string {
    const totalMB = this.recentReports()
      .filter(r => r.status === 'completed')
      .reduce((total, report) => {
        const size = parseFloat(report.size.replace(' MB', ''));
        return total + size;
      }, 0);
    
    return totalMB > 1024 ? `${(totalMB / 1024).toFixed(1)} GB` : `${totalMB.toFixed(1)} MB`;
  }
}
