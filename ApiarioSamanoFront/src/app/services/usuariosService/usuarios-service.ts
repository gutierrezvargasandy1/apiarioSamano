import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service'; 

export interface Usuario {
  id?: number;
  nombre: string;
  apellidoPa: string;
  apellidoMa: string;
  email: string;
  contrasena: string;
  rol: string;
}

export interface UsuarioRequestDTO {
  nombre: string;
  apellidoPa: string;
  apellidoMa: string;
  email: string;
  contrasena: string;
  rol: string;
}

export interface ResponseDTO<T> {
  statusCode: number;
  message: string;
  description?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class UsuariosService {

  private apiUrl = 'http://localhost:8080/api/usuarios'; 

  constructor(private http: HttpClient, private authService: AuthService) {}

  
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.obtenerToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  
  crearUsuario(usuario: UsuarioRequestDTO): Observable<ResponseDTO<Usuario>> {
    const headers = this.getAuthHeaders();
    return this.http.post<ResponseDTO<Usuario>>(this.apiUrl, usuario, { headers });
  }

  
  obtenerUsuarios(): Observable<ResponseDTO<Usuario[]>> {
    const headers = this.getAuthHeaders();
    return this.http.get<ResponseDTO<Usuario[]>>(this.apiUrl, { headers });
  }

  
  obtenerUsuarioPorId(id: number): Observable<ResponseDTO<Usuario>> {
    const headers = this.getAuthHeaders();
    return this.http.get<ResponseDTO<Usuario>>(`${this.apiUrl}/${id}`, { headers });
  }

  
  obtenerUsuarioPorEmail(email: string): Observable<ResponseDTO<Usuario>> {
    const headers = this.getAuthHeaders();
    return this.http.get<ResponseDTO<Usuario>>(`${this.apiUrl}/email/${email}`, { headers });
  }

  
  eliminarUsuario(id: number): Observable<ResponseDTO<void>> {
    const headers = this.getAuthHeaders();
    return this.http.delete<ResponseDTO<void>>(`${this.apiUrl}/${id}`, { headers });
  }

  
  actualizarUsuarioPorEmail(email: string, usuario: UsuarioRequestDTO): Observable<ResponseDTO<Usuario>> {
    const headers = this.getAuthHeaders();
    return this.http.put<ResponseDTO<Usuario>>(`${this.apiUrl}/email/${email}`, usuario, { headers });
  }
}
