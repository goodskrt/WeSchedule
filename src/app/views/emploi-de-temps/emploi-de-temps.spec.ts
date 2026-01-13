import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmploiDeTemps } from './emploi-de-temps';

describe('EmploiDeTemps', () => {
  let component: EmploiDeTemps;
  let fixture: ComponentFixture<EmploiDeTemps>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmploiDeTemps]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmploiDeTemps);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
