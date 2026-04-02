/**
 * LoginRequest: Define la estructura de los datos que ENVIAMOS al servidor.
 * Es crucial que los nombres coincidan con los atributos de la clase 'User' o 'LoginDTO' en Java.
 */
export interface LoginRequest {
  username: string; // Nombre de usuario (coincide con el campo del backend)
  password: string; // Contraseña del usuario
}

/**
 * LoginResponse: Define la estructura de lo que RECIBIMOS del servidor.
 * Mapea la respuesta JSON que envía el controlador de Spring Boot tras un login exitoso.
 */
export interface LoginResponse {
  mensaje: string; // Mensaje de éxito (ej: "Login correcto")
  token: string;   // El Token JWT (String largo) que usaremos en las cabeceras de otras peticiones
  rol: string;     // Rol del usuario (ej: 'ADMIN', 'GUARDA') para controlar la interfaz
}

/**
 * Visita: Define el modelo de datos de una visita penitenciaria.
 * Se utiliza tanto para el listado como para el objeto que se envía al crear una nueva.
 */
export interface Visita {
  visitanteNombre: string; // Nombre y apellidos del visitante
  visitanteDni: string;    // Documento de identidad (DNI/NIE)
  fechaVisita: string;     // Fecha en formato string (normalmente YYYY-MM-DD)
  idReo: number;           // ID único del interno en la base de datos
  autorizado: boolean;     // Estado de la autorización (true/false)
}