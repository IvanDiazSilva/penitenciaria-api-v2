import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { ListadoComponent } from './components/listado/listado.component';
import { AltaVisitaComponent } from './components/alta-visita/alta-visita.component';
import { PortalVisitanteComponent } from './components/portal-visitante/portal-visitante';
import { DashboardVisitanteComponent } from './components/dashboard-visitante/dashboard-visitante';

// Importamos tu nueva clase de Administración (Standalone)
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard'; 

export const routes: Routes = [
  // 1. Pantalla de acceso
  { path: 'login', component: LoginComponent },
  
  // 2. Ruta para la Administradora
  { path: 'admin', component: AdminDashboard }, 
  
  // 3. Tabla con todas las visitas registradas
  { path: 'listado', component: ListadoComponent },
  
  // 4. Formulario interno de registro de visitas
  { path: 'alta-visita', component: AltaVisitaComponent },
  
  // 5. Portal público para familiares
  { path: 'portal-visitante', component: PortalVisitanteComponent }, 

  // 6. DASHBOARD DEL VISITANTE (Estructura de contenedor)
  { 
    path: 'dashboard-visitante', 
    component: DashboardVisitanteComponent,
    children: [
      // Aquí es donde irán las páginas que se verán DENTRO del dashboard.
      // Por ahora puedes dejarlo vacío o apuntar a una página de inicio.
      // { path: 'inicio', component: InicioVisitanteComponent }
    ]
  },
  
  // 7. RUTA POR DEFECTO
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  // 8. RUTA COMODÍN
  { path: '**', redirectTo: '/login' }
];