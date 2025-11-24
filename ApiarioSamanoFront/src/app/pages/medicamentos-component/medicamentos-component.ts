import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Almacen, AlmacenService } from '../../services/almaceneService/almacen-service';
import { MedicamentosService, Medicamentos, MedicamentosRequest, MedicamentosResponse, MedicamentosConProveedorResponse, ProveedorResponseDTO } from '../../services/almaceneService/MedicamentosService/medicamentos-service';
import { ProveedoresService, Proveedor } from '../../services/proveedoresService/proveedores-service';
import { ToastService } from '../../services/toastService/toast-service';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-medicamentos-component',
  standalone: false,
  templateUrl: './medicamentos-component.html',
  styleUrl: './medicamentos-component.css'
})
export class MedicamentosComponent implements OnInit {
  medicamentos: MedicamentosConProveedorResponse[] = [];
  almacenes: Almacen[] = [];
  proveedores: Proveedor[] = [];

  mostrarFormulario: boolean = false;
  mostrarModalConfirmacion: boolean = false;
  editando: boolean = false;
  cargando: boolean = false;
  guardando: boolean = false;
  eliminando: boolean = false;

  terminoBusqueda: string = '';
  medicamentoSeleccionado: any = this.nuevoMedicamento();
  medicamentoAEliminar: MedicamentosConProveedorResponse | null = null;

  selectedFileName: string = '';
  selectedFileSize: string = '';
  mostrarErrorCantidad: boolean = false;
  fotoParaVistaPrevia: string = '';

  constructor(
    private medicamentoService: MedicamentosService,
    private almacenService: AlmacenService,
    private proveedorService: ProveedoresService,
    private cd: ChangeDetectorRef,
    private toast: ToastService,
    private audiService : AudioService
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  // ==================== CARGA DE DATOS ====================
  cargarDatos(): void {
    this.cargando = true;
    this.cd.detectChanges();

    this.medicamentoService.obtenerTodosConProveedor().subscribe({
      next: (response: any) => {
        this.medicamentos = response.data;
        console.log('✅ Medicamentos cargados:', this.medicamentos);
        this.cd.detectChanges();
      },
      error: (error: any) => {
        console.error('❌ Error al cargar medicamentos:', error);
        this.toast.error('Error', 'No se pudieron cargar los medicamentos');
        this.cd.detectChanges();
      },
      complete: () => {
        this.cargando = false;
        this.cd.detectChanges();
      }
    });

    this.almacenService.obtenerAlmacenes().subscribe({
      next: (response: any) => {
        this.almacenes = response.data;
        console.log('✅ Almacenes cargados:', this.almacenes);
        this.cd.detectChanges();
      },
      error: (error: any) => {
        console.error('❌ Error al cargar almacenes:', error);
        this.toast.error('Error', 'No se pudieron cargar los almacenes');
        this.cd.detectChanges();
      }
    });

    this.proveedorService.listarProveedores().subscribe({
      next: (data) => {
        this.proveedores = data;
        this.cargando = false;
        console.log('✅ Proveedores cargados:', data);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error al cargar proveedores:', err);
        this.toast.error('Error', 'No se pudieron cargar los proveedores');
        this.cargando = false;
        this.cd.detectChanges();
      }
    });
  }

  // ==================== FILTRADO Y BÚSQUEDA ====================
  filtrarMedicamentos(): MedicamentosConProveedorResponse[] {
    if (!this.terminoBusqueda || this.terminoBusqueda.trim() === '') {
      return this.medicamentos;
    }

    const busqueda = this.terminoBusqueda.toLowerCase().trim();
    return this.medicamentos.filter(medicamento =>
      medicamento.nombre.toLowerCase().includes(busqueda) ||
      medicamento.descripcion.toLowerCase().includes(busqueda) ||
      medicamento.proveedor?.nombreEmpresa?.toLowerCase().includes(busqueda)
    );
  }

  onBuscarChange(): void {
    console.log('🔍 Búsqueda:', this.terminoBusqueda);
    this.cd.detectChanges();
  }

  // ==================== CRUD - CREAR/EDITAR ====================
  abrirFormulario(): void {
    this.medicamentoSeleccionado = this.nuevoMedicamento();
    this.editando = false;
    this.mostrarFormulario = true;
    this.mostrarErrorCantidad = false;
    this.fotoParaVistaPrevia = '';
    this.cd.detectChanges();
  }

  editarMedicamento(medicamento: MedicamentosConProveedorResponse): void {
    this.medicamentoSeleccionado = {
      id: medicamento.id,
      nombre: medicamento.nombre,
      descripcion: medicamento.descripcion,
      cantidad: medicamento.cantidad.toString(),
      idAlmacen: '',
      idProveedor: medicamento.proveedor?.id || '',
      foto: medicamento.foto || ''
    };
    this.editando = true;
    this.mostrarFormulario = true;
    this.mostrarErrorCantidad = false;
    this.fotoParaVistaPrevia = medicamento.foto ? 'data:image/jpeg;base64,' + medicamento.foto : '';
    this.selectedFileName = medicamento.foto ? 'Imagen existente' : '';
    this.cd.detectChanges();
  }

  guardarMedicamento(): void {
    if (!this.validarFormulario()) {
      this.toast.warning('Validación', 'Por favor, completa todos los campos obligatorios');
      return;
    }

    if (this.mostrarErrorCantidad) {
      this.toast.warning('Validación', 'Por favor, corrige el campo de cantidad');
      return;
    }

    this.guardando = true;
    this.cd.detectChanges();

    const medicamentoData: MedicamentosRequest = {
      nombre: this.medicamentoSeleccionado.nombre.trim(),
      descripcion: this.medicamentoSeleccionado.descripcion.trim(),
      cantidad: this.medicamentoSeleccionado.cantidad,
      idAlmacen: Number(this.medicamentoSeleccionado.idAlmacen),
      idProveedor: Number(this.medicamentoSeleccionado.idProveedor),
      foto: this.medicamentoSeleccionado.foto || ''
    };

    if (this.editando && this.medicamentoSeleccionado.id) {
      (medicamentoData as any).id = this.medicamentoSeleccionado.id;
    }

    this.medicamentoService.crear(medicamentoData).subscribe({
      next: (response: any) => {
        if (this.editando) {
          this.toast.success('Éxito', 'Medicamento actualizado correctamente');
        } else {
          this.toast.success('Éxito', 'Medicamento creado correctamente');
        }
        this.cerrarFormulario();
        this.cargarDatos();
        this.cd.detectChanges();
      },
      error: (error: any) => {
        const accion = this.editando ? 'actualizar' : 'crear';
        console.error(`❌ Error al ${accion} medicamento:`, error);
        this.toast.error('Error', `No se pudo ${accion} el medicamento`);
        this.guardando = false;
        this.cd.detectChanges();
      }
    });
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.medicamentoSeleccionado = this.nuevoMedicamento();
    this.editando = false;
    this.guardando = false;
    this.mostrarErrorCantidad = false;
    this.selectedFileName = '';
    this.selectedFileSize = '';
    this.fotoParaVistaPrevia = '';
    this.cd.detectChanges();
  }

  // ==================== CRUD - ELIMINAR ====================
  abrirModalConfirmacion(medicamento: MedicamentosConProveedorResponse): void {
    this.audiService.play('assets/audios/Advertencia.mp3',0.6)
    this.medicamentoAEliminar = medicamento;
    this.mostrarModalConfirmacion = true;
    this.cd.detectChanges();
  }

  confirmarEliminacion(): void {
    if (!this.medicamentoAEliminar) return;

    this.eliminando = true;
    this.cd.detectChanges();

    this.medicamentoService.eliminar(this.medicamentoAEliminar.id).subscribe({
      next: () => {
        this.toast.success('Éxito', 'Medicamento eliminado correctamente');
        this.cerrarModalConfirmacion();
        this.cargarDatos();
        this.cd.detectChanges();
      },
      error: (error: any) => {
        console.error('❌ Error al eliminar medicamento:', error);
        this.toast.error('Error', 'No se pudo eliminar el medicamento');
        this.eliminando = false;
        this.cd.detectChanges();
      }
    });
  }

  cerrarModalConfirmacion(): void {
    this.mostrarModalConfirmacion = false;
    this.medicamentoAEliminar = null;
    this.eliminando = false;
    this.cd.detectChanges();
  }

  // ==================== UTILIDADES ====================
  nuevoMedicamento(): any {
    return {
      id: null,
      nombre: '',
      descripcion: '',
      cantidad: '',
      idAlmacen: '',
      idProveedor: '',
      foto: ''
    };
  }

  validarFormulario(): boolean {
    return !!(
      this.medicamentoSeleccionado.nombre &&
      this.medicamentoSeleccionado.nombre.trim() !== '' &&
      this.medicamentoSeleccionado.descripcion &&
      this.medicamentoSeleccionado.descripcion.trim() !== '' &&
      this.medicamentoSeleccionado.cantidad &&
      this.medicamentoSeleccionado.cantidad.trim() !== '' &&
      this.medicamentoSeleccionado.idAlmacen &&
      this.medicamentoSeleccionado.idProveedor
    );
  }

  trackByMedicamentoId(index: number, medicamento: MedicamentosConProveedorResponse): number {
    return medicamento.id;
  }

  obtenerNombreProveedor(medicamento: MedicamentosConProveedorResponse): string {
    return medicamento.proveedor?.nombreEmpresa || 'N/A';
  }

  obtenerNombreRepresentante(medicamento: MedicamentosConProveedorResponse): string {
    return medicamento.proveedor?.nombreRepresentante || 'N/A';
  }

  obtenerInfoAlmacen(medicamento: MedicamentosConProveedorResponse): string {
    return 'N/A';
  }

  // ==================== MANEJO DE ARCHIVOS ====================
  onFileSelected(event: any): void {
    const files = event.target.files;

    if (files.length > 1) {
      this.toast.warning('Advertencia', 'Solo puedes seleccionar una imagen a la vez');
      this.clearFileInput();
      this.cd.detectChanges();
      return;
    }

    const file = files[0];
    if (file) {
      const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'];
      if (!validTypes.includes(file.type)) {
        this.toast.warning('Formato inválido', 'Selecciona una imagen válida (JPG, PNG o GIF)');
        this.clearFileInput();
        this.cd.detectChanges();
        return;
      }

      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        this.toast.warning('Archivo muy grande', 'La imagen es demasiado grande (máx. 5MB)');
        this.clearFileInput();
        this.cd.detectChanges();
        return;
      }

      this.selectedFileName = file.name;
      this.selectedFileSize = this.formatFileSize(file.size);
      this.cd.detectChanges();

      const reader = new FileReader();
      reader.onload = (e: any) => {
        const base64WithPrefix = e.target.result;
        this.fotoParaVistaPrevia = base64WithPrefix;
        const base64Clean = this.limpiarBase64(base64WithPrefix);
        this.medicamentoSeleccionado.foto = base64Clean;
        this.toast.success('Éxito', 'Imagen cargada correctamente');
        this.cd.detectChanges();
      };
      reader.readAsDataURL(file);
    }
  }

  limpiarBase64(base64WithPrefix: string): string {
    if (base64WithPrefix.includes(',')) {
      return base64WithPrefix.split(',')[1];
    }
    return base64WithPrefix;
  }

  removeImage(): void {
    this.medicamentoSeleccionado.foto = '';
    this.fotoParaVistaPrevia = '';
    this.selectedFileName = '';
    this.selectedFileSize = '';
    this.clearFileInput();
    this.toast.info('Información', 'Imagen removida');
    this.cd.detectChanges();
  }

  clearFileInput(): void {
    const fileInput = document.getElementById('fotoInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  // ==================== VALIDACIÓN DE CANTIDAD ====================
  soloNumeros(event: any): boolean {
    const charCode = event.which ? event.which : event.keyCode;
    if ([8, 9, 13, 27, 46, 110, 190].includes(charCode)) return true;
    if ((charCode >= 48 && charCode <= 57) || charCode === 46) {
      const currentValue = event.target.value;
      if (charCode === 46 && currentValue.includes('.')) {
        event.preventDefault();
        return false;
      }
      return true;
    }
    event.preventDefault();
    return false;
  }

  validarCantidad(): void {
    const valor = this.medicamentoSeleccionado.cantidad;
    if (valor && valor.trim() !== '') {
      const numero = parseFloat(valor);
      this.mostrarErrorCantidad = isNaN(numero) || numero < 0;
    } else {
      this.mostrarErrorCantidad = false;
    }
    this.cd.detectChanges();
  }

  formatearCantidad(): void {
    const valor = this.medicamentoSeleccionado.cantidad;
    if (valor && valor.trim() !== '') {
      const numero = parseFloat(valor);
      if (!isNaN(numero) && numero >= 0) {
        this.medicamentoSeleccionado.cantidad = numero.toFixed(2);
        this.mostrarErrorCantidad = false;
      } else {
        this.mostrarErrorCantidad = true;
      }
      this.cd.detectChanges();
    }
  }
}