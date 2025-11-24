import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { Almacen } from '../almacen-service';

export interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

export interface HerramientasRequest {
  id?: number | null;
  nombre: string;
  foto: string; // o ArrayBuffer si manejas bytes
  idAlmacen: number;
  idProveedor: number;
}

export interface HerramientasResponse {
  id: number;
  nombre: string;
  foto: string; // o ArrayBuffer si manejas bytes
  idProveedor: number;
}

export interface HerramientasConProveedorResponse {
  id: number;
  nombre: string;
  idAlmacen?: number; /// Quiero que salga el nnombdre del almacen
  foto: string; // o ArrayBuffer si manejas bytes
  proveedor: ProveedorResponseDTO;
}

export interface ProveedorResponseDTO {
  // Define las propiedades según tu DTO de Proveedor
  id?: number;
  nombreEmpresa?: string;
  nombreRepresentante?: string;
  numTelefono?: string;
  materialProvee?: string;
  fotografia?: string;
}

@Injectable({
  providedIn: 'root'
})
export class HerramientasService {

  private apiUrl = 'http://localhost:8081/api/herramientas';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.obtenerToken(); 
    console.log("este es el token que se manda", token);
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`  
    });
  }

  // 🟢 Crear nueva herramienta
  guardar(request: HerramientasRequest): Observable<CodigoResponse<HerramientasResponse>> {
    return this.http.post<CodigoResponse<HerramientasResponse>>(
      `${this.apiUrl}/crear`,
      request,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener todas las herramientas
  obtenerTodas(): Observable<CodigoResponse<HerramientasResponse[]>> {
    console.log("obteniendo herramientas");
    return this.http.get<CodigoResponse<HerramientasResponse[]>>(
      this.apiUrl,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener herramienta por ID
  obtenerPorId(id: number): Observable<CodigoResponse<HerramientasResponse>> {
    return this.http.get<CodigoResponse<HerramientasResponse>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔴 Eliminar herramienta
  eliminar(id: number): Observable<CodigoResponse<void>> {
    return this.http.delete<CodigoResponse<void>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ================== MÉTODOS CON PROVEEDOR ==================

  // 🔍 Obtener todas las herramientas con información de proveedor
  obtenerTodasConProveedor(): Observable<CodigoResponse<HerramientasConProveedorResponse[]>> {
    return this.http.get<CodigoResponse<HerramientasConProveedorResponse[]>>(
      `${this.apiUrl}/con-proveedor`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener herramienta por ID con información de proveedor
  obtenerPorIdConProveedor(id: number): Observable<CodigoResponse<HerramientasConProveedorResponse>> {
    return this.http.get<CodigoResponse<HerramientasConProveedorResponse>>(
      `${this.apiUrl}/con-proveedor/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener herramientas por proveedor
  obtenerPorProveedor(idProveedor: number): Observable<CodigoResponse<HerramientasResponse[]>> {
    return this.http.get<CodigoResponse<HerramientasResponse[]>>(
      `${this.apiUrl}/proveedor/${idProveedor}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔄 Método para actualizar herramienta (si lo necesitas)
  actualizar(id: number, request: HerramientasRequest): Observable<CodigoResponse<HerramientasResponse>> {
    return this.http.put<CodigoResponse<HerramientasResponse>>(
      `${this.apiUrl}/${id}`,
      request,
      { headers: this.getHeaders() }
    );
  }
}