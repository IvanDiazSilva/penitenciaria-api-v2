import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Decorador @Component: Define la configuración del componente principal de la app.
 */
@Component({
  selector: 'app-root',      // Nombre de la etiqueta raíz que se encuentra en el index.html
  standalone: true,           // Indica que este componente es autónomo y gestiona sus propias dependencias
  imports: [RouterOutlet],    // IMPORTANTE: Permite que Angular use el sistema de rutas para intercambiar vistas
  templateUrl: './app.html',  // Ruta al archivo HTML que sirve de "marco" para la aplicación
  styleUrl: './app.css'       // Estilos globales específicos para este componente raíz
})
export class AppComponent {
  /**
   * Título de la aplicación. 
   * Puede usarse en el HTML mediante interpolación {{ title }}.
   */
  title = 'sistema-penal';
}
