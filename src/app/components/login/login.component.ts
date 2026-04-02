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
  // Vinculados a los inputs mediante [(ngModel)]
  usuario: string = '';
  password: string = '';
  rolSeleccionado: string = 'Visitante'; // Valor inicial del selector

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  /**
   * Ejecuta la lógica de autenticación al enviar el formulario.
   */
  onLogin() {
    /**
     * AJUSTE CLAVE: Iván añadió 'role' en su LoginRequest (DTO).
     * Si no enviamos el campo, el servidor podría dar error 400 o 500
     * al intentar mapear el JSON.
     */
    const datosLogin = {
      username: this.usuario,
      password: this.password,
      role: this.rolSeleccionado // Descomentado para coincidir con el DTO de Java
    };

    console.log("🚀 Enviando credenciales:", datosLogin);

    this.authService.login(datosLogin).subscribe({
      next: (res: any) => {
        /**
         * Verificamos que la respuesta contenga el token.
         * Nota: Si Iván devuelve el token dentro de un objeto, 
         * asegúrate de que el nombre de la propiedad sea 'token'.
         */
        if (res && res.token) {
          // Guardamos los datos de sesión localmente
          localStorage.setItem('token', res.token);
          localStorage.setItem('role', this.rolSeleccionado);
          
          console.log('✅ Acceso garantizado como:', this.rolSeleccionado);
          
          // Navegación a la página principal de la aplicación
          this.router.navigate(['/listado']); 
        } else {
          // Caso en que el servidor responde 200 pero sin contenido útil
          alert('Error inesperado: El servidor no devolvió una llave de acceso.');
        }
      },
      error: (err) => {
        /**
         * Si ves el error de CORS aquí, es que el navegador bloqueó la respuesta
         * antes de que Angular pudiera leerla.
         */
        console.error("❌ Error en la petición:", err);
        
        if (err.status === 0) {
          alert('Error de conexión: El servidor no responde o hay un bloqueo de CORS.');
        } else {
          alert('Credenciales incorrectas para el rol seleccionado.');
        }
      }
    });
  }
}