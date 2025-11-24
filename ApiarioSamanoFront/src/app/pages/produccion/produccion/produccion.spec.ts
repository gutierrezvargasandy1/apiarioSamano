import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Produccion } from './produccion';

describe('Produccion', () => {
  let component: Produccion;
  let fixture: ComponentFixture<Produccion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Produccion]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Produccion);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
