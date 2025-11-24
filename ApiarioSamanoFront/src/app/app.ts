import { Component } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
})
export class App {
  showLayout = true;

  constructor(private router: Router) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        // Rutas donde NO se debe mostrar la navbar, header y footer
        const rutasSinLayout = [
          '/login',
          '/forgot-password',
          '/verificacion-otp',
          '/cambiar-contrasena'
        ];

        
        this.showLayout = !rutasSinLayout.some(ruta => event.url.startsWith(ruta));
      });
  }
}
