import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiJson } from '../../api/cliente'

const colorEstado = {
  CONFIRMADO: '#1e7d32',
  CANCELADO: 'crimson',
  ATENDIDO: 'gray',
}

// Fecha de hoy en formato YYYY-MM-DD, para comparar con la del turno
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

export default function MisTurnos() {
  const [turnos, setTurnos] = useState(null)
  const [medicos, setMedicos] = useState({})
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  // Se incrementa despues de cancelar, para volver a pedir la lista
  const [version, setVersion] = useState(0)

  useEffect(() => {
    async function cargar() {
      try {
        const [listaTurnos, listaMedicos] = await Promise.all([
          apiJson('/turnos/mis-turnos'),
          apiJson('/medicos'),
        ])

        const nombres = {}
        listaMedicos.forEach((m) => {
          nombres[m.id] = m.nombre
        })

        setMedicos(nombres)
        setTurnos(listaTurnos)
      } catch (err) {
        setError(err.message)
      }
    }
    cargar()
  }, [version])

  async function cancelar(turno) {
    const confirmado = window.confirm(
      `¿Cancelar el turno del ${formatearFecha(turno.fecha)} a las ${turno.hora.slice(0, 5)}?`
    )
    if (!confirmado) return

    // null = toco "Cancelar" en el cuadro del motivo: no hacemos nada
    const motivo = window.prompt('Motivo de la cancelación (opcional):', '')
    if (motivo === null) return

    setError('')
    setAviso('')
    try {
      await apiJson(`/turnos/${turno.id}/cancelar`, {
        method: 'PUT',
        body: JSON.stringify({
          canceladoPorMedico: false,
          motivoCancelacion: motivo.trim() || null,
        }),
      })
      setAviso('Turno cancelado. El horario vuelve a estar disponible.')
      setVersion(version + 1)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 1rem' }}>
      <h1>Mis turnos</h1>

      {aviso && <p style={{ color: 'green' }}>{aviso}</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      {turnos === null && !error && <p>Cargando...</p>}

      {turnos !== null && turnos.length === 0 && (
        <p>
          Todavía no tenés turnos. <Link to="/paciente/reservar">Reservar uno</Link>
        </p>
      )}

      {turnos !== null && turnos.length > 0 && (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left', borderBottom: '2px solid #1e4fa3' }}>
                <th style={{ padding: '0.5rem' }}>Fecha</th>
                <th style={{ padding: '0.5rem' }}>Hora</th>
                <th style={{ padding: '0.5rem' }}>Médico</th>
                <th style={{ padding: '0.5rem' }}>Cobertura</th>
                <th style={{ padding: '0.5rem' }}>Estado</th>
                <th style={{ padding: '0.5rem' }}></th>
              </tr>
            </thead>
            <tbody>
              {turnos.map((t) => (
                <tr key={t.id} style={{ borderBottom: '1px solid #444' }}>
                  <td style={{ padding: '0.5rem' }}>{formatearFecha(t.fecha)}</td>
                  <td style={{ padding: '0.5rem' }}>{t.hora.slice(0, 5)}</td>
                  <td style={{ padding: '0.5rem' }}>{medicos[t.idMedico] || `Médico ${t.idMedico}`}</td>
                  <td style={{ padding: '0.5rem' }}>{t.idCobertura ? 'Con cobertura' : 'Particular'}</td>
                  <td style={{ padding: '0.5rem', color: colorEstado[t.estado] }}>
                    {t.estado}
                    {t.motivoCancelacion && (
                      <div style={{ fontSize: '0.85em', color: 'gray' }}>{t.motivoCancelacion}</div>
                    )}
                  </td>
                  <td style={{ padding: '0.5rem' }}>
                    {t.estado === 'CONFIRMADO' && t.fecha >= hoy() && (
                      <button type="button" onClick={() => cancelar(t)} style={{ padding: '0.3rem 0.6rem' }}>
                        Cancelar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
