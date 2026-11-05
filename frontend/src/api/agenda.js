import { apiFetch } from "./cliente";

// Llama al backend y devuelve el JSON, o lanza un Error con el mensaje del backend.
async function pedir(path, opciones = {}) {
  const respuesta = await apiFetch(path, opciones);
  if (!respuesta) return null; // 401: cliente.js ya redirigió a /login
  if (respuesta.status === 204) return null;

  const cuerpo = await respuesta.json().catch(() => null);
  if (!respuesta.ok) {
    throw new Error(cuerpo?.mensaje || cuerpo?.message || `Error ${respuesta.status}`);
  }
  return cuerpo;
}

// Disponibilidad semanal del médico
export const listarDisponibilidades = (idMedico) =>
  pedir(idMedico ? `/disponibilidades?idMedico=${idMedico}` : "/disponibilidades");

export const crearDisponibilidad = (datos) =>
  pedir("/disponibilidades", { method: "POST", body: JSON.stringify(datos) });

export const actualizarDisponibilidad = (id, datos) =>
  pedir(`/disponibilidades/${id}`, { method: "PUT", body: JSON.stringify(datos) });

export const eliminarDisponibilidad = (id) =>
  pedir(`/disponibilidades/${id}`, { method: "DELETE" });

// Bloqueos (vacaciones, licencias): al crear uno, el backend cancela los turnos de esos días
export const listarBloqueos = (idMedico) =>
  pedir(idMedico ? `/bloqueos?idMedico=${idMedico}` : "/bloqueos");

export const crearBloqueo = (datos) =>
  pedir("/bloqueos", { method: "POST", body: JSON.stringify(datos) });

export const actualizarBloqueo = (id, datos) =>
  pedir(`/bloqueos/${id}`, { method: "PUT", body: JSON.stringify(datos) });

export const eliminarBloqueo = (id) =>
  pedir(`/bloqueos/${id}`, { method: "DELETE" });
