import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router'; 
import { VisitaService } from '../../services/visita.service';

@Component({
  selector: 'app-alta-visita',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './alta-visita.html',
  styleUrls: ['./alta-visita.css']
})
export class AltaVisitaComponent {

  nuevaVisita = {
    visitanteNombre: '',
    visitanteDni: '',
    fechaVisita: '',
    idReo: null,
    autorizado: true,
    horaEntrada: '09:00',
    horaSalida: '10:00'
  };

  constructor(
    private visitaService: VisitaService,
    private router: Router
  ) {}

  guardarVisita() {
    this.visitaService.registrarVisita(this.nuevaVisita).subscribe({
      next: (res) => {
        alert('✅ Visita registrada con éxito.');
        this.limpiarFormulario();
      },
      error: (err) => {
        console.error("❌ Error 400:", err);
        alert('Error al registrar. Revisa los datos.');
      }
    });
  }

  limpiarFormulario() {
    this.nuevaVisita = {
      visitanteNombre: '',
      visitanteDni: '',
      fechaVisita: '',
      idReo: null,
      autorizado: true,
      horaEntrada: '09:00',
      horaSalida: '10:00'
    };
  }

  // Función para regresar al portal
  volverAlPortal() {
    this.router.navigate(['/portal-visitante']);
  }
}