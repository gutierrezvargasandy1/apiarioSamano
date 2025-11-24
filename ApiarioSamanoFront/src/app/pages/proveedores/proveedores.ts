import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ProveedoresService, Proveedor, ProveedorRequest } from '../../services/proveedoresService/proveedores-service';
import { ToastService } from '../../services/toastService/toast-service';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-proveedores',
  standalone: false,  
  templateUrl: './proveedores.html',
  styleUrls: ['./proveedores.css']
})
export class Proveedores implements OnInit {

  // Lista de proveedores
  proveedores: Proveedor[] = [];
  
  // Término de búsqueda
  terminoBusqueda: string = '';
  
  // Control del modal
  mostrarFormulario: boolean = false;
  editando: boolean = false;
  
  proveedorSeleccionado: ProveedorRequest = {
    nombreEmpresa: '',
    nombreRepresentante: '',
    numTelefono: '',
    materialProvee: ''
  };
  
  // Archivo de imagen seleccionado (solo para frontend)
  archivoSeleccionado: File | null = null;
  // Base64 de la imagen para preview
  imagenPreview: string | null = null;

  // Estados de carga
  cargando: boolean = false;
  guardando: boolean = false;

  // Para manejar el ID durante edición
  proveedorEditandoId: number | null = null;

  // Modal de confirmación
  mostrarModalConfirmacion: boolean = false;
  proveedorAEliminar: Proveedor | null = null;
  eliminando: boolean = false;

  constructor(
    private proveedoresService: ProveedoresService,
    private toastService: ToastService,
    private cd: ChangeDetectorRef,
    private audioService : AudioService
  ) {}

  ngOnInit() {
    this.cargarProveedores();
  }

  /**
   * Carga todos los proveedores desde el backend
   */
  cargarProveedores() {
    this.cargando = true;
    this.cd.detectChanges(); // Forzar detección de cambios para mostrar loading

    this.proveedoresService.listarProveedores().subscribe({
      next: (data) => {
        this.proveedores = data;
        this.cargando = false;
        this.cd.detectChanges(); // Actualizar vista después de cargar datos
      },
      error: (err) => {
        console.error('Error al cargar proveedores:', err);
        this.cargando = false;
        this.cd.detectChanges(); // Actualizar vista después del error
      }
    });
  }

  /**
   * Filtra proveedores según el término de búsqueda
   */
  filtrarProveedores(): Proveedor[] {
    if (!Array.isArray(this.proveedores)) return [];
    
    if (!this.terminoBusqueda.trim()) {
      return this.proveedores;
    }

    const termino = this.terminoBusqueda.toLowerCase().trim();
    
    return this.proveedores.filter(p =>
      p.nombreEmpresa?.toLowerCase().includes(termino) ||
      p.nombreRepresentante?.toLowerCase().includes(termino) ||
      p.numTelefono?.includes(termino) ||
      p.materialProvee?.toLowerCase().includes(termino)
    );
  }

  /**
   * Abre el modal para crear o editar un proveedor
   */
  abrirFormulario(proveedor?: Proveedor) {
    this.mostrarFormulario = true;
    this.archivoSeleccionado = null;
    this.imagenPreview = null;
    
    if (proveedor) {
      // Modo edición
      this.editando = true;
      this.proveedorEditandoId = proveedor.id || null;
      this.proveedorSeleccionado = { 
        nombreEmpresa: proveedor.nombreEmpresa || '',
        nombreRepresentante: proveedor.nombreRepresentante || '',
        numTelefono: proveedor.numTelefono || '',
        materialProvee: proveedor.materialProvee || '',
        fotografia: proveedor.fotografia
      };
      
      // Si hay foto existente, crear preview
      if (proveedor.fotografia) {
        this.imagenPreview = this.getFotoUrl(proveedor.fotografia);
      }
    } else {
      // Modo creación
      this.editando = false;
      this.proveedorEditandoId = null;
      this.proveedorSeleccionado = {
        nombreEmpresa: '',
        nombreRepresentante: '',
        numTelefono: '',
        materialProvee: ''
      };
    }
    
    this.cd.detectChanges(); // Forzar actualización del modal
  }

  /**
   * Cierra el modal y resetea el formulario
   */
  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.proveedorSeleccionado = {
      nombreEmpresa: '',
      nombreRepresentante: '',
      numTelefono: '',
      materialProvee: ''
    };
    this.archivoSeleccionado = null;
    this.imagenPreview = null;
    this.editando = false;
    this.proveedorEditandoId = null;
    
    this.cd.detectChanges(); // Forzar actualización después de cerrar
  }

  /**
   * Guarda o actualiza un proveedor
   */
  guardarProveedor() {
    // Validación básica
    if (!this.proveedorSeleccionado.nombreEmpresa?.trim()) {
      this.toastService.warning('Validación', 'El nombre de la empresa es obligatorio');
      return;
    }

    if (!this.proveedorSeleccionado.nombreRepresentante?.trim()) {
      this.toastService.warning('Validación', 'El nombre del representante es obligatorio');
      return;
    }

    if (!this.proveedorSeleccionado.numTelefono?.trim()) {
      this.toastService.warning('Validación', 'El número de teléfono es obligatorio');
      return;
    }

    if (!this.proveedorSeleccionado.materialProvee?.trim()) {
      this.toastService.warning('Validación', 'El material que provee es obligatorio');
      return;
    }

    this.guardando = true;
    this.cd.detectChanges(); // Actualizar estado de guardando

    // Preparar el objeto para enviar
    const proveedorParaEnviar: ProveedorRequest = {
      nombreEmpresa: this.proveedorSeleccionado.nombreEmpresa.trim(),
      nombreRepresentante: this.proveedorSeleccionado.nombreRepresentante.trim(),
      numTelefono: this.proveedorSeleccionado.numTelefono.trim(),
      materialProvee: this.proveedorSeleccionado.materialProvee.trim(),
      fotografia: this.proveedorSeleccionado.fotografia
    };

    console.log('📤 Enviando proveedor:', proveedorParaEnviar);

    if (this.editando && this.proveedorEditandoId) {
      // Actualizar proveedor existente
      this.proveedoresService.actualizarProveedor(
        this.proveedorEditandoId, 
        proveedorParaEnviar
      ).subscribe({
        next: () => {
          this.guardando = false;
          this.cargarProveedores();
          this.cerrarFormulario();
          this.cd.detectChanges(); // Actualizar después de guardar
        },
        error: (err) => {
          console.error('Error al actualizar proveedor:', err);
          this.guardando = false;
          this.cd.detectChanges(); // Actualizar estado de error
        }
      });
    } else {
      // Crear nuevo proveedor
      this.proveedoresService.crearProveedor(proveedorParaEnviar).subscribe({
        next: () => {
          this.guardando = false;
          this.cargarProveedores();
          this.cerrarFormulario();
          this.cd.detectChanges(); // Actualizar después de crear
        },
        error: (err) => {
          console.error('Error al crear proveedor:', err);
          this.guardando = false;
          this.cd.detectChanges(); // Actualizar estado de error
        }
      });
    }
  }

  /**
   * Abre el formulario en modo edición
   */
  editarProveedor(proveedor: Proveedor) {
    this.abrirFormulario(proveedor);
  }

  /**
   * Maneja la selección de un archivo de imagen y lo convierte a Base64
   */
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    
    if (file) {
      // Validar tipo de archivo
      if (!file.type.startsWith('image/')) {
        this.toastService.error('Error', 'Por favor selecciona un archivo de imagen válido');
        return;
      }

      // Validar tamaño (máximo 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.error('Error', 'La imagen no debe superar los 5MB');
        return;
      }

      this.archivoSeleccionado = file;
      this.cd.detectChanges(); // Actualizar estado del archivo seleccionado

      // Convertir a Base64 para el backend
      const reader = new FileReader();
      reader.onload = () => {
        const base64String = reader.result as string;
        
        // Remover el prefijo "data:image/...;base64," para enviar Base64 puro
        const base64Data = base64String.split(',')[1];
        
        // Guardar Base64 puro para enviar al backend
        this.proveedorSeleccionado.fotografia = base64Data;
        
        // Guardar Base64 completo para preview
        this.imagenPreview = base64String;
        
        this.cd.detectChanges(); // Forzar actualización del preview
        this.toastService.success('Éxito', 'Imagen cargada correctamente', 2000);
      };
      
      reader.onerror = () => {
        this.toastService.error('Error', 'Error al leer el archivo de imagen');
        this.cd.detectChanges(); // Actualizar estado de error
      };
      
      reader.readAsDataURL(file);
    }
  }

  /**
   * Elimina la foto seleccionada
   */
  eliminarFoto() {
    this.archivoSeleccionado = null;
    this.imagenPreview = null;
    this.proveedorSeleccionado.fotografia = undefined;
    
    // Resetear el input file
    const fileInput = document.getElementById('foto') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
    
    this.cd.detectChanges(); // Forzar actualización después de eliminar
    this.toastService.info('Información', 'Imagen eliminada');
  }

  /**
   * Maneja cambios en la búsqueda
   */
  onBuscarChange() {
    this.cd.detectChanges(); // Forzar actualización del filtro en tiempo real
  }

  /**
   * Actualiza un campo del proveedor seleccionado - CORREGIDO
   */
  actualizarCampo(campo: keyof ProveedorRequest, valor: string) {
    // Usar una aserción de tipo para evitar el error de TypeScript
    (this.proveedorSeleccionado as any)[campo] = valor;
    this.cd.detectChanges(); // Actualizar cambios en el formulario
  }

  /**
   * Obtiene la URL de la foto para mostrar en el template
   */
  getFotoUrl(fotografia: string | undefined): string {
    if (!fotografia) {
      return '';
    }

    // Si ya es una URL base64 completa (con prefijo data:)
    if (fotografia.startsWith('data:')) {
      return fotografia;
    }
    
    // Si es solo el string base64 puro (del backend), agregar el prefijo para preview
    return `data:image/jpeg;base64,${fotografia}`;
  }

  /**
   * Obtiene el placeholder de imagen según el tipo de material
   */
  getPlaceholderIcon(materialProvee: string): string {
    const material = materialProvee?.toLowerCase() || '';
    
    if (material.includes('madera')) return '🪵';
    if (material.includes('cemento') || material.includes('concreto')) return '🏗️';
    if (material.includes('herramienta')) return '🔧';
    if (material.includes('pintura')) return '🎨';
    if (material.includes('electricidad') || material.includes('eléctrico')) return '⚡';
    
    return '🏢'; // Icono por defecto
  }

  /**
   * TrackBy function para optimizar el *ngFor
   */
  trackByProveedorId(index: number, proveedor: Proveedor): number {
    return proveedor.id || index;
  }

  /**
   * Abre el modal de confirmación para eliminar proveedor
   */
  abrirModalConfirmacion(proveedor: Proveedor) {
    this.audioService.play('assets/audios/Advertencia.mp3',0.6)
    this.proveedorAEliminar = proveedor;
    this.mostrarModalConfirmacion = true;
    this.cd.detectChanges(); // Forzar actualización del modal
  }

  /**
   * Cierra el modal de confirmación
   */
  cerrarModalConfirmacion() {
    this.mostrarModalConfirmacion = false;
    this.proveedorAEliminar = null;
    this.eliminando = false;
    this.cd.detectChanges(); // Forzar actualización
  }

  /**
   * Confirma la eliminación del proveedor
   */
  confirmarEliminacion() {
    if (!this.proveedorAEliminar?.id) return;

    this.eliminando = true;
    this.cd.detectChanges(); // Actualizar estado de eliminación

    this.proveedoresService.eliminarProveedor(this.proveedorAEliminar.id).subscribe({
      next: () => {
        this.eliminando = false;
        this.cerrarModalConfirmacion();
        this.cargarProveedores();
        this.cd.detectChanges(); // Actualizar después de eliminar
      },
      error: (err) => {
        console.error('Error al eliminar proveedor:', err);
        this.eliminando = false;
        this.cd.detectChanges(); // Actualizar estado de error
        // El toast de error se maneja en el servicio
      }
    });
  }

  /**
   * Elimina un proveedor (método original actualizado para usar el modal)
   */
  eliminarProveedor(proveedor: Proveedor) {
    this.abrirModalConfirmacion(proveedor);
  }
}