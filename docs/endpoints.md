# 13. ENDPOINTS

**Base URL:** `/api/v1`

**Criterio general:** todos los recursos exponen su baja mediante `DELETE`, que realiza
exclusivamente una baja lógica (`activo = false`). La única excepción es el recurso **Turno**, que
no expone `DELETE`: su cancelación es un cambio de estado vía `PUT`, preservando trazabilidad.

---

## Autenticación

### `POST /api/v1/auth/register`

**Descripción**
Autorregistro de paciente. Crea en la misma operación el usuario (rol `PACIENTE`) y el paciente
asociado. Si el DNI corresponde a un usuario dado de baja previamente, se reactiva y re-vincula el
registro existente.

**Request Body**
```json
{
  "dni": "32111222",
  "contrasena": "********",
  "nombreApellido": "Juan Pérez",
  "email": "juan@gmail.com",
  "celular": "1122334455",
  "fechaNacimiento": "1990-02-15"
}
```

**Response `201 Created`**
```json
{
  "idUsuario": 15,
  "idPaciente": 5,
  "mensaje": "Registro exitoso."
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Datos inválidos. |
| 409 | Ya existe un usuario activo con ese DNI. |

### `POST /api/v1/auth/login`

**Descripción**
Inicia sesión con DNI y contraseña y devuelve el token JWT, que se envía en el resto de las
peticiones como `Authorization: Bearer <token>`. `rol` es opcional: solo hace falta si el DNI tiene
dos usuarios activos (uno `PACIENTE` y otro `ADMINISTRADOR`), para indicar con cuál ingresar.

**Request Body**
```json
{
  "dni": "32111222",
  "contrasena": "********",
  "rol": "PACIENTE"
}
```

**Response `200 OK`**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "rol": "PACIENTE"
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (falta el DNI o la contraseña). |
| 400 | Ese DNI tiene más de un rol activo; indicá con cuál querés ingresar. |
| 401 | DNI o contraseña incorrectos. |

### `POST /api/v1/auth/recuperar-contrasena/solicitar`

**Descripción**
Paso 1 de la recuperación de contraseña. Si el email pertenece a un paciente, genera un código
(válido por 30 minutos) y se lo envía por email. Responde lo mismo exista o no el email, para no
revelar qué emails están registrados.

**Request Body**
```json
{
  "email": "juan@gmail.com"
}
```

**Response `200 OK`**
```json
{
  "mensaje": "Si el email está registrado, te enviamos un correo con instrucciones."
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (email vacío o con formato inválido). |
| 500 | Ocurrió un error inesperado (no se pudo enviar el email: revisar la configuración `MAIL_*`). |

### `POST /api/v1/auth/recuperar-contrasena/confirmar`

**Descripción**
Paso 2 de la recuperación de contraseña. Recibe el código enviado por email y la contraseña nueva.
El código se puede usar una sola vez.

**Request Body**
```json
{
  "token": "3f1c9a2e-7b4d-4e0a-9c55-1a2b3c4d5e6f",
  "contrasenaNueva": "********"
}
```

**Response `200 OK`**
```json
{
  "mensaje": "Contraseña actualizada correctamente."
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (la contraseña nueva debe tener entre 6 y 100 caracteres). |
| 401 | El código no es válido. |
| 401 | El código no es válido o ya venció. |

---

## Turnos

**Criterio:** el turno no se elimina. Se cancela con `PUT /turnos/{id}/cancelar`, que cambia su
estado a `CANCELADO` y deja el horario libre. Estados posibles: `CONFIRMADO` (al crearlo),
`CANCELADO` y `ATENDIDO` (al registrar la consulta médica).

Un PACIENTE solo puede ver, reservar y cancelar sus propios turnos; el ADMINISTRADOR, los de
cualquier paciente. Si un PACIENTE intenta acceder a un turno ajeno: `403 No podés acceder a datos
de otro paciente`.

### `POST /api/v1/turnos`

**Descripción**
Reserva un turno. `idDisponibilidad` es la franja semanal del médico que contiene ese día y
horario. `idCobertura` es opcional (sin cobertura = particular).

**Request Body**
```json
{
  "idPaciente": 1,
  "idMedico": 1,
  "idDisponibilidad": 1,
  "idCobertura": 1,
  "fecha": "2026-11-23",
  "hora": "09:30",
  "motivoConsulta": "Control anual"
}
```

**Response `201 Created`**
```json
{
  "id": 7,
  "idPaciente": 1,
  "idMedico": 1,
  "idDisponibilidad": 1,
  "idCobertura": 1,
  "fecha": "2026-11-23",
  "hora": "09:30:00",
  "estado": "CONFIRMADO",
  "motivoConsulta": "Control anual",
  "motivoCancelacion": null
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios o la fecha ya pasó). |
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente, médico, disponibilidad o cobertura indicados. |
| 409 | El paciente o el médico está dado de baja. |
| 409 | La disponibilidad indicada está dada de baja o no pertenece al médico. |
| 409 | La fecha no coincide con el día de la disponibilidad indicada. |
| 409 | El horario está fuera de la disponibilidad del médico. |
| 409 | El horario no coincide con turnos de {N} minutos. |
| 409 | El médico no atiende en esa fecha (bloqueo). |
| 409 | La cobertura indicada no pertenece al paciente o está dada de baja. |
| 409 | El médico ya tiene turno asignado en ese horario. |

### `GET /api/v1/turnos/mis-turnos`

**Descripción**
Solo PACIENTE. Devuelve los turnos del paciente autenticado (lo identifica por el DNI del token),
del más nuevo al más viejo.

**Response `200 OK`**: lista de turnos, con el mismo formato que `POST /api/v1/turnos`.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol PACIENTE (mensaje `Access Denied`). |
| 404 | No existe un paciente asociado al DNI del usuario. |

### `PUT /api/v1/turnos/{id}/cancelar`

**Descripción**
Cancela un turno `CONFIRMADO`. Quién cancela lo decide el rol del token: para el PACIENTE el
motivo es opcional; para el ADMINISTRADOR (cancela del lado del médico) es obligatorio.

**Request Body**
```json
{
  "motivoCancelacion": "No puedo asistir"
}
```

**Response `200 OK`**: el turno con `"estado": "CANCELADO"` y el motivo cargado.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | El motivo de cancelación es obligatorio cuando cancela el médico. |
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe turno con el id: {id}. |
| 409 | Solo se puede cancelar un turno en estado CONFIRMADO. |

**Consultas**
- `GET /api/v1/turnos`: lista todos los turnos (solo ADMINISTRADOR).
- `GET /api/v1/turnos/{id}`: obtiene un turno (un PACIENTE, solo los suyos).

---

## Consultas médicas

Solo ADMINISTRADOR.

### `POST /api/v1/consultas-medicas`

**Descripción**
Registra la consulta médica de un turno. El turno tiene que estar `CONFIRMADO` y pasa
automáticamente a `ATENDIDO`. Cada turno puede tener una sola consulta.

**Request Body**
```json
{
  "idTurno": 7,
  "observaciones": "Paciente estable, control en 6 meses."
}
```

**Response `201 Created`**
```json
{
  "id": 3,
  "idTurno": 7,
  "fecha": "2026-11-23",
  "hora": "09:30:00",
  "observaciones": "Paciente estable, control en 6 meses."
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (`idTurno` es obligatorio). |
| 404 | No existe turno con el id: {id}. |
| 409 | Solo se puede registrar una consulta a partir de un turno en estado CONFIRMADO. |
| 409 | El turno ya tiene una consulta médica registrada. |

**Consultas**
- `GET /api/v1/consultas-medicas`: lista todas las consultas.
- `GET /api/v1/consultas-medicas/{id}`: obtiene una consulta.

---

## Usuarios

Cuentas de acceso (DNI, contraseña y rol). Un mismo DNI puede tener dos usuarios, uno `PACIENTE` y
otro `ADMINISTRADOR`. El ADMINISTRADOR gestiona todas las cuentas; un PACIENTE solo la suya. La
contraseña nunca se devuelve: se guarda encriptada.

### `GET /api/v1/usuarios`

**Descripción**
Solo ADMINISTRADOR. Lista los usuarios activos.

**Response `200 OK`**
```json
[
  { "id": 1, "dni": "30111222", "rol": "ADMINISTRADOR", "activo": true },
  { "id": 2, "dni": "32111222", "rol": "PACIENTE", "activo": true }
]
```

**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |

### `GET /api/v1/usuarios/{id}`

**Descripción**
Obtiene un usuario. El ADMINISTRADOR puede ver cualquiera; un PACIENTE, solo su propia cuenta.

**Response `200 OK`**
```json
{ "id": 2, "dni": "32111222", "rol": "PACIENTE", "activo": true }
```

**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a la cuenta de otro usuario. |
| 404 | No existe el usuario con id {id}. |

### `POST /api/v1/usuarios`

**Descripción**
Solo ADMINISTRADOR. Crea un usuario con el rol indicado (se usa para dar de alta otra cuenta
administradora; los pacientes se registran solos con `POST /api/v1/auth/register`).

**Request Body**
```json
{
  "dni": "40111222",
  "contrasena": "********",
  "rol": "ADMINISTRADOR"
}
```

**Response `201 Created`**
```json
{ "id": 9, "dni": "40111222", "rol": "ADMINISTRADOR", "activo": true }
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (DNI de hasta 10 caracteres, contraseña de 6 a 100, rol obligatorio). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 409 | Ya existe un usuario {rol} con el DNI '{dni}'. |

### `PUT /api/v1/usuarios/{id}/contrasena`

**Descripción**
Cambia la contraseña de una cuenta, pidiendo la actual. Un PACIENTE solo puede cambiar la suya.

**Request Body**
```json
{
  "contrasenaActual": "********",
  "contrasenaNueva": "********"
}
```

**Response `200 OK`**: el usuario, con el mismo formato que `GET /api/v1/usuarios/{id}`.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (la contraseña nueva debe tener de 6 a 100 caracteres). |
| 401 | La contraseña actual no es correcta. |
| 403 | No podés acceder a la cuenta de otro usuario. |
| 404 | No existe el usuario con id {id}. |

### `DELETE /api/v1/usuarios/{id}`

**Descripción**
Baja lógica de la cuenta (`activo = false`): deja de aparecer en el listado y ya no puede iniciar
sesión. Un PACIENTE solo puede dar de baja la suya.

**Response `204 No Content`**

**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a la cuenta de otro usuario. |
| 404 | No existe el usuario con id {id}. |

---

## Tratamientos

### `POST /api/v1/tratamientos`

Registra un tratamiento asociado a un diagnóstico existente. El estado inicial
se crea como
`EN_CURSO` automáticamente.

**Request Body**
```json
{
  "idDiagnostico": 4,
  "descripcion": "Paracetamol 500 mg cada 8 horas.",
  "duracion": "5 días"
}
```

**Response `201 Created`**
```json
{
  "id": 1,
  "idDiagnostico": 4,
  "descripcion": "Paracetamol 500 mg cada 8 horas.",
  "duracion": "5 días",
  "estado": "EN_CURSO"
}
```

**Consultas**
- `GET /api/v1/tratamientos`: lista todos los tratamientos.
- `GET /api/v1/tratamientos?idDiagnostico=4`: lista los tratamientos de un diagnóstico.
- `GET /api/v1/tratamientos/{id}`: obtiene un tratamiento.
- `PUT /api/v1/tratamientos/{id}`: actualiza diagnóstico, descripción y duración.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | `idDiagnostico` es obligatorio y `descripcion` no puede estar vacía. |
| 404 | No existe el diagnóstico o tratamiento indicado. |

---
## Médicos
 
**Criterio:** el médico es el profesional que atiende los turnos y está asociado a un usuario con
rol ADMINISTRADOR (mismo DNI). Cualquier usuario autenticado puede consultar los médicos y sus
horarios disponibles; crearlos, modificarlos y darlos de baja es solo del ADMINISTRADOR.
 
### `GET /api/v1/medicos`
 
**Descripción**
Lista los médicos activos. Lo usan el PACIENTE (para reservar un turno) y el ADMINISTRADOR.
 
**Response `200 OK`**
```json
[
  {
    "id": 1,
    "nombre": "Dra. Ana López",
    "matricula": "MP-12345",
    "especialidades": ["Clínica Médica", "Pediatría"],
    "activo": true
  }
]
```
 
### `GET /api/v1/medicos/{id}`
 
**Descripción**
Obtiene un médico. Si está dado de baja, lo devuelve igual con `"activo": false`.
 
**Response `200 OK`**: el médico, con el mismo formato que un elemento de `GET /api/v1/medicos`.
 
**Errores**
| Código | Mensaje |
|---|---|
| 404 | No existe el médico con id {id}. |
 
### `POST /api/v1/medicos`
 
**Descripción**
Solo ADMINISTRADOR. Crea un médico asociado a un usuario existente con rol ADMINISTRADOR. El DNI
del médico tiene que ser el mismo que el del usuario. `especialidades` es opcional.
 
**Request Body**
```json
{
  "idUsuario": 1,
  "nombre": "Dra. Ana López",
  "dni": "30111222",
  "matricula": "MP-12345",
  "especialidades": ["Clínica Médica", "Pediatría"]
}
```
 
**Response `201 Created`**
```json
{
  "id": 1,
  "nombre": "Dra. Ana López",
  "matricula": "MP-12345",
  "especialidades": ["Clínica Médica", "Pediatría"],
  "activo": true
}
```
 
**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (`idUsuario` obligatorio; nombre de hasta 100 caracteres, DNI de hasta 10 y matrícula de hasta 20, los tres obligatorios). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el usuario con id {idUsuario}. |
| 409 | El usuario {idUsuario} no tiene rol ADMINISTRADOR. |
| 409 | El DNI del médico debe coincidir con el DNI del usuario. |
| 409 | El usuario {idUsuario} ya tiene un médico asociado. |
| 409 | Ya existe un médico con el DNI '{dni}'. |
| 409 | Ya existe un médico con la matrícula '{matricula}'. |
 
### `PUT /api/v1/medicos/{id}`
 
**Descripción**
Solo ADMINISTRADOR. Actualiza nombre, matrícula y especialidades. El body es el mismo que el del
`POST` (los campos obligatorios se validan), pero `idUsuario` y `dni` no se modifican.
 
**Response `200 OK`**: el médico actualizado, con el mismo formato que `GET /api/v1/medicos/{id}`.
 
**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (mismas reglas que el `POST`). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el médico con id {id}. |
| 409 | Ya existe un médico con la matrícula '{matricula}'. |
 
### `DELETE /api/v1/medicos/{id}`
 
**Descripción**
Solo ADMINISTRADOR. Baja lógica (`activo = false`): el médico deja de aparecer en el listado.
 
**Response `204 No Content`**
 
**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el médico con id {id}. |
 
### `GET /api/v1/medicos/{id}/horarios-disponibles?fecha=2026-11-25`
 
**Descripción**
Devuelve los horarios libres del médico en una fecha (formato `AAAA-MM-DD`). Se calculan a partir
de su disponibilidad activa para ese día de la semana, en pasos de la duración del turno, y se
quitan los horarios que ya tienen un turno no cancelado. Si la fecha está bloqueada (feriado,
vacaciones) o el médico no atiende ese día, devuelve una lista vacía.
 
**Response `200 OK`** (miércoles, disponibilidad de 09:00 a 13:00 con turnos de 30 minutos)
```json
["09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30"]
```
 
**Errores**
| Código | Mensaje |
|---|---|
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (falta `fecha` o no tiene el formato `AAAA-MM-DD`). |
| 404 | No existe el médico con id {id}. |
 
---

## Pacientes

**Criterio:** el paciente es la persona que reserva turnos y está asociada a un usuario con rol
PACIENTE (mismo DNI). El ADMINISTRADOR gestiona a todos los pacientes; un PACIENTE solo puede ver y
modificar sus propios datos. Si intenta acceder a los de otro: `403 No podés acceder a datos de
otro paciente`.
 
### `GET /api/v1/pacientes`
 
**Descripción**
Solo ADMINISTRADOR. Lista los pacientes activos.
 
**Response `200 OK`**
```json
[
  {
    "id": 1,
    "idUsuario": 2,
    "nombreApellido": "Juan Pérez",
    "dni": "32111222",
    "fechaNacimiento": "1990-02-15",
    "email": "juan.perez@example.com",
    "celular": "1122334455",
    "domicilio": "Av. Siempre Viva 123",
    "activo": true
  }
]
```
 
**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
 
### `GET /api/v1/pacientes/me`
 
**Descripción**
Solo PACIENTE. Devuelve los datos del paciente que inició sesión. El token trae el DNI, y con él se
busca el paciente (por eso no lleva id en la ruta).
 
**Response `200 OK`**: el paciente, con el mismo formato que un elemento de `GET /api/v1/pacientes`.
 
**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol PACIENTE (mensaje `Access Denied`). |
| 404 | No existe un paciente asociado al dni {dni}. |
 
### `GET /api/v1/pacientes/{id}`
 
**Descripción**
Obtiene un paciente. El ADMINISTRADOR puede ver cualquiera; un PACIENTE, solo el suyo. Si está
dado de baja, lo devuelve igual con `"activo": false`.
 
**Response `200 OK`**: el paciente, con el mismo formato que un elemento de `GET /api/v1/pacientes`.
 
**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente con id {id}. |
 
### `POST /api/v1/pacientes`
 
**Descripción**
Solo ADMINISTRADOR. Crea un paciente asociado a un usuario existente con rol PACIENTE. El DNI del
paciente tiene que ser el mismo que el del usuario. `email`, `celular` y `domicilio` son
opcionales. (Un paciente nuevo se registra solo con `POST /api/v1/auth/register`, que crea el
usuario y el paciente juntos.)
 
**Request Body**
```json
{
  "idUsuario": 2,
  "nombreApellido": "Juan Pérez",
  "dni": "32111222",
  "fechaNacimiento": "1990-02-15",
  "email": "juan.perez@example.com",
  "celular": "1122334455",
  "domicilio": "Av. Siempre Viva 123"
}
```
 
**Response `201 Created`**: el paciente creado, con el mismo formato que un elemento de
`GET /api/v1/pacientes`.
 
**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (`idUsuario` obligatorio; nombre y apellido de hasta 100 caracteres y DNI de hasta 10, obligatorios; fecha de nacimiento obligatoria y en el pasado; email con formato válido de hasta 100; celular de hasta 20; domicilio de hasta 150). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el usuario con id {idUsuario}. |
| 409 | El usuario {idUsuario} no tiene rol PACIENTE. |
| 409 | El DNI del paciente debe coincidir con el DNI del usuario. |
| 409 | El usuario {idUsuario} ya tiene un paciente asociado. |
| 409 | Ya existe un paciente con el DNI '{dni}'. |
 
### `PUT /api/v1/pacientes/{id}`
 
**Descripción**
Actualiza nombre y apellido, email, celular y domicilio. El ADMINISTRADOR puede modificar
cualquiera; un PACIENTE, solo el suyo. El body es el mismo que el del `POST` (los campos
obligatorios se validan), pero `idUsuario`, `dni` y `fechaNacimiento` no se modifican.
 
**Response `200 OK`**: el paciente actualizado, con el mismo formato que `GET /api/v1/pacientes/{id}`.
 
**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (mismas reglas que el `POST`). |
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente con id {id}. |
 
### `DELETE /api/v1/pacientes/{id}`
 
**Descripción**
Solo ADMINISTRADOR. Baja lógica (`activo = false`): el paciente deja de aparecer en el listado.
 
**Response `204 No Content`**
 
**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el paciente con id {id}. |
 

### `GET /api/v1/pacientes/{id}/coberturas`

Devuelve las coberturas activas del paciente (obra social o prepaga, número de afiliado y plan).
El ADMINISTRADOR puede ver las de cualquier paciente; un PACIENTE, solo las suyas.

**Response `200 OK`**
```json
[
  { "id": 1, "idPaciente": 1, "idFinanciador": 2, "numeroAfiliado": "123456789", "plan": "210", "activo": true }
]
```

**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente con id {id}. |

### `GET /api/v1/pacientes/{id}/turnos`

Devuelve todos los turnos del paciente (confirmados, cancelados y atendidos), del más nuevo al más viejo.
El ADMINISTRADOR puede ver los de cualquier paciente; un PACIENTE, solo los suyos.

**Response `200 OK`**: lista de turnos, con el mismo formato que `GET /api/v1/turnos/{id}`.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente con id {id}. |

---

## Disponibilidades

**Criterio:** la disponibilidad es la franja semanal recurrente en la que atiende el médico (por
ejemplo, los lunes de 09:00 a 17:00 con turnos de 30 minutos). A partir de ella se calculan los
horarios disponibles. Cualquier usuario autenticado puede consultarlas; crearlas, modificarlas y
darlas de baja es solo del ADMINISTRADOR.

### `GET /api/v1/disponibilidades`

**Descripción**
Cualquier usuario autenticado. Lista las disponibilidades activas. Con `?idMedico=1` devuelve solo
las del médico indicado.

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "idMedico": 1,
    "diaSemana": "LUNES",
    "horaInicio": "09:00:00",
    "horaFin": "17:00:00",
    "duracionTurnoMin": 30,
    "activo": true
  }
]
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (`idMedico` no numérico). |
| 404 | No existe el médico con id {id}. |

### `GET /api/v1/disponibilidades/{id}`

**Descripción**
Cualquier usuario autenticado. Obtiene una disponibilidad. Si está dada de baja, la devuelve igual
con `"activo": false`.

**Response `200 OK`**: la disponibilidad, con el mismo formato que un elemento de
`GET /api/v1/disponibilidades`.

**Errores**
| Código | Mensaje |
|---|---|
| 404 | No existe la disponibilidad con id {id}. |

### `POST /api/v1/disponibilidades`

**Descripción**
Solo ADMINISTRADOR. Crea una franja semanal para un médico activo. La hora de inicio tiene que ser
anterior a la de fin, la duración del turno tiene que entrar en la franja y no se puede solapar con
otra disponibilidad activa del mismo médico ese día.

**Request Body**
```json
{
  "idMedico": 1,
  "diaSemana": "LUNES",
  "horaInicio": "09:00",
  "horaFin": "17:00",
  "duracionTurnoMin": 30
}
```

`diaSemana`: `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`, `SABADO` o `DOMINGO`.

**Response `201 Created`**
```json
{
  "id": 3,
  "idMedico": 1,
  "diaSemana": "LUNES",
  "horaInicio": "09:00:00",
  "horaFin": "17:00:00",
  "duracionTurnoMin": 30,
  "activo": true
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios o `duracionTurnoMin` no es positivo). |
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (`diaSemana` u hora con formato inválido). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el médico con id {id}. |
| 409 | El médico {id} está dado de baja. |
| 409 | La hora de inicio debe ser anterior a la hora de fin. |
| 409 | La duración del turno no entra en la ventana horaria indicada. |
| 409 | El médico ya tiene una disponibilidad que se solapa con ese horario. |

### `PUT /api/v1/disponibilidades/{id}`

**Descripción**
Solo ADMINISTRADOR. Reemplaza médico, día, horario y duración de la disponibilidad. Aplica las
mismas validaciones de horario y solapamiento que el alta (sin compararla consigo misma). No
revisa si el médico está dado de baja.

**Request Body**: igual que `POST /api/v1/disponibilidades`.

**Response `200 OK`**: la disponibilidad actualizada, con el mismo formato que el alta.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios o `duracionTurnoMin` no es positivo). |
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (`diaSemana` u hora con formato inválido). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe la disponibilidad con id {id}. |
| 404 | No existe el médico con id {id}. |
| 409 | La hora de inicio debe ser anterior a la hora de fin. |
| 409 | La duración del turno no entra en la ventana horaria indicada. |
| 409 | El médico ya tiene una disponibilidad que se solapa con ese horario. |

### `DELETE /api/v1/disponibilidades/{id}`

**Descripción**
Solo ADMINISTRADOR. Baja lógica (`activo = false`): la franja deja de aparecer en los listados.

**Response `204 No Content`**: sin cuerpo.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe la disponibilidad con id {id}. |

---

## Bloqueos

Solo ADMINISTRADOR (todos los endpoints). Un PACIENTE recibe `403 Sin permisos` (mensaje
`Access Denied`).

**Criterio:** el bloqueo es un rango de fechas en el que el médico no atiende (feriado, vacaciones,
ausencia) y tiene prioridad sobre la disponibilidad. Al crearlo, cancela los turnos `CONFIRMADO`
del médico dentro del rango con el motivo del bloqueo. Su baja vuelve a ofrecer esos horarios, pero
**no restaura** los turnos cancelados.

### `GET /api/v1/bloqueos`

**Descripción**
Lista los bloqueos activos. Con `?idMedico=1` devuelve solo los del médico indicado.

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "idMedico": 1,
    "fechaDesde": "2026-12-24",
    "fechaHasta": "2026-12-25",
    "motivo": "Feriado de Navidad",
    "activo": true
  }
]
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (`idMedico` no numérico). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el médico con id {id}. |

### `GET /api/v1/bloqueos/{id}`

**Descripción**
Obtiene un bloqueo. Si está dado de baja, lo devuelve igual con `"activo": false`.

**Response `200 OK`**: el bloqueo, con el mismo formato que un elemento de `GET /api/v1/bloqueos`.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el bloqueo con id {id}. |

### `POST /api/v1/bloqueos`

**Descripción**
Crea un bloqueo para un médico activo. `fechaHasta` puede ser igual a `fechaDesde` (bloqueo de un
solo día). Los turnos `CONFIRMADO` del médico entre esas fechas pasan a `CANCELADO` con
`motivoCancelacion` igual al `motivo` del bloqueo.

**Request Body**
```json
{
  "idMedico": 1,
  "fechaDesde": "2026-12-24",
  "fechaHasta": "2026-12-25",
  "motivo": "Feriado de Navidad"
}
```

**Response `201 Created`**
```json
{
  "id": 2,
  "idMedico": 1,
  "fechaDesde": "2026-12-24",
  "fechaHasta": "2026-12-25",
  "motivo": "Feriado de Navidad",
  "activo": true
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios o `motivo` vacío o de más de 100 caracteres). |
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (fecha con formato inválido). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el médico con id {id}. |
| 409 | El médico {id} está dado de baja. |
| 409 | La fecha de fin no puede ser anterior a la fecha de inicio. |

### `PUT /api/v1/bloqueos/{id}`

**Descripción**
Reemplaza médico, fechas y motivo del bloqueo. A diferencia del alta, **no** cancela turnos del
nuevo rango ni revisa si el médico está dado de baja.

**Request Body**: igual que `POST /api/v1/bloqueos`.

**Response `200 OK`**: el bloqueo actualizado, con el mismo formato que el alta.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios o `motivo` vacío o de más de 100 caracteres). |
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (fecha con formato inválido). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el bloqueo con id {id}. |
| 404 | No existe el médico con id {id}. |
| 409 | La fecha de fin no puede ser anterior a la fecha de inicio. |

### `DELETE /api/v1/bloqueos/{id}`

**Descripción**
Baja lógica (`activo = false`). Los horarios del rango vuelven a estar disponibles; los turnos que
el bloqueo canceló siguen `CANCELADO`.

**Response `204 No Content`**: sin cuerpo.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el bloqueo con id {id}. |

---

## Coberturas

**Criterio:** la cobertura vincula a un paciente con un financiador (obra social o prepaga). Un
paciente puede tener cero, una o varias. Los endpoints no restringen por rol: los usan el
ADMINISTRADOR (sobre cualquier paciente) y el PACIENTE (solo sobre las suyas). Si un PACIENTE
intenta operar sobre una cobertura de otro paciente: `403 No podés acceder a datos de otro
paciente`.

### `GET /api/v1/coberturas`

**Descripción**
Sin parámetros: solo ADMINISTRADOR, lista todas las coberturas activas del sistema. Con
`?idPaciente=1`: lista las coberturas activas de ese paciente (un PACIENTE, solo con su propio id).

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "idPaciente": 1,
    "idFinanciador": 2,
    "numeroAfiliado": "123456789",
    "plan": "210",
    "activo": true
  }
]
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | La petición tiene un parámetro faltante, de tipo incorrecto, o el body no es JSON válido (`idPaciente` no numérico). |
| 403 | Solo el administrador puede listar todas las coberturas; indicá idPaciente. |
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente con id {id}. |

### `GET /api/v1/coberturas/{id}`

**Descripción**
ADMINISTRADOR o el PACIENTE dueño de la cobertura. Si está dada de baja, la devuelve igual con
`"activo": false`.

**Response `200 OK`**: la cobertura, con el mismo formato que un elemento de
`GET /api/v1/coberturas`.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe la cobertura con id {id}. |

### `POST /api/v1/coberturas`

**Descripción**
ADMINISTRADOR, o un PACIENTE para sí mismo. Crea una cobertura para un paciente activo con un
financiador activo. `plan` es opcional. No puede repetirse la combinación paciente + financiador +
número de afiliado (cuentan también las coberturas dadas de baja).

**Request Body**
```json
{
  "idPaciente": 1,
  "idFinanciador": 2,
  "numeroAfiliado": "123456789",
  "plan": "210"
}
```

**Response `201 Created`**
```json
{
  "id": 4,
  "idPaciente": 1,
  "idFinanciador": 2,
  "numeroAfiliado": "123456789",
  "plan": "210",
  "activo": true
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios, `numeroAfiliado` vacío o de más de 30 caracteres, o `plan` de más de 50). |
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe el paciente con id {id}. |
| 404 | No existe el financiador con id {id}. |
| 409 | El paciente {id} está dado de baja. |
| 409 | El financiador {id} está dado de baja. |
| 409 | El paciente ya tiene una cobertura con ese financiador y número de afiliado. |

### `PUT /api/v1/coberturas/{id}`

**Descripción**
ADMINISTRADOR o el PACIENTE dueño de la cobertura. Actualiza financiador, número de afiliado y
plan. `idPaciente` tiene que ser el mismo que ya tiene la cobertura: el paciente no se puede
cambiar.

**Request Body**: igual que `POST /api/v1/coberturas`.

**Response `200 OK`**: la cobertura actualizada, con el mismo formato que el alta.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (faltan datos obligatorios, `numeroAfiliado` vacío o de más de 30 caracteres, o `plan` de más de 50). |
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe la cobertura con id {id}. |
| 404 | No existe el financiador con id {id}. |
| 409 | No se puede cambiar el paciente de una cobertura. |
| 409 | El paciente ya tiene una cobertura con ese financiador y número de afiliado. |

### `DELETE /api/v1/coberturas/{id}`

**Descripción**
ADMINISTRADOR o el PACIENTE dueño de la cobertura. Baja lógica (`activo = false`). Los turnos que
ya la usaban la conservan.

**Response `204 No Content`**: sin cuerpo.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | No podés acceder a datos de otro paciente. |
| 404 | No existe la cobertura con id {id}. |

---

## Financiadores

**Criterio:** el financiador es la obra social o prepaga. Cualquier usuario autenticado puede
consultarlos (el paciente los necesita para cargar su cobertura); crearlos, modificarlos y darlos
de baja es solo del ADMINISTRADOR. El nombre no se puede repetir (sin distinguir mayúsculas y
contando también los dados de baja).

### `GET /api/v1/financiadores`

**Descripción**
Cualquier usuario autenticado. Lista los financiadores activos.

**Response `200 OK`**
```json
[
  { "id": 1, "nombre": "IOMA", "cuit": "30-99999999-9", "activo": true },
  { "id": 2, "nombre": "OSDE", "cuit": "30-88888888-8", "activo": true }
]
```

**Errores**: no tiene errores propios (solo `401` si falta el token).

### `GET /api/v1/financiadores/{id}`

**Descripción**
Cualquier usuario autenticado. Obtiene un financiador. Si está dado de baja, lo devuelve igual con
`"activo": false`.

**Response `200 OK`**
```json
{ "id": 1, "nombre": "IOMA", "cuit": "30-99999999-9", "activo": true }
```

**Errores**
| Código | Mensaje |
|---|---|
| 404 | No existe el financiador con id {id}. |

### `POST /api/v1/financiadores`

**Descripción**
Solo ADMINISTRADOR. Crea un financiador. `cuit` es opcional.

**Request Body**
```json
{
  "nombre": "Swiss Medical",
  "cuit": "30-77777777-7"
}
```

**Response `201 Created`**
```json
{
  "id": 3,
  "nombre": "Swiss Medical",
  "cuit": "30-77777777-7",
  "activo": true
}
```

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (`nombre` vacío o de más de 100 caracteres, o `cuit` de más de 13). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 409 | Ya existe un financiador con el nombre '{nombre}'. |

### `PUT /api/v1/financiadores/{id}`

**Descripción**
Solo ADMINISTRADOR. Actualiza nombre y CUIT.

**Request Body**: igual que `POST /api/v1/financiadores`.

**Response `200 OK`**: el financiador actualizado, con el mismo formato que el alta.

**Errores**
| Código | Mensaje |
|---|---|
| 400 | Uno o más campos no son válidos (`nombre` vacío o de más de 100 caracteres, o `cuit` de más de 13). |
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el financiador con id {id}. |
| 409 | Ya existe un financiador con el nombre '{nombre}'. |

### `DELETE /api/v1/financiadores/{id}`

**Descripción**
Solo ADMINISTRADOR. Baja lógica (`activo = false`). Las coberturas que ya lo usan no se modifican,
pero no se pueden crear coberturas nuevas con un financiador dado de baja.

**Response `204 No Content`**: sin cuerpo.

**Errores**
| Código | Mensaje |
|---|---|
| 403 | Sin permisos: el usuario no tiene rol ADMINISTRADOR (mensaje `Access Denied`). |
| 404 | No existe el financiador con id {id}. |
