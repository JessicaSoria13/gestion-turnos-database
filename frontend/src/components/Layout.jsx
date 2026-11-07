import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { obtenerRol, cerrarSesion } from '../api/cliente'

// Links del menu segun el rol con el que se ingreso
const enlacesPorRol = {
  PACIENTE: [
    { to: '/paciente/reservar', texto: 'Reservar turno' },
    { to: '/paciente/mis-turnos', texto: 'Mis turnos' },
  ],
  ADMINISTRADOR: [
    { to: '/admin/agenda', texto: 'Agenda' },
    { to: '/admin/pacientes', texto: 'Pacientes' },
    { to: '/admin/historial', texto: 'Historial clínico' },
  ],
}

const nombreRol = {
  PACIENTE: 'Paciente',
  ADMINISTRADOR: 'Administrador',
}

export default function Layout() {
  const navigate = useNavigate()
  const rol = obtenerRol()
  const enlaces = enlacesPorRol[rol] || []

  function salir() {
    cerrarSesion()
    navigate('/login')
  }

  return (
    <div>
      <header style={{ background: '#1e4fa3', color: 'white', padding: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
          <strong>NEXA Salud</strong>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <span>{nombreRol[rol]}</span>
            <button type="button" onClick={salir} style={{ padding: '0.3rem 0.6rem', cursor: 'pointer' }}>
              Cerrar sesión
            </button>
          </div>
        </div>
        <nav style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem', flexWrap: 'wrap' }}>
          {enlaces.map((e) => (
            <NavLink
              key={e.to}
              to={e.to}
              style={({ isActive }) => ({ color: 'white', fontWeight: isActive ? 'bold' : 'normal' })}
            >
              {e.texto}
            </NavLink>
          ))}
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
