import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Proveedor {
  id?: number;
  nombreEmpresa: string;
  numTelefono: string;
  nombreRepresentante : string;
  materialProvee: string;
  fotografia?: string; // Base64 string
}

export interface ProveedorRequest {
  id?: number;
  nombreEmpresa: string;
  numTelefono: string;
  materialProvee: string;
  fotografia?: string; // Solo Base64 como string
  nombreRepresentante?: string; // Agregado según tu DTO
}

interface ApiResponse<T> {
  statusCode: number;        
  message: string;          
  data: T;
 
}

@Injectable({
  providedIn: 'root'
})
export class ProveedoresService {

  private baseUrl = 'http://localhost:8086/api/proveedores';

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

  // Listar todos los proveedores
  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<ApiResponse<Proveedor[]>>(this.baseUrl, { headers: this.getHeaders() })
      .pipe(map(res => res.data || []));
  }

  // Obtener un proveedor por ID
  obtenerProveedorPorId(id: number): Observable<Proveedor> {
    return this.http.get<ApiResponse<Proveedor>>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() })
      .pipe(map(res => res.data));
  }

  // Crear un proveedor - ENVIANDO COMO JSON
  crearProveedor(proveedor: ProveedorRequest): Observable<Proveedor> {
    // Preparar el objeto para enviar
    const proveedorParaEnviar = {
      nombreEmpresa: proveedor.nombreEmpresa,
      numTelefono: proveedor.numTelefono,
      materialProvee: proveedor.materialProvee,
      fotografia: proveedor.fotografia || null, // Enviar null si no hay imagen
      nombreReprecentante: proveedor.nombreRepresentante || null // Agregar si existe
    };

    console.log('Enviando proveedor como JSON:', proveedorParaEnviar);

    return this.http.post<ApiResponse<Proveedor>>(
      this.baseUrl, 
      proveedorParaEnviar, 
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // Actualizar un proveedor - ENVIANDO COMO JSON
  actualizarProveedor(id: number, proveedor: ProveedorRequest): Observable<Proveedor> {
    // Preparar el objeto para enviar
    const proveedorParaEnviar = {
      nombreEmpresa: proveedor.nombreEmpresa,
      numTelefono: proveedor.numTelefono,
      materialProvee: proveedor.materialProvee,
      fotografia: proveedor.fotografia || null, // Enviar null si no hay imagen
      nombreReprecentante: proveedor.nombreRepresentante || null // Agregar si existe
    };

    console.log('Actualizando proveedor como JSON:', proveedorParaEnviar);

    return this.http.put<ApiResponse<Proveedor>>(
      `${this.baseUrl}/${id}`, 
      proveedorParaEnviar, 
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // Eliminar un proveedor
  eliminarProveedor(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() })
      .pipe(map(() => undefined));
  }

  // Método auxiliar para limpiar Base64 (remover prefijo data:image/...)
  public limpiarBase64(base64ConPrefijo: string): string {
    if (!base64ConPrefijo) return '';
    
    // Si ya es Base64 puro, retornar tal cual
    if (!base64ConPrefijo.startsWith('data:')) {
      return base64ConPrefijo;
    }
    
    // Remover el prefijo "data:image/...;base64,"
    return base64ConPrefijo.split(',')[1] || base64ConPrefijo;
  }

  // Método auxiliar para agregar prefijo Base64 para mostrar en frontend
  public agregarPrefijoBase64(base64Puro: string, mimeType: string = 'image/jpeg'): string {
    if (!base64Puro) return '';
    
    // Si ya tiene prefijo, retornar tal cual
    if (base64Puro.startsWith('data:')) {
      return base64Puro;
    }
    
    // Agregar prefijo para mostrar en img src
    return `data:${mimeType};base64,${base64Puro}`;
  }
}