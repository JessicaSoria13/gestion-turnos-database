const BASE_URL = "http://localhost:8081/api/v1";

export function guardarSesion(token, rol) {
  localStorage.setItem("token", token);
  localStorage.setItem("rol", rol);
}

export function obtenerToken() {
  return localStorage.getItem("token");
}

export function obtenerRol() {
  return localStorage.getItem("rol");
}

export function cerrarSesion() {
  localStorage.removeItem("token");
  localStorage.removeItem("rol");
}

export async function apiFetch(path, opciones = {}) {
  const token = obtenerToken();

  const headers = {
    "Content-Type": "application/json",
    ...opciones.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const respuesta = await fetch(`${BASE_URL}${path}`, {
    ...opciones,
    headers,
  });

  // Solo redirige si habia sesion (token vencido o invalido).
  // En el login no hay token, asi el 401 llega a la pantalla y se muestra el error.
  if (respuesta.status === 401 && token) {
    cerrarSesion();
    window.location.href = "/login";
    return;
  }

  return respuesta;
}

// Igual que apiFetch, pero devuelve directamente el JSON.
// Si el back responde con error, lanza un Error con su "mensaje" para mostrarlo en pantalla.
export async function apiJson(path, opciones = {}) {
  let respuesta;
  try {
    respuesta = await apiFetch(path, opciones);
  } catch {
    throw new Error("No se pudo conectar con el servidor");
  }

  if (!respuesta) {
    throw new Error("Tu sesión venció, volvé a ingresar");
  }

  const datos = await respuesta.json().catch(() => null);

  if (!respuesta.ok) {
    throw new Error(datos?.mensaje || `Error ${respuesta.status}`);
  }

  return datos;
}
