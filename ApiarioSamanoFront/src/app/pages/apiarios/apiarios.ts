import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ApiarioService, DispositivosMap, Dispositivo } from '../../services/apiariosService/apiario-service';
import { MedicamentosService, MedicamentosResponse } from '../../services/almaceneService/MedicamentosService/medicamentos-service';
import { ToastService } from '../../services/toastService/toast-service';
import { IaService } from '../../services/apiariosService/IAService/ia-service';

// Interfaces actualizadas según el servicio
interface RecetaMedicamento {
  id: number;
  receta: Receta;
  idMedicamento: number;
  medicamentoInfo: any;
}

interface MensajeChat {
  texto: string;
  tiempo: string;
  tipo: 'usuario' | 'ia';
  estado?: 'enviando' | 'enviado' | 'error';
}
// En la sección de interfaces
interface HistorialMedicoItem {
  id: number;
  fechaAplicacion: string;
  notas: string;
  medicamentos?: any[];
  receta?: Receta;
}

interface HistorialCompleto {
  apiarioId: number;
  historiales: HistorialMedicoItem[];
}

interface Receta {
  id: number;
  descripcion: string;
  fechaDeCreacion: string;
  medicamentos: RecetaMedicamento[];
}
interface DatosSensores {
  temperatura?: string;
  humedad_ambiente?: string;
  humedad_suelo?: string;
  peso?: string;
}

interface HistorialMedico {
  id: number;
  fechaAplicacion: string;
  notas: string;
}

interface Apiario {
  id: number;
  numeroApiario: number;
  ubicacion: string;
  salud: string;
  dispositivoId: string | null;
  fechaVinculacion: string | null;
  receta: Receta | null;
  historialMedico: HistorialMedico | null;
}

interface ApiarioRequest {
  numeroApiario: number;
  ubicacion: string;
  salud: string;
  dispositivoId?: string;
}

interface RecetaRequest {
  descripcion: string;
  medicamentos: MedicamentoRequest[];
}

interface MedicamentoRequest {
  id: number;
}

// ✅ Nueva interfaz para sugerencias de IA
interface SugerenciaIA {
  titulo: string;
  descripcion: string;
  tipo: 'ia' | 'default';
}

@Component({
  selector: 'app-apiarios',
  standalone: false,
  templateUrl: './apiarios.html',
  styleUrl: './apiarios.css'
})
export class Apiarios implements OnInit {
  // Estado de la aplicación
  apiarios: Apiario[] = [];
  apiarioSeleccionado: Apiario | null = null;

    // 🔍 Propiedades para historial completo
  historialCompleto: HistorialCompleto | null = null;
  cargandoHistorial: boolean = false;
  mostrarHistorialCompleto: boolean = false;

isApiarioOpening: boolean = false;

  
  // Modales
  mostrarModalApiario: boolean = false;
  mostrarModalReceta: boolean = false;
  mostrarModalSugerencias: boolean = false;
  mostrarModalVinculacion: boolean = false;

  terminoBusqueda: string = '';
  
  // Edición
  apiarioEditando: Apiario | null = null;
  
  // Formularios
  formApiario = {
    numeroApiario: 0,
    ubicacion: '',
    salud: '',
    dispositivoId: ''
  };
  
  formReceta = {
    descripcion: '',
    medicamentos: [] as MedicamentoRequest[]
  };
  
  // Lista de medicamentos disponibles
  medicamentosDisponibles: MedicamentosResponse[] = [];
  cargandoMedicamentos: boolean = false;
  
  // Estado de carga
  cargando: boolean = false;

  // Modal IA
  nuevaPregunta: string = '';
  mensajesChat: any[] = [];
  cargandoRecomendaciones: boolean = false;
  recomendacionesIA: any = null;

  // ✅ Sugerencias vacías inicialmente
  sugerenciasAutomaticas: SugerenciaIA[] = [];

  // 🔥 PROPIEDADES PARA DISPOSITIVOS
  dispositivosDisponibles: any[] = [];
  dispositivoSeleccionado: string = '';
  apiarioIdDelDispositivo: string = '';
  dispositivoSeleccionadoObj: any = null;

  // 🔥 NUEVAS PROPIEDADES PARA GESTIÓN ESP32
  estadoMqtt: string = 'Desconectado';
  cargandoDispositivos: boolean = false;
  datosDispositivo: any = null;
  dispositivoVinculacion: any = null;

  // Estados de los componentes ESP32 - ACTUALIZADOS
  estadoVentilador: boolean = false;
  estadoCompuerta: boolean = false; // 🔄 NUEVO: Estado para compuerta
  estadoLuz: boolean = false;
  servo1Grados: number = 90;
  servo2Grados: number = 110;
  datosSensores: DatosSensores = {};

  // Carga de chat
  cargandoChat: boolean = false;

  constructor(
    private apiarioService: ApiarioService,
    private medicamentosService: MedicamentosService,
    private toastService: ToastService,
    private cdRef: ChangeDetectorRef,
    private iaService: IaService,
  ) {}

  ngOnInit(): void {
    this.cargarApiarios();
    this.cargarMedicamentosDisponibles();
    this.obtenerPrediccionesSalud();
    this.verificarSaludOllama();
    this.inicializarGestionESP32();
  }

  // ==================== CARGA DE MEDICAMENTOS ====================
  
cargarDatosSensores(idApiario: string): void {
  this.apiarioService.obtenerDatosSensores(idApiario).subscribe({
    next: (datos) => {
      this.datosSensores = datos;
      console.log('📊 Datos de sensores:', datos);
      this.cdRef.detectChanges();
    },
    error: (error) => {
      console.error('Error al cargar sensores:', error);
      this.datosSensores = {};
    }
  });
}

obtenerHistorialCompleto(): void {
  if (!this.apiarioSeleccionado) {
    this.toastService.warning('Selección requerida', 'Selecciona un apiario primero');
    return;
  }

  this.cargandoHistorial = true;
  this.historialCompleto = null;
  this.mostrarHistorialCompleto = true;

  this.apiarioService.obtenerHistorialCompleto(this.apiarioSeleccionado.id).subscribe({
    next: (response: any) => {
      if (response.codigo === 200 && response.data) {
        this.historialCompleto = response.data;
        console.log('📋 Historial completo cargado:', this.historialCompleto);
      } else {
        this.toastService.error('Error', response.descripcion || 'Error al cargar el historial');
        this.historialCompleto = {
          apiarioId: this.apiarioSeleccionado!.id,
          historiales: []
        };
      }
      this.cargandoHistorial = false;
      this.cdRef.detectChanges();
    },
    error: (err: any) => {
      console.error('❌ Error al obtener historial:', err);
      this.toastService.error('Error', 'No se pudo cargar el historial médico');
      this.cargandoHistorial = false;
      this.cdRef.detectChanges();
    }
  });
}

controlarApiario(abrir: boolean) {
    if (this.isApiarioOpening) {
        console.log('El apiario ya está en proceso de apertura/cierre');
        return;
    }

    if (abrir) {
        // Abrir apiario (mover a 140 grados)
        this.isApiarioOpening = true;
        this.servo2Grados = 140;
        
        // Llamar al método para controlar el servo
        this.controlarServo2(this.servo2Grados);
        
        // Después de 5 segundos, regresar a 110 grados
        setTimeout(() => {
            this.servo2Grados = 110;
            this.controlarServo2(this.servo2Grados);
            this.isApiarioOpening = false;
        }, 5000);
    } else {
        // Cerrar apiario inmediatamente
        this.servo2Grados = 110;
        this.controlarServo2(this.servo2Grados);
    }
}

/**
 * Cerrar modal de historial completo
 */
cerrarHistorialCompleto(): void {
  this.mostrarHistorialCompleto = false;
  this.historialCompleto = null;
  this.cdRef.detectChanges();
}

/**
 * Formatear fecha para mostrar en historial
 */
formatearFechaHistorial(fecha: string): string {
  if (!fecha) return 'Fecha no disponible';
  
  try {
    const fechaObj = new Date(fecha);
    return fechaObj.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return fecha;
  }
}

/**
 * Contar medicamentos en un historial
 */
contarMedicamentosHistorial(historial: HistorialMedicoItem): number {
  return historial.medicamentos?.length || historial.receta?.medicamentos?.length || 0;
}

/**
 * Obtener descripción de medicamentos del historial
 */
obtenerMedicamentosHistorial(historial: HistorialMedicoItem): string {
  if (historial.medicamentos && historial.medicamentos.length > 0) {
    return historial.medicamentos.map((m: any) => m.nombre).join(', ');
  } else if (historial.receta?.medicamentos) {
    return historial.receta.medicamentos.map((rm: RecetaMedicamento) => 
      rm.medicamentoInfo?.nombre || 'Medicamento desconocido'
    ).join(', ');
  }
  return 'Sin medicamentos registrados';
}
  cargarMedicamentosDisponibles(): void {
    this.cargandoMedicamentos = true;
    this.cdRef.detectChanges();
    
    this.medicamentosService.obtenerTodos().subscribe({
      next: (response: any) => {
        if (response.codigo === 200 && response.data) {
          this.medicamentosDisponibles = response.data;
          console.log('💊 Medicamentos cargados:', this.medicamentosDisponibles);
        } else {
          this.toastService.error('Error', response.descripcion || 'Error al cargar medicamentos');
        }
        this.cargandoMedicamentos = false;
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al cargar medicamentos:', err);
        this.toastService.error('Error', 'No se pudieron cargar los medicamentos');
        this.cargandoMedicamentos = false;
        this.cdRef.detectChanges();
      }
    });
  }

  // ==================== GESTIÓN ESP32 - ACTUALIZADA ====================

  /**
   * Obtener el estado de conexión MQTT
   */
  actualizarEstadoMqtt(): void {
    this.cargandoDispositivos = true;
    this.apiarioService.obtenerEstadoMqtt().subscribe({
      next: (estado: string) => {
        this.estadoMqtt = estado;
        this.cargandoDispositivos = false;
        this.cdRef.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener estado MQTT:', error);
        this.estadoMqtt = 'Error de conexión';
        this.cargandoDispositivos = false;
        this.cdRef.detectChanges();
      }
    });
  }

  /**
   * Actualizar lista de dispositivos detectados
   */
  actualizarDispositivos(): void {
    this.cargandoDispositivos = true;
    this.apiarioService.obtenerDispositivosDetectados().subscribe({
      next: (dispositivos: DispositivosMap) => {
        // Convertir el mapa a array
        this.dispositivosDisponibles = Object.values(dispositivos);
        this.cargandoDispositivos = false;
        console.log('Dispositivos detectados:', this.dispositivosDisponibles);
        this.cdRef.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener dispositivos:', error);
        this.dispositivosDisponibles = [];
        this.cargandoDispositivos = false;
        this.cdRef.detectChanges();
      }
    });
  }

  /**
   * Controlar ventilador (Motor A)
   */
  controlarVentilador(encender: boolean): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      return;
    }

    this.apiarioService.controlarVentilador(
      this.apiarioSeleccionado.dispositivoId, 
      encender
    ).subscribe({
      next: (respuesta: string) => {
        this.estadoVentilador = encender;
        console.log('Ventilador:', respuesta);
      },
      error: (error) => {
        console.error('Error al controlar ventilador:', error);
        this.toastService.error('Error', 'Error al controlar el ventilador');
      }
    });
  }

  /**
   * Controlar compuerta (Motor B) - 🔄 NUEVO MÉTODO
   */
  controlarCompuerta(abrir: boolean): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      return;
    }

    this.apiarioService.controlarCompuerta(
      this.apiarioSeleccionado.dispositivoId, 
      abrir
    ).subscribe({
      next: (respuesta: string) => {
        this.estadoCompuerta = abrir;
        console.log('Compuerta:', respuesta);
      },
      error: (error) => {
        console.error('Error al controlar compuerta:', error);
        this.toastService.error('Error', 'Error al controlar la compuerta');
      }
    });
  }

  /**
   * Controlar luz
   */
  controlarLuz(encender: boolean): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      return;
    }

    this.apiarioService.controlarLuz(
      this.apiarioSeleccionado.dispositivoId, 
      encender
    ).subscribe({
      next: (respuesta: string) => {
        this.estadoLuz = encender;
        console.log('Luz:', respuesta);
      },
      error: (error) => {
        console.error('Error al controlar luz:', error);
        this.toastService.error('Error', 'Error al controlar la luz');
      }
    });
  }

  /**
   * Controlar servo 1
   */
  controlarServo1(grados: number): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      return;
    }

    this.apiarioService.controlarServo1(
      this.apiarioSeleccionado.dispositivoId, 
      grados
    ).subscribe({
      next: (respuesta: string) => {
        console.log('Servo 1:', respuesta);
      },
      error: (error) => {
        console.error('Error al controlar servo 1:', error);
        this.toastService.error('Error', 'Error al controlar servo 1');
      }
    });
  }

  /**
   * Controlar servo 2
   */
  controlarServo2(grados: number): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      this.toastService.warning('Dispositivo requerido', 'No hay dispositivo vinculado');
      return;
    }

    this.apiarioService.controlarServo2(
      this.apiarioSeleccionado.dispositivoId, 
      grados
    ).subscribe({
      next: (respuesta: string) => {
        console.log('Servo 2:', respuesta);
      },
      error: (error) => {
        console.error('Error al controlar servo 2:', error);
        this.toastService.error('Error', 'Error al controlar servo 2');
      }
    });
  }

  /**
   * 🔄 NUEVO: Controlar todos los actuadores a la vez
   */
  controlarTodosActuadores(config: {
    ventilador?: boolean;
    compuerta?: boolean;
    luz?: boolean;
    servo1?: number;
    servo2?: number;
  }): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      this.toastService.warning('Dispositivo requerido', 'No hay dispositivo vinculado');
      return;
    }

    this.apiarioService.controlarTodosActuadores(
      this.apiarioSeleccionado.dispositivoId,
      config
    ).subscribe({
      next: (respuestas: string[]) => {
        console.log('Todos los actuadores controlados:', respuestas);
        
        // Actualizar estados locales
        if (config.ventilador !== undefined) this.estadoVentilador = config.ventilador;
        if (config.compuerta !== undefined) this.estadoCompuerta = config.compuerta;
        if (config.luz !== undefined) this.estadoLuz = config.luz;
      },
      error: (error) => {
        console.error('Error al controlar actuadores:', error);
        this.toastService.error('Error', 'Error al controlar los actuadores');
      }
    });
  }

  /**
   * 🔄 NUEVO: Apagar todos los actuadores
   */
  apagarTodosActuadores(): void {
    this.controlarTodosActuadores({
      ventilador: false,
      compuerta: false,
      luz: false,
      servo1: 0,
      servo2: 0
    });
  }

  /**
   * Abrir modal de vinculación
   */
  abrirModalVinculacion(): void {
    this.mostrarModalVinculacion = true;
    this.actualizarEstadoMqtt();
    this.actualizarDispositivos();
  }

  /**
   * Cerrar modal de vinculación
   */
  cerrarModalVinculacion(): void {
    this.mostrarModalVinculacion = false;
    this.dispositivoVinculacion = null;
    this.cdRef.detectChanges();
  }

  /**
   * Seleccionar dispositivo para vincular
   */
  seleccionarDispositivoParaVincular(dispositivo: any): void {
    this.dispositivoVinculacion = dispositivo;
    this.cdRef.detectChanges();
  }

  /**
   * Cancelar proceso de vinculación
   */
  cancelarVinculacion(): void {
    this.dispositivoVinculacion = null;
    this.cdRef.detectChanges();
  }

  /**
   * Confirmar vinculación del dispositivo
   */
  confirmarVinculacion(): void {
    if (!this.dispositivoVinculacion || !this.apiarioSeleccionado) {
      this.toastService.warning('Selección requerida', 'Debe seleccionar un dispositivo');
      return;
    }

    this.cargando = true;
    this.apiarioService.vincularDispositivo(
      this.apiarioSeleccionado.id,
      this.dispositivoVinculacion.dispositivoId
    ).subscribe({
      next: (response) => {
        this.cargando = false;
        this.toastService.success('Vinculación', 'Dispositivo vinculado correctamente');
        
        // Actualizar el apiario seleccionado
        this.apiarioSeleccionado!.dispositivoId = this.dispositivoVinculacion.dispositivoId;
        this.apiarioSeleccionado!.fechaVinculacion = new Date().toISOString();
        
        // Cerrar modal y resetear
        this.cerrarModalVinculacion();
        this.actualizarApiarios();
        this.inicializarGestionESP32();
      },
      error: (error) => {
        this.cargando = false;
        console.error('Error al vincular dispositivo:', error);
        this.toastService.error('Error', 'Error al vincular el dispositivo');
      }
    });
  }

  /**
   * Desvincular dispositivo
   */
  desvincularDispositivo(): void {
    if (!this.apiarioSeleccionado?.dispositivoId) {
      return;
    }

    if (!confirm('¿Estás seguro de que quieres desvincular el dispositivo?')) {
      return;
    }

    this.cargando = true;
    this.apiarioService.desvincularDispositivo(this.apiarioSeleccionado.id).subscribe({
      next: (response) => {
        this.cargando = false;
        this.toastService.success('Desvinculación', 'Dispositivo desvinculado correctamente');
        
        // Actualizar el apiario seleccionado
        this.apiarioSeleccionado!.dispositivoId = null;
        this.apiarioSeleccionado!.fechaVinculacion = null;
        
        // Resetear estados de control
        this.resetearControles();
        this.actualizarApiarios();
      },
      error: (error) => {
        this.cargando = false;
        console.error('Error al desvincular dispositivo:', error);
        this.toastService.error('Error', 'Error al desvincular el dispositivo');
      }
    });
  }

  /**
   * Obtener lista de sensores del dispositivo
   */
obtenerSensores(): any[] {
  if (!this.datosSensores || Object.keys(this.datosSensores).length === 0) {
    return [];
  }

  return [
    { nombre: '🌡️ Temperatura', valor: this.datosSensores.temperatura || '--', unidad: '°C' },
    { nombre: '💧 Humedad Ambiente', valor: this.datosSensores.humedad_ambiente || '--', unidad: '%' },
    { nombre: '🌱 Humedad Suelo', valor: this.datosSensores.humedad_suelo || '--', unidad: '%' },
    { nombre: '⚖️ Peso', valor: this.datosSensores.peso || '--', unidad: 'kg' }
  ];
}

  /**
   * Formatear nombre del sensor para mostrar
   */
  formatearNombreSensor(nombre: string): string {
    const nombres: { [key: string]: string } = {
      'temperature': 'Temperatura',
      'humidity': 'Humedad',
      'pressure': 'Presión',
      'light': 'Luz',
      'movement': 'Movimiento',
      'humedad_suelo': 'Humedad Suelo' // 🔄 NUEVO: Para el sensor de humedad del suelo
    };
    
    return nombres[nombre] || nombre;
  }

  /**
   * Obtener unidad del sensor
   */
  obtenerUnidadSensor(nombre: string): string {
    const unidades: { [key: string]: string } = {
      'temperature': '°C',
      'humidity': '%',
      'pressure': 'hPa',
      'light': 'lux',
      'movement': '',
      'humedad_suelo': '%' // 🔄 NUEVO: Para el sensor de humedad del suelo
    };
    
    return unidades[nombre] || '';
  }

  /**
   * Resetear todos los controles al estado inicial
   */
  resetearControles(): void {
    this.estadoVentilador = false;
    this.estadoCompuerta = false; // 🔄 NUEVO: Resetear compuerta
    this.estadoLuz = false;
    this.servo1Grados = 90;
    this.servo2Grados = 90;
  }

  /**
   * Inicializar gestión ESP32 cuando se selecciona un apiario
   */
  inicializarGestionESP32(): void {
    if (this.apiarioSeleccionado?.dispositivoId) {
      // Cargar datos del dispositivo si está vinculado
      this.cargarDatosDispositivo(this.apiarioSeleccionado.dispositivoId);
      
      // Obtener estado actual de los componentes
      this.obtenerEstadoDispositivo();
      setInterval(() => {
      if (this.apiarioSeleccionado?.dispositivoId) {
        this.cargarDatosSensores(this.apiarioSeleccionado.dispositivoId);
      }
    }, 1000);
    } else {
      this.resetearControles();
      this.datosDispositivo = null;
    }
  }

  /**
   * Cargar datos del dispositivo
   */
  cargarDatosDispositivo(dispositivoId: string): void {
    this.apiarioService.obtenerDispositivo(dispositivoId).subscribe({
      next: (dispositivo) => {
        this.datosDispositivo = dispositivo;
        this.cdRef.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar datos del dispositivo:', error);
        this.datosDispositivo = null;
      }
    });
  }

  /**
   * Obtener estado actual del dispositivo
   */
  obtenerEstadoDispositivo(): void {
    // Aquí puedes implementar la lógica para obtener el estado actual
    // de los componentes del ESP32 si tu backend lo soporta
    console.log('Obteniendo estado actual del dispositivo...');
  }

getEstadoCompuerta(grados: number): string {
    if (grados <= 100) {
        return 'Abierto';  // 75°-100° = Abierto
    } else if (grados > 100 && grados <= 140) {
        return 'Parcial';  // 101°-140° = Parcial
    } else {
        return 'Cerrado';  // 141°-180° = Cerrado
    }
}

  // ==================== CARGA DE DISPOSITIVOS ====================

  // Método para cargar dispositivos - MOSTRAR ASIGNADOS
 cargarDispositivosDisponibles(): void {
    this.apiarioService.obtenerDispositivosDetectados().subscribe({
        next: (dispositivosMap) => {
            console.log('📡 Dispositivos detectados (RAW):', dispositivosMap);
            
            // Convertir el mapa a array
            const todosDispositivos = Object.values(dispositivosMap);
            console.log('📡 Todos los dispositivos:', todosDispositivos);
            
            // ✅ CORRECCIÓN: Mostrar dispositivos SIN apiarioId (DISPONIBLES para asignar)
            this.dispositivosDisponibles = todosDispositivos.filter(
                (dispositivo: any) => !dispositivo.apiarioId || dispositivo.apiarioId === ''
            );
            
            console.log('📡 Dispositivos DISPONIBLES (sin asignar):', this.dispositivosDisponibles);
            this.cdRef.detectChanges();
        },
        error: (error) => {
            console.error('❌ Error al cargar dispositivos:', error);
            this.dispositivosDisponibles = [];
            this.cdRef.detectChanges();
        }
    });
}

  // 🔥 NUEVO: Método que se ejecuta cuando seleccionas un dispositivo
  onDispositivoSeleccionado(): void {
    if (this.dispositivoSeleccionado) {
        // Buscar el dispositivo seleccionado en la lista
        this.dispositivoSeleccionadoObj = this.dispositivosDisponibles.find(
            d => d.dispositivoId === this.dispositivoSeleccionado
        );
        
        if (this.dispositivoSeleccionadoObj) {
            // ✅ CORRECCIÓN: Usar dispositivoId, NO apiarioId
            this.formApiario.dispositivoId = this.dispositivoSeleccionadoObj.dispositivoId;
            
            console.log('🔗 Dispositivo seleccionado:', {
                dispositivoId: this.dispositivoSeleccionadoObj.dispositivoId, // Este es el que queremos enviar
                apiarioId: this.dispositivoSeleccionadoObj.apiarioId, // Este es solo informativo
                tipo: this.dispositivoSeleccionadoObj.tipo
            });
            
            console.log('🎯 Dispositivo ID que se enviará:', this.formApiario.dispositivoId);
            
            this.mostrarInfoDispositivo(this.dispositivoSeleccionadoObj);
        }
    } else {
        this.dispositivoSeleccionadoObj = null;
        this.formApiario.dispositivoId = ''; // Limpiar cuando no hay selección
    }
    this.cdRef.detectChanges();
}

  // Método para mostrar información del dispositivo seleccionado
  mostrarInfoDispositivo(dispositivo: any): void {
    console.log('📋 Información del dispositivo:');
    console.log('   ID:', dispositivo.dispositivoId);
    console.log('   Apiario ID:', dispositivo.apiarioId);
    console.log('   Tipo:', dispositivo.tipo);
    console.log('   Sensores:', dispositivo.sensores?.join(', '));
    console.log('   Actuadores:', dispositivo.actuadores?.join(', '));
    console.log('   Timestamp:', dispositivo.timestamp);
  }

  // Propiedades computadas para dispositivos ASIGNADOS
  get dispositivosAsignadosCount(): number {
    if (!this.dispositivosDisponibles || this.dispositivosDisponibles.length === 0) {
      return 0;
    }
    // Contar dispositivos CON apiarioId
    return this.dispositivosDisponibles.filter(d => 
      d.apiarioId && d.apiarioId !== ''
    ).length;
  }

  get dispositivosTotalCount(): number {
    return this.dispositivosDisponibles ? this.dispositivosDisponibles.length : 0;
  }

  // Lista de dispositivos asignados
  get dispositivosAsignadosList(): any[] {
    return this.dispositivosDisponibles.filter(d => 
      d.apiarioId && d.apiarioId !== ''
    );
  }

  // ==================== CARGA RÁPIDA DE DATOS ====================

  cargarApiarios(): void {
    this.cargando = true;
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez al iniciar carga
    this.cdRef.detectChanges();
    
    this.apiarioService.obtenerTodos().subscribe({
      next: (response: any) => {
        if (response.codigo === 200 && response.data) {
          this.apiarios = response.data;
          console.log('✅ Apiarios cargados:', this.apiarios);
        } else {
          this.toastService.error('Error', response.descripcion || 'Error al cargar apiarios');
        }
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de procesar la respuesta
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al cargar apiarios:', err);
        this.toastService.error('Error', 'No se pudieron cargar los apiarios');
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después del error
        this.cdRef.detectChanges();
      }
    });
  }

  // ==================== RECARGA RÁPIDA ====================

  recargarApiarios(): void {
    this.cargarApiarios();
  }

  recargarMedicamentos(): void {
    this.cargarMedicamentosDisponibles();
  }

  recargarTodo(): void {
    this.cargarApiarios();
    this.cargarMedicamentosDisponibles();
  }

  // ==================== SELECCIÓN ====================

  seleccionarApiario(apiario: Apiario): void {
    this.apiarioSeleccionado = apiario;
    this.inicializarGestionESP32();
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de cambiar la selección
    this.cdRef.detectChanges();
  }

  // ==================== MODAL APIARIO ====================

  abrirModalApiario(apiario?: Apiario): void {
    if (apiario) {
      this.apiarioEditando = apiario;
      this.formApiario = {
        numeroApiario: apiario.numeroApiario,
        ubicacion: apiario.ubicacion,
        salud: apiario.salud,
        dispositivoId: apiario.dispositivoId || ''
      };
      this.dispositivoSeleccionado = apiario.dispositivoId || '';
    } else {
      this.apiarioEditando = null;
      this.formApiario = {
        numeroApiario: 0,
        ubicacion: '',
        salud: '',
        dispositivoId: ''
      };
      this.dispositivoSeleccionado = '';
      this.apiarioIdDelDispositivo = '';
      this.dispositivoSeleccionadoObj = null;
    }
    
    // 🔗 Cargar dispositivos disponibles al abrir el modal
    this.cargarDispositivosDisponibles();
    
    this.mostrarModalApiario = true;
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de abrir el modal
    this.cdRef.detectChanges();
  }

 cerrarModalApiario(): void {
    this.mostrarModalApiario = false;
    this.apiarioEditando = null;
    this.formApiario = {
        numeroApiario: 0,
        ubicacion: '',
        salud: '',
        dispositivoId: ''
    };
    this.dispositivoSeleccionado = '';
    this.dispositivoSeleccionadoObj = null;
    this.dispositivosDisponibles = [];
    
    this.cdRef.detectChanges();
}


  guardarApiario(): void {
    if (!this.formApiario.ubicacion || !this.formApiario.salud || !this.formApiario.numeroApiario) {
        this.toastService.warning('Atención', 'Por favor complete todos los campos');
        return;
    }

    // Validar número de apiario único
    const numeroExistente = this.apiarios.find(a => 
        a.numeroApiario === this.formApiario.numeroApiario && 
        a.id !== this.apiarioEditando?.id
    );
    
    if (numeroExistente) {
        this.toastService.warning('Atención', `El número de apiario ${this.formApiario.numeroApiario} ya existe`);
        return;
    }

    // ✅ CORRECCIÓN: Enviar dispositivoId directamente
    const request: ApiarioRequest = {
        numeroApiario: this.formApiario.numeroApiario,
        ubicacion: this.formApiario.ubicacion,
        salud: this.formApiario.salud,
        dispositivoId: this.formApiario.dispositivoId || undefined // Enviar el dispositivoId del formulario
    };

    // Debug simplificado
    console.log('🚀 DATOS ENVIADOS AL BACKEND:');
    console.log('   - numeroApiario:', request.numeroApiario);
    console.log('   - ubicacion:', request.ubicacion);
    console.log('   - salud:', request.salud);
    console.log('   - dispositivoId:', request.dispositivoId);

    this.cargando = true;
    this.cdRef.detectChanges();

    if (this.apiarioEditando) {
        // Actualizar apiario existente
        this.apiarioService.modificarApiario(this.apiarioEditando.id, request).subscribe({
            next: (response: any) => {
                this.cargando = false;
                if (response.codigo === 200) {
                    this.toastService.success('Éxito', 'Apiario actualizado correctamente');
                    this.cargarApiarios();
                    this.cerrarModalApiario();
                } else {
                    this.toastService.error('Error', response.descripcion || 'Error al actualizar apiario');
                }
                this.cdRef.detectChanges();
            },
            error: (err: any) => {
                this.cargando = false;
                console.error('❌ Error al actualizar apiario:', err);
                this.toastService.error('Error', 'No se pudo actualizar el apiario');
                this.cdRef.detectChanges();
            }
        });
    } else {
        // Crear nuevo apiario
        this.apiarioService.crearApiario(request).subscribe({
            next: (response: any) => {
                this.cargando = false;
                if (response.codigo === 200) {
                    this.toastService.success('Éxito', 'Apiario creado correctamente');
                    this.cargarApiarios();
                    this.cerrarModalReceta();
                } else {
                    this.toastService.error('Error', response.descripcion || 'Error al crear apiario');
                }
                this.cdRef.detectChanges();
            },
            error: (err: any) => {
                this.cargando = false;
                console.error('❌ Error al crear apiario:', err);
                this.toastService.error('Error', 'No se pudo crear el apiario');
                this.cdRef.detectChanges();
            }
        });
    }
}

  editarApiario(apiario: Apiario, event: Event): void {
    event.stopPropagation();
    this.abrirModalApiario(apiario);
  }

  eliminarApiario(apiario: Apiario, event: Event): void {
    event.stopPropagation();
    
    if (!confirm(`¿Estás seguro de eliminar el apiario #${apiario.numeroApiario}?`)) {
      return;
    }

    this.cargando = true;
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez al iniciar el proceso de eliminación
    this.cdRef.detectChanges();
    
    this.apiarioService.eliminarApiario(apiario.id).subscribe({
      next: (response: any) => {
        if (response.codigo === 200) {
          this.apiarios = this.apiarios.filter(a => a.id !== apiario.id);
          
          if (this.apiarioSeleccionado?.id === apiario.id) {
            this.apiarioSeleccionado = null;
          }
          
          this.toastService.success('Éxito', 'Apiario eliminado correctamente');
        } else {
          this.toastService.error('Error', response.descripcion || 'Error al eliminar apiario');
        }
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de procesar la respuesta
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al eliminar apiario:', err);
        this.toastService.error('Error', 'No se pudo eliminar el apiario');
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después del error
        this.cdRef.detectChanges();
      }
    });
  }

  // ==================== MODAL RECETA ====================

  abrirModalReceta(apiario: Apiario, event: Event): void {
    event.stopPropagation();
    this.apiarioSeleccionado = apiario;
    this.formReceta = {
      descripcion: '',
      medicamentos: []
    };
    this.mostrarModalReceta = true;
    
    // Recargar medicamentos disponibles cada vez que se abre el modal
    this.cargarMedicamentosDisponibles();
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de abrir el modal y cambiar estado
    this.cdRef.detectChanges();
  }

  cerrarModalReceta(): void {
    this.mostrarModalReceta = false;
    this.formReceta = {
      descripcion: '',
      medicamentos: []
    };
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de cerrar el modal y limpiar formulario
    this.cdRef.detectChanges();
  }

  // Método para agregar medicamento al formulario
  agregarMedicamento(): void {
    this.formReceta.medicamentos.push({ id: 0 });
    this.toastService.info('Medicamento', 'Nuevo medicamento agregado al formulario');
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de modificar el array de medicamentos
    this.cdRef.detectChanges();
  }

  // Método para remover medicamento del formulario
  removerMedicamento(index: number): void {
    const medicamentoId = this.formReceta.medicamentos[index].id;
    if (medicamentoId > 0) {
      const nombre = this.obtenerNombreMedicamento(medicamentoId);
      this.toastService.warning('Removido', `Medicamento "${nombre}" removido`);
    } else {
      this.toastService.info('Removido', 'Medicamento removido del formulario');
    }
    this.formReceta.medicamentos.splice(index, 1);
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de modificar el array de medicamentos
    this.cdRef.detectChanges();
  }

  // Método para obtener nombre del medicamento por ID
  obtenerNombreMedicamento(id: number): string {
    const medicamento = this.medicamentosDisponibles.find(m => m.id === id);
    return medicamento ? medicamento.nombre : 'Medicamento no encontrado';
  }

  guardarReceta(): void {
    if (!this.formReceta.descripcion || this.formReceta.medicamentos.length === 0) {
      this.toastService.warning('Atención', 'Por favor complete la descripción y agregue al menos un medicamento');
      return;
    }

    // Validar que todos los medicamentos tengan ID
    const medicamentosInvalidos = this.formReceta.medicamentos.some(med => !med.id || med.id === 0);
    if (medicamentosInvalidos) {
      this.toastService.warning('Atención', 'Todos los medicamentos deben tener un ID válido');
      return;
    }

    // Validar medicamentos duplicados
    if (this.tieneMedicamentosDuplicados()) {
      this.toastService.warning('Atención', 'No puede haber medicamentos duplicados en la receta');
      return;
    }

    // Validar stock disponible
    const medicamentosSinStock = this.formReceta.medicamentos.filter(med => {
      const stock = this.obtenerStockMedicamento(med.id);
      return stock !== undefined && stock <= 0;
    });

    if (medicamentosSinStock.length > 0) {
      this.toastService.warning('Stock', 'Algunos medicamentos no tienen stock disponible');
      return;
    }

    if (!this.apiarioSeleccionado) {
      this.toastService.error('Error', 'Debe seleccionar un apiario');
      return;
    }

    const request: RecetaRequest = {
      descripcion: this.formReceta.descripcion,
      medicamentos: this.formReceta.medicamentos
    };

    this.cargando = true;
    this.toastService.info('Procesando', 'Guardando receta médica...');
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez al iniciar el proceso de guardado
    this.cdRef.detectChanges();

    this.apiarioService.agregarReceta(this.apiarioSeleccionado.id, request).subscribe({
      next: (response: any) => {
        if (response.codigo === 200) {
          this.toastService.success('Éxito', 'Receta agregada correctamente');
          this.cargarApiarios();
          this.cerrarModalReceta();
          
          // Actualizar apiario seleccionado
          setTimeout(() => {
            const apiarioActualizado = this.apiarios.find(a => a.id === this.apiarioSeleccionado?.id);
            if (apiarioActualizado) {
              this.apiarioSeleccionado = apiarioActualizado;
              // ✅ DETECCIÓN ESTRATÉGICA: Solo si se actualiza el apiario seleccionado
              this.cdRef.detectChanges();
            }
          }, 100);
        } else {
          this.toastService.error('Error', response.descripcion || 'Error al agregar receta');
        }
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de procesar la respuesta
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al agregar receta:', err);
        this.toastService.error('Error', 'No se pudo agregar la receta');
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después del error
        this.cdRef.detectChanges();
      }
    });
  }

  eliminarReceta(apiario: Apiario): void {
    if (!confirm('¿Marcar esta receta como cumplida?')) {
      this.toastService.info('Cancelado', 'Operación cancelada');
      return;
    }

    this.cargando = true;
    this.toastService.info('Procesando', 'Marcando receta como cumplida...');
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez al iniciar el proceso de eliminación
    this.cdRef.detectChanges();

    this.apiarioService.eliminarRecetaCumplida(apiario.id).subscribe({
      next: (response: any) => {
        if (response.codigo === 200) {
          this.toastService.success('Éxito', 'Receta marcada como cumplida y agregada al historial');
          this.cargarApiarios();
          
          // Actualizar apiario seleccionado
          setTimeout(() => {
            const apiarioActualizado = this.apiarios.find(a => a.id === apiario.id);
            if (apiarioActualizado) {
              this.apiarioSeleccionado = apiarioActualizado;
              // ✅ DETECCIÓN ESTRATÉGICA: Solo si se actualiza el apiario seleccionado
              this.cdRef.detectChanges();
            }
          }, 100);
        } else {
          this.toastService.error('Error', response.descripcion || 'Error al eliminar receta');
        }
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de procesar la respuesta
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al eliminar receta:', err);
        this.toastService.error('Error', 'No se pudo eliminar la receta');
        this.cargando = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después del error
        this.cdRef.detectChanges();
      }
    });
  }

  // ==================== UTILIDADES ====================

  obtenerClaseBadge(salud: string): string {
    switch (salud.toLowerCase()) {
      case 'buena':
        return 'badge-buena';
      case 'regular':
        return 'badge-regular';
      case 'crítica':
      case 'critica':
        return 'badge-critica';
      default:
        return 'badge-regular';
    }
  }

  tieneRecetaActiva(apiario: Apiario): boolean {
    return !!apiario.receta;
  }

  contarMedicamentos(apiario: Apiario): number {
    return apiario.receta?.medicamentos?.length || 0;
  }

  obtenerDescripcionReceta(apiario: Apiario): string {
    return apiario.receta?.descripcion || 'Sin receta activa';
  }

  obtenerFechaReceta(apiario: Apiario): string {
    return apiario.receta?.fechaDeCreacion || '';
  }

  // Método para formatear fecha si es necesario
  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    return new Date(fecha).toLocaleDateString('es-ES');
  }

  // ==================== MÉTODOS DE IA ====================

  abrirModalSugerencias(): void {
    if (!this.apiarioSeleccionado) {
      this.toastService.warning('Selección requerida', 'Por favor selecciona un apiario primero');
      return;
    }

    this.mostrarModalSugerencias = true;
    this.mensajesChat = []; 
    this.sugerenciasAutomaticas = []; 
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de inicializar el estado
    this.cdRef.detectChanges();

    this.obtenerRecomendacionesIA();
  }

  cerrarModalSugerencias(): void {
    this.mostrarModalSugerencias = false;
    this.nuevaPregunta = '';
    this.mensajesChat = [];
    this.sugerenciasAutomaticas = []; 
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de limpiar todo
    this.cdRef.detectChanges();
  }

  obtenerRecomendacionesIA(): void {
    if (!this.apiarioSeleccionado) {
      this.toastService.warning('Selección requerida', 'Por favor selecciona un apiario primero');
      return;
    }

    this.cargandoRecomendaciones = true;
    this.sugerenciasAutomaticas = []; 
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez al iniciar carga
    this.cdRef.detectChanges();

    this.iaService.obtenerRecomendacionesPersonalizadas(this.apiarioSeleccionado.id).subscribe({
      next: (response: any) => {
        if (response.codigo === 200) {
          this.recomendacionesIA = response.data;
          this.procesarSugerenciasIA();
          console.log('🤖 Recomendaciones de IA:', this.recomendacionesIA);
        } else {
        }
        this.cargandoRecomendaciones = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de procesar la respuesta
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al obtener recomendaciones de IA:', err);
        this.cargandoRecomendaciones = false;
        
        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después del error
        this.cdRef.detectChanges();
      }
    });
  }

  private procesarSugerenciasIA(): void {
    if (!this.recomendacionesIA) return;

    // Extraer sugerencias del texto de IA
    const sugerenciasTexto = this.recomendacionesIA.sugerenciasIA;
    
    if (sugerenciasTexto) {
      // ✅ Tipado correcto para los parámetros
      const lineas = sugerenciasTexto.split('\n').filter((linea: string) => 
        linea.trim() && !linea.trim().startsWith('¡Hola!') && !linea.trim().startsWith('•')
      );

      this.sugerenciasAutomaticas = lineas.map((linea: string, index: number) => {
        // Extraer título y descripción
        const partes = linea.split(':');
        const titulo = partes[0]?.trim() || `Sugerencia ${index + 1}`;
        const descripcion = partes.slice(1).join(':').trim() || linea.trim();

        return {
          titulo: this.limpiarTexto(titulo),
          descripcion: this.limpiarTexto(descripcion),
          tipo: 'ia' as const
        };
      }).filter((sugerencia: SugerenciaIA) => sugerencia.descripcion.length > 10);
      
      // ✅ DETECCIÓN ESTRATÉGICA: Solo si se procesaron sugerencias
      if (this.sugerenciasAutomaticas.length > 0) {
        this.cdRef.detectChanges();
      }
    }
  }

  private limpiarTexto(texto: string): string {
    return texto
      .replace(/\*\*/g, '') // Remover **
      .replace(/\*/g, '')   // Remover *
      .replace(/^- /, '')   // Remover guiones al inicio
      .trim();
  }

  usarSugerencia(sugerencia: SugerenciaIA): void {
    this.toastService.info('Sugerencia', `Aplicando: ${sugerencia.titulo}`);
    console.log('Aplicando sugerencia de IA:', sugerencia);
    
    // Aquí podrías implementar lógica específica para aplicar la sugerencia
  }

  // ==================== MÉTODOS AUXILIARES PARA EL TEMPLATE ====================

  // Verificar si un medicamento está duplicado
  esMedicamentoDuplicado(medicamentoId: number, indiceActual: number): boolean {
    if (!medicamentoId || medicamentoId === 0) return false;
    return this.formReceta.medicamentos.some((med, index) => 
      med.id === medicamentoId && index !== indiceActual
    );
  }

  // Obtener stock de un medicamento
  obtenerStockMedicamento(id: number): number | undefined {
    const medicamento = this.medicamentosDisponibles.find(m => m.id === id);
    return medicamento?.cantidad;
  }

  // Verificar si hay medicamentos duplicados en el índice actual
  tieneMedicamentoDuplicado(medicamentoId: number, indiceActual: number): boolean {
    if (!medicamentoId || medicamentoId === 0) return false;
    return this.formReceta.medicamentos.some((med, index) => 
      med.id === medicamentoId && index !== indiceActual
    );
  }

  // Verificar si hay medicamentos sin seleccionar
  tieneMedicamentosSinSeleccionar(): boolean {
    return this.formReceta.medicamentos.some(med => !med.id || med.id === 0);
  }

  // Verificar si hay medicamentos duplicados en toda la receta
  tieneMedicamentosDuplicados(): boolean {
    const ids = this.formReceta.medicamentos.map(med => med.id).filter(id => id > 0);
    return new Set(ids).size !== ids.length;
  }

  // Verificar si hay medicamentos inválidos
  tieneMedicamentosInvalidos(): boolean {
    return this.tieneMedicamentosSinSeleccionar() || this.tieneMedicamentosDuplicados();
  }

  // Contar medicamentos seleccionados
  contarMedicamentosSeleccionados(): number {
    return this.formReceta.medicamentos.filter(med => med.id > 0).length;
  }

  // Obtener lista de medicamentos seleccionados
  obtenerMedicamentosSeleccionados(): MedicamentoRequest[] {
    return this.formReceta.medicamentos.filter(med => med.id > 0);
  }

  // Verificar stock bajo
  tieneStockBajo(id: number): boolean {
    const stock = this.obtenerStockMedicamento(id);
    return stock !== undefined && stock < 10;
  }

  // Método para forzar detección de cambios en cambios de selección de medicamentos
  onMedicamentoChange(): void {
    // ✅ DETECCIÓN ESTRATÉGICA: Solo si realmente hay cambios que afectan la vista
    this.cdRef.detectChanges();
  }

  // Método para forzar detección de cambios en cambios de formulario
  onFormChange(): void {
    // ✅ DETECCIÓN ESTRATÉGICA: Solo si realmente hay cambios que afectan la vista
    this.cdRef.detectChanges();
  }

  private inicializarChat(): void {
    this.mensajesChat = [
      {
        texto: `Hola! Soy tu asistente de apicultura. ¿En qué puedo ayudarte con el apiario #${this.apiarioSeleccionado?.numeroApiario}?`,
        tiempo: this.obtenerHoraActual(),
        tipo: 'ia',
        estado: 'enviado'
      }
    ];
    
    // ✅ DETECCIÓN ESTRATÉGICA: Solo si se inicializa el chat y afecta la vista
    this.cdRef.detectChanges();
  }

  /**
   * Enviar pregunta al chat de IA
   */
  enviarPregunta(event?: any): void {
    if (event) {
      event.preventDefault();
    }
    
    if (!this.nuevaPregunta.trim()) {
      this.toastService.warning('Atención', 'Por favor ingrese una pregunta');
      return;
    }

    if (!this.apiarioSeleccionado) {
      this.toastService.error('Error', 'No hay apiario seleccionado');
      return;
    }

    const preguntaTexto = this.nuevaPregunta.trim();

    // ✅ 1. Agregar mensaje del usuario y limpiar input
    const mensajeUsuario: MensajeChat = {
      texto: preguntaTexto,
      tiempo: this.obtenerHoraActual(),
      tipo: 'usuario',
      estado: 'enviado'
    };

    this.mensajesChat.push(mensajeUsuario);
    this.nuevaPregunta = '';
    
    // ✅ 2. Agregar mensaje de "escribiendo" de la IA
    const mensajeEscribiendo: MensajeChat = {
      texto: 'Escribiendo...',
      tiempo: this.obtenerHoraActual(),
      tipo: 'ia',
      estado: 'enviando'
    };

    this.mensajesChat.push(mensajeEscribiendo);
    this.cargandoChat = true;

    // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de todos los cambios iniciales
    this.cdRef.detectChanges();
    this.scrollToBottom();

    // ✅ 3. Enviar pregunta a la IA
    this.iaService.consultaPersonalizada(preguntaTexto).subscribe({
      next: (response: any) => {
        // Remover mensaje de "escribiendo"
        this.mensajesChat = this.mensajesChat.filter(msg => msg.estado !== 'enviando');
        
        if (response.codigo === 200) {
          // ✅ Agregar respuesta de la IA
          const mensajeIA: MensajeChat = {
            texto: response.data.respuesta,
            tiempo: this.obtenerHoraActual(),
            tipo: 'ia',
            estado: 'enviado'
          };
          
          this.mensajesChat.push(mensajeIA);
        } else {
          // ✅ Manejar error de la API
          const mensajeError: MensajeChat = {
            texto: 'Lo siento, hubo un error al procesar tu pregunta. Por favor, intenta de nuevo.',
            tiempo: this.obtenerHoraActual(),
            tipo: 'ia',
            estado: 'error'
          };
          
          this.mensajesChat.push(mensajeError);
        }
        
        this.cargandoChat = false;

        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después de procesar la respuesta
        this.cdRef.detectChanges();
        this.scrollToBottom();
      },
      error: (err: any) => {
        console.error('❌ Error en consulta a IA:', err);

        // Remover mensaje de "escribiendo"
        this.mensajesChat = this.mensajesChat.filter(msg => msg.estado !== 'enviando');
        
        // ✅ Agregar mensaje de error
        const mensajeError: MensajeChat = {
          texto: 'Lo siento, no pude conectarme con el servicio de IA. Por favor, verifica tu conexión e intenta de nuevo.',
          tiempo: this.obtenerHoraActual(),
          tipo: 'ia',
          estado: 'error'
        };
        
        this.mensajesChat.push(mensajeError);
        this.cargandoChat = false;

        // ✅ DETECCIÓN ESTRATÉGICA: Solo una vez después del error
        this.cdRef.detectChanges();
        this.scrollToBottom();
      }
    });
  }

  private scrollToBottom(): void {
    // ✅ Reducir el timeout para respuesta más inmediata
    setTimeout(() => {
      const chatMessages = document.querySelector('.chat-messages');
      if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
      }
    }, 50); // ✅ Reducido de 100ms a 50ms
  }

  /**
   * Obtener hora actual formateada
   */
  private obtenerHoraActual(): string {
    return new Date().toLocaleTimeString('es-ES', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }

  /**
   * Método para manejar la tecla Enter en el chat
   */
  onChatKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.enviarPregunta();
    }
  }

  // ==================== MÉTODOS DE PREDICCIONES ====================

  obtenerPrediccionesSalud(): void {
    this.cargandoRecomendaciones = true;
    this.sugerenciasAutomaticas = []; // Limpiar sugerencias anteriores
    
    this.iaService.obtenerPrediccionesSalud().subscribe({
      next: (response: any) => {
        if (response.codigo === 200 && response.data) {
          this.recomendacionesIA = response.data; // Guardar datos completos
          this.procesarPredicciones(response.data);
        } else {
          this.sugerenciasAutomaticas = []; // Mantener vacío si hay error
        }
        this.cargandoRecomendaciones = false;
        this.cdRef.detectChanges();
      },
      error: (err: any) => {
        console.error('❌ Error al obtener predicciones:', err);
        this.sugerenciasAutomaticas = []; // Mantener vacío si hay error
        this.cargandoRecomendaciones = false;
        this.cdRef.detectChanges();
      }
    });
  }

  private procesarPredicciones(datosPredicciones: any): void {
    console.log('📊 Datos de predicciones recibidos:', datosPredicciones); // Debug
    
    // Verificar si hay datos de prediccionesIA en la respuesta
    if (datosPredicciones && datosPredicciones.prediccionesIA) {
      this.sugerenciasAutomaticas = this.extraerSugerenciasDeTexto(datosPredicciones.prediccionesIA);
      
      // Si no se pudieron extraer sugerencias, crear una con la información general
      if (this.sugerenciasAutomaticas.length === 0) {
        this.sugerenciasAutomaticas = this.crearSugerenciasDesdeDatos(datosPredicciones);
      }
    } else {
      // No hay datos - mantener array vacío
      this.sugerenciasAutomaticas = [];
    }
    
    console.log('🎯 Sugerencias procesadas:', this.sugerenciasAutomaticas); // Debug
  }

  private extraerSugerenciasDeTexto(textoPredicciones: string): SugerenciaIA[] {
    if (!textoPredicciones) return [];

    const sugerencias: SugerenciaIA[] = [];
    
    console.log('📝 Texto original:', textoPredicciones); // Debug
    
    // Método simplificado: dividir por líneas y buscar patrones claros
    const lineas = textoPredicciones.split('\n').filter(linea => linea.trim().length > 10);
    
    let tituloActual = '';
    let contenidoActual = '';

    for (const linea of lineas) {
      const lineaTrim = linea.trim();
      
      // Detectar si es un título (contiene patrones específicos)
      const esTitulo = 
        lineaTrim.startsWith('**') || 
        lineaTrim.startsWith('##') || 
        /^\d+\.\s+[A-Z]/.test(lineaTrim) ||
        lineaTrim.toLowerCase().includes('problema potencial') ||
        lineaTrim.toLowerCase().includes('estrés térmico') ||
        lineaTrim.toLowerCase().includes('nosema') ||
        lineaTrim.toLowerCase().includes('análisis predictivo');
      
      if (esTitulo) {
        // Guardar la sugerencia anterior si existe
        if (tituloActual && contenidoActual) {
          sugerencias.push({
            titulo: this.limpiarTitulo(tituloActual),
            descripcion: this.formatearDescripcion(contenidoActual),
            tipo: 'ia' as const
          });
          console.log('✅ Sugerencia agregada:', tituloActual); // Debug
        }
        
        // Iniciar nueva sugerencia
        tituloActual = lineaTrim;
        contenidoActual = '';
      } else if (tituloActual && lineaTrim.length > 5) {
        // Agregar al contenido actual (evitar líneas muy cortas)
        if (contenidoActual) {
          contenidoActual += ' ' + lineaTrim;
        } else {
          contenidoActual = lineaTrim;
        }
      }
    }

    // Agregar la última sugerencia
    if (tituloActual && contenidoActual) {
      sugerencias.push({
        titulo: this.limpiarTitulo(tituloActual),
        descripcion: this.formatearDescripcion(contenidoActual),
        tipo: 'ia' as const
      });
    }

    // Si no se encontraron sugerencias con el método anterior, usar método de respaldo
    if (sugerencias.length === 0 && textoPredicciones.length > 50) {
      return this.crearSugerenciasDeRespaldo(textoPredicciones);
    }
    
    return sugerencias;
  }

  private limpiarTitulo(titulo: string): string {
    let tituloLimpio = titulo
      .replace(/\*\*/g, '')
      .replace(/\*/g, '')
      .replace(/^#+\s*/, '')
      .replace(/^\d+\.\s*/, '')
      .trim();

    // Agregar emojis según el contenido
    if (tituloLimpio.toLowerCase().includes('estrés térmico') || tituloLimpio.toLowerCase().includes('temperatura')) {
      return '🌡️ ' + tituloLimpio;
    } else if (tituloLimpio.toLowerCase().includes('nosema') || tituloLimpio.toLowerCase().includes('infección')) {
      return '🦠 ' + tituloLimpio;
    } else if (tituloLimpio.toLowerCase().includes('problema')) {
      return '⚠️ ' + tituloLimpio;
    } else if (tituloLimpio.toLowerCase().includes('análisis') || tituloLimpio.toLowerCase().includes('predictivo')) {
      return '🔮 ' + tituloLimpio;
    } else {
      return '📋 ' + tituloLimpio;
    }
  }

  private formatearDescripcion(descripcion: string): string {
    // Limpiar y formatear la descripción
    let descripcionLimpia = descripcion
      .replace(/\*\*/g, '')
      .replace(/\*/g, '')
      .replace(/\|/g, ' - ')
      .replace(/\s+/g, ' ')
      .trim();

    // Capitalizar primera letra y asegurar punto final
    descripcionLimpia = descripcionLimpia.charAt(0).toUpperCase() + descripcionLimpia.slice(1);
    if (!descripcionLimpia.endsWith('.') && !descripcionLimpia.endsWith('!') && !descripcionLimpia.endsWith('?')) {
      descripcionLimpia += '.';
    }

    return descripcionLimpia;
  }

  private crearSugerenciasDeRespaldo(textoPredicciones: string): SugerenciaIA[] {
    const sugerencias: SugerenciaIA[] = [];
    
    // Dividir el texto en párrafos significativos
    const parrafos = textoPredicciones.split('\n\n').filter(p => p.trim().length > 30);
    
    parrafos.forEach((parrafo, index) => {
      const lineas = parrafo.split('\n').filter(l => l.trim().length > 10);
      
      if (lineas.length > 0) {
        const primeraLinea = lineas[0].trim();
        const resto = lineas.slice(1).join(' ');
        
        sugerencias.push({
          titulo: this.limpiarTitulo(primeraLinea.substring(0, 50) + (primeraLinea.length > 50 ? '...' : '')),
          descripcion: this.formatearDescripcion(resto || primeraLinea),
          tipo: 'ia' as const
        });
      }
    });

    return sugerencias;
  }

  private crearSugerenciasDesdeDatos(datos: any): SugerenciaIA[] {
    const sugerencias: SugerenciaIA[] = [];

    // Sugerencia 1: Información general
    if (datos.ubicacion || datos.temperaturaActual) {
      sugerencias.push({
        titulo: '🌡️ Condiciones Actuales',
        descripcion: `Ubicación: ${datos.ubicacion || 'No disponible'}. Temperatura: ${datos.temperaturaActual || 'No disponible'}. Modelo usado: ${datos.modeloUsado || 'No disponible'}.`,
        tipo: 'ia'
      });
    }

    // Sugerencia 2: Resumen del historial
    if (datos.resumenHistorial) {
      const resumen = datos.resumenHistorial;
      sugerencias.push({
        titulo: '📊 Resumen del Historial',
        descripcion: `${resumen.porcentajeConHistorial} de los apiarios tienen historial médico. ${resumen.porcentajeConTratamiento} están bajo tratamiento actual.`,
        tipo: 'ia'
      });
    }

    // Sugerencia 3: Tiempo de procesamiento
    if (datos.tiempoProcesamiento) {
      sugerencias.push({
        titulo: '⏱️ Análisis Realizado',
        descripcion: `El análisis predictivo tomó ${datos.tiempoProcesamiento} y evaluó ${datos.apiariosAnalizados || 1} apiario(s).`,
        tipo: 'ia'
      });
    }

    return sugerencias;
  }

  // Métodos auxiliares para el template
  obtenerUbicacionPredicciones(): string {
    return this.recomendacionesIA?.ubicacion || 'Ubicación no disponible';
  }

  obtenerTiempoProcesamiento(): string {
    return this.recomendacionesIA?.tiempoProcesamiento || 'Tiempo no disponible';
  }

  // Método para formatear texto con HTML básico
  formatearTextoParaHTML(texto: string): string {
    if (!texto) return '';
    
    return texto
      .replace(/Probabilidad:\s*(\w+)/g, '<strong>Probabilidad: $1</strong>')
      .replace(/Medida recomendada:/g, '<br><strong>Medida recomendada:</strong>')
      .replace(/Justificación:/g, '<br><strong>Justificación:</strong>')
      .replace(/\.\s+/g, '.<br>')
      .replace(/(?:\r\n|\r|\n)/g, '<br>');
  }

  verificarSaludOllama(): void {
    this.iaService.verificarSaludOllama().subscribe({
      next: (response: any) => {
        if (response.codigo === 200 && response.data) {
          const salud = response.data;
          if (salud.ollamaDisponible) {
            this.toastService.success(
              'BEE IA en línea 🐝', 
              salud.mensaje || 'Servicio de IA funcionando correctamente'
            );
          } else {
            
          }
          console.log('🔍 Estado de Ollama:', salud);
        } else {
         
        }
      },
      error: (err: any) => {
        console.error('❌ Error al verificar salud de Ollama:', err);
   
      }
    });
  }

  filtrarApiarios(): Apiario[] {
    if (!this.terminoBusqueda.trim()) {
      return this.apiarios;
    }
    
    const termino = this.terminoBusqueda.toLowerCase();
    return this.apiarios.filter(apiario =>
      apiario.numeroApiario.toString().includes(termino) ||
      apiario.ubicacion.toLowerCase().includes(termino) ||
      apiario.salud.toLowerCase().includes(termino) ||
      apiario.id?.toString().includes(termino)
    );
  }

  /**
   * Método para actualizar apiarios (usado en varios lugares)
   */
  private actualizarApiarios(): void {
    this.cargarApiarios();
  }
}