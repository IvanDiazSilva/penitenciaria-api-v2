import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard-visitante',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-visitante.html',
  styleUrls: ['./dashboard-visitante.css']
})
export class DashboardVisitanteComponent implements OnInit {
  
  // Variables que el HTML necesita para no dar error
  nombreUsuario: string = 'María García'; 
  
  // Objeto de prueba para la tarjeta de cita
  // Si lo pones como null, verás el estado "Sin citas"
  proximaCita: any = {
    fecha: new Date('2026-05-20'),
    hora: '10:30',
    nombreInterno: 'Juan Pérez García',
    modulo: 'M-04'
  };

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Aquí podrías recuperar el nombre real del localStorage si ya te has logueado
    const user = localStorage.getItem('usuario');
    if (user) {
      this.nombreUsuario = user;
    }
  }

  // Función para el botón de Nueva Cita
  irAAltaCita() {
    this.router.navigate(['/alta-visita']);
  }

  // Función para ver el historial (puedes crear la ruta luego)
  verMisCitas() {
    console.log("Navegando al historial...");
  }

  // Función para cerrar sesión
  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}