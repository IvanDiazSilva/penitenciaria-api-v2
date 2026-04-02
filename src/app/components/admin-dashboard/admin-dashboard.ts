import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // Para usar *ngIf, *ngFor y pipes
import { RouterModule, Router } from '@angular/router'; // Para la navegación y el Logout
import { AuthService } from '../../services/auth.service'; // Tu servicio de seguridad

@Component({
  selector: 'app-admin-dashboard',
  standalone: true, // Indica que no necesita estar en un AppModule
  imports: [CommonModule, RouterModule], // Importamos lo básico para que funcione el HTML
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboard implements OnInit {

  // Definimos variables para mostrar datos dinámicos en el "Escritorio"
  adminName: string = 'Estefanía';
  totalPresos: number = 1284;
  visitasHoy: number = 45;
  alertasActivas: number = 0;

  constructor(
    private authService: AuthService, 
    private router: Router
  ) { }

  ngOnInit(): void {
    /**
     * IMPORTANTE: 
     * Mientras estés diseñando la interfaz, deja estas líneas comentadas.
     * Así podrás entrar a http://localhost:4200/admin sin que te eche al login.
     */
    /*
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
    */
  }

  // Función para el botón "Salir" de la barra lateral
  onLogout(): void {
    this.authService.logout(); // Borra el token del localStorage
    this.router.navigate(['/login']); // Te manda al inicio
  }

  // Ejemplo de una función para un botón de la tabla
  verDetallesPreso(id: string) {
    console.log("Navegando al expediente del preso:", id);
    // Aquí podrías usar this.router.navigate(['/admin/expediente', id]);
  }
}