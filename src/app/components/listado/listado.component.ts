import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router'; // Añade RouterModule
import { VisitaService } from '../../services/visita.service';

@Component({
  selector: 'app-listado',
  standalone: true,
  imports: [CommonModule, RouterModule], // ¡Importante añadir RouterModule aquí!
  templateUrl: './listado.html',
  styleUrls: ['./listado.css']
})
export class ListadoComponent implements OnInit {
  visitas: any[] = [];
  cargando: boolean = false; // <--- Faltaba esto

  constructor(
    private visitaService: VisitaService,
    private router: Router // <--- Faltaba esto
  ) {}

  ngOnInit(): void {
    this.cargarVisitas();
  }

  cargarVisitas() {
    this.cargando = true; // Empezamos a cargar
    this.visitaService.obtenerVisitas().subscribe({
      next: (data) => {
        this.visitas = data;
        this.cargando = false; // Terminamos de cargar
      },
      error: (err: any) => {
        console.error("Error al cargar lista", err);
        this.cargando = false;
      }
    });
  }

  // <--- Faltaba este método que pide el botón del HTML
  cerrarSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.router.navigate(['/login']);
  }
}