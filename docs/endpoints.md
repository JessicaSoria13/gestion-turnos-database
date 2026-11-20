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
## Pacientes

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
