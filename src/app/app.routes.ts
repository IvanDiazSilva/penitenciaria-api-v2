import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { ListadoComponent } from './components/listado/listado.component';

/**
 * 1. IMPORTACIONES DE COMPONENTES
 * Traemos las clases de los componentes para que el Router sepa qué mostrar.
 * Es vital que la ruta del archivo ('./components/...') sea exacta.
 */
import { AltaVisitaComponent } from './components/alta-visita/alta-visita.component';
import { PortalVisitanteComponent } from './components/portal-visitante/portal-visitante';

/**
 * Constante 'routes': Array de objetos que define la tabla de rutas.
 * Cada objeto tiene un 'path' (lo que pones en la URL) y un 'component' (lo que se dibuja).
 */
export const routes: Routes = [
  // Ruta para la pantalla de acceso del personal
  { path: 'login', component: LoginComponent },
  
  // Ruta para ver la tabla con todas las visitas registradas
  { path: 'listado', component: ListadoComponent },
  
  // Ruta para el formulario interno de registro de visitas
  { path: 'alta-visita', component: AltaVisitaComponent },
  
  // Ruta para el portal público donde los familiares se pre-registran
  { path: 'portal-visitante', component: PortalVisitanteComponent }, 
  
  /**
   * RUTA POR DEFECTO:
   * Si el usuario entra a la raíz (http://localhost:4200/), 
   * lo redirigimos automáticamente a la pantalla de login.
   * 'pathMatch: full' asegura que la ruta esté totalmente vacía para redirigir.
   */
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];