import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Decorador @Injectable:
 * Define que este servicio puede ser usado por cualquier componente (Listado o Alta).
 * 'providedIn: root' garantiza que solo exista una instancia del servicio en toda la app.
 */
@Injectable({
  providedIn: 'root'
})
export class VisitaService {

  /**
   * URL de la API: 
   * Es fundamental que el puerto (8080) y la ruta coincidan con el controlador de Java.
   */
  private apiUrl = 'http://localhost:8080/penitenciaria-api/api/visitas'; 

  /**
   * Inyectamos HttpClient para realizar peticiones GET (leer) y POST (escribir).
   */
  constructor(private http: HttpClient) { }

  /**
   * 1. OBTENER TODAS LAS VISITAS
   * Realiza una petición GET al servidor para traer el array de visitas.
   * Requiere el token de seguridad para demostrar que el usuario está logueado.
   */
  obtenerVisitas(): Observable<any[]> {
    // Recuperamos el token almacenado en el navegador tras el login
    const token = localStorage.getItem('token');
    
    // Configuramos la cabecera 'Authorization' siguiendo el estándar de seguridad JWT
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    // Hacemos la petición y le decimos a TypeScript que esperamos un array de objetos (any[])
    return this.http.get<any[]>(this.apiUrl, { headers });
  }

  /**
   * 2. REGISTRAR UNA NUEVA VISITA
   * @param datosForm: Los datos que vienen del formulario del componente.
   */
  registrarVisita(datosForm: any): Observable<any> {
    const token = localStorage.getItem('token');
    
    // Definimos las cabeceras: Autorización + Tipo de contenido (JSON)
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    /**
     * MAPEADO DE DATOS (CRÍTICO):
     * Aquí transformamos el objeto plano del formulario al formato de objeto que espera Java.
     * En Java, una Visita tiene un objeto 'Reo' dentro, no solo un ID.
     */
    const body = {
      visitanteNombre: datosForm.visitanteNombre, 
      visitanteDni: datosForm.visitanteDni,
      fechaVisita: datosForm.fechaVisita, // Formato esperado: YYYY-MM-DD
      autorizado: true,
      // Construimos el sub-objeto Reo solo con el ID que capturamos del input
      reo: { 
        id: Number(datosForm.idReo) 
      }
    };

    // Imprimimos en consola para depurar antes de enviar
    console.log("🚀 Enviando visita al servidor...", body);

    /**
     * Enviamos la petición POST:
     * 1. La URL de la API.
     * 2. El cuerpo (body) con los datos mapeados.
     * 3. Las cabeceras con el token.
     */
    return this.http.post(this.apiUrl, body, { headers });
  }
}