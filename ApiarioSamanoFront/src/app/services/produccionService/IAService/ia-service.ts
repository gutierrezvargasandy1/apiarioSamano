import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

// Interfaces basadas en tus DTOs
export interface OllamaResponse {
  model: string;
  response: string;
  done: boolean;
  context: string[];
  totalDuration: number;
  loadDuration: number;
  promptEvalCount: number;
  evalCount: number;
}

export interface SaludSistema {
  ollamaDisponible: boolean;
  mensaje: string;
  modeloConfigurado: string;
}

export interface ConsultaRequest {
  pregunta: string;
}

export interface ConsultaResponse {
  consulta: string;
  respuesta: string;
  modeloUsado: string;
}

export interface EstadisticasResponse {
  [key: string]: any; // Estructura flexible para estadísticas
}

export interface PrediccionesResponse {
  [key: string]: any; // Estructura flexible para predicciones
}

export interface SugerenciasResponse {
  [key: string]: any; // Estructura flexible para sugerencias
}

export interface DiagnosticoResponse {
  [key: string]: any; // Estructura flexible para diagnóstico
}

interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class IaService {

  private baseUrl = 'http://localhost:8087/api/produccion/ia';

  constructor(private http: HttpClient) { }

  private obtenerToken(): string | null {
    return localStorage.getItem('token');
  }

  private getHeaders(): HttpHeaders {
    const token = this.obtenerToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  private getJsonHeaders(): HttpHeaders {
    const token = this.obtenerToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // 📊 ANÁLISIS ESTADÍSTICO COMPLETO
  obtenerAnalisisEstadistico(): Observable<EstadisticasResponse> {
    return this.http.get<CodigoResponse<EstadisticasResponse>>(
      `${this.baseUrl}/estadisticas`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔮 PREDICCIONES DE PRODUCCIÓN
  obtenerPrediccionesProduccion(): Observable<PrediccionesResponse> {
    return this.http.get<CodigoResponse<PrediccionesResponse>>(
      `${this.baseUrl}/predicciones`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 💡 SUGERENCIAS POR COSECHA
  obtenerSugerenciasCosecha(idCosecha: number): Observable<SugerenciasResponse> {
    return this.http.get<CodigoResponse<SugerenciasResponse>>(
      `${this.baseUrl}/sugerencias/cosecha/${idCosecha}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 📈 ANÁLISIS DE RENDIMIENTO
  obtenerAnalisisRendimiento(periodo: string): Observable<EstadisticasResponse> {
    return this.http.get<CodigoResponse<EstadisticasResponse>>(
      `${this.baseUrl}/rendimiento/${periodo}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🏥 SALUD DEL SISTEMA
  verificarSaludSistema(): Observable<SaludSistema> {
    return this.http.get<CodigoResponse<SaludSistema>>(
      `${this.baseUrl}/salud`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 💬 CONSULTA PERSONALIZADA SOBRE PRODUCCIÓN
  consultaPersonalizada(pregunta: string): Observable<ConsultaResponse> {
    const request: ConsultaRequest = { pregunta };
    
    return this.http.post<CodigoResponse<ConsultaResponse>>(
      `${this.baseUrl}/consulta`,
      request,
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // 📋 DIAGNÓSTICO COMPLETO
  obtenerDiagnosticoCompleto(): Observable<DiagnosticoResponse> {
    return this.http.get<CodigoResponse<DiagnosticoResponse>>(
      `${this.baseUrl}/diagnostico`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }


}