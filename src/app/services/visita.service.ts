import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class VisitaService {

  private apiUrl = 'http://localhost:8080/penitenciaria-api/api/visitas';

  constructor(private http: HttpClient) { }

  /**
   * REGISTRAR VISITA (POST)
   * Aquí es donde corregimos el Error 400 de deserialización
   */
  registrarVisita(datos: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    // IMPORTANTE: Estos nombres deben coincidir con los atributos de la Clase Java (Entity)
    const body = {
      // 1. reo_id -> Java suele esperar el objeto 'reo' con su 'id'
      reo: { 
        id: Number(datos.idReo) 
      },
      
      // 2. visitante_nombre -> Java espera 'visitanteNombre'
      visitanteNombre: datos.visitanteNombre,
      
      // 3. visitante_dni -> Java espera 'visitanteDni'
      visitanteDni: datos.visitanteDni,
      
      // 4. fecha_visita -> Java espera 'fechaVisita'
      fechaVisita: datos.fechaVisita,
      
      // 5. hora_entrada y hora_salida (Postgres pide HH:mm:ss)
      horaEntrada: datos.horaEntrada.length === 5 ? datos.horaEntrada + ":00" : datos.horaEntrada,
      horaSalida: datos.horaSalida.length === 5 ? datos.horaSalida + ":00" : datos.horaSalida,
      
      // 6. autorizado (Boolean)
      autorizado: datos.autorizado,
      
      // 7. codigo_qr (Obligatorio en tu captura de pgAdmin)
      // Lo enviamos como string vacío o temporal si no tienes input
      codigoQr: "QR-PENDIENTE" 
    };

    console.log("📡 Cuerpo enviado (Revisa que no falte nada):", body);

    return this.http.post(this.apiUrl, body, { headers });
  }

  /**
   * LISTADO DE VISITAS (GET)
   */
  obtenerVisitas(): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any[]>(this.apiUrl, { headers });
  }
}