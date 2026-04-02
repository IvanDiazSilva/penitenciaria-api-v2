import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { VisitaService } from '../../services/visita.service';

@Component({
  selector: 'app-listado',
  standalone: true,
  // IMPORTANTE: CommonModule permite usar *ngFor y *ngIf en el HTML
  imports: [CommonModule, RouterModule], 
  // REVISA: Si tu archivo se llama listado.html, déjalo así. 
  // Si se llama listado.component.html, cámbialo aquí abajo:
  templateUrl: './listado.html', 
  styleUrls: ['./listado.css']
})
export class ListadoComponent implements OnInit {
  
  visitas: any[] = [];
  cargando: boolean = true;
  miRol: string | null = '';

  constructor(
    private visitaService: VisitaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Recuperamos el rol del localStorage
    this.miRol = localStorage.getItem('role');

    // Seguridad: Redirigir si es Visitante
    if (this.miRol === 'Visitante') {
      this.router.navigate(['/alta-visita']);
      return;
    }

    this.cargarVisitas();
  }

  cargarVisitas(): void {
    this.cargando = true;
    
    this.visitaService.obtenerVisitas().subscribe({
      next: (data) => {
        this.visitas = data;
        this.cargando = false;
        console.log("✅ Visitas cargadas:", data);
      },
      error: (err) => {
        this.cargando = false;
        console.error("❌ Error al obtener visitas", err);
        
        if (err.status === 401 || err.status === 403) {
          this.cerrarSesion();
        }
      }
    });
  }

  cerrarSesion(): void {
    localStorage.clear(); // Borra token y rol de un golpe
    this.router.navigate(['/login']);
  }
}