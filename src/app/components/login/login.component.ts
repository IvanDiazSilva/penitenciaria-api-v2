import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router'; 
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

/**
 * Componente de Login:
 * Gestiona el acceso al sistema penal diferenciando entre roles.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  // Datos vinculados al formulario HTML mediante [(ngModel)]
  usuario: string = '';
  password: string = '';
  rolSeleccionado: string = 'Visitante'; // Valor por defecto del selector

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  /**
   * Ejecuta la lógica de autenticación.
   * IMPORTANTE: Enviamos solo username y password para evitar el Error 400 del servidor.
   */
  onLogin() {
    // Creamos el objeto JSON que espera la API de Java (Iván)
    const datosLogin = {
      username: this.usuario,
      password: this.password
    };

    console.log("🚀 Enviando credenciales al servidor:", datosLogin);

    this.authService.login(datosLogin).subscribe({
      next: (res: any) => {
        /**
         * Si el servidor responde 200 OK y nos entrega un Token:
         */
        if (res && res.token) {
          // 1. Guardamos los datos de sesión en el navegador
          localStorage.setItem('token', res.token);
          localStorage.setItem('role', this.rolSeleccionado);
          
          console.log('✅ Login exitoso. Rol activo:', this.rolSeleccionado);
          
          // 2. EL SEMÁFORO DE RUTAS (Lógica de redirección por Rol):
          if (this.rolSeleccionado === 'Visitante') {
            // Si el usuario eligió ser Visitante, va directo al portal de visitas
            console.log('➡️ Redirigiendo al Portal de Visitas...');
            this.router.navigate(['/portal-visitante']); 
          } else {
            // Si es Admin o Guardia, va al listado general de internos
            console.log('➡️ Redirigiendo al Listado de Internos...');
            this.router.navigate(['/listado']);
          }

        } else {
          // Caso en que el servidor responde pero el JSON no trae el campo 'token'
          alert('Error de seguridad: El servidor no ha generado una llave de acceso.');
        }
      },
      error: (err) => {
        /**
         * Manejo de errores de red o credenciales:
         */
        console.error("❌ Error en la petición:", err);
        
        if (err.status === 400) {
          alert('Error 400: El formato de los datos es incorrecto para el servidor.');
        } else if (err.status === 401) {
          alert('Acceso denegado: Usuario o contraseña incorrectos.');
        } else {
          alert('No se pudo establecer conexión con el servidor de la penitenciaría.');
        }
      }
    }); // Fin del subscribe
  } // Fin del método onLogin
} // Fin de la clase