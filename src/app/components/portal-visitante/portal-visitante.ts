import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router'; // Importamos RouterModule para el link de éxito
import { VisitaService } from '../../services/visita.service';

@Component({
  selector: 'app-portal-visitante',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule], // Añadido RouterModule
  templateUrl: './portal-visitante.html',
  styleUrls: ['./portal-visitante.css']
})
export class PortalVisitanteComponent {
  // --- Control de Vistas ---
  pasoActual: string = 'INICIO'; // INICIO, REGISTRO, CONSULTA, ESPERA
  subPaso: number = 1;           // 1, 2, 3 (dentro de REGISTRO)

  // --- Datos para Nueva Solicitud (Estefanía) ---
  datos = {
    nombreCompleto: '', 
    dniNie: '', 
    nacionalidad: '',
    telefono: '', 
    email: '', 
    direccion: '',
    nombreInterno: '', 
    parentesco: '', 
    acepta: false, 
    estado: 'PENDIENTE'
  };

  // --- Datos para Consulta de Estado ---
  dniBusqueda: string = '';
  resultadoEstado: string | null = null;

  constructor(private router: Router, private visitaService: VisitaService) {}

  // Navegación entre secciones principales
  forzarVista(vista: string) {
    this.pasoActual = vista;
    this.subPaso = 1;
    this.resultadoEstado = null; // Limpiamos búsquedas anteriores
    this.dniBusqueda = '';
  }

  // Lógica del Stepper (Registro)
  siguiente() {
    if (this.subPaso < 3) {
      this.subPaso++;
    } else {
      this.enviarSolicitud();
    }
  }

  atras() {
    if (this.subPaso > 1) {
      this.subPaso--;
    } else {
      this.forzarVista('INICIO');
    }
  }

  // --- ACCIÓN: Enviar Nuevo Registro ---
  enviarSolicitud() {
    if (!this.datos.acepta) {
      alert("Debe aceptar la normativa de seguridad.");
      return;
    }

    this.visitaService.registrarNuevoVisitante(this.datos).subscribe({
      next: () => {
        console.log("Registro enviado con éxito");
        this.forzarVista('ESPERA');
      },
      error: (err) => {
        console.error("Error al registrar", err);
        alert("No se pudo enviar la solicitud. Inténtelo más tarde.");
      }
    });
  }

  // --- ACCIÓN: Consultar Estado por DNI ---
  verificarEstado() {
    if (!this.dniBusqueda.trim()) {
      alert("Por favor, introduzca un DNI válido.");
      return;
    }

    // Llamada al servicio de Iván
    this.visitaService.consultarEstadoVisitante(this.dniBusqueda).subscribe({
      next: (res: any) => {
        // Asignamos el estado recibido (ej: 'PENDIENTE', 'APROBADO', 'RECHAZADO')
        this.resultadoEstado = res.estado;
      },
      error: (err) => {
        console.error("Error en la consulta", err);
        this.resultadoEstado = "NO ENCONTRADO";
        alert("No se encontró ninguna solicitud con ese DNI.");
      }
    });
  }
}