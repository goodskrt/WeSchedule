import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonEmploiDeTemps } from './mon-emploi-de-temps';

describe('MonEmploiDeTemps', () => {
  let component: MonEmploiDeTemps;
  let fixture: ComponentFixture<MonEmploiDeTemps>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonEmploiDeTemps]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonEmploiDeTemps);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
