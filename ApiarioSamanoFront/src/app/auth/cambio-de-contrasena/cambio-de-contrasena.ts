import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { DataService } from '../../services/data/data';
import { ToastService } from '../../services/toastService/toast-service'; // Importamos el toast

@Component({
  selector: 'app-cambio-de-contrasena',
  standalone: false,
  templateUrl: './cambio-de-contrasena.html',
  styleUrls: ['./cambio-de-contrasena.css']
})
export class CambioDeContrasena implements OnInit {

  email: string = '';
  nuevaContrasena: string = '';
  confirmarContrasena: string = '';
  otp: string = '';
  cargando: boolean = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private dataService: DataService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.email = this.dataService.getEmail();
    this.otp = this.dataService.getOtp();
  }

  cambiarContrasena(): void {
    // Validaciones
    if (!this.nuevaContrasena || !this.confirmarContrasena) {
      this.toastService.warning('Atención', 'Debes completar todos los campos.');
      return;
    }

    if (this.nuevaContrasena !== this.confirmarContrasena) {
      this.toastService.error('Error', 'Las contraseñas no coinciden.');
      return;
    }

    this.cargando = true;

    this.authService.cambiarContrasena(this.email, this.nuevaContrasena, this.otp)
      .subscribe({
        next: (response) => {
          this.cargando = false;
          this.dataService.clearData();
          this.toastService.success('Éxito', 'Contraseña cambiada correctamente. Ahora puedes iniciar sesión.');
          this.router.navigate(['login']);
        },
        error: (error) => {
          this.cargando = false;
          console.error('Error completo:', error);

          let mensajeError = '❌ Error al cambiar la contraseña.';

          if (error.status === 404) {
            mensajeError = '❌ Usuario no encontrado o OTP inválido.';
          } else if (error.error) {
            if (typeof error.error === 'string') {
              mensajeError = '❌ Usuario o OTP inválido.';
            } else if (error.error.message) {
              mensajeError = error.error.message;
            } else {
              mensajeError = JSON.stringify(error.error);
            }
          }

          this.toastService.error('Error', mensajeError);
        }
      });
  }

  reenviarContrasena(): void {
    // Podemos reutilizar cambiarContrasena para enviar de nuevo si hace falta
    this.cambiarContrasena();
  }
}
