# Sistema de Gestión de Turnos Médicos

API REST para gestionar los turnos de un consultorio médico: agenda del médico (disponibilidad
semanal y bloqueos), turnos, atención clínica (consulta → diagnóstico → tratamiento) y datos
administrativos (pacientes, coberturas y financiadores).

**Integrantes:** Camila Frontera · Claudia Lara · Fátima Gauna · Jessica Soria

**Tecnologías:** Java 21, Spring Boot 3, Spring Data JPA, Spring Security con JWT, PostgreSQL y Maven.

---

## Estructura del repositorio

```
backend/   proyecto Spring Boot (las tablas se generan a partir de las clases de entity/)
db/        scripts SQL: creación de la base, datos de prueba y migración
docs/      colección de Postman con todos los endpoints y documentación
```

---

## Cómo levantar el proyecto

### 1. Instalar lo necesario

1. **Java 21**.
2. **PostgreSQL**: al instalarlo te pide una **contraseña para el usuario `postgres`**. Anotala, la
   vas a usar en el paso 3. Trae incluido **pgAdmin**, que es el programa que usamos para
   administrar la base.
3. **Git**.

No hace falta instalar Maven aparte: el proyecto trae `mvnw.cmd` (Windows) / `mvnw` (Linux y Mac),
que lo descarga solo la primera vez.

### 2. Clonar el repositorio

```powershell
git clone https://github.com/JessicaSoria13/gestion-turnos-database.git
cd gestion-turnos-database
```

### 3. Crear la base de datos vacía

1. Abrí **pgAdmin** y conectate a **Servers → PostgreSQL** con la contraseña de `postgres`.
2. Click derecho sobre **Databases** → **Query Tool**.
3. Copiá el contenido de [db/crear-base.sql](db/crear-base.sql), pegalo y ejecutalo (▶ o F5).
4. Click derecho sobre **Databases** → **Refresh**: tiene que aparecer la base `turnos_medicos`.

### 4. Configurar la conexión del backend

El archivo con las credenciales **no está en el repo**, se arma a partir de una plantilla:

1. En `backend/src/main/resources/` copiá `application.properties.example` y renombrá la copia a
   `application.properties`.
2. Abrilo y reemplazá estos valores:

```properties
spring.datasource.username=postgres
spring.datasource.password=TU_PASSWORD_DE_POSTGRES
jwt.secret=un-texto-largo-inventado-de-al-menos-32-caracteres
spring.mail.username=sin-configurar
spring.mail.password=sin-configurar
```

- `jwt.secret` tiene que tener **32 caracteres o más** (es la clave con la que se firman los tokens).
- Las dos líneas de `spring.mail` **tienen que tener algún valor** o el backend no arranca. Un
  email real (Gmail con contraseña de aplicación) solo hace falta para probar la recuperación de
  contraseña.
- El resto ya viene con valores que funcionan (puerto 8080, base `turnos_medicos`).

### 5. Levantar el backend (crea las tablas)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

La primera vez tarda porque descarga las dependencias. Cuando aparece
`Started BackendApplication in X seconds`, ya está corriendo en `http://localhost:8080` y Hibernate
creó todas las tablas en `turnos_medicos`. Dejá esa terminal abierta.

### 6. Cargar los datos de prueba

1. En pgAdmin seleccioná la base **`turnos_medicos`** → click derecho → **Query Tool**.
2. Copiá el contenido de [db/datos-prueba.sql](db/datos-prueba.sql), pegalo y ejecutalo.

> [db/migracion-2026-09.sql](db/migracion-2026-09.sql) **no hace falta** en una base nueva: es solo
> para bases que ya existían antes de septiembre.

### 7. Probar

| Usuario | DNI | Contraseña | Rol |
|---|---|---|---|
| Dra. Ana López | `30111222` | `123456` | ADMINISTRADOR |
| Juan Pérez | `32111222` | `123456` | PACIENTE |
| María Gómez | `33222111` | `123456` | PACIENTE |

1. `POST http://localhost:8080/api/v1/auth/login` con el body:

   ```json
   { "dni": "30111222", "contrasena": "123456" }
   ```

2. Copiá el `token` de la respuesta y mandalo en el resto de las peticiones con el header
   `Authorization: Bearer <token>`.
3. Por ejemplo, `GET http://localhost:8080/api/v1/financiadores` tiene que devolver **IOMA** y **OSDE**.

En [docs/Turnos Médicos/](docs/Turnos%20M%C3%A9dicos/) está la colección de Postman con todos los
endpoints organizados por recurso.

---

## Endpoints principales

Base: `/api/v1`. Todo requiere token salvo `/auth/**`.

| Recurso | Ruta | Quién lo usa |
|---|---|---|
| Autenticación | `/auth/register`, `/auth/login`, `/auth/recuperar-contrasena/...` | Público |
| Usuarios | `/usuarios` | Admin / el propio usuario (cambio de contraseña, baja) |
| Pacientes | `/pacientes`, `/pacientes/me` | Admin / paciente |
| Médicos | `/medicos`, `/medicos/{id}/horarios-disponibles?fecha=` | Todos (ABM solo admin) |
| Disponibilidades | `/disponibilidades` | Todos (ABM solo admin) |
| Bloqueos | `/bloqueos` | Admin |
| Financiadores | `/financiadores` | Todos (ABM solo admin) |
| Coberturas | `/coberturas` | Admin / paciente |
| Turnos | `/turnos`, `/turnos/mis-turnos`, `/turnos/{id}/cancelar` | Admin / paciente |
| Consultas médicas | `/consultas-medicas` | Admin |
| Diagnósticos | `/diagnosticos` | Admin |
| Tratamientos | `/tratamientos` | Admin |

Algunas reglas del sistema:

- Los `DELETE` son **bajas lógicas** (`activo = false`), no se borra nada de la base.
- Los turnos **no se eliminan**: se cancelan (`PUT /turnos/{id}/cancelar`). Si cancela el médico,
  el motivo es obligatorio.
- No se puede dar un turno fuera de la disponibilidad del médico ni en un horario ocupado.
- Al cargar un bloqueo (vacaciones, feriado) se cancelan automáticamente los turnos de esas fechas.
- Registrar la consulta médica pasa el turno a `ATENDIDO`.
