import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

// Interfaces basadas en tus DTOs
export interface Producto {
  id?: number;
  nombre: string;
  precioMayoreo: number;
  precioMenudeo: number;
  foto?: Uint8Array | string | null;
  codigoBarras: string;
  tipoDeProducto: string;
  idLote: number;
  activo?: boolean;
}

export interface ProductoRequest {
  nombre: string;
  precioMayoreo: number;
  precioMenudeo: number;
  foto?: Uint8Array | string | null;
  codigoBarras: string;
  tipoDeProducto: string;
  idLote: number;
  activo?: boolean;
}

export interface ProductoResponse {
  id: number;
  nombre: string;
  precioMayoreo: number;
  precioMenudeo: number;
  fotoBase64: string;
  codigoBarras: string;
  tipoDeProducto: string;
  idLote: number;
  numeroSeguimientoLote: string;
  tipoProductoLote: string;
  activo: boolean;
  fechaCreacion: string;
  fechaActualizacion: string;
}

interface CodigoResponse<T> {
  codigo: number;
  descripcion: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ProductosService {

  private baseUrl = 'http://localhost:8080/api/productos';

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

  private getMultipartHeaders(): HttpHeaders {
    const token = this.obtenerToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
      // No Content-Type para FormData, se establece automáticamente
    });
  }

  // 🔹 Crear un producto
  crearProducto(productoRequest: ProductoRequest): Observable<Producto> {
    return this.http.post<CodigoResponse<Producto>>(
      `${this.baseUrl}/crear`,
      productoRequest,
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Actualizar un producto
  actualizarProducto(id: number, productoRequest: ProductoRequest): Observable<Producto> {
    return this.http.put<CodigoResponse<Producto>>(
      `${this.baseUrl}/${id}`,
      productoRequest,
      { headers: this.getJsonHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Listar todos los productos activos
  listarProductosActivos(): Observable<ProductoResponse[]> {
    return this.http.get<CodigoResponse<ProductoResponse[]>>(
      `${this.baseUrl}/listar`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Obtener un producto por ID
  obtenerProductoPorId(id: number): Observable<ProductoResponse> {
    return this.http.get<CodigoResponse<ProductoResponse>>(
      `${this.baseUrl}/${id}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data));
  }

  // 🔹 Obtener productos por lote
  obtenerProductosPorLote(idLote: number): Observable<ProductoResponse[]> {
    return this.http.get<CodigoResponse<ProductoResponse[]>>(
      `${this.baseUrl}/lote/${idLote}`,
      { headers: this.getHeaders() }
    ).pipe(map(res => res.data || []));
  }

  // 🔹 Desactivar un producto
  desactivarProducto(id: number): Observable<void> {
    return this.http.put<CodigoResponse<void>>(
      `${this.baseUrl}/${id}/desactivar`,
      null,
      { headers: this.getHeaders() }
    ).pipe(map(() => undefined));
  }

  // 🔹 Obtener foto del producto como blob
  obtenerFotoProducto(id: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/${id}/foto`,
      { 
        headers: this.getHeaders(),
        responseType: 'blob'
      }
    );
  }

  // 🔹 Obtener URL de la foto del producto
  obtenerUrlFotoProducto(id: number): string {
    return `${this.baseUrl}/${id}/foto`;
  }

  // 🔹 Actualizar foto del producto
  actualizarFotoProducto(id: number, archivo: File): Observable<void> {
    const formData = new FormData();
    formData.append('foto', archivo);

    return this.http.put<CodigoResponse<void>>(
      `${this.baseUrl}/${id}/foto`,
      formData,
      { headers: this.getMultipartHeaders() }
    ).pipe(map(() => undefined));
  }

  // 🔹 Métodos auxiliares para manejar imágenes
  convertirBase64AFile(base64: string, nombreArchivo: string): File {
    const arr = base64.split(',');
    const mime = arr[0].match(/:(.*?);/)![1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    
    return new File([u8arr], nombreArchivo, { type: mime });
  }

  // 🔹 Convertir File a Base64
  fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = error => reject(error);
    });
  }

  // 🔹 Obtener URL segura para imagen
  obtenerUrlImagenSegura(fotoBase64: string | null | undefined): string {
    if (!fotoBase64) {
      return 'assets/images/producto-placeholder.jpg';
    }
    
    if (fotoBase64.startsWith('data:')) {
      return fotoBase64;
    }
    
    return `data:image/jpeg;base64,${fotoBase64}`;
  }

  // 🔹 Métodos para respuestas completas
  crearProductoCompleto(productoRequest: ProductoRequest): Observable<CodigoResponse<Producto>> {
    return this.http.post<CodigoResponse<Producto>>(
      `${this.baseUrl}/crear`,
      productoRequest,
      { headers: this.getJsonHeaders() }
    );
  }

  actualizarProductoCompleto(id: number, productoRequest: ProductoRequest): Observable<CodigoResponse<Producto>> {
    return this.http.put<CodigoResponse<Producto>>(
      `${this.baseUrl}/${id}`,
      productoRequest,
      { headers: this.getJsonHeaders() }
    );
  }
}