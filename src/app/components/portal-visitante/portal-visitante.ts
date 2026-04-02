import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // Necesario para directivas como *ngIf, *ngSwitch y [ngClass]
import { FormsModule } from '@angular/forms';   // Necesario para capturar datos con [(ngModel)]

@Component({
  selector: 'app-portal-visitante',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './portal-visitante.html',
  styleUrls: ['./portal-visitante.css']
})
export class PortalVisitanteComponent {

  /** * pasoActual: Controla la vista principal (Pantalla de Inicio, Formulario o Espera).
   * Valores posibles: 'INICIO', 'REGISTRO', 'ESPERA'.
   */
  pasoActual: string = 'INICIO'; 

  /** * subPasoRegistro: Controla en qué sección del formulario está el usuario.
   * Útil para dividir un formulario largo en partes más pequeñas (Wizard).
   */
  subPasoRegistro: number = 1;

  /**
   * datosVisitante: Objeto que agrupa toda la información que el usuario rellena.
   * Se inicializa con valores por defecto para evitar errores de 'undefined' en el HTML.
   */
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

  /**
   * Lógica para avanzar en el formulario.
   * Si no ha llegado al final (paso 3), avanza; si ya terminó, envía la solicitud.
   */
  siguienteSubPaso() {
    if (this.subPasoRegistro < 3) {
      this.subPasoRegistro++;
    } else {
      this.enviarSolicitud();
    }
  }

  /**
   * Lógica para retroceder.
   * Si está en el primer paso del formulario, vuelve a la pantalla de 'INICIO'.
   */
  anteriorSubPaso() {
    if (this.subPasoRegistro > 1) {
      this.subPasoRegistro--;
    } else {
      this.forzarVista('INICIO');
    }
  }

  /**
   * Cambia la vista principal del portal.
   * Resetea el contador de subpasos al cambiar de pantalla.
   * @param vista El nombre de la vista a la que queremos ir.
   */
  forzarVista(vista: string) {
    this.pasoActual = vista;
    this.subPasoRegistro = 1;
  }

  /**
   * Simulación de envío de datos.
   * Aquí iría la llamada al servicio HTTP. Tras "enviar",
   * movemos al usuario a una pantalla de espera o confirmación.
   */
  enviarSolicitud() {
    // Aquí podrías añadir: this.miServicio.post(this.datosVisitante).subscribe(...)
    this.forzarVista('ESPERA');
  }
}