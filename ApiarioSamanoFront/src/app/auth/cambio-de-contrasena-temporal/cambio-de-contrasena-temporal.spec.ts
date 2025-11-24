import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CambioDeContrasenaTemporal } from './cambio-de-contrasena-temporal';

describe('CambioDeContrasenaTemporal', () => {
  let component: CambioDeContrasenaTemporal;
  let fixture: ComponentFixture<CambioDeContrasenaTemporal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CambioDeContrasenaTemporal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CambioDeContrasenaTemporal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
