import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';
import { Router } from '@angular/router';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrls: ['./header.css'],
  standalone: true
})
export class Header implements OnInit {
  showProfile = false;
  userName = '';
  userEmail = '';
  userRole = '';
  userId = '';
  userEstado = false;
  nombreCompleto = '';

  constructor(private authService: AuthService, private router:Router, private audioService : AudioService) {}

  ngOnInit() {
    this.cargarDatosUsuario();
  }

  cargarDatosUsuario() {
    const datos = this.authService.getAllJwtInfo();
    if (datos) {
      this.userName = datos.nombre || 'Usuario';
      this.userEmail = datos.email || 'No disponible';
      this.userRole = datos.rol || 'No asignado';
      this.userId = datos.id || 'N/A';
      this.userEstado = datos.estado || false;
      this.nombreCompleto = datos.nombreCompleto || datos.nombre || 'Usuario';
      
      // Si quieres mostrar nombre + apellido paterno en el botón
      if (datos.nombre && datos.apellidoPa) {
        this.userName = `${datos.nombre} ${datos.apellidoPa}`;
      } else if (datos.nombre) {
        this.userName = datos.nombre;
      }
    } else {
      this.userName = 'Usuario';
      this.userEmail = 'No se pudo cargar la información';
      this.userRole = 'No disponible';
      this.userId = 'N/A';
      this.userEstado = false;
      this.nombreCompleto = 'Usuario';
    }
  }

  getInitials(): string {
    if (this.nombreCompleto && this.nombreCompleto !== 'Usuario') {
      return this.nombreCompleto
        .split(' ')
        .map(name => name[0])
        .join('')
        .toUpperCase()
        .substring(0, 2);
    }
    return 'US';
  }

  toggleProfile() {
    this.showProfile = !this.showProfile;
    this.audioService.play('assets/audios/boton.mp3',0.6)

    // Recargar datos cada vez que se abre el perfil
    if (this.showProfile) {
      this.cargarDatosUsuario();
    }
  }

  closeProfile() {
    this.showProfile = false;
  }

  logout() {
    this.audioService.play('assets/audios/boton.mp3',0.6)
    this.authService.cerrarSesion();
    this.audioService.play('assets/audios/Despedida.mp3')
    this.router.navigate(['/login']);
    

  }
}