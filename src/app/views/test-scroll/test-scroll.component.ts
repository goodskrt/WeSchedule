import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-test-scroll',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="test-scroll-container">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Test de Scroll</h1>
      
      <div class="space-y-8">
        @for (section of sections; track section.id) {
          <div class="bg-white/80 backdrop-blur-sm rounded-2xl p-8 shadow-lg border border-gray-200/50">
            <h2 class="text-2xl font-semibold text-gray-800 mb-4">{{ section.title }}</h2>
            <p class="text-gray-600 leading-relaxed mb-4">{{ section.content }}</p>
            
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              @for (item of section.items; track item) {
                <div class="bg-gradient-to-br from-blue-50 to-purple-50 rounded-xl p-4 border border-blue-200/50">
                  <h3 class="font-medium text-gray-800 mb-2">Élément {{ item }}</h3>
                  <p class="text-sm text-gray-600">Contenu de test pour l'élément {{ item }}. Ce texte permet de tester le scroll et la fluidité de l'interface.</p>
                </div>
              }
            </div>
          </div>
        }
      </div>
      
      <div class="mt-16 text-center">
        <button 
          (click)="scrollToTop()"
          class="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-medium py-3 px-6 rounded-xl transition-all duration-300 shadow-lg hover:shadow-xl transform hover:scale-105"
        >
          Retour en haut
        </button>
      </div>
    </div>
  `,
  styles: [`
    .test-scroll-container {
      min-height: 200vh;
      padding: 2rem 0;
    }
    
    @media (max-width: 768px) {
      .test-scroll-container {
        padding: 1rem 0;
      }
    }
  `]
})
export class TestScrollComponent {
  sections = [
    {
      id: 1,
      title: 'Section 1 - Test de Scroll',
      content: 'Cette section teste le comportement du scroll dans l\'application. Le scroll doit être fluide et réactif.',
      items: [1, 2, 3, 4, 5, 6]
    },
    {
      id: 2,
      title: 'Section 2 - Navbar Sticky',
      content: 'Cette section permet de tester si la navbar reste collée en haut lors du scroll et si elle réagit correctement.',
      items: [1, 2, 3, 4, 5, 6]
    },
    {
      id: 3,
      title: 'Section 3 - Sidebar Responsive',
      content: 'Cette section teste le comportement de la sidebar pendant le scroll et sa réactivité.',
      items: [1, 2, 3, 4, 5, 6]
    },
    {
      id: 4,
      title: 'Section 4 - Performance',
      content: 'Cette section teste les performances du scroll avec beaucoup de contenu.',
      items: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
    },
    {
      id: 5,
      title: 'Section 5 - Animations',
      content: 'Cette section teste les animations pendant le scroll.',
      items: [1, 2, 3, 4, 5, 6]
    },
    {
      id: 6,
      title: 'Section 6 - Contenu Long',
      content: 'Cette section contient beaucoup de contenu pour tester le scroll sur de longues pages.',
      items: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]
    }
  ];

  scrollToTop() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }
}