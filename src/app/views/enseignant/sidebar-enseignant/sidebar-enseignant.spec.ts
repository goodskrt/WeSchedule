import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarEnseignant } from './sidebar-enseignant';

describe('SidebarEnseignant', () => {
  let component: SidebarEnseignant;
  let fixture: ComponentFixture<SidebarEnseignant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarEnseignant]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarEnseignant);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
