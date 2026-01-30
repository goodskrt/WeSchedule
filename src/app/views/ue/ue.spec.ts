import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ue } from './ue';

describe('Ue', () => {
  let component: Ue;
  let fixture: ComponentFixture<Ue>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Ue]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Ue);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
