import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterEnseignant } from './ajouter-enseignant';

describe('AjouterEnseignant', () => {
  let component: AjouterEnseignant;
  let fixture: ComponentFixture<AjouterEnseignant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AjouterEnseignant]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterEnseignant);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
