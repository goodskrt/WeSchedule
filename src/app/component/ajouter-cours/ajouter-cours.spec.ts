import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterCours } from './ajouter-cours';

describe('AjouterCours', () => {
  let component: AjouterCours;
  let fixture: ComponentFixture<AjouterCours>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AjouterCours]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterCours);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
