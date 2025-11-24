import { Component, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { UsuariosService, Usuario } from '../../services/usuariosService/usuarios-service';
import { ToastService } from '../../services/toastService/toast-service';
import { AudioService } from '../../services/Audio/audio-service';

@Component({
  selector: 'app-usuarios',
  standalone: false,
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {

  usuarios: Usuario[] = [];
  terminoBusqueda: string = '';
  mostrarFormulario: boolean = false;
  editando: boolean = false;
  mensaje: string = '';
  cargando: boolean = false;
  usuarioLogueadoEmail: string | null = null;

  usuarioSeleccionado: Usuario = {
    id:0,
    nombre: '',
    apellidoPa: '',
    apellidoMa: '',
    email: '',
    contrasena: '',
    rol: ''
  };

  constructor(
    private usuariosService: UsuariosService,
    private audioService : AudioService,
    private toastService: ToastService,
    private ngZone: NgZone,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
     const token = localStorage.getItem('token');
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.usuarioLogueadoEmail = payload.sub; // El email en tu JWT va en "sub"
      console.log("Usuario logueado:", this.usuarioLogueadoEmail);
    } catch (error) {
      console.error("Error decodificando token:", error);
    }
    
  }
    this.cargarUsuarios();
  }
  esUsuarioLogueado(usuario: Usuario): boolean {
  return usuario.email === this.usuarioLogueadoEmail;
}

  cargarUsuarios(): void {
    this.cargando = true;
    this.cd.detectChanges();

    this.usuariosService.obtenerUsuarios().subscribe({
      next: (response) => {
        this.ngZone.run(() => {
          this.usuarios = response.data || [];
          console.log('Usuarios cargados:', this.usuarios);
          this.cargando = false;
          this.cd.detectChanges();
        });
      },
      error: (error) => {
        this.ngZone.run(() => {
          this.cargando = false;
          this.toastService.error('Error', 'No se pudieron cargar los usuarios');
          this.cd.detectChanges();
        });
      }
    });
  }

  filtrarUsuarios(): Usuario[] {
    if (!this.usuarios) return [];
    if (!this.terminoBusqueda.trim()) return this.usuarios;

    const termino = this.terminoBusqueda.toLowerCase();
    return this.usuarios.filter(u =>
      (u.nombre + ' ' + u.apellidoPa + ' ' + u.apellidoMa)
        .toLowerCase()
        .includes(termino)
    );
  }

  abrirFormulario(usuario?: Usuario): void {
    this.ngZone.run(() => {
      this.mostrarFormulario = true;
      if (usuario) {
        this.editando = true;
        this.usuarioSeleccionado = { ...usuario };
      } else {
        this.editando = false;
        this.usuarioSeleccionado = {
          nombre: '',
          apellidoPa: '',
          apellidoMa: '',
          email: '',
          contrasena: '',
          rol: ''
        };
      }
      this.cd.detectChanges();
    });
  }

  cerrarFormulario(): void {
    this.ngZone.run(() => {
      this.mostrarFormulario = false;
      this.editando = false;
      this.cd.detectChanges();
    });
  }
guardarUsuario(): void {
  this.cargando = true;
  this.cd.detectChanges();

  if (this.editando) {
    this.actualizarUsuario();
  } else {
    this.crearUsuario();
  }
}

actualizarUsuario(): void {
  // Verificar que tenemos el email para actualizar
  if (!this.usuarioSeleccionado.email) {
    console.error('Error: No se puede actualizar sin email');
    this.toastService.error('Error', 'No se puede identificar el usuario');
    this.cargando = false;
    this.cd.detectChanges();
    return;
  }

  this.usuariosService.actualizarUsuarioPorEmail(
    this.usuarioSeleccionado.email,
    this.usuarioSeleccionado
  ).subscribe({
    next: (res) => {
      this.ngZone.run(() => {
        const index = this.usuarios.findIndex(u => u.email === this.usuarioSeleccionado.email);
        if (index !== -1) {
          // Usar los datos actualizados del servidor si están disponibles
          this.usuarios[index] = { ...(res.data || this.usuarioSeleccionado) };
        }
        this.cerrarFormulario();
        this.cargando = false;
        this.toastService.success('Éxito', 'Usuario actualizado correctamente');
        this.cd.detectChanges();
      });
    },
    error: (err) => {
      this.ngZone.run(() => {
        console.error('Error completo al actualizar usuario:', err);
        this.cargando = false;
        let mensajeError = 'No se pudo actualizar el usuario';
        
        // Mensajes más específicos según el error
        if (err.status === 404) {
          mensajeError = 'Usuario no encontrado';
        } else if (err.status === 400) {
          mensajeError = 'Datos inválidos para la actualización';
        } else if (err.status === 500) {
          mensajeError = 'Error interno del servidor';
        } else if (err.status === 0) {
          mensajeError = 'Error de conexión con el servidor';
        }
        
        this.toastService.error('Error', mensajeError);
        this.cd.detectChanges();
      });
    }
  });
}

crearUsuario(): void {
  this.usuariosService.crearUsuario(this.usuarioSeleccionado).subscribe({
    next: (respuesta) => {
      this.ngZone.run(() => {
        // Usar los datos del servidor si están disponibles, o los locales
        const nuevoUsuario = { ...(respuesta.data || this.usuarioSeleccionado) };
        this.usuarios.push(nuevoUsuario);
        this.cerrarFormulario();
        this.cargando = false;
        this.toastService.success('Éxito', 'Usuario creado correctamente');
        this.cd.detectChanges();
      });
    },
    error: (err) => {
      this.ngZone.run(() => {
        console.error('Error completo al crear usuario:', err);
        this.cargando = false;
        let mensajeError = 'No se pudo crear el usuario';
        
        // Mensajes más específicos según el error
        if (err.status === 409) {
          mensajeError = 'El usuario ya existe';
        } else if (err.status === 400) {
          mensajeError = 'Datos inválidos para crear el usuario';
        } else if (err.status === 500) {
          mensajeError = 'Error interno del servidor';
        } else if (err.status === 0) {
          mensajeError = 'Error de conexión con el servidor';
        }
        
        this.toastService.error('Error', mensajeError);
        this.cd.detectChanges();
      });
    }
  });
}

  editarUsuario(usuario: Usuario): void {
    this.abrirFormulario(usuario);
  }

  eliminarUsuario(usuario: Usuario): void {
  this.cargando = true;
  this.cd.detectChanges();

  this.usuariosService.obtenerUsuarioPorEmail(usuario.email).subscribe({
    next: (res) => {
      this.ngZone.run(() => {
        const id = res.data?.id;
        if (id) {
          this.usuariosService.eliminarUsuario(id).subscribe({
            next: () => {
              this.ngZone.run(() => {
                this.usuarios = this.usuarios.filter(u => u.email !== usuario.email);
                this.cargando = false;
                this.toastService.success('Éxito', 'Usuario eliminado correctamente');
                this.cd.detectChanges();
              });
            },
            error: (err) => {
              this.ngZone.run(() => {
                console.error('Error al eliminar usuario:', err);
                this.cargando = false;
                this.toastService.error('Error', 'No se pudo eliminar el usuario');
                this.cd.detectChanges();
              });
            }
          });
        } else {
          console.error('⚠️ No se encontró el ID del usuario para eliminar.');
          this.cargando = false;
          this.toastService.warning('Advertencia', 'No se encontró el usuario');
          this.cd.detectChanges();
        }
      });
    },
    error: (err) => {
      this.ngZone.run(() => {
        console.error('Error al buscar usuario por email:', err);
        this.cargando = false;
        this.toastService.error('Error', 'Error al buscar usuario');
        this.cd.detectChanges();
      });
    }
  });
}




  // Variables para el modal de confirmación
mostrarModalConfirmacion: boolean = false;
usuarioAEliminar: Usuario | null = null;

// Métodos para manejar la confirmación
abrirModalConfirmacion(usuario: Usuario): void {
  this.audioService.play('assets/audios/Advertencia.mp3',0.6);
  this.ngZone.run(() => {
    this.usuarioAEliminar = usuario;
    this.mostrarModalConfirmacion = true;
    this.cd.detectChanges();
  });
}

cerrarModalConfirmacion(): void {
  this.ngZone.run(() => {
    this.mostrarModalConfirmacion = false;
    this.usuarioAEliminar = null;
    this.cd.detectChanges();
  });
}

confirmarEliminacion(): void {
  if (this.usuarioAEliminar) {
    this.eliminarUsuario(this.usuarioAEliminar);
    this.cerrarModalConfirmacion();
  }
}

}
