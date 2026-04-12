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
    reoId: null,
    visitanteNombre: '',
    visitanteDni: '',
    fechaVisita: '',
    horaEntrada: '09:00',
    horaSalida: '10:00',
    autorizado: true,
  };

  constructor(
    private visitaService: VisitaService,
    private router: Router
  ) {}

 guardarVisita() {
  const cuerpoEnvio = {
    visitanteNombre: this.nuevaVisita.visitanteNombre,
    visitanteDni: this.nuevaVisita.visitanteDni,
    fechaVisita: this.nuevaVisita.fechaVisita,
    horaEntrada: this.nuevaVisita.horaEntrada + ':00', 
    horaSalida: this.nuevaVisita.horaSalida + ':00',
    autorizado: this.nuevaVisita.autorizado,
    reo: {
      // FORZAMOS QUE SEA NÚMERO AQUÍ MISMO
      id: Number(this.nuevaVisita.reoId) 
    }
  };

  this.visitaService.registrarVisita(cuerpoEnvio).subscribe({
    next: (res: any) => {
      alert('✅ ¡FUNCIONÓ!');
      this.limpiarFormulario();
    },
    error: (err: any) => {
      console.error("Detalle del error:", err.error);
      alert('Sigue dando 400. Mira la consola de Java.');
    }
  });
}

  limpiarFormulario() {
    this.nuevaVisita = {
      reoId: null,
      visitanteNombre: '',
      visitanteDni: '',
      fechaVisita: '',
      horaEntrada: '09:00',
      horaSalida: '10:00',
      autorizado: true
    };
  }

  volverAlPortal() {
    this.router.navigate(['/portal-visitante']);
  }
}