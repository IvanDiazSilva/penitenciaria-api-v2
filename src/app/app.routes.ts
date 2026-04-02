import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { ListadoComponent } from './components/listado/listado.component';
import { AltaVisitaComponent } from './components/alta-visita/alta-visita.component';
import { PortalVisitanteComponent } from './components/portal-visitante/portal-visitante';

// Importamos tu nueva clase de Administración (Standalone)
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard'; 

export const routes: Routes = [
  // 1. Pantalla de acceso (Solo deja una definición de login)
  { path: 'login', component: LoginComponent },
  
  // 2. Ruta para la Administradora (Tu panel de escritorio)
  { path: 'admin', component: AdminDashboard }, 
  
  // 3. Tabla con todas las visitas registradas
  { path: 'listado', component: ListadoComponent },
  
  // 4. Formulario interno de registro de visitas
  { path: 'alta-visita', component: AltaVisitaComponent },
  
  // 5. Portal público para familiares
  { path: 'portal-visitante', component: PortalVisitanteComponent }, 
  
  // 6. RUTA POR DEFECTO: Redirigir al login si la URL está vacía
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  // 7. RUTA COMODÍN (Opcional): Si escriben cualquier cosa mal, al login
  { path: '**', redirectTo: '/login' }
];