import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { DataService } from '../../services/data/data';
import { ToastService } from '../../services/toastService/toast-service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-verificacion-otp',
  standalone: false,
  templateUrl: './verificacion-otp.html',
  styleUrls: ['./verificacion-otp.css']
})
export class VerificacionOTP {

  codigoOTP: string = '';
  email: string = '';
  isSubmitting: boolean = false;

  constructor(
    private authService: AuthService, 
    private router: Router, 
    private dataService: DataService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.email = this.dataService.getEmail();
  }
   
  verificarOTP(): void {
    // Validación
    if (!this.codigoOTP.trim()) {
      this.toastService.warning('Atención', 'Debes ingresar el código OTP.');
      return;
    }

    if (this.codigoOTP.length !== 6) {
      this.toastService.warning('Atención', 'El código OTP debe tener 6 dígitos.');
      return;
    }

    this.isSubmitting = true;
    this.dataService.setOtp(this.codigoOTP);

    this.authService.verificarOtpYCambiarContrasena(this.email, this.codigoOTP).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.toastService.success('Éxito', 'OTP verificado correctamente.');
        this.router.navigate(['cambiar-contrasena']);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        console.error('Error al verificar OTP:', error);

        let mensaje = 'Ocurrió un error, intenta nuevamente.';

        if (error.status === 400 || error.status === 401) {
          mensaje = 'Código OTP inválido o expirado.';
        } else if (error.error?.message) {
          mensaje = error.error.message;
        }

        this.toastService.error('Error', mensaje);
      }
    });
  }

  reenviarOTP(): void {
    if (!this.email) {
      this.toastService.warning('Atención', 'No se encontró el correo electrónico.');
      return;
    }

    this.isSubmitting = true;

    this.authService.iniciarRecuperacion(this.email).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.toastService.success('Éxito', 'Código OTP reenviado correctamente.');
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        console.error('Error al reenviar OTP:', error);

        let mensaje = 'No se pudo reenviar el OTP, intenta nuevamente.';
        if (error.error?.message) {
          mensaje = error.error.message;
        }

        this.toastService.error('Error', mensaje);
      }
    });
  }
}
