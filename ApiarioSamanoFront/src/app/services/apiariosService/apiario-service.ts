import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';

export interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

export interface ApiarioRequestDTO {
  numeroApiario: number;
  ubicacion: string;
  salud: string;
  dispositivoId?: string; // 🔹 Nueva propiedad opcional
}

export interface MedicamentosRequestDTO {
  id: number;
}

export interface MedicamentosResponse {
  id: number;
  nombre: string;
  cantidad: number;
  descripcion: string;
  foto: string;
  idProveedor: number;
}

export interface OllamaRequest {
  model: string;
  prompt: string;
  stream?: boolean;
  system?: string;
  context?: string;
  options?: Options;
}

export interface OllamaResponse {
  model: string;
  response: string;
  done?: boolean;
  context?: string[];
  totalDuration?: number;
  loadDuration?: number;
  promptEvalCount?: number;
  evalCount?: number;
}

export interface Options {
  temperature?: number;
  top_k?: number;
  top_p?: number;
  num_predict?: number;
  seed?: number;
  repeatPenalty?: number;
}

export interface RecetaRequest {
  descripcion: string;
  medicamentos: MedicamentosRequestDTO[];
}

export interface RecetaResponse {
  id: number;
  idRecetaPadre: number;
  descripcion: string;
  fechaDeCreacion: string;
}

export interface Apiarios {
  id: number;
  numeroApiario: number;
  ubicacion: string;
  salud: string;
  dispositivoId: string | null; // 🔹 Nueva columna - ID único del ESP32
  fechaVinculacion: string | null; // 🔹 Nueva columna - Fecha de vinculación
  receta: Receta | null;
  historialMedico: HistorialMedico | null;
}

export interface Receta {
  id: number;
  descripcion: string;
  fechaDeCreacion: string;
  medicamentos: RecetaMedicamento[];
}

export interface HistorialMedico {
  id: number;
  fechaAplicacion: string;
  notas: string;
}

export interface HistorialRecetas {
  id: number;
  historialMedico: HistorialMedico;
  receta: Receta;
}

export interface RecetaMedicamento {
  id: number;
  receta: Receta;
  idMedicamento: number;
  medicamentoInfo: MedicamentosResponse;
}

// 🔹 Interfaces para dispositivos
export interface Dispositivo {
  dispositivoId: string;
  nombre?: string;
  tipo?: string;
  estado?: string;
  ultimaConexion?: string;
  datos?: any;
}

export interface DispositivosMap {
  [dispositivoId: string]: Dispositivo;
}

@Injectable({
  providedIn: 'root'
})
export class ApiarioService {

  private apiUrl = 'http://localhost:8082/api/apiarios';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.obtenerToken(); 
    console.log("este es el token que se manda", token);
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`  
    });
  }

  // 🟢 Crear nuevo apiario
  crearApiario(request: ApiarioRequestDTO): Observable<CodigoResponse<Apiarios>> {
    return this.http.post<CodigoResponse<Apiarios>>(
      this.apiUrl,
      request,
      { headers: this.getHeaders() }
    );
  }

  // 🟡 Modificar un apiario existente
  modificarApiario(id: number, datosActualizados: ApiarioRequestDTO): Observable<CodigoResponse<Apiarios>> {
    return this.http.put<CodigoResponse<Apiarios>>(
      `${this.apiUrl}/${id}`,
      datosActualizados,
      { headers: this.getHeaders() }
    );
  }

  // 🔴 Eliminar apiario
  eliminarApiario(id: number): Observable<CodigoResponse<void>> {
    return this.http.delete<CodigoResponse<void>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔹 Agregar receta a un apiario
  agregarReceta(idApiario: number, recetaRequest: RecetaRequest): Observable<CodigoResponse<Receta>> {
    return this.http.post<CodigoResponse<Receta>>(
      `${this.apiUrl}/${idApiario}/recetas`,
      recetaRequest,
      { headers: this.getHeaders() }
    );
  }

  // 🔹 Eliminar receta cumplida
  eliminarRecetaCumplida(idApiario: number): Observable<CodigoResponse<any>> {
    return this.http.delete<CodigoResponse<any>>(
      `${this.apiUrl}/${idApiario}/receta`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener todos los apiarios
  obtenerTodos(): Observable<CodigoResponse<Apiarios[]>> {
    console.log("obteniendo apiarios");
    return this.http.get<CodigoResponse<Apiarios[]>>(
      this.apiUrl,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener apiario por ID
  obtenerPorId(id: number): Observable<CodigoResponse<Apiarios>> {
    return this.http.get<CodigoResponse<Apiarios>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener historial completo de un apiario
  obtenerHistorialCompleto(idApiario: number): Observable<CodigoResponse<any>> {
    return this.http.get<CodigoResponse<any>>(
      `${this.apiUrl}/${idApiario}/historial-completo`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener historial médico por ID con recetas y medicamentos completos
  obtenerHistorialMedicoPorId(idHistorial: number): Observable<CodigoResponse<any>> {
    return this.http.get<CodigoResponse<any>>(
      `${this.apiUrl}/historial-medico/${idHistorial}`,
      { headers: this.getHeaders() }
    );
  }

  // ===========================
  // VINCULACIÓN DE DISPOSITIVOS
  // ===========================

  // 🔗 Vincular dispositivo a apiario
  vincularDispositivo(idApiario: number, dispositivoId: string): Observable<CodigoResponse<Apiarios>> {
    return this.http.post<CodigoResponse<Apiarios>>(
      `${this.apiUrl}/${idApiario}/vincular-dispositivo`,
      { dispositivoId },
      { headers: this.getHeaders() }
    );
  }

  // 🔗 Desvincular dispositivo de apiario
  desvincularDispositivo(idApiario: number): Observable<CodigoResponse<Apiarios>> {
    return this.http.delete<CodigoResponse<Apiarios>>(
      `${this.apiUrl}/${idApiario}/desvincular-dispositivo`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener apiarios sin dispositivo vinculado
  obtenerApiariosSinDispositivo(): Observable<CodigoResponse<Apiarios[]>> {
    return this.http.get<CodigoResponse<Apiarios[]>>(
      `${this.apiUrl}/sin-dispositivo`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener apiario por dispositivo ID
  obtenerApiarioPorDispositivoId(dispositivoId: string): Observable<CodigoResponse<Apiarios>> {
    return this.http.get<CodigoResponse<Apiarios>>(
      `${this.apiUrl}/dispositivo/${dispositivoId}`,
      { headers: this.getHeaders() }
    );
  }

  // ===========================
  // ESTADO MQTT
  // ===========================
  obtenerEstadoMqtt(): Observable<string> {
    return this.http.get(
      `${this.apiUrl}/mqtt/status`,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // ===========================
  // COMANDOS MQTT
  // ===========================

  // 🌀 Control del Ventilador
  controlarVentilador(id: string, estado: boolean): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/${id}/ventilador/${estado}`,
      null,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // 💡 Control de la Luz
  controlarLuz(id: string, estado: boolean): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/${id}/luz/${estado}`,
      null,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // 🔧 Control del Servo 1
  controlarServo1(id: string, grados: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/${id}/servo1/${grados}`,
      null,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // 🔧 Control del Servo 2
  controlarServo2(id: string, grados: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/${id}/servo2/${grados}`,
      null,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // ⚙️ Control del Motor DC - L298N
  controlarMotorDC(id: string, estado: boolean): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/${id}/motor/${estado}`,
      null,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // 🌈 Control del LED RGB
  controlarLedRGB(id: string, r: number, g: number, b: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/${id}/rgb/${r}/${g}/${b}`,
      null,
      { headers: this.getHeaders(), responseType: 'text' }
    );
  }

  // ===========================
  // DISPOSITIVOS MQTT
  // ===========================

  // 📡 Obtener todos los dispositivos detectados
  obtenerDispositivosDetectados(): Observable<DispositivosMap> {
    return this.http.get<DispositivosMap>(
      `${this.apiUrl}/dispositivos/detectados`,
      { headers: this.getHeaders() }
    );
  }

  // 📡 Obtener un dispositivo específico por ID
  obtenerDispositivo(dispositivoId: string): Observable<Dispositivo> {
    return this.http.get<Dispositivo>(
      `${this.apiUrl}/dispositivos/${dispositivoId}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔹 Métodos adicionales para Ollama (si los necesitas)
  consultarOllama(request: OllamaRequest): Observable<OllamaResponse> {
    const ollamaUrl = 'http://localhost:11434/api/generate';
    return this.http.post<OllamaResponse>(
      ollamaUrl,
      request,
      { headers: this.getHeaders() }
    );
  }

  // 🔹 Obtener medicamentos (si necesitas este endpoint)
  obtenerMedicamentos(): Observable<CodigoResponse<MedicamentosResponse[]>> {
    return this.http.get<CodigoResponse<MedicamentosResponse[]>>(
      `${this.apiUrl}/medicamentos`,
      { headers: this.getHeaders() }
    );
  }
}