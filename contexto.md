# Contexto del proyecto — Sistema de Gestión de Turnos Médicos

> Este archivo describe el proyecto para que un asistente de IA (GitHub Copilot) genere código
> **consistente con la arquitectura, las convenciones y las reglas de negocio** definidas. Leelo como
> la fuente de verdad: si una sugerencia contradice algo de acá, gana este documento.

---

## 1. Qué es el proyecto

Backend REST + frontend web para la gestión de turnos de un consultorio médico: administración de
agenda, atención y seguimiento clínico (turnos, consultas, diagnósticos, tratamientos) y datos
administrativos (pacientes, coberturas). Diseñado para **un único médico** en su versión inicial,
con escalabilidad prevista a múltiples profesionales **sin reestructurar el modelo de datos**.
El sistema corre **solo en local** (sin despliegue).

## 2. Stack tecnológico (respetar exactamente)

**Backend**
- Java 21 (o 17), Maven, Spring Boot 3.x
- Spring Web (API REST), Spring Data JPA / Hibernate
- Spring Security + JWT (librería `io.jsonwebtoken:jjwt` 0.12.x)
- Jakarta Validation, Lombok
- PostgreSQL Driver

**Base de datos**
- PostgreSQL. **Las tablas las genera Hibernate** a partir de las entidades (`spring.jpa.hibernate.ddl-auto=update`). **No** escribir DDL de creación de tablas a mano.

**Frontend**
- React + Vite, React Router. Consume la API REST y envía el JWT en el header `Authorization: Bearer <token>`.

**Herramientas**: IntelliJ IDEA, pgAdmin, Postman, Git/GitHub.

## 3. Estructura del repositorio (monorepo)

```
turnos-medicos/
├── backend/     ← proyecto Spring Boot (las tablas se definen aquí, en las @Entity)
├── frontend/    ← proyecto React + Vite
├── db/          ← crear-base.sql (CREATE DATABASE) + datos-prueba.sql + schema.sql (export informativo)
├── docs/        ← acta, EDT, documentación, DER
├── .gitignore   ← ignora target/, node_modules/ y archivos con credenciales
└── README.md    ← cómo levantar backend, frontend y base (puesta en marcha)
```

## 4. Arquitectura del backend

Arquitectura en capas. Toda petición fluye:

```
Controller → Service → Repository → PostgreSQL
```

Paquetes: `entity`, `repository`, `service`, `controller`, `dto`, `config`.

**Reglas de la arquitectura (obligatorias):**
- Las **reglas de negocio van SIEMPRE en la capa `service`**. Nunca en el controlador ni en la base.
- Usar **inyección por constructor** (no `@Autowired` en campos).
- Los controladores solo reciben la petición, delegan al service y devuelven la respuesta.
- Exponer **DTOs** en requests/responses; no exponer entidades crudas. **Nunca** devolver el campo `contrasena`.
- Validar entradas con anotaciones de Jakarta Validation (`@NotNull`, `@Email`, etc.).
- Manejo de errores centralizado con `@RestControllerAdvice` que traduce excepciones a códigos HTTP.

## 5. Modelo de dominio — 11 entidades

Tablas: `usuario`, `paciente`, `medico`, `disponibilidad`, `bloqueo`, `turno`, `consulta_medica`,
`diagnostico`, `tratamiento`, `cobertura`, `financiador`.

| Entidad | Campos principales | Notas |
|---|---|---|
| **Usuario** | dni, contrasena (BCrypt), rol (enum), activo | Credenciales de acceso. |
| **Paciente** | nombreApellido, dni, email, celular, domicilio, fechaNacimiento, activo | 1–1 con Usuario, asociados por DNI. |
| **Medico** | matricula, especialidades, activo | 1–1 con Usuario. Puede tener varias especialidades. |
| **Disponibilidad** | idMedico, diaSemana (enum), horaInicio, horaFin, duracionTurnoMin, activo | Regla semanal recurrente. |
| **Bloqueo** | idMedico, fechaDesde, fechaHasta, motivo, activo | Feriados/vacaciones/ausencias. |
| **Turno** | idPaciente, idMedico, idCobertura (nullable), idDisponibilidad, fecha, hora, estado (enum), motivoConsulta, motivoCancelacion | Ver reglas especiales abajo. |
| **ConsultaMedica** | idTurno, observaciones | 1–1 con Turno. Fecha/hora se toman del turno. |
| **Diagnostico** | idConsulta, descripcion | |
| **Tratamiento** | idDiagnostico, descripcion, duracion, estado (enum) | |
| **Cobertura** | idPaciente, idFinanciador, numeroAfiliado, plan, activa | Un paciente puede tener 0..N. |
| **Financiador** | nombre, activo | Obra social o prepaga. |

**Enums:** `Rol { ADMINISTRADOR, PACIENTE }` · `EstadoTurno { CONFIRMADO, CANCELADO, ATENDIDO }` ·
`DiaSemana { LUNES..DOMINGO }` · `EstadoTratamiento { EN_CURSO, FINALIZADO, SUSPENDIDO }`.
(`MEDICO` está previsto como valor futuro del enum `Rol`, no se usa aún.)

**Relaciones (FK con `@ManyToOne` del lado que tiene la FK, según el DER):**
- Usuario 1–1 Paciente · Usuario 1–1 Medico
- Medico 1–N Disponibilidad · Medico 1–N Bloqueo · Medico 1–N Turno
- Paciente 1–N Turno · Paciente 1–N Cobertura
- Turno N–1 Cobertura (nullable → atención particular) · Turno 1–1 ConsultaMedica
- ConsultaMedica 1–N Diagnostico · Diagnostico 1–N Tratamiento
- Cobertura N–1 Financiador

## 6. Seguridad y roles

- Dos roles: **ADMINISTRADOR** (el médico y/o su secretaria: funciones clínicas + configuración) y **PACIENTE**.
- Autenticación con **JWT**: `POST /auth/login` valida DNI + contraseña y devuelve `{ token, rol }`. Cada petición posterior lleva el token; se autoriza según el rol.
- Contraseñas **siempre** con `BCryptPasswordEncoder`. Nunca en texto plano ni en respuestas.
- Restringir endpoints por rol con `@PreAuthorize("hasRole('ADMINISTRADOR')")` donde corresponda.
- Un mismo DNI puede existir como PACIENTE y/o ADMINISTRADOR; elige el rol al ingresar.

## 7. Convenciones de la API

- **Base URL:** `/api/v1`. Recursos en plural (`/pacientes`, `/turnos`, `/financiadores`, ...).
- `POST /api/v1/auth/register` → autorregistro de paciente: crea Usuario (rol PACIENTE) **y** Paciente en la misma operación; si el DNI es de un usuario dado de baja, se reactiva y re-vincula.
- `POST /api/v1/auth/login` → devuelve token JWT y rol.
- **`DELETE` = baja lógica** (`activo = false`) en todos los recursos. **Excepción: `Turno` NO expone `DELETE`**; su cancelación es un cambio de estado vía `PUT`.
- Códigos de error: 400 (datos inválidos), 401 (no autenticado), 403 (sin permisos), 404 (no existe), 409 (conflicto, p. ej. horario ocupado o DNI duplicado), 500 (error interno).

## 8. Reglas de negocio (implementar como validaciones en `service`)

1. **Baja lógica universal:** nada se elimina físicamente; todas las bajas hacen `activo = false`. Los listados usan `findByActivoTrue()`.
2. Un turno solo se crea si **paciente y médico existen y están activos**.
3. **No** se asigna un turno fuera de la disponibilidad del médico, ni en un horario ya ocupado (restricción de unicidad sobre médico + fecha + hora, también a nivel base de datos).
4. El turno se crea directamente en estado **CONFIRMADO**. Nunca se elimina; se cancela cambiando el estado.
5. Si la cancelación la hace el médico, el **motivo es obligatorio**.
6. Si `idCobertura` es `NULL`, la atención es **particular**.
7. Una **ConsultaMedica** solo se genera a partir de un turno **CONFIRMADO** existente (relación 1–1). Al registrarla, el turno pasa a **ATENDIDO**.
8. **Cadena clínica estricta:** no hay consulta sin turno, ni diagnóstico sin consulta, ni tratamiento sin diagnóstico.
9. **Bloqueo:** al crearlo, cancela automáticamente los turnos **CONFIRMADO** cuya fecha caiga en el rango, propagando el motivo del bloqueo como motivo de cancelación. Su baja es lógica y vuelve a ofrecer los horarios, **sin restaurar** los turnos cancelados.
10. El usuario puede modificar sus datos **excepto DNI y fechaNacimiento**.
11. El usuario puede cambiar/recuperar su contraseña y darse de baja (el Paciente asociado persiste como historial).

## 9. Endpoint calculado: horarios disponibles

`GET /api/v1/medicos/{id}/horarios-disponibles?fecha=YYYY-MM-DD` **no lee una tabla, calcula al vuelo**:
1. Si la fecha cae dentro de un **bloqueo activo** → devolver lista vacía.
2. Tomar la disponibilidad del médico para el **día de la semana** de esa fecha.
3. Generar los slots según `duracionTurnoMin` (09:00, 09:30, ...).
4. Descontar los slots con turno **CONFIRMADO** o **ATENDIDO** en esa fecha (los CANCELADO no descuentan).
5. Devolver la lista de horarios libres.

## 10. Patrones de código a seguir

- **CRUD estándar por entidad:** `@Entity` → `Repository extends JpaRepository` → `Service` (reglas) → `@RestController`.
- **Baja lógica:** campo `Boolean activo = true`; el método `DELETE` del service hace `setActivo(false)`; los listados filtran por activo.
- **Estados con enums**, no con Strings sueltos.
- **Excepciones de dominio** (p. ej. `RecursoNoEncontradoException`, `ConflictoException`) mapeadas a HTTP en el `@RestControllerAdvice`.
- **Orden de construcción sugerido** (respeta dependencias): Financiador → Usuario → Paciente → Medico → Disponibilidad → Bloqueo → Turno → ConsultaMedica → Diagnostico → Tratamiento → Cobertura. La **seguridad JWT se agrega al final**, cuando los CRUD ya funcionan.

## 11. Qué NO hacer (evitar siempre)

- ❌ **No** eliminar registros físicamente (`delete` real). Todo es baja lógica; el Turno ni siquiera expone DELETE.
- ❌ **No** escribir SQL de creación de tablas: las genera Hibernate desde las entidades.
- ❌ **No** poner lógica de negocio en los controladores.
- ❌ **No** devolver la entidad `Usuario` ni el campo `contrasena` en las respuestas.
- ❌ **No** hardcodear credenciales de la base ni el secreto del JWT en el código: usar `application.properties` (no commiteado) o variables de entorno.
- ❌ **No** commitear `target/`, `node_modules/` ni archivos con contraseñas.
- ❌ **No** usar `ddl-auto=create` con datos cargados (borra las tablas al arrancar); usar `update`.

## 12. Frontend (React) — contexto breve

- SPA con React + Vite y React Router. Consume los endpoints de `/api/v1`.
- Guarda el JWT tras el login y lo envía como `Authorization: Bearer <token>` en cada llamada.
- Renderiza según rol: vistas de administrador (agenda, disponibilidad, bloqueos, historial clínico, pacientes) y vistas de paciente (solicitar/ver turnos, sus datos, sus coberturas).
- Una pantalla (o conjunto) por módulo del backend, respetando el control de acceso por rol.

---

### Referencia rápida
- **Roles:** ADMINISTRADOR, PACIENTE (MEDICO reservado a futuro).
- **Base API:** `/api/v1` · **Auth:** JWT · **Bajas:** lógicas (`activo=false`) · **Turno:** sin DELETE.
- **Tablas:** las genera Hibernate. **Reglas:** en `service`. **Local:** sin despliegue.
