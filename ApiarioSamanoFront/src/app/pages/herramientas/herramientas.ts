import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HerramientasService, HerramientasConProveedorResponse, HerramientasRequest, HerramientasResponse } from '../../services/almaceneService/herramientasService/herramientas-service';
import { AlmacenService, Almacen } from '../../services/almaceneService/almacen-service';
import { ProveedoresService, Proveedor } from '../../services/proveedoresService/proveedores-service';
import { ToastService } from '../../services/toastService/toast-service';
import { catchError, map, Observable, of, forkJoin } from 'rxjs';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-herramientas',
  standalone: false,
  templateUrl: './herramientas.html',
  styleUrl: './herramientas.css'
})
export class Herramientas implements OnInit {

  // Listas
  herramientas: HerramientasConProveedorResponse[] = [];
  almacenes: Almacen[] = [];
  proveedores: Proveedor[] = [];

  // Búsqueda y selección
  terminoBusqueda: string = '';
  herramientaSeleccionada: HerramientasConProveedorResponse | null = null;

  // Control de modales - IMPORTANTE: DEBEN estar en false al inicio
  mostrarFormulario: boolean = false;
  mostrarModalConfirmacion: boolean = false;
  editando: boolean = false;

  // Formulario
  formHerramienta: HerramientasRequest = {
    id: 0,
    nombre: '',
    foto: '',
    idAlmacen: 0,
    idProveedor: 0
  };

  // Manejo de imagen
  archivoSeleccionado: File | null = null;
  fotoPreview: string | null = null;

  // Estados de carga
  cargando: boolean = false;
  cargandoAlmacenes: boolean = false;
  cargandoProveedores: boolean = false;
  guardando: boolean = false;
  eliminando: boolean = false;

  // Herramienta a eliminar
  herramientaAEliminar: HerramientasConProveedorResponse | null = null;

  constructor(
    private herramientasService: HerramientasService,
    private almacenesService: AlmacenService,
    private proveedoresService: ProveedoresService,
    private toastService: ToastService,
    private cd: ChangeDetectorRef,
    private audioService:AudioService
  ) {}

  ngOnInit() {
    this.cargarHerramientasCompletas();
    this.cargarAlmacenes();
    this.cargarProveedores();
  }

  /**
   * Carga todas las herramientas combinando ambos endpoints
   * para obtener idAlmacen + información del proveedor
   */
  cargarHerramientasCompletas() {
    this.cargando = true;
    this.cd.detectChanges();
    
    // Usamos forkJoin para hacer ambas peticiones en paralelo
    forkJoin({
      herramientasConAlmacen: this.herramientasService.obtenerTodas(),
      herramientasConProveedor: this.herramientasService.obtenerTodasConProveedor()
    }).subscribe({
      next: (result) => {
        console.log('🔍 Herramientas con almacén:', result.herramientasConAlmacen.data);
        console.log('🔍 Herramientas con proveedor:', result.herramientasConProveedor.data);
        
        // Combinar la información de ambos endpoints
        this.combinarHerramientas(
          result.herramientasConAlmacen.data || [],
          result.herramientasConProveedor.data || []
        );
        
        this.cargando = false;
        this.cd.detectChanges();
        
      },
      error: (err) => {
        console.error('Error al cargar herramientas:', err);
        this.cargando = false;
        this.cd.detectChanges();
        this.toastService.error(
          'Error de carga', 
          'No se pudieron cargar las herramientas. Intente nuevamente.'
        );
      }
    });
  }

  /**
   * Combina la información de ambos endpoints
   */
  private combinarHerramientas(
    herramientasConAlmacen: any[],
    herramientasConProveedor: HerramientasConProveedorResponse[]
  ) {
    const herramientasCombinadas: HerramientasConProveedorResponse[] = [];

    // Para cada herramienta con proveedor, buscar su idAlmacen correspondiente
    herramientasConProveedor.forEach(herramientaConProveedor => {
      // Buscar la misma herramienta en la lista que tiene idAlmacen
      const herramientaConAlmacen = herramientasConAlmacen.find(
        h => h.id === herramientaConProveedor.id
      );

      // Crear objeto combinado
      const herramientaCombinada: HerramientasConProveedorResponse = {
        ...herramientaConProveedor,
        idAlmacen: herramientaConAlmacen?.idAlmacen || 0
      };

      herramientasCombinadas.push(herramientaCombinada);
    });

    this.herramientas = herramientasCombinadas;
    console.log('✅ Herramientas combinadas:', this.herramientas);
    this.cd.detectChanges();
  }

  /**
   * Carga todos los almacenes
   */
  cargarAlmacenes() {
    this.cargandoAlmacenes = true;
    this.cd.detectChanges();
    
    this.almacenesService.obtenerAlmacenes().subscribe({
      next: (response) => {
        console.log(response.data)
        this.almacenes = response.data || [];
        this.cargandoAlmacenes = false;
        this.cd.detectChanges();

      },
      error: (err) => {
        console.error('Error al cargar almacenes:', err);
        this.cargandoAlmacenes = false;
        this.cd.detectChanges();
        this.toastService.error(
          'Error de carga', 
          'No se pudieron cargar los almacenes. Intente nuevamente.'
        );
      }
    });
  }

  /**
   * Carga todos los proveedores
   */
  cargarProveedores() {
    this.cargandoProveedores = true;
    this.cd.detectChanges();
    
    this.proveedoresService.listarProveedores().subscribe({
      next: (proveedores) => {
        this.proveedores = proveedores || [];
        this.cargandoProveedores = false;
        this.cd.detectChanges();

      },
      error: (err) => {
        console.error('Error al cargar proveedores:', err);
        this.cargandoProveedores = false;
        this.cd.detectChanges();
        this.toastService.error(
          'Error de carga', 
          'No se pudieron cargar los proveedores. Intente nuevamente.'
        );
      }
    });
  }

  /**
   * Obtiene el nombre del almacén basado en el ID
   */
  obtenerNombreAlmacen(idAlmacen: number | undefined): string {
    if (!idAlmacen) return 'No asignado';
    
    const almacen = this.almacenes.find(a => a.id === idAlmacen);
    return almacen ? `${almacen.numeroSeguimiento} - ${almacen.ubicacion}` : 'No asignado';
  }

  /**
   * Filtra herramientas según el término de búsqueda
   */
  filtrarHerramientas(): HerramientasConProveedorResponse[] {
    if (!this.terminoBusqueda.trim()) {
      return this.herramientas;
    }

    const termino = this.terminoBusqueda.toLowerCase().trim();
    return this.herramientas.filter(h =>
      h.nombre?.toLowerCase().includes(termino) ||
      h.proveedor?.nombreEmpresa?.toLowerCase().includes(termino) ||
      h.id?.toString().includes(termino)
    );
  }

  /**
   * Selecciona una herramienta para ver detalles
   */
  seleccionarHerramienta(herramienta: HerramientasConProveedorResponse) {
    console.log('🎯 Herramienta seleccionada:', herramienta);
    this.herramientaSeleccionada = herramienta;
    this.cd.detectChanges();
  }

  /**
   * Abre el formulario para crear nueva herramienta
   */
  abrirFormulario() {
    this.mostrarFormulario = true;
    this.editando = false;
    this.archivoSeleccionado = null;
    this.fotoPreview = null;
    
    // Resetear formulario
    this.formHerramienta = {
      id: 0,
      nombre: '',
      foto: '',
      idAlmacen: 0,
      idProveedor: 0
    };
    this.cd.detectChanges();
  }

  /**
   * Cierra el formulario
   */
  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.editando = false;
    this.formHerramienta = {
      id: 0,
      nombre: '',
      foto: '',
      idAlmacen: 0,
      idProveedor: 0
    };
    this.archivoSeleccionado = null;
    this.fotoPreview = null;
    this.cd.detectChanges();
  }

  /**
   * Guarda o actualiza una herramienta
   */
  async guardarHerramienta() {
    // Validaciones mejoradas
    if (!this.formHerramienta.nombre?.trim()) {
      this.toastService.warning(
        'Campo requerido', 
        'El nombre de la herramienta es obligatorio'
      );
      return;
    }

    if (this.formHerramienta.nombre.trim().length < 2) {
      this.toastService.warning(
        'Nombre muy corto', 
        'El nombre debe tener al menos 2 caracteres'
      );
      return;
    }

    if (!this.formHerramienta.idAlmacen) {
      this.toastService.warning(
        'Almacén requerido', 
        'Debe seleccionar un almacén'
      );
      return;
    }

    if (!this.formHerramienta.idProveedor) {
      this.toastService.warning(
        'Proveedor requerido', 
        'Debe seleccionar un proveedor'
      );
      return;
    }

    this.guardando = true;
    this.cd.detectChanges();

    try {
      // ✅ CORRECCIÓN: Siempre enviar el ID, será null/0 para creación
      const herramientaData: HerramientasRequest = {
        id: this.formHerramienta.id || null, // Esto será 0 para nuevo, o el ID real para editar
        nombre: this.formHerramienta.nombre.trim(),
        foto: this.formHerramienta.foto || '',
        idAlmacen: this.formHerramienta.idAlmacen,
        idProveedor: this.formHerramienta.idProveedor
      };

      // Procesar imagen si hay archivo seleccionado
      if (this.archivoSeleccionado) {
        const base64 = await this.convertirArchivoABase64(this.archivoSeleccionado);
        herramientaData.foto = base64;
      }

      // ✅ CORRECCIÓN: Usar solo el método guardar para ambos casos
      this.enviarHerramienta(herramientaData);
    } catch (err) {
      console.error('Error al convertir imagen:', err);
      this.guardando = false;
      this.cd.detectChanges();
      this.toastService.error(
        'Error de imagen', 
        'No se pudo procesar la imagen. Intente con otra.'
      );
    }
  }

  /**
   * Envía la herramienta al backend - usa solo el método guardar
   */
  private enviarHerramienta(herramientaData: HerramientasRequest) {
    console.log('Enviando herramienta:', herramientaData);
    
    // ✅ CORRECCIÓN: Usar siempre el mismo método guardar
    this.herramientasService.guardar(herramientaData).subscribe({
      next: (response) => {
        this.guardando = false;
        this.cd.detectChanges();
        this.cargarHerramientasCompletas();
        this.cerrarFormulario();
        
        // Mensaje según si es creación o actualización
        if (herramientaData.id && herramientaData.id > 0) {
          this.toastService.success(
            'Actualización exitosa', 
            'Herramienta actualizada correctamente'
          );
        } else {
          this.toastService.success(
            'Creación exitosa', 
            'Herramienta creada correctamente'
          );
        }
      },
      error: (err) => {
        console.error('Error al guardar/actualizar herramienta:', err);
        this.guardando = false;
        this.cd.detectChanges();
        const errorMessage = this.obtenerMensajeError(err);
        
        if (herramientaData.id && herramientaData.id > 0) {
          this.toastService.error(
            'Error al actualizar', 
            errorMessage
          );
        } else {
          this.toastService.error(
            'Error al guardar', 
            errorMessage
          );
        }
      }
    });
  }

  /**
   * Obtiene mensajes de error más descriptivos
   */
  private obtenerMensajeError(err: any): string {
    if (err.error?.descripcion) {
      return err.error.descripcion;
    }
    
    if (err.status === 0) {
      return 'No hay conexión con el servidor. Verifique su conexión a internet.';
    }
    
    if (err.status === 400) {
      return 'Datos inválidos. Verifique la información ingresada.';
    }
    
    if (err.status === 404) {
      return 'Recurso no encontrado. La herramienta o almacén no existe.';
    }
    
    if (err.status === 500) {
      return 'Error interno del servidor. Intente nuevamente más tarde.';
    }
    
    return 'Ocurrió un error inesperado. Intente nuevamente.';
  }

  /**
   * Edita una herramienta
   */
  editarHerramienta(herramienta: HerramientasConProveedorResponse) {
    this.mostrarFormulario = true;
    this.editando = true;
    this.archivoSeleccionado = null;
    this.fotoPreview = null;

    // Cargar datos de la herramienta
    this.formHerramienta = {
      id: herramienta.id || 0,
      nombre: herramienta.nombre || '',
      foto: herramienta.foto || '',
      idAlmacen: herramienta.idAlmacen || 0,
      idProveedor: herramienta.proveedor?.id || 0
    };

    // Mostrar preview de foto existente
    if (herramienta.foto) {
      this.fotoPreview = this.getFotoUrl(herramienta.foto);
    }

    this.cd.detectChanges();
    this.toastService.info(
      'Modo edición', 
      `Editando herramienta: ${herramienta.nombre}`
    );
  }

  /**
   * Abre el modal de confirmación para eliminar una herramienta
   */
  abrirModalConfirmacion(herramienta: HerramientasConProveedorResponse, event?: Event) {
    this.audioService.play('assets/audios/Advertencia.mp3',0.6)

    if (event) {
      event.stopPropagation();
    }
    this.herramientaAEliminar = herramienta;
    this.mostrarModalConfirmacion = true;
    this.cd.detectChanges();
  }

  /**
   * Cierra el modal de confirmación
   */
  cerrarModalConfirmacion() {
    this.mostrarModalConfirmacion = false;
    this.herramientaAEliminar = null;
    this.eliminando = false;
    this.cd.detectChanges();
  }

  /**
   * Confirma la eliminación de la herramienta
   */
  confirmarEliminacion() {
    if (!this.herramientaAEliminar?.id) {
      this.toastService.warning(
        'Selección requerida', 
        'No se ha seleccionado ninguna herramienta para eliminar'
      );
      return;
    }

    this.eliminando = true;
    this.cd.detectChanges();

    this.herramientasService.eliminar(this.herramientaAEliminar.id).subscribe({
      next: () => {
        this.eliminando = false;
        this.cd.detectChanges();
        this.cargarHerramientasCompletas();
        
        // Si la herramienta eliminada era la seleccionada, limpiar selección
        if (this.herramientaSeleccionada?.id === this.herramientaAEliminar?.id) {
          this.herramientaSeleccionada = null;
          this.cd.detectChanges();
        }
        
        this.cerrarModalConfirmacion();
        this.toastService.success(
          'Eliminación exitosa', 
          'Herramienta eliminada correctamente'
        );
      },
      error: (err) => {
        console.error('Error al eliminar herramienta:', err);
        this.eliminando = false;
        this.cd.detectChanges();
        const errorMessage = this.obtenerMensajeError(err);
        this.toastService.error(
          'Error al eliminar', 
          errorMessage
        );
      }
    });
  }

  /**
   * Maneja la selección de archivo
   */
  onFileSelected(event: any) {
    const file: File = event.target.files[0];

    if (file) {
      // Validar tipo
      if (!file.type.startsWith('image/')) {
        this.toastService.warning(
          'Archivo no válido', 
          'Por favor selecciona un archivo de imagen válido (JPEG, PNG, etc.)'
        );
        return;
      }

      // Validar tamaño (máximo 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.warning(
          'Archivo muy grande', 
          'La imagen no debe superar los 5MB'
        );
        return;
      }

      this.archivoSeleccionado = file;

      // Crear preview
      const reader = new FileReader();
      reader.onload = () => {
        this.fotoPreview = reader.result as string;
        this.cd.detectChanges();
      };
      reader.readAsDataURL(file);

      this.toastService.info(
        'Imagen cargada', 
        'Imagen seleccionada correctamente'
      );
    }
  }

  /**
   * Elimina la foto seleccionada
   */
  eliminarFoto() {
    this.archivoSeleccionado = null;
    this.fotoPreview = null;
    this.formHerramienta.foto = '';

    // Resetear input file
    const fileInput = document.getElementById('foto') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }

    this.cd.detectChanges();
    this.toastService.info(
      'Imagen removida', 
      'Imagen eliminada correctamente'
    );
  }

  /**
   * Obtiene la URL de la foto para mostrar
   */
  getFotoUrl(foto: string | undefined): string {
    if (!foto) {
      return '';
    }

    // Si ya es una URL base64 completa
    if (foto.startsWith('data:')) {
      return foto;
    }

    // Si es solo el string base64, agregar el prefijo
    return `data:image/jpeg;base64,${foto}`;
  }

  /**
   * Convierte un archivo a Base64
   */
  private convertirArchivoABase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        // Extraer solo la parte base64 (sin el prefijo data:image/...)
        const base64 = result.split(',')[1];
        resolve(base64);
      };
      reader.onerror = error => reject(error);
      reader.readAsDataURL(file);
    });
  }

  /**
   * Método para obtener el ícono placeholder según el nombre de la herramienta
   */
  getPlaceholderIcon(nombre: string | undefined): string {
    if (!nombre) return '🔧';
    
    const nombreLower = nombre.toLowerCase();
    
    if (nombreLower.includes('extractor') || nombreLower.includes('miel')) return '🍯';
    if (nombreLower.includes('cuchillo') || nombreLower.includes('cuchilla')) return '🔪';
    if (nombreLower.includes('guante') || nombreLower.includes('protección')) return '🧤';
    if (nombreLower.includes('fumigador') || nombreLower.includes('humo')) return '💨';
    if (nombreLower.includes('panal') || nombreLower.includes('cuadro')) return '🐝';
    if (nombreLower.includes('traje') || nombreLower.includes('protector')) return '👨‍🚀';
    if (nombreLower.includes('cepillo') || nombreLower.includes('brocha')) return '🖌️';
    if (nombreLower.includes('pinza') || nombreLower.includes('tenaza')) return '🔧';
    
    return '🔧';
  }
}