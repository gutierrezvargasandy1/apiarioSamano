import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

export interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

export interface MateriasPrimasRequest {
  nombre: string;
  foto: string; // o ArrayBuffer si manejas bytes
  cantidad: number;
  idAlmacen: number;
  idProvedor: number; // Nota: "idProvedor" con 'e' según tu DTO
}

export interface MateriasPrimasResponse {
  id: number;
  nombre: string;
  foto: string; // o ArrayBuffer si manejas bytes
  cantidad: number;
  idProveedor: number; // Nota: "idProveedor" con 'ee' según tu DTO
}

export interface MateriasPrimasConProveedorDTO {
  id: number;
  nombre: string;
  foto: string; // o ArrayBuffer si manejas bytes
  cantidad: number;
  almacen: AlmacenResponse;
  proveedor: ProveedorResponseDTO;
}

export interface AlmacenResponse {
  // Define las propiedades según tu DTO de Almacen
  id?: number;
  numeroSeguimiento?: string;
  ubicacion?: string;
  capacidad?: number;
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
export class MateriasPrimasService {

  private apiUrl = 'http://localhost:8081/api/materias-primas';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.obtenerToken(); 
    console.log("este es el token que se manda", token);
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`  
    });
  }

  // 🟢 Crear nueva materia prima
  guardar(request: MateriasPrimasRequest): Observable<CodigoResponse<MateriasPrimasResponse>> {
    return this.http.post<CodigoResponse<MateriasPrimasResponse>>(
      `${this.apiUrl}/crear`,
      request,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener todas las materias primas
  obtenerTodas(): Observable<CodigoResponse<MateriasPrimasResponse[]>> {
    console.log("obteniendo materias primas");
    return this.http.get<CodigoResponse<MateriasPrimasResponse[]>>(
      this.apiUrl,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener materia prima por ID
  obtenerPorId(id: number): Observable<CodigoResponse<MateriasPrimasResponse>> {
    return this.http.get<CodigoResponse<MateriasPrimasResponse>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔴 Eliminar materia prima
  eliminarPorId(id: number): Observable<CodigoResponse<void>> {
    return this.http.delete<CodigoResponse<void>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ================== MÉTODOS CON PROVEEDOR ==================

  // 🔍 Obtener todas las materias primas con información de proveedor
  obtenerTodasConProveedor(): Observable<CodigoResponse<MateriasPrimasConProveedorDTO[]>> {
    return this.http.get<CodigoResponse<MateriasPrimasConProveedorDTO[]>>(
      `${this.apiUrl}/con-proveedor`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener materia prima por ID con información de proveedor
  obtenerPorIdConProveedor(id: number): Observable<CodigoResponse<MateriasPrimasConProveedorDTO>> {
    return this.http.get<CodigoResponse<MateriasPrimasConProveedorDTO>>(
      `${this.apiUrl}/con-proveedor/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener materias primas por proveedor
  obtenerPorProveedor(idProveedor: number): Observable<CodigoResponse<MateriasPrimasResponse[]>> {
    return this.http.get<CodigoResponse<MateriasPrimasResponse[]>>(
      `${this.apiUrl}/proveedor/${idProveedor}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener materias primas por almacén
  obtenerPorAlmacen(idAlmacen: number): Observable<CodigoResponse<MateriasPrimasResponse[]>> {
    return this.http.get<CodigoResponse<MateriasPrimasResponse[]>>(
      `${this.apiUrl}/almacen/${idAlmacen}`,
      { headers: this.getHeaders() }
    );
  }


}