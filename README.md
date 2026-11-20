# Sistema de Gestión de Turnos Médicos

API REST para gestionar los turnos de un consultorio médico: agenda del médico (disponibilidad
semanal y bloqueos), turnos, atención clínica (consulta → diagnóstico → tratamiento) y datos
administrativos (pacientes, coberturas y financiadores).

**Integrantes:** Camila Frontera · Claudia Lara · Fátima Gauna · Jessica Soria

**Tecnologías:** Java 21, Spring Boot 3, Spring Data JPA, Spring Security con JWT, PostgreSQL y Maven.

**Frontend:** está en otro repositorio, en Angular 22:
https://github.com/JessicaSoria13/gestion-turnos-frontend (cómo levantarlo, en su README).

---

## Estructura del repositorio

```
backend/     proyecto Spring Boot (las tablas se generan a partir de las clases de entity/)
db/          scripts SQL: creación de la base, datos de prueba y migración
docs/        colección de Postman con todos los endpoints y documentación
contexto.md  reglas del proyecto (arquitectura, seguridad, reglas de negocio, forma de trabajo)
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

`application.properties` **no está en el repo** (está en el `.gitignore`). Se arma a partir de una
plantilla que lee las credenciales de **variables de entorno**, así nadie escribe contraseñas en un
archivo.

1. En `backend/src/main/resources/` copiá `application.properties.example` y renombrá la copia a
   `application.properties`. **No le cambies nada**: los valores salen de las variables de entorno.
2. Definí estas cinco variables de entorno. Las cinco son obligatorias: si falta alguna, el backend
   no arranca.

| Variable | Qué va |
|---|---|
| `DB_USERNAME` | Usuario de PostgreSQL: `postgres`. |
| `DB_PASSWORD` | La contraseña de `postgres` que elegiste al instalar PostgreSQL (paso 1). |
| `JWT_SECRET` | Un texto inventado de **32 caracteres o más**. Es la clave con la que se firman los tokens. |
| `MAIL_USERNAME` | La dirección completa del Gmail que envía los correos (ver [El email](#el-email-recuperación-de-contraseña)). |
| `MAIL_PASSWORD` | La contraseña de aplicación de ese Gmail: 16 letras, sin espacios. |

El resto (puerto `8080`, base `turnos_medicos` en `localhost:5432`, servidor `smtp.gmail.com`) ya
viene con valores por defecto en la plantilla y no hace falta definirlo.

#### En Windows

En una terminal de PowerShell:

```powershell
setx DB_USERNAME "postgres"
setx DB_PASSWORD "TU_PASSWORD_DE_POSTGRES"
setx JWT_SECRET "un-texto-largo-inventado-de-al-menos-32-caracteres"
setx MAIL_USERNAME "tu.cuenta@gmail.com"
setx MAIL_PASSWORD "abcdefghijklmnop"
```

`setx` las guarda para siempre en tu usuario de Windows, pero **solo las ven los programas que
abras después**: cerrá la terminal (y IntelliJ, si estaba abierto) y abrí una nueva. Para
comprobarlo: `echo $env:DB_USERNAME` tiene que mostrar `postgres`. Para corregir un valor, volvé a
correr el `setx` de esa variable.

#### En IntelliJ IDEA

Si las definiste con `setx` y reiniciaste IntelliJ, no hace falta nada más. Si preferís cargarlas
solo en IntelliJ:

1. **Run → Edit Configurations…** → elegí `BackendApplication`.
2. Si no ves el campo **Environment variables**, activalo desde **Modify options → Environment
   variables**.
3. Pegá las cinco separadas por punto y coma:

   ```
   DB_USERNAME=postgres;DB_PASSWORD=TU_PASSWORD_DE_POSTGRES;JWT_SECRET=un-texto-largo-inventado-de-al-menos-32-caracteres;MAIL_USERNAME=tu.cuenta@gmail.com;MAIL_PASSWORD=abcdefghijklmnop
   ```

Así cargadas, solo valen cuando el backend se ejecuta desde IntelliJ (no desde `mvnw.cmd` en una
terminal).

#### El email (recuperación de contraseña)

`POST /api/v1/auth/recuperar-contrasena/solicitar` le manda al paciente un código por email desde
el Gmail de `MAIL_USERNAME`. Para que funcione:

1. La cuenta de Gmail tiene que tener activada la **verificación en 2 pasos** (sin eso, Google no
   deja crear contraseñas de aplicación).
2. Creá una **contraseña de aplicación** en https://myaccount.google.com/apppasswords. No es la
   contraseña normal de la cuenta. Google la muestra en cuatro grupos (`abcd efgh ijkl mnop`): en
   `MAIL_PASSWORD` van las **16 letras juntas, sin espacios**.
3. En `MAIL_USERNAME` va la dirección completa de esa cuenta (`tu.cuenta@gmail.com`).

El servidor de correo sale de la variable `MAIL_HOST`, que por defecto es `smtp.gmail.com`. **Si
`MAIL_HOST` apunta a otro servidor (por ejemplo, Mailtrap), el email no llega a Gmail**: queda en
la bandeja de ese servicio. Para recibirlo en la casilla real, no definas `MAIL_HOST`.

Si el usuario o la contraseña del Gmail están mal, el backend arranca igual, pero al pedir la
recuperación responde `500`.

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
