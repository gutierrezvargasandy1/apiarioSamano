import { Component } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';  
import { Router } from '@angular/router';
import { DataService } from '../../services/data/data';
import { ToastService } from '../../services/toastService/toast-service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPassword {

  email: string = '';
  cargando: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private dataService: DataService,
    private toastService: ToastService
  ) {}

  inicioRecuperacion(): void {
    // Validar que el email tenga contenido real
    if (!this.email || this.email.trim() === '') {
      this.toastService.warning('Atención', 'Debes ingresar un correo electrónico.');
      return;
    }

    this.cargando = true;
    this.dataService.setEmail(this.email);

    this.authService.iniciarRecuperacion(this.email).subscribe({
      next: (response) => {
        this.cargando = false;
        this.toastService.success('Éxito', 'Se ha enviado el código de verificación a tu correo.');
        this.router.navigate(['verificacion-otp']);
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;

        // Mostrar el error en consola para debugging
        console.error('Error completo:', error);

        // Obtener mensaje amigable
        const mensaje = this.obtenerMensajeError(error);

        // Mostrar toast de error
        this.toastService.error('Error', mensaje);
      }
    });
  }

  // Función centralizada para obtener un mensaje de error amigable
  private obtenerMensajeError(error: HttpErrorResponse): string {
    // Error de red o backend inalcanzable
    if (!navigator.onLine) {
      return 'No hay conexión a internet. Revisa tu red.';
    }

    // Timeout o error de servidor
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Intenta más tarde.';
    }

    // Usuario no encontrado
    if (error.status === 404) {
      return 'El usuario no existe.';
    }

    // Error con mensaje específico del backend
    if (error.error) {
      if (typeof error.error === 'string') {
        // HTML o texto plano
        return 'El usuario no existe.';
      } else if (error.error.message) {
        return error.error.message;
      } else {
        // Cualquier otro objeto complejo
        return 'Ocurrió un error inesperado. Intenta de nuevo.';
      }
    }

    // Mensaje por defecto
    return 'Ocurrió un error inesperado. Intenta de nuevo.';
  }

  regreso(): void {
    this.router.navigate(['login']);
  }

}
