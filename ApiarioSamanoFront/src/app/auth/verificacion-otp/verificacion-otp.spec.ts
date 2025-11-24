import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificacionOTP } from './verificacion-otp';

describe('VerificacionOTP', () => {
  let component: VerificacionOTP;
  let fixture: ComponentFixture<VerificacionOTP>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VerificacionOTP]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerificacionOTP);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
