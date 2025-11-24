import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

// Interfaces basadas en tus DTOs
export interface Lote {
  id?: number;
  numeroSeguimiento: string;
  tipoProducto: string;
  fechaCreacion: string;
  idAlmacen: number;
}

export interface LoteRequest {
  idAlmacen: number;
  tipoProducto: string;
}

// Interfaces completas para AlmacenResponse y sus componentes
export interface MateriasPrimasResponse {
  id: number;
  nombre: string;
  foto: Uint8Array | string | null; // byte[] se puede manejar como Uint8Array o string base64
  cantidad: number; // BigDecimal se mapea a number
  idProveedor: number;
}

export interface HerramientasResponse {
  id: number;
  nombre: string;
  foto: Uint8Array | string | null; // byte[] se puede manejar como Uint8Array o string base64
  idProveedor: number;
}

export interface MedicamentosResponse {
  id: number;
  nombre: string;
  cantidad: number; // BigDecimal se mapea a number
  descripcion: string;
  foto: Uint8Array | string | null; // byte[] se puede manejar como Uint8Array o string base64
  idProveedor: number;
}

export interface AlmacenResponse {
  id: number;
  numeroSeguimiento: string;
  ubicacion: string;
  capacidad: number;
  materiasPrimas: MateriasPrimasResponse[];
  herramientas: HerramientasResponse[];
  medicamentos: MedicamentosResponse[];
}

export interface LoteConAlmacenResponse {
  id: number;
  numeroSeguimiento: string;
  tipoProducto: string;
  fechaCreacion: string;
  idAlmacen: number;
  almacen: AlmacenResponse;
}

interface ApiResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class LotesService {

  private baseUrl = 'http://localhost:8087/api/lotes';

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

  // 🔹 Crear o actualizar un lote
  guardarLote(loteRequest: LoteRequest): Observable<Lote> {
    return this.http.post<ApiResponse<Lote>>(
      `${this.baseUrl}/crear`, 
      loteRequest, 
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Listar todos los lotes
  listarLotes(): Observable<Lote[]> {
    return this.http.get<ApiResponse<Lote[]>>(
      this.baseUrl, 
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Obtener un lote por ID junto con su almacén
  obtenerLoteConAlmacen(id: number): Observable<LoteConAlmacenResponse> {
    return this.http.get<ApiResponse<LoteConAlmacenResponse>>(
      `${this.baseUrl}/${id}`, 
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Eliminar un lote por ID
  eliminarLote(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${id}`, 
      { headers: this.getHeaders() }
    ).pipe(map(() => undefined));
  }

  // 🔹 Método adicional para obtener la respuesta completa
  guardarLoteCompleto(loteRequest: LoteRequest): Observable<ApiResponse<Lote>> {
    return this.http.post<ApiResponse<Lote>>(
      `${this.baseUrl}/crear`, 
      loteRequest, 
      { headers: this.getJsonHeaders() }
    );
  }

  // 🔹 Método adicional para listar con respuesta completa
  listarLotesCompleto(): Observable<ApiResponse<Lote[]>> {
    return this.http.get<ApiResponse<Lote[]>>(
      this.baseUrl, 
      { headers: this.getHeaders() }
    );
  }
}