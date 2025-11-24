import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ToastService, ToastMessage } from '../../services/toastService/toast-service';
import { Subscription } from 'rxjs';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-toast',
  standalone: false,
  templateUrl: './toast.html',
  styleUrls: ['./toast.css'],
  animations: [
    trigger('slideIn', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ])
  ]
})
export class Toast implements OnInit, OnDestroy {
  toasts: ToastMessage[] = [];
  private subscription: Subscription = new Subscription();
  private timeouts: Map<number, any> = new Map();

  constructor(
    private toastService: ToastService,
    private cd: ChangeDetectorRef // Inyectar ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.subscription = this.toastService.toasts$.subscribe(toast => {
      this.toasts.push(toast);
      this.cd.detectChanges(); // Actualizar vista después de agregar toast

      // Auto-cierre con duración
      if (toast.duration) {
        const timeout = setTimeout(() => {
          this.removeToast(toast.id);
          this.cd.detectChanges(); // Actualizar vista después de auto-remover
        }, toast.duration);

        this.timeouts.set(toast.id, timeout);
      }
    });
    this.cd.detectChanges(); // Actualizar vista después de inicializar suscripción
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
    this.timeouts.forEach(timeout => clearTimeout(timeout));
    this.timeouts.clear();
    this.cd.detectChanges(); // Actualizar vista después de limpiar recursos
  }

  removeToast(id: number) {
    const timeout = this.timeouts.get(id);
    if (timeout) {
      clearTimeout(timeout);
      this.timeouts.delete(id);
      this.cd.detectChanges(); // Actualizar vista después de limpiar timeout
    }
    this.toasts = this.toasts.filter(toast => toast.id !== id);
    this.cd.detectChanges(); // Actualizar vista después de remover toast
  }

  getIcon(type: 'success' | 'error' | 'warning' | 'info'): string {
    const icons: Record<string, string> = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    };
    return icons[type] || 'ℹ️';
  }

  // Método adicional para limpiar todos los toasts
  clearAllToasts(): void {
    this.timeouts.forEach(timeout => clearTimeout(timeout));
    this.timeouts.clear();
    this.toasts = [];
    this.cd.detectChanges(); // Actualizar vista después de limpiar todos los toasts
  }

  // Método para pausar auto-cierre
  pauseAutoClose(id: number): void {
    const timeout = this.timeouts.get(id);
    if (timeout) {
      clearTimeout(timeout);
      this.timeouts.delete(id);
      this.cd.detectChanges(); // Actualizar vista después de pausar
    }
  }

  // Método para reanudar auto-cierre
  resumeAutoClose(id: number, duration: number): void {
    const timeout = setTimeout(() => {
      this.removeToast(id);
      this.cd.detectChanges(); // Actualizar vista después de reanudar y remover
    }, duration);

    this.timeouts.set(id, timeout);
    this.cd.detectChanges(); // Actualizar vista después de reanudar
  }
}