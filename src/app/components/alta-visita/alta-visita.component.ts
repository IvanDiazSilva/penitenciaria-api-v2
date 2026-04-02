import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { VisitaService } from '../../services/visita.service';
import { Router } from '@angular/router'; // <--- 1. IMPORTANTE: Para navegar tras guardar

@Component({
  selector: 'app-alta-visita',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './alta-visita.html',
  styleUrls: ['./alta-visita.css'] // <--- Asegúrate de que apunte a tu nuevo CSS "Dark"
})
export class AltaVisitaComponent {

  /**
   * Mantenemos el objeto, pero inicializamos idReo como null o vacío 
   * para que el placeholder "ID del interno" se vea en el input.
   */
  nuevaVisita = {
    visitanteNombre: '',
    visitanteDni: '',
    fechaVisita: new Date().toISOString().split('T')[0],
    idReo: null as any, // Cambiado de 1 a null para mejor UX
    autorizado: true
  };

  /**
   * Inyectamos el Router para que, tras el éxito, el usuario no se quede 
   * "atrapado" en el formulario y vuelva automáticamente al listado.
   */
  constructor(
    private visitaService: VisitaService,
    private router: Router // <--- Inyección del Router
  ) {}

  guardarVisita() {
    // Validación básica antes de enviar (opcional pero recomendada)
    if (!this.nuevaVisita.visitanteNombre || !this.nuevaVisita.idReo) {
      alert('⚠️ Por favor, rellena los campos obligatorios.');
      return;
    }

    this.visitaService.registrarVisita(this.nuevaVisita).subscribe({
      next: (res) => {
        console.log('✅ Visita registrada:', res);
        
        // En lugar de un alert simple, podrías usar un Toast (si tuvieras la librería)
        alert('¡Registro completado con éxito!');
        
        /**
         * 2. FLUJO DE NAVEGACIÓN:
         * Tras guardar, lo más lógico en un sistema de gestión es 
         * volver a la tabla para ver el nuevo registro al final.
         */
        this.router.navigate(['/listado']); 
      },
      error: (err) => {
        console.error('❌ Error en el servidor:', err);
        if (err.status === 401) {
          alert('Sesión caducada. Vuelve a entrar.');
          this.router.navigate(['/login']);
        } else {
          alert('No se pudo conectar con el servidor de la penitenciaría.');
        }
      }
    });
  }

  /**
   * Si decides no navegar y quedarte en la página, este método 
   * limpia el formulario para una nueva entrada.
   */
  private limpiarFormulario() {
    this.nuevaVisita = {
      visitanteNombre: '',
      visitanteDni: '',
      fechaVisita: new Date().toISOString().split('T')[0],
      idReo: null as any,
      autorizado: true
    };
  }
}