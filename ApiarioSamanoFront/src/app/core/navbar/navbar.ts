import { Component, OnInit, OnDestroy, PLATFORM_ID, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { AudioService } from '../../services/Audio/audio-service';
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
  standalone: true,
  imports: [CommonModule]
})
export class Navbar implements OnInit, OnDestroy {
  isOperator: boolean = false;
  private routerSub!: Subscription;

  constructor(
    public router: Router, 
    private authService: AuthService,
    private audiService :AudioService,
    @Inject(PLATFORM_ID) private platformId: Object // ✅ INYECTAR PLATFORM_ID
  ) {}

  ngOnInit(): void {
    // ✅ SOLO EJECUTAR EN EL NAVEGADOR
    if (isPlatformBrowser(this.platformId)) {
      this.verificarRol(); 

      this.routerSub = this.router.events
        .pipe(filter(event => event instanceof NavigationEnd))
        .subscribe(() => {
          this.verificarRol();
        });
    }
  }

  ngOnDestroy(): void {
    if (this.routerSub) this.routerSub.unsubscribe();
  }

  shouldShowNavbar(): boolean {
    return this.router.url !== '/login';
  }

  verificarRol(): void {
    // ✅ YA NO NECESITA VERIFICACIÓN ADICIONAL PORQUE ngOnInit YA LO HIZO
    const rol = this.authService.getRoleFromToken();
    this.isOperator = rol === 'OPERADOR';
  }

  // === NAVEGACIÓN ===
  goToHome() { this.router.navigate(['/home']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)
  }
  goToProduccion() { this.router.navigate(['/produccion']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToLotes() { this.router.navigate(['/lotes']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToApiarios() { this.router.navigate(['/apiarios']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToAlmacenes() { this.router.navigate(['/almacenes']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToHerramientas() { this.router.navigate(['/herramientas']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToMateriasPrimas() { this.router.navigate(['/materias-primas']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToProveedores() { this.router.navigate(['/proveedores']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToUsuarios() { this.router.navigate(['/usuarios']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
  goToHistorialMedico() { this.router.navigate(['/medicamentos']); 
  this.audiService.play('assets/audios/boton.mp3',0.6)

  }
}