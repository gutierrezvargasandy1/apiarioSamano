import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Apiarios } from './apiarios';

describe('Apiarios', () => {
  let component: Apiarios;
  let fixture: ComponentFixture<Apiarios>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Apiarios]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Apiarios);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
