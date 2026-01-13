import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterSalle } from './ajouter-salle';

describe('AjouterSalle', () => {
  let component: AjouterSalle;
  let fixture: ComponentFixture<AjouterSalle>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AjouterSalle]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterSalle);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
