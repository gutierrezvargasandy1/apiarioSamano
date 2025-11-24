import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

export interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

export interface MedicamentosRequest {
  nombre: string;
  descripcion: string;
  idAlmacen: number;
  cantidad: string; // Nota: string según tu DTO
  idProveedor: number;
  foto: string; // o ArrayBuffer si manejas bytes
}

export interface MedicamentosResponse {
  id: number;
  nombre: string;
  cantidad: number; // Nota: number (BigDecimal en Java se convierte a number)
  descripcion: string;
  foto: string; // o ArrayBuffer si manejas bytes
  idProveedor: number;
}

export interface Medicamentos {
  id: number;
  nombre: string;
  cantidad: number; // Nota: number (BigDecimal en Java se convierte a number)
  descripcion: string;
  foto: string; // o ArrayBuffer si manejas bytes
  idProveedor: number;
}

export interface MedicamentosConProveedorResponse {
  id: number;
  nombre: string;
  cantidad: number;
  descripcion: string;
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
export class MedicamentosService {

  private apiUrl = 'http://localhost:8081/api/medicamentos';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.obtenerToken(); 
    console.log("este es el token que se manda", token);
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`  
    });
  }

  // 🟢 Crear nuevo medicamento
  crear(request: MedicamentosRequest): Observable<CodigoResponse<MedicamentosResponse>> {
    return this.http.post<CodigoResponse<MedicamentosResponse>>(
      `${this.apiUrl}/crear`,
      request,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener medicamento por ID
  obtenerPorId(id: number): Observable<CodigoResponse<MedicamentosResponse>> {
    return this.http.get<CodigoResponse<MedicamentosResponse>>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener todos los medicamentos
  obtenerTodos(): Observable<CodigoResponse<MedicamentosResponse[]>> {
    console.log("obteniendo medicamentos");
    return this.http.get<CodigoResponse<MedicamentosResponse[]>>(
      `${this.apiUrl}/todos`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener todos los medicamentos con información de proveedor
  obtenerTodosConProveedor(): Observable<CodigoResponse<MedicamentosConProveedorResponse[]>> {
    return this.http.get<CodigoResponse<MedicamentosConProveedorResponse[]>>(
      `${this.apiUrl}/todos-con-proveedor`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener medicamentos por proveedor (sin detalle)
  obtenerPorProveedor(idProveedor: number): Observable<CodigoResponse<MedicamentosResponse[]>> {
    return this.http.get<CodigoResponse<MedicamentosResponse[]>>(
      `${this.apiUrl}/proveedor/${idProveedor}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔍 Obtener medicamentos por proveedor (con detalle)
  obtenerPorProveedorConDetalle(idProveedor: number): Observable<CodigoResponse<MedicamentosConProveedorResponse[]>> {
    return this.http.get<CodigoResponse<MedicamentosConProveedorResponse[]>>(
      `${this.apiUrl}/proveedor-detalle/${idProveedor}`,
      { headers: this.getHeaders() }
    );
  }

  // 🔴 Eliminar medicamento
  eliminar(id: number): Observable<CodigoResponse<void>> {
    return this.http.delete<CodigoResponse<void>>(
      `${this.apiUrl}/eliminar/${id}`,
      { headers: this.getHeaders() }
    );
  }

 

}