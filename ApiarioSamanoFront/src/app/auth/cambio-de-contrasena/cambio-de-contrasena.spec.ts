import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CambioDeContrasena } from './cambio-de-contrasena';

describe('CambioDeContrasena', () => {
  let component: CambioDeContrasena;
  let fixture: ComponentFixture<CambioDeContrasena>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CambioDeContrasena]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CambioDeContrasena);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
