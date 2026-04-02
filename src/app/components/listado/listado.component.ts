import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VisitaService } from '../../services/visita.service';
import { Router, RouterModule } from '@angular/router';

/**
 * Decorador @Component: Configura el componente de listado.
 */
@Component({
  selector: 'app-listado',
  standalone: true,              // Componente autónomo (no requiere NgModules)
  imports: [CommonModule, RouterModule], // CommonModule para directivas como *ngFor y *ngIf
  templateUrl: './listado.html',
  styleUrls: ['./listado.css']
})
export class ListadoComponent implements OnInit {
  
  // Array para almacenar la lista de visitas que viene del servidor
  visitas: any[] = [];
  
  // Flag para controlar el estado visual de "cargando" en la interfaz
  cargando: boolean = true;

  /**
   * Se inyectan los servicios necesarios:
   * - VisitaService: Para obtener los datos de la API.
   * - Router: Para redirigir al usuario (ej. al login).
   */
  constructor(
    private visitaService: VisitaService,
    private router: Router
  ) {}

  /**
   * ngOnInit: Método del ciclo de vida de Angular.
   * Se ejecuta automáticamente una vez que el componente se ha inicializado.
   * Es el lugar ideal para realizar la carga inicial de datos.
   */
  ngOnInit(): void {
    this.cargarVisitas();
  }

  /**
   * Lógica para obtener las visitas desde el servicio.
   */
  cargarVisitas(): void {
    this.cargando = true; // Activamos el estado de carga
    
    this.visitaService.obtenerVisitas().subscribe({
      // Se ejecuta si la petición tiene éxito
      next: (data) => {
        this.visitas = data;    // Guardamos los datos recibidos
        this.cargando = false;  // Desactivamos el indicador de carga
        console.log("✅ Datos cargados correctamente:", data);
      },
      // Se ejecuta si hay un error (ej. servidor caído o error de permisos)
      error: (err) => {
        this.cargando = false;
        console.error("❌ Error al obtener visitas", err);
        
        // Manejo específico para error 401 (No autorizado)
        if (err.status === 401) {
          alert("Sesión caducada. Por favor, inicia sesión de nuevo.");
          this.router.navigate(['/login']); // Redirigimos al usuario a la pantalla de acceso
        }
      }
    });
  }

  /**
   * Método para limpiar la sesión del usuario.
   */
  cerrarSesion(): void {
    // Eliminamos el token de seguridad almacenado en el navegador
    localStorage.removeItem('token');
    
    // Navegamos de vuelta a la página de login
    this.router.navigate(['/login']);
  }
}