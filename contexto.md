# Contexto del proyecto — Sistema de Gestión de Turnos Médicos (NEXA Salud)

> Este archivo describe el proyecto para que los asistentes de IA que usa el equipo (GitHub Copilot y
> Claude Code) generen código **consistente con la arquitectura, las convenciones y las reglas de
> negocio** definidas. Es la fuente de verdad del equipo: si una sugerencia contradice algo de acá,
> gana este documento. Si el código cambia, se actualiza este archivo en el mismo Pull Request.
>
> Última actualización: 03/11/2026.

---

## 1. Qué es el proyecto

Backend REST + frontend web para la gestión de turnos de un consultorio médico: administración de
agenda, atención y seguimiento clínico (turnos, consultas, diagnósticos, tratamientos) y datos
administrativos (pacientes, coberturas). Diseñado para **un único médico** en su versión inicial,
con escalabilidad prevista a múltiples profesionales **sin reestructurar el modelo de datos**.
El sistema corre **solo en local** (sin despliegue).

**Equipo:** Camila Frontera (directora del proyecto) · Jessica Soria · Claudia Lara · Fatima Gauna.

## 2. Stack tecnológico (respetar exactamente)

**Backend**
- Java 21, Maven (wrapper `mvnw` incluido), Spring Boot 3.3.4
- Spring Web (API REST), Spring Data JPA / Hibernate
- Spring Security + JWT (librería `io.jsonwebtoken:jjwt` 0.12.6)
- Jakarta Validation, Lombok
- PostgreSQL Driver

**Base de datos**
- PostgreSQL, base `turnos_medicos`. **Las tablas las genera Hibernate** a partir de las entidades
  (`spring.jpa.hibernate.ddl-auto=update`). **No** escribir DDL de creación de tablas a mano.

**Frontend**
- **Angular 22** (TypeScript, componentes standalone), por indicación del profe. Se basa en su
  proyecto base: https://github.com/fborzi/angular.
- Corre en `http://localhost:4200` con `npm start`. El proxy de desarrollo (`frontend/proxy.conf.mjs`)
  redirige `/api` al backend en el 8081 y quita el header `Origin`, así que no hace falta CORS para el 4200.
- Consume la API REST y envía el JWT en el header `Authorization: Bearer <token>`.
- No agregar librerías sin acordarlo con el equipo.

**Herramientas**: IntelliJ IDEA, VS Code, pgAdmin, Postman, Git/GitHub, Jira (proyecto GDT).

## 3. Estructura del repositorio (monorepo)

Repositorio: `gestion-turnos-database` (https://github.com/JessicaSoria13/gestion-turnos-database).

```
gestion-turnos-database/
├── backend/     ← proyecto Spring Boot (paquete base com.turnosmedicos.backend)
├── frontend/    ← proyecto Angular 22 (basado en el proyecto base del profe)
├── db/          ← crear-base.sql + datos-prueba.sql + migracion-2026-09.sql + schema.sql (informativo)
├── docs/        ← endpoints.md + colección de Postman (docs/Turnos Médicos/)
├── contexto.md  ← este archivo
├── .gitignore   ← ignora target/, node_modules/ y archivos con credenciales
└── README.md    ← cómo levantar el proyecto (puesta en marcha)
```

## 4. Arquitectura del backend

Arquitectura en capas. Toda petición fluye:

```
Controller → Service → Repository → PostgreSQL
```

Paquetes de `com.turnosmedicos.backend`: `entity`, `repository`, `service`, `controller`, `dto`,
`enums`, `exception`, `security`, `config`.

**Reglas de la arquitectura (obligatorias):**
- Las **reglas de negocio van SIEMPRE en la capa `service`**. Nunca en el controlador ni en la base.
- Usar **inyección por constructor** con campos `final` (no `@Autowired` en campos).
- Los controladores solo reciben la petición, delegan al service y devuelven la respuesta.
- Exponer **DTOs como `record`** en requests/responses; no exponer entidades crudas. **Nunca** devolver el campo `contrasena`.
- Validar entradas con Jakarta Validation (`@NotNull`, `@NotBlank`, `@Email`, etc.) y `@Valid` en el controller.
- Errores centralizados en `GlobalExceptionHandler` (`@RestControllerAdvice`). Reusar las excepciones existentes antes de crear nuevas:
  `RecursoNoEncontradoException` → 404 · `ConflictoException` → 409 · `DatosInvalidosException` → 400.
- El service no conoce HTTP: lanza excepciones de dominio, no devuelve `ResponseEntity`.
- **Módulo de referencia para copiar el patrón:** `Financiador` (entity → repository → service → controller → DTOs).

## 5. Modelo de dominio — 11 entidades

Tablas: `usuario`, `paciente`, `medico`, `medico_especialidad`, `disponibilidad`, `bloqueo`, `turno`,
`consulta_medica`, `diagnostico`, `tratamiento`, `cobertura`, `financiador`
(más `password_reset_token`, de soporte para la recuperación de contraseña).

| Entidad | Campos (como están en el código) | Notas |
|---|---|---|
| **Usuario** | dni, contrasena (BCrypt), rol (enum), activo | Credenciales de acceso. |
| **Paciente** | usuario, nombreApellido, dni, fechaNacimiento, email, celular, domicilio, activo | 1–1 con Usuario. |
| **Medico** | usuario, nombreApellido, dni, matricula, especialidades (lista), activo | 1–1 con Usuario. Varias especialidades en `medico_especialidad`. |
| **Disponibilidad** | medico, diaSemana (enum), horaInicio, horaFin, duracionTurnoMin, activo | Regla semanal recurrente. |
| **Bloqueo** | medico, fechaDesde, fechaHasta, motivo, activo | Feriados, vacaciones, ausencias. |
| **Turno** | paciente, medico, disponibilidad, cobertura (nullable), fecha, hora, estado (enum), motivoConsulta, motivoCancelacion | **Sin campo `activo`**: su ciclo de vida es el `estado`. |
| **ConsultaMedica** | turno, observaciones | 1–1 con Turno. **Todavía sin `activo`** (ver sección 13). |
| **Diagnostico** | consulta, descripcion | **Todavía sin `activo`** (ver sección 13). |
| **Tratamiento** | diagnostico, descripcion, duracion, estado (enum) | **Todavía sin `activo`** (ver sección 13). |
| **Cobertura** | paciente, financiador, numeroAfiliado, plan, activo | Un paciente puede tener 0..N. |
| **Financiador** | nombre, cuit, activo | Obra social o prepaga. |

**Enums:** `Rol { ADMINISTRADOR, PACIENTE }` · `EstadoTurno { CONFIRMADO, CANCELADO, ATENDIDO }` ·
`DiaSemana { LUNES..DOMINGO }` · `EstadoTratamiento { EN_CURSO, FINALIZADO, SUSPENDIDO }`.
(`MEDICO` está reservado como valor futuro del enum `Rol`; no se usa.)

**Relaciones (FK con `@ManyToOne` del lado que tiene la FK):**
- Usuario 1–1 Paciente · Usuario 1–1 Medico
- Medico 1–N Disponibilidad · Medico 1–N Bloqueo · Medico 1–N Turno
- Paciente 1–N Turno · Paciente 1–N Cobertura
- Turno N–1 Cobertura (nullable → atención particular) · Turno 1–1 ConsultaMedica
- ConsultaMedica 1–N Diagnostico · Diagnostico 1–N Tratamiento
- Cobertura N–1 Financiador

**Unicidad:** `(dni, rol)` en usuario · `financiador.nombre` · `(medico, fecha, hora)` en turno, solo entre los no CANCELADO ·
`(paciente, financiador, numeroAfiliado)` en cobertura.

## 6. Seguridad y roles (implementada)

- Dos roles: **ADMINISTRADOR** (el médico y/o su secretaria) y **PACIENTE**.
- JWT ya implementado (`security/JwtAuthFilter`, `JwtService`). `POST /api/v1/auth/login` valida DNI + contraseña y devuelve `{ token, rol }`.
- `/api/v1/auth/**` es público; todo lo demás requiere token.
- Restringir por rol con `@PreAuthorize("hasRole('ADMINISTRADOR')")` o `hasRole('PACIENTE')` en el controller.
- Contraseñas **siempre** con `BCryptPasswordEncoder`. Nunca en texto plano ni en respuestas.
- Un mismo DNI puede existir como PACIENTE y ADMINISTRADOR; elige el rol al ingresar.

## 7. Convenciones de la API

- **Base URL:** `/api/v1`. Recursos en plural (`/pacientes`, `/turnos`, `/financiadores`, ...).
- **`DELETE` = baja lógica** (`activo = false`). **Excepción: `Turno` NO expone `DELETE`**; se cancela con `PUT /turnos/{id}/cancelar`.
- Códigos: 400 datos inválidos · 401 no autenticado · 403 sin permisos · 404 no existe o inactivo · 409 conflicto (horario ocupado, DNI o nombre duplicado) · 500 error interno.
- Todo endpoint nuevo se documenta en `docs/endpoints.md` (descripción, request, response, tabla de errores) y se agrega a la colección de Postman.

**Endpoints implementados (03/11/2026):**

| Recurso | Endpoints |
|---|---|
| Auth | `POST /auth/register` · `POST /auth/login` · `POST /auth/recuperar-contrasena/solicitar` · `POST /auth/recuperar-contrasena/confirmar` |
| Usuarios | `GET /usuarios` · `GET /usuarios/{id}` · `POST /usuarios` · `PUT /usuarios/{id}/contrasena` · `DELETE /usuarios/{id}` |
| Pacientes | `GET /pacientes` · `GET /pacientes/me` · `GET /pacientes/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Médicos | `GET /medicos` · `GET /medicos/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` · `GET /medicos/{id}/horarios-disponibles?fecha=YYYY-MM-DD` |
| Disponibilidades | `GET` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Bloqueos | `GET` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Turnos | `GET /turnos` (admin) · `GET /turnos/mis-turnos` (paciente) · `GET /turnos/{id}` · `POST` · `PUT /turnos/{id}/cancelar` |
| Consultas médicas | `GET` · `GET /{id}` · `POST` |
| Diagnósticos | `GET` · `GET /{id}` · `GET /diagnosticos/consulta/{consultaId}` · `POST` · `PUT /{id}` |
| Tratamientos | `GET` · `GET /{id}` · `POST` · `PUT /{id}` |
| Coberturas | `GET` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| Financiadores | `GET` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |

## 8. Reglas de negocio (validaciones en `service`)

1. **Baja lógica universal:** nada se elimina físicamente; las bajas hacen `activo = false` y los listados usan `findByActivoTrue()`.
2. **El turno nunca se elimina:** no tiene baja lógica ni DELETE; su ciclo de vida es el estado.
3. Un turno solo se crea si **paciente y médico existen y están activos**.
4. **No** se asigna un turno fuera de la disponibilidad del médico para ese día, dentro de un bloqueo activo, ni en un horario ocupado.
5. El turno se crea en **CONFIRMADO**. Al cancelarlo pasa a **CANCELADO** y el horario vuelve a quedar libre.
6. Si la cancelación la hace el médico, el **motivo es obligatorio**.
7. Si `cobertura` es `null`, la atención es **particular**.
8. Una **ConsultaMedica** solo se genera desde un turno **CONFIRMADO** (1–1). Al registrarla, el turno pasa a **ATENDIDO**.
9. **Cadena clínica estricta:** no hay consulta sin turno, ni diagnóstico sin consulta, ni tratamiento sin diagnóstico.
10. **Bloqueo:** al crearlo cancela los turnos CONFIRMADO del rango con el motivo del bloqueo. Su baja vuelve a ofrecer los horarios, **sin restaurar** los turnos cancelados.
11. Los usuarios deben autenticarse y cada endpoint se restringe por rol.
12. El usuario puede modificar sus datos **excepto DNI y fechaNacimiento**.
13. Si un usuario se da de baja, su Paciente se conserva como historial. Si vuelve a registrarse con el mismo DNI, se reactiva.
14. Un paciente puede tener cero, una o varias coberturas.
15. Unicidad según la sección 5.

## 9. Endpoint calculado: horarios disponibles

`GET /api/v1/medicos/{id}/horarios-disponibles?fecha=YYYY-MM-DD` **no lee una tabla, calcula al vuelo**:
1. Si la fecha cae dentro de un **bloqueo activo** → lista vacía.
2. Toma la disponibilidad del médico para el **día de la semana** de esa fecha.
3. Genera los horarios según `duracionTurnoMin` (09:00, 09:30, ...).
4. Descuenta los que tienen turno **CONFIRMADO** o **ATENDIDO** (los CANCELADO no descuentan).
5. Devuelve los horarios libres.

## 10. Frontend (Angular)

Estructura del proyecto base del profe (`core/` + `features/`). Detalle y comandos en `frontend/README.md`.

**Estructura de carpetas (`frontend/src/app/`):**

```
app/
├── core/
│   ├── guards/        ← auth.guard.ts: authGuard y rolGuard('PACIENTE' | 'ADMINISTRADOR')
│   ├── interceptors/  ← jwt.interceptor.ts: ÚNICO lugar que agrega el token y maneja el 401
│   ├── models/        ← interfaces de los DTOs del backend
│   ├── services/      ← un servicio por recurso (auth.service.ts, turno.service.ts, ...)
│   └── utils/         ← errores.ts: mensajeDeError() para mostrar el ErrorResponse
├── features/
│   ├── auth/          ← login, registro, recuperar contraseña
│   ├── turnos/        ← reservar, mis turnos (paciente)
│   ├── agenda/        ← disponibilidad, bloqueos, horarios (admin)
│   ├── historial/     ← consulta → diagnóstico → tratamiento (admin)
│   └── pacientes/     ← pacientes y coberturas (admin)
├── app.ts / app.html  ← encabezado con menú por rol y Cerrar sesión
└── app.routes.ts      ← rutas (cada pantalla reemplaza solo su línea de EnConstruccionComponent)
```

**Reglas del frontend:**
- Ningún componente usa `HttpClient` directo: siempre a través de un servicio de `core/services/`.
  El token lo agrega el interceptor; no se arman headers a mano.
- El token y el rol se guardan al hacer login y se borran al cerrar sesión. Si el backend responde 401
  a un pedido con token, se vuelve a `/login`.
- Rutas `/paciente/...` solo para PACIENTE y `/admin/...` solo para ADMINISTRADOR.
- Los errores del backend (`ErrorResponse`) se muestran en pantalla con un mensaje claro, no solo en la consola.
- Estética NEXA Salud: tonos azules, tipografía limpia.

## 11. Forma de trabajo del equipo

- **Una rama por tarea**, creada desde `main`, con la clave de Jira: `feature/GDT-98-descripcion-corta`.
- Commits chicos con la clave de Jira al principio: `GDT-98 Agrega finder de coberturas por paciente`.
- Todo entra a `main` por **Pull Request** revisado por la directora del proyecto.
- **Nadie modifica el código de otra integrante:** los cambios se piden como comentario en el PR y los hace la autora.
- Cada integrante commitea con su propio usuario y con el mail de su cuenta de GitHub.
- **Endpoint de punta a punta** = la misma persona hace repository, service, DTO, controller, permiso por rol, prueba en Postman, entrada en `docs/endpoints.md` y la pantalla que lo usa.

## 12. Qué NO hacer (evitar siempre)

- ❌ Eliminar registros físicamente (`deleteById`). Todo es baja lógica; el Turno ni siquiera expone DELETE.
- ❌ Escribir SQL de creación de tablas: las genera Hibernate desde las entidades.
- ❌ Poner lógica de negocio en los controladores o en los componentes de Angular.
- ❌ Devolver la entidad `Usuario` o el campo `contrasena` en las respuestas.
- ❌ Hardcodear credenciales o el secreto del JWT: van en `application.properties` (no commiteado) o en variables de entorno.
- ❌ Commitear `target/`, `node_modules/`, `application.properties` ni archivos con contraseñas.
- ❌ Usar `ddl-auto=create` con datos cargados (borra las tablas); siempre `update`.
- ❌ Introducir librerías, patrones o arquitecturas no acordadas (MapStruct, NgRx, Angular Material, Docker, microservicios).
- ❌ Trabajar directo sobre `main` o modificar código de otra integrante.

## 13. Pendientes técnicos

El reparto de tareas está en Jira (proyecto GDT). Acá solo lo que el código necesita saber.

**Endpoints que faltan respecto del análisis:**
- `GET /pacientes/{id}/coberturas`: coberturas activas de un paciente.
- `GET /pacientes/{id}/turnos`: historial de turnos de un paciente, del más nuevo al más viejo.
- `DELETE /diagnosticos/{id}` y `DELETE /tratamientos/{id}`: baja lógica. **Requiere agregar
  `private Boolean activo = true;` a `Diagnostico` y `Tratamiento`** (Hibernate crea la columna al
  arrancar) y filtrar sus listados con `findByActivoTrue()`.
- Opcionales: `PUT /turnos/{id}` (reprogramar, revalidando disponibilidad) y `PUT /usuarios/{id}`
  (actualizar datos, regla 12).

**Frontend:** proyecto base en Angular listo (login, guard por rol, menú, interceptor JWT). Las
pantallas que faltan usan `EnConstruccionComponent` en `app.routes.ts` hasta que cada integrante
haga la suya.

---

### Referencia rápida
- **Roles:** ADMINISTRADOR, PACIENTE (MEDICO reservado a futuro).
- **Base API:** `/api/v1` · **Auth:** JWT · **Bajas:** lógicas (`activo=false`) · **Turno:** sin DELETE.
- **Tablas:** las genera Hibernate · **Reglas:** en `service` · **Frontend:** Angular en `localhost:4200` · **Local:** sin despliegue.
