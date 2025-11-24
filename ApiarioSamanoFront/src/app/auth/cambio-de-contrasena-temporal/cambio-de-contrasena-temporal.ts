import { Component, OnInit } from '@angular/core';
import { AuthService, ResponseDTO } from '../../services/auth/auth.service';
import { DataService } from '../../services/data/data';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cambio-de-contrasena-temporal',
  standalone: false,
  templateUrl: './cambio-de-contrasena-temporal.html',
  styleUrls: ['./cambio-de-contrasena-temporal.css']
})
export class CambioDeContrasenaTemporal implements OnInit {

  email: string = '';
  contrasenaTemporal: string = '';
  nuevaContrasena: string = '';
  confirmarContrasena: string = '';
  mensaje: string = '';
  cargando: boolean = false;

  constructor(
    private authService: AuthService,
    private dataService: DataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.email = this.dataService.getEmail();
    this.contrasenaTemporal = this.dataService.getContrasenaTemporal(); 
    console.log('Email y contraseña temporal:', this.email, this.contrasenaTemporal);
  }

  cambiarContrasenaTemporal(): void {
    // Validaciones básicas
    if (!this.email || !this.contrasenaTemporal || !this.nuevaContrasena) {
      this.mensaje = '⚠️ Todos los campos son obligatorios.';
      return;
    }

    if (this.nuevaContrasena !== this.confirmarContrasena) {
      this.mensaje = '⚠️ Las contraseñas no coinciden.';
      return;
    }

    this.cargando = true;
    this.mensaje = 'Procesando solicitud...';

    this.authService.cambiarContrasenaTemporal(this.email, this.contrasenaTemporal, this.nuevaContrasena)
      .subscribe({
        next: (response: ResponseDTO<string>) => {
          this.cargando = false;
          this.mensaje = response.message || '✅ Contraseña cambiada exitosamente. Por favor, inicia sesión con tu nueva contraseña.';
          this.router.navigate(['home']);
        },
        error: (error) => {
          this.cargando = false;
          console.error('Error completo:', error);

          // Mensaje por defecto
          let mensajeError = '❌ Error al cambiar la contraseña temporal.';

          if (error.status === 404) {
            // Usuario no encontrado
            mensajeError = '❌ Usuario no encontrado.';
          } else if (error.error) {
            if (typeof error.error === 'string') {
              // Backend devuelve HTML o string
              mensajeError = '❌ Usuario o contraseña temporal incorrectos.';
            } else if (error.error.message) {
              // Backend devuelve objeto con mensaje
              mensajeError = error.error.message;
            } else {
              // Respuesta compleja
              mensajeError = JSON.stringify(error.error);
            }
          }

          this.mensaje = mensajeError;
        }
      });
  }
}
