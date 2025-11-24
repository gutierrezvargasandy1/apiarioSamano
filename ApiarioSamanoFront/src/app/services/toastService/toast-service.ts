import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { AudioService } from '../Audio/audio-service';

export interface ToastMessage {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  private toastSubject = new Subject<ToastMessage>();
  public toasts$ = this.toastSubject.asObservable();
  private idCounter = 0;

  constructor(private audioService: AudioService) {}

  /** Muestra el toast */
  private show(type: 'success' | 'error' | 'warning' | 'info', title: string, message: string, duration: number = 4000): void {
    const toast: ToastMessage = { id: this.idCounter++, type, title, message, duration };
    this.toastSubject.next(toast);
  }

  /** Toast de éxito */
  success(title: string, message: string, duration?: number): void {
    this.show('success', title, message, duration);
    this.audioService.play('assets/audios/Exito.mp3', 0.6);
  }

  /** Toast de error */
  error(title: string, message: string, duration?: number): void {
    this.show('error', title, message, duration);
    this.audioService.play('assets/audios/error.mp3', 0.6);
  }

  /** Toast de advertencia */
  warning(title: string, message: string, duration?: number): void {
    this.show('warning', title, message, duration);
    this.audioService.play('assets/audios/Advertencia.mp3', 0.6);
  }

  /** Toast de información */
  info(title: string, message: string, duration?: number): void {
    this.show('info', title, message, duration);
    this.audioService.play('assets/audios/Advertencia2.mp3', 0.6);
  }
}
