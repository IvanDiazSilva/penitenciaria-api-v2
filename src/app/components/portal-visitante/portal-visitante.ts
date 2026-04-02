import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router'; // Importación vital

@Component({
  selector: 'app-portal-visitante',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './portal-visitante.html',
  styleUrls: ['./portal-visitante.css']
})
export class PortalVisitanteComponent {

  pasoActual: string = 'INICIO'; 
  subPasoRegistro: number = 1;

  datosVisitante = {
    nombre: '',
    dni: '',
    tipoDoc: 'DNI',
    fechaNac: '',
    nacionalidad: '',
    telefono: '',
    email: '',
    direccion: '',
    nombreInterno: '',
    parentesco: '',
    tipoVisita: 'ORDINARIA',
    aceptaTerminos: false
  };

  constructor(private router: Router) {}

  /**
   * ESTA FUNCIÓN ES LA QUE SOLUCIONA TU PROBLEMA:
   * En lugar de cambiar una variable interna, cambia la URL del navegador.
   */
  irAAltaVisita() {
    console.log("🚀 Navegando hacia el formulario de alta de visita...");
    this.router.navigate(['/alta-visita']);
  }

  siguienteSubPaso() {
    if (this.subPasoRegistro < 3) {
      this.subPasoRegistro++;
    } else {
      this.enviarSolicitud();
    }
  }

  anteriorSubPaso() {
    if (this.subPasoRegistro > 1) {
      this.subPasoRegistro--;
    } else {
      this.forzarVista('INICIO');
    }
  }

  forzarVista(vista: string) {
    this.pasoActual = vista;
    this.subPasoRegistro = 1;
  }

  enviarSolicitud() {
    this.forzarVista('ESPERA');
  }
}