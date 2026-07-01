# Sistema de Gestión Penitenciaria — API Backend

API REST desarrollada como proyecto de fin de ciclo (DAM), para la gestión de un centro penitenciario: reclusos, visitas, visitantes, incidentes y control de accesos mediante código QR. Sirve de backend para una aplicación frontend (Angular) consumida por distintos roles: administración, guardias y visitantes.

## Tecnologías

- **Java 17** + **Jakarta EE 10**
- **WildFly** (servidor de aplicaciones)
- **PostgreSQL** + **Hibernate / EclipseLink (JPA)**
- **JWT** (`io.jsonwebtoken`) para autenticación y autorización
- **BCrypt** (`jbcrypt`) para hash de contraseñas
- **OpenPDF** para generación de informes en PDF
- **Maven** como gestor de dependencias

## Arquitectura

La API expone sus recursos bajo el path base `/api` y sigue una organización por capas:

```
src/main/java/
├── api/            # Configuración JAX-RS (ApplicationConfig, JsonConfig)
├── cors/           # Filtro CORS
├── dto/            # Objetos de transferencia (LoginRequest, CrearVisitaRequest, PreregistroVisitanteRequest)
├── jwt/            # Generación y validación de JWT, filtro de autenticación
├── model/          # Entidades JPA (Reo, Visita, Visitante, Usuario, Incidente)
└── resources/      # Endpoints REST (uno por entidad + login + monitor)
```

## Seguridad y roles

La autenticación se realiza mediante **JWT**: el endpoint `/api/login` valida usuario y contraseña (hash con BCrypt) y devuelve un token con el rol del usuario embebido como claim.

Un filtro (`JwtFilter`) intercepta cada petición, valida el token y añade el `username` y el `rol` al contexto de seguridad. A partir de ahí, cada recurso comprueba el rol antes de permitir la acción.

Roles del sistema:

- **ADMIN** — gestión completa de reclusos, visitantes, autorización de visitas y consulta de incidentes.
- **GUARDIA** — gestión de incidentes, validación de accesos por QR, consulta del panel de monitorización.
- **VISITANTE** — preregistro, solicitud de visitas, consulta de sus propias citas y generación de su código QR.

Rutas públicas (sin token): login, preregistro de visitantes y consulta de estado de solicitud por DNI.

## Endpoints principales

| Recurso | Método | Ruta | Descripción | Rol requerido |
|---|---|---|---|---|
| Login | POST | `/api/login` | Autenticación y generación de JWT | Público |
| Reclusos | GET | `/api/reos` | Listado de reclusos | Autenticado |
| Reclusos | GET | `/api/reos/{id}` | Detalle de un recluso | Autenticado |
| Reclusos | POST | `/api/reos` | Alta de recluso | ADMIN |
| Reclusos | PUT | `/api/reos/{id}` | Actualizar recluso | ADMIN |
| Reclusos | DELETE | `/api/reos/{id}` | Eliminar recluso | ADMIN |
| Visitantes | POST | `/api/visitantes/preregistro` | Preregistro de visitante | Público |
| Visitantes | GET | `/api/visitantes/estado/{dni}` | Consultar estado de solicitud | Público |
| Visitantes | GET | `/api/visitantes` | Listado de visitantes (filtrable por estado) | ADMIN / GUARDIA |
| Visitantes | PUT | `/api/visitantes/{id}/estado` | Aprobar/denegar solicitud | ADMIN |
| Visitas | GET | `/api/visitas/mis-citas` | Citas del visitante autenticado | VISITANTE |
| Visitas | GET | `/api/visitas` | Listado completo de visitas | ADMIN / GUARDIA |
| Visitas | POST | `/api/visitas` | Solicitar una visita | VISITANTE |
| Visitas | PATCH | `/api/visitas/{id}/autorizar` | Autorizar una visita | ADMIN |
| Visitas | POST | `/api/visitas/qr/{id}` | Generar código QR de una visita autorizada | VISITANTE |
| Visitas | POST | `/api/visitas/validar-qr` | Validar QR en el control de acceso | GUARDIA / ADMIN |
| Incidentes | GET / POST / PUT / DELETE | `/api/incidentes` | Gestión de incidentes | ADMIN / GUARDIA |
| Monitor | GET | `/api/monitor` | Estadísticas generales del sistema | ADMIN / GUARDIA |
| Monitor | GET | `/api/monitor/visitas-hoy` | Visitas del día | ADMIN / GUARDIA |
| Monitor | GET | `/api/monitor/informe` | Informe filtrable por recluso/fechas | ADMIN / GUARDIA |
| Monitor | GET | `/api/monitor/informe/pdf` | Descarga del informe en PDF | ADMIN / GUARDIA |

## Modelo de datos (resumen)

- **Usuario**: credenciales y rol (ADMIN, GUARDIA, VISITANTE).
- **Reo**: nombre, DNI, delito.
- **Visitante**: datos personales, estado de la solicitud (PENDIENTE / APROBADO / DENEGADO), vinculado a un Usuario.
- **Visita**: recluso, visitante, fecha/hora, estado de autorización, código QR y validación de entrada.
- **Incidente**: tipo, descripción, guardia responsable, recluso relacionado (opcional).

## Cómo desplegar en local

1. Clonar el repositorio.
2. Tener una instancia de **PostgreSQL** accesible y crear la base de datos del proyecto.
3. Configurar el datasource `PenitenciariaDS` en WildFly (coincide con lo definido en `persistence.xml`).
4. Compilar el proyecto con Maven:
   ```bash
   mvn clean package
   ```
5. Desplegar el `.war` generado en WildFly.
6. Probar los endpoints con Postman, empezando por `POST /api/login`.

## Autor

Iván Díaz Silva
