import { Component, OnInit, AfterViewInit, PLATFORM_ID, Inject, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { isPlatformBrowser } from '@angular/common';
import { CosechasService, Cosecha, CosechaRequest } from '../../../services/produccionService/cosechasService/cosechas-service';
import { ApiarioService, Apiarios } from '../../../services/apiariosService/apiario-service';
import { LotesService, Lote } from '../../../services/produccionService/lotesService/lotes-service';
import { IaService, EstadisticasResponse } from '../../../services/produccionService/IAService/ia-service';

Chart.register(...registerables);

interface EstadisticasData {
  estadisticas: {
    cosechas: {
      total: number;
      porCalidad: { [key: string]: number };
      cantidadTotal: number;
      porTipo: { [key: string]: number };
      promedioPorCosecha: number;
    };
    lotes: {
      porTipoProducto: { [key: string]: number };
      total: number;
    };
  };
  analisisIA: string;
  resumen: {
    productosActivos: number;
    ultimaCosecha: string;
    totalLotes: number;
    totalProductos: number;
    totalCosechas: number;
  };
  tiempoProcesamiento: string;
  modeloUsado: string;
}

interface Sugerencia {
  titulo: string;
  texto: string;
}

// ✅ Interfaz para mensajes del chat
interface MensajeChat {
  texto: string;
  tiempo: string;
  tipo: 'usuario' | 'ia';
  estado?: 'enviando' | 'enviado' | 'error';
}

@Component({
  selector: 'app-produccion',
  standalone: false,
  templateUrl: './produccion.html',
  styleUrl: './produccion.css'
})
export class Produccion implements OnInit, AfterViewInit, OnDestroy {
  
  private chart: Chart | null = null;
  private chartIndividual: Chart | null = null;
  
  cosechas: Cosecha[] = [];
  cosechasFiltradas: Cosecha[] = [];
  terminoBusqueda: string = '';
  mostrarModalCosecha: boolean = false;
  mostrarModalRendimiento: boolean = false;
  cargando: boolean = false;
  cosechaSeleccionada: Cosecha | null = null;

  apiarios: Apiarios[] = [];
  lotes: Lote[] = [];

  // Datos de estadísticas del backend
  estadisticasData: EstadisticasData | null = null;
  cargandoEstadisticas: boolean = false;

  // Sugerencias de la IA
  sugerenciasIA: Sugerencia[] = [];
  cargandoSugerencias: boolean = false;

  formCosecha: CosechaRequest = {
    idLote: 0,
    calidad: '',
    tipoCosecha: '',
    cantidad: 0,
    idApiario: 0
  };

  // Sugerencias individuales para el modal
  sugerenciasIndividuales: any[] = [
    {
      titulo: '🌸 Optimización de Flora',
      texto: 'Considera diversificar las fuentes de néctar para mejorar la calidad del producto.'
    },
    {
      titulo: '🔍 Frecuencia de Revisión',
      texto: 'Aumenta la frecuencia de revisiones durante la temporada de alta producción.'
    }
  ];

  // ✅ NUEVAS PROPIEDADES PARA EL CHAT
  mostrarModalChat: boolean = false;
  mensajesChat: MensajeChat[] = [];
  nuevaPregunta: string = '';
  cargandoChat: boolean = false;

  constructor(
    private cosechasService: CosechasService,
    private apiarioService: ApiarioService,
    private lotesService: LotesService,
    private iaService: IaService,
    @Inject(PLATFORM_ID) private platformId: Object,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCosechas();
    this.cargarEstadisticas();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.estadisticasData) {
        this.inicializarGrafica();
      }
    }, 500);
  }

  cargarEstadisticas(): void {
    this.cargandoEstadisticas = true;
    this.cargandoSugerencias = true;
    
    this.iaService.obtenerAnalisisEstadistico().subscribe({
      next: (data: any) => {
        this.estadisticasData = data;
        console.log('Estadísticas cargadas:', data);
        
        this.procesarSugerenciasIA(data.analisisIA);
        
        this.cargandoEstadisticas = false;
        this.cargandoSugerencias = false;
        
        setTimeout(() => {
          this.inicializarGrafica();
          this.cd.detectChanges();
        }, 100);
      },
      error: (error) => {
        console.error('Error al cargar estadísticas:', error);
        this.cargandoEstadisticas = false;
        this.cargandoSugerencias = false;
        
        this.cargarSugerenciasPorDefecto();
        this.cd.detectChanges();
      }
    });
  }

  private procesarSugerenciasIA(analisisTexto: string): void {
    if (!analisisTexto) {
      this.cargarSugerenciasPorDefecto();
      return;
    }

    const sugerencias: Sugerencia[] = [];
    
    const lineas = analisisTexto.split('\n').filter(l => l.trim());
    
    let tituloActual = '';
    let textoActual = '';
    
    for (const linea of lineas) {
      if (linea.match(/^##\s+\d+\./) || linea.match(/^\*\*\d+\./)) {
        if (tituloActual && textoActual) {
          sugerencias.push({
            titulo: this.agregarIcono(tituloActual),
            texto: textoActual.trim()
          });
        }
        
        tituloActual = linea.replace(/^##\s+/, '').replace(/^\*\*/, '').replace(/\*\*$/, '').trim();
        textoActual = '';
      } 
      else if (linea.trim().startsWith('*') || linea.trim().startsWith('-')) {
        textoActual += linea.replace(/^\*\s*/, '').replace(/^-\s*/, '') + ' ';
      }
      else if (linea.trim() && !linea.startsWith('#')) {
        textoActual += linea + ' ';
      }
    }
    
    if (tituloActual && textoActual) {
      sugerencias.push({
        titulo: this.agregarIcono(tituloActual),
        texto: textoActual.trim()
      });
    }
    
    if (sugerencias.length === 0) {
      this.cargarSugerenciasPorDefecto();
    } else {
      this.sugerenciasIA = sugerencias.slice(0, 5);
    }
  }

  private agregarIcono(titulo: string): string {
    const tituloLower = titulo.toLowerCase();
    
    if (tituloLower.includes('rendimiento') || tituloLower.includes('producción')) {
      return '📈 ' + titulo;
    } else if (tituloLower.includes('flora') || tituloLower.includes('flores') || tituloLower.includes('néctar')) {
      return '🌸 ' + titulo;
    } else if (tituloLower.includes('inspección') || tituloLower.includes('revisión') || tituloLower.includes('monitoreo')) {
      return '🔍 ' + titulo;
    } else if (tituloLower.includes('cosecha') || tituloLower.includes('miel')) {
      return '🍯 ' + titulo;
    } else if (tituloLower.includes('enjambrazón') || tituloLower.includes('reina')) {
      return '👑 ' + titulo;
    } else if (tituloLower.includes('temperatura') || tituloLower.includes('clima')) {
      return '🌡️ ' + titulo;
    } else if (tituloLower.includes('salud') || tituloLower.includes('enfermedad')) {
      return '🏥 ' + titulo;
    } else if (tituloLower.includes('alimentación') || tituloLower.includes('nutrición')) {
      return '🍴 ' + titulo;
    } else {
      return '💡 ' + titulo;
    }
  }

  private cargarSugerenciasPorDefecto(): void {
    this.sugerenciasIA = [];
  }

  cargarCosechas(): void {
    this.cargando = true;
    this.cosechasService.listarCosechas().subscribe({
      next: (data) => {
        this.cosechas = data;
        this.cosechasFiltradas = data;
        this.cargando = false;
        console.log('Cosechas cargadas:', data);
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar cosechas:', error);
        this.cargando = false;
        this.cd.detectChanges();
      }
    });
  }

  cargarApiarios(): void {
    this.apiarioService.obtenerTodos().subscribe({
      next: (response) => {
        if (response.data) {
          this.apiarios = response.data;
          console.log('Apiarios cargados:', this.apiarios);
          this.cd.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error al cargar apiarios:', error);
        this.apiarios = [];
        this.cd.detectChanges();
      }
    });
  }

  cargarLotes(): void {
    this.lotesService.listarLotes().subscribe({
      next: (data) => {
        this.lotes = data;
        console.log('Lotes cargados:', this.lotes);
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar lotes:', error);
        this.lotes = [];
        this.cd.detectChanges();
      }
    });
  }

  filtrarCosechas(): void {
    if (!this.terminoBusqueda.trim()) {
      this.cosechasFiltradas = this.cosechas;
    } else {
      const termino = this.terminoBusqueda.toLowerCase();
      this.cosechasFiltradas = this.cosechas.filter(cosecha =>
        cosecha.tipoCosecha.toLowerCase().includes(termino) ||
        cosecha.calidad.toLowerCase().includes(termino) ||
        cosecha.id?.toString().includes(termino) ||
        cosecha.idApiario.toString().includes(termino) ||
        (cosecha.lote && cosecha.lote.numeroSeguimiento?.toLowerCase().includes(termino))
      );
    }
    this.cd.detectChanges();
  }

  abrirModalCosecha(): void {
    this.formCosecha = {
      idLote: 0,
      calidad: '',
      tipoCosecha: '',
      cantidad: 0,
      idApiario: 0
    };
    this.mostrarModalCosecha = true;
    this.cargarApiarios();
    this.cargarLotes();
    this.cd.detectChanges();
  }

  cerrarModalCosecha(): void {
    this.mostrarModalCosecha = false;
    this.cd.detectChanges();
  }

  abrirModalRendimiento(cosecha: Cosecha): void {
    this.cosechaSeleccionada = cosecha;
    this.mostrarModalRendimiento = true;
    
    setTimeout(() => {
      this.inicializarGraficaIndividual();
      this.cd.detectChanges();
    }, 100);
    
    this.cd.detectChanges();
  }

  cerrarModalRendimiento(): void {
    this.mostrarModalRendimiento = false;
    this.cosechaSeleccionada = null;
    if (this.chartIndividual) {
      this.chartIndividual.destroy();
      this.chartIndividual = null;
    }
    this.cd.detectChanges();
  }

  guardarCosecha(): void {
    if (!this.validarFormularioCosecha()) {
      return;
    }

    this.cargando = true;
    this.cosechasService.crearCosecha(this.formCosecha).subscribe({
      next: (data) => {
        console.log('Cosecha guardada:', data);
        this.cargarCosechas();
        this.cerrarModalCosecha();
        
        setTimeout(() => {
          this.cargarEstadisticas();
        }, 500);
      },
      error: (error) => {
        console.error('Error al guardar cosecha:', error);
        alert('Error al guardar la cosecha');
        this.cargando = false;
        this.cd.detectChanges();
      }
    });
  }

  private validarFormularioCosecha(): boolean {
    if (!this.formCosecha.idLote || this.formCosecha.idLote <= 0) {
      alert('Por favor selecciona un lote válido');
      return false;
    }
    if (!this.formCosecha.tipoCosecha.trim()) {
      alert('Por favor ingresa el tipo de cosecha');
      return false;
    }
    if (!this.formCosecha.calidad.trim()) {
      alert('Por favor ingresa la calidad');
      return false;
    }
    if (!this.formCosecha.cantidad || this.formCosecha.cantidad <= 0) {
      alert('Por favor ingresa una cantidad válida');
      return false;
    }
    if (!this.formCosecha.idApiario || this.formCosecha.idApiario <= 0) {
      alert('Por favor selecciona un apiario válido');
      return false;
    }
    return true;
  }

  eliminarCosecha(cosecha: Cosecha, event: Event): void {
    event.stopPropagation();
    
    if (!cosecha.id) return;
    
    if (confirm(`¿Estás seguro de eliminar la cosecha del ${this.formatearFecha(cosecha.fechaCosecha)}?`)) {
      this.cargando = true;
      this.cosechasService.eliminarCosecha(cosecha.id).subscribe({
        next: () => {
          console.log('Cosecha eliminada');
          this.cargarCosechas();
          
          setTimeout(() => {
            this.cargarEstadisticas();
          }, 500);
        },
        error: (error) => {
          console.error('Error al eliminar cosecha:', error);
          alert('Error al eliminar la cosecha');
          this.cargando = false;
          this.cd.detectChanges();
        }
      });
    }
  }

  editarCosecha(id: number): void {
    alert(`Editando cosecha #${id}`);
    this.cd.detectChanges();
  }

  formatearFecha(fecha: string | undefined): string {
    if (!fecha) return 'N/A';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  formatearCantidad(cantidad: number | undefined): string {
    if (!cantidad) return '0.00';
    return cantidad.toFixed(2);
  }

  calcularPorcentajeRendimiento(): number {
    if (!this.cosechaSeleccionada?.cantidad || this.cosechas.length === 0) return 0;
    
    const promedio = this.cosechas.reduce((sum, c) => sum + c.cantidad, 0) / this.cosechas.length;
    return promedio > 0 ? Math.round((this.cosechaSeleccionada.cantidad / promedio) * 100) : 0;
  }

  obtenerNombreApiario(idApiario: number): string {
    const apiario = this.apiarios.find(a => a.id === idApiario);
    return apiario ? `Apiario #${apiario.numeroApiario} - ${apiario.ubicacion}` : 'N/A';
  }

  obtenerNumeroLote(idLote: number): string {
    const lote = this.lotes.find(l => l.id === idLote);
    return lote ? lote.numeroSeguimiento : 'N/A';
  }

private inicializarGrafica(): void {
    const ctx = document.getElementById('rendimientoChart') as HTMLCanvasElement;
    
    if (!ctx) {
        console.warn('No se encontró el canvas rendimientoChart');
        return;
    }

    // ✅ Establecer dimensiones del canvas
    ctx.style.width = '100%';
    ctx.style.height = '350px';

    if (this.chart) {
        this.chart.destroy();
    }

    if (!this.estadisticasData) {
        console.warn('No hay datos de estadísticas disponibles');
        return;
    }

    const porTipo = this.estadisticasData.estadisticas.cosechas.porTipo;
    const tipos = Object.keys(porTipo);
    const cantidadesPorTipo = Object.values(porTipo);

    this.chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: tipos.length > 0 ? tipos : ['Sin Datos'],
            datasets: [
                {
                    label: 'Cantidad de Cosechas por Tipo',
                    data: cantidadesPorTipo.length > 0 ? cantidadesPorTipo : [0],
                    backgroundColor: '#fbbf24',
                    borderColor: '#f59e0b',
                    borderWidth: 2
                },
                {
                    label: 'Producción Total (kg)',
                    data: [this.estadisticasData.estadisticas.cosechas.cantidadTotal],
                    backgroundColor: '#92400e',
                    borderColor: '#78350f',
                    borderWidth: 2,
                    type: 'line'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true, // ✅ Cambiar a false si quieres control total
            aspectRatio: 2, // ✅ Ratio 2:1 (ancho:alto)
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: {
                            size: 12,
                            weight: 'bold'
                        },
                        color: '#78350f'
                    }
                },
                // ... resto de la configuración
            },
            // ... resto de las opciones
        }
    });
    
    this.cd.detectChanges();
}
  private inicializarGraficaIndividual(): void {
    const ctx = document.getElementById('rendimientoIndividualChart') as HTMLCanvasElement;
    
    if (ctx && this.cosechaSeleccionada && this.cosechaSeleccionada.cantidad) {
      if (this.chartIndividual) {
        this.chartIndividual.destroy();
      }

      const promedio = this.estadisticasData?.estadisticas.cosechas.promedioPorCosecha || 0;
      const cantidadSeleccionada = this.cosechaSeleccionada.cantidad;

      this.chartIndividual = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Cantidad Real', 'Promedio General', 'Rendimiento Esperado'],
          datasets: [{
            label: 'Comparativa (kg)',
            data: [
              cantidadSeleccionada,
              promedio,
              promedio * 1.2
            ],
            backgroundColor: [
              '#fbbf24',
              '#92400e',
              '#78350f'
            ],
            borderColor: [
              '#f59e0b',
              '#78350f',
              '#92400e'
            ],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
            title: {
              display: true,
              text: 'Análisis Comparativo de Rendimiento',
              font: {
                size: 16,
                weight: 'bold'
              }
            },
            tooltip: {
              callbacks: {
                label: (context) => {
                  const label = context.dataset.label || '';
                  const yValue = context.parsed.y ?? 0;
                  const value = yValue.toFixed(2);
                  const porcentaje = promedio > 0 ? ((yValue / promedio) * 100).toFixed(1) : '0';
                  return `${label}: ${value} kg (${porcentaje}% del promedio)`;
                }
              }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: 'Kilogramos (kg)'
              }
            }
          }
        }
      });
      this.cd.detectChanges();
    }
  }

  // ==================== MÉTODOS DEL CHAT DE IA ====================

  /**
   * Abrir modal de chat con IA
   */
  abrirModalChat(): void {
    this.mostrarModalChat = true;
    this.mensajesChat = [];
    this.nuevaPregunta = '';
    
    // Mensaje de bienvenida
    this.inicializarChat();
    
    this.cd.detectChanges();
  }

  /**
   * Cerrar modal de chat
   */
  cerrarModalChat(): void {
    this.mostrarModalChat = false;
    this.mensajesChat = [];
    this.nuevaPregunta = '';
    this.cd.detectChanges();
  }

  /**
   * Inicializar chat con mensaje de bienvenida
   */
  private inicializarChat(): void {
    this.mensajesChat = [
      {
        texto: '¡Hola! Soy tu asistente de producción apícola. Puedo ayudarte con análisis de cosechas, estadísticas y recomendaciones. ¿En qué puedo ayudarte?',
        tiempo: this.obtenerHoraActual(),
        tipo: 'ia',
        estado: 'enviado'
      }
    ];
    
    this.cd.detectChanges();
  }

  /**
   * Enviar pregunta al chat de IA
   */
  enviarPregunta(event?: any): void {
    if (event) {
      event.preventDefault();
    }
    
    if (!this.nuevaPregunta.trim()) {
      return;
    }

    const preguntaTexto = this.nuevaPregunta.trim();

    // Agregar mensaje del usuario
    const mensajeUsuario: MensajeChat = {
      texto: preguntaTexto,
      tiempo: this.obtenerHoraActual(),
      tipo: 'usuario',
      estado: 'enviado'
    };

    this.mensajesChat.push(mensajeUsuario);
    this.nuevaPregunta = '';
    
    // Agregar mensaje de "escribiendo" de la IA
    const mensajeEscribiendo: MensajeChat = {
      texto: 'Escribiendo...',
      tiempo: this.obtenerHoraActual(),
      tipo: 'ia',
      estado: 'enviando'
    };

    this.mensajesChat.push(mensajeEscribiendo);
    this.cargandoChat = true;

    this.cd.detectChanges();
    this.scrollToBottom();

    // Enviar pregunta a la IA usando el servicio de producción
    this.iaService.consultaPersonalizada(preguntaTexto).subscribe({
      next: (response: any) => {
        // Remover mensaje de "escribiendo"
        this.mensajesChat = this.mensajesChat.filter(msg => msg.estado !== 'enviando');
        
        if (response.respuesta) {
          // Agregar respuesta de la IA
          const mensajeIA: MensajeChat = {
            texto: response.respuesta,
            tiempo: this.obtenerHoraActual(),
            tipo: 'ia',
            estado: 'enviado'
          };
          
          this.mensajesChat.push(mensajeIA);
        } else {
          // Manejar error de la API
          const mensajeError: MensajeChat = {
            texto: 'Lo siento, hubo un error al procesar tu pregunta. Por favor, intenta de nuevo.',
            tiempo: this.obtenerHoraActual(),
            tipo: 'ia',
            estado: 'error'
          };
          
          this.mensajesChat.push(mensajeError);
        }
        
        this.cargandoChat = false;
        this.cd.detectChanges();
        this.scrollToBottom();
      },
      error: (err: any) => {
        console.error('❌ Error en consulta a IA:', err);

        // Remover mensaje de "escribiendo"
        this.mensajesChat = this.mensajesChat.filter(msg => msg.estado !== 'enviando');
        
        // Agregar mensaje de error
        const mensajeError: MensajeChat = {
          texto: 'Lo siento, no pude conectarme con el servicio de IA. Por favor, verifica tu conexión e intenta de nuevo.',
          tiempo: this.obtenerHoraActual(),
          tipo: 'ia',
          estado: 'error'
        };
        
        this.mensajesChat.push(mensajeError);
        this.cargandoChat = false;
        this.cd.detectChanges();
        this.scrollToBottom();
      }
    });
  }

  /**
   * Scroll automático al final del chat
   */
  private scrollToBottom(): void {
    setTimeout(() => {
      const chatMessages = document.querySelector('.chat-messages');
      if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
      }
    }, 50);
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

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
    if (this.chartIndividual) {
      this.chartIndividual.destroy();
    }
  }
}