import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiJson } from '../../api/cliente'

const estiloInput = { display: 'block', width: '100%', padding: '0.5rem', boxSizing: 'border-box' }
const DIAS = ['DOMINGO', 'LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO']

// Fecha de hoy en formato YYYY-MM-DD (el que usan el input date y el back)
function hoy() {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-${dia}`
}

// 2026-11-09 -> 09/11/2026
function formatearFecha(fecha) {
  return fecha.split('-').reverse().join('/')
}

// Busca la disponibilidad del medico que corresponde al dia de la semana y a la hora elegidos
function buscarDisponibilidad(disponibilidades, fecha, hora) {
  const dia = DIAS[new Date(`${fecha}T00:00`).getDay()]
  return disponibilidades.find((d) =>
    d.activo &&
    d.diaSemana === dia &&
    d.horaInicio.slice(0, 5) <= hora &&
    hora < d.horaFin.slice(0, 5)
  )
}

export default function ReservarTurno() {
  // Datos que se cargan al entrar
  const [paciente, setPaciente] = useState(null)
  const [medicos, setMedicos] = useState([])
  const [coberturas, setCoberturas] = useState([])
  const [financiadores, setFinanciadores] = useState({})

  // Lo que elige el paciente
  const [idMedico, setIdMedico] = useState('')
  const [fecha, setFecha] = useState('')
  const [hora, setHora] = useState('')
  const [idCobertura, setIdCobertura] = useState('')
  const [motivo, setMotivo] = useState('')

  // Horarios libres (null = todavia buscando) y disponibilidades del medico elegido
  const [horarios, setHorarios] = useState(null)
  const [disponibilidades, setDisponibilidades] = useState([])
  // Se incrementa despues de reservar, para volver a pedir los horarios libres
  const [version, setVersion] = useState(0)

  const [error, setError] = useState('')
  const [turnoReservado, setTurnoReservado] = useState(null)
  const [cargando, setCargando] = useState(false)

  // Al entrar: datos del paciente logueado, medicos, sus coberturas y los financiadores
  useEffect(() => {
    async function cargarInicio() {
      try {
        const yo = await apiJson('/pacientes/me')
        const [listaMedicos, listaCoberturas, listaFinanciadores] = await Promise.all([
          apiJson('/medicos'),
          apiJson(`/coberturas?idPaciente=${yo.id}`),
          apiJson('/financiadores'),
        ])

        const nombres = {}
        listaFinanciadores.forEach((f) => {
          nombres[f.id] = f.nombre
        })

        setPaciente(yo)
        setMedicos(listaMedicos.filter((m) => m.activo))
        setCoberturas(listaCoberturas.filter((c) => c.activo))
        setFinanciadores(nombres)
      } catch (err) {
        setError(err.message)
      }
    }
    cargarInicio()
  }, [])

  // Cada vez que cambia el medico o la fecha (o despues de reservar): horarios libres
  useEffect(() => {
    if (!idMedico || !fecha) return

    async function cargarHorarios() {
      try {
        const [libres, dispos] = await Promise.all([
          apiJson(`/medicos/${idMedico}/horarios-disponibles?fecha=${fecha}`),
          apiJson(`/disponibilidades?idMedico=${idMedico}`),
        ])
        setHorarios(libres)
        setDisponibilidades(dispos)
      } catch (err) {
        setError(err.message)
      }
    }
    cargarHorarios()
  }, [idMedico, fecha, version])

  function cambiarMedico(e) {
    setIdMedico(e.target.value)
    setHora('')
    setHorarios(null)
    setError('')
    setTurnoReservado(null)
  }

  function cambiarFecha(e) {
    setFecha(e.target.value)
    setHora('')
    setHorarios(null)
    setError('')
    setTurnoReservado(null)
  }

  async function reservar(e) {
    e.preventDefault()
    setError('')

    const disponibilidad = buscarDisponibilidad(disponibilidades, fecha, hora)
    if (!disponibilidad) {
      setError('No se encontró la disponibilidad del médico para ese horario')
      return
    }

    setCargando(true)
    try {
      const turno = await apiJson('/turnos', {
        method: 'POST',
        body: JSON.stringify({
          idPaciente: paciente.id,
          idMedico: Number(idMedico),
          idDisponibilidad: disponibilidad.id,
          idCobertura: idCobertura ? Number(idCobertura) : null,
          fecha,
          hora,
          motivoConsulta: motivo || null,
        }),
      })
      setTurnoReservado(turno)
      setHora('')
      setMotivo('')
      setVersion(version + 1)
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  if (!paciente) {
    return (
      <div style={{ maxWidth: '480px', margin: '2rem auto', padding: '0 1rem' }}>
        <h1>Reservar turno</h1>
        {error ? <p style={{ color: 'crimson' }}>{error}</p> : <p>Cargando...</p>}
      </div>
    )
  }

  return (
    <div style={{ maxWidth: '480px', margin: '2rem auto', padding: '0 1rem' }}>
      <h1>Reservar turno</h1>

      {turnoReservado && (
        <p style={{ color: 'green' }}>
          Turno reservado para el {formatearFecha(turnoReservado.fecha)} a las {turnoReservado.hora.slice(0, 5)}.{' '}
          <Link to="/paciente/mis-turnos">Ver mis turnos</Link>
        </p>
      )}

      <form onSubmit={reservar} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <label>
          Médico
          <select value={idMedico} onChange={cambiarMedico} required style={estiloInput}>
            <option value="">Elegí un médico</option>
            {medicos.map((m) => (
              <option key={m.id} value={m.id}>
                {m.nombre} ({m.especialidades.join(', ')})
              </option>
            ))}
          </select>
        </label>

        <label>
          Fecha
          <input type="date" value={fecha} min={hoy()} onChange={cambiarFecha} required style={estiloInput} />
        </label>

        {idMedico && fecha && (
          <div>
            <p style={{ margin: '0 0 0.5rem' }}>Horarios libres</p>
            {horarios === null && <p>Buscando horarios...</p>}
            {horarios !== null && horarios.length === 0 && (
              <p>No hay horarios libres ese día. Probá con otra fecha.</p>
            )}
            {horarios !== null && horarios.length > 0 && (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                {horarios.map((h) => (
                  <button
                    key={h}
                    type="button"
                    onClick={() => setHora(h)}
                    style={{
                      padding: '0.4rem 0.7rem',
                      background: h === hora ? '#1e4fa3' : 'transparent',
                      color: h === hora ? 'white' : 'inherit',
                      border: '1px solid #1e4fa3',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    {h}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        <label>
          Cobertura
          <select value={idCobertura} onChange={(e) => setIdCobertura(e.target.value)} style={estiloInput}>
            <option value="">Particular (sin cobertura)</option>
            {coberturas.map((c) => (
              <option key={c.id} value={c.id}>
                {financiadores[c.idFinanciador] || 'Cobertura'} · Plan {c.plan} · N° {c.numeroAfiliado}
              </option>
            ))}
          </select>
        </label>

        <label>
          Motivo de la consulta (opcional)
          <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} maxLength={255} rows={3} style={estiloInput} />
        </label>

        {error && <p style={{ color: 'crimson', margin: 0 }}>{error}</p>}

        <button type="submit" disabled={!hora || cargando} style={{ padding: '0.6rem' }}>
          {cargando ? 'Reservando...' : hora ? `Reservar turno de las ${hora}` : 'Elegí un horario'}
        </button>
      </form>
    </div>
  )
}
