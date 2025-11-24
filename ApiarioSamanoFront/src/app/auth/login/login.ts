import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { DataService } from '../../services/data/data';
import { ToastService } from '../../services/toastService/toast-service';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {

  email = '';
  contrasena = '';
  errorMessage: string = '';
  isSubmitting: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private dataService: DataService,
    private toastService: ToastService,
    private audioService: AudioService
  ) {}

  ngOnInit(): void {}

  login() {
    this.errorMessage = '';

    // Validaciones
    if (!this.email.trim() || !this.contrasena.trim()) {
      this.toastService.warning('Atención', 'Por favor, ingresa correo y contraseña.');
      return;
    }

    if (!this.validarEmail(this.email.trim())) {
      this.toastService.warning('Atención', 'Ingresa un correo válido.');
      return;
    }

    if (this.contrasena.length < 6) {
      this.toastService.warning('Atención', 'La contraseña debe tener al menos 6 caracteres.');
      return;
    }

    this.isSubmitting = true;

    const credentials = {
      email: this.email.trim(),
      contrasena: this.contrasena
    };

    this.authService.login(credentials).subscribe({
      next: (res) => {
        this.isSubmitting = false;

        if (res.data) {
          this.authService.guardarToken(res.data);

          if (this.authService.getEstadoFromToken() === true) {
            this.dataService.setContrasenaTemporal(this.contrasena);
            this.dataService.setEmail(this.email);
            this.toastService.success('Éxito', 'Inicia sesión con tu contraseña temporal.');
            this.router.navigate(['cambiar-contrasena-temporal']);
          } else {
            this.audioService.play('assets/audios/Bienvenida.mp3', 0.6);
            this.router.navigate(['home']);
          }
        } else {
          this.toastService.error('Error', res.message || 'Credenciales incorrectas.');
        }
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        console.error('Error en login:', err);

        let mensaje = 'Ocurrió un error, intenta nuevamente.';

        if (err.status === 401) {
          mensaje = 'Credenciales incorrectas.';
        } else if (err.error) {
          if (typeof err.error === 'string') {
            mensaje = 'Usuario o contraseña inválidos.';
          } else if (err.error.message) {
            mensaje = err.error.message;
          } else {
            mensaje = JSON.stringify(err.error);
          }
        }

        this.toastService.error('Error', mensaje);
      }
    });
  }

  validarEmail(email: string): boolean {
    // Expresión regular simple para validar correo
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }

  validarCampos() {
    if (!this.email.trim() || !this.contrasena.trim()) {
      this.errorMessage = 'Por favor, ingresa correo y contraseña.';
    } else {
      this.errorMessage = '';
    }
  }

  irARecuperacionContrasena() {
    this.router.navigate(['forgot-password']);
  }
}
