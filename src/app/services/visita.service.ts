import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class VisitaService {

  private urlVisitas = 'http://localhost:8080/penitenciaria-api/api/visitas';
  private urlVisitantes = 'http://localhost:8080/penitenciaria-api/api/visitantes';

  constructor(private http: HttpClient) { }

  /**
   * 1. REGISTRO DE VISITANTE (Portal-Visitante)
   * Ajustado al formato exacto de tu BBDD
   */
 registrarNuevoVisitante(datos: any): Observable<any> {
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders({ 
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  });

  // MAPEO EXACTO CON LA ENTIDAD JAVA (Visitante.java)
  const paquete = {
    nombreCompleto: datos.nombreCompleto,
    dniNie: datos.dniNie,
    nacionalidad: datos.nacionalidad,
    telefono: datos.telefono,
    email: datos.email,
    direccion: datos.direccion,
    nombreInterno: datos.nombreInterno,
    parentesco: datos.parentesco,
    aceptaNormativa: datos.acepta, // <--- ANTES ERA 'acepta', AQUÍ ESTABA EL ERROR 400
    estado: 'PENDIENTE'            // Coincide con el default de Java
  };

  console.log("🚀 Enviando a Java con nombres corregidos:", paquete);

  return this.http.post(this.urlVisitantes, paquete, { headers });
}

  /**
   * 2. ALTA DE CITA (Para el reo)
   */
  registrarVisita(body: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post(this.urlVisitas, body, { headers });
  }

  /**
   * 3. LISTADO DE VISITAS
   */
  obtenerVisitas(): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(this.urlVisitas, { headers });
  }

  consultarEstadoVisitante(dni: string) {
  // Ajusta esta URL según lo que Iván te diga (ej: /api/visitantes/estado/)
  const url = `http://localhost:8080/api/visitantes/estado/${dni}`;
  
  // Hacemos una petición GET para obtener el estado del DNI
  return this.http.get(url);
}
}