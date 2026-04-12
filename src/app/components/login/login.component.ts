import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms'; 
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router'; // <--- Añade RouterModule aquí

/**
 * Componente de Login:
 * Gestiona el acceso al sistema penal diferenciando entre roles.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  // Datos vinculados al formulario HTML mediante [(ngModel)]
  usuario: string = '';
  password: string = '';
  

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  /**
   * Ejecuta la lógica de autenticación.
   * IMPORTANTE: Enviamos solo username y password para evitar el Error 400 del servidor.
   */
  onLogin() {
  const datosLogin = {
    username: this.usuario,
    password: this.password
  };

  this.authService.login(datosLogin).subscribe({
  // Dentro de tu método onLogin()
next: (res: any) => {
  if (res && res.token) {
    localStorage.setItem('token', res.token);
    
    // 1. Normalizamos el nombre de usuario para evitar fallos por mayúsculas
    const userLogueado = this.usuario.toLowerCase().trim();
    const rolServidor = res.rol ? res.rol.toUpperCase() : '';

    // 2. LA REDIRECCIÓN AL PATH /admin
    if (userLogueado === 'admin' || rolServidor === 'ADMIN') {
      console.log("Redirigiendo al Dashboard de Administración...");
      
      // DEBE coincidir con el path: 'admin' de tu app.routes.ts
      this.router.navigate(['/admin']); 
    } 
    else if (userLogueado === 'guardia1' || rolServidor === 'GUARDIA') {
      this.router.navigate(['/listado']);
    } 
    else {
      // Si es un visitante
      this.router.navigate(['/portal-visitante']);
    }
  }
  },
  error: (err) => {
    console.error("Error en login:", err);
    alert('Credenciales incorrectas');
  }
});
}
} // Fin de la clase