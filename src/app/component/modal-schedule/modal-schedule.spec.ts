import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSchedule } from './modal-schedule';

describe('ModalSchedule', () => {
  let component: ModalSchedule;
  let fixture: ComponentFixture<ModalSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSchedule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSchedule);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
