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

**Response Body**
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

---

## Tratamientos

### `POST /api/v1/tratamientos`

Registra un tratamiento asociado a un diagnóstico existente. El estado inicial se crea como
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
