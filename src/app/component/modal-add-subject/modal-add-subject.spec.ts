import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalAddSubject } from './modal-add-subject';

describe('ModalAddSubject', () => {
  let component: ModalAddSubject;
  let fixture: ComponentFixture<ModalAddSubject>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalAddSubject]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalAddSubject);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start with step 1', () => {
    expect(component['currentStep']()).toBe(1);
  });

  it('should validate step 1 correctly', () => {
    // Test with empty form
    expect(component['isStep1Valid']()).toBeFalsy();
    
    // Fill required fields
    component['updateForm']('name', 'Test Subject');
    component['updateForm']('code', 'TEST123');
    component['updateForm']('department', 'Informatique');
    component['updateForm']('school', 'sji');
    
    expect(component['isStep1Valid']()).toBeTruthy();
  });

  it('should generate code correctly', () => {
    const code = component['generateCode']('Algorithmique');
    expect(code).toMatch(/^[A-Z]{1,4}\d{3}$/);
  });
});