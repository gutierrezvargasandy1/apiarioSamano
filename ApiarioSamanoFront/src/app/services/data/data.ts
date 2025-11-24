import { Injectable } from '@angular/core';
@Injectable({
  providedIn: 'root'
})
export class DataService {
  setOtp(codigoOTP: string) {
    this.otp = codigoOTP;
  }
  setEmail(email: string) {
    this.email = email;
  }
  private email: string = '';
  private otp: string = '';
  private id: number = 0;
  private contrasenaTemporal: string = '';

  setContrasenaTemporal(contrasena: string) {
    this.contrasenaTemporal = contrasena;
  }

  getContrasenaTemporal(): string {
    return this.contrasenaTemporal;
  }

  setData(email: string, otp: string) {
    this.email = email;
    this.otp = otp;
  }

  getId(): number {
    return this.id;
  }

  setId(id: number) {
    this.id = id;
  }

  getEmail(): string {
    return this.email;
  }

  getOtp(): string {
    return this.otp;
  }

  clearData() {
    this.email = '';
    this.otp = '';
    this.id = 0;  
  }
}
