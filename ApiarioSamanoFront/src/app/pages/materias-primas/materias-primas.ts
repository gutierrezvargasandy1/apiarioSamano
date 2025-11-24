import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MateriasPrimasService, MateriasPrimasRequest, MateriasPrimasConProveedorDTO } from '../../services/almaceneService/materiasPrimasService/materias-primas-service';
import { ProveedoresService, Proveedor } from '../../services/proveedoresService/proveedores-service';
import { AlmacenService, Almacen } from '../../services/almaceneService/almacen-service';
import { ToastService } from '../../services/toastService/toast-service';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-materias-primas',
  standalone: false,
  templateUrl: './materias-primas.html',
  styleUrl: './materias-primas.css'
})
export class MateriasPrimas implements OnInit {
  // Lista de materias primas
  materias: MateriasPrimasConProveedorDTO[] = [];
  
  // Listas para selects
  proveedores: Proveedor[] = [];
  almacenes: Almacen[] = [];
  
  // Término de búsqueda
  terminoBusqueda: string = '';
  
  // Control del modal
  mostrarFormulario: boolean = false;
  editando: boolean = false;
  
  // Materia prima seleccionada para formulario
  materiaSeleccionada: MateriasPrimasRequest & { id?: number } = {
    nombre: '',
    foto: '',
    cantidad: 0,
    idAlmacen: 0,
    idProvedor: 0
  };
  
  // Archivo de imagen seleccionado (solo para frontend)
  archivoSeleccionado: File | null = null;
  // Base64 de la imagen para preview
  imagenPreview: string | null = null;

  // Estados de carga
  cargando: boolean = false;
  guardando: boolean = false;

  // Para manejar el ID durante edición
  materiaEditandoId: number | null = null;

  // Modal de confirmación
  mostrarModalConfirmacion: boolean = false;
  materiaAEliminar: MateriasPrimasConProveedorDTO | null = null;
  eliminando: boolean = false;

  constructor(
    private materiasService: MateriasPrimasService,
    private proveedoresService: ProveedoresService,
    private almacenService: AlmacenService,
    private toastService: ToastService,
    private cd: ChangeDetectorRef,
    private audiService : AudioService
  ) {}

  ngOnInit() {
    this.cargarMateriasPrimas();
    this.cargarProveedores();
    this.cargarAlmacenes();
  }

  /**
   * Carga todas las materias primas desde el backend
   */
  cargarMateriasPrimas() {
    this.cargando = true;
    this.cd.detectChanges();

    this.materiasService.obtenerTodasConProveedor().subscribe({
      next: (response) => {
        this.materias = response.data || [];
        this.cargando = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar materias primas:', err);
        this.cargando = false;
        this.cd.detectChanges();
      }
    });
  }

  /**
   * Carga todos los proveedores
   */
  cargarProveedores() {
    this.proveedoresService.listarProveedores().subscribe({
      next: (proveedores) => {
        this.proveedores = proveedores;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar proveedores:', err);
      }
    });
  }

  /**
   * Carga todos los almacenes
   */
  cargarAlmacenes() {
    this.almacenService.obtenerAlmacenes().subscribe({
      next: (response) => {
        this.almacenes = response.data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar almacenes:', err);
      }
    });
  }

  /**
   * Filtra materias primas según el término de búsqueda
   */
  filtrarMaterias(): MateriasPrimasConProveedorDTO[] {
    if (!Array.isArray(this.materias)) return [];
    
    if (!this.terminoBusqueda.trim()) {
      return this.materias;
    }

    const termino = this.terminoBusqueda.toLowerCase().trim();
    
    return this.materias.filter(m =>
      m.nombre?.toLowerCase().includes(termino) ||
      m.proveedor?.nombreEmpresa?.toLowerCase().includes(termino) ||
      m.almacen?.ubicacion?.toLowerCase().includes(termino)
    );
  }

  /**
   * Abre el modal para crear o editar una materia prima
   */
  abrirFormulario(materia?: MateriasPrimasConProveedorDTO) {
    this.mostrarFormulario = true;
    this.archivoSeleccionado = null;
    this.imagenPreview = null;
    
    if (materia) {
      // Modo edición
      this.editando = true;
      this.materiaEditandoId = materia.id || null;
      this.materiaSeleccionada = {
        id: materia.id,
        nombre: materia.nombre || '',
        foto: materia.foto || '',
        cantidad: materia.cantidad || 0,
        idAlmacen: materia.almacen?.id || 0,
        idProvedor: materia.proveedor?.id || 0
      };
      
      // Si hay foto existente, crear preview
      if (materia.foto) {
        this.imagenPreview = this.getFotoUrl(materia.foto);
      }
    } else {
      // Modo creación
      this.editando = false;
      this.materiaEditandoId = null;
      this.materiaSeleccionada = {
        nombre: '',
        foto: '',
        cantidad: 0,
        idAlmacen: 0,
        idProvedor: 0
      };
    }
    
    this.cd.detectChanges();
  }

  /**
   * Cierra el modal y resetea el formulario
   */
  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.materiaSeleccionada = {
      nombre: '',
      foto: '',
      cantidad: 0,
      idAlmacen: 0,
      idProvedor: 0
    };
    this.archivoSeleccionado = null;
    this.imagenPreview = null;
    this.editando = false;
    this.materiaEditandoId = null;
    
    this.cd.detectChanges();
  }

  /**
   * Guarda o actualiza una materia prima
   */
 guardarMateria() {
  if (!this.materiaSeleccionada.nombre?.trim()) {
    this.toastService.warning('Validación', 'El nombre de la materia prima es obligatorio');
    return;
  }

  if (!this.materiaSeleccionada.cantidad || this.materiaSeleccionada.cantidad <= 0) {
    this.toastService.warning('Validación', 'La cantidad debe ser mayor a 0');
    return;
  }

  if (!this.materiaSeleccionada.idAlmacen) {
    this.toastService.warning('Validación', 'Debe seleccionar un almacén');
    return;
  }

  if (!this.materiaSeleccionada.idProvedor) {
    this.toastService.warning('Validación', 'Debe seleccionar un proveedor');
    return;
  }

  this.guardando = true;
  this.cd.detectChanges();

  const materiaParaEnviar: any = {
    nombre: this.materiaSeleccionada.nombre.trim(),
    foto: this.materiaSeleccionada.foto,
    cantidad: this.materiaSeleccionada.cantidad,
    idAlmacen: this.materiaSeleccionada.idAlmacen,
    idProvedor: this.materiaSeleccionada.idProvedor
  };

  // ✅ Solo se agrega el ID si está editando
  if (this.editando && this.materiaEditandoId !== null) {
    materiaParaEnviar.id = this.materiaEditandoId;
  }

  console.log("📤 Enviando:", materiaParaEnviar);

  this.materiasService.guardar(materiaParaEnviar).subscribe({
    next: () => {
      this.guardando = false;
      this.cargarMateriasPrimas();
      this.cerrarFormulario();
      this.cd.detectChanges();

      this.toastService.success(
        this.editando ? 'Actualizado' : 'Creado',
        this.editando 
          ? 'Materia prima actualizada correctamente ✅'
          : 'Materia prima registrada correctamente ✅'
      );
    },
    error: (err) => {
      console.error('❌ Error al guardar materia prima:', err);
      this.guardando = false;
      this.cd.detectChanges();
      this.toastService.error('Error', 'Ha ocurrido un error al guardar la materia');
    }
  });
}

  /**
   * Abre el formulario en modo edición
   */
  editarMateria(materia: MateriasPrimasConProveedorDTO) {
    this.abrirFormulario(materia);
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
      this.cd.detectChanges();

      // Convertir a Base64 para el backend
      const reader = new FileReader();
      reader.onload = () => {
        const base64String = reader.result as string;
        
        // Remover el prefijo "data:image/...;base64," para enviar Base64 puro
        const base64Data = base64String.split(',')[1];
        
        // Guardar Base64 puro para enviar al backend
        this.materiaSeleccionada.foto = base64Data;
        
        // Guardar Base64 completo para preview
        this.imagenPreview = base64String;
        
        this.cd.detectChanges();
        this.toastService.success('Éxito', 'Imagen cargada correctamente', 2000);
      };
      
      reader.onerror = () => {
        this.toastService.error('Error', 'Error al leer el archivo de imagen');
        this.cd.detectChanges();
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
    this.materiaSeleccionada.foto = '';
    
    // Resetear el input file
    const fileInput = document.getElementById('foto') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
    
    this.cd.detectChanges();
    this.toastService.info('Información', 'Imagen eliminada');
  }

  /**
   * Maneja cambios en la búsqueda
   */
  onBuscarChange() {
    this.cd.detectChanges();
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
  getPlaceholderIcon(nombreMateria: string | undefined): string {
    const nombre = nombreMateria?.toLowerCase() || '';
    
    if (nombre.includes('cera') || nombre.includes('abeja')) return '🐝';
    if (nombre.includes('vidrio') || nombre.includes('envase')) return '🍶';
    if (nombre.includes('tapa') || nombre.includes('metal')) return '🔩';
    if (nombre.includes('madera')) return '🪵';
    if (nombre.includes('herramienta')) return '🔧';
    if (nombre.includes('pintura')) return '🎨';
    if (nombre.includes('electricidad') || nombre.includes('eléctrico')) return '⚡';
    
    return '📦'; // Icono por defecto
  }

  /**
   * TrackBy function para optimizar el *ngFor
   */
  trackByMateriaId(index: number, materia: MateriasPrimasConProveedorDTO): number {
    return materia.id || index;
  }

  /**
   * Abre el modal de confirmación para eliminar materia prima
   */
  abrirModalConfirmacion(materia: MateriasPrimasConProveedorDTO) {
    this.materiaAEliminar = materia;
    this.mostrarModalConfirmacion = true;
    this.audiService.play('assets/audios/Advertencia.mp3',0.6)

    this.cd.detectChanges();
  }

  /**
   * Cierra el modal de confirmación
   */
  cerrarModalConfirmacion() {
    this.mostrarModalConfirmacion = false;
    this.materiaAEliminar = null;
    this.eliminando = false;
    this.cd.detectChanges();
  }

  /**
   * Confirma la eliminación de la materia prima
   */
  confirmarEliminacion() {
    if (!this.materiaAEliminar?.id) return;

    this.eliminando = true;
    this.cd.detectChanges();

    this.materiasService.eliminarPorId(this.materiaAEliminar.id).subscribe({
      next: () => {
        this.eliminando = false;
        this.cerrarModalConfirmacion();
        this.cargarMateriasPrimas();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error al eliminar materia prima:', err);
        this.eliminando = false;
        this.cd.detectChanges();
      }
    });
  }

  /**
   * Obtiene el nombre del almacén por ID
   */
  getNombreAlmacen(idAlmacen: number): string {
    const almacen = this.almacenes.find(a => a.id === idAlmacen);
    return almacen ? `${almacen.ubicacion} (${almacen.numeroSeguimiento})` : 'No asignado';
  }

  /**
   * Obtiene el nombre del proveedor por ID
   */
  getNombreProveedor(idProveedor: number): string {
    const proveedor = this.proveedores.find(p => p.id === idProveedor);
    return proveedor ? proveedor.nombreEmpresa : 'No asignado';
  }
}