import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private loginUrl = 'http://localhost:8080/penitenciaria-api/api/login';

  constructor(private http: HttpClient) { }

  login(datos: any): Observable<any> {
    // Especificamos el Content-Type para evitar el error 400 de Wildfly
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post(this.loginUrl, datos, { headers });
  }

  estaLogueado(): boolean {
    return !!localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
  }
}