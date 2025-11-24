import { NgModule, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './auth/login/login';
import { ForgotPassword } from './auth/forgot-password/forgot-password';
import { Navbar } from './core/navbar/navbar';
import { Header } from './core/header/header';
import { Home } from './pages/home/home';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Lotes } from './pages/lotes/lotes';
import { Apiarios } from './pages/apiarios/apiarios';
import { HttpClientModule, provideHttpClient, withFetch } from '@angular/common/http'; // ✅ AGREGAR withFetch
import { Almacenes } from './pages/almacenes/almacenes';
import { Usuarios } from './pages/usuarios/usuarios';
import { Herramientas } from './pages/herramientas/herramientas';
import { MateriasPrimas } from './pages/materias-primas/materias-primas';
import { Proveedores } from './pages/proveedores/proveedores';
import { Footer } from './core/footer/footer';
import { VerificacionOTP } from './auth/verificacion-otp/verificacion-otp';
import { CambioDeContrasena } from './auth/cambio-de-contrasena/cambio-de-contrasena';
import { CambioDeContrasenaTemporal } from './auth/cambio-de-contrasena-temporal/cambio-de-contrasena-temporal';
import { Produccion } from './pages/produccion/produccion/produccion';
import { Toast } from './core/toast/toast';
import { MedicamentosComponent } from './pages/medicamentos-component/medicamentos-component';

@NgModule({
  declarations: [
    App,
    Login,
    ForgotPassword,
    Home,
    Lotes,
    Apiarios,
    Almacenes,
    Usuarios,
    Herramientas,
    MateriasPrimas,
    Proveedores,
    Footer,
    VerificacionOTP,
    CambioDeContrasena,
    CambioDeContrasenaTemporal,
    Produccion,
    Toast,
    MedicamentosComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule, // ✅ MANTENER para compatibilidad
    Navbar,
    Header,
    FormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch()) 
  ],
  bootstrap: [App]
})
export class AppModule { }