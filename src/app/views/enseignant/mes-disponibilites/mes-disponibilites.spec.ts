import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesDisponibilites } from './mes-disponibilites';

describe('MesDisponibilites', () => {
  let component: MesDisponibilites;
  let fixture: ComponentFixture<MesDisponibilites>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesDisponibilites]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MesDisponibilites);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
