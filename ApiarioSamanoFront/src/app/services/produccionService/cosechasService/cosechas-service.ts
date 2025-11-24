import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

// Interfaces basadas en tus DTOs y modelos
export interface Cosecha {
  id?: number;
  fechaCosecha: string; // LocalDate se convierte a string
  calidad: string;
  tipoCosecha: string;
  cantidad: number; // BigDecimal se mapea a number
  idApiario: number;
  lote?: any; // Puedes definir una interfaz Lote si necesitas más detalle
}

export interface CosechaRequest {
  idLote: number;
  calidad: string;
  tipoCosecha: string;
  cantidad: number;
  idApiario: number;
}

interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class CosechasService {

  private baseUrl = 'http://localhost:8087/api/cosechas';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object // ✅ AGREGAR INYECCIÓN
  ) { }

  private obtenerToken(): string | null {
    // ✅ VERIFICAR SI ESTAMOS EN EL NAVEGADOR
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }

  private getHeaders(): HttpHeaders {
    const token = this.obtenerToken() || '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  private getJsonHeaders(): HttpHeaders {
    const token = this.obtenerToken() || '';
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // 🔹 Crear una cosecha
  crearCosecha(cosechaRequest: CosechaRequest): Observable<Cosecha> {
    return this.http.post<CodigoResponse<Cosecha>>(
      `${this.baseUrl}/crear`,
      cosechaRequest,
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Actualizar una cosecha
  actualizarCosecha(id: number, cosechaRequest: CosechaRequest): Observable<Cosecha> {
    return this.http.put<CodigoResponse<Cosecha>>(
      `${this.baseUrl}/${id}`,
      cosechaRequest,
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Listar todas las cosechas
  listarCosechas(): Observable<Cosecha[]> {
    return this.http.get<CodigoResponse<Cosecha[]>>(
      `${this.baseUrl}/listar`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Obtener una cosecha por ID
  obtenerCosechaPorId(id: number): Observable<Cosecha> {
    return this.http.get<CodigoResponse<Cosecha>>(
      `${this.baseUrl}/${id}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Obtener cosechas por lote
  obtenerCosechasPorLote(idLote: number): Observable<Cosecha[]> {
    return this.http.get<CodigoResponse<Cosecha[]>>(
      `${this.baseUrl}/lote/${idLote}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Obtener cosechas por apiario
  obtenerCosechasPorApiario(idApiario: number): Observable<Cosecha[]> {
    return this.http.get<CodigoResponse<Cosecha[]>>(
      `${this.baseUrl}/apiario/${idApiario}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Obtener cosechas por rango de fechas
  obtenerCosechasPorRangoFechas(fechaInicio: string, fechaFin: string): Observable<Cosecha[]> {
    const params = {
      fechaInicio: fechaInicio,
      fechaFin: fechaFin
    };
    
    return this.http.get<CodigoResponse<Cosecha[]>>(
      `${this.baseUrl}/rango-fechas`,
      { 
        headers: this.getHeaders(),
        params: params
      }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Eliminar una cosecha
  eliminarCosecha(id: number): Observable<void> {
    return this.http.delete<CodigoResponse<void>>(
      `${this.baseUrl}/${id}`,
      { headers: this.getHeaders() }
    ).pipe(map(() => undefined));
  }

  // 🔹 Métodos adicionales para respuestas completas
  crearCosechaCompleta(cosechaRequest: CosechaRequest): Observable<CodigoResponse<Cosecha>> {
    return this.http.post<CodigoResponse<Cosecha>>(
      `${this.baseUrl}/crear`,
      cosechaRequest,
      { headers: this.getJsonHeaders() }
    );
  }

  actualizarCosechaCompleta(id: number, cosechaRequest: CosechaRequest): Observable<CodigoResponse<Cosecha>> {
    return this.http.put<CodigoResponse<Cosecha>>(
      `${this.baseUrl}/${id}`,
      cosechaRequest,
      { headers: this.getJsonHeaders() }
    );
  }
}